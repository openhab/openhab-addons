/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api2.ShellyBluApi.buildBluServiceName;
import static org.openhab.binding.shelly.internal.discovery.ShellyThingCreator.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.*;
import static org.openhab.core.thing.Thing.PROPERTY_MODEL_ID;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.shelly.internal.api1.Shelly1CoapServer;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2NotifyEvent;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ShellyBluSensorHandler} implements the thing handler for the BLU devices
 *
 * @author Markus Michels - Initial contribution
 */
public class ShellyBluSensorHandler extends ShellyBaseHandler {
    private static final Logger logger = LoggerFactory.getLogger(ShellyBluSensorHandler.class);

    public ShellyBluSensorHandler(final Thing thing, final ShellyTranslationProvider translationProvider,
            final ShellyBindingConfiguration bindingConfig, final ShellyThingTable thingTable,
            final Shelly1CoapServer coapServer, final HttpClient httpClient) {
        super(thing, translationProvider, bindingConfig, thingTable, coapServer, httpClient);
    }

    @Override
    public void initialize() {
        logger.debug("Thing is using  {}", this.getClass());
        super.initialize();
    }

    public static void addBluThing(String gateway, Shelly2NotifyEvent e, ShellyThingTable thingTable) {
        String model = substringBefore(getString(e.data.name), "-").toUpperCase();
        String mac = e.data.addr.replace(":", "");
        String ttype = "";
        logger.debug("{}: Create thing for new BLU device {}: {} / {}", gateway, e.data.name, model, mac);
        ThingTypeUID tuid;
        switch (model) {
            case SHELLYDT_BLUBUTTON:
                ttype = THING_TYPE_SHELLYBLUBUTTON_STR;
                tuid = THING_TYPE_SHELLYBLUBUTTON;
                break;
            case SHELLYDT_BLUDW:
                ttype = THING_TYPE_SHELLYBLUDW_STR;
                tuid = THING_TYPE_SHELLYBLUDW;
                break;
            default:
                logger.debug("{}: Unsupported BLU device model {}, MAC={}", gateway, model, mac);
                return;
        }
        String serviceName = buildBluServiceName(model, mac);

        Map<String, Object> properties = new TreeMap<>();
        addProperty(properties, PROPERTY_MODEL_ID, model);
        addProperty(properties, PROPERTY_SERVICE_NAME, serviceName);
        addProperty(properties, PROPERTY_DEV_NAME, e.data.name);
        addProperty(properties, PROPERTY_DEV_TYPE, ttype);
        addProperty(properties, PROPERTY_DEV_GEN, "BLU");
        addProperty(properties, PROPERTY_GW_DEVICE, gateway);
        addProperty(properties, CONFIG_DEVICEADDRESS, mac);

        if (thingTable != null) {
            thingTable.discoveredResult(tuid, model, serviceName, mac, properties);
        }
    }

    private static void addProperty(Map<String, Object> properties, String key, @Nullable String value) {
        properties.put(key, value != null ? value : "");
    }
}
