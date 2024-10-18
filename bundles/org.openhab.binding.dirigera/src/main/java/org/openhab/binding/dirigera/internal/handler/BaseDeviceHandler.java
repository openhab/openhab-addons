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

import static org.openhab.binding.dirigera.internal.Constants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.config.BaseDeviceConfiguration;
import org.openhab.binding.dirigera.internal.exception.NoGatewayException;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.binding.dirigera.internal.model.Model;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
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
    protected Map<String, State> channelStateMap;

    public BaseDeviceHandler(Thing thing, Map<String, String> mapping) {
        super(thing);
        config = new BaseDeviceConfiguration();

        // mapping contains, reverse mapping for commands plus state cache
        property2ChannelMap = mapping;
        channel2PropertyMap = reverse(mapping);
        channelStateMap = initializeCache(mapping);
    }

    protected void setChildHandler(BaseDeviceHandler child) {
        this.child = child;
    }

    @Override
    public void initialize() {
        config = getConfigAs(BaseDeviceConfiguration.class);

        // first get bridge as Gateway
        Bridge bridge = getBridge();
        if (bridge != null) {
            updateStatus(ThingStatus.UNKNOWN);
            BridgeHandler handler = bridge.getHandler();
            if (handler != null) {
                if (handler instanceof Gateway gw) {
                    gateway = gw;
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

        if (!checkHandler()) {
            // if handler doesn't match model status will be set to offline and it will stay until correction
            return;
        }

        if (!config.id.isBlank()) {
            BaseDeviceHandler proxy = child;
            if (proxy != null) {
                gateway().registerDevice(proxy, config.id);
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            String channel = channelUID.getIdWithoutGroup();
            State cachedState = channelStateMap.get(channel);
            if (cachedState != null) {
                super.updateState(channelUID, cachedState);
            }
        }
    }

    public void handleUpdate(JSONObject update) {
        // check online offline for each device
        if (update.has(Model.REACHABLE)) {
            if (update.getBoolean(Model.REACHABLE)) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Device not reachable");
            }
        }
        if (update.has(Model.ATTRIBUTES)) {
            JSONObject attributes = update.getJSONObject(Model.ATTRIBUTES);
            // check ota for each device
            if (attributes.has(PROPERTY_OTA_STATUS)) {
                String otaStatusString = attributes.getString(PROPERTY_OTA_STATUS);
                if (OTA_STATUS_MAP.containsKey(otaStatusString)) {
                    int otaStatus = OTA_STATUS_MAP.get(otaStatusString);
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATUS), new DecimalType(otaStatus));
                } else {
                    logger.warn("Cannot decode ota status {}", otaStatusString);
                }
            }
            if (attributes.has(PROPERTY_OTA_STATE)) {
                String otaStateString = attributes.getString(PROPERTY_OTA_STATE);
                if (OTA_STATE_MAP.containsKey(otaStateString)) {
                    int otaState = OTA_STATE_MAP.get(otaStateString);
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATE), new DecimalType(otaState));
                } else {
                    logger.warn("Cannot decode ota state {}", otaStateString);
                }
            }
            if (attributes.has(PROPERTY_OTA_PROGRESS)) {
                updateState(new ChannelUID(thing.getUID(), CHANNEL_OTA_PROGRESS),
                        QuantityType.valueOf(attributes.getInt(PROPERTY_OTA_PROGRESS), Units.PERCENT));
            }
            // battery also common, not for all but sensors and remote controller
            if (attributes.has(PROPERTY_BATTERY_PERCENTAGE)) {
                updateState(new ChannelUID(thing.getUID(), CHANNEL_BATTERY_LEVEL),
                        QuantityType.valueOf(attributes.getInt(PROPERTY_BATTERY_PERCENTAGE), Units.PERCENT));
            }
        }
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        channelStateMap.put(channelUID.getIdWithoutGroup(), state);
        super.updateState(channelUID, state);
    }

    @Override
    public void dispose() {
        BaseDeviceHandler proxy = child;
        if (proxy != null) {
            gateway().unregisterDevice(proxy, config.id);
        }
    }

    @Override
    public void handleRemoval() {
        BaseDeviceHandler proxy = child;
        if (proxy != null) {
            gateway().deleteDevice(proxy, config.id);
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

    private Map<String, String> reverse(Map<String, String> mapping) {
        final Map<String, String> reverseMap = new HashMap<>();
        mapping.forEach((key, value) -> {
            reverseMap.put(value, key);
        });
        return reverseMap;
    }

    private Map<String, State> initializeCache(Map<String, String> mapping) {
        final Map<String, State> stateMap = new HashMap<>();
        mapping.forEach((key, value) -> {
            stateMap.put(key, UnDefType.UNDEF);
        });
        return stateMap;
    }

    public boolean checkHandler() {
        // cross check if configured thing type is matching with the model
        // if handler is taken from discovery this will do no harm
        // but if it's created manually mismatch can happen
        ThingTypeUID modelTTUID = gateway().model().identifyDevice(config.id);
        if (!thing.getThingTypeUID().equals(modelTTUID)) {
            String message = "Handler " + thing.getThingTypeUID() + " doesn't match with model " + modelTTUID;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
            return false;
        } else {
            return true;
        }
    }
}
