package org.eclipse.nebula.widgets.tablecombo;
import java.util.Arrays;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.nebula.jface.tablecomboviewer.TableComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class Bug705 {

	public static void main(String[] args) {

		// get display.
		Display display = new Display();

		// create a new visible shell.
		final Shell shell = new Shell(display);
		shell.setText("Test");
		shell.setSize(600, 400);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(shell);

		final TableComboViewer comboViewer = new TableComboViewer(shell, SWT.MULTI| SWT.BORDER | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboViewer.getControl());
		comboViewer.getTableCombo().defineColumns(1);
		
		// does not work
		// with org.eclipse.nebula.widgets.tablecombo_1.3.0.202606141240.jar
		// from https://download.eclipse.org/nebula/updates/release/3.4.0/index.html
		comboViewer.getTableCombo().setEditable(false);
		comboViewer.getTableCombo().setClosePopupAfterSelection(false);

		comboViewer.setContentProvider(ArrayContentProvider.getInstance());
		comboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return (String) element;
			}
		});
		comboViewer.setInput(Arrays.asList("One", "Two", "Three"));

    final Label label = new Label(shell, SWT.NONE);
    label.setText("...");
    GridDataFactory.fillDefaults().grab(true, false).applyTo(label);
    
    comboViewer.addSelectionChangedListener(event ->
    {
      label.setText(event.getSelection().toString());
    });

		// open the shell.
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}

		// dispose display
		display.dispose();
	}
}