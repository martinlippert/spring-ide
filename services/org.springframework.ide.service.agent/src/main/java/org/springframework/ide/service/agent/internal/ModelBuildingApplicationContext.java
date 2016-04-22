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
package org.springframework.ide.service.agent.internal;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Martin Lippert
 */
public class ModelBuildingApplicationContext extends AnnotationConfigApplicationContext {
	
	public ModelBuildingApplicationContext() {
		super();
	}
	
	@Override
	public void refresh() throws BeansException, IllegalStateException {
		prepareRefresh();
		ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
		prepareBeanFactory(beanFactory);

		try {
			postProcessBeanFactory(beanFactory);
			invokeBeanFactoryPostProcessors(beanFactory);
			registerBeanPostProcessors(beanFactory);
		}
		catch (BeansException ex) {
//			if (logger.isWarnEnabled()) {
//				logger.warn("Exception encountered during context initialization - " +
//						"cancelling refresh attempt: " + ex);
//			}
			
			ex.printStackTrace();

			destroyBeans();
			cancelRefresh(ex);

			throw ex;
		}
		finally {
			resetCommonCaches();
		}
	}
	
}
