/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo.internal.models;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The model representing an item value (serialize/deserialize json use only).
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
public class NeeoItemValue {

    /** The value */
    private final Object value;

    /**
     * Instantiates a new neeo item value.
     *
     * @param value the possibly null value
     */
    public NeeoItemValue(Object value) {
        this.value = value;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "NeeoItemValue [value=" + value + "]";
    }
}
