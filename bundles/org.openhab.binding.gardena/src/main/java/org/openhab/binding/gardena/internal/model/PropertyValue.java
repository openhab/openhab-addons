/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
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
