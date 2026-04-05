/*****************************************************************************
 * Copyright (c) 2015 CEA LIST.
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
package org.eclipse.nebula.widgets.richtext.example.e4.part;

import jakarta.annotation.PostConstruct;

import org.eclipse.nebula.widgets.richtext.example.RichTextViewerExample;
import org.eclipse.swt.widgets.Composite;

public class RichTextViewerExamplePart {

	@PostConstruct
	public void postConstruct(Composite parent) {
		RichTextViewerExample example = new RichTextViewerExample();
		example.createControls(parent);
	}

}