import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/services.dart';

class ImageSave {
  static const MethodChannel _channel = const MethodChannel('image_save');

  static Future<String> saveImage(String imageType, Uint8List imageData) async {
    final String path = await _channel.invokeMethod(
        'saveImage', {"imageType": imageType, "imageData": imageData});
    return path;
  }
}
