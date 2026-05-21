package ntu.tranngocvu.sudokurank.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.List;
import ntu.tranngocvu.sudokurank.MainActivity;
import ntu.tranngocvu.sudokurank.R;
import ntu.tranngocvu.sudokurank.adapters.AvatarAdapter;
import ntu.tranngocvu.sudokurank.models.User;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText edtUsername, edtEmail, edtPassword, edtConfirmPassword;
    private RecyclerView rvAvatars;
    private Button btnRegister;
    private TextView tvToLogin;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private AvatarAdapter avatarAdapter;
    private String selectedAvatar = "avatar_1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        rvAvatars = findViewById(R.id.rvAvatars);
        btnRegister = findViewById(R.id.btnRegister);
        tvToLogin = findViewById(R.id.tvToLogin);

        setupAvatarPicker();

        btnRegister.setOnClickListener(v -> handleRegister());
        tvToLogin.setOnClickListener(v -> finish());
    }

    private void setupAvatarPicker() {
        List<String> avatars = Arrays.asList(
                "avatar_1", "avatar_2", "avatar_3", "avatar_4", "avatar_5",
                "avatar_6", "avatar_7", "avatar_8", "avatar_9", "avatar_10"
        );
        avatarAdapter = new AvatarAdapter(this, avatars, avatarName -> selectedAvatar = avatarName);
        rvAvatars.setLayoutManager(new GridLayoutManager(this, 5));
        rvAvatars.setAdapter(avatarAdapter);
    }

    private void handleRegister() {

        String username = edtUsername.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {

                    if (task.isSuccessful()) {

                        String uid = mAuth.getCurrentUser().getUid();

                        Map<String, Object> user = new HashMap<>();
                        user.put("name", username);
                        user.put("email", email);
                        user.put("avatar", selectedAvatar);
                        user.put("rank", "Bronze I");
                        user.put("rankImage", "rank_bronze");
                        user.put("theme", "default");
                        user.put("bestScore", 0L);
                        user.put("totalGames", 0L);
                        user.put("winStreak", 0L);
                        user.put("loseStreak", 0L);

                        db.collection("users")
                                .document(uid)
                                .set(user)
                                .addOnSuccessListener(unused -> {

                                    Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    finishAffinity();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this,
                                                "Lỗi Firestore: " + e.getMessage(),
                                                Toast.LENGTH_LONG).show());

                    } else {

                        Toast.makeText(this,
                                "Đăng ký thất bại: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
