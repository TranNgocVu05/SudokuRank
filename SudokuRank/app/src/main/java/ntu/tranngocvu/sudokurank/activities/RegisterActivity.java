package ntu.tranngocvu.sudokurank.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import ntu.tranngocvu.sudokurank.MainActivity;
import ntu.tranngocvu.sudokurank.R;
import ntu.tranngocvu.sudokurank.models.User;

public class RegisterActivity extends AppCompatActivity {
    EditText edtName, edtEmail, edtPassword;
    Button btnRegister;
    TextView tvToLogin;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvToLogin = findViewById(R.id.tvToLogin);

        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String pass = edtPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        // Tạo đối tượng User với constructor mới
                        User user = new User(name, email);
                        
                        // Dùng collection 'users' chữ thường theo yêu cầu
                        db.collection("users").document(uid).set(user)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, MainActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Lỗi lưu dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                    } else {
                        Toast.makeText(this, "Đăng ký thất bại: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        });

        tvToLogin.setOnClickListener(v -> finish());
    }
}
