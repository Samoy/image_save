package com.samoy.image_save;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * ImageSavePlugin
 */
public class ImageSavePlugin implements MethodCallHandler {
  /**
   * Plugin registration.
   */
  private Context context;

  private ImageSavePlugin(Context context) {
    this.context = context;
  }

  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "image_save");
    channel.setMethodCallHandler(new ImageSavePlugin(registrar.context()));
  }

  @Override
  public void onMethodCall(MethodCall call,@NonNull Result result) {
    if (call.method.equals("saveImage")) {
      byte[] data = call.argument("imageData");
      String imageType = call.argument("imageType");
      result.success(saveImage(imageType, data));
    } else {
      result.notImplemented();
    }
  }

  private String saveImage(String imageType, byte[] data) {
    String name = "IMAGE_" + new Date().getTime() + "." + imageType;
    String appName = getApplicationName();
    String parentPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + appName;
    File parentDir = new File(parentPath);
    if (!parentDir.exists()) {
      parentDir.mkdir();
    }
    File file = new File(parentDir, name);
    try {
      FileOutputStream fos = new FileOutputStream(file);
      fos.write(data);
      fos.close();
      context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file.getAbsoluteFile())));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return file.getAbsolutePath();
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

}
