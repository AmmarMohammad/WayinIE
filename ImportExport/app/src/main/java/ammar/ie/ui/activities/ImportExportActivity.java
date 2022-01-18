package ammar.ie.ui.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.PermissionChecker;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.io.FileUtils;
import java.io.PrintWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ammar.ie.R;

public class ImportExportActivity extends AppCompatActivity {

    static final int CODE_PICK_ARCHIVE = 1;
    private static final int CODE_REQUEST_STORAGE = 2;

    private static WeakReference<AppCompatActivity> sRef;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                try {
                    PrintWriter pr = new PrintWriter(new File(Environment.getExternalStorageDirectory(), "ie.txt"));
                    e.printStackTrace(pr);
                    pr.flush();
                    pr.close();
                    
                } catch (Exception fileNotFoundException) {
                    fileNotFoundException.printStackTrace();
                }
                System.exit(0);
            }
        });
        sRef = new WeakReference<>(this);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.hide();
        }
        /*LinearLayout ll = new LinearLayout(this);
        ll.setGravity(Gravity.CENTER);
        ll.setOrientation(LinearLayout.VERTICAL);
        //Button btnImport = new Button(this);
        AppCompatButton btnImport = new AppCompatButton(this);
        //btnImport.setCompoundDrawablesRelativeWithIntrinsicBounds(ContextCompat.getDrawable(this, R.drawable.ic_import), null, null, null);
        //btnImport.setText("Import");//↓
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = 16;
        ll.addView(btnImport, lp);

        Button btnExport = new Button(this);
        btnExport.setText("↑ Export");
        lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = 16;
        ll.addView(btnExport, lp);


        setContentView(ll);*/

        File dataFile = new File(getApplicationInfo().dataDir);
        long size = FileUtils.sizeOfDirectory(dataFile);
        String info = FileUtils.byteCountToDisplaySize(size);


        setContentView(R.layout.activity_import_export);

        TextView title = findViewById(R.id.txt_title);
        TextView dir = findViewById(R.id.txt_dir);
        TextView details = findViewById(R.id.txt_info);
        title.setText(getString(R.string.import_export_glyph));
        dir.setText(getApplicationInfo().dataDir);

        FilesAdapter.ContentCounter cc = new FilesAdapter.ContentCounter();
        FilesAdapter.enumDir(dataFile, cc);
        details.setText(String.format(Locale.getDefault(), "%s: %s", Utils.countedFilesText(this, cc), info));


        recyclerView = findViewById(R.id.recycler);
        Point p = new Point();
        getWindowManager().getDefaultDisplay().getSize(p);
        recyclerView.post(() -> {
            recyclerView.getLayoutParams().height = p.y / 3;
            recyclerView.requestLayout();
        });

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(new FilesAdapter(getApplicationInfo().dataDir));

        ImageButton btnArchives = findViewById(R.id.btn_archives);
        btnArchives.setOnClickListener(v -> startSearch());

        Button btnImport = findViewById(R.id.btn_import);
        Button btnExport = findViewById(R.id.btn_export);

        String name;
        try {
            name = BluetoothAdapter.getDefaultAdapter().getName();
        } catch (Exception e) {
            name = new SimpleDateFormat("dd/MM/yyyy", Locale.US).format(new Date());
            e.printStackTrace();
        }
        final String defName = String.format("%s_%s.tar.gz", name, getPackageName());
        final String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("gz");

        btnImport.setOnClickListener(v -> {
            try {
                FileHelper.openDocument(this, mimeType, CODE_PICK_ARCHIVE);
            } catch (Exception e) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.import_))
                        .setMessage(getString(R.string.no_picker_search_prompt))
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> startSearch())
                        .show();
                e.printStackTrace();
            }
        });

        btnExport.setOnClickListener(v -> {
            AlertDialog.Builder adb = new AlertDialog.Builder(ImportExportActivity.this);
            adb.setMessage(getString(R.string.exported_file_name));


            /*LinearLayout etWrapper = new LinearLayout(ImportExportActivity.this);
            etWrapper.setPadding(32, 16, 32, 16);
            final EditText editName = new EditText(ImportExportActivity.this);
            LinearLayout.LayoutParams etLayoutParams = new LinearLayout
                    .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            etWrapper.addView(editName, etLayoutParams);*/

            View etWrapper = getLayoutInflater().inflate(R.layout.dialog_export_file_name, null);
            EditText editName = etWrapper.findViewById(R.id.edit_file_name);

            editName.setText(defName);
            editName.setSelection(defName.length());

            adb.setView(etWrapper);
            AlertDialog alertDialog = adb.setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {


                        String name1 = editName.getText().toString();
                        String pkg = getApplicationInfo().dataDir;
                        File t = new File(getExternalCacheDir(), name1);
                        try {
                            Tar.CreateTarGZ(pkg, t.getPath());


                            try {
                                //Intent share = new Intent(Intent.ACTION_SEND);
                        /*Uri data = FileProvider.getUriForFile(ImportExportActivity.this,
                                getPackageName() + ".provider", t);*/
                                Uri data = FileProvider.getUriForFile(ImportExportActivity.this,
                                        Utils.AUTHORITY, t);

                                //share.setDataAndType(data, mimeType);
                                //share.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                //startActivity(share);

                                Intent shareIntent = ShareCompat.IntentBuilder.from(ImportExportActivity.this)
                                        .setType(mimeType)
                                        .setStream(data)
                                        .getIntent();
                                //.setData(data);
                                if (shareIntent.resolveActivity(getPackageManager()) != null) {
                                    startActivity(shareIntent);
                                }
                            } catch (Exception e) {
                                Toast.makeText(ImportExportActivity.this,
                                        getString(R.string.ie_sharing_failed) + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }


                            Toast.makeText(ImportExportActivity.this,
                                    getString(R.string.ie_exported_to) + t.toString(), Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(ImportExportActivity.this, e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).create();
            alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            alertDialog.setOnShowListener(dialog -> {
                editName.requestFocus();
                editName.setSelection(editName.length());
            });
            alertDialog.show();
        });
    }

    void startSearch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(ImportExportActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) !=
                    PermissionChecker.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(ImportExportActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                            PermissionChecker.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ImportExportActivity.this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        CODE_REQUEST_STORAGE);
            } else {
                showArchives();
            }
        } else {
            showArchives();
        }
    }

    void askToQuit() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.import_success_exit_prompt))
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> System.exit(0)).show();
    }

    void offerImportProcedures(Runnable action) {
        LinearLayout view = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_import_procedure, null);
        RadioGroup radioGroup = (RadioGroup) view.getChildAt(0);
        new AlertDialog.Builder(this)
                .setTitle(R.string.import_)
                .setMessage(getString(R.string.import_procedure_prompt))
                .setView(view)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    int id = radioGroup.getCheckedRadioButtonId();
                    if (id == R.id.radio_delete) {
                        File dataDir = new File(getApplicationInfo().dataDir);
                        File[] files = dataDir.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                FileUtils.deleteQuietly(file);
                            }
                        }
                    }

                    action.run();

                    recyclerView.setAdapter(new FilesAdapter(getApplicationInfo().dataDir));
                }).setNegativeButton(android.R.string.cancel, null)
                .show();

    }

    void extract(Uri uri) {
        File pkg = new File(getApplicationInfo().dataDir);
        //File src = new File(Environment.getExternalStorageDirectory(), defName);
        if (uri != null) {
            try {
                InputStream is = getContentResolver().openInputStream(uri);
                Tar.decompress(is, pkg);
                Toast.makeText(this, getString(R.string.ie_imported_to) + pkg, Toast.LENGTH_SHORT).show();
                askToQuit();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    void showProgress(String message) {
        ViewSwitcher switcher = findViewById(R.id.view_switcher);
        if (switcher.getCurrentView().getId() != R.id.progress_container) {
            TextView progressText = findViewById(R.id.txt_progress);
            progressText.setText(message);
            switcher.showNext();
        }
    }

    void hideProgress() {
        ViewSwitcher switcher = findViewById(R.id.view_switcher);
        if (switcher.getCurrentView().getId() == R.id.progress_container) {
            switcher.showPrevious();
        }
    }

    void showArchives() {
        File root = Environment.getExternalStorageDirectory();

        showProgress(getString(R.string.ie_searching));
        FindFilesTask.start(root, results -> {
            hideProgress();


            if (results.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(ImportExportActivity.this,
                        String.format(getString(R.string.ie_no_archives_found), root.getAbsolutePath()),
                        Toast.LENGTH_LONG).show());
            } else {
                runOnUiThread(() -> {

                    Context context = sRef.get();
                    if (context != null) {
                        AlertDialog.Builder adb = new AlertDialog.Builder(context);

                        ListView lv = new ListView(context);

                        adb.setTitle(getString(R.string.found_archives))
                                .setView(lv)
                                .setNegativeButton(android.R.string.cancel, null);
                        AlertDialog alertDialog = adb.create();
                        //lv.setAdapter(new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, results));
                        lv.setAdapter(new ResultsAdapter(context, results));
                        lv.setOnItemClickListener((parent, view, position, id) -> {
                            alertDialog.dismiss();
                            Runnable action = () -> {
                                File pkg = new File(getApplicationInfo().dataDir);
                                try {
                                    Tar.decompress(results.get(position), pkg);
                                    Toast.makeText(context, getString(R.string.ie_imported_to) + pkg, Toast.LENGTH_SHORT).show();
                                    askToQuit();
                                } catch (Exception e) {
                                    Toast.makeText(context
                                            , getString(R.string.ie_error) + e.getMessage(), Toast.LENGTH_LONG).show();
                                    e.printStackTrace();
                                }
                            };

                            offerImportProcedures(action);
                        });

                        alertDialog.show();
                    }

                });
            }
        });

        //if (root.canRead()) {
        //Toast.makeText(this, "Finding files...", Toast.LENGTH_LONG).show();

        /*new Thread(() -> {
            List<File> results = scan(root, new ArrayList<>());
            if (results.isEmpty()) {
                runOnUiThread(() -> Toast.makeText(ImportExportActivity.this,
                        String.format("No archives found in %s", root.getAbsolutePath()),
                        Toast.LENGTH_LONG).show());
            } else {
                runOnUiThread(() -> {

                    AlertDialog.Builder adb = new AlertDialog.Builder(ImportExportActivity.this);

                    ListView lv = new ListView(ImportExportActivity.this);

                    adb.setTitle("Found archives")
                            .setView(lv)
                            .setNegativeButton(android.R.string.cancel, null);
                    AlertDialog alertDialog = adb.create();
                    lv.setAdapter(new ArrayAdapter<>(ImportExportActivity.this, android.R.layout.simple_list_item_1, results));
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            alertDialog.dismiss();
                            File pkg = new File(getApplicationInfo().dataDir);
                            try {
                                Tar.decompress(results.get(position), pkg);
                                Toast.makeText(ImportExportActivity.this, "Imported to " + pkg, Toast.LENGTH_SHORT).show();
                                askToQuit();
                            } catch (Exception e) {
                                Toast.makeText(ImportExportActivity.this
                                        , "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    });

                    alertDialog.show();
                });
            }
        }).start();*/
        //}
    }

/*    List<File> scan(File dir, List<File> found) {

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
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CODE_PICK_ARCHIVE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null) {
                        //extract(uri);
                        offerImportProcedures(() -> extract(uri));
                    }
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == CODE_REQUEST_STORAGE) {
            if (grantResults[0] == PermissionChecker.PERMISSION_GRANTED
                    && grantResults[1] == PermissionChecker.PERMISSION_GRANTED) {
                showArchives();
            } else {
                Toast.makeText(this, getString(R.string.no_storage_permission_prompt), Toast.LENGTH_LONG).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}