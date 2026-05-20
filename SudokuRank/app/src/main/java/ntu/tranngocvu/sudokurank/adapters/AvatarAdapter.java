package ntu.tranngocvu.sudokurank.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ntu.tranngocvu.sudokurank.R;

public class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.AvatarViewHolder> {

    private final Context context;
    private final List<String> avatarList;
    private int selectedPosition = 0;
    private OnAvatarClickListener listener;

    public interface OnAvatarClickListener {
        void onAvatarClick(String avatarName);
    }

    public AvatarAdapter(Context context, List<String> avatarList, OnAvatarClickListener listener) {
        this.context = context;
        this.avatarList = avatarList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AvatarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_avatar, parent, false);
        return new AvatarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvatarViewHolder holder, int position) {
        String avatarName = avatarList.get(position);
        
        // Map string name to drawable resource
        int resId = context.getResources().getIdentifier(avatarName, "drawable", context.getPackageName());
        if (resId != 0) {
            holder.imgAvatar.setImageResource(resId);
        } else {
            // Placeholder
            holder.imgAvatar.setImageResource(android.R.drawable.sym_def_app_icon);
        }

        if (selectedPosition == position) {
            holder.imgAvatar.setBackgroundResource(R.drawable.avatar_selected_border);
        } else {
            holder.imgAvatar.setBackgroundResource(R.drawable.avatar_border);
        }

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);
            listener.onAvatarClick(avatarName);
        });
    }

    @Override
    public int getItemCount() {
        return avatarList.size();
    }

    public String getSelectedAvatar() {
        return avatarList.get(selectedPosition);
    }

    static class AvatarViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;

        AvatarViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }
}
