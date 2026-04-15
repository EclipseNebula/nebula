/*******************************************************************************
 *  Copyright (c) 2010, 2012 Weltevree Beheer BV, Remain Software & Industrial-TSI
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
 *   Wim S. Jongman - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.oscilloscope.multichannel;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

/**
 * Animated widget that tries to mimic an Oscilloscope.
 *
 * <i>An oscilloscope (also known as a scope, CRO or, an O-scope) is a type of
 * electronic test instrument that allows observation of constantly varying
 * signal voltages, usually as a two-dimensional graph of one or more electrical
 * potential differences using the vertical or 'Y' axis, plotted as a function
 * of time, (horizontal or 'x' axis).</i>
 * <p/>
 * <a href="http://en.wikipedia.org/wiki/Oscilloscope">http://en.wikipedia.org/
 * wiki/Oscilloscope<a/>
 *
 * @author Wim.Jongman (@remainsoftware.com)
 *
 */
public class Oscilloscope extends Canvas {

	/**
	 * This class holds the data per channel.
	 *
	 * @author Wim Jongman
	 *
	 */
	private class Data {

		private int base;
		private int baseOffset = BASE_CENTER;
		private boolean connect;
		private int cursor = CURSOR_START_DEFAULT;
		private OscilloscopeDispatcher dispatcher;
		private boolean fade;
		private Color fg;
		private int height = DEFAULT_HEIGHT;
		private int lineWidth = LINE_WIDTH_DEFAULT;
		private int originalSteadyPosition = STEADYPOSITION_75PERCENT;

		/**
		 * This contains the actual values that where input by the user before
		 * scaling. If the user resized we can calculate how the tail would have
		 * looked with the new window dimensions.
		 *
		 * @see Oscilloscope#tail
		 */
		private int originalTailSize;

		private boolean percentage = false;
		private int progression = PROGRESSION_DEFAULT;
		private IntegerFiFoCircularStack stack;
		private List<OscilloscopeStackAdapter> stackListeners;
		private boolean steady;
		/**
		 * This contains the old or historical input and is used to paint the
		 * tail of the graph.
		 */
		private int[] tail;
		private int tailFade = TAILFADE_PERCENTAGE;
		private int tailSize;
		private int width = DEFAULT_WIDTH;
		private boolean antiAlias = false;

	}

	/**
	 * The stack can hold a limited number of values but will never overflow.
	 * Think of the stack as a tube with a given length. If you push too many
	 * values in, the oldest will be pushed out at the end.
	 *
	 */
	public class IntegerFiFoCircularStack {
		private int bottom;
		private final int capacity;
		private final int[] stack;
		private int storedValues;
		private int top;

		/**
		 * Creates a stack with the indicated capacity.
		 *
		 * @param capacity
		 *            must be greater than 1
		 */
		public IntegerFiFoCircularStack(int capacity) {
			if (capacity <= 1) {
				throw new RuntimeException("Stack capacity must be > 1");
			}
			this.capacity = capacity;
			stack = new int[capacity];
			top = 0;
			bottom = 0;
		}

		/**
		 * Creates stack with the indicated capacity and copies the old stack
		 * into the new stack and the old stack will be empty after this action.
		 *
		 * @param capacity
		 *            must be greater than 1
		 * @param oldStack
		 */
		public IntegerFiFoCircularStack(int capacity, IntegerFiFoCircularStack oldStack) {
			this(capacity);
			while (!oldStack.isEmpty()) {
				push(oldStack.pop(0));
			}
		}

		/**
		 * Clears the stack.
		 */
		public void clear() {
			synchronized (stack) {
				for (int i = 0; i < stack.length; i++) {
					stack[i] = 0;
				}
				top = 0;
				bottom = 0;
			}
		}

		public int getCapacity() {
			return capacity;
		}

		/**
		 *
		 * @return boolean
		 */
		public int getLoad() {
			return storedValues;
		}

		/**
		 *
		 * @return boolean
		 */
		public boolean isEmpty() {
			return storedValues == 0;
		}

		/**
		 *
		 * @return boolean
		 */
		public boolean isFull() {
			return storedValues == capacity;
		}

