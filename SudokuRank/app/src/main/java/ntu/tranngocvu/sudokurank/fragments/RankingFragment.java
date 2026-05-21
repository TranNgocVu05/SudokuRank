package ntu.tranngocvu.sudokurank.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

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

import ntu.tranngocvu.sudokurank.R;
import ntu.tranngocvu.sudokurank.adapters.ScoreAdapter;
import ntu.tranngocvu.sudokurank.models.Score;
import ntu.tranngocvu.sudokurank.models.User;

public class RankingFragment extends Fragment {

    private ListView lvRanking;

    private TextView btnWorld;
    private TextView btnFriends;

    private ScoreAdapter adapter;
    private final List<Score> scoreList = new ArrayList<>();

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

        adapter = new ScoreAdapter(requireContext(), scoreList);
        lvRanking.setAdapter(adapter);

        btnWorld.setOnClickListener(v -> {

            if (!isWorldView) {
                isWorldView = true;

                updateTabUI();

                animateList();

                loadWorldScores();
            }
        });

        btnFriends.setOnClickListener(v -> {

            if (isWorldView) {
                isWorldView = false;

                updateTabUI();

                animateList();

                loadFriendScores();
            }
        });

        updateTabUI();

        loadWorldScores();

        return view;
    }

    private void animateList() {

        lvRanking.setAlpha(0f);

        lvRanking.animate()
                .alpha(1f)
                .setDuration(220)
                .start();
    }

    private void updateTabUI() {

        if (getContext() == null) return;

        if (isWorldView) {

            btnWorld.setBackgroundResource(R.drawable.tab_selected_bg);
            btnWorld.setTextColor(getContext().getColor(R.color.white));

            btnFriends.setBackgroundResource(android.R.color.transparent);
            btnFriends.setTextColor(getContext().getColor(R.color.text_secondary));

        } else {

            btnFriends.setBackgroundResource(R.drawable.tab_selected_bg);
            btnFriends.setTextColor(getContext().getColor(R.color.white));

            btnWorld.setBackgroundResource(android.R.color.transparent);
            btnWorld.setTextColor(getContext().getColor(R.color.text_secondary));
        }
    }

    private void loadWorldScores() {

        db.collection("scores")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (queryDocumentSnapshots.size() < 50) {

                        seedWorldScores();

                        return;
                    }

                    scoreList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        Score score = doc.toObject(Score.class);

                        if (score != null) {

                            if (score.username == null)
                                score.username = "Người chơi";

                            if (score.avatar == null)
                                score.avatar = "avatar_1";

                            score.rank = getRankName(score.score);

                            score.rankImage = getRankImage(score.score);

                            scoreList.add(score);
                        }
                    }

                    sortAndLimit50();
                });
    }

    private void loadFriendScores() {

        String uid = mAuth.getUid();

        if (uid == null) return;

        scoreList.clear();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(userDoc -> {

                    User user = userDoc.toObject(User.class);

                    if (user != null) {

                        Score myScore = new Score();

                        myScore.userId = uid;
                        myScore.username = user.name;
                        myScore.avatar = user.avatar;

                        myScore.score = user.bestScore;

                        myScore.rank = getRankName(myScore.score);

                        myScore.rankImage = getRankImage(myScore.score);

                        scoreList.add(myScore);
                    }

                    loadFriendsAfterSelf(uid);
                });
    }

    private void loadFriendsAfterSelf(String uid) {

        db.collection("friends")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    if (queryDocumentSnapshots.size() < 10) {

                        seedFriends();

                        return;
                    }

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        Score score = new Score();

                        score.userId = doc.getString("friendId");
                        score.username = doc.getString("friendName");
                        score.avatar = doc.getString("avatar");

                        Long scoreLong = doc.getLong("score");

                        score.score = scoreLong == null ? 0 : scoreLong;

                        score.rank = getRankName(score.score);

                        score.rankImage = getRankImage(score.score);

                        scoreList.add(score);
                    }

                    Collections.sort(scoreList,
                            (a, b) -> Long.compare(b.score, a.score));

                    adapter.notifyDataSetChanged();
                });
    }

    private void sortAndLimit50() {

        Collections.sort(scoreList,
                (a, b) -> Long.compare(b.score, a.score));

        if (scoreList.size() > 50) {

            scoreList.subList(50, scoreList.size()).clear();
        }

        adapter.notifyDataSetChanged();
    }

    private String getRankName(long score) {

        if (score >= 100000)
            return "Kim cương";

        if (score >= 50000)
            return "Bạch kim";

        if (score >= 10000)
            return "Vàng";

        if (score >= 5000)
            return "Bạc";

        return "Đồng";
    }

    private String getRankImage(long score) {

        if (score >= 100000)
            return "rank_diamond";

        if (score >= 50000)
            return "rank_platinum";

        if (score >= 10000)
            return "rank_gold";

        if (score >= 5000)
            return "rank_silver";

        return "rank_bronze";
    }

    private void seedWorldScores() {

        for (int i = 0; i < 50; i++) {

            HashMap<String, Object> data = new HashMap<>();

            data.put("userId", "fake_user_" + i);

            data.put("username", worldNames[i]);

            data.put("avatar",
                    "avatar_" + ((i % 10) + 1));

            data.put("score", worldScores[i]);

            data.put("rank",
                    getRankName(worldScores[i]));

            data.put("rankImage",
                    getRankImage(worldScores[i]));

            db.collection("scores").add(data);
        }

        lvRanking.postDelayed(this::loadWorldScores, 1200);
    }

    private void seedFriends() {

        String uid = mAuth.getUid();

        if (uid == null) return;

        for (int i = 0; i < 10; i++) {

            HashMap<String, Object> data = new HashMap<>();

            data.put("userId", uid);

            data.put("friendId",
                    "friend_" + i);

            data.put("friendName",
                    friendNames[i]);

            data.put("avatar",
                    "avatar_" + ((i % 10) + 1));

            data.put("score",
                    friendScores[i]);

            data.put("rank",
                    getRankName(friendScores[i]));

            data.put("rankImage",
                    getRankImage(friendScores[i]));

            db.collection("friends").add(data);
        }

        lvRanking.postDelayed(this::loadFriendScores, 1000);
    }
}