/*******************************************************************************
 * Copyright (c) 2012 Laurent CARON
 * All rights reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Laurent CARON (laurent.caron at gmail dot com) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.opal.calculator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.nebula.widgets.opal.commons.SWTGraphicUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * This composite contains all buttons
 */
class CalculatorButtonsComposite extends Composite {

	private static final String LABEL_C = "C";
	private static final String LABEL_CE = "CE";
	private static final String LABEL_BACK = "Back";
	private final Color darkRedColor;
	private final Color darkBlueColor;
	private final CalculatorEngine engine;
	private Label displayArea;
	private Listener keyListener;
	private final List<ModifyListener> modifyListeners;
	private final CalculatorButtonsBehaviourEngine behaviourEngine;
	private boolean readyToEnterNewNumber;

	/**
	 * Constructor
	 *
	 * @param parent parent composite
	 * @param style style
	 */
	CalculatorButtonsComposite(final Composite parent, final int style) {
		super(parent, style);
		setLayout(new GridLayout(5, false));
		darkRedColor = new Color(139, 0, 0);
		darkBlueColor = new Color(0, 0, 139);
		createButtons();

		SWTGraphicUtil.addDisposer(this, darkBlueColor);
		SWTGraphicUtil.addDisposer(this, darkRedColor);

		engine = new CalculatorEngine(this);
		behaviourEngine = new CalculatorButtonsBehaviourEngine(this);
		addKeyListeners();
		modifyListeners = new ArrayList<ModifyListener>();
	}

