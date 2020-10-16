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
package org.openhab.binding.gardena.internal.model.property;

/**
 * Represents a String Gardena property.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class StringProperty extends BaseProperty {
    private String value;

    public StringProperty(String name, String value) {
        super(name);
        this.value = value;
    }

    /**
     * Returns the property value.
     */
    @Override
    public String getValue() {
        return value;
    }
}
