/*******************************************************************************
 * Copyright (c) 2025 Christoph Läubrich.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Christoph Läubrich - initial API and implementation
 *******************************************************************************/
package org.eclipse.nebula.widgets.opal.roundedtoolbar;

import org.eclipse.swt.graphics.Color;

/**
 * Represents a gradient color with a start and end
 */
public record GradientColor(Color start, Color end) {
}
