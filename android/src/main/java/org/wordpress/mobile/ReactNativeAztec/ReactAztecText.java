package org.wordpress.mobile.ReactNativeAztec;

import android.content.Context;
import android.graphics.Rect;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.text.Spannable;
import android.view.Gravity;
import android.widget.TextView;
import android.view.inputmethod.BaseInputConnection;
import android.view.KeyEvent;

import com.facebook.infer.annotation.Assertions;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.events.EventDispatcher;
import com.facebook.react.views.textinput.ContentSizeWatcher;
import com.facebook.react.views.textinput.ReactTextChangedEvent;
import com.facebook.react.views.textinput.ReactTextInputLocalData;
import com.facebook.react.views.textinput.ScrollWatcher;

import org.wordpress.aztec.AztecText;
import org.wordpress.aztec.AlignmentRendering;
import org.wordpress.aztec.AztecTextFormat;
import org.wordpress.aztec.ITextFormat;
import org.wordpress.aztec.plugins.IAztecPlugin;
import org.wordpress.aztec.plugins.IToolbarButton;

import java.util.ArrayList;
import java.util.LinkedList;

public class ReactAztecText extends AztecText {

    private final InputMethodManager mInputMethodManager;
    // This flag is set to true when we set the text of the EditText explicitly. In that case, no
    // *TextChanged events should be triggered. This is less expensive than removing the text
    // listeners and adding them back again after the text change is completed.
    private boolean mIsSettingTextFromJS = false;
    // This component is controlled, so we want it to get focused only when JS ask it to do so.
    // Whenever android requests focus (which it does for random reasons), it will be ignored.
    private boolean mIsJSSettingFocus = false;
    private @Nullable ArrayList<TextWatcher> mListeners;
    private @Nullable TextWatcherDelegator mTextWatcherDelegator;
    private @Nullable ContentSizeWatcher mContentSizeWatcher;
    private @Nullable ScrollWatcher mScrollWatcher;

    // FIXME: Used in `incrementAndGetEventCounter` but never read. I guess we can get rid of it, but before this
    // check when it's used in EditText in RN. (maybe tests?)
    int mNativeEventCount = 0;

    String lastSentFormattingOptionsEventString = "";
    boolean shouldHandleOnEnter = false;
    boolean shouldHandleOnBackspace = false;
    boolean shouldHandleOnSelectionChange = false;
    boolean shouldHandleActiveFormatsChange = false;

    public ReactAztecText(ThemedReactContext reactContext) {
        super(reactContext);
        this.setAztecKeyListener(new ReactAztecText.OnAztecKeyListener() {
            @Override
            public boolean onEnterKey(Spannable text, boolean firedAfterTextChanged, int selStart, int selEnd) {
                return false;
            }
            @Override
            public boolean onBackspaceKey() {
                if (shouldHandleOnBackspace) {
                    return onBackspace();
                }
                return false;
            }
        });
        mInputMethodManager = (InputMethodManager)
                Assertions.assertNotNull(getContext().getSystemService(Context.INPUT_METHOD_SERVICE));
        this.setOnSelectionChangedListener(new OnSelectionChangedListener() {
            @Override
            public void onSelectionChanged(int selStart, int selEnd) {
                ReactAztecText.this.updateToolbarButtons(selStart, selEnd);
                ReactAztecText.this.propagateSelectionChanges(selStart, selEnd);
            }
        });
        this.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        this.setGravity(Gravity.TOP | Gravity.START);

        BetterLinkMovementMethod linkClick = BetterLinkMovementMethod.newInstance();

        linkClick.setOnLinkClickListener(new BetterLinkMovementMethod.OnLinkClickListener() {
            @Override
            public boolean onClick(TextView textView, String url) {
                hideSoftKeyboard();
                return false;
            }
        });

        this.setMovementMethod(linkClick);
    }

    @Override
    public void refreshText() {
        super.refreshText();
        onContentSizeChange();
    }

    void addPlugin(IAztecPlugin plugin) {
        super.getPlugins().add(plugin);
        if (plugin instanceof IToolbarButton && getToolbar() != null ) {
            getToolbar().addButton((IToolbarButton)plugin);
        }
    }

    // VisibleForTesting from {@link TextInputEventsTestCase}.
    public void requestFocusFromJS() {
        mIsJSSettingFocus = true;
        requestFocus();
        mIsJSSettingFocus = false;
    }

    void clearFocusFromJS() {
        clearFocus();
    }

    @Override
    public void clearFocus() {
        setFocusableInTouchMode(false);
        super.clearFocus();
        hideSoftKeyboard();
    }

    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {
        // Always return true if we are already focused. This is used by android in certain places,
        // such as text selection.
        if (isFocused()) {
            return true;
        }
        //TODO check why it's needed - doesn't seem to work fine with this in it, since each focus call
        // from the Android FW is skipped here.
        /*if (!mIsJSSettingFocus) {
            return false;
        }*/
        setFocusableInTouchMode(true);
        boolean focused = super.requestFocus(direction, previouslyFocusedRect);

        final int scrollAmount = this.getLayout().getLineTop(this.getLineCount()) - this.getHeight();
        if (scrollAmount > 0) {
            this.scrollTo(0, scrollAmount + 50);
        }

        super.setSelection(this.length());

        showSoftKeyboard();
        return focused;
    }

