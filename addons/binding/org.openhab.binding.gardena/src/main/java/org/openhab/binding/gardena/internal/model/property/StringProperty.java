/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
