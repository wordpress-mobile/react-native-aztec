import UIKit


/// Returns a specialised representation for a `<hr>` element.
///
class HRElementConverter: AttachmentElementConverter {
    
    let serializeChildren: ChildrenSerializer
    
    required init(childrenSerializer: @escaping ChildrenSerializer) {
        self.serializeChildren = childrenSerializer
    }
    
    // MARK: - ElementConverter
    
    func canConvert(element: ElementNode) -> Bool {
        return element.standardName == .hr
    }
    
    // MARK: - AttachmentElementConverter
    
    typealias T = NSTextAttachment
    
    func convert(_ element: ElementNode, inheriting attributes: [NSAttributedStringKey: Any]) -> (attachment: NSTextAttachment, string: NSAttributedString) {
        let elementRepresentation = HTMLElementRepresentation(element)
        let representation = HTMLRepresentation(for: .element(elementRepresentation))
        
        let attributes = combine(attributes, with: representation)
        let attachment = self.attachment(for: element)
        
        return (attachment, NSAttributedString(attachment: attachment, attributes: attributes))
    }
    
    // MARK: - Attachment Creation
    
    private func attachment(for element: ElementNode) -> NSTextAttachment {
        return LineAttachment()
    }
    
    // MARK: - Additional HTMLRepresentation Logic
    
    private func combine(_ attributes: [NSAttributedStringKey: Any], with representation: HTMLRepresentation) -> [NSAttributedStringKey : Any] {
        var combinedAttributes = attributes
        
        combinedAttributes[.hrHtmlRepresentation] = representation
        
        return combinedAttributes
    }
}
