package ntu.tranngocvu.sudokurank.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import ntu.tranngocvu.sudokurank.R;
import ntu.tranngocvu.sudokurank.adapters.ScoreAdapter;
import ntu.tranngocvu.sudokurank.models.Score;

public class RankingFragment extends Fragment {

    private ListView lvRanking;
    private ScoreAdapter adapter;
    private List<Score> scoreList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);

        lvRanking = view.findViewById(R.id.lvRanking);
        scoreList = new ArrayList<>();
        adapter = new ScoreAdapter(getContext(), scoreList);
        lvRanking.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadWorldRanking();

        return view;
    }

    private void loadWorldRanking() {
        db.collection("scores")
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    scoreList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Score score = doc.toObject(Score.class);
                        if (score != null) {
                            scoreList.add(score);
                        }
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
