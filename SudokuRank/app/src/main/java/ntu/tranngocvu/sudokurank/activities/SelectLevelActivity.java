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
import com.google.firebase.firestore.Query;
import ntu.tranngocvu.sudokurank.R;
import ntu.tranngocvu.sudokurank.models.Level;

public class SelectLevelActivity extends AppCompatActivity {
    private CardView cardEasy, cardMedium, cardHard;
    private TextView tvProgressEasy, tvProgressMedium, tvProgressHard;
    private ImageButton btnBack;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

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

        cardEasy.setOnClickListener(v -> startLevel("Easy"));
        cardMedium.setOnClickListener(v -> startLevel("Medium"));
        cardHard.setOnClickListener(v -> startLevel("Hard"));

        loadProgress();
    }

    private void loadProgress() {
        String uid = mAuth.getCurrentUser().getUid();
        // Here we would fetch progress count for each difficulty
        // For simplicity, we just keep placeholders as per requirement
    }

    private void startLevel(String difficulty) {
        db.collection("sudoku_levels")
                .whereEqualTo("difficulty", difficulty)
                .orderBy("level", Query.Direction.ASCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Level level = queryDocumentSnapshots.getDocuments().get(0).toObject(Level.class);
                        Intent intent = new Intent(SelectLevelActivity.this, GameActivity.class);
                        intent.putExtra("PUZZLE", level.getPuzzle());
                        intent.putExtra("SOLUTION", level.getSolution());
                        intent.putExtra("LEVEL", level.getLevel());
                        intent.putExtra("DIFFICULTY", level.getDifficulty());
                        startActivity(intent);
                    } else {
                        Toast.makeText(this, "Chưa có màn chơi cho độ khó này", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
