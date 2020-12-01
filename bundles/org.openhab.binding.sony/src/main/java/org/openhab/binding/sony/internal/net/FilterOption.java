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
package org.openhab.binding.sony.internal.net;

import java.util.Objects;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The class provides a key/value option for a filter
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class FilterOption {
    /** Key for the filter option */
    private final String key;

    /** Value of the filter option */
    private final Object value;

    /**
     * Constructs the option from the key/value
     * 
     * @param key a non-null, non-empty key
     * @param value a non-null value
     */
    public FilterOption(final String key, final Object value) {
        Validate.notEmpty(key, "key cannot be empty");
        Objects.requireNonNull(value, "value cannot be null");

        this.key = key;
        this.value = value;
    }

    /**
     * Returns the key for the filter option
     * 
     * @return a non-null, non-empty key
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the value for the filter option
     * 
     * @return a non-null value
     */
    public Object getValue() {
        return value;
    }
}
