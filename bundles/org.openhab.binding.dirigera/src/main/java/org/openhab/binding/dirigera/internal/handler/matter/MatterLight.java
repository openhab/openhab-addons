/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.handler.matter;

import static org.openhab.binding.dirigera.internal.Constants.COLOR_LIGHT_MAP;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dirigera.internal.DirigeraStateDescriptionProvider;
import org.openhab.binding.dirigera.internal.handler.light.ColorLightHandler;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MatterLight} is configured by devices.json
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MatterLight extends ColorLightHandler {
    private final Logger logger = LoggerFactory.getLogger(MatterLight.class);
    // protected BaseMatterConfiguration matterConfig;;

    public MatterLight(Thing thing, Map<String, String> stateChannelMapping,
            DirigeraStateDescriptionProvider stateProvider) {
        super(thing, COLOR_LIGHT_MAP, stateProvider);
        // matterConfig = new BaseMatterConfiguration(this);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Matter Light handler for thing {}", getThing().getUID());
        // JSONArray deviceUpdates = new JSONArray();
        // matterConfig.collectDevices(config.id).forEach(deviceId -> {
        // JSONObject values = gateway().api().readDevice(deviceId);
        // deviceUpdates.put(values);
        // // createChannels(values);
        // });
        super.initialize();
    }
}
