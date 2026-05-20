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
import ntu.tranngocvu.sudokurank.models.Level;

public class LevelAdapter extends ArrayAdapter<Level> {

    public LevelAdapter(@NonNull Context context, @NonNull List<Level> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_level, parent, false);
        }

        Level level = getItem(position);

        TextView tvName = convertView.findViewById(R.id.tvLevelName);
        TextView tvDifficulty = convertView.findViewById(R.id.tvDifficulty);

        if (level != null) {
            // Hiển thị Level 1, Level 2...
            tvName.setText("Level " + level.getLevel());

            // Hiển thị độ khó
            tvDifficulty.setText(level.getDifficulty());
        }

        return convertView;
    }
}
