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
package org.springframework.ide.eclipse.boot.properties.editor.metadata;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.springframework.boot.configurationmetadata.ValueHint;
import org.springframework.ide.eclipse.boot.properties.editor.metadata.ValueProviderRegistry.ValueProviderStrategy;
import org.springframework.ide.eclipse.boot.properties.editor.util.Type;
import org.springframework.ide.eclipse.boot.properties.editor.util.TypedProperty;
import org.springframework.ide.eclipse.editor.support.util.CollectionUtil;
import org.springframework.ide.eclipse.editor.support.yaml.path.YamlPathSegment;

import com.google.common.collect.ImmutableList;

/**
 * Methods for creating hints providers that provide hint in specific kind of context.
 *
 * @author Kris De Volder
 */
public class HintProviders {

	/**
	 * HintProvider that never returns any hints. This should be used
	 * instead of null pointer.
	 */
	public static final HintProvider NULL = new HintProvider() {

		@Override
		public HintProvider traverse(YamlPathSegment s) throws Exception {
			return NULL;
		}

		@Override
		public List<ValueHint> getValueHints(String query) {
			return ImmutableList.of();
		}

		@Override
		public List<TypedProperty> getPropertyHints(String query) {
			return ImmutableList.of();
		}
	};

	/**
	 * Creates a non-context-aware hint provider. Typically a hint provider is created by composing the result of
	 * this with one or more of the other methods in this class to wrap the basic provider so it becomes context-aware.
	 */
	public static HintProvider basic(IJavaProject jp, final ImmutableList<ValueHint> valueHints, final ValueProviderStrategy valueProvider) {
		if (!CollectionUtil.hasElements(valueHints) && valueProvider==null) {
			return NULL;
		}
		return new BasicHintProvider(jp, valueHints, valueProvider);
	}

	/**
	 * Create a hint provider that will return the given hints in the context following
	 * a traversal that goes down into a 'domain of' context a given number of times.
	 */
	public static HintProvider forDomainAt(final HintProvider valueHints, final int dim) {
		if (isNull(valueHints)) {
			return NULL;
		}
		if (dim==0) {
			return forHere(valueHints);
		}
		return new HintProvider() {
			public HintProvider traverse(YamlPathSegment s) throws Exception {
				switch (s.getType()) {
				case VAL_AT_INDEX:
				case VAL_AT_KEY:
					return forDomainAt(valueHints, dim-1);
				default:
					return NULL;
				}
			}

			public List<ValueHint> getValueHints(String query) {
				return ImmutableList.of();
			}

			@Override
			public List<TypedProperty> getPropertyHints(String query) {
				return ImmutableList.of();
			}
		};
	}

	/**
	 * Only returns the given hints in this context but not one of its 'sub contexts'.
	 */
	public static HintProvider forHere(final HintProvider valueHints) {
		if (isNull(valueHints)) {
			return NULL;
		}
		return new HintProvider() {

			@Override
			public HintProvider traverse(YamlPathSegment s) throws Exception {
				return NULL;
			}

			@Override
			public List<ValueHint> getValueHints(String query) {
				return valueHints.getValueHints(query);
			}

			@Override
			public List<TypedProperty> getPropertyHints(String query) {
				return ImmutableList.of();
			}
		};
	}

	/**
	 * REturns the given hints in this context and any of its subcontexts that expect values.
	 */
	public static HintProvider forAllValueContexts(final HintProvider valueProvider) {
		if (isNull(valueProvider)) {
			return NULL;
		}
		return new HintProvider() {
			@Override
			public HintProvider traverse(YamlPathSegment s) throws Exception {
				switch (s.getType()) {
				case VAL_AT_INDEX:
				case VAL_AT_KEY:
					return this;
				default:
					return NULL;
				}
			}

			@Override
			public List<ValueHint> getValueHints(String query) {
				return valueProvider.getValueHints(query);
			}

			@Override
			public List<TypedProperty> getPropertyHints(String query) {
				return ImmutableList.of();
			}
		};
	}

	public static boolean isNull(HintProvider p) {
		//If everyone is nice and doesn't ever use null pointers then the p==null check is
		// not needed. But just in case.
		return p == NULL || p==null;
	}

	public static HintProvider forMap(HintProvider _keyProvider, HintProvider _valueProvider, final Type valueType, final boolean dimensionAware) {
		final HintProvider keyProvider = notNull(_keyProvider);
		final HintProvider valueProvider = notNull(_valueProvider);
		if (isNull(keyProvider) && isNull(valueProvider)) {
			return NULL;
		}
		return new HintProvider() {

			@Override
			public HintProvider traverse(YamlPathSegment s) throws Exception {
				switch (s.getType()) {
				case VAL_AT_INDEX:
				case VAL_AT_KEY:
					if (dimensionAware) {
						return forHere(valueProvider);
					} else {
						return forAllValueContexts(valueProvider);
					}
				default:
					return NULL;
				}
			}

			@Override
			public List<ValueHint> getValueHints(String query) {
				if (dimensionAware) {
					//pickier, completions only suggested in the domain of map, but not for map itself.
					return ImmutableList.of();
				} else {
					return valueProvider.getValueHints(query);
				}
			}

			@Override
			public List<TypedProperty> getPropertyHints(String query) {
				List<ValueHint> keyHints = keyProvider.getValueHints(query);
				if (CollectionUtil.hasElements(keyHints)) {
					List<TypedProperty> props = new ArrayList<>(keyHints.size());
					for (ValueHint keyHint : keyHints) {
						Object key = keyHint.getValue();
						if (key instanceof String) {
							props.add(new TypedProperty((String)key, valueType, null));
						}
					}
					return props;
				}
				return ImmutableList.of();
			}
		};
	}

	/**
	 * Protection against bad code passing us null pointers.
	 */
	private static HintProvider notNull(HintProvider p) {
		if (p==null) {
			return NULL;
		}
		return p;
	}
}
