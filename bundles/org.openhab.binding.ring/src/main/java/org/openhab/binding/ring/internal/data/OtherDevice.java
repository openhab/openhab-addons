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
import org.openhab.binding.ring.internal.RingAccount;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;

import com.google.gson.JsonObject;

/**
 * @author Ben Rosenblum - Initial contribution
 */
@NonNullByDefault
public class OtherDevice extends AbstractRingDevice {

    /**
     * Create OtherDevice instance from JSON object.
     *
     * @param jsonOtherDevice the JSON Other retrieved from the Ring API.
     * @param ringAccount the Ring Account in use
     */
    public OtherDevice(JsonObject jsonOtherDevice, RingAccount ringAccount) {
        super(jsonOtherDevice, ringAccount);
    }

    /**
     * Get the DiscoveryResult object to identify the device as
     * discovered thing.
     *
     * @return the device as DiscoveryResult instance.
     */
    @Override
    public DiscoveryResult getDiscoveryResult(RingDeviceTO deviceTO) {
        DiscoveryResult result = DiscoveryResultBuilder
                .create(new ThingUID("ring:otherdevice:" + getRingAccount().getThingId() + ":" + deviceTO.id))
                .withLabel("Ring Other Device - " + deviceTO.description).build();
        return result;
    }
}
