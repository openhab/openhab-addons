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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interface common to all Ring devices.
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@NonNullByDefault
public abstract class AbstractRingDevice implements RingDevice {
    private final Logger logger = LoggerFactory.getLogger(AbstractRingDevice.class);
    private RingDeviceTO deviceStatus;

    protected AbstractRingDevice(RingDeviceTO jsonObject) {
        this.deviceStatus = jsonObject;
    }

    @Override
    public void setDeviceStatus(RingDeviceTO ringDeviceTO) {
        this.deviceStatus = ringDeviceTO;
        logger.trace("AbstractRingDevice - setJsonObject - Updated JSON: {}", ringDeviceTO);
    }

    @Override
    public RingDeviceTO getDeviceStatus() {
        return deviceStatus;
    }
}
