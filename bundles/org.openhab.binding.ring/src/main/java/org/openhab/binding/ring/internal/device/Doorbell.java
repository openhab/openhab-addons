/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal.device;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.ring.internal.api.RingDeviceTO;

/**
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@NonNullByDefault
public class Doorbell extends AbstractRingDevice {
    /**
     * Create Doorbell instance from JSON object.
     *
     * @param deviceTO the JSON doorbell (doorbot) retrieved from the Ring API.
     */
    public Doorbell(RingDeviceTO deviceTO) {
        super(deviceTO);
    }
}
