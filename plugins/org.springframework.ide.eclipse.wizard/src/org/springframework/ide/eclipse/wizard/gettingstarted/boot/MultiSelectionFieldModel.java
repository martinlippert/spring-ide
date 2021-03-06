/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.gettingstarted.boot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.springframework.ide.eclipse.wizard.gettingstarted.boot.CheckBoxesSection.CheckBoxModel;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.core.ValidationResult;
import org.springsource.ide.eclipse.commons.livexp.core.Validator;

/**
 * Model for a UI widget that offers multiple choices. Could be represented
 * by a set of Checkboxes or multi-selection enabled list/tree viewer.
 *
 * @author Kris De Volder
 */
public class MultiSelectionFieldModel<T> {

	private static final LiveExpression<Boolean> ALLWAYS_ENABLED = LiveExpression.constant(true);

	private Class<T> type; //Type of data stored in the field.
	private String name; // used to submit value to some service that handles the form
	private String label; // Label to display in forms
	private LiveExpression<ValidationResult> validator;

	private Map<T,String> labelMap = new LinkedHashMap<T, String>();
	private Map<T,String> tooltips = null; //don't allocate unless used.
	private boolean mustSort;

	private Map<T, LiveExpression<Boolean>> enablements = null;

	//selection state for each individual choice (created on an as-needed basis).
	private Map<T, LiveVariable<Boolean>> selections = new HashMap<T, LiveVariable<Boolean>>();

	public MultiSelectionFieldModel(Class<T> type, String name) {
		this.type = type;
		this.name  = name;
		this.label = name;
		this.validator = Validator.OK;
	}

	public Class<T> getType() {
		return type;
	}

	public String getLabel() {
		return label;
	}

	public String getLabel(T value) {
		return labelMap.get(value);
	}

	public String getName() {
		return name;
	}

	public LiveExpression<ValidationResult> getValidator() {
		return validator;
	}

	public MultiSelectionFieldModel<T> validator(LiveExpression<ValidationResult> v) {
		this.validator = v;
		return this;
	}

	public void select(T v) {
		getSelection(v).setValue(true);
	}

	public synchronized LiveVariable<Boolean> getSelection(T v) {
		LiveVariable<Boolean> existing = selections.get(v);
		if (existing==null) {
			selections.put(v, existing = new LiveVariable<Boolean>(false));
		}
		return existing;
	}

	public void unselect(T v) {
		getSelection(v).setValue(false);
	}

	/**
	 * Add a valid choice to the multi selection model.
	 * @param label String to show the choice to a user
	 * @param key  Value added to the set when user selects this choice.
	 */
	public MultiSelectionFieldModel<T> choice(String label, T value) {
		Assert.isLegal(labelMap.get(value)==null, "Duplicate choice "+value+" already added");
		labelMap.put(value, label);
		return this;
	}

	public void choice(String label, T value, String tooltipText) {
		choice(label, value);
		setTooltip(value, tooltipText);
	}

	public void choice(String label, T value, String tooltipText, LiveExpression<Boolean> enablement) {
		choice(label, value, tooltipText);
		if (enablements==null) {
			enablements = new HashMap<T, LiveExpression<Boolean>>();
		}
		enablements.put(value, enablement);
	}

	public void setTooltip(T value, String tooltipText) {
		if (tooltips==null) {
			tooltips = new HashMap<T, String>();
		}
		tooltips.put(value,  tooltipText);
	}

	public String getTooltip(T value) {
		if (tooltips!=null) {
			return tooltips.get(value);
		}
		return null;
	}

	public MultiSelectionFieldModel<T> label(String label) {
		this.label = label;
		return this;
	}

	@SuppressWarnings("unchecked")
	public synchronized T[] getChoices() {
		Collection<T> values = labelMap.keySet();
		T[] choices = values.toArray((T[]) Array.newInstance(getType(), values.size()));
		if (mustSort) {
			Arrays.sort(choices, new Comparator<T>() {
				public int compare(T o1, T o2) {
					String s1 = getLabel(o1);
					String s2 = getLabel(o2);
					return s1.compareTo(s2);
				}
			});
		}
		return choices;
	}

	public void sort() {
		mustSort = true;
	}

	public LiveExpression<Boolean> getEnablement(T choice) {
		if (enablements!=null) {
			LiveExpression<Boolean> e = enablements.get(choice);
			if (e!=null) {
				return e;
			}
		}
		return ALLWAYS_ENABLED;
	}

	public synchronized List<T> getCurrentSelection() {
		List<T> selecteds = new ArrayList<T>();
		for (Entry<T, LiveVariable<Boolean>> e : selections.entrySet()) {
			if (e.getValue().getValue()) {
				selecteds.add(e.getKey());
			}
		}
		return Collections.unmodifiableList(selecteds);
	}

	/**
	 * Converts all the current choices into CheckBoxModel objects. One checkbox to
	 * represent each choice.
	 */
	public List<CheckBoxModel<T>> getCheckBoxModels() {
		List<CheckBoxModel<T>> checkboxes = new ArrayList<CheckBoxModel<T>>(labelMap.size());
		for (T choice : labelMap.keySet()) {
			CheckBoxModel<T> cb;
			checkboxes.add(cb = new CheckBoxModel<T>(getLabel(choice), choice, getSelection(choice), getEnablement(choice)));
			cb.setTooltip(getTooltip(choice));
		}
		return Collections.unmodifiableList(checkboxes);
	}

}
