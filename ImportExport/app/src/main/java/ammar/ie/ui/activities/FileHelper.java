package ammar.ie.ui.activities;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.widget.Toast;

import java.util.List;

public class FileHelper {
    private static Boolean hasOpenDocumentIntent;

    public static void openDocument(Activity activity, String mimeType, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && hasOpenDocumentIntent(activity)) {
            openDocumentKitKat(activity, mimeType, requestCode);
        } else {
            openDocumentPreKitKat(activity, mimeType, requestCode);
        }
    }

    /**
     * Opens the preferred installed file manager on Android and shows a toast
     * if no manager is installed.
     */
    private static void openDocumentPreKitKat(Activity activity, String mimeType, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);

        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Opens the storage browser on Android 4.4 or later for opening a file
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void openDocumentKitKat(Activity activity, String mimeType, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mimeType);
        // Note: This is not documented, but works: Show the Internal Storage menu item in the drawer!
        intent.putExtra("android.content.extra.SHOW_ADVANCED", true);

        activity.startActivityForResult(intent, requestCode);
    }

    /**
     * Does the device actually have a ACTION_OPEN_DOCUMENT Intent? Looks like some Android
     * distributions are missing the ACTION_OPEN_DOCUMENT Intent even on Android 4.4,
     * see https://github.com/open-keychain/open-keychain/issues/1625
     *
     * @return True, if the device supports ACTION_OPEN_DOCUMENT. False, otherwise.
     */
    @TargetApi(VERSION_CODES.KITKAT)
    private static boolean hasOpenDocumentIntent(Context context) {
        if (hasOpenDocumentIntent == null) {
            PackageManager packageManager = context.getPackageManager();
            List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(
                    new Intent(Intent.ACTION_OPEN_DOCUMENT), 0);
            hasOpenDocumentIntent = !resolveInfoList.isEmpty();
        }

        return hasOpenDocumentIntent;
    }
}
