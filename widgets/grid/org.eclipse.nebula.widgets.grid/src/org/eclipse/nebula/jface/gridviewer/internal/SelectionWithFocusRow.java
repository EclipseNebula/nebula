package org.eclipse.nebula.jface.gridviewer.internal;

import java.util.List;

import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * FIXME
 */
public class SelectionWithFocusRow extends StructuredSelection {
	private final Object focusElement;

	/**
	 * Creates a new selection with focus row.
	 * 
	 * @param elements the list of selected elements
	 * @param focusElement the element that has focus
	 * @param comparer the element comparer
	 */
	@SuppressWarnings("rawtypes")
	public SelectionWithFocusRow(List elements, Object focusElement, IElementComparer comparer) {
        super(elements,comparer);
        this.focusElement = focusElement;
	}
	
	/**
	 * FIXME
	 * @return the focus element
	 */
	public Object getFocusElement() {
		return focusElement;
	}

}
