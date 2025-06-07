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
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@NonNullByDefault
public class Doorbell extends AbstractRingDevice {

    /**
     * Create Doorbell instance from JSON object.
     *
     * @param jsonDoorbell the JSON doorbell (doorbot) retrieved from the Ring API.
     * @param ringAccount the Ring Account in use
     */
    public Doorbell(JsonObject jsonDoorbell, RingAccount ringAccount) {
        super(jsonDoorbell, ringAccount);
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
                .create(new ThingUID("ring:doorbell:" + getRingAccount().getThingId() + ":" + deviceTO.id))
                .withLabel("Ring Video Doorbell - " + deviceTO.description).build();
        return result;
    }
}
