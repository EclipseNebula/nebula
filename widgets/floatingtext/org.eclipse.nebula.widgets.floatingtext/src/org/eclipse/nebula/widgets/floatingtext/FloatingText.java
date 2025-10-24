/******************************************************************************
 * Copyright (c) 2018 Remain BV (Remain Software)
 *
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Wim Jongman (wim.jongman@remainsoftware.com) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.nebula.widgets.floatingtext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * Instances of this class are selectable user interface objects that allow the
 * user to enter and modify text with a label floating above the input area.
 * FloatingText controls can be either single or multi-line. In contrast to a
 * Text widget, when a floating text control is created with a border, a custom
 * drawn inset will be painted INSTEAD OF the platform specific inset.
 *
 * <p>
 * Style SEPARATOR leaves a gap between the label and the text of one pixel in
 * the color set by {@link #setBackground(Color)}. To add more space use the
 * {@link #setSeparatorSpace(int)} method.
 *
 * <dl>
 * <dt><b>Styles are inherited from Text and subclasses:</b></dt>
 * <dd>CENTER, ICON_CANCEL, ICON_SEARCH, LEFT, MULTI, PASSWORD, SEARCH, SINGLE,
 * RIGHT, READ_ONLY, WRAP, BORDER, H_SCROLL, V_SCROLL, LEFT_TO_RIGT,
 * RIGHT_TO_LEFT, FLIP_TEXT_DIRECTION, SEPARATOR</dd>
 * <dt><b>Events:</b></dt>
 * <dd>DefaultSelection, Modify, Verify, OrientationChange</dd>
 * </dl>
 * <p>
 * Note: Only one of the styles MULTI and SINGLE may be specified, and only one
 * of the styles LEFT, CENTER, and RIGHT may be specified.
 * </p>
 * <p>
 * Note: The styles ICON_CANCEL and ICON_SEARCH are hints used in combination
 * with SEARCH. When the platform supports the hint, the text control shows
 * these icons. When an icon is selected, a default selection event is sent with
 * the detail field set to one of ICON_CANCEL or ICON_SEARCH. Normally,
 * application code does not need to check the detail. In the case of
 * ICON_CANCEL, the text is cleared before the default selection event is sent
 * causing the application to search for an empty string.
 * </p>
 */
public class FloatingText extends Composite {
	private Text fText;
	private Label fLabel;
	private int fStyle;
	private int fLabelToTextRatio = 90;
	private Font fLabelFont;

