/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gardena.internal.model;

/**
 * Represents a Gardena property value.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class PropertyValue {
    private String value;

    public PropertyValue() {
    }

    public PropertyValue(String value) {
        this.value = value;
    }

    /**
     * Returns the value of the property.
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the property.
     */
    public void setValue(String value) {
        this.value = value;
    }

}
