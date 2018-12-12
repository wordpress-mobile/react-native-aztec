#import <React/RCTViewManager.h>

@interface RCT_EXTERN_MODULE(RCTAztecViewManager, NSObject)

RCT_REMAP_VIEW_PROPERTY(text, contents, NSDictionary)
RCT_EXPORT_VIEW_PROPERTY(onContentSizeChange, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onBackspace, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onDelete, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onChange, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onEnter, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onFocus, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onBlur, RCTBubblingEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onSelectionChange, RCTDirectEventBlock)

RCT_EXPORT_VIEW_PROPERTY(onActiveFormatsChange, RCTBubblingEventBlock)

RCT_EXPORT_VIEW_PROPERTY(placeholder, NSString)
RCT_EXPORT_VIEW_PROPERTY(placeholderTextColor, UIColor)

RCT_EXTERN_METHOD(applyFormat:(nonnull NSNumber *)node format:(NSString *)format)

@end
