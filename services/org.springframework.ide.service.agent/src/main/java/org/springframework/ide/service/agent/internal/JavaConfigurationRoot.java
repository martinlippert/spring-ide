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

import org.springframework.beans.factory.annotation.AnnotatedGenericBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.ide.service.agent.BackChannel;
import org.springframework.ide.service.agent.ConfigurationRoot;

/**
 * @author Martin Lippert
 */
public class JavaConfigurationRoot implements ConfigurationRoot {

	private final String configElement;
	private final BackChannel backChannel;
	private final String projectName;
	
	private DefaultListableBeanFactory registry;

	public JavaConfigurationRoot(String configElement, String projectName, BackChannel backChannel) {
		this.configElement = configElement;
		this.projectName = projectName;
		this.backChannel = backChannel;
	}

	@Override
	public void createModel() {
		String rootClassName = configElement.substring(ConfigurationRoot.JAVA_CONFIG_ROOT_PREFIX.length());
		if (rootClassName.length() > 0) {
			try {
//				Class<?> rootClass = this.getClass().getClassLoader().loadClass(rootClassName);
//				SpringApplication application = new SpringApplication(rootClass);
//				application.setApplicationContextClass(ModelBuildingContextClass.class);
//				application.run(new String[] {});
				
				setupModelCreation(rootClassName);
			} catch (Exception e) {
				backChannel.sendException(e);
			}
		}
	}
	
	protected void setupModelCreation(String rootClassName) throws Exception {
		this.registry = new DefaultListableBeanFactory();

		// register annotation processors
		AnnotationConfigUtils.registerAnnotationConfigProcessors(registry);

		// register root bean
		CachingMetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(this.getClass().getClassLoader());
		MetadataReader metadataReader = readerFactory.getMetadataReader(rootClassName);
		
		AnnotatedGenericBeanDefinition beanDefinition = new AnnotatedGenericBeanDefinition(metadataReader.getAnnotationMetadata());
		beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);

		AnnotationBeanNameGenerator nameGenerator = new AnnotationBeanNameGenerator();
		String beanName = nameGenerator.generateBeanName(beanDefinition, this.registry);

		BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, this.registry);
	}
	
}
