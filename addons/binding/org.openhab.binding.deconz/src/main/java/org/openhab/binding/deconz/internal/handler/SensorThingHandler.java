/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.deconz.internal.handler;

import java.util.Map;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Illuminance;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.deconz.internal.BindingConstants;
import org.openhab.binding.deconz.internal.dto.SensorMessage;
import org.openhab.binding.deconz.internal.dto.SensorState;
import org.openhab.binding.deconz.internal.netutils.AsyncHttpClient;
import org.openhab.binding.deconz.internal.netutils.ValueUpdateListener;
import org.openhab.binding.deconz.internal.netutils.WebSocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This sensor Thing doesn't establish any connections, that is done by the bridge Thing.
 *
 * It waits for the bridge to come online, grab the websocket connection and bridge configuration
 * and registers to the websocket connection as a listener.
 *
 * A REST API call is made to get the initial sensor state.
 *
 * Every sensor and switch is supported by this Thing, because a unified state is kept
 * in {@link #state}. Every field that got received by the REST API for this specific
 * sensor is published to the framework.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public class SensorThingHandler extends BaseThingHandler implements ValueUpdateListener {
    private final Logger logger = LoggerFactory.getLogger(SensorThingHandler.class);
    private SensorThingConfig config = new SensorThingConfig();
    private DeconzBridgeConfig bridgeConfig = new DeconzBridgeConfig();
    private final Gson gson = new Gson();
    private @Nullable WebSocketConnection connection;
    /** The sensor state. Contains all possible fields for all supported sensors and switches */
    private SensorState state = new SensorState();
    /** Prevent a dispose/init cycle while this flag is set. Use for property updates */
    private boolean ignoreConfigurationUpdate;

    public SensorThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            return;
        }

        state.buttonevent = null;
        valueUpdated(channelUID.getId(), state);
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        if (!ignoreConfigurationUpdate) {
            super.handleConfigurationUpdate(configurationParameters);
        }
    }

    private @Nullable SensorMessage parseStateResponse(AsyncHttpClient.Result r) {
        if (r.getResponseCode() == 403) {
            return null;
        } else if (r.getResponseCode() == 200) {
            return gson.fromJson(r.getBody(), SensorMessage.class);
        } else {
            throw new IllegalStateException("Unknown status code for full state request");
        }
    }

    @Override
    public void bridgeStatusChanged(@NonNull ThingStatusInfo bridgeStatusInfo) {

        if (config.id.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "ID not set");
            return;
        }

        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            WebSocketConnection webSocketConnection = connection;
            if (webSocketConnection != null) {
                webSocketConnection.unregisterValueListener(config.id);
            }
            return;
        }

        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            return;
        }

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        DeconzBridgeHandler handler = (DeconzBridgeHandler) bridge.getHandler();
        if (handler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        final WebSocketConnection webSocketConnection = handler.getWebsocketConnection();
        this.connection = webSocketConnection;
        this.bridgeConfig = handler.getBridgeConfig();

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING);

        String url = BindingConstants.url(bridgeConfig.host, bridgeConfig.apikey, "sensors", config.id);

        // Get initial data
        handler.getHttp().get(url.toString(), bridgeConfig.timeout).thenApply(this::parseStateResponse) //
                .exceptionally(e -> {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                    logger.debug("Get state failed", e);
                    return null;
                }).thenAccept(newState -> {
                    // Auth failed
                    if (newState == null) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Auth failed");
                        return;
                    }

                    // Add some information about the bridge

                    if (newState.config.battery != null) {
                        ignoreConfigurationUpdate = true;
                        updateProperty("battery", String.valueOf(newState.config.battery));
                        ignoreConfigurationUpdate = false;
                    }

                    if (!newState.config.reachable) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Not reachable");
                        return;
                    }

                    if (!newState.config.on) {
                        updateStatus(ThingStatus.OFFLINE);
                        return;
                    }

                    // Initial data
                    valueUpdated(config.id, newState.state);

                    // Real-time data
                    webSocketConnection.registerValueListener(config.id, this);
                    updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
                });

    }

    @Override
    public void dispose() {
        WebSocketConnection webSocketConnection = connection;
        if (webSocketConnection != null) {
            webSocketConnection.unregisterValueListener(config.id);
        }
        super.dispose();
    }

    @Override
    public void initialize() {
        config = getConfigAs(SensorThingConfig.class);

        Bridge bridge = getBridge();
        if (bridge != null) {
            bridgeStatusChanged(bridge.getStatusInfo());
        }
    }

    @Override
    public void valueUpdated(String id, SensorState state) {
        this.state = state;

        Integer buttonevent = state.buttonevent;
        Integer status = state.status;
        Boolean presence = state.presence;
        Integer power = state.power;
        Integer lux = state.lux;
        Float temperature = state.temperature;

        switch (id) {
            case BindingConstants.CHANNEL_DAYLIGHT:
                if (state.dark != null) {
                    boolean dark = state.dark;
                    if (dark) { // if it's dark, it's dark ;)
                        updateState(BindingConstants.CHANNEL_DAYLIGHT, new StringType("Dark"));
                    } else if (state.daylight != null) { // if its not dark, it might be between darkness and daylight
                        boolean daylight = state.daylight;
                        if (daylight) {
                            updateState(BindingConstants.CHANNEL_DAYLIGHT, new StringType("Daylight"));
                        } else if (!daylight) {
                            updateState(BindingConstants.CHANNEL_DAYLIGHT, new StringType("Sunset"));
                        }
                    } else { // if no daylight value is known, we assume !dark means daylight
                        updateState(BindingConstants.CHANNEL_DAYLIGHT, new StringType("Daylight"));
                    }
                }
                break;
            case BindingConstants.CHANNEL_POWER:
                if (power != null) {
                    updateState(id, new QuantityType<Energy>(power, SmartHomeUnits.WATT_HOUR));
                }
                break;
            case BindingConstants.CHANNEL_LIGHT_LUX:
                if (lux != null) {
                    updateState(id, new QuantityType<Illuminance>(lux, SmartHomeUnits.LUX));
                }
                break;
            case BindingConstants.CHANNEL_TEMPERATURE:
                if (temperature != null) {
                    updateState(id, new QuantityType<Temperature>(temperature, SIUnits.CELSIUS));
                }
                break;
            case BindingConstants.CHANNEL_PRESENCE:
                if (presence != null) {
                    updateState(id, OnOffType.from(presence));
                }
                break;
            case BindingConstants.CHANNEL_VALUE:
                if (status != null) {
                    updateState(id, new DecimalType(status));
                }
                break;
        }

        if (buttonevent != null) {
            triggerChannel(BindingConstants.CHANNEL_BUTTONEVENT, String.valueOf(buttonevent));
        }
    }
}
