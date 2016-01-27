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
package org.springframework.ide.eclipse.boot.dash.test.yaml;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.DeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.YamlGraphDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.test.AllBootDashTests;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;

/**
 * Manifest YAML file and Deployment properties compare and merge tests.
 * 
 * @author Alex Boyko
 *
 */
public class ManifestCompareMergeTests {
	
	private static final List<CloudDomain> SPRING_CLOUD_DOMAINS = Collections.singletonList(new CloudDomain(null, "springsource.org", null));
	
	private static void peroformMergeTest(File manifest, DeploymentProperties props, File expected) throws Exception {
		FileInputStream manifestStream = null, expectedStream = null;
		try {
			String yamlContents = IOUtil.toString(manifestStream = new FileInputStream(manifest));
			YamlGraphDeploymentProperties yamlGraphProps = new YamlGraphDeploymentProperties(yamlContents, props.getAppName(), SPRING_CLOUD_DOMAINS);
			TextEdit edit = yamlGraphProps.getDifferences(props);
			Document doc = new Document(yamlContents);
			edit.apply(doc);
			assertEquals(IOUtil.toString(expectedStream = new FileInputStream(expected)), doc.get());
		} finally {
			if (manifestStream != null) {
				manifestStream.close();
			}
			if (expectedStream != null) {
				expectedStream.close();
			}
		}
	}
	
	public static File getTestFile(String path) throws IOException {
		Bundle bundle = Platform.getBundle(AllBootDashTests.PLUGIN_ID);
		File bundleFile = FileLocator.getBundleFile(bundle);
		Assert.assertNotNull(bundleFile);
		Assert.assertTrue("The bundle "+bundle.getBundleId()+" must be unpacked to allow using the embedded test resources", bundleFile.isDirectory());
		return new File(bundleFile, path);
	}

	
	@Test
	public void test_memory_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		peroformMergeTest(getTestFile("mergeTestsData/memory-1.yml"), props, getTestFile("mergeTestsData/memory-1-expected.yml"));
	}

	@Test
	public void test_memory_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		peroformMergeTest(getTestFile("mergeTestsData/memory-2.yml"), props, getTestFile("mergeTestsData/memory-2-expected.yml"));
	}

	@Test
	public void test_memory_3() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		peroformMergeTest(getTestFile("mergeTestsData/memory-3.yml"), props, getTestFile("mergeTestsData/memory-3-expected.yml"));
	}

	@Test
	public void test_appNameNoMatch_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app1");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		peroformMergeTest(getTestFile("mergeTestsData/appNameNoMatch-1.yml"), props, getTestFile("mergeTestsData/appNameNoMatch-1-expected.yml"));
	}

	@Test
	public void test_appNameNoMatch_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app1");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		peroformMergeTest(getTestFile("mergeTestsData/appNameNoMatch-2.yml"), props, getTestFile("mergeTestsData/appNameNoMatch-2-expected.yml"));
	}

	@Test
	public void test_noAppsNode_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		peroformMergeTest(getTestFile("mergeTestsData/noAppsNode-1.yml"), props, getTestFile("mergeTestsData/noAppsNode-1-expected.yml"));
	}

	@Test
	public void test_noAppsNode_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app1");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		peroformMergeTest(getTestFile("mergeTestsData/noAppsNode-2.yml"), props, getTestFile("mergeTestsData/noAppsNode-2-expected.yml"));
	}

	/**
	 * TODO: uncomment when URIs work is completed on YAML Graph deploy props 
	 */
