import 'dart:typed_data';
import 'package:dio/dio.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:image_save/image_save.dart';

void main() {
  const MethodChannel channel = MethodChannel('image_save');
  var _data;

  setUp(() async {
    Response<List<int>> res = await Dio().get<List<int>>(
        "http://img.youai123.com/1507615921-5474.gif",
        options: Options(responseType: ResponseType.bytes));
    _data = Uint8List.fromList(res.data!);
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
    _data = null;
  });

  test('saveImage', () async {
    expect(await ImageSave.saveImage(_data, "gif", albumName: "demo"), isTrue);
  });

  test('saveImageToSandbox', () async {
    expect(await ImageSave.saveImageToSandbox(_data, "test.gif"), isTrue);
  });
}
