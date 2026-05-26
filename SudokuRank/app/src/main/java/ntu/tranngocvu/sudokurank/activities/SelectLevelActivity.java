package ntu.tranngocvu.sudokurank.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import ntu.tranngocvu.sudokurank.R;
import ntu.tranngocvu.sudokurank.utils.SudokuSeeder;

public class SelectLevelActivity extends AppCompatActivity {

    private CardView cardEasy, cardMedium, cardHard;
    private TextView tvProgressEasy, tvProgressMedium, tvProgressHard;
    private ImageButton btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private long easyProgress = 0;
    private long mediumProgress = 0;
    private long hardProgress = 0;

    // Sau khi tạo 300 màn xong thì đổi thành false
    //private static final boolean SHOULD_SEED_LEVELS = true;
    private static final boolean SHOULD_SEED_LEVELS = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_level);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        cardEasy = findViewById(R.id.cardEasy);
        cardMedium = findViewById(R.id.cardMedium);
        cardHard = findViewById(R.id.cardHard);

        tvProgressEasy = findViewById(R.id.tvProgressEasy);
        tvProgressMedium = findViewById(R.id.tvProgressMedium);
        tvProgressHard = findViewById(R.id.tvProgressHard);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        cardEasy.setOnClickListener(v -> startLevel("Easy", easyProgress));
        cardMedium.setOnClickListener(v -> startLevel("Medium", mediumProgress));
        cardHard.setOnClickListener(v -> startLevel("Hard", hardProgress));

        loadProgress();

        if (SHOULD_SEED_LEVELS) {
            seedSudokuLevelsOnce();
        }
    }

    private void loadProgress() {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    Long easy = doc.getLong("easyProgress");
                    Long medium = doc.getLong("mediumProgress");
                    Long hard = doc.getLong("hardProgress");

                    easyProgress = easy == null ? 0 : easy;
                    mediumProgress = medium == null ? 0 : medium;
                    hardProgress = hard == null ? 0 : hard;

                    tvProgressEasy.setText("Tiến độ: " + easyProgress + "/100");
                    tvProgressMedium.setText("Tiến độ: " + mediumProgress + "/100");
                    tvProgressHard.setText("Tiến độ: " + hardProgress + "/100");
                });
    }

    private void seedSudokuLevelsOnce() {
        SudokuSeeder.seedAllLevels(new SudokuSeeder.SeedCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(
                        SelectLevelActivity.this,
                        "Đã tạo 300 màn Sudoku",
                        Toast.LENGTH_SHORT
                ).show();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(
                        SelectLevelActivity.this,
                        "Lỗi seed: " + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void startLevel(String difficulty, long progress) {
        long nextLevel = progress + 1;

        if (nextLevel > 100) {
            Toast.makeText(this, "Bạn đã hoàn thành hết chế độ này", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("sudoku_levels")
                .whereEqualTo("difficulty", difficulty)
                .whereEqualTo("level", nextLevel)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        Toast.makeText(this, "Chưa có màn chơi này", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String puzzle = query.getDocuments().get(0).getString("puzzle");
                    String solution = query.getDocuments().get(0).getString("solution");

                    Intent intent = new Intent(SelectLevelActivity.this, GameActivity.class);
                    intent.putExtra("PUZZLE", puzzle);
                    intent.putExtra("SOLUTION", solution);
                    intent.putExtra("LEVEL", (int) nextLevel);
                    intent.putExtra("DIFFICULTY", difficulty);

                    startActivity(intent);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProgress();
    }
}