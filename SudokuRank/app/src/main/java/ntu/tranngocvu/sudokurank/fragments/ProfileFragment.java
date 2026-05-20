package ntu.tranngocvu.sudokurank.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import ntu.tranngocvu.sudokurank.R;
import ntu.tranngocvu.sudokurank.activities.LoginActivity;
import ntu.tranngocvu.sudokurank.models.User;

public class ProfileFragment extends Fragment {
    TextView tvName, tvEmail, tvRank, tvBestScore, tvTotalGames;
    ImageView imgAvatar;
    Button btnSignOut;
    FirebaseAuth mAuth;
    FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imgAvatar = view.findViewById(R.id.imgProfileAvatar);
        tvName = view.findViewById(R.id.tvProfileName);
        tvEmail = view.findViewById(R.id.tvProfileEmail);
        tvRank = view.findViewById(R.id.tvProfileRank);
        tvBestScore = view.findViewById(R.id.tvProfileBestScore);
        tvTotalGames = view.findViewById(R.id.tvProfileTotalGames);
        btnSignOut = view.findViewById(R.id.btnSignOut);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() != null) {
            String uid = mAuth.getCurrentUser().getUid();
            // Dùng collection 'users' chữ thường
            db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    tvName.setText("Tên: " + user.name);
                    tvEmail.setText("Email: " + user.email);
                    tvRank.setText("Hạng: " + user.rank);
                    tvBestScore.setText("Điểm cao nhất: " + user.bestScore);
                    tvTotalGames.setText("Tổng số trận: " + user.totalGames);
                    
                    // Hiển thị avatar từ drawable
                    if (user.avatar != null && !user.avatar.isEmpty()) {
                        int resId = getResources().getIdentifier(user.avatar, "drawable", getContext().getPackageName());
                        if (resId != 0) {
                            imgAvatar.setImageResource(resId);
                        }
                    }
                }
            });
        }

        btnSignOut.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        });

        return view;
    }
}
