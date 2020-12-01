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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class represents the output and is used in serialization to specify the output uri in a call
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class Output {
    /** The output */
    private final String output;

    /**
     * Constructs the output using the default output
     */
    public Output() {
        this.output = "";
    }

    /**
     * Constructs the output from the parameter
     * 
     * @param output a non-null, can be empty output (empty meaning default)
     */
    public Output(final String output) {
        Objects.requireNonNull(output, "output cannot be null");
        this.output = output;
    }

    /**
     * Gets the output
     * 
     * @return the output
     */
    public String getOutput() {
        return output;
    }
}
