package ntu.tranngocvu.sudokurank.activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import ntu.tranngocvu.sudokurank.R;
import ntu.tranngocvu.sudokurank.models.Progress;
import ntu.tranngocvu.sudokurank.models.User;

public class GameActivity extends AppCompatActivity {
    private GridLayout gridSudoku;
    private TextView[][] cells = new TextView[9][9];
    private String puzzle, solution, currentBoardStr;
    private String difficulty;
    private long levelId;
    private int selectedRow = -1, selectedCol = -1;
    private int mistakes = 0, score = 0, seconds = 0;
    private boolean isPaused = false, isNoteMode = false;
    private TextView tvScore, tvMistakes, tvTime, tvTitle, tvNoteStatus;
    private ImageView[] hearts = new ImageView[3];
    private Handler timerHandler = new Handler();
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews();
        handleIntent();
        setupGrid();
        setupNumberPad();
        setupToolbar();
        startTimer();
    }

    private void initViews() {
        gridSudoku = findViewById(R.id.gridSudoku);
        tvScore = findViewById(R.id.tvScore);
        tvMistakes = findViewById(R.id.tvMistakes);
        tvTime = findViewById(R.id.tvTime);
        tvTitle = findViewById(R.id.tvGameTitle);
        tvNoteStatus = findViewById(R.id.tvNoteStatus);
        hearts[0] = findViewById(R.id.heart1);
        hearts[1] = findViewById(R.id.heart2);
        hearts[2] = findViewById(R.id.heart3);

        findViewById(R.id.btnBack).setOnClickListener(v -> handleExit());
        findViewById(R.id.btnExit).setOnClickListener(v -> handleExit());
        findViewById(R.id.btnPause).setOnClickListener(v -> {
            isPaused = !isPaused;
            Toast.makeText(this, isPaused ? "Đã tạm dừng" : "Tiếp tục", Toast.LENGTH_SHORT).show();
        });
    }

    private void handleIntent() {
        puzzle = getIntent().getStringExtra("PUZZLE");
        solution = getIntent().getStringExtra("SOLUTION");
        levelId = getIntent().getLongExtra("LEVEL", 1);
        difficulty = getIntent().getStringExtra("DIFFICULTY");
        
        if (getIntent().getBooleanExtra("CONTINUE", false)) {
            currentBoardStr = getIntent().getStringExtra("CURRENT_BOARD");
            score = (int) getIntent().getLongExtra("SCORE", 0);
            mistakes = (int) getIntent().getLongExtra("MISTAKES", 0);
            seconds = (int) getIntent().getLongExtra("TIME", 0);
        } else {
            currentBoardStr = puzzle;
        }
        
        if (difficulty == null) difficulty = "Easy";
        if (tvTitle != null) tvTitle.setText(String.format(Locale.getDefault(), "Màn %d - %s", levelId, difficulty));
        if (tvScore != null) tvScore.setText(String.valueOf(score));
        updateMistakesUI();
    }

    private void setupGrid() {
        if (puzzle == null || currentBoardStr == null) return;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cellSize = (screenWidth - 60) / 9;

        gridSudoku.removeAllViews();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                TextView tv = new TextView(this);
                cells[r][c] = tv;
                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(18);
                
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                
                int right = (c % 3 == 2 && c != 8) ? 4 : 1;
                int bottom = (r % 3 == 2 && r != 8) ? 4 : 1;
                params.setMargins(1, 1, right, bottom);
                tv.setLayoutParams(params);

                char pVal = puzzle.charAt(r * 9 + c);
                char cVal = currentBoardStr.charAt(r * 9 + c);

                if (pVal != '0') {
                    tv.setText(String.valueOf(pVal));
                    tv.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    tv.setTextColor(Color.BLACK);
                    tv.setTypeface(null, Typeface.BOLD);
                } else {
                    if (cVal != '0') tv.setText(String.valueOf(cVal));
                    tv.setBackgroundColor(Color.WHITE);
                    tv.setTextColor(getResources().getColor(R.color.sudoku_primary));
                    final int finalR = r, finalC = c;
                    tv.setOnClickListener(v -> selectCell(finalR, finalC));
                }
                gridSudoku.addView(tv);
            }
        }
    }

    private void selectCell(int r, int c) {
        if (isPaused) return;
        if (selectedRow != -1) {
            if (puzzle.charAt(selectedRow * 9 + selectedCol) == '0') {
                cells[selectedRow][selectedCol].setBackgroundColor(Color.WHITE);
            } else {
                cells[selectedRow][selectedCol].setBackgroundColor(Color.parseColor("#EEEEEE"));
            }
        }
        selectedRow = r; selectedCol = c;
        cells[r][c].setBackgroundColor(getResources().getColor(R.color.sudoku_primary_light));
    }

    private void setupNumberPad() {
        int[] ids = {R.id.btnNum1, R.id.btnNum2, R.id.btnNum3, R.id.btnNum4, R.id.btnNum5, R.id.btnNum6, R.id.btnNum7, R.id.btnNum8, R.id.btnNum9};
        for (int i = 0; i < 9; i++) {
            final int num = i + 1;
            View btn = findViewById(ids[i]);
            if (btn != null) btn.setOnClickListener(v -> handleInput(num));
        }
    }

    private void handleInput(int num) {
        if (selectedRow == -1 || isPaused) return;
        if (isNoteMode) {
            Toast.makeText(this, "Ghi chú: " + num, Toast.LENGTH_SHORT).show();
            return;
        }

        if (solution == null) return;
        char correct = solution.charAt(selectedRow * 9 + selectedCol);
        String inputStr = String.valueOf(num);
        
        if (inputStr.equals(String.valueOf(correct))) {
            cells[selectedRow][selectedCol].setText(inputStr);
            cells[selectedRow][selectedCol].setTextColor(getResources().getColor(R.color.sudoku_primary));
            score += 10;
            if (tvScore != null) tvScore.setText(String.valueOf(score));
            saveProgress();
            checkWin();
        } else {
            mistakes++;
            updateMistakesUI();
            cells[selectedRow][selectedCol].setBackgroundColor(getResources().getColor(R.color.error_red));
            new Handler().postDelayed(() -> {
                if (selectedRow != -1) {
                   cells[selectedRow][selectedCol].setBackgroundColor(getResources().getColor(R.color.sudoku_primary_light));
                }
            }, 500);
            if (mistakes >= 3) handleLose();
        }
    }

    private void updateMistakesUI() {
        if (tvMistakes != null) tvMistakes.setText(String.format(Locale.getDefault(), "Lỗi: %d/3", mistakes));
        for (int i = 0; i < 3; i++) {
            if (hearts[i] != null) {
                if (i < mistakes) {
                    hearts[i].setImageResource(android.R.drawable.btn_star_big_off);
                } else {
                    hearts[i].setImageResource(android.R.drawable.btn_star_big_on);
                }
            }
        }
    }

    private void setupToolbar() {
        View hint = findViewById(R.id.layoutHint);
        if (hint != null) hint.setOnClickListener(v -> {
            if (isPaused || solution == null) return;
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (cells[r][c].getText().toString().isEmpty()) {
                        cells[r][c].setText(String.valueOf(solution.charAt(r * 9 + c)));
                        checkWin();
                        return;
                    }
                }
            }
        });

        View undo = findViewById(R.id.layoutUndo);
        if (undo != null) undo.setOnClickListener(v -> Toast.makeText(this, "Hoàn tác", Toast.LENGTH_SHORT).show());

        View note = findViewById(R.id.layoutNote);
        if (note != null) note.setOnClickListener(v -> {
            isNoteMode = !isNoteMode;
            if (tvNoteStatus != null) tvNoteStatus.setText("Ghi chú: " + (isNoteMode ? "Bật" : "Tắt"));
        });

        View erase = findViewById(R.id.layoutErase);
        if (erase != null) erase.setOnClickListener(v -> {
            if (selectedRow != -1 && puzzle != null && puzzle.charAt(selectedRow * 9 + selectedCol) == '0') {
                cells[selectedRow][selectedCol].setText("");
            }
        });
    }

    private void startTimer() {
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isPaused) {
                    seconds++;
                    int m = seconds / 60, s = seconds % 60;
                    if (tvTime != null) tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", m, s));
                }
                timerHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void saveProgress() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                String val = cells[r][c].getText().toString();
                sb.append(val.isEmpty() ? "0" : val);
            }
        }
        currentBoardStr = sb.toString();
        
        String uid = mAuth.getUid();
        if (uid == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("userId", uid);
        data.put("difficulty", difficulty);
        data.put("currentLevel", levelId);
        data.put("currentBoard", currentBoardStr);
        data.put("puzzle", puzzle);
        data.put("solution", solution);
        data.put("score", (long)score);
        data.put("mistakes", (long)mistakes);
        data.put("time", (long)seconds);
        data.put("completed", false);

        db.collection("progress").document(uid).set(data);
    }

    private void checkWin() {
        boolean win = true;
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (cells[r][c].getText().toString().isEmpty()) win = false;
            }
        }
        if (win) handleWin();
    }

    private void handleWin() {
        timerHandler.removeCallbacksAndMessages(null);
        String uid = mAuth.getUid();
        if (uid == null) return;
        db.collection("progress").document(uid).update("completed", true);
        
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            User user = doc.toObject(User.class);
            if (user != null) {
                if (score > user.bestScore) db.collection("users").document(uid).update("bestScore", (long)score);
                db.collection("users").document(uid).update(
                    "winStreak", user.winStreak + 1,
                    "loseStreak", 0L,
                    "totalGames", user.totalGames + 1
                );
                
                Map<String, Object> s = new HashMap<>();
                s.put("userId", uid);
                s.put("username", user.name);
                s.put("avatar", user.avatar);
                s.put("rankImage", user.rankImage);
                s.put("score", (long)score);
                s.put("time", (long)seconds);
                s.put("mistakes", (long)mistakes);
                s.put("difficulty", difficulty);
                s.put("createdAt", FieldValue.serverTimestamp());
                db.collection("scores").add(s);

                showWinDialog(user.winStreak + 1);
            }
        });
    }

    private void showWinDialog(long currentWinStreak) {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_win_dialog, null);
        TextView tvWinMessage = view.findViewById(R.id.tvWinMessage);
        TextView tvWinScore = view.findViewById(R.id.tvWinScore);
        TextView tvWinTime = view.findViewById(R.id.tvWinTime);
        TextView tvWinMistakes = view.findViewById(R.id.tvWinMistakes);
        TextView tvWinStreak = view.findViewById(R.id.tvWinStreak);
        
        if (tvWinMessage != null) tvWinMessage.setText(String.format(Locale.getDefault(), "Bạn đã hoàn thành\nMàn %d - %s", levelId, difficulty));
        if (tvWinScore != null) tvWinScore.setText(String.valueOf(score));
        int m = seconds / 60, s = seconds % 60;
        if (tvWinTime != null) tvWinTime.setText(String.format(Locale.getDefault(), "%02d:%02d", m, s));
        if (tvWinMistakes != null) tvWinMistakes.setText(String.valueOf(mistakes));
        if (tvWinStreak != null) tvWinStreak.setText(String.format(Locale.getDefault(), "🔥 Chuỗi thắng hiện tại: %d", currentWinStreak));

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialog).setView(view).setCancelable(false).create();
        View btnNext = view.findViewById(R.id.btnWinNext);
        if (btnNext != null) btnNext.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
        dialog.show();
    }

    private void handleLose() {
        timerHandler.removeCallbacksAndMessages(null);
        String uid = mAuth.getUid();
        if (uid == null) return;
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            Long loses = doc.getLong("loseStreak");
            if (loses == null) loses = 0L;
            db.collection("users").document(uid).update(
                "loseStreak", loses + 1,
                "winStreak", 0L
            );
            showLoseDialog(loses + 1);
        });
    }

    private void showLoseDialog(long currentLoseStreak) {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_lose_dialog, null);
        TextView tvLoseScore = view.findViewById(R.id.tvLoseScore);
        TextView tvLoseTime = view.findViewById(R.id.tvLoseTime);
        TextView tvLoseMistakes = view.findViewById(R.id.tvLoseMistakes);
        TextView tvLoseStreak = view.findViewById(R.id.tvLoseStreak);

        if (tvLoseScore != null) tvLoseScore.setText(String.valueOf(score));
        int m = seconds / 60, s = seconds % 60;
        if (tvLoseTime != null) tvLoseTime.setText(String.format(Locale.getDefault(), "%02d:%02d", m, s));
        if (tvLoseMistakes != null) tvLoseMistakes.setText(String.valueOf(mistakes));
        if (tvLoseStreak != null) tvLoseStreak.setText(String.format(Locale.getDefault(), "❄️ Chuỗi thua hiện tại: %d", currentLoseStreak));

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialog).setView(view).setCancelable(false).create();
        View btnRestart = view.findViewById(R.id.btnLoseRestart);
        if (btnRestart != null) btnRestart.setOnClickListener(v -> {
            dialog.dismiss();
            recreate();
        });
        View btnHome = view.findViewById(R.id.btnLoseToHome);
        if (btnHome != null) btnHome.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
        dialog.show();
    }

    private void handleExit() {
        if (!isPaused) {
            Toast.makeText(this, "Vui lòng tạm dừng trước khi thoát", Toast.LENGTH_SHORT).show();
        } else {
            saveProgress();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        handleExit();
    }
}
