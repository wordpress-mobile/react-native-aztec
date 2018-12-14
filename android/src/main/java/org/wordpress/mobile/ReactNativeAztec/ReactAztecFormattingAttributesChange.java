package org.wordpress.mobile.ReactNativeAztec;

import android.text.TextUtils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

/**
 * Event emitted by Aztec native view when attributes detected.
 */

public class ReactAztecFormattingAttributesChange extends Event<ReactAztecFormattingChangeEvent> {

    private static final String EVENT_NAME = "topFormatAttributeChanges";

    private static final String KEY_EVENT_DATA_ATTRIBUTES = "attributes";

    private static final String KEY_ATTRIBUTES_DATA_LINK = "link";

    private static final String KEY_LINK_DATA_IS_ACTIVE = "isActive";
    private static final String KEY_LINK_DATA_IS_URL = "url";

    private String mUrl;

    public ReactAztecFormattingAttributesChange(int viewId) {
        super(viewId);
    }

    public void setLinkData(String url) {
        mUrl = url;
    }

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

        WritableMap attributesData = Arguments.createMap();
        attributesData.putMap(KEY_ATTRIBUTES_DATA_LINK, getLinkData());

        WritableMap eventData = Arguments.createMap();
        eventData.putMap(KEY_EVENT_DATA_ATTRIBUTES, attributesData);

        return eventData;
    }

    private WritableMap getLinkData() {
        WritableMap linkData = Arguments.createMap();
        if (!TextUtils.isEmpty(mUrl)) {
            linkData.putBoolean(KEY_LINK_DATA_IS_ACTIVE, true);
            linkData.putString(KEY_LINK_DATA_IS_URL, mUrl);
        }
        else {
            linkData.putBoolean(KEY_LINK_DATA_IS_ACTIVE, false);
        }
        return linkData;
    }
}
