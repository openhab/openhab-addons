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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.config.BaseDeviceConfiguration;
import org.openhab.binding.dirigera.internal.exception.NoGatewayException;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.binding.dirigera.internal.model.Model;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
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

    protected BaseDeviceConfiguration config;

    private @Nullable Gateway gateway;
    private @Nullable BaseDeviceHandler child;

    protected Map<String, String> property2ChannelMap;
    protected Map<String, String> channel2PropertyMap;

    public BaseDeviceHandler(Thing thing, Map<String, String> mapping) {
        super(thing);
        config = new BaseDeviceConfiguration();
        // mapping contains
        property2ChannelMap = mapping;
        channel2PropertyMap = reverse(mapping);
    }

    protected void setChildHandler(BaseDeviceHandler child) {
        this.child = child;
    }

    @Override
    public void initialize() {
        // first get bridge as Gateway
        Bridge bridge = getBridge();
        if (bridge != null) {
            updateStatus(ThingStatus.UNKNOWN);
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler instanceof Gateway gw) {
                    gateway = gw;
                    BaseDeviceHandler proxy = child;
                    if (proxy != null) {
                        gateway().registerDevice(proxy);
                    }
                    logger.trace("DIRIGERA BASE_DEVICE Gateway found");
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

        config = getConfigAs(BaseDeviceConfiguration.class);
        if (!config.id.isBlank()) {
            gateway().registerDevice(this);
        }

        updateStatus(ThingStatus.ONLINE);
    }

    public void handleUpdate(JSONObject update) {
        if (update.has(Model.REACHABLE)) {
            logger.trace("DIRIGERA BASE_DEVICE Device switches to reachable {}", update.getBoolean(Model.REACHABLE));
            if (update.getBoolean(Model.REACHABLE)) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device not reachable");
            }
        } else {
            logger.trace("DIRIGERA BASE_DEVICE no reachable found in {}", update);
        }
    }

    @Override
    public void dispose() {
        BaseDeviceHandler proxy = child;
        if (proxy != null) {
            gateway().unregisterDevice(proxy);
        }
    }

    @Override
    public void handleRemoval() {
        BaseDeviceHandler proxy = child;
        if (proxy != null) {
            gateway().deleteDevice(proxy);
        }
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
