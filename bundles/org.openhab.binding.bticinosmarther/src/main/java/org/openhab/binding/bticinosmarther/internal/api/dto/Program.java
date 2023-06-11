/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.bticinosmarther.internal.api.dto;

import static org.openhab.binding.bticinosmarther.internal.SmartherBindingConstants.DEFAULT_PROGRAM;

/**
 * The {@code Program} class defines the dto for Smarther API program object.
 *
 * @author Fabio Possieri - Initial contribution
 */
public class Program {

    private int number;
    private String name;

    /**
     * Returns the program number.
     *
     * @return the program number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Returns the program reference label (i.e. the program "name").
     *
     * @return a string containing the program reference label
     */
    public String getName() {
        return (number == 0) ? DEFAULT_PROGRAM : name;
    }

    @Override
    public String toString() {
        return String.format("number=%d, name=%s", number, name);
    }
}
