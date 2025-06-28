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
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class Chime extends AbstractRingDevice {

    /**
     * Create Chime instance from JSON object.
     *
     * @param deviceTO the JSON Chime retrieved from the Ring API.
     */
    public Chime(RingDeviceTO deviceTO) {
        super(deviceTO);
    }
}
