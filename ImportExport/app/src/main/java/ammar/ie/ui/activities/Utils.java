package ammar.ie.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;

import ammar.ie.R;

public class Utils {
    final static String AUTHORITY = "ammar.ie.file.provider";

    static void openFile(Context context, File target) {
        Uri data = FileProvider.getUriForFile(context, AUTHORITY, target);
        Intent open = new Intent(Intent.ACTION_VIEW);
        open.setDataAndType(data, "*/*");
        open.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(open);
    }

    static String countedFilesText(Context context, FilesAdapter.ContentCounter counter) {
        String dirs = context.getResources().getQuantityString(R.plurals.dirs, counter.folderCount, counter.folderCount);
        String files = context.getResources().getQuantityString(R.plurals.files, counter.fileCount, counter.fileCount);
        return dirs + ", " + files;
    }
}
