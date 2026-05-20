package ntu.tranngocvu.sudokurank.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import ntu.tranngocvu.sudokurank.R;
import ntu.tranngocvu.sudokurank.models.Score;

public class ScoreAdapter extends ArrayAdapter<Score> {
    public ScoreAdapter(@NonNull Context context, @NonNull List<Score> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_score, parent, false);
        }

        Score score = getItem(position);
        TextView tvRank = convertView.findViewById(R.id.tvRank);
        TextView tvName = convertView.findViewById(R.id.tvName);
        TextView tvScore = convertView.findViewById(R.id.tvScore);

        if (score != null) {
            tvRank.setText(String.valueOf(position + 1));
            tvName.setText(score.getUsername() != null ? score.getUsername() : "Player");
            tvScore.setText(score.getScore() + " pts");
        }

        return convertView;
    }
}
