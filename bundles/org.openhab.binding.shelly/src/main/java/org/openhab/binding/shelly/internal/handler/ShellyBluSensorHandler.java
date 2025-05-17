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
package org.openhab.binding.shelly.internal.handler;

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.CONFIG_DEVICEADDRESS;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.PROPERTY_DEV_GEN;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.PROPERTY_DEV_NAME;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.PROPERTY_DEV_TYPE;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.PROPERTY_GW_DEVICE;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.PROPERTY_SERVICE_NAME;
import static org.openhab.binding.shelly.internal.discovery.ShellyThingCreator.*;
import static org.openhab.core.thing.Thing.PROPERTY_MODEL_ID;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapServer;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2NotifyEvent;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.binding.shelly.internal.util.ShellyUtils;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ShellyBluSensorHandler} implements the thing handler for the BLU devices
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyBluSensorHandler extends ShellyBaseHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShellyBluSensorHandler.class);

    public ShellyBluSensorHandler(final Thing thing, final ShellyTranslationProvider translationProvider,
            final ShellyBindingConfiguration bindingConfig, final ShellyThingTable thingTable,
            final Shelly1CoapServer coapServer, final HttpClient httpClient) {
        super(thing, translationProvider, bindingConfig, thingTable, coapServer, httpClient);
    }

    @Override
    public void initialize() {
        LOGGER.debug("Thing is using  {}", this.getClass());
        super.initialize();
    }

    public static void addBluThing(String gateway, Shelly2NotifyEvent e, @Nullable ShellyThingTable thingTable) {
        String model = ShellyUtils.substringBefore(ShellyUtils.getString(e.data.name), "-").toUpperCase();
        String mac = e.data.addr.replaceAll(":", "");

        LOGGER.debug("{}: Create thing for new BLU device {}: {} / {}", gateway, e.data.name, model, mac);
        String thingType = switch (model) {
            case SHELLYDT_BLUBUTTON -> THING_TYPE_SHELLYBLUBUTTON_STR;
            case SHELLYDT_BLUDW -> THING_TYPE_SHELLYBLUDW_STR;
            case SHELLYDT_BLUMOTION -> THING_TYPE_SHELLYBLUMOTION_STR;
            case SHELLYDT_BLUHT -> THING_TYPE_SHELLYBLUHT_STR;
            default -> {
                LOGGER.debug("{}: Unsupported BLU device model {}, MAC={}", gateway, model, mac);
                yield null;
            }
        };

        if (thingType != null) {
            String serviceName = ShellyDeviceProfile.buildBluServiceName(ShellyUtils.getString(e.data.name), mac);

            Map<String, Object> properties = new TreeMap<>();
            properties.put(PROPERTY_MODEL_ID, model);
            properties.put(PROPERTY_SERVICE_NAME, serviceName);
            properties.put(PROPERTY_DEV_NAME, ShellyUtils.getString(e.data.name));
            properties.put(PROPERTY_DEV_TYPE, thingType);
            properties.put(PROPERTY_DEV_GEN, "BLU");
            properties.put(PROPERTY_GW_DEVICE, ShellyUtils.getString(gateway));
            properties.put(CONFIG_DEVICEADDRESS, mac);

            if (thingTable != null) {
                thingTable.discoveredResult(model, serviceName, mac, properties);
            }
        }
    }
}
