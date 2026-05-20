package ntu.tranngocvu.sudokurank.fragments;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
        btnSettings = view.findViewById(R.id.btnSettings);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        loadUserData();

        btnNewGame.setOnClickListener(v -> startActivity(new Intent(getActivity(), SelectLevelActivity.class)));

        btnContinue.setOnClickListener(v -> checkProgress());

        return view;
    }

    private void loadUserData() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("users").document(uid).addSnapshotListener((documentSnapshot, e) -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    tvWelcome.setText("Xin chào, " + user.name);
                    tvRank.setText(user.rank);
                    tvBestScore.setText(String.valueOf(user.bestScore));
                    tvTotalGames.setText(String.valueOf(user.totalGames));
                    tvWinStreak.setText("🔥 " + user.winStreak);
                    tvLoseStreak.setText("❄️ " + user.loseStreak);

                    // Map avatar
                    int avatarResId = getResources().getIdentifier(user.avatar, "drawable", getActivity().getPackageName());
                    imgAvatar.setImageResource(avatarResId != 0 ? avatarResId : R.drawable.avatar_1);

                    // Map rank image
                    int rankResId = getResources().getIdentifier(user.rankImage, "drawable", getActivity().getPackageName());
                    imgRank.setImageResource(rankResId != 0 ? rankResId : R.drawable.rank_bronze);
                }
            }
        });
    }

    private void checkProgress() {
        String uid = mAuth.getCurrentUser().getUid();
        db.collection("progress")
                .whereEqualTo("userId", uid)
                .whereEqualTo("completed", false)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Progress progress = queryDocumentSnapshots.getDocuments().get(0).toObject(Progress.class);
                        Intent intent = new Intent(getActivity(), GameActivity.class);
                        intent.putExtra("CONTINUE", true);
                        intent.putExtra("PUZZLE", progress.puzzle);
                        intent.putExtra("SOLUTION", progress.solution);
                        intent.putExtra("CURRENT_BOARD", progress.currentBoard);
                        intent.putExtra("SCORE", progress.score);
                        intent.putExtra("MISTAKES", progress.mistakes);
                        intent.putExtra("TIME", progress.time);
                        intent.putExtra("LEVEL", progress.currentLevel);
                        intent.putExtra("DIFFICULTY", progress.difficulty);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getActivity(), "Chưa có màn chơi đang dở", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
