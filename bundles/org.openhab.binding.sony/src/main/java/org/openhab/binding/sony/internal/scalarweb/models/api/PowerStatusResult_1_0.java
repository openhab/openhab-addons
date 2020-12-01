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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class represents the request for the power status and is used for deserialization/serialization only
 *
 * Version: 1.0
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class PowerStatusResult_1_0 {
    // Various constants for the status
    public static final String ACTIVE = "active";

    // Note - the request is "OFF" but the result is "standby"
    public static final String STANDBY = "standby";

    /** The status */
    private @Nullable String status;

    /**
     * Constructor used for deserialization only
     */
    public PowerStatusResult_1_0() {
    }

    /**
     * Gets the power status
     *
     * @return true if on, false otherwise
     */
    public @Nullable String getStatus() {
        return status;
    }

    public boolean isActive() {
        return StringUtils.equalsIgnoreCase(ACTIVE, status);
    }

    @Override
    public String toString() {
        return "PowerStatus_1_0 [status=" + status + "]";
    }
}
