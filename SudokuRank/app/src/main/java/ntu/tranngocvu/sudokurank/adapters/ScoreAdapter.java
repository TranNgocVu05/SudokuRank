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

    private final Context context;

    public ScoreAdapter(@NonNull Context context, @NonNull List<Score> objects) {
        super(context, 0, objects);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position,
                        @Nullable View convertView,
                        @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_score, parent, false);
        }

        Score score = getItem(position);

        TextView tvRankNum = convertView.findViewById(R.id.tvRankNum);
        ImageView imgAvatar = convertView.findViewById(R.id.imgScoreAvatar);
        TextView tvName = convertView.findViewById(R.id.tvScoreName);
        TextView tvPoints = convertView.findViewById(R.id.tvScorePoints);
        TextView tvPlayerRank = convertView.findViewById(R.id.tvPlayerRank);
        ImageView imgPlayerRank = convertView.findViewById(R.id.imgPlayerRank);

        if (score != null) {
            tvRankNum.setText(String.valueOf(position + 1));

            String username = score.username == null ? "Người chơi" : score.username;
            tvName.setText(username);

            tvPoints.setText(String.format("%,d", score.score));

            String rankName = getRankName(score.score);
            tvPlayerRank.setText(rankName);

            int avatarId = getDrawableId(score.avatar == null ? "avatar_1" : score.avatar);
            imgAvatar.setImageResource(avatarId != 0 ? avatarId : R.drawable.avatar_1);

            int rankId = getDrawableId(getRankImage(score.score));
            imgPlayerRank.setImageResource(rankId != 0 ? rankId : R.drawable.rank_bronze);
        }

        return convertView;
    }

    private int getDrawableId(String name) {
        return context.getResources().getIdentifier(
                name,
                "drawable",
                context.getPackageName()
        );
    }

    private String getRankName(long score) {
        if (score >= 100000) return "Kim cương";
        if (score >= 50000) return "Bạch kim";
        if (score >= 10000) return "Vàng";
        if (score >= 5000) return "Bạc";
        return "Đồng";
    }

    private String getRankImage(long score) {
        if (score >= 100000) return "rank_diamond";
        if (score >= 50000) return "rank_platinum";
        if (score >= 10000) return "rank_gold";
        if (score >= 5000) return "rank_silver";
        return "rank_bronze";
    }
}