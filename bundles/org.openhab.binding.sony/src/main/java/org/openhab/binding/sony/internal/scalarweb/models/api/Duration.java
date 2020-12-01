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
 * This class represents the content duration
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class Duration {
    /** Duration in seconds */
    private @Nullable Integer seconds;

    /** Duration in milliseconds */
    private @Nullable Integer millseconds;

    /**
     * Constructor used for deserialization only
     */
    public Duration() {
    }

    /**
     * Gets the duration in seconds
     * 
     * @return duration in seconds
     */
    public @Nullable Integer getSeconds() {
        return seconds;
    }

    /**
     * Gets duration in milliseconds
     * 
     * @return duration in milliseconds
     */
    public @Nullable Integer getMillseconds() {
        return millseconds;
    }

    @Override
    public String toString() {
        return "Duration [seconds=" + seconds + ", millseconds=" + millseconds + "]";
    }
}
