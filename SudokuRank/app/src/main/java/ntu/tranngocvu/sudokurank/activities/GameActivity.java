package ntu.tranngocvu.sudokurank.activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ntu.tranngocvu.sudokurank.R;
import ntu.tranngocvu.sudokurank.models.User;

public class GameActivity extends AppCompatActivity {

    private GridLayout gridSudoku;
    private TextView[][] cells = new TextView[9][9];

    private String puzzle, solution, currentBoardStr, difficulty;
    private int levelId;

    private int selectedRow = -1, selectedCol = -1;
    private int mistakes = 0, score = 0, seconds = 0;
    private int hintsUsed = 0;

    private boolean isPaused = false;
    private boolean isNoteMode = false;
    private boolean isGameEnded = false;

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

        if (puzzle == null || solution == null || currentBoardStr == null
                || puzzle.length() != 81
                || solution.length() != 81
                || currentBoardStr.length() != 81) {
            Toast.makeText(this, "Dữ liệu màn chơi bị lỗi", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

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
            if (isGameEnded) return;
            isPaused = !isPaused;
            Toast.makeText(this, isPaused ? "Đã tạm dừng" : "Tiếp tục", Toast.LENGTH_SHORT).show();
        });
    }

    private void handleIntent() {
        puzzle = getIntent().getStringExtra("PUZZLE");
        solution = getIntent().getStringExtra("SOLUTION");
        difficulty = getIntent().getStringExtra("DIFFICULTY");
        levelId = getIntent().getIntExtra("LEVEL", 1);

        if (getIntent().getBooleanExtra("CONTINUE", false)) {
            currentBoardStr = getIntent().getStringExtra("CURRENT_BOARD");
            score = (int) getIntent().getLongExtra("SCORE", 0);
            mistakes = (int) getIntent().getLongExtra("MISTAKES", 0);
            seconds = (int) getIntent().getLongExtra("TIME", 0);
        } else {
            currentBoardStr = puzzle;
        }

        if (difficulty == null) difficulty = "Easy";

        tvTitle.setText(String.format(Locale.getDefault(), "Màn %d - %s", levelId, difficulty));
        tvScore.setText(String.valueOf(score));
        updateMistakesUI();
    }

    private void setupGrid() {
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
                    if (cVal != '0') {
                        tv.setText(String.valueOf(cVal));
                    }

                    tv.setBackgroundColor(Color.WHITE);
                    tv.setTextColor(getResources().getColor(R.color.sudoku_primary));

                    final int finalR = r;
                    final int finalC = c;
                    tv.setOnClickListener(v -> selectCell(finalR, finalC));
                }

                gridSudoku.addView(tv);
            }
        }
    }

    private void selectCell(int r, int c) {
        if (isPaused || isGameEnded) return;

        if (selectedRow != -1 && puzzle.charAt(selectedRow * 9 + selectedCol) == '0') {
            cells[selectedRow][selectedCol].setBackgroundColor(Color.WHITE);
        }

        selectedRow = r;
        selectedCol = c;
        cells[r][c].setBackgroundColor(getResources().getColor(R.color.sudoku_primary_light));
    }

    private void setupNumberPad() {
        int[] ids = {
                R.id.btnNum1, R.id.btnNum2, R.id.btnNum3,
                R.id.btnNum4, R.id.btnNum5, R.id.btnNum6,
                R.id.btnNum7, R.id.btnNum8, R.id.btnNum9
        };

        for (int i = 0; i < 9; i++) {
            final int num = i + 1;
            View btn = findViewById(ids[i]);
            if (btn != null) {
                btn.setOnClickListener(v -> handleInput(num));
            }
        }
    }

    private void handleInput(int num) {
        if (selectedRow == -1 || selectedCol == -1 || isPaused || isGameEnded) return;

        if (isNoteMode) {
            Toast.makeText(this, "Ghi chú: " + num, Toast.LENGTH_SHORT).show();
            return;
        }

        char correct = solution.charAt(selectedRow * 9 + selectedCol);

        if (String.valueOf(num).equals(String.valueOf(correct))) {
            cells[selectedRow][selectedCol].setText(String.valueOf(num));
            cells[selectedRow][selectedCol].setTextColor(getResources().getColor(R.color.sudoku_primary));
            cells[selectedRow][selectedCol].setBackgroundColor(Color.parseColor("#E3F2FD"));

            score += 10;
            tvScore.setText(String.valueOf(score));

            saveProgress();
            checkWin();
        } else {
            mistakes++;
            updateMistakesUI();

            cells[selectedRow][selectedCol].setBackgroundColor(getResources().getColor(R.color.error_red));

            new Handler().postDelayed(() -> {
                if (!isGameEnded && selectedRow != -1 && selectedCol != -1) {
                    cells[selectedRow][selectedCol].setBackgroundColor(getResources().getColor(R.color.sudoku_primary_light));
                }
            }, 500);

            saveProgress();

            if (mistakes >= 3) {
                handleLose();
            }
        }
    }

    private void updateMistakesUI() {
        tvMistakes.setText(String.format(Locale.getDefault(), "Lỗi: %d/3", mistakes));

        for (int i = 0; i < 3; i++) {
            if (hearts[i] != null) {
                hearts[i].setImageResource(i < mistakes
                        ? android.R.drawable.btn_star_big_off
                        : android.R.drawable.btn_star_big_on);
            }
        }
    }

    private void setupToolbar() {
        View hint = findViewById(R.id.layoutHint);
        if (hint != null) {
            hint.setOnClickListener(v -> {
                if (isPaused || isGameEnded) return;

                if (hintsUsed >= 3) {
                    Toast.makeText(this, "Bạn đã hết lượt gợi ý", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (int r = 0; r < 9; r++) {
                    for (int c = 0; c < 9; c++) {
                        if (puzzle.charAt(r * 9 + c) == '0'
                                && cells[r][c].getText().toString().isEmpty()) {

                            cells[r][c].setText(String.valueOf(solution.charAt(r * 9 + c)));
                            cells[r][c].setTextColor(Color.parseColor("#4CAF50"));

                            hintsUsed++;
                            saveProgress();
                            checkWin();
                            return;
                        }
                    }
                }
            });
        }

        View undo = findViewById(R.id.layoutUndo);
        if (undo != null) {
            undo.setOnClickListener(v -> Toast.makeText(this, "Hoàn tác đang phát triển", Toast.LENGTH_SHORT).show());
        }

        View note = findViewById(R.id.layoutNote);
        if (note != null) {
            note.setOnClickListener(v -> {
                isNoteMode = !isNoteMode;
                tvNoteStatus.setText("Ghi chú: " + (isNoteMode ? "Bật" : "Tắt"));
            });
        }

        View erase = findViewById(R.id.layoutErase);
        if (erase != null) {
            erase.setOnClickListener(v -> {
                if (selectedRow != -1 && selectedCol != -1
                        && puzzle.charAt(selectedRow * 9 + selectedCol) == '0') {
                    cells[selectedRow][selectedCol].setText("");
                    saveProgress();
                }
            });
        }
    }

    private void startTimer() {
        timerHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isPaused && !isGameEnded) {
                    seconds++;
                    int m = seconds / 60;
                    int s = seconds % 60;
                    tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", m, s));
                }

                if (!isGameEnded) {
                    timerHandler.postDelayed(this, 1000);
                }
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
        data.put("currentLevel", (long) levelId);
        data.put("currentBoard", currentBoardStr);
        data.put("puzzle", puzzle);
        data.put("solution", solution);
        data.put("score", (long) score);
        data.put("mistakes", (long) mistakes);
        data.put("time", (long) seconds);
        data.put("completed", false);

        db.collection("progress").document(uid).set(data);
    }

    private void checkWin() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                String value = cells[r][c].getText().toString();

                if (value.isEmpty()) return;

                if (!value.equals(String.valueOf(solution.charAt(r * 9 + c)))) {
                    return;
                }
            }
        }

        handleWin();
    }

    private void handleWin() {
        if (isGameEnded) return;

        isGameEnded = true;
        timerHandler.removeCallbacksAndMessages(null);

        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("progress").document(uid).update("completed", true);

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            User user = doc.toObject(User.class);

            if (user == null) {
                Toast.makeText(this, "Không tìm thấy user", Toast.LENGTH_SHORT).show();
                return;
            }

            long newWinStreak = user.winStreak + 1;
            long newBestScore = Math.max(user.bestScore, score);

            Map<String, Object> updateUser = new HashMap<>();
            updateUser.put("bestScore", newBestScore);
            updateUser.put("winStreak", newWinStreak);
            updateUser.put("loseStreak", 0L);
            updateUser.put("totalGames", FieldValue.increment(1));

            db.collection("users").document(uid).update(updateUser);

            Map<String, Object> scoreData = new HashMap<>();
            scoreData.put("userId", uid);
            scoreData.put("username", user.name);
            scoreData.put("avatar", user.avatar);
            scoreData.put("rankImage", user.rankImage);
            scoreData.put("score", (long) score);
            scoreData.put("time", (long) seconds);
            scoreData.put("mistakes", (long) mistakes);
            scoreData.put("difficulty", difficulty);
            scoreData.put("mode", "world");
            scoreData.put("createdAt", FieldValue.serverTimestamp());

            db.collection("scores").add(scoreData);

            showWinDialog(newWinStreak);
        });
    }

    private void showWinDialog(long currentWinStreak) {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_win_dialog, null);

        TextView tvWinMessage = view.findViewById(R.id.tvWinMessage);
        TextView tvWinScore = view.findViewById(R.id.tvWinScore);
        TextView tvWinTime = view.findViewById(R.id.tvWinTime);
        TextView tvWinMistakes = view.findViewById(R.id.tvWinMistakes);
        TextView tvWinStreak = view.findViewById(R.id.tvWinStreak);

        tvWinMessage.setText(String.format(Locale.getDefault(), "Bạn đã hoàn thành\nMàn %d - %s", levelId, difficulty));
        tvWinScore.setText(String.valueOf(score));

        int m = seconds / 60;
        int s = seconds % 60;

        tvWinTime.setText(String.format(Locale.getDefault(), "%02d:%02d", m, s));
        tvWinMistakes.setText(String.valueOf(mistakes));
        tvWinStreak.setText(String.format(Locale.getDefault(), "🔥 Chuỗi thắng hiện tại: %d", currentWinStreak));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        View btnNext = view.findViewById(R.id.btnWinNext);
        btnNext.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    private void handleLose() {
        if (isGameEnded) return;

        isGameEnded = true;
        timerHandler.removeCallbacksAndMessages(null);

        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            Long loses = doc.getLong("loseStreak");
            if (loses == null) loses = 0L;

            long newLoseStreak = loses + 1;

            db.collection("users").document(uid).update(
                    "loseStreak", newLoseStreak,
                    "winStreak", 0L,
                    "totalGames", FieldValue.increment(1)
            );

            showLoseDialog(newLoseStreak);
        });
    }

    private void showLoseDialog(long currentLoseStreak) {
        View view = LayoutInflater.from(this).inflate(R.layout.layout_lose_dialog, null);

        TextView tvLoseScore = view.findViewById(R.id.tvLoseScore);
        TextView tvLoseTime = view.findViewById(R.id.tvLoseTime);
        TextView tvLoseMistakes = view.findViewById(R.id.tvLoseMistakes);
        TextView tvLoseStreak = view.findViewById(R.id.tvLoseStreak);

        tvLoseScore.setText(String.valueOf(score));

        int m = seconds / 60;
        int s = seconds % 60;

        tvLoseTime.setText(String.format(Locale.getDefault(), "%02d:%02d", m, s));
        tvLoseMistakes.setText(String.valueOf(mistakes));
        tvLoseStreak.setText(String.format(Locale.getDefault(), "❄️ Chuỗi thua hiện tại: %d", currentLoseStreak));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create();

        View btnRestart = view.findViewById(R.id.btnLoseRestart);
        btnRestart.setOnClickListener(v -> {
            dialog.dismiss();
            recreate();
        });

        View btnHome = view.findViewById(R.id.btnLoseToHome);
        btnHome.setOnClickListener(v -> {
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
    @SuppressWarnings("deprecation")
    public void onBackPressed() {
        handleExit();
    }

    @Override
    protected void onDestroy() {
        timerHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}