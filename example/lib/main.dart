import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'dart:async';
import 'package:dio/dio.dart';
import 'package:image_save/image_save.dart';

void main() => runApp(MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _result = "";

  @override
  void initState() {
    super.initState();
  }

  // Platform messages are asynchronous, so we initialize in an async method.
  Future<void> _saveImage() async {
    bool success = false;
    Response<List<int>> res = await Dio().get<List<int>>(
        "http://img.youai123.com/1507615921-5474.gif",
        options: Options(responseType: ResponseType.bytes));
    // Platform messages may fail, so we use a try/catch PlatformException.
    try {
      success = await ImageSave.saveImage(Uint8List.fromList(res.data), "gif",
          albumName: "demo");
    } on Exception catch (e, s) {
      print(e);
      print(s);
    }
    setState(() {
      _result = success ? "Success" : "Failed";
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
              Image.network('http://img.youai123.com/1507615921-5474.gif'),
              RaisedButton(
                onPressed: _saveImage,
                child: Text("Click to save"),
              ),
              Text(_result)
            ],
          ),
        ),
      ),
    );
  }
}
