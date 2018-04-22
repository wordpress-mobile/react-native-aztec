import XCTest
@testable import Aztec

class CSSParserTests: XCTestCase {

    /// Tests parsing input CSS: "color: blue;"
    ///
    func testParsingCSS() {

        let cssAttributeName = "color"
        let cssAttributeValue = "blue"

        let parser = CSSParser()
        let cssString = "\(cssAttributeName):\(cssAttributeValue);"

        let attributes = parser.parse(cssString)

        XCTAssertTrue(attributes.contains(CSSAttribute(name: cssAttributeName, value: cssAttributeValue)))
    }

    /// Tests parsing input CSS: "color: blue"
    ///
    func testParsingCSSWithoutClosingSemicolon() {

        let cssAttributeName = "color"
        let cssAttributeValue = "blue"

        let parser = CSSParser()
        let cssString = "\(cssAttributeName):\(cssAttributeValue)"

        let attributes = parser.parse(cssString)

        XCTAssertTrue(attributes.contains(CSSAttribute(name: cssAttributeName, value: cssAttributeValue)))
    }

    /// Tests parsing input CSS: "  color  :  blue  ;  "
    ///
    func testParsingCSSWithExtraSpaces() {

        let cssAttributeName = "color"
        let cssAttributeValue = "blue"

        let parser = CSSParser()
        let cssString = "  \(cssAttributeName)  :  \(cssAttributeValue)  ;  "

        let attributes = parser.parse(cssString)

        XCTAssertTrue(attributes.contains(CSSAttribute(name: cssAttributeName, value: cssAttributeValue)))
    }

    /// Tests parsing input CSS: "color: blue; text-decoration: underline;"
    ///
    func testParsingCSSWithMultipleValues() {

        let inputAttributes = [
            "color": "blue",
            "text-decoration": "underline"
        ]

        let parser = CSSParser()

        let cssString = inputAttributes.reduce("") { (previous, nextAttribute) -> String in
            return previous + nextAttribute.key + ": " + nextAttribute.value + "; "
        }

        let attributes = parser.parse(cssString)

        XCTAssertEqual(inputAttributes.count, attributes.count)

        for inputAttribute in inputAttributes {
            XCTAssertTrue(attributes.contains(CSSAttribute(name: inputAttribute.key, value: inputAttribute.value)))
        }
    }

    /// Tests parsing input CSS: "color  :  blue  ;  text-decoration  :  underline  ;  "
    ///
    func testParsingCSSWithMultipleValuesAndExtraSpaces() {

        let inputAttributes = [
            "color": "blue",
            "text-decoration": "underline"
        ]

        let parser = CSSParser()

        let cssString = inputAttributes.reduce("") { (previous, nextAttribute) -> String in
            return previous + nextAttribute.key + "  :  " + nextAttribute.value + "  ;  "
        }

        let attributes = parser.parse(cssString)

        XCTAssertEqual(inputAttributes.count, attributes.count)

        for inputAttribute in inputAttributes {
            XCTAssertTrue(attributes.contains(CSSAttribute(name: inputAttribute.key, value: inputAttribute.value)))
        }
    }
}
