import Aztec
import UIKit

class RCTAztecViewManager: RCTViewManager {
    
    @objc override func view() -> UIView {
        let view = UIView() // Aztec.TextView()
        
        view.backgroundColor = .blue
        
        return view
    }
}
