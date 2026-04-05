/*****************************************************************************
 * Copyright (c) 2015, 2026 CEA LIST.
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
 *		Dirk Fauth <dirk.fauth@googlemail.com> - Initial API and implementation
 *
 *****************************************************************************/
package org.eclipse.nebula.widgets.richtext;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

// TODO add scrolling support

/**
 * Simple rich text viewer control that uses the {@link RichTextPainter} to render text with HTML
 * markups on a {@link Canvas}.
 */
public class RichTextViewer extends Canvas {

	private String htmlText;

	private RichTextPainter painter;

	/**
	 * 
	 * @param parent
	 * @param style
	 *            The style bits to use.
	 * 
	 * @see SWT#WRAP
	 */
	public RichTextViewer(Composite parent, int style) {
		super(parent, style | SWT.DOUBLE_BUFFERED);

		final boolean wordWrap = (getStyle() & SWT.WRAP) != 0;
		this.painter = new RichTextPainter(wordWrap, () -> parent.getShell().getZoom());

		addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				painter.paintHTML(htmlText != null ? htmlText : "", e.gc, getClientArea());
			}
		});

	}

	public void setText(String text) {
		this.htmlText = text;
		redraw();
		update();
	}
	
	/**
	 * @param wordSplitRegex
	 *            The regular expression that will be used to determine word boundaries.
	 * @since 1.3.0
	 */
	public void setWordSplitRegex(String wordSplitRegex) {
		painter.setWordSplitRegex(wordSplitRegex);
		if (htmlText != null) {
			redraw();
			update();
		}
	}
}
