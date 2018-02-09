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
 * Represents a simple Gardena property.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class SimpleProperties {

    private String name;
    private String value;

    public SimpleProperties(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the property name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the property value.
     */
    public String getValue() {
        return value;
    }

}
