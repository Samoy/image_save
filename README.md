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
// Save to album.
bool success = await ImageSave.saveImage(data, "gif", albumName: "demo");

// Save to sandbox.
// Notice: Image saved in this way will be deleted when the application is uninstalled.
bool success = await ImageSave.saveImageToSandbox(data, "demo.gif");
```
