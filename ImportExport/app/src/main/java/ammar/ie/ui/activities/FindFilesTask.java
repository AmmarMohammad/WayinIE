package ammar.ie.ui.activities;

import android.os.AsyncTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FindFilesTask extends AsyncTask<Void, Void, List<File>> {


    interface OnFinishedCallback {
        void onFinished(List<File> files);
    }

    private final OnFinishedCallback callback;
    private final File root;
    private static FindFilesTask sTask;

    private FindFilesTask(File root, OnFinishedCallback callback) {
        this.root = root;
        this.callback = callback;
    }

    static void start(File root, OnFinishedCallback callback) {
        if (sTask == null) {
            sTask = new FindFilesTask(root, callback);
            sTask.execute();
        }
    }

    @Override
    protected List<File> doInBackground(Void... voids) {

        return scan(root, new ArrayList<>());
    }

    @Override
    protected void onPostExecute(List<File> files) {
        super.onPostExecute(files);
        if (callback != null) callback.onFinished(files);
        sTask = null;
    }

    List<File> scan(File dir, List<File> found) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        //found.addAll(scan(file));
                        scan(file, found);
                    } else if (file.isFile()) {
                        if (file.getName().toLowerCase().endsWith(".tar.gz")) {
                            found.add(file);
                        }
                    }
                }
            }
        }
        return found;
    }
}
