import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

///This class could save image to album.
///Support Android and iOS.
///It is ability to get storage path.
class ImageSave {
  static const MethodChannel _channel = const MethodChannel('image_save');

  /// Save Image to album.
  /// [imageType]: image type,such as jpg,gif and so on.
  /// [imageData]:Image Data.
  /// returns the path of image if image saved success.
  /// ```dart
  /// String path = await ImageSave.saveImage("gif", Uint8List.fromList(res.data));
  /// ```
  static Future<String> saveImage(String imageType, Uint8List imageData) async {
    final String path = await _channel.invokeMethod(
        'saveImage', {"imageType": imageType, "imageData": imageData});
    return path;
  }
}
