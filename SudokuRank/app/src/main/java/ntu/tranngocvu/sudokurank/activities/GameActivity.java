package ntu.tranngocvu.sudokurank.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
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
import java.util.Stack;

import ntu.tranngocvu.sudokurank.R;
import ntu.tranngocvu.sudokurank.models.User;
import ntu.tranngocvu.sudokurank.views.SudokuBoardView;

public class GameActivity extends AppCompatActivity {

    private SudokuBoardView boardSudoku;

    private String puzzle, solution, currentBoardStr, difficulty;
    private int levelId;

    private int selectedRow = -1, selectedCol = -1;
    private int mistakes = 0, score = 0, seconds = 0;
    private int hintsUsed = 0;

    private boolean isPaused = false;
    private boolean isNoteMode = false;
    private boolean isGameEnded = false;

    private TextView tvScore, tvMistakes, tvTime, tvTitle, tvNoteStatus;
    private final ImageView[] hearts = new ImageView[3];

    private final Handler timerHandler = new Handler();
    private final Stack<String> undoStack = new Stack<>();

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
        boardSudoku = findViewById(R.id.boardSudoku);

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
            score = (int) getIntent().getLongExtra("SCORE", 0L);
            mistakes = (int) getIntent().getLongExtra("MISTAKES", 0L);
            seconds = (int) getIntent().getLongExtra("TIME", 0L);
        } else {
            currentBoardStr = puzzle;
        }

        if (difficulty == null) difficulty = "Easy";

        tvTitle.setText(String.format(Locale.getDefault(), "Màn %d - %s", levelId, difficulty));
        tvScore.setText(String.valueOf(score));
        updateMistakesUI();
    }

    private void setupGrid() {
        boardSudoku.setBoard(puzzle, currentBoardStr);

        boardSudoku.setOnCellClickListener((row, col) -> {
            if (isPaused || isGameEnded) return;

            selectedRow = row;
            selectedCol = col;
            boardSudoku.selectCell(row, col);
        });
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
            if (btn != null) btn.setOnClickListener(v -> handleInput(num));
        }
    }

    private void handleInput(int num) {
        if (selectedRow == -1 || selectedCol == -1 || isPaused || isGameEnded) return;

        if (boardSudoku.isFixedCell(selectedRow, selectedCol)) {
            Toast.makeText(this, "Ô này là số có sẵn", Toast.LENGTH_SHORT).show();
            return;
        }

        int index = selectedRow * 9 + selectedCol;
        String boardNow = boardSudoku.getCurrentBoardString();

        if (boardNow.charAt(index) != '0') {
            Toast.makeText(this, "Ô này đã có số", Toast.LENGTH_SHORT).show();
            return;
        }

        undoStack.push(boardNow);

        if (isNoteMode) {
            boardSudoku.addNote(selectedRow, selectedCol, num);
            return;
        }

        char correct = solution.charAt(index);

        if (String.valueOf(num).equals(String.valueOf(correct))) {
            boardSudoku.setNumber(selectedRow, selectedCol, (char) ('0' + num));

            score += getPointByDifficulty();
            tvScore.setText(String.valueOf(score));

            saveProgress();
            checkWin();
        } else {
            mistakes++;
            updateMistakesUI();

            boardSudoku.markWrongCell(selectedRow, selectedCol);

            new Handler().postDelayed(() -> boardSudoku.clearWrongCell(), 400);

            saveProgress();

            if (mistakes >= 3) handleLose();
        }
    }

    private int getPointByDifficulty() {
        if (difficulty.equalsIgnoreCase("Medium")) return 25;
        if (difficulty.equalsIgnoreCase("Hard")) return 50;
        return 10;
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

                if (selectedRow == -1 || selectedCol == -1) {
                    Toast.makeText(this, "Hãy chọn ô cần gợi ý", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (boardSudoku.isFixedCell(selectedRow, selectedCol)) {
                    Toast.makeText(this, "Ô này là số có sẵn", Toast.LENGTH_SHORT).show();
                    return;
                }

                int index = selectedRow * 9 + selectedCol;
                String boardNow = boardSudoku.getCurrentBoardString();

                if (boardNow.charAt(index) != '0') {
                    Toast.makeText(this, "Ô này đã có số", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (hintsUsed >= 3) {
                    Toast.makeText(this, "Bạn đã hết lượt gợi ý", Toast.LENGTH_SHORT).show();
                    return;
                }

                undoStack.push(boardNow);

                char correct = solution.charAt(index);
                boardSudoku.setNumber(selectedRow, selectedCol, correct);

                hintsUsed++;
                saveProgress();
                checkWin();
            });
        }

        View undo = findViewById(R.id.layoutUndo);
        if (undo != null) {
            undo.setOnClickListener(v -> {
                if (isPaused || isGameEnded) return;

                if (undoStack.isEmpty()) {
                    Toast.makeText(this, "Không có thao tác để hoàn tác", Toast.LENGTH_SHORT).show();
                    return;
                }

                String previousBoard = undoStack.pop();
                currentBoardStr = previousBoard;

                boardSudoku.setBoard(puzzle, previousBoard);
                saveProgress();
            });
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
                if (selectedRow != -1 && selectedCol != -1) {
                    if (boardSudoku.isFixedCell(selectedRow, selectedCol)) {
                        Toast.makeText(this, "Không thể xóa số có sẵn", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    undoStack.push(boardSudoku.getCurrentBoardString());

                    boardSudoku.clearCell(selectedRow, selectedCol);
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

                if (!isGameEnded) timerHandler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void saveProgress() {
        currentBoardStr = boardSudoku.getCurrentBoardString();

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
        String board = boardSudoku.getCurrentBoardString();

        for (int i = 0; i < 81; i++) {
            if (board.charAt(i) == '0') return;
            if (board.charAt(i) != solution.charAt(i)) return;
        }

        handleWin();
    }

    private String getProgressField() {
        if (difficulty.equalsIgnoreCase("Medium")) return "mediumProgress";
        if (difficulty.equalsIgnoreCase("Hard")) return "hardProgress";
        return "easyProgress";
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

            String progressField = getProgressField();
            Long oldProgress = doc.getLong(progressField);
            if (oldProgress == null) oldProgress = 0L;

            Map<String, Object> updateUser = new HashMap<>();
            updateUser.put("bestScore", newBestScore);
            updateUser.put("winStreak", newWinStreak);
            updateUser.put("loseStreak", 0L);
            updateUser.put("totalGames", FieldValue.increment(1));
            updateUser.put(progressField, Math.max(oldProgress, (long) levelId));

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
            goNextLevel();
        });

        dialog.show();
    }

    private void goNextLevel() {
        int nextLevel = levelId + 1;

        if (nextLevel > 100) {
            Toast.makeText(this, "Bạn đã hoàn thành hết chế độ này", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("sudoku_levels")
                .whereEqualTo("difficulty", difficulty)
                .whereEqualTo("level", (long) nextLevel)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        Toast.makeText(this, "Chưa có màn tiếp theo", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    String nextPuzzle = query.getDocuments().get(0).getString("puzzle");
                    String nextSolution = query.getDocuments().get(0).getString("solution");

                    Intent intent = new Intent(GameActivity.this, GameActivity.class);
                    intent.putExtra("PUZZLE", nextPuzzle);
                    intent.putExtra("SOLUTION", nextSolution);
                    intent.putExtra("LEVEL", nextLevel);
                    intent.putExtra("DIFFICULTY", difficulty);

                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi mở màn tiếp theo", Toast.LENGTH_SHORT).show();
                    finish();
                });
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