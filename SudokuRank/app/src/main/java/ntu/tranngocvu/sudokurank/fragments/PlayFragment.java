package ntu.tranngocvu.sudokurank.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import ntu.tranngocvu.sudokurank.R;
import ntu.tranngocvu.sudokurank.activities.GameActivity;
import ntu.tranngocvu.sudokurank.adapters.LevelAdapter;
import ntu.tranngocvu.sudokurank.models.Level;

public class PlayFragment extends Fragment {
    private ListView lvLevels;
    private LevelAdapter adapter;
    private List<Level> levelList;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play, container, false);

        lvLevels = view.findViewById(R.id.lvLevels);
        levelList = new ArrayList<>();
        adapter = new LevelAdapter(getContext(), levelList);
        lvLevels.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadLevels();

        lvLevels.setOnItemClickListener((parent, v, position, id) -> {
            Level selected = levelList.get(position);
            Intent intent = new Intent(getActivity(), GameActivity.class);
            // Theo đúng yêu cầu field mới
            intent.putExtra("LEVEL_DATA", selected.getPuzzle());
            intent.putExtra("LEVEL_SOLUTION", selected.getSolution());
            intent.putExtra("LEVEL_ID", selected.getLevel());
            intent.putExtra("LEVEL_DIFFICULTY", selected.getDifficulty());
            startActivity(intent);
        });

        return view;
    }

    private void loadLevels() {
        // Collection 'sudoku_levels'
        db.collection("sudoku_levels").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                levelList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Level level = document.toObject(Level.class);
                    levelList.add(level);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}
