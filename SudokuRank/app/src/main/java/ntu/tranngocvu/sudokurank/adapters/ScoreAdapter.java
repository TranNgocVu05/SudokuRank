package ntu.tranngocvu.sudokurank.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;
import ntu.tranngocvu.sudokurank.R;
import ntu.tranngocvu.sudokurank.models.Score;

public class ScoreAdapter extends ArrayAdapter<Score> {
    private Context context;

    public ScoreAdapter(@NonNull Context context, @NonNull List<Score> objects) {
        super(context, 0, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_score, parent, false);
        }

        Score score = getItem(position);
        TextView tvRank = convertView.findViewById(R.id.tvRankNum);
        ImageView imgAvatar = convertView.findViewById(R.id.imgScoreAvatar);
        TextView tvName = convertView.findViewById(R.id.tvScoreName);
        TextView tvPoints = convertView.findViewById(R.id.tvScorePoints);

        if (score != null) {
            tvRank.setText(String.valueOf(position + 1));
            tvName.setText(score.username);
            tvPoints.setText(String.format("%,d", score.score));

            // Map avatar
            String avatarName = score.avatar != null ? score.avatar : "avatar_1";
            int resId = context.getResources().getIdentifier(avatarName, "drawable", context.getPackageName());
            imgAvatar.setImageResource(resId != 0 ? resId : R.drawable.avatar_1);
        }

        return convertView;
    }
}
