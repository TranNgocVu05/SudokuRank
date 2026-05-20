package ntu.tranngocvu.sudokurank.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import ntu.tranngocvu.sudokurank.R;
import ntu.tranngocvu.sudokurank.adapters.ScoreAdapter;
import ntu.tranngocvu.sudokurank.models.Score;

public class RankingFragment extends Fragment {
    private ListView lvRanking;
    private Button btnWorld, btnFriends;
    private ScoreAdapter adapter;
    private List<Score> scoreList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private boolean isWorldView = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        lvRanking = view.findViewById(R.id.lvRanking);
        btnWorld = view.findViewById(R.id.btnWorld);
        btnFriends = view.findViewById(R.id.btnFriends);

        adapter = new ScoreAdapter(getContext(), scoreList);
        lvRanking.setAdapter(adapter);

        btnWorld.setOnClickListener(v -> {
            isWorldView = true;
            updateTabUI();
            loadWorldScores();
        });

        btnFriends.setOnClickListener(v -> {
            isWorldView = false;
            updateTabUI();
            loadFriendScores();
        });

        loadWorldScores();
        // seedWorldScores(); // Uncomment once to populate database
        
        return view;
    }

    private void updateTabUI() {
        if (isWorldView) {
            btnWorld.setBackgroundTintList(getContext().getColorStateList(R.color.sudoku_primary));
            btnWorld.setTextColor(getContext().getColor(R.color.white));
            btnFriends.setBackgroundTintList(getContext().getColorStateList(android.R.color.transparent));
            btnFriends.setTextColor(getContext().getColor(R.color.text_secondary));
        } else {
            btnFriends.setBackgroundTintList(getContext().getColorStateList(R.color.sudoku_primary));
            btnFriends.setTextColor(getContext().getColor(R.color.white));
            btnWorld.setBackgroundTintList(getContext().getColorStateList(android.R.color.transparent));
            btnWorld.setTextColor(getContext().getColor(R.color.text_secondary));
        }
    }

    private void loadWorldScores() {
        db.collection("scores")
                .orderBy("score", Query.Direction.DESCENDING)
                .orderBy("time", Query.Direction.ASCENDING)
                .limit(50)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    scoreList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        scoreList.add(doc.toObject(Score.class));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void loadFriendScores() {
        // Simple logic: fetch current user's friends first, then scores.
        // For students, let's just filter by mode="friends" or specific IDs if available.
        String uid = mAuth.getUid();
        db.collection("friends")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> friendIds = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        friendIds.add(doc.getString("friendId"));
                    }
                    // Also include self
                    friendIds.add(uid);

                    if (friendIds.isEmpty()) {
                        scoreList.clear();
                        adapter.notifyDataSetChanged();
                        return;
                    }

                    db.collection("scores")
                            .whereIn("userId", friendIds)
                            .orderBy("score", Query.Direction.DESCENDING)
                            .get()
                            .addOnSuccessListener(scoreDocs -> {
                                scoreList.clear();
                                for (QueryDocumentSnapshot doc : scoreDocs) {
                                    scoreList.add(doc.toObject(Score.class));
                                }
                                adapter.notifyDataSetChanged();
                            });
                });
    }

    private void seedWorldScores() {
        // Implementation for seeding as requested
        String[] names = {"BrainMaster", "SudokuKing", "LogicPro", "NhanhNhuChop", "Puzzler", "NumberHero", "GridMaster", "SmartPlayer", "FastThinker", "BlueRanker"};
        int[] bases = {15680, 12430, 9870, 8210, 7450, 7100, 6900, 6600, 6300, 6000};
        
        for (int i = 0; i < names.length; i++) {
            Score s = new Score();
            s.userId = "fake_user_" + i;
            s.username = names[i];
            s.score = bases[i];
            s.time = 120 + i * 10;
            s.avatar = "avatar_" + ((i % 10) + 1);
            s.rankImage = "rank_bronze";
            s.mode = "world";
            db.collection("scores").add(s);
        }
        Toast.makeText(getContext(), "Seeded 10 scores", Toast.LENGTH_SHORT).show();
    }
}
