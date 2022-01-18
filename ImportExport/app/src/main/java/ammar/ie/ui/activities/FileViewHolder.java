package ammar.ie.ui.activities;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ammar.ie.R;

public class FileViewHolder extends RecyclerView.ViewHolder {
    ImageView icon;
    TextView name, details;

    public FileViewHolder(@NonNull View itemView) {
        super(itemView);
        icon = itemView.findViewById(R.id.img_icon);
        name = itemView.findViewById(R.id.txt_name);
        details = itemView.findViewById(R.id.txt_details);
    }
}
