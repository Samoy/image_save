import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'dart:async';
import 'package:dio/dio.dart';
import 'package:flutter/services.dart';
import 'package:image_save/image_save.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _imagePath = '';

  @override
  void initState() {
    super.initState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> _saveImage() async {
    // Platform messages may fail, so we use a try/catch PlatformException.
    String imagePath = "";
    try {
      Response<List<int>> res = await Dio().get<List<int>>(
          "http://img.youai123.com/1507615921-5474.gif",
          options: Options(responseType: ResponseType.bytes));
      imagePath =
          await ImageSave.saveImage("gif", Uint8List.fromList(res.data));
    } on PlatformException {
      imagePath = '未能保存成功';
    }
    setState(() {
      _imagePath = imagePath;
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: <Widget>[
              RaisedButton(
                onPressed: _saveImage,
                child: Text("点击保存"),
              ),
              Text("保存到了$_imagePath")
            ],
          ),
        ),
      ),
    );
  }
}
