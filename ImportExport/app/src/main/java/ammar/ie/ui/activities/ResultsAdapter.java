package ammar.ie.ui.activities;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.List;

import ammar.ie.R;

public class ResultsAdapter extends ArrayAdapter<File> {

    private List<File> results;

    public ResultsAdapter(@NonNull Context context, List<File> results) {
        super(context, 0);
        this.results = results;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.archive_item, parent, false);
        }
        ViewGroup vg = (ViewGroup) convertView;

        File file = results.get(position);
        TextView name = (TextView) vg.getChildAt(0);
        TextView path = (TextView) vg.getChildAt(1);

        String fileName = file.getName();
        CharSequence viewName;
        if (fileName.toLowerCase().contains(convertView.getContext().getPackageName())) {
            SpannableString ss = new SpannableString(fileName);
            ss.setSpan(new ForegroundColorSpan(Color.parseColor("#4CAF50")), 0, fileName.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            viewName = ss;
        } else {
            viewName = fileName;
        }
        name.setText(viewName);
        path.setText(file.getPath().substring(0, file.getPath().lastIndexOf("/")));

        return convertView;
    }

    @Override
    public int getCount() {
        return results == null ? 0 : results.size();
    }
}
