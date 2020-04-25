import 'dart:typed_data';
import 'package:dio/dio.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:image_save/image_save.dart';

void main() {
  const MethodChannel channel = MethodChannel('image_save');

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('saveImage', () async {
    Response<List<int>> res = await Dio().get<List<int>>(
        "http://img.youai123.com/1507615921-5474.gif",
        options: Options(responseType: ResponseType.bytes));
    expect(
        await ImageSave.saveImage(Uint8List.fromList(res.data), "gif",
            albumName: "demo"),
        isTrue);
  });
}
