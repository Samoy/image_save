# CHANGELOG

## 4.0.1
* Breaking changes:
  1. The method `saveImage` change parameter  `imageExtension` to `imageName`
  2. The method `saveImage` add parameter `overwriteSameNameFile`
* Other changes:
  1. Optimize code.

## 3.1.3
* Optimize  *CHANGELOG*

## 3.1.2
* Update example to support iOS 14.

## 3.1.1
* Fix iOS bug for saving image to album with no name.

## 3.1.0
* Add `getImagesFromSandbox()` function.

## 3.0.0
* Add platform support tags.
* Upgrade flutter version.

## 2.1.0
* Now you could save image to sandbox by `saveImageToSandBox`.
* Optimize code.

## 2.0.0
* The parameters have changed.
* No longer obtain image path you saved.
For Android, The path is <code>/{album name}/{image name}</code>.
For iOS, The path can't obtain, the image saved to a new album with name you given.
* Android: migrate to androidx.

## 1.1.3+1
* Fix bug

## 1.1.3
* Fix README.md
* Add API document.

## 1.1.2
* Update example

## 1.1.1
* Format code

## 1.1.0
* Fit iOS 13

## 1.0.1
* Fix android plugin name bug

## 1.0.0
* Change plugin name

## 0.0.1
* Save Image to album use Uint8List
* Need add permission manually
