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
 * The state information class used for deserialization only. Please note that sony broke this in their API and all
 * states are stopped
 * 
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class StateInfo {
    /** The static field for the Stopped state */
    public static final String STOPPED = "stopped";

    /** The current state */
    private @Nullable String state;

    /** The current state supplemental information */
    private @Nullable String supplement;

    /**
     * Constructor used for deserialization only
     */
    public StateInfo() {
    }

    /**
     * Returns the current state
     * 
     * @return the current state
     */
    public @Nullable String getState() {
        return state;
    }

    /**
     * Returns the current state supplemental information
     * 
     * @return the current state supplemental information
     */
    public @Nullable String getSupplement() {
        return supplement;
    }

    @Override
    public String toString() {
        return "StateInfo [state=" + state + ", supplement=" + supplement + "]";
    }
}