	/**
	 * Constructs a new instance of this class given its parent and a style value
	 * describing its behavior and appearance.
	 * <p>
	 * The style value is either one of the style constants defined in class
	 * <code>SWT</code> which is applicable to instances of this class, or must be
	 * built by <em>bitwise OR</em>'ing together (that is, using the
	 * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
	 * constants. The class description lists the style constants that are
	 * applicable to the class. Style bits are NOT inherited from superclasses.
	 * </p>
	 *
	 * @param parent a composite control which will be the parent of the new
	 *               instance (cannot be null)
	 * @param style  the style of control to construct
	 *
	 * @exception IllegalArgumentException
	 *                                     <ul>
	 *                                     <li>ERROR_NULL_ARGUMENT - if the parent
	 *                                     is null</li>
	 *                                     </ul>
	 * @exception SWTException
	 *                                     <ul>
	 *                                     <li>ERROR_THREAD_INVALID_ACCESS - if not
	 *                                     called from the thread that created the
	 *                                     parent</li>
	 *                                     <li>ERROR_INVALID_SUBCLASS - if this
	 *                                     class is not an allowed subclass</li>
	 *                                     </ul>
	 *
	 * @see SWT#SEPARATOR
	 * @see SWT#SINGLE
	 * @see SWT#MULTI
	 * @see SWT#READ_ONLY
	 * @see SWT#WRAP
	 * @see SWT#LEFT
	 * @see SWT#RIGHT
	 * @see SWT#CENTER
	 * @see SWT#PASSWORD
	 * @see SWT#SEARCH
	 * @see SWT#BORDER
	 * @see SWT#H_SCROLL
	 * @see SWT#V_SCROLL
	 * @see SWT#LEFT_TO_RIGHT
	 * @see SWT#RIGHT_TO_LEFT
	 * @see SWT#FLIP_TEXT_DIRECTION
	 * @see Widget#checkSubclass
	 * @see Widget#getStyle
	 */
	public FloatingText(final Composite pParent, final int pStyle) {
		super(pParent, SWT.DOUBLE_BUFFERED | (pStyle & SWT.BORDER));
		fStyle = pStyle;
		setLayout(createLayout(pStyle));
		fLabel = createLabel(pStyle);
		fLabel.addDisposeListener(e -> {
			if (fLabelFont != null)
				fLabelFont.dispose();
		});
		fText = new Text(this, removeStyles(pStyle, SWT.BORDER, SWT.SEPARATOR));
		fText.setLayoutData(getTextLayoutData());
		fLabel.setBackground(fText.getBackground());
		fLabel.setForeground(fText.getForeground());
		fLabel.setLayoutData(getLabelLayoutData());
		fText.addListener(SWT.FocusIn, e -> setLabelText(true));
		fText.addListener(SWT.FocusOut, e -> setLabelText(false));
		fText.addListener(SWT.Modify, e -> {
			if (!fText.getText().isEmpty() && fLabel.getText().isEmpty()) {
				fLabel.setText(fText.getMessage());
			}
			if (fText.getText().isEmpty() && !fLabel.getText().isEmpty()
					&& !(getDisplay().getFocusControl() == fText)) {
				fLabel.setText("");
			}
		});
	}

	@Override
	public Point computeSize(int pWidthHint, int pHeightHint, boolean pChanged) {
		Point textSize = fText.computeSize(pWidthHint, pHeightHint, pChanged);
		Point labelSize = fLabel.computeSize(pWidthHint,
				(pHeightHint == SWT.DEFAULT ? SWT.DEFAULT : (textSize.y * fLabelToTextRatio) / 100), pChanged);
		Point result = new Point(textSize.x + labelSize.x, textSize.y + labelSize.y);
		if ((fStyle & SWT.BORDER) == SWT.BORDER) {
			result.x += 2;
			result.y += 6;
		}
		result.y += getLayout().verticalSpacing;
		return result;
	}

	private Label createLabel(final int pStyle) {
		return new Label(this, SWT.NONE | ((pStyle & SWT.LEFT_TO_RIGHT) > 0 ? SWT.LEFT : SWT.NONE)
				| ((pStyle & SWT.RIGHT_TO_LEFT) > 0 ? SWT.RIGHT : SWT.NONE)) {

			@Override
			protected void checkSubclass() {
			}

			@Override
			public Point computeSize(int pWHint, int pHHint, boolean pChanged) {
				Point result = super.computeSize(pWHint, pHHint, pChanged);
				result.y = ((GridData) fLabel.getLayoutData()).heightHint;
				result.y = result.y;
				return result;
			}
		};
	}

	private GridLayout createLayout(final int pStyle) {
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		gridLayout.verticalSpacing = 0;
		if ((pStyle & SWT.SEPARATOR) == SWT.SEPARATOR) {
			gridLayout.verticalSpacing = 1;
		}
		return gridLayout;
	}

