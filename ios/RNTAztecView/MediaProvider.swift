import Aztec
import Foundation

class MediaProvider: Aztec.TextViewAttachmentDelegate {
    
    func textView(_ textView: TextView, attachment: NSTextAttachment, imageAt url: URL, onSuccess success: @escaping (UIImage) -> Void, onFailure failure: @escaping () -> Void) {
    
        DispatchQueue.main.async {
            let image = UIImage()
            
            success(image)
        }
    }
    
    func textView(_ textView: TextView, urlFor imageAttachment: ImageAttachment) -> URL? {
        return URL(string: "www.google.com")
    }
    
    func textView(_ textView: TextView, placeholderFor attachment: NSTextAttachment) -> UIImage {
        return UIImage()
    }
    
    func textView(_ textView: TextView, deletedAttachment attachment: MediaAttachment) {
        textView.setNeedsDisplay()
    }
    
    func textView(_ textView: TextView, selected attachment: NSTextAttachment, atPosition position: CGPoint) {
    }
    
    func textView(_ textView: TextView, deselected attachment: NSTextAttachment, atPosition position: CGPoint) {
    }
    
    
}

extension MediaProvider: Aztec.TextViewAttachmentImageProvider {
    func textView(_ textView: TextView, shouldRender attachment: NSTextAttachment) -> Bool {
        return true
    }
    
    func textView(_ textView: TextView, boundsFor attachment: NSTextAttachment, with lineFragment: CGRect) -> CGRect {
        return CGRect(x: 0, y: 0, width: 0, height: 0)
    }
    
    func textView(_ textView: TextView, imageFor attachment: NSTextAttachment, with size: CGSize) -> UIImage? {
        let image = UIImage()
        
        return image;
        
//        return imageWithImage(image: image, scaledToSize: size)
    }
    
//    func imageWithImage(image:UIImage, scaledToSize newSize:CGSize) -> UIImage{
//        UIGraphicsBeginImageContextWithOptions(newSize, false, 0.0);
//        image.draw(in: CGRect(origin: CGPoint.zero, size: CGSize(width: newSize.width, height: newSize.height)))
//        let newImage:UIImage = UIGraphicsGetImageFromCurrentImageContext()!
//        UIGraphicsEndImageContext()
//        return newImage
//    }
    
}
