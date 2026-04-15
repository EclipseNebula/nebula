/*******************************************************************************
 * Copyright (c) 2019 Laurent CARON.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Laurent CARON (laurent.caron at gmail dot com) - initial API
 * and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.passwordrevealer;

import org.eclipse.nebula.widgets.opal.commons.SWTGraphicUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

class EyeButton extends Canvas {
	private static final int CIRCLE_RAY = 4;
	private boolean mouseIn;
	private boolean pressed;
	private final Color color;
	private Image image, clickImage;
	private boolean isPushMode;
	private boolean pushState;

	EyeButton(final Composite parent, final int style) {
		super(parent, SWT.DOUBLE_BUFFERED);
		isPushMode = (style & SWT.PUSH) == SWT.PUSH;
		addListeners();
		color = new Color(0, 127, 222);
		SWTGraphicUtil.addDisposer(this, color);
		setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
		pushState = false;
	}

	private void addListeners() {
		addPaintListener(e -> {
			paintControl(e);
		});

		addListener(SWT.MouseEnter, event -> {
			mouseIn = true;
			redraw();
		});

		addListener(SWT.MouseExit, event -> {
			mouseIn = false;
			pressed = false;
			redraw();
		});

		addListener(SWT.MouseDown, event -> {
			if (isPushMode) {
				return;
			}
			
			pressed = true;
			((PasswordRevealer) getParent()).revealPassword();
			redraw();
		});

		addListener(SWT.MouseUp, event -> {
			if (isPushMode) {
				pushState = !pushState;
				if (pushState) {
					((PasswordRevealer) getParent()).revealPassword();
				} else {
					((PasswordRevealer) getParent()).hidePassword();
				}
				return;
			}
			
			pressed = false;
			((PasswordRevealer) getParent()).hidePassword();
			redraw();
		});
	}

	private void paintControl(final PaintEvent e) {
		final GC gc = e.gc;
		if (!mouseIn && !pressed) {
			if (image != null) {
				drawImage(gc, image);
			} else {
				drawEye(gc, getDisplay().getSystemColor(SWT.COLOR_GRAY));
			}
			return;
		}

		if (mouseIn && !pressed) {
			if (image != null) {
				drawImage(gc, image);
			} else {
				drawEye(gc, color);
			}
			return;
		}

		if (clickImage == null && image != null) {
			drawImage(gc, image);
			return;
		} else if (clickImage != null) {
			drawImage(gc, clickImage);
			return;

		}

		final Rectangle rect = getClientArea();
		gc.setBackground(color);
		gc.fillRectangle(rect);

		drawEye(gc, getDisplay().getSystemColor(SWT.COLOR_WHITE));
	}

	private void drawEye(final GC gc, final Color clr) {
		gc.setAdvanced(true);
		gc.setAntialias(SWT.ON);
		gc.setLineWidth(2);

		final Rectangle rect = getClientArea();
		final int eyeWidth = (int) (rect.width * .7);
		final int eyeHeight = (int) (rect.height * .5);
		gc.setForeground(clr);
		gc.drawOval((int) (rect.width * .15), (int) (rect.height * .25), eyeWidth, eyeHeight);

		gc.setBackground(clr);
		gc.fillOval(rect.width / 2 - CIRCLE_RAY / 2, rect.height / 2 - CIRCLE_RAY / 2, CIRCLE_RAY, CIRCLE_RAY);
	}

	private void drawImage(final GC gc, final Image img) {
		final Rectangle rect = getClientArea();
		final Rectangle imageBounds = img.getBounds();
		final int middleX = (rect.width - imageBounds.width) / 2;
		final int middleY = (rect.height - imageBounds.height) / 2;
		gc.drawImage(img, middleX, middleY);
	}

	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		final PasswordRevealer parent = (PasswordRevealer) getParent();
		final int preferred = parent.passwordField.computeSize(SWT.DEFAULT, hHint, changed).y;
		int minWidth, minHeight;
		if (image == null) {
			minWidth = minHeight = 20;
		} else {
			minWidth = image.getBounds().width;
			minHeight = image.getBounds().height;
			if (clickImage != null) {
				minWidth = Math.max(clickImage.getBounds().width, minWidth);
				minHeight = Math.max(clickImage.getBounds().height, minHeight);
			}
		}
		return super.computeSize(Math.max(preferred, minWidth), Math.max(preferred, minHeight), changed);
	}

	void setImage(final Image image) {
		this.image = image;
	}

	void setClickImage(final Image clickImage) {
		this.clickImage = clickImage;
	}

	Image getImage() {
		return image;
	}

	Image getClickImage() {
		return clickImage;
	}

	void setPushMode(boolean pushMode) {
		isPushMode = pushMode;
	}

}
