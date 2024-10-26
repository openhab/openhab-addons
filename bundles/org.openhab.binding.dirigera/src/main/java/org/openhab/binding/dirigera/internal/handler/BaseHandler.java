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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openhab.binding.dirigera.internal.config.BaseDeviceConfiguration;
import org.openhab.binding.dirigera.internal.exception.NoGatewayException;
import org.openhab.binding.dirigera.internal.interfaces.Gateway;
import org.openhab.binding.dirigera.internal.model.Model;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseHandler} basic DeviceHandler for all devices
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public abstract class BaseHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(BaseHandler.class);
    private @Nullable BaseHandler child;
    private @Nullable Gateway gateway;

    protected Map<String, String> property2ChannelMap;
    protected Map<String, String> channel2PropertyMap;
    protected Map<String, State> channelStateMap;
    protected JSONObject deviceData = new JSONObject();
    protected List<JSONObject> updates = new ArrayList<>();
    protected BaseDeviceConfiguration config;

    public BaseHandler(Thing thing, Map<String, String> mapping) {
        super(thing);
        config = new BaseDeviceConfiguration();

        // mapping contains, reverse mapping for commands plus state cache
        property2ChannelMap = mapping;
        channel2PropertyMap = reverse(mapping);
        channelStateMap = initializeCache(mapping);
    }

    protected void setChildHandler(BaseHandler child) {
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
            BaseHandler proxy = child;
            if (proxy != null) {
                gateway().registerDevice(proxy, config.id);
            }
        }
    }

    /**
     * Handling of basic commands which are the same for many devices
     * - RefreshType for all channels
     * - Startup behavior for lights and plugs
     * - Power state for lights and plugs
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            String channel = channelUID.getIdWithoutGroup();
            State cachedState = channelStateMap.get(channel);
            if (cachedState != null) {
                super.updateState(channelUID, cachedState);
            }
        } else {
            String targetChannel = channelUID.getIdWithoutGroup();
            String targetProperty = channel2PropertyMap.get(targetChannel);
            if (targetProperty != null) {
                switch (targetChannel) {
                    case CHANNEL_STARTUP_BEHAVIOR:
                        if (command instanceof DecimalType decimal) {
                            String behaviorCommand = STARTUP_BEHAVIOR_REVERSE_MAPPING.get(decimal.intValue());
                            if (behaviorCommand != null) {
                                JSONObject stqartupAttributes = new JSONObject();
                                stqartupAttributes.put(targetProperty, behaviorCommand);
                                gateway().api().sendPatch(config.id, stqartupAttributes);
                            }
                            break;
                        }
                    case CHANNEL_POWER_STATE:
                        if (command instanceof OnOffType onOff) {
                            JSONObject attributes = new JSONObject();
                            attributes.put(targetProperty, onOff.equals(OnOffType.ON));
                            logger.trace("DIRIGERA LIGHT_DEVICE send to API {}", attributes);
                            gateway().api().sendPatch(config.id, attributes);
                        }
                }
            }
        }
    }

    /**
     * Handling generic channel updates for many devices.
     * If they are not present in child configuration they won't be triggered.
     * - Reachable flag for every device to evaluate ONLINE and OFFLINE states
     * - Over the air (OTA) updates channels
     * - Battery charge level
     * - Startup behavior for lights and plugs
     * - Power state for lights and plugs
     *
     * @param update
     */
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
                Integer otaStatus = OTA_STATUS_MAP.get(otaStatusString);
                if (otaStatus != null) {
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_OTA_STATUS), new DecimalType(otaStatus));
                } else {
                    logger.warn("Cannot decode ota status {}", otaStatusString);
                }
            }
            if (attributes.has(PROPERTY_OTA_STATE)) {
                String otaStateString = attributes.getString(PROPERTY_OTA_STATE);
                Integer otaState = OTA_STATE_MAP.get(otaStateString);
                if (otaState != null) {
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
            if (attributes.has(PROPERTY_STARTUP_BEHAVIOR)) {
                String startupString = attributes.getString(PROPERTY_STARTUP_BEHAVIOR);
                Integer startupValue = STARTUP_BEHAVIOR_MAPPING.get(startupString);
                if (startupValue != null) {
                    updateState(new ChannelUID(thing.getUID(), CHANNEL_STARTUP_BEHAVIOR),
                            new DecimalType(startupValue));
                } else {
                    logger.warn("Cannot decode startup behavior {}", startupString);
                }
            }
            if (attributes.has(PROPERTY_POWER_STATE)) {
                updateState(new ChannelUID(thing.getUID(), CHANNEL_POWER_STATE),
                        OnOffType.from(attributes.getBoolean(PROPERTY_POWER_STATE)));
            }
        }

        // store data for development and analysis purposes
        if (deviceData.isEmpty())

        {
            deviceData = new JSONObject(update.toString());
        } else {
            updates.add(new JSONObject(update.toString()));
            // keep last 10 updates for debugging
            if (updates.size() > 10) {
                updates.remove(0);
            }
        }
        deviceData.put("updates", new JSONArray(updates));

        updateState(new ChannelUID(thing.getUID(), CHANNEL_JSON), StringType.valueOf(deviceData.toString()));
    }

    @Override
    protected void updateState(ChannelUID channelUID, State state) {
        channelStateMap.put(channelUID.getIdWithoutGroup(), state);
        super.updateState(channelUID, state);
    }

    @Override
    public void dispose() {
        BaseHandler proxy = child;
        if (proxy != null) {
            gateway().unregisterDevice(proxy, config.id);
        }
    }

    @Override
    public void handleRemoval() {
        BaseHandler proxy = child;
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

    protected boolean checkHandler() {
        // cross check if configured thing type is matching with the model
        // if handler is taken from discovery this will do no harm
        // but if it's created manually mismatch can happen
        ThingTypeUID modelTTUID = gateway().model().identifyDeviceFromModel(config.id);
        if (!thing.getThingTypeUID().equals(modelTTUID)) {
            // check if id is present in mdoel
            if (THING_TYPE_NOT_FOUND.equals(modelTTUID)) {
                String message = "Device id " + config.id + " not found";
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, message);
            } else {
                String message = "Handler " + thing.getThingTypeUID() + " doesn't match with model " + modelTTUID;
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, message);
            }
            return false;
        }
        return true;
    }

    private Map<String, State> initializeCache(Map<String, String> mapping) {
        final Map<String, State> stateMap = new HashMap<>();
        mapping.forEach((key, value) -> {
            stateMap.put(key, UnDefType.UNDEF);
        });
        return stateMap;
    }

    /**
     * for unit testing
     */
    @Override
    public @Nullable ThingHandlerCallback getCallback() {
        return super.getCallback();
    }
}
