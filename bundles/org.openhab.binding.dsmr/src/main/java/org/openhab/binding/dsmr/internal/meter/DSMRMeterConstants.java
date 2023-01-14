/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.dsmr.internal.meter;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Class containing constants that are applicable to the DSMRMeter
 *
 * @author M. Volaart - Initial contribution
 */
@NonNullByDefault
public final class DSMRMeterConstants {

    /**
     * Unknown M-Bus channel
     */
    public static final int UNKNOWN_CHANNEL = -1;

    private DSMRMeterConstants() {
        // Constants class
    }
}