		/**
		 * Returns the oldest value from the stack without removing the value
		 * from the stack. Returns the supplied entry if the stack is empty.
		 *
		 * @param valueIfEmpty
		 * @return int
		 */
		public int peek(int valueIfEmpty) {
			if (storedValues > 0) {
				return stack[bottom];
			}
			return valueIfEmpty;
		}

		/**
		 * Returns the oldest value from the stack. Returns the supplied entry
		 * if the stack is empty.
		 *
		 * @param valueIfEmpty
		 * @return int
		 */
		public int pop(int valueIfEmpty) {
			if (isEmpty()) {
				return valueIfEmpty;
			}

			storedValues--;
			int result = stack[bottom++];

			if (bottom == capacity) {
				bottom = 0;
			}

			return result;
		}

		/**
		 * Returns the oldest value from the stack and negates the value.
		 * Returns the supplied entry if the stack is empty.
		 *
		 * @param valueIfEmpty
		 * @return int
		 */
		public int popNegate(int valueIfEmpty) {
			return pop(valueIfEmpty) * -1;
		}

		/**
		 * Puts a value on the stack.
		 *
		 * @param value
		 */
		public void push(int value) {
			if (storedValues == capacity) {
				top = bottom;
				bottom++;
				if (bottom == capacity) {
					bottom = 0;
				}
			} else {
				storedValues++;
			}

			if (top == capacity) {
				top = 0;
			}

			stack[top++] = value;
		}
	}

	/**
	 * The base of the line is positioned at the center of the widget.
	 *
	 * @see #setBaseOffset(int)
	 */
	public static final int BASE_CENTER = 50;

	/**
	 * The default cursor starting position.
	 */
	public static final int CURSOR_START_DEFAULT = 50;

	/**
	 * The default comfortable widget height.
	 */
	public static final int DEFAULT_HEIGHT = 100;

	/**
	 * The default comfortable widget width.
	 */
	public static final int DEFAULT_WIDTH = 180;

	/**
	 * This set of values will draw a figure that is similar to the heart beat
	 * that you see on hospital monitors.
	 */
	public static final int[] HEARTBEAT = new int[] { 2, 10, 2, -16, 16, 44, 49, 44, 32, 14, -16, -38, -49, -47, -32, -10, 8, 6, 6, -2, 6,
			4, 2, 0, 0, 6, 8, 6 };

	/**
	 * The default line width.
	 */
	public static final int LINE_WIDTH_DEFAULT = 1;

	/**
	 * The default tail fade percentage
	 */
	public static final int PROGRESSION_DEFAULT = 1;

	/**
	 * Steady position @ 75% of graph.
	 */
	public static final int STEADYPOSITION_75PERCENT = -1;
	/**
	 * The default amount of tail fading in percentages (25).
	 */
	public static final int TAILFADE_DEFAULT = 25;

	/**
	 * No tailfade.
	 */
	public static final int TAILFADE_NONE = 0;

	/**
	 * The default tail fade percentage
	 */
	public static final int TAILFADE_PERCENTAGE = 25;

	/**
	 * The default tail size is 75% of the width.
	 */
	public static final int TAILSIZE_DEFAULT = -3;

	/**
	 * Will draw a tail from the left border but is only valid if the boolean in
	 * {@link #setSteady(boolean, int)} was set to true, will default to
	 * {@link #TAILSIZE_MAX} otherwise.
	 */
	public static final int TAILSIZE_FILL = -2;

	/**
	 * Will draw a maximum tail.
	 */
	public static final int TAILSIZE_MAX = -1;

	private Color bg;

	private final Data[] chan;

	// Blocks painting if true
	private boolean paintBlock;

	private int width;

	private int gridSquareSize;
	private int gridLineWidth;
	private Color gridBackground, gridForeground;
	
	/**
	 * Creates a scope with one channel.
	 *
	 * @param parent
	 * @param style
	 */
	public Oscilloscope(Composite parent, int style) {
		this(1, null, parent, style);
	}

