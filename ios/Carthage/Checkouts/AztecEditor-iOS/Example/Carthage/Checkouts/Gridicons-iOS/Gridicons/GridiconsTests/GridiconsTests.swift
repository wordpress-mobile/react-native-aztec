//
//  GridiconsTests.swift
//  GridiconsTests
//
//  Created by James Frost on 04/04/2016.
//  Copyright © 2016 Automattic. All rights reserved.
//

import XCTest
@testable import Gridicons

class GridiconsTests: XCTestCase {
    
    override func setUp() {
        super.setUp()
        Gridicon.clearCache()
    }
    
    func testIconsAreCached() {
        let icon = Gridicon.iconOfType(.addImage)
        let icon2 = Gridicon.iconOfType(.addImage)
        
        XCTAssertEqual(icon, icon2)
        
        Gridicon.clearCache()
        let icon3 = Gridicon.iconOfType(.addImage)
        
        XCTAssertNotEqual(icon2, icon3)
    }
    
    func testIconsAreTheCorrectSize() {
        let icon = Gridicon.iconOfType(.domains)
        XCTAssertEqual(icon.size, Gridicon.defaultSize)
        
        let size = CGSize(width: 250, height: 250)
        let icon2 = Gridicon.iconOfType(.userCircle, withSize: size)
        XCTAssertEqual(icon2.size, size)
    }
    
    func testSingleIconGenerationPerformance() {
        self.measure {
            let _ = Gridicon.iconOfType(.pages)
        }
    }
    
    func testAllIconGenerationPerformance() {
        let iconTypes: [GridiconType] = {
            var types = [GridiconType]()
            while let type = GridiconType(rawValue: types.count) {
                types.append(type)
            }
            
            return types
        }()
        
        self.measure {
            let _ = iconTypes.map { Gridicon.iconOfType($0) }
        }
    }
}
