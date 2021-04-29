import Aztec
import Foundation

@objc (RCTAztecViewManager)
public class RCTAztecViewManager: RCTViewManager {

    public static var attachmentDelegate: Aztec.TextViewAttachmentDelegate?
    public static var imageProvider: Aztec.TextViewAttachmentImageProvider?
    
    @objc
    public static var defaultFont: UIFont?
    
    public override static func requiresMainQueueSetup() -> Bool {
        return true
    }

    @objc
    func applyFormat(_ node: NSNumber, format: String) {
        executeBlock({ (aztecView) in
            aztecView.apply(format: format)
        }, onNode: node)
    }
    
    @objc
    func focusTextInput(_ node: NSNumber) {
        executeBlock({ (aztecView) in
            aztecView.becomeFirstResponder()
        }, onNode: node)
    }

    @objc
    func blurTextInput(_ node: NSNumber) {
        executeBlock({ (aztecView) in
            aztecView.hideKeyboard()
        }, onNode: node)
    }

    @objc
    func removeLink(_ node: NSNumber) {
        executeBlock({ (aztecView) in
            aztecView.removeLink()
        }, onNode: node)
    }

    @objc
    func setLink(_ node: NSNumber, url: String, title: String?) {
        executeBlock({ (aztecView) in
            aztecView.setLink(with: url, and: title)
        }, onNode: node)
    }
    
    @objc
    public override func view() -> UIView {
        if (RCTAztecViewManager.defaultFont == nil) {
            RCTAztecViewManager.defaultFont = UIFont(name: "Inter-Regular", size: 16.0);
        }
        let view = RCTAztecView(
            defaultFont: defaultFont,
            defaultParagraphStyle: .default,
            defaultMissingImage: UIImage())

        view.isScrollEnabled = true
        
        view.autocorrectionType = .no
        
        let defaultMediaProvider = MediaProvider()
        
        if (RCTAztecViewManager.attachmentDelegate == nil) {
            RCTAztecViewManager.attachmentDelegate = defaultMediaProvider
        }
        
        if (RCTAztecViewManager.imageProvider == nil) {
            RCTAztecViewManager.imageProvider = defaultMediaProvider
        }

        view.textAttachmentDelegate = RCTAztecViewManager.attachmentDelegate
        
        if let imageProvider = RCTAztecViewManager.imageProvider {
            view.registerAttachmentImageProvider(imageProvider)
        }
        
        if #available(iOS 13, *) {
            view.overrideUserInterfaceStyle = .light
        }
    
        return view
    }

    func executeBlock(_ block: @escaping (RCTAztecView) -> Void, onNode node: NSNumber) {
        self.bridge.uiManager.addUIBlock { (manager, viewRegistry) in
            let view = viewRegistry?[node]
            guard let aztecView = view as? RCTAztecView else {
                return
            }
            block(aztecView)
        }
    }

     private var defaultFont: UIFont {
         if let font = UIFont(name: "NotoSerif", size: 16) {
             return font
         }

         let defaultFont = UIFont.systemFont(ofSize: 16)
         guard let url = Bundle.main.url(forResource: "NotoSerif-Regular", withExtension: "ttf") else {
             return defaultFont
         }
         CTFontManagerRegisterFontsForURL(url as CFURL, CTFontManagerScope.process, nil)
         if let font = UIFont(name: "NotoSerif", size: 16) {
             return font
         }

         return defaultFont
     }
}
