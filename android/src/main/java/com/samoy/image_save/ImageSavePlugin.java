package com.samoy.image_save;

import static android.os.Environment.DIRECTORY_PICTURES;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/**
 * ImageSavePlugin
 */
public class ImageSavePlugin implements MethodCallHandler, FlutterPlugin, ActivityAware, PluginRegistry.RequestPermissionsResultListener {
    private Context applicationContext;
    private static final int REQ_CODE = 100;
    private MethodCall call;
    private Result result;
    private MethodChannel channel;
    private ActivityPluginBinding activityPluginBinding;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        this.applicationContext = flutterPluginBinding.getApplicationContext();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "image_save");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        applicationContext = null;
        channel.setMethodCallHandler(null);
        channel = null;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        this.call = call;
        this.result = result;
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            methodCall(call, result);
        } else {
            ActivityCompat.requestPermissions(activityPluginBinding.getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_CODE);
            activityPluginBinding.addRequestPermissionsResultListener(this);
        }
    }

    private void methodCall(MethodCall call, Result result) {
        byte[] data = call.argument("imageData");
        String imageName = call.argument("imageName");
        String albumName = call.argument("albumName");
        Boolean overwriteSameNameFile = call.argument("overwriteSameNameFile");
        switch (call.method) {
            case "saveImage":
                saveImageCall(data, imageName, albumName, overwriteSameNameFile);
                break;
            case "saveImageToSandbox":
                saveImageToSandboxCall(data, imageName);
                break;
            case "getImagesFromSandbox":
                getImagesFromSandboxCall();
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void saveImageCall(byte[] data, String imageName, String albumName, Boolean overwrite) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10
            ContentResolver resolver = applicationContext.getContentResolver();
            Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentValues contentValues = new ContentValues();
            String displayName = contentValues.getAsString(MediaStore.Images.Media.DISPLAY_NAME);
            if (TextUtils.equals(displayName, imageName)) {
                result.error("2", "Duplicate image name", "The file '" + imageName + "' already exists");
            }
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, imageName);
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, URLConnection.getFileNameMap().getContentTypeFor(imageName));
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, DIRECTORY_PICTURES + "/" + albumName);
            Uri uri = resolver.insert(contentUri, contentValues);
            if (uri == null) {
                result.error("2", "File not found", "The file '" + imageName + "' saves failed");
                return;
            }
            try {
                OutputStream os = resolver.openOutputStream(uri);
                os.write(data);
                os.flush();
                os.close();
                result.success(true);
            } catch (IOException e) {
                result.error("2", e.getMessage(), "The file '" + imageName + "' saves failed");
            }
            MediaScannerConnection.scanFile(applicationContext, new String[]{contentUri.getPath()}, new String[]{"images/*"}, null);
        } else {
            try {
                result.success(saveImage(data, imageName, albumName, overwrite));
            } catch (IOException e) {
                result.error("2", e.getMessage(), "The file '" + imageName + "' already exists");
            }
        }
    }

    private Boolean saveImage(byte[] data, String imageName, String albumName, Boolean overwriteSameNameFile) throws IOException {
        if (albumName == null) {
            albumName = getApplicationName();
        }
        File parentDir = new File(Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES), albumName);
        if (!parentDir.exists()) {
            parentDir.mkdir();
        }
        File file = new File(parentDir, imageName);
        if (!overwriteSameNameFile) {
            if (file.exists()) {
                throw new IOException("File already exists");
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.close();
            applicationContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file.getAbsoluteFile())));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


    private void saveImageToSandboxCall(byte[] data, String imageName) {
        saveImageToSandbox(data, imageName);
    }

    private void saveImageToSandbox(byte[] data, String imageName) {
        File files = applicationContext.getExternalFilesDir(DIRECTORY_PICTURES);
        if (files == null) {
            result.error("-1", "No SD Card found.", "Couldn't obtain external storage.");
            return;
        }
        String filesDirPath = files.getPath();

        File parentDir = new File(filesDirPath);
        if (!parentDir.exists()) {
            parentDir.mkdir();
        }
        File file = new File(parentDir, imageName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();
            fos.close();
            applicationContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file.getAbsoluteFile())));
            result.success(true);
        } catch (IOException e) {
            result.error("1", e.getMessage(), e.getCause());
        }
    }

    private void getImagesFromSandboxCall() {
        result.success(getImagesFromSandbox());
    }

    private List<byte[]> getImagesFromSandbox() {
        List<byte[]> images = new ArrayList<>();
        File files = applicationContext.getExternalFilesDir(DIRECTORY_PICTURES);
        if (files != null) {
            File[] fileList = files.listFiles();
            if (fileList != null) {
                for (File file : fileList) {
                    try {
                        if (!file.isDirectory()) {
                            images.add(getContent(file.getPath()));
                        }
                    } catch (IOException e) {
                        result.error("2", e.getMessage(), e.getCause());
                    }
                }
            }
        }
        return images;
    }

    public byte[] getContent(String filePath) throws IOException {
        File file = new File(filePath);
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            System.out.println("File too big...");
            return null;
        }
        FileInputStream fi = new FileInputStream(file);
        byte[] buffer = new byte[(int) fileSize];
        int offset = 0;
        int numRead = 0;
        while (offset < buffer.length
                && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
            offset += numRead;
        }
        if (offset != buffer.length) {
            throw new IOException("Could not completely read file "
                    + file.getName());
        }
        fi.close();
        return buffer;
    }


    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding activityPluginBinding) {
        this.activityPluginBinding = activityPluginBinding;
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {

    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding activityPluginBinding) {

    }

    @Override
    public void onDetachedFromActivity() {
        this.activityPluginBinding = null;
    }

    @Override
    public boolean onRequestPermissionsResult(int i, String[] strings, int[] grantResults) {
        boolean granted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (granted) {
            methodCall(call, result);
        } else {
            result.error("0", "Permission denied", null);
        }
        return granted;
    }

    private String getApplicationName() {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = applicationContext.getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(applicationContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return (String) packageManager.getApplicationLabel(applicationInfo);
    }
}
