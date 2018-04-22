import Aztec
import UIKit

@objc (RCTAztecViewManager)
class RCTAztecViewManager: RCTViewManager {
    
    @objc override func view() -> UIView {
        let view = Aztec.TextView.init(
            defaultFont: .systemFont(ofSize: 12),
            defaultParagraphStyle: .default,
            defaultMissingImage: UIImage())
        
        view.backgroundColor = .blue
        view.text = "Hello world!"
        
        return view
    }
}
