package org.eclipse.nebula.jface.gridviewer.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IElementComparer;

/**
 * A selection that includes both elements and their corresponding cell indices.
 */
public class CellSelection extends SelectionWithFocusRow {
	@SuppressWarnings("rawtypes")
	private List indicesList;
	@SuppressWarnings("rawtypes")
	private List elements;
	
    /**
	 * Creates a structured selection from the given <code>List</code> and
	 * element comparer. If an element comparer is provided, it will be used to
	 * determine equality between structured selection objects provided that
	 * they both are based on the same (identical) comparer. See bug 
	 * 
	 * @param elements
	 *            list of selected elements
	 * @param indicesList
	 *            list of indices for each element
	 * @param focusElement
	 *            the element that has focus
	 * @param comparer
	 *            the comparer, or null
	 * @since 3.4
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public CellSelection(List elements, List indicesList, Object focusElement, IElementComparer comparer) {
        super(elements,focusElement,comparer);
        this.elements = new ArrayList(elements);
        this.indicesList = indicesList;
	}
	
	/**
	 * Returns the list of indices for the given element.
	 * 
	 * @param element the element to get indices for
	 * @return the indices
	 */
	@SuppressWarnings("rawtypes")
	public List getIndices(Object element) {
		return (List) indicesList.get(elements.indexOf(element));
	}	
}