    public void sendSpaceAndBackspace() {
        BaseInputConnection inputConnection = new BaseInputConnection(this, true);
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_SPACE));
        inputConnection.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL));
    }

    public void scrollToBottom() {
        final int scrollAmount = this.getLayout().getLineTop(this.getLineCount()) - this.getHeight() + 50;

        // only scroll if the user is already at the bottom.  ignore otherwise
        if (scrollAmount > 0 && this.getSelectionStart() >= this.getText().toString().length() - 1) {
            this.scrollTo(0, scrollAmount);

            super.setSelection(this.length());
        }
    }

    private boolean showSoftKeyboard() {
        return mInputMethodManager.showSoftInput(this, 0);
    }

    private void hideSoftKeyboard() {
        mInputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    public void setScrollWatcher(ScrollWatcher scrollWatcher) {
        mScrollWatcher = scrollWatcher;
    }

    @Override
    protected void onScrollChanged(int horiz, int vert, int oldHoriz, int oldVert) {
        super.onScrollChanged(horiz, vert, oldHoriz, oldVert);

        if (mScrollWatcher != null) {
            mScrollWatcher.onScrollChanged(horiz, vert, oldHoriz, oldVert);
        }
    }

    public void setContentSizeWatcher(ContentSizeWatcher contentSizeWatcher) {
        mContentSizeWatcher = contentSizeWatcher;
    }

    private void onContentSizeChange() {
        if (mContentSizeWatcher != null) {
            new java.util.Timer().schedule(
                    new java.util.TimerTask() {
                        @Override
                        public void run() {
                            if (mContentSizeWatcher != null) {
                                mContentSizeWatcher.onLayout();

                            }
                        }
                    },
                    500
            );
        }
        setIntrinsicContentSize();
    }

    private void updateToolbarButtons(int selStart, int selEnd) {
        ArrayList<ITextFormat> appliedStyles = getAppliedStyles(selStart, selEnd);
        updateToolbarButtons(appliedStyles);
    }

    private void updateToolbarButtons(ArrayList<ITextFormat> appliedStyles) {
        // Read the applied styles and get the String list of formatting options
        LinkedList<String> formattingOptions = new LinkedList<>();
        for (ITextFormat currentStyle : appliedStyles) {
            if ((currentStyle == AztecTextFormat.FORMAT_STRONG || currentStyle == AztecTextFormat.FORMAT_BOLD)
                    && !formattingOptions.contains("bold")) {
                formattingOptions.add("bold");
            }
            if ((currentStyle == AztecTextFormat.FORMAT_ITALIC || currentStyle == AztecTextFormat.FORMAT_CITE)
                    && !formattingOptions.contains("italic")) {
                formattingOptions.add("italic");
            }
            if (currentStyle == AztecTextFormat.FORMAT_STRIKETHROUGH) {
                formattingOptions.add("strikethrough");
            }

            if (currentStyle == AztecTextFormat.FORMAT_UNDERLINE) {
                formattingOptions.add("underline");
            }
        }

        // Check if the same formatting event was already sent
        String newOptionsAsString = "";
        for (String currentFormatting: formattingOptions) {
            newOptionsAsString += currentFormatting;
        }
        if (newOptionsAsString.equals(lastSentFormattingOptionsEventString)) {
            // no need to send any event now
            return;
        }
        lastSentFormattingOptionsEventString = newOptionsAsString;

        if (shouldHandleActiveFormatsChange) {
            ReactContext reactContext = (ReactContext) getContext();
            EventDispatcher eventDispatcher = reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();
            eventDispatcher.dispatchEvent(
                    new ReactAztecFormattingChangeEvent(
                            getId(),
                            formattingOptions.toArray(new String[formattingOptions.size()])
                    )
            );
        }
    }

    private void propagateSelectionChanges(int selStart, int selEnd) {
        if (!shouldHandleOnSelectionChange) {
            return;
        }
        String content = toHtml(false);
        ReactContext reactContext = (ReactContext) getContext();
        EventDispatcher eventDispatcher = reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();
        eventDispatcher.dispatchEvent(
                new ReactAztecSelectionChangeEvent(getId(), content, selStart, selEnd)
        );
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        onContentSizeChange();
    }

    private void setIntrinsicContentSize() {
        ReactContext reactContext = (ReactContext) getContext();
        UIManagerModule uiManager = reactContext.getNativeModule(UIManagerModule.class);
        final ReactTextInputLocalData localData = new ReactTextInputLocalData(this);
        uiManager.setViewLocalData(getId(), localData);
    }

    //// Text changed events

    public int incrementAndGetEventCounter() {
        return ++mNativeEventCount;
    }

    @Override
    public void addTextChangedListener(TextWatcher watcher) {
        if (mListeners == null) {
            mListeners = new ArrayList<>();
            super.addTextChangedListener(getTextWatcherDelegator());
        }

        mListeners.add(watcher);
    }

    @Override
    public void removeTextChangedListener(TextWatcher watcher) {
        if (mListeners != null) {
            mListeners.remove(watcher);

            if (mListeners.isEmpty()) {
                mListeners = null;
                super.removeTextChangedListener(getTextWatcherDelegator());
            }
        }
    }

    private TextWatcherDelegator getTextWatcherDelegator() {
        if (mTextWatcherDelegator == null) {
            mTextWatcherDelegator = new TextWatcherDelegator();
        }
        return mTextWatcherDelegator;
    }

    public void setIsSettingTextFromJS(boolean mIsSettingTextFromJS) {
        this.mIsSettingTextFromJS = mIsSettingTextFromJS;
    }

    private boolean onEnter() {
        disableTextChangedListener();
        String content = toHtml(false);
        int cursorPositionStart = getSelectionStart();
        int cursorPositionEnd = getSelectionEnd();
        enableTextChangedListener();
        ReactContext reactContext = (ReactContext) getContext();
        EventDispatcher eventDispatcher = reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();
        eventDispatcher.dispatchEvent(
                new ReactAztecEnterEvent(getId(), content, cursorPositionStart, cursorPositionEnd)
        );
        return true;
    }

    private boolean onBackspace() {
        int cursorPositionStart = getSelectionStart();
        int cursorPositionEnd = getSelectionEnd();
        // Make sure to report backspace at the beginning only, with no selection.
        if (cursorPositionStart != 0 || cursorPositionEnd != 0) {
            return false;
        }

        disableTextChangedListener();
        String content = toHtml(false);
        enableTextChangedListener();
        ReactContext reactContext = (ReactContext) getContext();
        EventDispatcher eventDispatcher = reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();
        // TODO: isRTL? Should be passed here?
        eventDispatcher.dispatchEvent(
                new ReactAztecBackspaceEvent(getId(), content, cursorPositionStart, cursorPositionEnd)
        );
        return true;
    }

    public void applyFormat(String format) {
        ArrayList<ITextFormat> newFormats = new ArrayList<>();
        switch (format) {
            case ("bold"):
            case ("strong"):
                newFormats.add(AztecTextFormat.FORMAT_STRONG);
                newFormats.add(AztecTextFormat.FORMAT_BOLD);
            break;
            case ("italic"):
                newFormats.add(AztecTextFormat.FORMAT_ITALIC);
                newFormats.add(AztecTextFormat.FORMAT_CITE);
            break;
            case ("strikethrough"):
                newFormats.add(AztecTextFormat.FORMAT_STRIKETHROUGH);
            break;
            case ("underline"):
                newFormats.add(AztecTextFormat.FORMAT_UNDERLINE);
            break;
        }

        if (newFormats.size() == 0) {
            return;
        }

        if (!isTextSelected()) {
            final ArrayList<ITextFormat> newStylesList = getNewStylesList(newFormats);
            setSelectedStyles(newStylesList);
            // Update the toolbar state
            updateToolbarButtons(newStylesList);
        } else {
            toggleFormatting(newFormats.get(0));
            // Update the toolbar state
            updateToolbarButtons(getSelectionStart(), getSelectionEnd());
        }

        // emit onChange because the underlying HTML has changed applying the style
        ReactContext reactContext = (ReactContext) getContext();
        EventDispatcher eventDispatcher = reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();
        eventDispatcher.dispatchEvent(
                new ReactTextChangedEvent(
                        getId(),
                        toHtml(false),
                        incrementAndGetEventCounter())
        );
    }

    // Removes all formats in the list but if none found, applies the first one
    private ArrayList<ITextFormat> getNewStylesList(ArrayList<ITextFormat> newFormats) {
        ArrayList<ITextFormat> textFormats = new ArrayList<>();
        textFormats.addAll(getSelectedStyles());
        boolean wasRemoved = false;
        for (ITextFormat newFormat : newFormats) {
            if (textFormats.contains(newFormat)) {
                wasRemoved = true;
                textFormats.remove(newFormat);
            }
        }

        if (!wasRemoved) {
            textFormats.add(newFormats.get(0));
        }

        return textFormats;
    }

    /**
     * This class will redirect *TextChanged calls to the listeners only in the case where the text
     * is changed by the user, and not explicitly set by JS.
     */
    private class TextWatcherDelegator implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (!mIsSettingTextFromJS && mListeners != null) {
                for (TextWatcher listener : mListeners) {
                    listener.beforeTextChanged(s, start, count, after);
                }
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!mIsSettingTextFromJS && mListeners != null) {
                for (TextWatcher listener : mListeners) {
                    listener.onTextChanged(s, start, before, count);
                }
            }

            onContentSizeChange();
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (!mIsSettingTextFromJS && mListeners != null) {
                for (TextWatcher listener : mListeners) {
                    listener.afterTextChanged(s);
                }
            }
        }
    }
}

