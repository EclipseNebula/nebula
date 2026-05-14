/*******************************************************************************
 * Copyright (c) 2026 Christoph Läubrich
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.opal.switchbutton;

/**
 * Represents the three possible states of a {@link TriStateSwitchButton}.
 *
 * <ul>
 * <li>{@link #FIRST} - the first active state, shown at the left position.</li>
 * <li>{@link #OFF} - the neutral/off state, shown at the center position.</li>
 * <li>{@link #SECOND} - the second active state, shown at the right position.</li>
 * </ul>
 */
public enum TriState {

	/** The first active state (left position). */
	FIRST,

	/** The neutral/off state (center position). */
	OFF,

	/** The second active state (right position). */
	SECOND

}
