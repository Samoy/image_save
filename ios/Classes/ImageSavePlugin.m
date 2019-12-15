#import "ImageSavePlugin.h"
#import <Photos/Photos.h>

@implementation ImageSavePlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  FlutterMethodChannel* channel = [FlutterMethodChannel
      methodChannelWithName:@"image_save"
            binaryMessenger:[registrar messenger]];
  ImageSavePlugin* instance = [[ImageSavePlugin alloc] init];
  [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
  if ([@"saveImage" isEqualToString:call.method]) {
      NSString *imageType = call.arguments[@"imageType"];
      FlutterStandardTypedData *imageData = call.arguments[@"imageData"];
      [self saveImageWithImageType:imageType imageData:imageData result:result];
  } else {
    result(FlutterMethodNotImplemented);
  }
}

-(void)saveImageWithImageType:(NSString*) imageType imageData:(FlutterStandardTypedData*) imageData result:(FlutterResult)result{
    __block NSString* fileName;
    __block NSString* localId;
    __block PHAssetChangeRequest *assetChangeRequest = nil;
    [[PHPhotoLibrary sharedPhotoLibrary] performChanges:^{
        PHAssetCreationRequest *assetChangeRequest = [PHAssetCreationRequest creationRequestForAsset];
        [assetChangeRequest addResourceWithType:PHAssetResourceTypePhoto data:imageData.data options:nil];
        localId = [[assetChangeRequest placeholderForCreatedAsset] localIdentifier];
    } completionHandler:^(BOOL success, NSError *error) {
        if(error.code == NSValidationErrorMaximum){
            [self saveImageWithImageType:imageType imageData:imageData result:result];
            return;
        }
        if (success) {
            PHFetchResult* assetResult = [PHAsset fetchAssetsWithLocalIdentifiers:@[localId] options:nil];
            PHAsset *asset = [assetResult firstObject];
            [asset requestContentEditingInputWithOptions:nil completionHandler:^(PHContentEditingInput * _Nullable contentEditingInput, NSDictionary * _Nonnull info) {
                fileName = contentEditingInput.fullSizeImageURL.absoluteString;
                result(fileName);
            }];
        } else {
            result(fileName);
        }
    }];
}


@end
