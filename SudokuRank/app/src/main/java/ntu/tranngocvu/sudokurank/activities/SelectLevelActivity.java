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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ntu.tranngocvu.sudokurank.R;
import ntu.tranngocvu.sudokurank.models.Level;
import ntu.tranngocvu.sudokurank.utils.SudokuSeeder;

public class SelectLevelActivity extends AppCompatActivity {

    private CardView cardEasy, cardMedium, cardHard;
    private TextView tvProgressEasy, tvProgressMedium, tvProgressHard;
    private ImageButton btnBack;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

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

        cardEasy.setOnClickListener(v -> startRandomLevel("Easy"));
        cardMedium.setOnClickListener(v -> startRandomLevel("Medium"));
        cardHard.setOnClickListener(v -> startRandomLevel("Hard"));

        loadProgress();

        if (SHOULD_SEED_LEVELS) {
            seedSudokuLevelsOnce();
        }
    }

    private void loadProgress() {
        if (mAuth.getCurrentUser() == null) return;

        tvProgressEasy.setText("Tiến độ: 0/100");
        tvProgressMedium.setText("Tiến độ: 0/100");
        tvProgressHard.setText("Tiến độ: 0/100");
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

    private void startRandomLevel(String difficulty) {
        db.collection("sudoku_levels")
                .whereEqualTo("difficulty", difficulty)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    List<Level> levels = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Level level = document.toObject(Level.class);

                        if (level != null
                                && level.getPuzzle() != null
                                && level.getSolution() != null
                                && level.getPuzzle().length() == 81
                                && level.getSolution().length() == 81) {
                            levels.add(level);
                        }
                    }

                    if (levels.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Chưa có màn hợp lệ cho độ khó này",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    int randomIndex = new Random().nextInt(levels.size());
                    Level selectedLevel = levels.get(randomIndex);

                    Intent intent = new Intent(SelectLevelActivity.this, GameActivity.class);
                    intent.putExtra("PUZZLE", selectedLevel.getPuzzle());
                    intent.putExtra("SOLUTION", selectedLevel.getSolution());
                    intent.putExtra("LEVEL", (int) selectedLevel.getLevel());
                    intent.putExtra("DIFFICULTY", selectedLevel.getDifficulty());

                    startActivity(intent);
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Lỗi: " + e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show());
    }
}