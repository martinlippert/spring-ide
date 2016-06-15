/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.service.eclipse.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.springframework.ide.service.eclipse.Activator;

/**
 * @author Martin Lippert
 */
public class ClasspathChangedListener implements IElementChangedListener {

	@Override
	public void elementChanged(ElementChangedEvent event) {
		for (IJavaElementDelta delta : event.getDelta().getAffectedChildren()) {
			if ((delta.getFlags() & IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED) != 0
					|| (delta.getFlags() & IJavaElementDelta.F_CLASSPATH_CHANGED) != 0) {
				
				IProject project = delta.getElement().getJavaProject().getProject();
				Activator.getDefault().getServiceManager().classpathChanged(project);
			}
		}
	}

}
