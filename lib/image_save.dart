import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

/// This is a plugin that can save image to album.
/// Support Android and iOS.
/// For Android: The path is <code>/{album name}/{image name}</code>.
/// For iOS: The path can't obtain, the image saved to a new album with name you given.
class ImageSave {
  static const MethodChannel _channel = const MethodChannel('image_save');

  /// Save Image to album.
  /// [imageData] Image data.
  /// [imageName] Only works on Android. Image name, such as a.jpg, b.gif and so on.
  /// [albumName] Album name, optional. For Android, default application name. For iOS, default system album.
  /// [overwriteSameNameFile] Only works on Android. If <code>true</code>, overwrite the original file that has same name, default <code>true</code>.
  static Future<bool> saveImage(Uint8List imageData, String imageName,
      {String albumName, overwriteSameNameFile = true}) async {
    bool success = false;
    try {
      success = await _channel.invokeMethod('saveImage', {
        "imageData": imageData,
        "imageName": imageName,
        "albumName": albumName,
        "overwriteSameNameFile": overwriteSameNameFile
      });
    } on PlatformException {
      rethrow;
    }
    return success;
  }

  /// Save Image to Sandbox.
  /// <b>Notice: Image saved in this way will be deleted when the application is uninstalled.</b>
  /// For Android, the full path is <code>/storage/emulated/0/Android/data/${application_package_name}/files/Pictures/[imageName]</code>.
  /// For iOS, the full path is <code>${NSDocumentDirectory}/Pictures/[imageName]</code>, not support dynamic images.
  /// [imageData] Image data.
  /// [imageName] Image name,contains extension, such as "demo.png".
  static Future<bool> saveImageToSandbox(
      Uint8List imageData, String imageName) async {
    bool success = false;
    try {
      success = await _channel.invokeMethod('saveImageToSandbox',
          {"imageData": imageData, "imageName": imageName});
    } on PlatformException {
      rethrow;
    }
    return success;
  }

  /// Get images from sandbox.
  static Future<List<Uint8List>> getImagesFromSandbox() async {
    List<Uint8List> images = [];
    try {
      images = await _channel.invokeListMethod("getImagesFromSandbox");
    } on PlatformException {
      rethrow;
    }
    return images;
  }
}
