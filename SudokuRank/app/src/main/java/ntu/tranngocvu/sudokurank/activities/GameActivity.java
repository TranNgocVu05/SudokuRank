package ntu.tranngocvu.sudokurank.activities;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import ntu.tranngocvu.sudokurank.R;

public class GameActivity extends AppCompatActivity {

    private GridLayout gridSudoku;
    private final TextView[][] cells = new TextView[9][9];

    private String puzzle;
    private String solution;
    private String difficulty;

    private int selectedRow = -1;
    private int selectedCol = -1;
    private int mistakes = 0;
    private int score = 0;
    private int seconds = 0;
    private int hintsUsed = 0;

    private boolean isPaused = false;
    private boolean isNoteMode = false;
    private boolean isGameEnded = false;

    private TextView tvScore, tvMistakes, tvTime, tvHintCount, tvNotesStatus;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private Runnable timerRunnable;

    private final Stack<Move> moveHistory = new Stack<>();

    private static class Move {
        int row, col, value;

        Move(int row, int col, int value) {
            this.row = row;
            this.col = col;
            this.value = value;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        puzzle = getIntent().getStringExtra("LEVEL_DATA");
        solution = getIntent().getStringExtra("LEVEL_SOLUTION");
        difficulty = getIntent().getStringExtra("LEVEL_DIFFICULTY");

        if (puzzle != null) puzzle = puzzle.trim();
        if (solution != null) solution = solution.trim();

        if (puzzle == null || solution == null || puzzle.length() != 81 || solution.length() != 81) {
            Toast.makeText(this, "Dữ liệu màn chơi không hợp lệ!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        gridSudoku = findViewById(R.id.gridSudoku);
        tvScore = findViewById(R.id.tvScore);
        tvMistakes = findViewById(R.id.tvMistakes);
        tvTime = findViewById(R.id.tvTime);
        tvHintCount = findViewById(R.id.tvHintCount);
        tvNotesStatus = findViewById(R.id.tvNotesStatus);

        gridSudoku.setRowCount(9);
        gridSudoku.setColumnCount(9);

        updateTopInfo();

        setupGrid();
        setupNumberPad();
        setupToolbar();
        startTimer();

        findViewById(R.id.btnPause).setOnClickListener(v -> {
            if (isGameEnded) return;

            isPaused = !isPaused;
            Toast.makeText(this, isPaused ? "Đã tạm dừng" : "Tiếp tục", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupGrid() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cellSize = (screenWidth - 80) / 9;

        gridSudoku.removeAllViews();

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                TextView tv = new TextView(this);
                cells[r][c] = tv;

                char val = puzzle.charAt(r * 9 + c);

                tv.setGravity(Gravity.CENTER);
                tv.setTextSize(18);
                tv.setTypeface(null, Typeface.BOLD);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        GridLayout.spec(r),
                        GridLayout.spec(c)
                );

                params.width = cellSize;
                params.height = cellSize;

                int left = (c % 3 == 0) ? 4 : 1;
                int top = (r % 3 == 0) ? 4 : 1;
                int right = (c == 8) ? 4 : 1;
                int bottom = (r == 8) ? 4 : 1;

                params.setMargins(left, top, right, bottom);
                tv.setLayoutParams(params);

                if (val != '0') {
                    tv.setText(String.valueOf(val));
                    tv.setTextColor(Color.BLACK);
                    tv.setBackgroundColor(Color.parseColor("#E3EAF2"));
                } else {
                    tv.setText("");
                    tv.setTextColor(ContextCompat.getColor(this, R.color.sudoku_blue_primary));
                    tv.setBackgroundColor(Color.WHITE);

                    final int row = r;
                    final int col = c;

                    tv.setOnClickListener(v -> selectCell(row, col));
                }

                gridSudoku.addView(tv);
            }
        }
    }

    private void selectCell(int row, int col) {
        if (isPaused || isGameEnded) return;

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (puzzle.charAt(r * 9 + c) == '0') {
                    cells[r][c].setBackgroundColor(Color.WHITE);
                }
            }
        }

        selectedRow = row;
        selectedCol = col;
        cells[row][col].setBackgroundColor(Color.parseColor("#BBDEFB"));
    }

    private void setupNumberPad() {
        int[] btnIds = {
                R.id.btn1, R.id.btn2, R.id.btn3,
                R.id.btn4, R.id.btn5, R.id.btn6,
                R.id.btn7, R.id.btn8, R.id.btn9
        };

        for (int i = 0; i < 9; i++) {
            final int number = i + 1;
            findViewById(btnIds[i]).setOnClickListener(v -> handleInput(number));
        }
    }