	private void doSetLabelText() {
		String message = getMessage();
		if (message.isEmpty()) {
			return;
		}
		if (fLabel.getSize().y <= 0) {
			getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					doSetLabelText();
				}
			});
			return;
		}

		if (fLabelFont != null) {
			fLabelFont.dispose();
		}
		fLabelFont = findFittingFont(fLabel);

		fLabel.setFont(fLabelFont);
		fLabel.setText(message);
	}

	public Font findFittingFont(Label label) {
		int fontSize = (label.getSize().y * 75) / 100;
		Font font = getFont(label, fontSize);
		GC gc = new GC(label);
		try {
			gc.setFont(font);
			while (fontSize > 2) {
				int textHeight = gc.textExtent("PQR").y;
				if (textHeight <= label.getBounds().height) {
					return font; // Found a fitting font
				}
				// Cleanup and decrease font size
				font.dispose();
				fontSize--;
				font = getFont(label, fontSize);
				gc.setFont(font);
			}
			return font;
		} finally {
			gc.dispose();
		}
	}

	private Font getFont(Label label, int fontSize) {
		FontData[] fontData = label.getFont().getFontData();
		FontData[] newFontData = new FontData[fontData.length];
		for (int i = 0; i < fontData.length; i++) {
			newFontData[i] = new FontData(fontData[i].getName(), fontSize, fontData[i].getStyle());
			newFontData[i].setLocale(fontData[i].getLocale());
		}
		return new Font(getDisplay(), newFontData);
	}

	/**
	 * @return the label that floats above the text
	 */
	public Label getLabel() {
		return fLabel;
	}

	private GridData getLabelLayoutData() {
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.heightHint = ((fText.computeSize(-1, -1).y) * fLabelToTextRatio) / 100;
		return gridData;
	}

	/**
	 * The default is 90 which means that the label height is 90% of the text text
	 * height.
	 * 
	 * @return the label to text ratio.
	 * @see FloatingText#setRatio(int)
	 */
	public int getLabelRatio() {
		return fLabelToTextRatio;
	}

	@Override
	public GridLayout getLayout() {
		return (GridLayout) super.getLayout();
	}

	private String getMessage() {
		String message = fText.getMessage();
		if (message == null || message.trim().isEmpty()) {
			message = fText.getMessage();
		}
		return message == null ? "" : message.trim();
	}

	/**
	 * @return the underlying text widget.
	 */
	public Text getText() {
		return fText;
	}

	private GridData getTextLayoutData() {
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		return gridData;
	}

	private int removeStyles(int pStyle, int... styles) {
		int result = pStyle;
		for (int i : styles) {
			if ((result & i) == i) {
				result = result ^ i;
			}
		}
		return result;
	}

	/**
	 * Sets the backgrounds of the label and the text to the provided color.
	 *
	 * @param color the color.
	 */
	public void setBackgroundColors(Color color) {
		fText.setBackground(color);
		fLabel.setBackground(color);
	}

	@Override
	public void setEnabled(boolean pEnabled) {
		super.setEnabled(pEnabled);
		fText.setEnabled(pEnabled);
		fLabel.setEnabled(pEnabled);
	}

	/**
	 * Sets the foregrounds of the label and the text to the provided color.
	 *
	 * @param color the color.
	 */
	public void setForegroundColors(Color color) {
		fText.setForeground(color);
		fLabel.setForeground(color);
	}

	private void setLabelText(boolean pFocus) {
		fLabel.setText("");
		String prompt = fText.getMessage();
		if (pFocus) {
			doSetLabelText();
		} else {
			if (!fText.getText().isEmpty() && !fText.getText().equals(prompt)) {
				doSetLabelText();
			}
		}
	}

	/**
	 * Sets the height of the label as ratio of the text height where 100 means that
	 * the label and text are the same size.
	 *
	 * @param ratio the ratio of the label versus the text height
	 */
	public void setRatio(int ratio) {
		fLabelToTextRatio = ratio;
		fLabel.setLayoutData(getLabelLayoutData());
		requestLayout();
	}

	/**
	 * If you have used the SWT.SEPARATOR style hint then you can set the width of
	 * the separator here.
	 * 
	 * @param space the amount of pixels
	 * @return this
	 */
	public FloatingText setSeparatorSpace(final int space) {
		getLayout().verticalSpacing = space;
		requestLayout();
		return this;
	}
}
