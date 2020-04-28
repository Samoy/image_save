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
  /// [imageExtension] Image extension, such as jpg,gif and so on.
  /// [albumName] Album name, optional. default application name.
  static Future<bool> saveImage(Uint8List imageData, String imageExtension,
      {String albumName}) async {
    bool success = false;
    try {
      success = await _channel.invokeMethod('saveImage', {
        "imageData": imageData,
        "imageExtension": imageExtension,
        "albumName": albumName
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
}
