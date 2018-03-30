/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.innogysmarthome.internal.client.entity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a list of {@link Property}s and commonly used methods.
 *
 * @author Oliver Kuhl - Initial contribution
 */
public abstract class PropertyList {

    /**
     * Returns a {@link Map} of the {@link Property}s. Must be overwritten!
     *
     * @return {@link Map} of {@link Property}s
     */
    protected abstract Map<String, Property> getPropertyMap();

    /**
     * Returns a {@link List} of the {@link Property}s. Must be overwritten!
     *
     * @return {@link List} of {@link Property}s
     */
    protected abstract List<Property> getPropertyList();

    /**
     * Adds a {@link Property} to the {@link #PropertyList()} and corresponding {@link #getPropertyMap()}. Make sure
     * the element is new first - this is not checked!
     *
     * @param p
     */
    private void addProperty(Property p) {
        getPropertyList().add(p);
        getPropertyMap().put(p.getName(), p);
    }

    /**
     * Returns the value of the {@link Property} with the given name.
     *
     * @param propertyName
     * @return
     */
    protected Object getPropertyValue(String propertyName) {
        return getPropertyMap().get(propertyName);
    }

    /**
     * Returns the value of the {@link Property} with the given name as {@link String}.
     *
     * @param propertyName
     * @return
     */
    protected String getPropertyValueAsString(String propertyName) {
        Property p = getPropertyMap().get(propertyName);
        if (p != null) {
            Object value = p.getValue();
            if (value instanceof String) {
                return (String) value;
            }
        }
        return null;
    }

    /**
     * Sets the value of the {@link Property} with the given name to the stringState.
     *
     * @param propertyName String the name of the {@link Property}
     * @param stringValue String the new value to set
     */
    protected void setPropertyValueAsString(String propertyName, String stringValue) {
        Property p = getPropertyMap().get(propertyName);
        if (p != null) {
            p.setValue(stringValue);
        } else {
            addProperty(new Property(propertyName, stringValue));
        }
    }

    /**
     * Returns the value of the {@link Property} with the given name as {@link Boolean}.
     *
     * @param propertyName
     * @return
     */
    protected Boolean getPropertyValueAsBoolean(String propertyName) {
        Property p = getPropertyMap().get(propertyName);
        if (p != null) {
            Object value = p.getValue();
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
        }
        return null;
    }

    /**
     * Sets the value of the {@link Property} with the given name to the booleanState.
     *
     * @param propertyName String the name of the {@link Property}
     * @param booleanValue boolean the new value to set
     */
    protected void setPropertyValueAsBoolean(String propertyName, boolean booleanValue) {
        Property p = getPropertyMap().get(propertyName);
        if (p != null) {
            p.setValue(booleanValue);
        } else {
            addProperty(new Property(propertyName, booleanValue));
        }
    }

    /**
     * Returns the value of the {@link Property} with the given name as {@link Double}.
     *
     * @param propertyName
     * @return
     */
    protected Double getPropertyValueAsDouble(String propertyName) {
        Property p = getPropertyMap().get(propertyName);
        if (p != null) {
            Object value = p.getValue();
            // return (Double) value;
            return Double.parseDouble(value.toString());
        }
        return null;
    }

    /**
     * Sets the value of the {@link Property} with the given name to the doubleState.
     *
     * @param propertyName String the name of the {@link Property}
     * @param doubleValue double the new value to set
     */
    protected void setPropertyValueAsDouble(String propertyName, double doubleValue) {
        Property p = getPropertyMap().get(propertyName);
        if (p != null) {
            p.setValue(doubleValue);
        } else {
            addProperty(new Property(propertyName, doubleValue));
        }
    }

    /**
     * Returns the value of the {@link Property} with the given name as {@link Integer}.
     *
     * @param propertyName
     * @return
     */
    protected Integer getPropertyValueAsInteger(String propertyName) {
        Property p = getPropertyMap().get(propertyName);
        if (p != null) {
            Object value = p.getValue();
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof Double) {
                Double doubleValue = (Double) value;
                return doubleValue.intValue();
            } else if (value instanceof String) {
                return Integer.parseInt(value.toString());
            }
        }
        return null;
    }

    /**
     * Sets the value of the {@link Property} with the given name to the intState.
     *
     * @param propertyName String the name of the {@link Property}
     * @param intValue int the new value to set
     */
    protected void setPropertyValueAsInteger(String propertyName, int intValue) {
        Property p = getPropertyMap().get(propertyName);
        if (p != null) {
            p.setValue(intValue);
        } else {
            addProperty(new Property(propertyName, intValue));
        }
    }

    /**
     * Returns a {@link HashMap} with the name as key and {@link Property} as value.
     *
     * @param propertyList
     * @return
     */
    public static HashMap<String, Property> getHashMap(List<Property> propertyList) {
        HashMap<String, Property> map = new HashMap<>();
        for (Property p : propertyList) {
            map.put(p.getName(), p);
        }
        return map;
    }

}