    private void handleInput(int number) {
        if (isPaused || isGameEnded) return;

        if (selectedRow == -1 || selectedCol == -1) {
            Toast.makeText(this, "Hãy chọn một ô trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (puzzle.charAt(selectedRow * 9 + selectedCol) != '0') {
            Toast.makeText(this, "Không thể sửa ô đề bài!", Toast.LENGTH_SHORT).show();
            return;
        }

        char correctChar = solution.charAt(selectedRow * 9 + selectedCol);

        if (String.valueOf(number).equals(String.valueOf(correctChar))) {
            cells[selectedRow][selectedCol].setText(String.valueOf(number));
            cells[selectedRow][selectedCol].setTextColor(ContextCompat.getColor(this, R.color.sudoku_blue_primary));
            cells[selectedRow][selectedCol].setBackgroundColor(Color.parseColor("#E3F2FD"));
            cells[selectedRow][selectedCol].setOnClickListener(null);

            moveHistory.push(new Move(selectedRow, selectedCol, number));

            score += 10;
            updateTopInfo();

            selectedRow = -1;
            selectedCol = -1;

            checkWin();
        } else {
            mistakes++;
            updateTopInfo();

            cells[selectedRow][selectedCol].setBackgroundColor(Color.parseColor("#FFCDD2"));

            if (mistakes >= 3) {
                gameOver();
            } else {
                Toast.makeText(this, "Sai rồi! Còn " + (3 - mistakes) + " mạng.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupToolbar() {
        findViewById(R.id.layoutUndo).setOnClickListener(v -> undoMove());

        findViewById(R.id.layoutErase).setOnClickListener(v -> eraseSelectedCell());

        findViewById(R.id.layoutNotes).setOnClickListener(v -> {
            if (isGameEnded) return;

            isNoteMode = !isNoteMode;

            if (tvNotesStatus != null) {
                tvNotesStatus.setText(isNoteMode ? "Ghi chú: Bật" : "Ghi chú: Tắt");
            }

            Toast.makeText(this, isNoteMode ? "Đã bật ghi chú" : "Đã tắt ghi chú", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.layoutHint).setOnClickListener(v -> {
            if (isPaused || isGameEnded) return;

            if (hintsUsed < 3) {
                fillOneHint();
            } else {
                Toast.makeText(this, "Hết lượt gợi ý!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void undoMove() {
        if (isPaused || isGameEnded) return;

        if (moveHistory.isEmpty()) {
            Toast.makeText(this, "Không có bước để hoàn tác!", Toast.LENGTH_SHORT).show();
            return;
        }

        Move lastMove = moveHistory.pop();

        cells[lastMove.row][lastMove.col].setText("");
        cells[lastMove.row][lastMove.col].setBackgroundColor(Color.WHITE);

        final int row = lastMove.row;
        final int col = lastMove.col;

        cells[row][col].setOnClickListener(v -> selectCell(row, col));

        score = Math.max(0, score - 10);
        updateTopInfo();
    }

    private void eraseSelectedCell() {
        if (isPaused || isGameEnded) return;

        if (selectedRow == -1 || selectedCol == -1) {
            Toast.makeText(this, "Hãy chọn ô cần xóa!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (puzzle.charAt(selectedRow * 9 + selectedCol) == '0') {
            cells[selectedRow][selectedCol].setText("");
        }
    }

    private void fillOneHint() {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (puzzle.charAt(r * 9 + c) == '0'
                        && cells[r][c].getText().toString().trim().isEmpty()) {

                    char correct = solution.charAt(r * 9 + c);

                    cells[r][c].setText(String.valueOf(correct));
                    cells[r][c].setTextColor(Color.parseColor("#4CAF50"));
                    cells[r][c].setBackgroundColor(Color.parseColor("#E8F5E9"));
                    cells[r][c].setOnClickListener(null);

                    hintsUsed++;

                    if (tvHintCount != null) {
                        tvHintCount.setText(String.format(Locale.getDefault(), "Gợi ý (%d)", 3 - hintsUsed));
                    }

                    checkWin();
                    return;
                }
            }
        }
    }

    private void checkWin() {
        if (isGameEnded) return;

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                String value = cells[r][c].getText().toString().trim();

                if (value.isEmpty()) {
                    return;
                }

                char correct = solution.charAt(r * 9 + c);

                if (!value.equals(String.valueOf(correct))) {
                    return;
                }
            }
        }

        isGameEnded = true;
        stopTimer();

        saveResultToFirebase();

        Toast.makeText(this, "XUẤT SẮC! Bạn đã thắng!", Toast.LENGTH_LONG).show();
        finish();
    }

    private void gameOver() {
        if (isGameEnded) return;

        isGameEnded = true;
        stopTimer();

        Toast.makeText(this, "GAME OVER! Bạn đã mắc 3 lỗi.", Toast.LENGTH_LONG).show();
        finish();
    }

    private void updateTopInfo() {
        if (tvScore != null) {
            tvScore.setText(String.format(Locale.getDefault(), "Điểm: %d", score));
        }

        if (tvMistakes != null) {
            tvMistakes.setText(String.format(Locale.getDefault(), "Lỗi: %d/3", mistakes));
        }
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isGameEnded && !isFinishing()) {
                    if (!isPaused) {
                        seconds++;

                        int minutes = seconds / 60;
                        int second = seconds % 60;

                        if (tvTime != null) {
                            tvTime.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, second));
                        }
                    }

                    timerHandler.postDelayed(this, 1000);
                }
            }
        };

        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void stopTimer() {
        if (timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }

        timerHandler.removeCallbacksAndMessages(null);
    }

    private void saveResultToFirebase() {
        String uid = FirebaseAuth.getInstance().getUid();

        if (uid == null) {
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    String username = "Người chơi";

                    if (doc.exists() && doc.getString("name") != null) {
                        username = doc.getString("name");
                    }

                    Map<String, Object> scoreData = new HashMap<>();
                    scoreData.put("userId", uid);
                    scoreData.put("username", username);
                    scoreData.put("score", (long) score);
                    scoreData.put("time", (long) seconds);
                    scoreData.put("mistakes", (long) mistakes);
                    scoreData.put("difficulty", difficulty == null ? "Easy" : difficulty);
                    scoreData.put("mode", "world");
                    scoreData.put("createdAt", FieldValue.serverTimestamp());

                    db.collection("scores").add(scoreData);

                    Long bestScore = doc.getLong("bestScore");

                    if (bestScore == null || score > bestScore) {
                        db.collection("users")
                                .document(uid)
                                .update("bestScore", (long) score);
                    }

                    db.collection("users")
                            .document(uid)
                            .update("totalGames", FieldValue.increment(1));
                });
    }

    @Override
    protected void onDestroy() {
        stopTimer();
        super.onDestroy();
    }
}