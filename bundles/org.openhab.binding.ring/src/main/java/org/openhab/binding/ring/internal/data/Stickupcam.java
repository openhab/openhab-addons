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
import org.json.simple.JSONObject;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingUID;

/**
 * @author Chris Milbert - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@NonNullByDefault
public class Stickupcam extends AbstractRingDevice {

    /**
     * Create Stickup Cam instance from JSON object.
     *
     * @param jsonStickupCam the JSON Stickup Cam retrieved from the Ring API.
     */
    public Stickupcam(JSONObject jsonStickupcam) {
        super(jsonStickupcam);
    }

    /**
     * Get the DiscoveryResult object to identify the device as
     * discovered thing.
     *
     * @return the device as DiscoveryResult instance.
     */
    @Override
    public DiscoveryResult getDiscoveryResult() {
        DiscoveryResult result = DiscoveryResultBuilder
                .create(new ThingUID("ring:stickupcam:" + getRingAccount().getThingId() + ":" + getId()))
                .withLabel("Ring Video Stickup Cam - " + getDescription()).build();
        return result;
    }
}
