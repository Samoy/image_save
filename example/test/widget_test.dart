// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility that Flutter provides. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'dart:typed_data';
import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:image_save/image_save.dart';

void main() {
  var _data;

  setUp(() async {
    Response<List<int>> res = await Dio().get<List<int>>(
        "http://img.youai123.com/1507615921-5474.gif",
        options: Options(responseType: ResponseType.bytes));
    _data = Uint8List.fromList(res.data);
  });

  tearDown(() {
    _data = null;
  });

  test('saveImage', () async {
    expect(await ImageSave.saveImage(_data, "gif", albumName: "demo"), isTrue);
  });

  test('saveImageToSandbox', () async {
    expect(await ImageSave.saveImageToSandbox(_data, "test.gif"), isTrue);
  });
}
