package ntu.tranngocvu.sudokurank.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import ntu.tranngocvu.sudokurank.R;
import ntu.tranngocvu.sudokurank.activities.GameActivity;
import ntu.tranngocvu.sudokurank.activities.SelectLevelActivity;
import ntu.tranngocvu.sudokurank.models.Progress;
import ntu.tranngocvu.sudokurank.models.User;

public class HomeFragment extends Fragment {

    private ImageView imgAvatar, imgRank;
    private TextView tvWelcome, tvRank, tvBestScore, tvTotalGames, tvWinStreak, tvLoseStreak;
    private Button btnContinue, btnNewGame, btnSettings;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private ListenerRegistration userListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        imgAvatar = view.findViewById(R.id.imgHomeAvatar);
        imgRank = view.findViewById(R.id.imgRank);

        tvWelcome = view.findViewById(R.id.tvWelcome);
        tvRank = view.findViewById(R.id.tvRank);
        tvBestScore = view.findViewById(R.id.tvBestScore);
        tvTotalGames = view.findViewById(R.id.tvTotalGames);
        tvWinStreak = view.findViewById(R.id.tvWinStreak);
        tvLoseStreak = view.findViewById(R.id.tvLoseStreak);

        btnContinue = view.findViewById(R.id.btnContinue);
        btnNewGame = view.findViewById(R.id.btnNewGame);


        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadUserData();

        btnNewGame.setOnClickListener(v -> {
            if (!isAdded()) return;
            startActivity(new Intent(requireContext(), SelectLevelActivity.class));
        });

        btnContinue.setOnClickListener(v -> checkProgress());

        return view;
    }

    private void loadUserData() {
        if (mAuth.getCurrentUser() == null) return;

        String uid = mAuth.getCurrentUser().getUid();

        userListener = db.collection("users")
                .document(uid)
                .addSnapshotListener((documentSnapshot, e) -> {

                    if (!isAdded() || getContext() == null) return;

                    if (e != null) {
                        Toast.makeText(getContext(), "Lỗi tải user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (documentSnapshot == null || !documentSnapshot.exists()) {
                        return;
                    }

                    User user = documentSnapshot.toObject(User.class);

                    if (user == null) return;

                    tvWelcome.setText("Xin chào, " + (user.name == null ? "Người chơi" : user.name));
                    tvRank.setText(user.rank == null ? "Bronze I" : user.rank);
                    tvBestScore.setText(String.valueOf(user.bestScore));
                    tvTotalGames.setText(String.valueOf(user.totalGames));
                    tvWinStreak.setText("🔥 " + user.winStreak);
                    tvLoseStreak.setText("❄️ " + user.loseStreak);

                    Context context = getContext();

                    String avatarName = user.avatar == null || user.avatar.trim().isEmpty()
                            ? "avatar_1"
                            : user.avatar;

                    int avatarResId = context.getResources().getIdentifier(
                            avatarName,
                            "drawable",
                            context.getPackageName()
                    );

                    imgAvatar.setImageResource(avatarResId != 0 ? avatarResId : R.drawable.avatar_1);

                    String rankImage = user.rankImage == null || user.rankImage.trim().isEmpty()
                            ? "rank_bronze"
                            : user.rankImage;

                    int rankResId = context.getResources().getIdentifier(
                            rankImage,
                            "drawable",
                            context.getPackageName()
                    );

                    imgRank.setImageResource(rankResId != 0 ? rankResId : R.drawable.rank_bronze);
                });
    }

    private void checkProgress() {
        if (!isAdded() || getContext() == null) return;

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getContext(), "Bạn chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = mAuth.getCurrentUser().getUid();

        db.collection("progress")
                .whereEqualTo("userId", uid)
                .whereEqualTo("completed", false)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (!isAdded() || getContext() == null) return;

                    if (!queryDocumentSnapshots.isEmpty()) {
                        Progress progress = queryDocumentSnapshots
                                .getDocuments()
                                .get(0)
                                .toObject(Progress.class);

                        if (progress == null) {
                            Toast.makeText(getContext(), "Dữ liệu tiến trình bị lỗi", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Intent intent = new Intent(requireContext(), GameActivity.class);

                        intent.putExtra("CONTINUE", true);
                        intent.putExtra("PUZZLE", progress.puzzle);
                        intent.putExtra("SOLUTION", progress.solution);
                        intent.putExtra("CURRENT_BOARD", progress.currentBoard);
                        intent.putExtra("SCORE", progress.score);
                        intent.putExtra("MISTAKES", progress.mistakes);
                        intent.putExtra("TIME", progress.time);
                        intent.putExtra("LEVEL", (int) progress.currentLevel);
                        intent.putExtra("DIFFICULTY", progress.difficulty);

                        startActivity(intent);
                    } else {
                        Toast.makeText(getContext(), "Chưa có màn chơi đang dở", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded() || getContext() == null) return;
                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        if (userListener != null) {
            userListener.remove();
            userListener = null;
        }

        super.onDestroyView();
    }
}