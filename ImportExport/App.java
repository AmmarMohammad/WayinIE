package ammar;

import android.app.Application;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                try {
                    PrintWriter pr = new PrintWriter(new File(Environment.getExternalStorageDirectory(), "ie.txt"));
                    e.printStackTrace(pr);
                    pr.flush();
                    pr.close();
                    System.exit(0);
                } catch (FileNotFoundException fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }
            }
        });
    }
}
