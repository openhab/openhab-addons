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
package org.openhab.binding.meross.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.meross.internal.MerossBindingConstants;
import org.openhab.binding.meross.internal.config.MerossLightConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Thing;

/**
 * The {@link MerossLightHandler} class is responsible for handling communication with plugs and bulbs
 *
 * @author Giovanni Fabiani - Initial contribution
 * @author Mark Herwege - Extract common methods to abstract base class
 * @author Mark Herwege - Implement refresh
 */
@NonNullByDefault
public class MerossLightHandler extends MerossDeviceHandler {

    public MerossLightHandler(Thing thing, HttpClient httpClient) {
        super(thing, httpClient);
    }

    @Override
    public void initialize() {
        // The following code is to update older light configurations:
        // This code moves from a "lightName" configuration parameter to a "name" configuration parameter
        // It also sets the uuid configuration representation configuration property from the deviceUUID property
        MerossLightConfiguration config = getConfigAs(MerossLightConfiguration.class);
        boolean configChanged = false;
        Configuration configuration = editConfiguration();
        String lightName = config.lightName;
        if (config.name.isEmpty() && (lightName != null)) {
            config.name = lightName;
            configuration.put(MerossBindingConstants.PROPERTY_DEVICE_NAME, config.lightName);
            configuration.put(MerossBindingConstants.PROPERTY_LIGHT_DEVICE_NAME, null);
            configChanged = true;
        }
        String deviceUUID = thing.getProperties().get("deviceUUID");
        if (config.uuid.isEmpty() && deviceUUID != null && !deviceUUID.isEmpty()) {
            config.uuid = deviceUUID;
            configuration.put(MerossBindingConstants.PROPERTY_DEVICE_UUID, deviceUUID);
            updateProperty("deviceUUID", null);
            configChanged = true;
        }
        if (configChanged) {
            updateConfiguration(configuration);
            this.config = config;
        }

        super.initialize();
    }
}
