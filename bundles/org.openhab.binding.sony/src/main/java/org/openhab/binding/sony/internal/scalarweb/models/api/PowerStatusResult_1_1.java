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
 * Version: 1.1
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class PowerStatusResult_1_1 extends PowerStatusResult_1_0 {

    /** The standby detail */
    private @Nullable String standByDetail;

    /**
     * Constructor used for deserialization only
     */
    public PowerStatusResult_1_1() {
    }

    /**
     * Gets the power status
     *
     * @return true if on, false otherwise
     */
    public @Nullable String getStandByDetail() {
        return standByDetail;
    }

    @Override
    public String toString() {
        return "PowerStatus_1_1 [status=" + getStatus() + ", standByDetail=" + standByDetail + "]";
    }
}
