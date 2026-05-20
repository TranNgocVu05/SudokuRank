package ntu.tranngocvu.sudokurank.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import ntu.tranngocvu.sudokurank.R;
import ntu.tranngocvu.sudokurank.models.User;

public class HomeFragment extends Fragment {

    private TextView tvStreak;
    private Button btnNewGame, btnContinue;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tvStreak = view.findViewById(R.id.tvStreak);
        btnNewGame = view.findViewById(R.id.btnNewGame);
        btnContinue = view.findViewById(R.id.btnContinue);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            loadUserStats();
        }

        btnNewGame.setOnClickListener(v -> {
            // Chuyển sang tab Play
            if (getActivity() != null) {
                ((ntu.tranngocvu.sudokurank.MainActivity) getActivity()).findViewById(R.id.nav_play).performClick();
            }
        });

        btnContinue.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Tính năng đang phát triển!", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    private void loadUserStats() {
        String uid = mAuth.getCurrentUser().getUid();
        // Dùng đúng collection 'users' chữ thường
        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Giả sử có field streak trong Firestore hoặc tính toán từ dữ liệu khác
                Long streak = documentSnapshot.getLong("streak");
                if (streak != null) {
                    tvStreak.setText("🔥 " + streak);
                }
            }
        });
    }
}
