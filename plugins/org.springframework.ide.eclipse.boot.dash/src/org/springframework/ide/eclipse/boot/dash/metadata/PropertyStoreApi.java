/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.metadata;

import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.util.StringUtil;
import org.springsource.ide.eclipse.commons.frameworks.core.util.ArrayEncoder;

/**
 * This is a 'wrapper' for a basic 'IPropertyStore' instance that is used as
 * the 'backing store' to persist metadata.
 * <p>
 * The 'PropertyStoreApi' implements additional convenience methods that encode
 * a variety of data types for storing into the backing store.
 * <p>
 * The idea is that leaves us free to add lots of convenience methods here
 * without making the basic IPropertyStore interface bigger and harder to implement.
 * <p>
 * In other words the responsibility of 'PropertyStoreApi' is 'encoding' and
 * 'decoding' data into a storeable format. But it is not concerned with
 * how/where this encoded data is persisted.
 * <p>
 * IPropertyStore's, on the other hand is concerned with how / where the data
 * is stored but doesn't deal with encoding data (beyond basic key-value
 * pairs).
 *
 * @author Kris De Volder
 */
public class PropertyStoreApi {

	private IPropertyStore backingStore;

	public PropertyStoreApi(IPropertyStore backingStore) {
		this.backingStore = backingStore;
	}

	public String get(String key) {
		return backingStore.get(key);
	}

	public void put(String key, String value) throws Exception {
		backingStore.put(key, value);
	}

	public void put(String key, String[] values) throws Exception {
		if (values==null) {
			backingStore.put(key, null);
		} else {
			backingStore.put(key, ArrayEncoder.encode(values));
		}
	}

	public String[] get(String key, String[] dflt) throws Exception {
		String encoded = backingStore.get(key);
		if (encoded==null) {
			return dflt;
		} else {
			return ArrayEncoder.decode(encoded);
		}
	}

	public boolean get(String key, boolean dflt) {
		String string = get(key);
		if (StringUtil.hasText(string)) {
			try {
				return Boolean.parseBoolean(string);
			} catch (Exception e) {
				BootDashActivator.log(e);
			}
		}
		return dflt;
	}

	public void put(String id, boolean enable) throws Exception {
		put(id, Boolean.toString(enable));
	}

	public IPropertyStore getBackingStore() {
		return backingStore;
	}


}
