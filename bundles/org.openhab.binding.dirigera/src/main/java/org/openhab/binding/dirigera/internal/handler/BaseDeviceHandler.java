/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.handler;

import static org.openhab.binding.dirigera.internal.Constants.PROPERTY_DEVICE_ID;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.config.BaseDeviceConfiguration;
import org.openhab.binding.dirigera.internal.exception.NoGatewayException;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseDeviceHandler} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public abstract class BaseDeviceHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(BaseDeviceHandler.class);
    private BaseDeviceConfiguration config;
    private @Nullable Gateway gateway;

    protected Map<String, String> property2ChannelMap;
    protected Map<String, String> channel2PropertyMap;

    public BaseDeviceHandler(Thing thing, Map<String, String> mapping) {
        super(thing);
        config = new BaseDeviceConfiguration();
        // mapping contains
        property2ChannelMap = mapping;
        channel2PropertyMap = reverse(mapping);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public void handleUpdate(JSONObject update) {
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            updateStatus(ThingStatus.UNKNOWN);
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler instanceof Gateway gw) {
                    gateway = gw;
                    gateway.registerDevice(this);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "Bridgehandler isn't a Gateway");
                    return;
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No BridgeHandler found");
                return;
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No Bridge found");
            return;
        }

        // check if thing was created by discovery and id is already present
        config = getConfigAs(BaseDeviceConfiguration.class);
        if (config.id.isBlank()) {
            logger.trace("DIRIGERA BASE_DEVICE id not configured - try to evaluate");
            Map<String, String> properties = editProperties();
            String id = properties.get(PROPERTY_DEVICE_ID);
            if (id != null) {
                if (!id.isBlank()) {
                    Configuration currentConfig = editConfiguration();
                    currentConfig.put(PROPERTY_DEVICE_ID, id);
                    updateConfiguration(currentConfig);
                }
            }
        }
        config = getConfigAs(BaseDeviceConfiguration.class);
        if (!config.id.isBlank()) {
            gateway().registerDevice(this);
        }
    }

    @Override
    public void dispose() {
    }

    public Gateway gateway() {
        Gateway gw = gateway;
        if (gw != null) {
            return gw;
        } else {
            throw new NoGatewayException(thing.getUID() + " has no Gateway defined");
        }
    }

    public String getId() {
        return config.id;
    }

    private Map<String, String> reverse(Map<String, String> mapping) {
        final Map<String, String> reverseMap = new HashMap<>();
        mapping.forEach((key, value) -> {
            reverseMap.put(value, key);
        });
        return reverseMap;
    }
}