	/**
	 * Creates a scope with <code>channels</code> channels.
	 *
	 * @param channels
	 * @param parent
	 * @param style
	 */
	public Oscilloscope(int channels, Composite parent, int style) {
		this(channels, null, parent, style);
	}

	/**
	 * Creates a new scope with <code>channels</code> channels and adds attaches
	 * it to the supplied <code>dispatcher</code>.
	 *
	 * @param channels
	 * @param dispatcher
	 *            may be null
	 * @param parent
	 * @param style
	 */
	public Oscilloscope(int channels, OscilloscopeDispatcher dispatcher, Composite parent, int style) {
		super(parent, SWT.DOUBLE_BUFFERED | style);

		chan = new Data[channels];
		for (int i = 0; i < chan.length; i++) {

			chan[i] = new Data();

			if (dispatcher == null) {
				chan[i].dispatcher = new OscilloscopeDispatcher(i, this);
			} else {
				chan[i].dispatcher = dispatcher;
				dispatcher.setOscilloscope(this);
			}
			bg = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
			setBackground(bg);

			chan[i].fg = Display.getDefault().getSystemColor(SWT.COLOR_WHITE);

			setTailSize(i, TAILSIZE_DEFAULT);
		}

		addListener(SWT.Dispose, e-> Oscilloscope.this.widgetDisposed(e));
		addListener(SWT.Paint, e -> {
			if (!paintBlock) {
				Oscilloscope.this.paintControl(e);
			}
			paintBlock = false;
		});

		addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {

			}
		});

		addListener(SWT.Move, e -> Oscilloscope.this.controlMoved(e));

		addListener(SWT.Resize, e -> {
			paintBlock = true;
			Oscilloscope.this.controlResized(e);
		});

		gridSquareSize = 20;
		gridLineWidth = 1;
		final Color defaultGridBackground = new Color(57, 52, 56);
		gridBackground = defaultGridBackground;
		final Color defaultGridForeground = new Color(81, 96, 77);
		gridForeground = defaultGridForeground;
		addListener(SWT.Dispose, e-> {
			defaultGridBackground.dispose();
			defaultGridForeground.dispose();
		});
		
	}

	/**
	 * Adds a new stack listener to the collection of stack listeners. Adding
	 * the same listener twice will have no effect.
	 * <p/>
	 * This method can be called outside of the UI thread.
	 *
	 * @param listener
	 */
	public synchronized void addStackListener(int channel, OscilloscopeStackAdapter listener) {
		if (chan[channel].stackListeners == null) {
			chan[channel].stackListeners = new ArrayList<>();
		}
		if (!chan[channel].stackListeners.contains(listener)) {
			chan[channel].stackListeners.add(listener);
		}
	}

	/**
	 * This method calculates the progression of the line.
	 *
	 * @return
	 */
	private Object[] calculate(int channel) {

		int c = channel;

		int[] line1 = null;
		int[] line2 = null;
		int splitPos = 0;

		for (int progress = 0; progress < getProgression(c); progress++) {

			if (chan[c].stack.isEmpty() && chan[c].stackListeners != null) {
				notifyListeners(c);
			}

			splitPos = chan[c].tailSize * 4;

			if (!isSteady(c)) {
				chan[c].cursor++;
			}
			if (chan[c].cursor >= chan[c].width) {
				chan[c].cursor = 0;
			}

			// Draw
			int tailIndex = 1;
			line1 = new int[chan[c].tailSize * 4];
			line2 = new int[chan[c].tailSize * 4];

			chan[c].tail[chan[c].tailSize] = transform(c, chan[c].width, chan[c].height, chan[c].stack.popNegate(0));

			for (int i = 0; i < chan[c].tailSize; i++) {

				int posx = chan[c].cursor - chan[c].tailSize + i;
				int pos = i * 4;
				if (posx < 0) {
					posx += chan[c].width;
					line1[pos] = posx - 1;

					line1[pos + 1] = getBase(c) + (isSteady(c) ? 0 : chan[c].tail[tailIndex - 1]);
					line1[pos + 2] = posx;
					line1[pos + 3] = getBase(c) + (isSteady(c) ? 0 : chan[c].tail[tailIndex]);
				}

				else {
					if (splitPos == chan[c].tailSize * 4) {
						splitPos = pos;
					}
					line2[pos] = posx - 1;
					line2[pos + 1] = getBase(c) + chan[c].tail[tailIndex - 1];
					line2[pos + 2] = posx;
					line2[pos + 3] = (getBase(c) + chan[c].tail[tailIndex]);
				}
				chan[c].tail[tailIndex - 1] = chan[c].tail[tailIndex++];
			}
		}

		int[] l1 = new int[splitPos];
		System.arraycopy(line1, 0, l1, 0, l1.length);
		int[] l2 = new int[(chan[c].tailSize * 4) - splitPos];
		System.arraycopy(line2, splitPos, l2, 0, l2.length);

		return new Object[] { l1, l2 };
	}

	private void calculateBase(int channel) {
		if (chan[channel].height > 2) {
			chan[channel].base = (chan[channel].height * +(100 - getBaseOffset(channel))) / 100;
		}
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		checkWidget();

		int width;
		int height;

		if (wHint != SWT.DEFAULT) {
			width = wHint;
		} else {
			width = DEFAULT_WIDTH;
		}

		if (hHint != SWT.DEFAULT) {
			height = hHint;
		} else {
			height = DEFAULT_HEIGHT;
		}

		return new Point(width + 2, height + 2);
	}

	protected void controlMoved(Event e) {

	}

	protected void controlResized(Event e) {
		setSizeInternal(getSize().x, getSize().y);
		width = getBounds().width;
		if (getBounds().width > 0) {
			for (int channel = 0; channel < chan.length; channel++) {
				setSteady(channel, chan[channel].steady, chan[channel].originalSteadyPosition);
				setTailSizeInternal(channel);
			}
		}
	}

	/**
	 * This method can be called outside of the UI thread.
	 *
	 * @return the base of the line.
	 */
	public int getBase(int channel) {
		return chan[channel].base;
	}

	/**
	 * Gets the relative location where the line is drawn in the widget. This
	 * method can be called outside of the UI thread.
	 *
	 * @return baseOffset
	 */
	public int getBaseOffset(int channel) {
		return chan[channel].baseOffset;
	}

	/**
	 * This method can be called outside of the UI thread.
	 *
	 * @return int, number of channels.
	 */
	public int getChannels() {
		return chan.length;
	}

	/**
	 * This method can be called outside of the UI thread.
	 *
	 * @param channel
	 * @return the dispatcher associated with this channel.
	 */
	public OscilloscopeDispatcher getDispatcher(int channel) {
		return chan[channel].dispatcher;
	}

	/**
	 * This method can be called outside of the UI thread.
	 *
	 * @param channel
	 * @return the foreground color associated with the supplied channel.
	 */
	public Color getForeground(int channel) {
		return chan[channel].fg;
	}

	/**
	 * This method can be called outside of the UI thread.
	 *
	 * @return int, the width of the line.
	 * @see #setLineWidth(int)
	 */
	public int getLineWidth(int channel) {
		return chan[channel].lineWidth;
	}

	/**
	 * This method can be called outside of the UI thread.
	 *
	 * @return the number of internal calculation steps at each draw request.
	 * @see #setProgression(int)
	 */
	public int getProgression(int channel) {
		return chan[channel].progression;
	}

	/**
	 * Gets the percentage of tail that must be faded out. This method can be
	 * called outside of the UI thread.
	 *
	 * @return int percentage
	 * @see #setFade(boolean)
	 */
	public int getTailFade(int channel) {
		return chan[channel].tailFade;
	}

	/**
	 * Returns the size of the tail. This method can be called outside of the UI
	 * thread.
	 *
	 * @return int
	 * @see #setTailSize(int)
	 * @see #TAILSIZE_DEFAULT
	 * @see #TAILSIZE_FILL
	 * @see #TAILSIZE_MAX
	 *
	 */
	public int getTailSize(int channel) {
		return chan[channel].tailSize;
	}

	/**
	 * This method can be called outside of the UI thread.
	 *
	 * @return boolean, true if the tail and the head of the graph must be
	 *         connected if tail size is {@link #TAILSIZE_MAX} no fading graph.
	 */
	public boolean isConnect(int channel) {
		return chan[channel].connect;
	}

	/**
	 * This method can be called outside of the UI thread.
	 *
	 * @see #setFade(boolean)
	 * @return boolean fade
	 */
	public boolean isFade(int channel) {
		return chan[channel].fade;
	}

	/**
	 * This method can be called outside of the UI thread.
	 *
	 * @return boolean
	 * @see #setPercentage(boolean)
	 */
	public boolean isPercentage(int channel) {
		return chan[channel].percentage;
	}

	/**
	 * This method can be called outside of the UI thread.
	 *
	 * @return boolean steady indicator
	 * @see Oscilloscope#setSteady(boolean, int)
	 */
	public boolean isSteady(int channel) {
		return chan[channel].steady;
	}

	/**
	 * This method can be called outside of the UI thread.
	 *
	 * @return boolean anti-alias indicator
	 * @see Oscilloscope#setAntialias(int, boolean)
	 */
	public boolean isAntiAlias(int channel) {
		return chan[channel].antiAlias;
	}

	public boolean needsRedraw() {
		checkWidget();
		return isDisposed() ? false : true;
	}

	private void notifyListeners(int channel) {
		if (chan[channel].stackListeners == null || chan[channel].stackListeners.size() == 0) {
			return;
		}
		for (int i = 0; i < chan[channel].stackListeners.size(); i++) {
			chan[channel].stackListeners.get(i).stackEmpty(this, channel);
		}
	}

	protected void paintControl(Event e) {

		for (int c = 0; c < chan.length; c++) {

			if (chan[c].tailSize <= 0) {
				chan[c].stack.popNegate(0);
				continue;
			}

			// Go calculate the line
			Object[] result = calculate(c);
			int[] l1 = (int[]) result[0];
			int[] l2 = (int[]) result[1];

			// Draw it
			GC gc = e.gc;
			gc.setForeground(getForeground(c));
			gc.setAdvanced(true);
			gc.setAntialias(chan[c].antiAlias ? SWT.ON : SWT.OFF);
			gc.setLineWidth(getLineWidth(c));

			// Fade tail
			if (isFade(c)) {
				gc.setAlpha(0);
				double fade = 0;
				double fadeOutStep = (double) 125 / (double) ((getTailSize(c) * (getTailFade(c)) / 100));
				for (int i = 0; i < l1.length - 4;) {
					fade += (fadeOutStep / 2);
					setAlpha(gc, fade);
					gc.drawLine(l1[i], l1[i + 1], l1[i + 2], l1[i + 3]);
					i += 2;
				}

				for (int i = 0; i < l2.length - 4;) {
					fade += (fadeOutStep / 2);
					setAlpha(gc, fade);
					gc.drawLine(l2[i], l2[i + 1], l2[i + 2], l2[i + 3]);
					i += 2;
				}

			} else {
				gc.drawPolyline(l1);
				gc.drawPolyline(l2);
			}

			// Connects the head with the tail
			if (isConnect(c) && !isFade(c) && chan[c].originalTailSize == TAILSIZE_MAX && l1.length > 0 && l2.length > 0) {
				gc.drawLine(l2[l2.length - 2], l2[l2.length - 1], l1[0], l1[1]);
			}
		}
	}

	/**
	 * Removes a stack listener from the collection of stack listeners. This
	 * method can be called outside of the UI thread.
	 *
	 * @param listener
	 */
	public void removeStackListener(int channel, OscilloscopeStackAdapter listener) {
		if (chan[channel].stackListeners != null) {
			chan[channel].stackListeners.remove(listener);
			if (chan[channel].stackListeners.size() == 0) {
				synchronized (chan[channel].stackListeners) {
					chan[channel].stackListeners = null;
				}
			}
		}
	}

	private void setAlpha(GC gc, double fade) {

		if (gc.getAlpha() == fade) {
			return;
		}
		if (fade >= 255) {
			gc.setAlpha(255);
		} else {
			gc.setAlpha((int) fade);
		}
	}

	/**
	 * Gets the relative location where the line is drawn in the widget, the
	 * default is <code>BASE_CENTER</code> which is in the middle of the scope.
	 * This method can be called outside of the UI thread.
	 *
	 * @param baseOffset
	 *            must be between 100 and -100, exceeding values are rounded to
	 *            the closest allowable value.
	 */
	public void setBaseOffset(int channel, int baseOffset) {

		if (baseOffset > 100) {
			baseOffset = 100;
		}

		if (baseOffset < -100) {
			baseOffset = -100;
		}

		chan[channel].baseOffset = baseOffset;

		calculateBase(channel);
	}

	/**
	 * Connects head and tail only if tail size is {@link #TAILSIZE_MAX} and no
	 * fading. This method can be called outside of the UI thread.
	 *
	 * @param connectHeadAndTail
	 */
	public void setConnect(int channel, boolean connectHeadAndTail) {
		chan[channel].connect = connectHeadAndTail;
	}

	/**
	 * Sets if the line must be anti-aliased which uses more processing power in
	 * return of a smoother image. The default value is false. This method can
	 * be called outside of the UI thread.
	 *
	 * @param channel
	 * @param antialias
	 */
	public void setAntialias(int channel, boolean antialias) {
		chan[channel].antiAlias = antialias;
	}

	/**
	 * Sets the dispatcher that is associated with the supplied channel. This
	 * method can be called outside of the UI thread.
	 *
	 * @param channel
	 * @param dispatcher
	 */
	public void setDispatcher(int channel, OscilloscopeDispatcher dispatcher) {
		chan[channel].dispatcher = dispatcher;
	}

	/**
	 * Sets fade mode so that a percentage of the tail will be faded out at the
	 * costs of extra CPU utilization (no beauty without pain or as the Dutch
	 * say: "Wie mooi wil gaan moet pijn doorstaan"). The reason for this is
	 * that each pixel must be drawn separately with alpha faded in instead of
	 * the elegant {@link GC#drawPolygon(int[])} routine which does not support
	 * alpha blending.
	 * <p>
	 * In addition to this, set the percentage of tail that must be faded out
	 * {@link #setTailFade(int)}.
	 * <p>
	 * This method can be called outside of the UI thread.
	 *
	 * @param fade
	 *            true or false
	 * @see #setTailFade(int)
	 */
	public void setFade(int channel, boolean fade) {
		chan[channel].fade = fade;
	}

	/**
	 * Sets the foreground color for the supplied channel.
	 * <p/>
	 * This method can be called outside of the UI thread.
	 *
	 * @param channel
	 * @param color
	 */
	public void setForeground(int channel, Color color) {
		chan[channel].fg = color;
	}

	/**
	 * Sets the line width. A value equal or below zero is ignored. The default
	 * width is 1. This method can be called outside of the UI thread.
	 *
	 * @param lineWidth
	 */
	public void setLineWidth(int channel, int lineWidth) {
		if (lineWidth > 0) {
			chan[channel].lineWidth = lineWidth;
		}
	}

	/**
	 * If set to true then the values are treated as percentages of the
	 * available space rather than absolute values. This will scale the
	 * amplitudes if the control is resized. Default is false.
	 * <p/>
	 * This method can be called outside of the UI thread.
	 *
	 * @param percentage
	 *            true if percentages
	 */
	public void setPercentage(int channel, boolean percentage) {
		chan[channel].percentage = percentage;
	}

	/**
	 * The number of internal steps that must be made before drawing. Normally
	 * this will slide the graph one pixel. Setting this to a higher value will
	 * speed up the animation at the cost of a more jerky motion.
	 * <p/>
	 * This method can be called outside of the UI thread.
	 *
	 * @param progression
	 */
	public void setProgression(int channel, int progression) {
		if (progression > 0) {
			chan[channel].progression = progression;
		}

	}

	private void setSizeInternal(int width, int height) {

		for (int c = 0; c < chan.length; c++) {

			chan[c].width = width;
			chan[c].height = height;

			// calculate the base of the line
			calculateBase(c);

			if (width > 1) {
				if (chan[c].stack == null) {
					chan[c].stack = new IntegerFiFoCircularStack(width);
				} else {
					chan[c].stack = new IntegerFiFoCircularStack(width, chan[c].stack);
				}
			}
		}
	}

	/**
	 * If steady is true the graph will draw on a steady position instead of
	 * advancing.
	 * <p/>
	 * This method can be called outside of the UI thread.
	 *
	 * @param steady
	 * @param steadyPosition
	 */
	public void setSteady(int channel, boolean steady, int steadyPosition) {
		chan[channel].steady = steady;
		chan[channel].originalSteadyPosition = steadyPosition;
		if (steady) {
			if (steadyPosition == STEADYPOSITION_75PERCENT) {
				chan[channel].cursor = (int) (chan[channel].width * 0.75);
			} else if (steadyPosition > 0 && steadyPosition < chan[channel].width) {
				chan[channel].cursor = steadyPosition;
			}
		}
	}

	/**
	 * Sets the percentage of tail that must be faded out. If you supply 100
	 * then the tail is faded out all the way to the top. The effect will become
	 * increasingly less obvious.
	 * <p/>
	 * This method can be called outside of the UI thread.
	 *
	 * @param tailFade
	 */
	public void setTailFade(int channel, int tailFade) {
		checkWidget();
		if (tailFade > 100) {
			tailFade = 100;
		}
		if (tailFade < 1) {
			tailFade = 1;
		}
		chan[channel].tailFade = tailFade;
	}

	/**
	 * The tail size defaults to TAILSIZE_DEFAULT which is 75% of the width.
	 * Setting it with TAILSIZE_MAX will leave one pixel between the tail and
	 * the head. All values are absolute except TAILSIZE*. If the width is
	 * smaller then the tail size then the tail size will behave like
	 * TAILSIZE_MAX.
	 *
	 * @param size
	 *            the size of the tail
	 * @see #getTailSize()
	 * @see #TAILSIZE_DEFAULT
	 * @see #TAILSIZE_FILL
	 * @see #TAILSIZE_MAX
	 */
	public void setTailSize(int channel, int size) {
		checkWidget();

		if (size == TAILSIZE_FILL && !isSteady(channel)) {
			size = TAILSIZE_MAX;
		}

		if (chan[channel].originalTailSize != size) {
			tailSizeCheck(size);
			chan[channel].originalTailSize = size;
			setTailSizeInternal(channel);
		}
	}

	private void setTailSizeInternal(int channel) {

		if (chan[channel].originalTailSize == TAILSIZE_DEFAULT) {
			chan[channel].tailSize = (chan[channel].width / 4) * 3;
			chan[channel].tailSize--;
		} else if (chan[channel].originalTailSize == TAILSIZE_FILL) {
			if (isSteady(channel)) {
				chan[channel].tailSize = chan[channel].originalSteadyPosition - 1;
			} else {
				// act as if TAILSIZE_MAX
				chan[channel].tailSize = chan[channel].width - 2;
			}
		} else if (chan[channel].originalTailSize == TAILSIZE_MAX || chan[channel].originalTailSize > chan[channel].width) {
			chan[channel].tailSize = chan[channel].width - 2;
		} else if (chan[channel].tailSize != chan[channel].originalTailSize) {
			chan[channel].tailSize = chan[channel].originalTailSize;
		}

		// Transform the old tail. This is we want to see sort of the same form
		// after resize.
		int[] oldTail = chan[channel].tail;
		if (oldTail == null) {
			chan[channel].tail = new int[chan[channel].tailSize + 1];
		} else {
			chan[channel].tail = new int[chan[channel].tailSize + 1];
			if (chan[channel].tail.length >= oldTail.length) {
				for (int i = 0; i < oldTail.length; i++) {
					chan[channel].tail[chan[channel].tail.length - 1 - i] = oldTail[oldTail.length - 1 - i];
				}
			} else {
				for (int i = 0; i < chan[channel].tail.length; i++) {
					chan[channel].tail[chan[channel].tail.length - 1 - i] = oldTail[oldTail.length - 1 - i];
				}
			}
		}
	}

	/**
	 * Sets a value to be drawn relative to the center of the channel. Supply a
	 * positive or negative value. This method will only accept values if the
	 * width of the scope > 0. The values will be stored in a stack and popped
	 * once a value is needed. The size of the stack is the width of the widget.
	 * If you resize the widget, the old stack will be copied into a new stack
	 * with the new capacity.
	 * <p/>
	 * This method can be called outside of the UI thread.
	 *
	 * @param channel
	 * @param value
	 *            which is an absolute value or a percentage
	 *
	 * @see #isPercentage(int)
	 * @see #setBaseOffset(int, int)
	 */
	public void setValue(int channel, int value) {
		if (width > 0) {
			if (chan[channel].stack.capacity > 0) {
				chan[channel].stack.push(value);
			}
		}
	}

	/**
	 * Sets a bunch of values that will be drawn. See
	 * {@link #setValue(int, int)} for details.
	 * <p/>
	 * This method can be called outside of the UI thread.
	 *
	 * @param channel
	 * @param values
	 *
	 * @see #setValue(int, int)
	 */
	public synchronized void setValues(int channel, int[] values) {
		if (width > 0) {
			for (int value : values) {
				setValue(channel, value);
			}
		}
	}

	private void tailSizeCheck(int size) {
		if (size < -3 || size == 0) {
			throw new RuntimeException("Invalid tail size " + size);
		}
	}

	/**
	 * Transforms the value before it is drawn.
	 *
	 * @param value
	 *            the next value to be processed
	 * @return the transformed value
	 */
	private int transform(int channel, int vWidth, int vHeight, int value) {
		if (isPercentage(channel)) {
			return (((vHeight / 2) * value) / 100);
		}
		return value;
	}

	protected void widgetDisposed(Event e) {
		bg.dispose();
		for (Data element : chan) {
			element.fg.dispose();
		}
	}

	/**
	 * @return the width of the square displayed in the grid
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public int getGridSquareSize() {
		checkWidget();
		return gridSquareSize;
	}

	/**
	 * Set the size of the square displayed in the grid
	 * @param size the new size
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *    <li>ERROR_INVALID_ARGUMENT - if size is lower than 1</li>
	 * </ul>
	 */
	public void setGridSquareSize(int size) {
		checkWidget();
		if (size < 1) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		this.gridSquareSize = size;
		updateGrid();
	}

	private void updateGrid() {
		if (chan.length > 0) {
			OscilloscopeDispatcher dispatcher = chan[0].dispatcher;
			dispatcher.updateBackgroundImage();
			setBackgroundImage(dispatcher.getBackgroundImage());
		}
	}

	/**
	 * @return the width of the lines of the grid
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public int getGridLineWidth() {
		checkWidget();
		return gridLineWidth;
	}

	/**
	 * Set the with of the lines size of the square displayed in the grid
	 * @param gridLineWidth the new size
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 *    <li>ERROR_INVALID_ARGUMENT - if gridLineWidth is lower than 1</li>
	 * </ul>
	 */
	public void setGridLineWidth(int gridLineWidth) {
		checkWidget();
		if (gridLineWidth < 1) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}
		this.gridLineWidth = gridLineWidth;
		updateGrid();
	}

	/**
	 * Returns the background color of the oscilloscope.
	 *
	 * @return the oscilloscope background color
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public Color getGridBackground() {
		checkWidget();
		return gridBackground;
	}

	/**
	 * Sets the oscilloscope's background color to the color specified
	 * by the argument, or to the default system color for the control
	 * if the argument is null.
	 * @param color the new color (or null)
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setGridBackground(Color gridBackground) {
		checkWidget();
		this.gridBackground = gridBackground;
		updateGrid();
	}

	/**
	 * Returns the color of the grid.
	 *
	 * @return the color or the grid
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public Color getGridForeground() {
		checkWidget();
		return gridForeground;
	}

	/**
	 * Sets the color of the grid to the color specified by the argument, or to the default system color for the control
	 * if the argument is null.
	 * @param color the new color (or null)
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_INVALID_ARGUMENT - if the argument has been disposed</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setGridForeground(Color gridForeground) {
		checkWidget();
		this.gridForeground = gridForeground;
		updateGrid();
	}
	
	
}
