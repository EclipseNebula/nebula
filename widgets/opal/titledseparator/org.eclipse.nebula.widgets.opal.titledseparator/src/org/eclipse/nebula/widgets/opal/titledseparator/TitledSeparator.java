/*******************************************************************************
 * Copyright (c) 2011 Laurent CARON.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Laurent CARON (laurent.caron@gmail.com) - initial API and
 * implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.opal.titledseparator;

import org.eclipse.nebula.widgets.opal.commons.SWTGraphicUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;

/**
 * Instances of this class provide a separator with a title and/or an image.
 * <p>
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>BORDER</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * </p>
 */
public class TitledSeparator extends Composite {

	private int alignment;
	private Image image;
	private String text;

	/**
	 * Constructs a new instance of this class given its parent and a style value
	 * describing its behavior and appearance.
	 * <p>
	 * The style value is either one of the style constants defined in class
	 * <code>SWT</code> which is applicable to instances of this class, or must be
	 * built by <em>bitwise OR</em>'ing together (that is, using the
	 * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
	 * constants. The class description lists the style constants that are
	 * applicable to the class. Style bits are also inherited from superclasses.
	 * </p>
	 *
	 * @param parent a composite control which will be the parent of the new
	 *            instance (cannot be null)
	 * @param style the style of control to construct
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the parent</li>
	 *                </ul>
	 *
	 */
	public TitledSeparator(final Composite parent, final int style) {
		super(parent, style);
		alignment = SWT.LEFT;

		final Color originalColor = new Color(0, 88, 150);
		setForeground(originalColor);

		final Font originalFont;
		final FontData[] fontData = getFont().getFontData();
		if (fontData != null && fontData.length > 0) {
			final FontData fd = fontData[0];
			fd.setStyle(SWT.BOLD);
			originalFont = new Font(getDisplay(), fd);
			setFont(originalFont);
		} else {
			originalFont = null;
		}

		addListener(SWT.Resize, e-> redrawComposite());

		SWTGraphicUtil.addDisposer(this, originalColor);
		SWTGraphicUtil.addDisposer(this, originalFont);
	}

	/**
	 * Redraw the composite
	 */
	private void redrawComposite() {
		// Dispose previous content
		for (final Control c : getChildren()) {
			c.dispose();
		}

		int numberOfColumns = 1;

		if (text != null) {
			numberOfColumns++;
		}

		if (image != null) {
			numberOfColumns++;
		}

		if (alignment == SWT.CENTER) {
			numberOfColumns++;
		}

		super.setLayout(new GridLayout(numberOfColumns, false));
		createContent();
	}

	/**
	 * Create the content
	 */
	private void createContent() {
		switch (alignment) {
			case SWT.CENTER:
				createSeparator();
				createTitle();
				createSeparator();
				break;
			case SWT.LEFT:
				createTitle();
				createSeparator();
				break;
			default:
				createSeparator();
				createTitle();
				break;
		}
	}

	/**
	 * Create a separator
	 */
	private void createSeparator() {
		final Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		separator.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
	}

	/**
	 * Create the title
	 */
	private void createTitle() {
		if (image != null) {
			final Label imageLabel = createLabel();
			imageLabel.setImage(image);
		}

		if (text != null && !text.trim().equals("")) {
			final Label textLabel = createLabel();
			textLabel.setText(text);
		}
	}

	/**
	 * @return a SWT label
	 */
	private Label createLabel() {
		final Label label = new Label(this, SWT.NONE);
		label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		label.setFont(getFont());
		label.setForeground(getForeground());
		label.setBackground(getBackground());
		return label;
	}

	/**
	 * @see org.eclipse.swt.widgets.Composite#setLayout(org.eclipse.swt.widgets.Layout)
	 */
	@Override
	public void setLayout(final Layout layout) {
		throw new UnsupportedOperationException("Not supported");
	}

	/**
	 * Returns a value which describes the position of the text or image in the
	 * receiver. The value will be one of <code>LEFT</code>, <code>RIGHT</code> or
	 * <code>CENTER</code>.
	 *
	 * @return the alignment
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */

	public int getAlignment() {
		checkWidget();
		return alignment;
	}

	/**
	 * Returns the receiver's image if it has one, or null if it does not.
	 *
	 * @return the receiver's image
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public Image getImage() {
		checkWidget();
		return image;
	}

	/**
	 * Returns the receiver's text.
	 *
	 * @return the receiver's text
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public String getText() {
		checkWidget();
		return text;
	}

	/**
	 * Controls how text will be displayed in the receiver. The argument should be
	 * one of <code>LEFT</code>, <code>RIGHT</code> or <code>CENTER</code>.
	 *
	 * @param alignment the new alignment
	 *
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setAlignment(final int alignment) {
		checkWidget();
		this.alignment = alignment;
		redrawComposite();
	}

	/**
	 * Sets the receiver's image to the argument, which may be null indicating that
	 * no image should be displayed.
	 *
	 * @param image the image to display on the receiver (may be null)
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_INVALID_ARGUMENT - if the image has been
	 *                disposed</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setImage(final Image image) {
		checkWidget();
		this.image = image;
		redrawComposite();
	}

	/**
	 * Sets the receiver's text.
	 *
	 * @param string the new text
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the text is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 */
	public void setText(final String text) {
		checkWidget();
		this.text = text;
		redrawComposite();
	}
}
