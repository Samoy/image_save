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
    } on PlatformException catch (e){
    	print(e);
		}
    return success;
  }
}
