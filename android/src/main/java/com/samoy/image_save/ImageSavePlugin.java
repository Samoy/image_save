package com.samoy.image_save;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import static android.os.Environment.DIRECTORY_PICTURES;

/**
 * ImageSavePlugin
 */
public class ImageSavePlugin implements MethodCallHandler, PluginRegistry.RequestPermissionsResultListener {
    /**
     * Plugin registration.
     */
    private Context context;
    private static final int REQ_CODE = 100;
    private static Registrar registrar;
    private MethodCall call;
    private Result result;

    private ImageSavePlugin(Context context) {
        this.context = context;
    }

    public static void registerWith(Registrar r) {
        registrar = r;
        MethodChannel channel = new MethodChannel(registrar.messenger(), "image_save");
        channel.setMethodCallHandler(new ImageSavePlugin(r.context()));
    }

    @Override
    public void onMethodCall(final MethodCall call, Result result) {
        this.call = call;
        this.result = result;
        if (ActivityCompat.checkSelfPermission(registrar.activity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            methodCall(call, result);
        } else {
            ActivityCompat.requestPermissions(registrar.activity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_CODE);
            registrar.addRequestPermissionsResultListener(this);
        }
    }

    private void methodCall(MethodCall call, Result result) {
        switch (call.method) {
            case "saveImage":
                saveImageCall();
                break;
            case "saveImageToSandbox":
                saveImageToSandboxCall();
                break;
            case "getImagesFromSandbox":
                getImagesFromSandboxCall();
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void saveImageCall() {
        byte[] data = call.argument("imageData");
        String imageName = call.argument("imageName");
        String albumName = call.argument("albumName");
        Boolean overwriteSameNameFile = call.argument("overwriteSameNameFile");
        try {
            result.success(saveImage(data, imageName, albumName, overwriteSameNameFile));
        } catch (IOException e) {
            result.error("2", e.getMessage(), "The file '" + imageName + "' already exists");
        }
    }

    private Boolean saveImage(byte[] data, String imageName, String albumName, Boolean overwriteSameNameFile) throws IOException {
        if (albumName == null) {
            albumName = getApplicationName();
        }
        if(Build.VERSION.SDK_INT >= 29){
            String mimeType = URLConnection.getFileNameMap().getContentTypeFor(imageName);
            String fileName = imageName;
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME,fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if(uri == null){
                return false;
            }
            try {
                OutputStream out = contentResolver.openOutputStream(uri);
                out.write(data);
                out.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
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
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file.getAbsoluteFile())));
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private void saveImageToSandboxCall() {
        byte[] data = call.argument("imageData");
        String imageName = call.argument("imageName");
        saveImageToSandbox(data, imageName);
    }

    private void saveImageToSandbox(byte[] data, String imageName) {
        File files = context.getExternalFilesDir(DIRECTORY_PICTURES);
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
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file.getAbsoluteFile())));
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
        File files = context.getExternalFilesDir(DIRECTORY_PICTURES);
        if (files != null) {
            for (File file : files.listFiles()) {
                try {
                    images.add(getContent(file.getPath()));
                } catch (IOException e) {
                    result.error("2", e.getMessage(), e.getCause());
                }
            }
        }
        return images;
    }

    public byte[] getContent(String filePath) throws IOException {
        File file = new File(filePath);
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            System.out.println("file too big...");
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


    private String getApplicationName() {
        PackageManager packageManager = null;
        ApplicationInfo applicationInfo = null;
        try {
            packageManager = context.getPackageManager();
            applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return (String) packageManager.getApplicationLabel(applicationInfo);
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean granted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
        if (granted) {
            methodCall(call, result);
        } else {
            result.error("0", "Permission denied", null);
        }
        return granted;
    }
}
