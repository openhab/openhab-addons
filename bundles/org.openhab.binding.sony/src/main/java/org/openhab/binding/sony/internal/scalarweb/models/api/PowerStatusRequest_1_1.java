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
 * This class represents the request for the power status and is used for deserialization/serialization only
 *
 * Version: 1.0
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class PowerStatusRequest_1_1 {

    // Various constants for the status
    public static final String ACTIVE = "active";
    public static final String OFF = "off";

    /** The status */
    private final String status;

    /** The standby detail */
    private final @Nullable String standbyDetail;

    /**
     * Instantiates a new power status.
     *
     * @param status the status
     */
    public PowerStatusRequest_1_1(final boolean status) {
        this.status = status ? ACTIVE : OFF;
        standbyDetail = null;
    }

    @Override
    public String toString() {
        return "PowerStatus_1_1 [status=" + status + ", standByDetail=" + standbyDetail + "]";
    }
}
