/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    cgross - initial API and implementation
 *******************************************************************************/

package org.eclipse.nebula.examples;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * The base class for all Nebula example extensions. Extenders will need to
 * provide implementation for <code>createParameters</code> and
 * <code>createControl</code>.
 *
 * Extenders may also require the use of following methods:
 * <ul>
 * <li><code>recreateExample</code></li>
 * <li><code>relayoutExample</code></li>
 * <li><code>addEventParticipant</code></li>
 * </ul>
 *
 * @author cgross
 */
public abstract class AbstractExampleTab {
	public static int[] eventIds = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
			21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37 };
	public static String[] eventNames = new String[] { "KeyDown", "KeyUp", "MouseDown", "MouseUp", "MouseMove",
			"MouseEnter", "MouseExit", "MouseDoubleClick", "Paint", "Move", "Resize", "Dispose", "Selection",
			"DefaultSelection", "FocusIn", "FocusOut", "Expand", "Collapse", "Iconify", "Deiconify", "Close", "Show",
			"Hide", "Modify", "Verify", "Activate", "Deactivate", "Help", "DragDetect", "Arm", "Traverse", "MouseHover",
			"HardKeyDown", "HardKeyUp", "MenuDetect", "SetData", "MouseWheel" };

	private ArrayList selectedEvents = new ArrayList();
	private ArrayList additionalEventParticipants = new ArrayList();

	private Listener listenerThatPrints;

	private GridData controlGridData = new GridData();
	private Composite controlArea;
	private Control controlExample;

	private boolean vFill = false;
	private boolean hFill = false;
	private Button listen;

	private Color modifiedBack;
	private Color modifiedFore;

	/**
	 * Create the parameters section that will display to the right of the example
	 * widget.
	 *
	 * @param parent
	 *            the parent composite.
	 */
	public abstract void createParameters(Composite parent);

	/**
	 * Create the control, based upon the selections made on the widgets created in
	 * <code>createParameters</code>.
	 *
	 * @param parent
	 *            the parent composite.
	 * @return the example control.
	 */
	public abstract Control createControl(Composite parent);

	public abstract String[] createLinks();

	/**
	 * Recreates the example control. This method will call
	 * <code>createControl</code>. Extenders should call this method after a user
	 * changes an option or style on the control that will require the control to be
	 * recreated.
	 */
	protected void recreateExample() {
		if (controlExample != null && !controlExample.isDisposed()) {
			controlExample.dispose();
		}

		controlExample = createControl(controlArea);

		if (modifiedBack != null) {
			controlExample.setBackground(modifiedBack);
		}

		if (modifiedFore != null) {
			controlExample.setForeground(modifiedFore);
		}

		updateListeners();

		controlExample.setLayoutData(controlGridData);

		controlArea.layout(true);
	}

	/**
	 * Recalculates and repositions the layout of the example control based upon the
	 * layout options chosen by the user. Extenders may wish to call this method if
	 * a user changes a widget parameter that might affect the layout (usually the
	 * preferred size).
	 */
	protected void relayoutExample() {
		controlGridData.verticalAlignment = vFill ? SWT.FILL : SWT.BEGINNING;
		controlGridData.grabExcessVerticalSpace = vFill;

		controlGridData.horizontalAlignment = hFill ? SWT.FILL : SWT.BEGINNING;
		controlGridData.grabExcessHorizontalSpace = hFill;

		controlExample.setLayoutData(controlGridData);
		controlArea.layout(true, true);
	}

	/**
	 * Creates the main container and default example options (such as event
	 * listening and size options). This method should not be called by extenders.
	 *
	 * @param parent
	 */
	public final void create(Composite parent) {
		GridLayout gl = new GridLayout(2, false);
		parent.setLayout(gl);

		controlArea = new Composite(parent, SWT.NONE);
		GridData gd = new GridData();
		gd.verticalAlignment = SWT.FILL;
		gd.horizontalAlignment = SWT.FILL;
		gd.grabExcessHorizontalSpace = true;
		gd.minimumHeight = 100;
		controlArea.setLayoutData(gd);
		controlArea.setLayout(new GridLayout());

		Group paramsGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		paramsGroup.setText("Parameters");
		paramsGroup.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		paramsGroup.setLayout(new FillLayout());

		ScrolledComposite sc = new ScrolledComposite(paramsGroup, SWT.V_SCROLL);
		sc.getVerticalBar().setIncrement(10);
		sc.getVerticalBar().setPageIncrement(100);
		Composite content = new Composite(sc, SWT.NONE);
		content.setLayout(new GridLayout());

		Composite paramsArea = new Composite(content, SWT.NONE);
		createParameters(paramsArea);

		Composite lowerParamsArea = new Composite(content, SWT.NONE);
		lowerParamsArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(lowerParamsArea);

		Group sizeGroup = new Group(lowerParamsArea, SWT.SHADOW_ETCHED_IN);
		sizeGroup.setText("Size");
		sizeGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

		createSize(sizeGroup);

		Group linksGroup = new Group(lowerParamsArea, SWT.SHADOW_ETCHED_IN);
		linksGroup.setText("Links");
		linksGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

		createLinks(linksGroup);

		Composite thirdParmsArea = new Composite(content, SWT.NONE);
		thirdParmsArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(thirdParmsArea);

		Group colorsGroup = new Group(thirdParmsArea, SWT.SHADOW_ETCHED_IN);
		colorsGroup.setText("Colors");
		colorsGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

		createColors(colorsGroup);

		Group backModeGroup = new Group(thirdParmsArea, SWT.SHADOW_ETCHED_IN);
		backModeGroup.setText("Background Mode on Parent");
		backModeGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

		createBackMode(backModeGroup);

		sc.setContent(content);
		sc.setMinSize(content.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);

		Group listenersGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		listenersGroup.setText("Listeners");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		listenersGroup.setLayoutData(gd);

		createListeners(listenersGroup);

		recreateExample();
		relayoutExample();
	}

	private void createColors(Composite parent) {
		parent.setLayout(new GridLayout());
		Button back = new Button(parent, SWT.PUSH);
		back.setText("Background...");
		back.addListener(SWT.Selection, event -> {
			ColorDialog cd = new ColorDialog(Display.getCurrent().getActiveShell());
			RGB newRGB = cd.open();
			if (newRGB != null) {
				Color newColor = new Color(newRGB);
				controlExample.setBackground(newColor);
				if (modifiedBack != null) {
					modifiedBack.dispose();
				}
				modifiedBack = newColor;
			}
		});
		Button fore = new Button(parent, SWT.PUSH);
		fore.setText("Foreground...");
		fore.addListener(SWT.Selection, event -> {
			ColorDialog cd = new ColorDialog(Display.getCurrent().getActiveShell());
			RGB newRGB = cd.open();
			if (newRGB != null) {
				Color newColor = new Color(newRGB);
				controlExample.setForeground(newColor);
				if (modifiedFore != null) {
					modifiedFore.dispose();
				}
				modifiedFore = newColor;
			}
		});
	}

	private void createBackMode(Composite parent) {
		parent.setLayout(new GridLayout());
		final Combo backMode = new Combo(parent, SWT.READ_ONLY);
		backMode.setItems(new String[] { "SWT.INHERIT_NONE", "SWT.INHERIT_DEFAULT", "SWT.INHERIT_FORCE" });
		backMode.select(0);
		backMode.addListener(SWT.Selection, event -> {
			int mode = SWT.INHERIT_NONE;
			if (backMode.getText().indexOf("DEFAULT") != -1) {
				mode = SWT.INHERIT_DEFAULT;
			}
			if (backMode.getText().indexOf("FORCE") != -1) {
				mode = SWT.INHERIT_FORCE;
			}
			controlArea.setBackgroundMode(mode);
		});

		final Button backImage = new Button(parent, SWT.CHECK);
		backImage.setText("Background Image");
		backImage.addListener(SWT.Selection, event -> {
			if (backImage.getSelection()) {
				controlArea.setBackgroundImage(ExamplesView.getImage("icons/background.PNG"));
			} else {
				controlArea.setBackgroundImage(null);
			}
		});

		final Button backColor = new Button(parent, SWT.CHECK);
		backColor.setText("Background Color");
		backColor.addListener(SWT.Selection, event -> {
			if (backColor.getSelection()) {
				controlArea.setBackground(controlArea.getDisplay().getSystemColor(SWT.COLOR_CYAN));
			} else {
				controlArea.setBackground(null);
			}
		});
	}

	private void createLinks(Composite parent) {
		parent.setLayout(new GridLayout());

		String[] links = createLinks();

		if (links == null) {
			return;
		}

		for (String link2 : links) {
			Link link = new Link(parent, SWT.NONE);
			link.setText(link2);
			link.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					Program.launch(e.text);
				}
			});
		}
	}

	/**
	 * @param parent
	 */
	private void createSize(Composite parent) {
		parent.setLayout(new GridLayout());

		Button prefSize = new Button(parent, SWT.RADIO);
		prefSize.setText("Preferred");
		prefSize.setSelection(true);
		prefSize.addListener(SWT.Selection, event -> {
			controlGridData = new GridData();
			relayoutExample();
		});

		Button tenSize = new Button(parent, SWT.RADIO);
		tenSize.setText("10 X 10");
		tenSize.addListener(SWT.Selection, event -> {
			controlGridData = new GridData(10, 10);
			relayoutExample();
		});

		Button fiftySize = new Button(parent, SWT.RADIO);
		fiftySize.setText("50 X 50");
		fiftySize.addListener(SWT.Selection, event -> {
			controlGridData = new GridData(50, 50);
			relayoutExample();
		});

		Button hundredSize = new Button(parent, SWT.RADIO);
		hundredSize.setText("100 X 100");
		hundredSize.addListener(SWT.Selection, event -> {
			controlGridData = new GridData(100, 100);
			relayoutExample();
		});

		final Button hFillB = new Button(parent, SWT.CHECK);
		hFillB.setText("Horizontal Fill");
		hFill = getInitialHorizontalFill();
		hFillB.setSelection(hFill);
		hFillB.addListener(SWT.Selection, event -> {
			hFill = hFillB.getSelection();
			relayoutExample();
		});

		final Button vFillB = new Button(parent, SWT.CHECK);
		vFillB.setText("Vertical Fill");
		vFill = getInitialVerticalFill();
		vFillB.setSelection(vFill);
		vFillB.addListener(SWT.Selection, event -> {
			vFill = vFillB.getSelection();
			relayoutExample();
		});
	}

	/**
	 * Gets the initial value for horizontal fill, subclasses may override.
	 *
	 * @return <code>true</code> if the horizontal fill flag must be set the default
	 *         is <code>false</code>
	 */
	public boolean getInitialHorizontalFill() {
		return false;
	}

	/**
	 * Gets the initial value for vertical fill, subclasses may override.
	 *
	 * @return <code>true</code> if the vertical fill flag must be set the default
	 *         is <code>false</code>
	 */
	public boolean getInitialVerticalFill() {
		return false;
	}

	private void createListeners(Composite parent) {
		parent.setLayout(new GridLayout(4, false));

		ButtonFactory.create(parent, SWT.PUSH, "Select Listeners", event -> {
			ListenersDialog dialog = new ListenersDialog(Display.getCurrent().getActiveShell());

			if (dialog.open(selectedEvents) == Window.OK) {
				updateListeners();
			}
		});

		listen = ButtonFactory.create(parent, SWT.CHECK, "Listen", event -> updateListeners());

		Label spacer = new Label(parent, SWT.NONE);
		spacer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Button clear = new Button(parent, SWT.PUSH);
		clear.setText("Clear");

		final Text eventText = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 120;
		gd.horizontalSpan = 4;
		eventText.setLayoutData(gd);

		clear.addListener(SWT.Selection, event -> eventText.setText(""));

		listenerThatPrints = event -> {
			TypedEvent typedEvent = getTypedEvent(event);
			eventText.append("\n" + typedEvent.toString());
		};
	}

	private void updateListeners() {
		for (int eventId : eventIds) {
			boolean selected = selectedEvents.contains(new Integer(eventId));

			controlExample.removeListener(eventId, listenerThatPrints);
			if (selected && listen.getSelection()) {
				controlExample.addListener(eventId, listenerThatPrints);
			}

			for (Iterator iterator = additionalEventParticipants.iterator(); iterator.hasNext();) {
				Widget participant = (Widget) iterator.next();

				participant.removeListener(eventId, listenerThatPrints);
				if (selected && listen.getSelection()) {
					participant.addListener(eventId, listenerThatPrints);
				}
			}
		}
	}

	/**
	 * Adds the given widget to the list of which will participate in the event
	 * listening mechanism of the example tab. In other words, if a user has chosen
	 * to listen for a specific event, and that event is fired on the given widget,
	 * the event's details will print in the event text area in the example.
	 *
	 * This method is primarily used to include a widget's <code>Item</code>
	 * children in the event listening mechanism.
	 *
	 * @param widget
	 */
	protected void addEventParticipant(final Widget widget) {
		additionalEventParticipants.add(widget);
		widget.addDisposeListener(e -> additionalEventParticipants.remove(widget));
	}

	private TypedEvent getTypedEvent(Event e) {
		TypedEvent typedEvent = null;

		switch (e.type) {
		case SWT.KeyDown:
		case SWT.KeyUp:
			typedEvent = new KeyEvent(e);
			break;

		case SWT.MouseUp:
		case SWT.MouseDown:
		case SWT.MouseDoubleClick:
		case SWT.MouseMove:
		case SWT.MouseEnter:
		case SWT.MouseExit:
		case SWT.MouseHover:
		case SWT.MouseWheel:
			typedEvent = new MouseEvent(e);
			break;

		case SWT.Paint:
			typedEvent = new PaintEvent(e);
			break;

		case SWT.Move:
		case SWT.Resize:
			typedEvent = new ControlEvent(e);
			break;

		case SWT.Dispose:
			typedEvent = new DisposeEvent(e);
			break;

		case SWT.Selection:
		case SWT.DefaultSelection:
			typedEvent = new SelectionEvent(e);
			break;

		case SWT.FocusIn:
		case SWT.FocusOut:
			typedEvent = new FocusEvent(e);
			break;

		case SWT.Expand:
		case SWT.Collapse:
			typedEvent = new TreeEvent(e);
			break;

		case SWT.Iconify:
		case SWT.Deiconify:
		case SWT.Close:
		case SWT.Show:
		case SWT.Hide:
		case SWT.Activate:
		case SWT.Deactivate:
			typedEvent = new ShellEvent(e);
			break;

		case SWT.Modify:
			typedEvent = new ModifyEvent(e);
			break;

		case SWT.Verify:
			typedEvent = new VerifyEvent(e);
			break;

		case SWT.Help:
			typedEvent = new HelpEvent(e);
			break;

		case SWT.Arm:
			typedEvent = new ArmEvent(e);
			break;

		case SWT.Traverse:
			typedEvent = new TraverseEvent(e);
			break;

		default:
			typedEvent = new TypedEvent(e);
			break;
		}

		return typedEvent;
	}

	/**
	 * This method is called when the user reveals the tab.
	 */
	public void reveal() {

	}

}