	/**
	 * Create all buttons
	 */
	private void createButtons() {
		final Button buttonBackSpace = createButton(LABEL_BACK, darkRedColor);
		buttonBackSpace.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false, 3, 1));
		buttonBackSpace.addListener(SWT.Selection, e -> {
			behaviourEngine.processBackSpace();
		});

		final Button buttonCe = createButton(LABEL_CE, darkRedColor);
		buttonCe.addListener(SWT.Selection, e -> {
			behaviourEngine.clearResult();
		});

		final Button buttonC = createButton(LABEL_C, darkRedColor);
		buttonC.addListener(SWT.Selection, e -> {
			behaviourEngine.clearResult();
			engine.cancel();
		});

		createDigitButton(7);
		createDigitButton(8);
		createDigitButton(9);

		final Button buttonDivide = createButton(CalculatorEngine.OPERATOR_DIVIDE, getDisplay().getSystemColor(SWT.COLOR_RED));
		buttonDivide.addListener(SWT.Selection, e -> {
			engine.processOperation(CalculatorEngine.OPERATOR_DIVIDE);
		});

		final Button buttonSqrt = createButton("\u221A", darkRedColor);
		buttonSqrt.addListener(SWT.Selection, e -> {
			engine.processSquareRootOperation();
		});

		createDigitButton(4);
		createDigitButton(5);
		createDigitButton(6);

		final Button buttonMultiply = createButton(CalculatorEngine.OPERATOR_MULTIPLY, getDisplay().getSystemColor(SWT.COLOR_RED));
		buttonMultiply.addListener(SWT.Selection, e -> {
			engine.processOperation(CalculatorEngine.OPERATOR_MULTIPLY);
		});
		final Button buttonInverse = createButton("1/x", darkBlueColor);
		buttonInverse.addListener(SWT.Selection, e -> {
			engine.processInverseOperation();
		});

		createDigitButton(1);
		createDigitButton(2);
		createDigitButton(3);

		final Button buttonMinus = createButton(CalculatorEngine.OPERATOR_MINUS, getDisplay().getSystemColor(SWT.COLOR_RED));
		buttonMinus.addListener(SWT.Selection, e -> {
			engine.processOperation(CalculatorEngine.OPERATOR_MINUS);
		});

		final Button buttonPercent = createButton("%", darkBlueColor);
		buttonPercent.addListener(SWT.Selection, e -> {
			engine.processPerCentageOperation();
		});

		createDigitButton(0);

		final Button buttonPlusMinus = createButton("+/-", getDisplay().getSystemColor(SWT.COLOR_BLUE));
		buttonPlusMinus.addListener(SWT.Selection, e -> {
			engine.processSignChange();
		});
		final Button buttonDot = createButton(".", getDisplay().getSystemColor(SWT.COLOR_BLUE));
		buttonDot.addListener(SWT.Selection, e -> {
			behaviourEngine.addDecimalPoint();
		});

		final Button buttonPlus = createButton(CalculatorEngine.OPERATOR_PLUS, getDisplay().getSystemColor(SWT.COLOR_RED));
		buttonPlus.addListener(SWT.Selection, e -> {
			engine.processOperation(CalculatorEngine.OPERATOR_PLUS);
		});

		final Button buttonEquals = createButton("=", getDisplay().getSystemColor(SWT.COLOR_RED));
		buttonEquals.addListener(SWT.Selection, e -> {
			engine.processEquals();
		});
	}

	private void createDigitButton(final int digit) {
		final Button button = createButton(" " + digit + " ", getDisplay().getSystemColor(SWT.COLOR_BLUE));
		button.addListener(SWT.Selection, e -> {
			behaviourEngine.addDigitToDisplay(digit);
		});

	}

	private Button createButton(final String label, final Color color) {
		final Button button = new Button(this, SWT.PUSH | SWT.DOUBLE_BUFFERED);
		button.setText("");
		final GridData gd = new GridData(GridData.FILL, GridData.FILL, false, false);
		gd.widthHint = 30;
		button.setLayoutData(gd);

		// Use a paint listener because setForeground is not working on Windows
		button.addPaintListener(e -> {
			e.gc.setForeground(color);
			e.gc.setFont(getFont());
			final Point textSize = e.gc.textExtent(" " + label + " ", SWT.TRANSPARENT);
			e.gc.drawText(" " + label + " ", (button.getBounds().width - textSize.x) / 2, (button.getBounds().height - textSize.y) / 2, true);
		});

		return button;
	}

	/**
	 * Add key listeners
	 */
	private void addKeyListeners() {
		keyListener = e -> {

			switch (e.character) {
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
					behaviourEngine.addDigitToDisplay(Integer.parseInt(String.valueOf(e.character)));
					return;
				case '.':
					behaviourEngine.addDecimalPoint();
					return;
				case '+':
					engine.processOperation(CalculatorEngine.OPERATOR_PLUS);
					return;
				case '-':
					engine.processOperation(CalculatorEngine.OPERATOR_MINUS);
					return;
				case '*':
					engine.processOperation(CalculatorEngine.OPERATOR_MULTIPLY);
					return;
				case '/':
					engine.processOperation(CalculatorEngine.OPERATOR_DIVIDE);
					return;
				case '=':
					engine.processEquals();
					return;
				case '%':
					engine.processPerCentageOperation();
					return;

			}

			switch (e.keyCode) {
				case SWT.KEYPAD_0:
				case SWT.KEYPAD_1:
				case SWT.KEYPAD_2:
				case SWT.KEYPAD_3:
				case SWT.KEYPAD_4:
				case SWT.KEYPAD_5:
				case SWT.KEYPAD_6:
				case SWT.KEYPAD_7:
				case SWT.KEYPAD_8:
				case SWT.KEYPAD_9:
					final int digit = e.keyCode - SWT.KEYCODE_BIT - 47;
					behaviourEngine.addDigitToDisplay(digit);
					return;
				case SWT.KEYPAD_ADD:
					engine.processOperation(CalculatorEngine.OPERATOR_PLUS);
					return;
				case SWT.KEYPAD_SUBTRACT:
					engine.processOperation(CalculatorEngine.OPERATOR_MINUS);
					return;
				case SWT.KEYPAD_DIVIDE:
					engine.processOperation(CalculatorEngine.OPERATOR_DIVIDE);
					return;
				case SWT.KEYPAD_MULTIPLY:
					engine.processOperation(CalculatorEngine.OPERATOR_MULTIPLY);
					return;
				case SWT.KEYPAD_CR:
				case SWT.KEYPAD_EQUAL:
				case SWT.CR:
					engine.processEquals();
					return;
				case SWT.BS:
					behaviourEngine.processBackSpace();
					return;
				case SWT.ESC:
					behaviourEngine.clearResult();
					engine.cancel();
					return;
			}
		};

		for (final Control control : getChildren()) {
			control.addListener(SWT.KeyDown, keyListener);
		}
		addListener(SWT.KeyDown, keyListener);

	}

	/**
	 * Adds the listener to the collection of listeners who will be notified when
	 * the receiver's text is modified, by sending it one of the messages defined in
	 * the <code>ModifyListener</code> interface.
	 *
	 * @param listener the listener which should be notified
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 *
	 * @see ModifyListener
	 * @see #removeModifyListener
	 */
	public void addModifyListener(final ModifyListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		modifyListeners.add(listener);
	}

	/**
	 * Fire the modify listeners
	 */
	void fireModifyListeners() {
		for (final ModifyListener listener : modifyListeners) {
			final Event e = new Event();
			e.widget = this;
			final ModifyEvent modifyEvent = new ModifyEvent(e);
			listener.modifyText(modifyEvent);
		}
	}

	/**
	 * @return the keyListener
	 */
	Listener getKeyListener() {
		return keyListener;
	}

	/**
	 * @return the text
	 */
	Label getDisplayArea() {
		return displayArea;
	}

	/**
	 * Removes the listener from the collection of listeners who will be notified
	 * when the receiver's text is modified.
	 *
	 * @param listener the listener which should no longer be notified
	 *
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the listener is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the receiver has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the receiver</li>
	 *                </ul>
	 *
	 * @see ModifyListener
	 * @see #addModifyListener
	 */
	public void removeModifyListener(final ModifyListener listener) {
		checkWidget();
		if (listener == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}
		modifyListeners.remove(listener);
	}

	/**
	 * @param text the text to set
	 */
	void setDisplayArea(final Label text) {
		displayArea = text;
	}

	void setReadyToEnterNewNumber(final boolean newValue) {
		readyToEnterNewNumber = newValue;
	}

	boolean isReadyToEnterNewNumber() {
		return readyToEnterNewNumber;
	}

}
