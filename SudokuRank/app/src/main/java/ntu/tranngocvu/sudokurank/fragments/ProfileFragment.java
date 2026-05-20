package ntu.tranngocvu.sudokurank.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Arrays;
import java.util.List;
import ntu.tranngocvu.sudokurank.R;
import ntu.tranngocvu.sudokurank.activities.LoginActivity;
import ntu.tranngocvu.sudokurank.adapters.AvatarAdapter;
import ntu.tranngocvu.sudokurank.models.User;

public class ProfileFragment extends Fragment {
    private ImageView imgAvatar, imgRank;
    private TextView tvName, tvEmail, tvRank, tvBest, tvTotal, tvWin, tvLose;
    private ImageButton btnChangeAvatar;
    private Button btnLogout;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        imgAvatar = view.findViewById(R.id.imgProfileAvatar);
        imgRank = view.findViewById(R.id.imgProfileRank);
        tvName = view.findViewById(R.id.tvProfileName);
        tvEmail = view.findViewById(R.id.tvProfileEmail);
        tvRank = view.findViewById(R.id.tvProfileRank);
        tvBest = view.findViewById(R.id.tvStatBest);
        tvTotal = view.findViewById(R.id.tvStatTotal);
        tvWin = view.findViewById(R.id.tvStatWin);
        tvLose = view.findViewById(R.id.tvStatLose);
        btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        btnLogout = view.findViewById(R.id.btnLogout);

        loadUserData();

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finishAffinity();
        });

        btnChangeAvatar.setOnClickListener(v -> showAvatarPicker());

        return view;
    }

    private void loadUserData() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).addSnapshotListener((doc, e) -> {
            if (doc != null && doc.exists()) {
                User user = doc.toObject(User.class);
                if (user != null) {
                    tvName.setText(user.name);
                    tvEmail.setText(user.email);
                    tvRank.setText(user.rank);
                    tvBest.setText(String.valueOf(user.bestScore));
                    tvTotal.setText(String.valueOf(user.totalGames));
                    tvWin.setText("🔥 " + user.winStreak);
                    tvLose.setText("❄️ " + user.loseStreak);

                    int avId = getResources().getIdentifier(user.avatar, "drawable", getActivity().getPackageName());
                    imgAvatar.setImageResource(avId != 0 ? avId : R.drawable.avatar_1);

                    int rkId = getResources().getIdentifier(user.rankImage, "drawable", getActivity().getPackageName());
                    imgRank.setImageResource(rkId != 0 ? rkId : R.drawable.rank_bronze);
                }
            }
        });
    }

    private void showAvatarPicker() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_avatar_picker, null);
        RecyclerView rv = dialogView.findViewById(R.id.rvAvatarsDialog);
        
        List<String> avatars = Arrays.asList(
                "avatar_1", "avatar_2", "avatar_3", "avatar_4", "avatar_5",
                "avatar_6", "avatar_7", "avatar_8", "avatar_9", "avatar_10"
        );
        
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Chọn ảnh đại diện")
                .setView(dialogView)
                .create();

        AvatarAdapter adapter = new AvatarAdapter(getContext(), avatars, avatarName -> {
            db.collection("users").document(mAuth.getUid()).update("avatar", avatarName);
            dialog.dismiss();
        });
        
        rv.setLayoutManager(new GridLayoutManager(getContext(), 4));
        rv.setAdapter(adapter);
        dialog.show();
    }
}
