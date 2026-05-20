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
import android.widget.Toast;

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

    private String currentAvatar = "avatar_1";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

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

        btnChangeAvatar.setOnClickListener(v -> showAvatarPicker());

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            requireActivity().finishAffinity();
        });

        return view;
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null || getActivity() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("users").document(uid).addSnapshotListener((doc, e) -> {
            if (doc != null && doc.exists() && getActivity() != null) {
                User user = doc.toObject(User.class);

                if (user == null) return;

                tvName.setText(user.name == null ? "Người chơi" : user.name);
                tvEmail.setText(user.email == null ? "" : user.email);
                tvRank.setText(user.rank == null ? "Bronze I" : user.rank);

                tvBest.setText(String.valueOf(user.bestScore));
                tvTotal.setText(String.valueOf(user.totalGames));
                tvWin.setText("🔥 " + user.winStreak);
                tvLose.setText("❄️ " + user.loseStreak);

                currentAvatar = user.avatar == null ? "avatar_1" : user.avatar;

                int avatarId = getResources().getIdentifier(
                        currentAvatar,
                        "drawable",
                        requireActivity().getPackageName()
                );

                imgAvatar.setImageResource(avatarId != 0 ? avatarId : R.drawable.avatar_1);

                String rankImage = user.rankImage == null ? "rank_bronze" : user.rankImage;

                int rankId = getResources().getIdentifier(
                        rankImage,
                        "drawable",
                        requireActivity().getPackageName()
                );

                imgRank.setImageResource(rankId != 0 ? rankId : R.drawable.rank_bronze);
            }
        });
    }

    private void showAvatarPicker() {
        if (getContext() == null || mAuth.getUid() == null) return;

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
            db.collection("users")
                    .document(mAuth.getUid())
                    .update("avatar", avatarName)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(getContext(), "Đã đổi avatar", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        rv.setLayoutManager(new GridLayoutManager(getContext(), 4));
        rv.setAdapter(adapter);

        dialog.show();
    }
}