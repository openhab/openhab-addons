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

/**
 * This class represents the registration options and is used for serialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class ActRegisterOptions {

    /** The registration value */
    private final String value = "yes";

    /** The registration function */
    private final String function = "WOL";

    /**
     * Gets the value of the registration option
     *
     * @return the value of the registration option
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the function of the registration option
     *
     * @return the function of the registration option
     */
    public String getFunction() {
        return function;
    }

    @Override
    public String toString() {
        return "ActRegisterOptions [value=" + value + ", function=" + function + "]";
    }
}
