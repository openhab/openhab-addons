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
package org.openhab.binding.sony.internal.scalarweb.models.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the system functions supported and is used for deserialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SystemSupportedFunction {
    /** The function */
    private @Nullable String option;

    /** The value */
    private @Nullable String value;

    /**
     * Constructor used for deserialization only
     */
    public SystemSupportedFunction() {
    }

    /**
     * Gets the function
     *
     * @return the function
     */
    public @Nullable String getOption() {
        return option;
    }

    /**
     * Gets the value
     *
     * @return the value
     */
    public @Nullable String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "SystemSupportedFunction [option=" + option + ", value=" + value + "]";
    }
}
