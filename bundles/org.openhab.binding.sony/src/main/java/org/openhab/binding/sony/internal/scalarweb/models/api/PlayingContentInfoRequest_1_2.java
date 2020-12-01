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
 * This class represents the request to play content and is used for serialization only
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class PlayingContentInfoRequest_1_2 {

    /** The output of the content */
    private final String output;

    /**
     * Instantiates a new play content request
     *
     * @param output the non-null, possibly empty empty output
     */
    public PlayingContentInfoRequest_1_2(final String output) {
        Objects.requireNonNull(output, "output cannot be empty");
        this.output = output;
    }

    /**
     * Gets the output of the content
     *
     * @return the output of the content
     */
    public String getOutput() {
        return output;
    }

    @Override
    public String toString() {
        return "PlayContent_1_2 [output=" + output + "]";
    }
}
