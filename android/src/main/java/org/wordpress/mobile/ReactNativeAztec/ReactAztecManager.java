package org.wordpress.mobile.ReactNativeAztec;


import android.graphics.Color;
import android.graphics.Typeface;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.InputType;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.ViewDefaults;
import com.facebook.react.uimanager.ViewProps;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.EventDispatcher;
import com.facebook.react.views.scroll.ScrollEvent;
import com.facebook.react.views.scroll.ScrollEventType;
import com.facebook.react.views.text.DefaultStyleValuesUtil;
import com.facebook.react.views.text.ReactFontManager;
import com.facebook.react.views.textinput.ReactContentSizeChangedEvent;
import com.facebook.react.views.textinput.ReactTextChangedEvent;
import com.facebook.react.views.textinput.ReactTextInputEvent;
import com.facebook.react.views.textinput.ReactTextInputManager;
import com.facebook.react.views.textinput.ScrollWatcher;

import org.wordpress.aztec.glideloader.GlideImageLoader;
import org.wordpress.aztec.glideloader.GlideVideoThumbnailLoader;
import org.wordpress.aztec.plugins.CssUnderlinePlugin;
import org.wordpress.aztec.plugins.shortcodes.AudioShortcodePlugin;
import org.wordpress.aztec.plugins.shortcodes.CaptionShortcodePlugin;
import org.wordpress.aztec.plugins.shortcodes.VideoShortcodePlugin;
import org.wordpress.aztec.plugins.wpcomments.HiddenGutenbergPlugin;
import org.wordpress.aztec.plugins.wpcomments.WordPressCommentsPlugin;
import org.wordpress.aztec.plugins.wpcomments.toolbar.MoreToolbarButton;

import java.util.Map;

public class ReactAztecManager extends SimpleViewManager<ReactAztecText> {

    public static final String REACT_CLASS = "RCTAztecView";

    private static final int FOCUS_TEXT_INPUT = 1;
    private static final int BLUR_TEXT_INPUT = 2;
    private static final int SET_HTML = 3;
    private static final int SCROLL_TO_BOTTOM = 4;
    private static final int SEND_SPACE_AND_BACKSPACE = 5;
    private static final int COMMAND_NOTIFY_APPLY_FORMAT = 100;
    private static final int UNSET = -1;

    // we define the same codes in ReactAztecText as they have for ReactNative's TextInput, so
    // it's easier to handle focus between Aztec and TextInput instances on the same screen.
    // see https://github.com/wordpress-mobile/react-native-aztec/pull/79
    private int mFocusTextInputCommandCode = FOCUS_TEXT_INPUT; // pre-init
    private int mBlurTextInputCommandCode = BLUR_TEXT_INPUT; // pre-init
    private int mSetHTMLCommandCode = SET_HTML;
    private int mScrollToBottomCode = SCROLL_TO_BOTTOM;
    private int mSendSpaceAndBackspaceCode = SEND_SPACE_AND_BACKSPACE;

    private static final String TAG = "ReactAztecText";

    public ReactAztecManager() {
        initializeFocusAndBlurCommandCodes();
    }

