# image_save

Save image to album, support Android and iOS.

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
See [Example](https://github.com/Samoy/image_save/tree/master/example)

```
bool success = ImageSave.saveImage(Uint8List.fromList(res.data), "gif", albumName: "demo");
```
