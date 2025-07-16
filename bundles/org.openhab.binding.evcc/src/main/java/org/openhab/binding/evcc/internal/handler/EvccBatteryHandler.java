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
package org.openhab.binding.evcc.internal.handler;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.type.ChannelTypeRegistry;

import com.google.gson.JsonObject;

/**
 * The {@link EvccBatteryHandler} is responsible for fetching the data from the API response for Battery things
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public class EvccBatteryHandler extends EvccBaseThingHandler {

    private final int index;

    public EvccBatteryHandler(Thing thing, ChannelTypeRegistry channelTypeRegistry) {
        super(thing, channelTypeRegistry);
        Map<String, String> props = thing.getProperties();
        String indexString = props.getOrDefault("index", "0");
        index = Integer.parseInt(indexString);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (null == bridgeHandler) {
            return;
        }
        // endpoint = bridgeHandler.getBaseURL(); // Currenlty there is no endpoint
        JsonObject stateOpt = bridgeHandler.getCachedEvccState();
        if (stateOpt.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }

        JsonObject state = stateOpt.getAsJsonArray("battery").get(index).getAsJsonObject();
        commonInitialize(state);
    }

    @Override
    public void updateFromEvccState(JsonObject root) {
        root = root.getAsJsonArray("battery").get(index).getAsJsonObject();
        super.updateFromEvccState(root);
    }
}