    private void initializeFocusAndBlurCommandCodes() {
        // For this, we'd like to keep track of potential command code changes in the future,
        // so we obtain an instance of ReactTextInputManager and call getCommandsMap in our
        // constructor to use the very same codes as TextInput does.
        ReactTextInputManager reactTextInputManager = new ReactTextInputManager();
        Map<String, Integer> map = reactTextInputManager.getCommandsMap();
        mFocusTextInputCommandCode = map.get("focusTextInput");
        mBlurTextInputCommandCode = map.get("blurTextInput");
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Override
    protected ReactAztecText createViewInstance(ThemedReactContext reactContext) {
        ReactAztecText aztecText = new ReactAztecText(reactContext);
        aztecText.setFocusableInTouchMode(true);
        aztecText.setFocusable(true);
        aztecText.setCalypsoMode(false);
        aztecText.setLinksClickable(true);
        aztecText.setAutoLinkMask(Linkify.WEB_URLS);
        aztecText.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        return aztecText;
    }

    @Nullable
    @Override
    public Map<String, Object> getExportedCustomBubblingEventTypeConstants() {
        return MapBuilder.<String, Object>builder()
             /*   .put(
                        "topSubmitEditing",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of(
                                        "bubbled", "onSubmitEditing", "captured", "onSubmitEditingCapture")))*/
                .put(
                        "topChange",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onChange")))
                .put(
                        "topFormatsChanges",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onActiveFormatsChange")))
                .put(
                        "topEndEditing",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onEndEditing", "captured", "onEndEditingCapture")))
                .put(
                        "topTextInput",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onTextInput", "captured", "onTextInputCapture")))
                .put(
                        "topTextInputEnter",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onEnter")))
                .put(
                        "topTextInputBackspace",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onBackspace")))
                .put(
                        "topFocus",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onFocus", "captured", "onFocusCapture")))
                .put(
                        "topBlur",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onBlur", "captured", "onBlurCapture")))
              /*  .put(
                        "topKeyPress",
                        MapBuilder.of(
                                "phasedRegistrationNames",
                                MapBuilder.of("bubbled", "onKeyPress", "captured", "onKeyPressCapture")))*/
                .build();
    }

    @Nullable
    @Override
    public Map getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.of(
                "topSelectionChange",
                MapBuilder.of("registrationName", "onSelectionChange")
                );
    }

    @ReactProp(name = "text")
    public void setText(ReactAztecText view, ReadableMap inputMap) {
        if (!inputMap.hasKey("eventCount")) {
            setTextfromJS(view, inputMap.getString("text"));
        } else {
            // Don't think there is necessity of this branch, but justin case we want to
            // force a 2nd setText from JS side to Native, just set a high eventCount
            int eventCount = inputMap.getInt("eventCount");
            if (view.mNativeEventCount < eventCount) {
                setTextfromJS(view, inputMap.getString("text"));
            }
        }
    }

    private void setTextfromJS(ReactAztecText view, String text) {
        view.setIsSettingTextFromJS(true);
        view.fromHtml(text, true);
        view.setIsSettingTextFromJS(false);
    }


    /*
     The code below was taken from the class ReactTextInputManager
     */
    @ReactProp(name = ViewProps.FONT_SIZE, defaultFloat = ViewDefaults.FONT_SIZE_SP)
    public void setFontSize(ReactAztecText view, float fontSize) {
        view.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                (int) Math.ceil(PixelUtil.toPixelFromSP(fontSize)));
    }

    @ReactProp(name = ViewProps.FONT_FAMILY)
    public void setFontFamily(ReactAztecText view, String fontFamily) {
        int style = Typeface.NORMAL;
        if (view.getTypeface() != null) {
            style = view.getTypeface().getStyle();
        }
        Typeface newTypeface = ReactFontManager.getInstance().getTypeface(
                fontFamily,
                style,
                view.getContext().getAssets());
        view.setTypeface(newTypeface);
    }

    /**
     /* This code was taken from the method setFontWeight of the class ReactTextShadowNode
     /* TODO: Factor into a common place they can both use
     */
    @ReactProp(name = ViewProps.FONT_WEIGHT)
    public void setFontWeight(ReactAztecText view, @Nullable String fontWeightString) {
        int fontWeightNumeric = fontWeightString != null ?
                parseNumericFontWeight(fontWeightString) : -1;
        int fontWeight = UNSET;
        if (fontWeightNumeric >= 500 || "bold".equals(fontWeightString)) {
            fontWeight = Typeface.BOLD;
        } else if ("normal".equals(fontWeightString) ||
                (fontWeightNumeric != -1 && fontWeightNumeric < 500)) {
            fontWeight = Typeface.NORMAL;
        }
        Typeface currentTypeface = view.getTypeface();
        if (currentTypeface == null) {
            currentTypeface = Typeface.DEFAULT;
        }
        if (fontWeight != currentTypeface.getStyle()) {
            view.setTypeface(currentTypeface, fontWeight);
        }
    }

    /**
     /* This code was taken from the method setFontStyle of the class ReactTextShadowNode
     /* TODO: Factor into a common place they can both use
     */
    @ReactProp(name = ViewProps.FONT_STYLE)
    public void setFontStyle(ReactAztecText view, @Nullable String fontStyleString) {
        int fontStyle = UNSET;
        if ("italic".equals(fontStyleString)) {
            fontStyle = Typeface.ITALIC;
        } else if ("normal".equals(fontStyleString)) {
            fontStyle = Typeface.NORMAL;
        }

        Typeface currentTypeface = view.getTypeface();
        if (currentTypeface == null) {
            currentTypeface = Typeface.DEFAULT;
        }
        if (fontStyle != currentTypeface.getStyle()) {
            view.setTypeface(currentTypeface, fontStyle);
        }
    }

    /**
     * This code was taken from the method parseNumericFontWeight of the class ReactTextShadowNode
     * TODO: Factor into a common place they can both use
     *
     * Return -1 if the input string is not a valid numeric fontWeight (100, 200, ..., 900), otherwise
     * return the weight.
     */
    private static int parseNumericFontWeight(String fontWeightString) {
        // This should be much faster than using regex to verify input and Integer.parseInt
        return fontWeightString.length() == 3 && fontWeightString.endsWith("00")
                && fontWeightString.charAt(0) <= '9' && fontWeightString.charAt(0) >= '1' ?
                100 * (fontWeightString.charAt(0) - '0') : -1;
    }

    /* End of the code taken from ReactTextInputManager */

    @ReactProp(name = "color", customType = "Color")
    public void setColor(ReactAztecText view, @Nullable Integer color) {
        int newColor = Color.BLACK;
        if (color != null) {
            newColor = color;
        }
        view.setTextColor(newColor);
    }

    @ReactProp(name = "placeholder")
    public void setPlaceholder(ReactAztecText view, @Nullable String placeholder) {
        view.setHint(placeholder);
    }

    @ReactProp(name = "placeholderTextColor", customType = "Color")
    public void setPlaceholderTextColor(ReactAztecText view, @Nullable Integer color) {
        if (color == null) {
            view.setHintTextColor(DefaultStyleValuesUtil.getDefaultTextColorHint(view.getContext()));
        } else {
            view.setHintTextColor(color);
        }
    }

    @ReactProp(name = "autoCorrect")
    public void setAutoCorrect(ReactAztecText view, Boolean autoCorrect) {
        if (autoCorrect) {
            view.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        } else {
            view.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        }
    }

    @ReactProp(name = "maxImagesWidth")
    public void setMaxImagesWidth(ReactAztecText view, int maxWidth) {
        view.setMaxImagesWidth(maxWidth);
    }

    @ReactProp(name = "minImagesWidth")
    public void setMinImagesWidth(ReactAztecText view, int minWidth) {
        view.setMinImagesWidth(minWidth);
    }

    /*
     * This property/method is used to disable the Gutenberg compatibility mode on AztecRN.
     *
     * Aztec comes along with some nice plugins that are able to show preview of Pictures/Videos/shortcodes,
     * and WP specific features, in the visual editor.
     *
     * We don't need those improvements in Gutenberg mobile, so this RN wrapper around Aztec
     * that's only used in GB-mobile at the moment, does have them OFF by default.
     *
     * An external 3rd party RN-app can use AztecRN wrapper and set the `disableGutenbergMode` to false to have a fully
     * working visual editor. See the demo app, where `disableGutenbergMode` is already OFF.
     */
    @ReactProp(name = "disableGutenbergMode", defaultBoolean = false)
    public void disableGBMode(final ReactAztecText view, boolean disable) {
        if (disable) {
            view.addPlugin(new WordPressCommentsPlugin(view));
            view.addPlugin(new MoreToolbarButton(view));
            view.addPlugin(new CaptionShortcodePlugin(view));
            view.addPlugin(new VideoShortcodePlugin());
            view.addPlugin(new AudioShortcodePlugin());
            view.addPlugin(new HiddenGutenbergPlugin(view));
            view.addPlugin(new CssUnderlinePlugin());
            view.setImageGetter(new GlideImageLoader(view.getContext()));
            view.setVideoThumbnailGetter(new GlideVideoThumbnailLoader(view.getContext()));
            // we need to restart the editor now
            String content = view.toHtml(false);
            view.fromHtml(content, false);
        }
    }

    /*
     * This property/method is used to tell the native AztecText to grab the focus when isSelected is true
     *
     */
    @ReactProp(name = "isSelected", defaultBoolean = false)
    public void isSelected(final ReactAztecText view, boolean selected) {
        if (selected) {
            view.requestFocus();
        }
    }

    @ReactProp(name = "onContentSizeChange", defaultBoolean = false)
    public void setOnContentSizeChange(final ReactAztecText view, boolean onContentSizeChange) {
        if (onContentSizeChange) {
            view.setContentSizeWatcher(new AztecContentSizeWatcher(view));
        } else {
            view.setContentSizeWatcher(null);
        }
    }

    @ReactProp(name = "onActiveFormatsChange", defaultBoolean = false)
    public void setOnActiveFormatsChange(final ReactAztecText view, boolean onActiveFormatsChange) {
        view.shouldHandleActiveFormatsChange = onActiveFormatsChange;
    }

    @ReactProp(name = "onSelectionChange", defaultBoolean = false)
    public void setOnSelectionChange(final ReactAztecText view, boolean onSelectionChange) {
        view.shouldHandleOnSelectionChange = onSelectionChange;
    }

    @ReactProp(name = "onScroll", defaultBoolean = false)
    public void setOnScroll(final ReactAztecText view, boolean onScroll) {
        if (onScroll) {
            view.setScrollWatcher(new AztecScrollWatcher(view));
        } else {
            view.setScrollWatcher(null);
        }
    }

    @ReactProp(name = "onEnter", defaultBoolean = false)
    public void setOnEnterHandling(final ReactAztecText view, boolean onEnterHandling) {
        view.shouldHandleOnEnter = onEnterHandling;
    }

    @ReactProp(name = "onBackspace", defaultBoolean = false)
    public void setOnBackspaceHandling(final ReactAztecText view, boolean onBackspaceHandling) {
        view.shouldHandleOnBackspace = onBackspaceHandling;
    }

    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.<String, Integer>builder()
                .put("applyFormat", COMMAND_NOTIFY_APPLY_FORMAT)
                .put("focusTextInput", mFocusTextInputCommandCode)
                .put("blurTextInput", mBlurTextInputCommandCode)
                .put("setHTML", mSetHTMLCommandCode)
                .put("scrollToBottom", mScrollToBottomCode)
                .put("sendSpaceAndBackspace", mSendSpaceAndBackspaceCode)
                .build();
    }

    @Override
    public void receiveCommand(final ReactAztecText parent, int commandType, @Nullable ReadableArray args) {
        Assertions.assertNotNull(parent);
        if (commandType == COMMAND_NOTIFY_APPLY_FORMAT) {
            final String format = args.getString(0);
            Log.d(TAG, String.format("Apply format: %s", format));
            parent.applyFormat(format);
            return;
        } else if (commandType == mFocusTextInputCommandCode) {
            parent.requestFocusFromJS();
            return;
        } else if (commandType == mBlurTextInputCommandCode) {
            parent.clearFocusFromJS();
            return;
        } else if (commandType == mSetHTMLCommandCode) {
            final String html = args.getString(0);
            setTextfromJS(parent, html);
            return;
        } else if (commandType == mScrollToBottomCode) {
            Log.d("SCROLLING", "1");
            parent.scrollToBottom();
        } else if (commandType == mSendSpaceAndBackspaceCode) {
            parent.sendSpaceAndBackspace();
        }
        super.receiveCommand(parent, commandType, args);
    }

    @Override
    protected void addEventEmitters(final ThemedReactContext reactContext, final ReactAztecText aztecText) {
        aztecText.addTextChangedListener(new AztecTextWatcher(reactContext, aztecText));
        aztecText.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    public void onFocusChange(View v, boolean hasFocus) {
                        EventDispatcher eventDispatcher = reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();
                        final ReactAztecText editText = (ReactAztecText)v;
                        if (hasFocus) {
                            eventDispatcher.dispatchEvent(
                                    new ReactAztecFocusEvent(
                                            editText.getId()));
                        } else {
                            eventDispatcher.dispatchEvent(
                                    new ReactAztecBlurEvent(
                                            editText.getId()));

                            eventDispatcher.dispatchEvent(
                                    new ReactAztecEndEditingEvent(
                                            editText.getId(),
                                            editText.toHtml(false)));
                        }
                    }
                });

        // Don't think we need to add setOnEditorActionListener here (intercept Enter for example), but
        // in case check ReactTextInputManager
    }

    private class AztecTextWatcher implements TextWatcher {

        private EventDispatcher mEventDispatcher;
        private ReactAztecText mEditText;
        private String mPreviousText;

        public AztecTextWatcher(final ReactContext reactContext, final ReactAztecText aztecText) {
            mEventDispatcher = reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();
            mEditText = aztecText;
            mPreviousText = null;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Incoming charSequence gets mutated before onTextChanged() is invoked
            mPreviousText = s.toString();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // Rearranging the text (i.e. changing between singleline and multiline attributes) can
            // also trigger onTextChanged, call the event in JS only when the text actually changed
            if (count == 0 && before == 0) {
                return;
            }

            Assertions.assertNotNull(mPreviousText);
            String newText = s.toString().substring(start, start + count);
            String oldText = mPreviousText.substring(start, start + before);
            // Don't send same text changes
            if (count == before && newText.equals(oldText)) {
                return;
            }

            mEventDispatcher.dispatchEvent(
                    new ReactTextInputEvent(
                            mEditText.getId(),
                            newText,
                            oldText,
                            start,
                            start + before));
        }

        @Override
        public void afterTextChanged(Editable s) {
            mEventDispatcher.dispatchEvent(
                    new ReactTextChangedEvent(
                            mEditText.getId(),
                            mEditText.toHtml(false),
                            mEditText.incrementAndGetEventCounter()));
        }
    }

    private class AztecContentSizeWatcher implements com.facebook.react.views.textinput.ContentSizeWatcher {
        private ReactAztecText mReactAztecText;
        private EventDispatcher mEventDispatcher;
        private int mPreviousContentWidth = 0;
        private int mPreviousContentHeight = 0;

        public AztecContentSizeWatcher(ReactAztecText view) {
            mReactAztecText = view;
            ReactContext reactContext = (ReactContext) mReactAztecText.getContext();
            mEventDispatcher = reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();
        }

        @Override
        public void onLayout() {
            int contentWidth = mReactAztecText.getWidth();
            int contentHeight = mReactAztecText.getHeight();

            // Use instead size of text content within EditText when available
            if (mReactAztecText.getLayout() != null) {
                contentWidth = mReactAztecText.getCompoundPaddingLeft() + mReactAztecText.getLayout().getWidth() +
                        mReactAztecText.getCompoundPaddingRight();
                contentHeight = mReactAztecText.getCompoundPaddingTop() + mReactAztecText.getLayout().getHeight() +
                        mReactAztecText.getCompoundPaddingBottom();
            }

            if (contentWidth != mPreviousContentWidth || contentHeight != mPreviousContentHeight) {
                mPreviousContentHeight = contentHeight;
                mPreviousContentWidth = contentWidth;

                // FIXME: Note the 2 hacks here
                mEventDispatcher.dispatchEvent(
                        new ReactContentSizeChangedEvent(
                                mReactAztecText.getId(),
                                PixelUtil.toDIPFromPixel(contentWidth),
                                PixelUtil.toDIPFromPixel(contentHeight)));
            }
        }
    }

    private class AztecScrollWatcher implements ScrollWatcher {

        private ReactAztecText mReactAztecText;
        private EventDispatcher mEventDispatcher;
        private int mPreviousHoriz;
        private int mPreviousVert;

        public AztecScrollWatcher(ReactAztecText editText) {
            mReactAztecText = editText;
            ReactContext reactContext = (ReactContext) editText.getContext();
            mEventDispatcher = reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();
        }

        @Override
        public void onScrollChanged(int horiz, int vert, int oldHoriz, int oldVert) {
            if (mPreviousHoriz != horiz || mPreviousVert != vert) {
                ScrollEvent event = ScrollEvent.obtain(
                        mReactAztecText.getId(),
                        ScrollEventType.SCROLL,
                        horiz,
                        vert,
                        0f, // can't get x velocity
                        0f, // can't get y velocity
                        0, // can't get content width
                        0, // can't get content height
                        mReactAztecText.getWidth(),
                        mReactAztecText.getHeight());

                mEventDispatcher.dispatchEvent(event);

                mPreviousHoriz = horiz;
                mPreviousVert = vert;
            }
        }
    }
}

