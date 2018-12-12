import PropTypes from 'prop-types';
import React from 'react';
import ReactNative, {requireNativeComponent, ViewPropTypes, UIManager, ColorPropType, TouchableWithoutFeedback} from 'react-native';
import TextInputState from 'react-native/lib/TextInputState';

class AztecView extends React.Component {
  
  static propTypes = {
    isSelected: PropTypes.bool,
    disableGutenbergMode: PropTypes.bool,
    text: PropTypes.object,
    placeholder: PropTypes.string,
    placeholderTextColor: ColorPropType,
    color: ColorPropType,
    maxImagesWidth: PropTypes.number,
    minImagesWidth: PropTypes.number,
    onChange: PropTypes.func,
    onFocus: PropTypes.func,
    onBlur: PropTypes.func,
    onContentSizeChange: PropTypes.func,
    onEnter: PropTypes.func,
    onBackspace: PropTypes.func,
    onDelete: PropTypes.func,
    onScroll: PropTypes.func,
    onActiveFormatsChange: PropTypes.func,
    onSelectionChange: PropTypes.func,
    onHTMLContentWithCursor: PropTypes.func,
    ...ViewPropTypes, // include the default view properties
  }

  applyFormat(format) {   
    UIManager.dispatchViewManagerCommand(
                                          ReactNative.findNodeHandle(this),
                                          UIManager.RCTAztecView.Commands.applyFormat,
                                          [format],
                                        );    
  }

  requestHTMLWithCursor() {
    UIManager.dispatchViewManagerCommand(
                                          ReactNative.findNodeHandle(this),
                                          UIManager.RCTAztecView.Commands.returnHTMLWithCursor,
                                          [],
                                        );    
  }

  _onActiveFormatsChange = (event) => {
    if (!this.props.onActiveFormatsChange) {
      return;
    }
    const formats = event.nativeEvent.formats;
    const { onActiveFormatsChange } = this.props;
    onActiveFormatsChange(formats);
  }

  _onContentSizeChange = (event) => {
    if (!this.props.onContentSizeChange) {
      return;
    }
    const size = event.nativeEvent.contentSize;
    const { onContentSizeChange } = this.props;
    onContentSizeChange(size);
  }

  _onEnter = (event) => {
    if (!this.props.onEnter) {
      return;
    }

    const { onEnter } = this.props;
    onEnter(event);
  }

  _onBackspace = (event) => {
    if (!this.props.onBackspace) {
      return;
    }

    const { onBackspace } = this.props;
    onBackspace(event);
  }

  _onDelete = (event) => {
    if (!this.props.onDelete) {
      return;
    }

    const { onDelete } = this.props;
    onDelete(event);
  }

  _onHTMLContentWithCursor = (event) => {
    if (!this.props.onHTMLContentWithCursor) {
      return;
    }
    
    const text = event.nativeEvent.text;
    const selectionStart = event.nativeEvent.selectionStart;
    const selectionEnd = event.nativeEvent.selectionEnd;
    const { onHTMLContentWithCursor } = this.props;
    onHTMLContentWithCursor(text, selectionStart, selectionEnd);
  }

  _onFocus = (event) => {
    if (!this.props.onFocus) {
      return;
    }

    const { onFocus } = this.props;
    onFocus(event);
  }
  
  _onBlur = (event) => {
    TextInputState.blurTextInput(ReactNative.findNodeHandle(this));

    if (!this.props.onBlur) {
      return;
    }

    const { onBlur } = this.props;
    onBlur(event);
  }

  _onSelectionChange = (event) => {
    if (!this.props.onSelectionChange) {
      return;
    }
    const { selectionStart, selectionEnd, text } = event.nativeEvent;
    const { onSelectionChange } = this.props;
    onSelectionChange(selectionStart, selectionEnd, text);
  }

  blur = () => {
    TextInputState.blurTextInput(ReactNative.findNodeHandle(this));
  }

  focus = () => {
    TextInputState.focusTextInput(ReactNative.findNodeHandle(this));
  }

  _onPress = (event) => {
    this.focus(event); // Call to move the focus in RN way (TextInputState)
    this._onFocus(event); // Check if there are listeners set on the focus event
  }

  render() {
    const { onActiveFormatsChange, ...otherProps } = this.props    
    return (
      <TouchableWithoutFeedback onPress={ this._onPress }>
        <RCTAztecView {...otherProps}
          onActiveFormatsChange={ this._onActiveFormatsChange }
          onContentSizeChange = { this._onContentSizeChange }
          onHTMLContentWithCursor = { this._onHTMLContentWithCursor }
          onSelectionChange = { this._onSelectionChange }
          onEnter = { this._onEnter }
          // IMPORTANT: the onFocus events are thrown away as these are handled by onPress() in the upper level.
          // It's necessary to do this otherwise onFocus may be set by `{...otherProps}` and thus the onPress + onFocus
          // combination generate an infinite loop as described in https://github.com/wordpress-mobile/gutenberg-mobile/issues/302
          onFocus = { () => {} } 
          onBlur = { this._onBlur }
          onBackspace = { this._onBackspace }
          onDelete = { this._onDelete }
        />
      </TouchableWithoutFeedback>
    );
  }
}

const RCTAztecView = requireNativeComponent('RCTAztecView', AztecView);

export default AztecView