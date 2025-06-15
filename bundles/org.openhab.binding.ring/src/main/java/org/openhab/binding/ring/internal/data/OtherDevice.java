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
package org.openhab.binding.ring.internal.data;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class OtherDevice extends AbstractRingDevice {

    /**
     * Create OtherDevice instance from JSON object.
     *
     * @param deviceTO the JSON Other retrieved from the Ring API.
     */
    public OtherDevice(RingDeviceTO deviceTO) {
        super(deviceTO);
    }
}
