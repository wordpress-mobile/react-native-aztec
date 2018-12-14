package org.wordpress.mobile.ReactNativeAztec;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

/**
 * Event emitted by Aztec native view when attributes detected.
 */

public class ReactAztecFormattingAttributesChange extends Event<ReactAztecFormattingChangeEvent> {

    private static final String EVENT_NAME = "topFormatAttributeChanges";

    @Override
    public String getEventName() {
        return EVENT_NAME;
    }

    @Override
    public boolean canCoalesce() {
        return false;
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
    }

    private WritableMap serializeEventData() {

        WritableMap linkData = Arguments.createMap();
        linkData.putBoolean("isActive", true);
        linkData.putString("url", "wp.com");

        WritableMap attributesData = Arguments.createMap();
        attributesData.putMap("link", linkData);

        WritableMap eventData = Arguments.createMap();
        eventData.putMap("attributes", attributesData);

        return eventData;
    }
}
