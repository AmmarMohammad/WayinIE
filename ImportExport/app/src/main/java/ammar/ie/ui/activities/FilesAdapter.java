package ammar.ie.ui.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import ammar.ie.R;

public class FilesAdapter extends RecyclerView.Adapter<FileViewHolder> implements View.OnClickListener {
    List<File> files;

    RecyclerView recyclerView;
    File parent;
    String root;

    public FilesAdapter(String root) {
        File pkg = new File(root);
        File[] files = pkg.listFiles();
        if (files != null) {
            this.files = Arrays.asList(files);
            sortFiles(this.files);
        }
        this.root = root;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_item, parent, false);
        FileViewHolder holder = new FileViewHolder(view);
        holder.itemView.setOnClickListener(this);
        return holder;
    }

    final DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale.getDefault());

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        File file = files.get(position);
        if (file.getPath().equals("/")) {
            holder.icon.setImageResource(R.drawable.ic_up);
            holder.name.setText("...");
            holder.details.setVisibility(View.GONE);
        } else {
            holder.icon.setImageResource(file.isDirectory() ? R.drawable.ic_folder : R.drawable.ic_file);
            holder.name.setText(file.getName());
            String firstDetail;
            if (file.isDirectory()) {
                ContentCounter counter = enumDir(file, new ContentCounter());
                /*firstDetail = String.format(Locale.ENGLISH, "%d dirs, %d files",
                        counter.folderCount, counter.fileCount);*/
                firstDetail=Utils.countedFilesText(recyclerView.getContext(), counter);
            } else {
                //firstDetail = Utils.getReadableSize(file.length());
                firstDetail = FileUtils.byteCountToDisplaySize(file.length());
            }
            holder.details.setText(String.format("%s • %s",
                    firstDetail, DATE_FORMAT.format(file.lastModified())));
            holder.details.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return files == null ? 0 : files.size();
    }

    @Override
    public void onClick(View v) {
        int position = recyclerView.getChildAdapterPosition(v);
        File file = files.get(position);
        if (file.getPath().equals("/")) {
            navigate(parent);
        } else if (file.isDirectory()) {
            navigate(file);
        } else {
            Context context = recyclerView.getContext();
            File target = new File(context.getExternalCacheDir(), file.getName());
            try {
                FileUtils.copyFile(file, target);
                Utils.openFile(context, target);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, context.getString(R.string.ie_error) + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    void navigate(File dir) {
        File[] subFiles = dir.listFiles();
        if (subFiles == null) subFiles = new File[0];
        this.files = new ArrayList<>(Arrays.asList(subFiles));
        sortFiles(files);

        if (!dir.getPath().equals(root)) {
            File up = new File("/");
            parent = dir.getParentFile();
            this.files.add(0, up);
        }

        notifyDataSetChanged();
    }

    void sortFiles(List<File> files) {
        Collections.sort(files, (o1, o2) -> o1.isDirectory() && o2.isDirectory() ? 0 : o1.isDirectory() && !o2.isDirectory() ? -1 : 1);
    }

    static class ContentCounter {
        int fileCount;
        int folderCount;
    }

    public static ContentCounter enumDir(File file, ContentCounter out) {
        File[] files = file.listFiles();
        if (files != null) {
            for (File subFile : files) {
                if (subFile.isDirectory()) {
                    out.folderCount++;
                    enumDir(subFile, out);
                } else {
                    out.fileCount++;
                }
            }
        }
        return out;
    }
}