//	@Test
//	public void test_noManifest_1() throws Exception {
//		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
//		props.setAppName("app");
//		props.setUris(Collections.singletonList("test-app.springsource.org"));
//		props.setMemory(2048);
//		peroformMergeTest(getTestFile("mergeTestsData/noManifest-1.yml"), props, getTestFile("mergeTestsData/noManifest-1-expected.yml"));
//	}

	@Test
	public void test_noManifest_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		peroformMergeTest(getTestFile("mergeTestsData/noManifest-2.yml"), props, getTestFile("mergeTestsData/noManifest-2-expected.yml"));
	}

	@Test
	public void test_map_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		Map<String, String> env = new LinkedHashMap<>();
		env.put("KEY1", "value1");
		env.put("KEY2", "value2");
		env.put("KEY3", "value3");
		props.setEnvironmentVariables(env);
		peroformMergeTest(getTestFile("mergeTestsData/map-1.yml"), props, getTestFile("mergeTestsData/map-1-expected.yml"));
	}

	@Test
	public void test_map_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		Map<String, String> env = new LinkedHashMap<>();
		env.put("KEY1", "value1");
		env.put("KEY2", "value2");
		env.put("KEY3", "value3");
		props.setEnvironmentVariables(env);
		peroformMergeTest(getTestFile("mergeTestsData/map-2.yml"), props, getTestFile("mergeTestsData/map-2-expected.yml"));
	}
	
	@Test
	public void test_map_3() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		peroformMergeTest(getTestFile("mergeTestsData/map-3.yml"), props, getTestFile("mergeTestsData/map-3-expected.yml"));
	}

	@Test
	public void test_map_4() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		peroformMergeTest(getTestFile("mergeTestsData/map-4.yml"), props, getTestFile("mergeTestsData/map-4-expected.yml"));
	}

	@Test
	public void test_map_5() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		Map<String, String> env = new LinkedHashMap<>();
		env.put("KEY1", "value1");
		env.put("KEY2", "value2");
		env.put("KEY3", "value3");
		env.put("KEY4", "value4");
		props.setEnvironmentVariables(env);
		peroformMergeTest(getTestFile("mergeTestsData/map-5.yml"), props, getTestFile("mergeTestsData/map-5-expected.yml"));
	}

	@Test
	public void test_map_6() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		Map<String, String> env = new LinkedHashMap<>();
		env.put("KEY1", "value1");
		env.put("KEY2", "value2");
		env.put("KEY3", "value3");
		env.put("KEY4", "value4");
		props.setEnvironmentVariables(env);
		peroformMergeTest(getTestFile("mergeTestsData/map-6.yml"), props, getTestFile("mergeTestsData/map-6-expected.yml"));
	}

	@Test
	public void test_map_7() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		Map<String, String> env = new LinkedHashMap<>();
		env.put("KEY1", "value1");
		env.put("KEY2", "value2");
		env.put("KEY3", "value3");
		env.put("KEY4", "value4");
		props.setEnvironmentVariables(env);
		peroformMergeTest(getTestFile("mergeTestsData/map-7.yml"), props, getTestFile("mergeTestsData/map-7-expected.yml"));
	}

	@Test
	public void test_map_8() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		peroformMergeTest(getTestFile("mergeTestsData/map-8.yml"), props, getTestFile("mergeTestsData/map-8-expected.yml"));
	}

	@Test
	public void test_map_9() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		Map<String, String> env = new LinkedHashMap<>();
		env.put("KEY1", "value1");
		env.put("KEY2", "value2");
		env.put("KEY3", "value3");
		env.put("KEY4", "value4");
		props.setEnvironmentVariables(env);
		peroformMergeTest(getTestFile("mergeTestsData/map-9.yml"), props, getTestFile("mergeTestsData/map-9-expected.yml"));
	}

	@Test
	public void test_map_10() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		Map<String, String> env = new LinkedHashMap<>();
		env.put("KEY1", "value1");
		env.put("KEY2", "value2");
		env.put("KEY3", "value3");
		env.put("KEY4", "value4");
		props.setEnvironmentVariables(env);
		peroformMergeTest(getTestFile("mergeTestsData/map-10.yml"), props, getTestFile("mergeTestsData/map-10-expected.yml"));
	}

	@Test
	public void test_instances_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		peroformMergeTest(getTestFile("mergeTestsData/instances-1.yml"), props, getTestFile("mergeTestsData/instances-1-expected.yml"));
	}

	@Test
	public void test_instances_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		peroformMergeTest(getTestFile("mergeTestsData/instances-2.yml"), props, getTestFile("mergeTestsData/instances-2-expected.yml"));
	}

	@Test
	public void test_instances_3() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		peroformMergeTest(getTestFile("mergeTestsData/instances-3.yml"), props, getTestFile("mergeTestsData/instances-3-expected.yml"));
	}

	@Test
	public void test_instances_4() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(2048);
		peroformMergeTest(getTestFile("mergeTestsData/instances-4.yml"), props, getTestFile("mergeTestsData/instances-4-expected.yml"));
	}

	@Test
	public void test_root_comment_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(512);
		peroformMergeTest(getTestFile("mergeTestsData/root-comment-1.yml"), props, getTestFile("mergeTestsData/root-comment-1-expected.yml"));
	}

	@Test
	public void test_root_comment_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(512);
		peroformMergeTest(getTestFile("mergeTestsData/root-comment-2.yml"), props, getTestFile("mergeTestsData/root-comment-2-expected.yml"));
	}

	@Test
	public void test_root_comment_3() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(512);
		props.setInstances(2);
		peroformMergeTest(getTestFile("mergeTestsData/root-comment-3.yml"), props, getTestFile("mergeTestsData/root-comment-3-expected.yml"));
	}

	@Test
	public void test_root_comment_4() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setUris(Collections.singletonList("test-app.springsource.org"));
		props.setMemory(512);
		peroformMergeTest(getTestFile("mergeTestsData/root-comment-4.yml"), props, getTestFile("mergeTestsData/root-comment-4-expected.yml"));
	}
}

