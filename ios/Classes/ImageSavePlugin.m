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
        NSString *imageExtension = call.arguments[@"imageExtension"];
        NSString *albumName = call.arguments[@"albumName"];
        FlutterStandardTypedData *imageData = call.arguments[@"imageData"];
        PHAuthorizationStatus authorizationStatus = [PHPhotoLibrary authorizationStatus];
        if (authorizationStatus == PHAuthorizationStatusAuthorized) {
            [self saveImageWithImageExtension:imageExtension imageData:imageData albumName:albumName result:result];
        } else if (authorizationStatus == PHAuthorizationStatusNotDetermined) {
            [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
                if (status == PHAuthorizationStatusAuthorized) {
                    [self saveImageWithImageExtension:imageExtension imageData:imageData albumName:albumName result:result];
                }
            }];
        } else if(authorizationStatus == PHAuthorizationStatusDenied){
            FlutterError *error = [FlutterError errorWithCode:@"0" message:@"Permission denied" details:nil];
            result(error);
        }
    } else if([@"saveImageToSandbox" isEqualToString:call.method]){
        FlutterStandardTypedData *data = call.arguments[@"imageData"];
        NSString *imageName = call.arguments[@"imageName"];
        [self saveImageToSandBoxWithImageData:data imageName:imageName result:result];
    }else if([@"getImagesFromSandbox" isEqualToString:call.method]){
        [self getImagesFromSandboxWithResult:result];
    }else{
        result(FlutterMethodNotImplemented);
    }
}

- (void)getImagesFromSandboxWithResult:(FlutterResult)result{
    NSString *pictureDir = [[NSHomeDirectory() stringByAppendingPathComponent:@"Documents"] stringByAppendingPathComponent:@"Pictures/"];
    NSArray *pictures = [[NSFileManager defaultManager] contentsOfDirectoryAtPath:pictureDir error:nil];
    if (pictures.count == 0) {
        FlutterError *error = [FlutterError errorWithCode:@"2" message:@"File not found" details:nil];
        result(error);
        return;
    }
    BOOL isDir;
    BOOL isExist = NO;
    NSMutableArray<FlutterStandardTypedData*> *imageDatas = [NSMutableArray array];
    
    for (NSString *path in pictures) {
        NSString *fullPath = [NSString stringWithFormat:@"%@/%@", pictureDir, path];
        isExist = [[NSFileManager defaultManager] fileExistsAtPath:fullPath isDirectory:&isDir];
        if (isExist&&!isDir) {
            NSData *data = [NSData dataWithContentsOfFile:fullPath];
            FlutterStandardTypedData *flutterData = [FlutterStandardTypedData typedDataWithBytes:data];
            [imageDatas addObject:flutterData];
        }
    }
    result(imageDatas);
}

- (void)saveImageToSandBoxWithImageData:(FlutterStandardTypedData *)imageData imageName:(NSString*)imageName result:(FlutterResult)result{
    NSString *pictureDir = [[NSHomeDirectory() stringByAppendingPathComponent:@"Documents"] stringByAppendingPathComponent:@"Pictures"];
    NSString *picturePath = [pictureDir stringByAppendingPathComponent:[NSString stringWithFormat:@"%@",imageName]];
    BOOL isDir;
    if (![[NSFileManager defaultManager] fileExistsAtPath:picturePath isDirectory:&isDir]) {
        [[NSFileManager defaultManager] createDirectoryAtPath:pictureDir withIntermediateDirectories:YES attributes:nil error:nil];
    };
    
    UIImage *currentImage  = [UIImage imageWithData:imageData.data];
    NSData *data = UIImagePNGRepresentation(currentImage);
    BOOL success = [data writeToFile:picturePath atomically:YES];
    result(@(success));
}

-(void)saveImageWithImageExtension:(NSString*) imageExtension imageData:(FlutterStandardTypedData*) imageData albumName:(NSString *)albumName result:(FlutterResult)result{
    __block NSString* localId;
    [[PHPhotoLibrary sharedPhotoLibrary] performChanges:^{
        PHAssetCreationRequest *assetChangeRequest = [PHAssetCreationRequest creationRequestForAsset];
        [assetChangeRequest addResourceWithType:PHAssetResourceTypePhoto data:imageData.data options:nil];
        PHObjectPlaceholder *placeholder = [assetChangeRequest placeholderForCreatedAsset];
        localId = placeholder.localIdentifier;
        if(![albumName isEqual:[NSNull null]]){
            PHAssetCollectionChangeRequest *collectionRequest;
            PHAssetCollection *assetCollection = [self getCurrentPhotoCollectionWithTitle:albumName];
            if (assetCollection) {
                collectionRequest = [PHAssetCollectionChangeRequest changeRequestForAssetCollection:assetCollection];
            } else {
                collectionRequest = [PHAssetCollectionChangeRequest creationRequestForAssetCollectionWithTitle:albumName];
            }
            [collectionRequest addAssets:@[placeholder]];
        }
    } completionHandler:^(BOOL success, NSError *error) {
        if(error !=  nil){
            result([FlutterError errorWithCode:[NSString stringWithFormat:@"%ld",error.code] message:error.description details:error.localizedFailureReason]);
            return;
        }
        if (success) {
            PHFetchResult* assetResult = [PHAsset fetchAssetsWithLocalIdentifiers:@[localId] options:nil];
            PHAsset *asset = [assetResult firstObject];
            [asset requestContentEditingInputWithOptions:nil completionHandler:^(PHContentEditingInput * _Nullable contentEditingInput, NSDictionary * _Nonnull info) {
                result(@YES);
            }];
        } else {
            result(@NO);
        }
    }];
}

- (PHAssetCollection *)getCurrentPhotoCollectionWithTitle:(NSString *)collectionName {
    for (PHAssetCollection *assetCollection in [self getAlbumGroup]) {
        if ([assetCollection.localizedTitle containsString:collectionName]) {
            return assetCollection;
        }
    }
    return nil;
}

-(PHFetchResult<PHAssetCollection *> *)getAlbumGroup{
    return [PHAssetCollection fetchAssetCollectionsWithType:PHAssetCollectionTypeAlbum subtype:PHAssetCollectionSubtypeAlbumRegular options:nil];
}


@end
