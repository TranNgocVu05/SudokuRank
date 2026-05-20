package ntu.tranngocvu.sudokurank.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private final String[] worldNames = {
            "BrainMaster", "SudokuKing", "LogicPro", "NhanhNhuChop", "Puzzler",
            "NumberHero", "GridMaster", "SmartPlayer", "FastThinker", "BlueRanker",
            "MindGame", "PuzzleBee", "RankHunter", "SudokuFox", "SharpMind",
            "EasyWin", "LogicStar", "QuickSolve", "GoldenBrain", "FireStreak",
            "CalmSolver", "GridNinja", "NumberOne", "PuzzleCat", "ThinkFast",
            "LevelUp", "SmartFox", "WinMore", "BrainFox", "SudokuVN",
            "PuzzleVN", "TopSolver", "FastGrid", "NumberKing", "CoolPlayer",
            "EasyLogic", "RankUp", "MiniBrain", "TrueSolver", "GameMind",
            "BlueFox", "PuzzleRank", "CalmMind", "GridFox", "SmartRank",
            "WinFox", "SudokuBoy", "LogicVN", "PuzzleHero", "NewSolver"
    };

    private final long[] worldScores = {
            15680, 12430, 9870, 8210, 7450,
            7100, 6900, 6600, 6300, 6000,
            5750, 5520, 5300, 5100, 4950,
            4800, 4630, 4510, 4390, 4210,
            4050, 3920, 3800, 3690, 3540,
            3400, 3290, 3180, 3060, 2950,
            2840, 2720, 2600, 2490, 2380,
            2260, 2150, 2040, 1930, 1820,
            1710, 1600, 1500, 1400, 1320,
            1240, 1160, 1080, 990, 900
    };

    private final String[] friendNames = {
            "MinhPro", "AnSudoku", "BaoLogic", "HuyNhanh", "LinhPuzzle",
            "NamRank", "KhoaBrain", "TuanGrid", "VySmart", "NhiWin"
    };

    private final long[] friendScores = {
            5200, 4800, 4500, 4200, 3900,
            3600, 3300, 3000, 2700, 2400
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

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

        updateTabUI();
        loadWorldScores();

        return view;
    }

    private void updateTabUI() {
        if (getContext() == null) return;

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
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (queryDocumentSnapshots.isEmpty()) {
                        seedWorldScores();
                        return;
                    }

                    scoreList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Score score = doc.toObject(Score.class);

                        if (score != null) {
                            scoreList.add(score);
                        }
                    }

                    Collections.sort(scoreList, (a, b) -> {

                        int compareScore = Long.compare(b.score, a.score);

                        if (compareScore != 0) {
                            return compareScore;
                        }

                        return Long.compare(a.time, b.time);
                    });

                    if (scoreList.size() > 50) {
                        scoreList = new ArrayList<>(scoreList.subList(0, 50));
                        adapter.clear();
                        adapter.addAll(scoreList);
                    }

                    adapter.notifyDataSetChanged();
                });
    }

    private void loadFriendScores() {

        String uid = mAuth.getUid();
        if (uid == null) return;

        db.collection("friends")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (queryDocumentSnapshots.isEmpty()) {
                        seedFriends();
                        return;
                    }

                    scoreList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        Score score = new Score();

                        score.userId = doc.getString("friendId");
                        score.username = doc.getString("friendName");
                        score.avatar = doc.getString("avatar");
                        score.rankImage = doc.getString("rankImage");

                        Long scoreLong = doc.getLong("score");

                        score.score = scoreLong == null ? 0L : scoreLong;

                        score.time = 200L;
                        score.mistakes = 1L;
                        score.difficulty = "Easy";
                        score.mode = "friends";

                        scoreList.add(score);
                    }

                    Collections.sort(scoreList,
                            (a, b) -> Long.compare(b.score, a.score));

                    adapter.notifyDataSetChanged();
                });
    }

    private void seedWorldScores() {

        for (int i = 0; i < 50; i++) {

            Map<String, Object> data = new HashMap<>();

            data.put("userId", "fake_user_" + (i + 1));
            data.put("username", worldNames[i]);
            data.put("avatar", "avatar_" + ((i % 10) + 1));

            if (worldScores[i] >= 7000) {
                data.put("rankImage", "rank_gold");
            } else if (worldScores[i] >= 4000) {
                data.put("rankImage", "rank_silver");
            } else {
                data.put("rankImage", "rank_bronze");
            }

            data.put("score", worldScores[i]);
            data.put("time", (long) (120 + i * 8));
            data.put("mistakes", (long) (i % 3));
            data.put("difficulty", i % 3 == 0 ? "Hard" :
                    i % 3 == 1 ? "Medium" : "Easy");

            data.put("mode", "world");

            db.collection("scores").add(data);
        }

        lvRanking.postDelayed(this::loadWorldScores, 1000);
    }

    private void seedFriends() {

        String uid = mAuth.getUid();
        if (uid == null) return;

        for (int i = 0; i < 10; i++) {

            Map<String, Object> data = new HashMap<>();

            data.put("userId", uid);
            data.put("friendId", "fake_friend_" + (i + 1));
            data.put("friendName", friendNames[i]);
            data.put("avatar", "avatar_" + ((i % 10) + 1));

            if (friendScores[i] >= 4500) {
                data.put("rankImage", "rank_gold");
            } else if (friendScores[i] >= 3300) {
                data.put("rankImage", "rank_silver");
            } else {
                data.put("rankImage", "rank_bronze");
            }

            data.put("score", friendScores[i]);

            db.collection("friends").add(data);
        }

        lvRanking.postDelayed(this::loadFriendScores, 1000);
    }
}