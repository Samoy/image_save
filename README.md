# image_save

Save image to album

## Permission

* ### Android

Add the following statement in `AndroidManifest.xml`:
```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
* ### iOS

Add the following statement in `Info.plist`
```
<key>NSPhotoLibraryUsageDescription</key>
<string>Modify the description of the permission you need here.</string>
```

## Usage
See [Example](https://github.com/Samoy/image_saver/tree/master/example)

```
String path = await mageSaver.saveImage("gif", Uint8List.fromList(data))
```