package com.samoy.image_save;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

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
		if ("saveImage".equals(call.method)) {
			if (ActivityCompat.checkSelfPermission(registrar.activity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
				methodCall();
				return;
			}
			ActivityCompat.requestPermissions(registrar.activity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_CODE);
			registrar.addRequestPermissionsResultListener(this);
		} else {
			result.notImplemented();
		}
	}

	private void methodCall() {
		byte[] data = call.argument("imageData");
		String imageExtension = call.argument("imageExtension");
		String albumName = call.argument("albumName");
		result.success(saveImage(data, imageExtension, albumName));
	}

	private Boolean saveImage(byte[] data, String imageExtension, String albumName) {
		String name = "IMAGE_" + new Date().getTime() + "." + imageExtension;
		if (albumName == null) {
			albumName = getApplicationName();
		}
		String parentPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + albumName;
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
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
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
			methodCall();
		} else {
			result.error("0", "Permission denied", null);
		}
		return granted;
	}
}
