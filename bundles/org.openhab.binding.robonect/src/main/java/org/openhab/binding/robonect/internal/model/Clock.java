/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Clock information returned by the Robonect status API.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public class Clock {

    private @Nullable String unix;

    /**
     * Returns the Unix timestamp in seconds.
     *
     * @return the Unix timestamp in seconds
     */
    public @Nullable String getUnix() {
        return unix;
    }

    /**
     * Sets the Unix timestamp in seconds.
     *
     * @param unix the Unix timestamp in seconds
     */
    public void setUnix(@Nullable String unix) {
        this.unix = unix;
    }
}
