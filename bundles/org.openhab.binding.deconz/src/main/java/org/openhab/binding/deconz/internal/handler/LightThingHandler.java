/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.deconz.internal.handler;

import static org.openhab.binding.deconz.internal.BindingConstants.*;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StopMoveType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.deconz.internal.dto.DeconzRestMessage;
import org.openhab.binding.deconz.internal.dto.LightConfig;
import org.openhab.binding.deconz.internal.dto.LightMessage;
import org.openhab.binding.deconz.internal.dto.LightState;
import org.openhab.binding.deconz.internal.netutils.AsyncHttpClient;
import org.openhab.binding.deconz.internal.netutils.WebSocketConnection;
import org.openhab.binding.deconz.internal.netutils.WebSocketMessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This light thing doesn't establish any connections, that is done by the bridge Thing.
 *
 * It waits for the bridge to come online, grab the websocket connection and bridge configuration
 * and registers to the websocket connection as a listener.
 *
 * A REST API call is made to get the initial light/rollershutter state.
 *
 * Every light and rollershutter is supported by this Thing, because a unified state is kept
 * in {@link #lightState}. Every field that got received by the REST API for this specific
 * sensor is published to the framework.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class LightThingHandler extends BaseThingHandler implements WebSocketMessageListener {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Stream.of(THING_TYPE_COLOR_TEMPERATURE_LIGHT,
            THING_TYPE_DIMMABLE_LIGHT, THING_TYPE_COLOR_LIGHT, THING_TYPE_EXTENDED_COLOR_LIGHT, THING_TYPE_WINDOW_COVERING)
            .collect(Collectors.toSet());
    private final Logger logger = LoggerFactory.getLogger(LightThingHandler.class);
    private ThingConfig config = new ThingConfig();
    private DeconzBridgeConfig bridgeConfig = new DeconzBridgeConfig();
    private final Gson gson;
    private @Nullable ScheduledFuture<?> scheduledFuture;
    private @Nullable WebSocketConnection connection;
    private @Nullable AsyncHttpClient http;
    /**
     * The light state. Contains all possible fields for all supported lights
     */
    private LightConfig lightConfig = new LightConfig();
    private LightState lightState = new LightState();
    private @Nullable Boolean lastCommand = null;

    public LightThingHandler(Thing thing, Gson gson) {
        super(thing);
        this.gson = gson;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannels(lightState);
            return;
        }

        LightState newlightState = new LightState();

        switch (channelUID.getId()) {
            case CHANNEL_SWITCH:
                if (command instanceof OnOffType) {
                    newlightState.on = (command == OnOffType.ON);
                } else {
                    return;
                }
                break;
            case CHANNEL_BRIGHTNESS:
            case CHANNEL_COLOR:
                if (command instanceof OnOffType) {
                    newlightState.on = (command == OnOffType.ON);
                } else if (command instanceof PercentType) {
                    newlightState.bri = (int) (((PercentType) command).doubleValue() * 2.55);
                } else if (command instanceof DecimalType) {
                    newlightState.bri = ((DecimalType) command).intValue();
                } else if (command instanceof HSBType) {
                    HSBType hsbCommand = (HSBType) command;
                    switch (lightState.colormode) {
                        case "xy":
                            PercentType[] xy = hsbCommand.toXY();
                            if (xy.length != 2) {
                                logger.warn("Failed to convert {} to xy-values", command);
                            }
                            newlightState.xy = new Double[] { xy[0].doubleValue() / 100.0,
                                    xy[1].doubleValue() / 100.0 };
                            break;
                        case "hs":
                            newlightState.bri = (int) (hsbCommand.getBrightness().doubleValue() * 2.55);
                            newlightState.hue = (int) (hsbCommand.getHue().doubleValue() * (65535 / 360));
                            newlightState.sat = (int) (hsbCommand.getSaturation().doubleValue() * 2.55);
                            break;
                        default:
                            return;
                    }
                } else {
                    return;
                }

                // send on/off state together with brightness if not already set
                if (newlightState.bri != null && (newlightState.bri > 0) != lightState.on) {
                    newlightState.on = (newlightState.bri > 0);
                }
                break;
            case CHANNEL_COLOR_TEMPERATURE:
                if (command instanceof DecimalType) {
                    newlightState.colormode = "ct";
                    newlightState.ct = scaleColorTemperature(((DecimalType) command).doubleValue());
                } else {
                    return;
                }
                break;
            case CHANNEL_POSITION:
                if (command instanceof UpDownType) {
                    newlightState.on = (command == UpDownType.DOWN);
                    lastCommand = newlightState.on;
                } else if (command == StopMoveType.STOP && lastCommand != null && lightState.on != lastCommand) {
                    newlightState.on = lastCommand;
                    lastCommand = null;
                } else if (command instanceof PercentType) {
                    newlightState.bri = (int) (((PercentType) command).doubleValue() * 2.55);
                } else {
                    return;
                }
                break;
            default:
                // no supported command
                return;
        }

        AsyncHttpClient asyncHttpClient = http;
        if (asyncHttpClient == null) {
            return;
        }
        String url = url(bridgeConfig.host, bridgeConfig.httpPort, bridgeConfig.apikey, "lights", config.id);
        url += "/state";

        String json = gson.toJson(newlightState);
        logger.debug("about so send {} to light {}", json, config.id);

        asyncHttpClient.put(url, json, bridgeConfig.timeout).thenAccept(v -> {
            logger.trace("code={}, body={}", v.getResponseCode(), v.getBody());
        }).exceptionally(e -> {
            logger.debug("exception:", e);
            return null;
        });
    }

    /**
     * Stops the API request
     */
    private void stopTimer() {
        ScheduledFuture<?> future = scheduledFuture;
        if (future != null) {
            future.cancel(true);
            scheduledFuture = null;
        }
    }

    private @Nullable LightMessage parseStateResponse(AsyncHttpClient.Result r) {
        if (r.getResponseCode() == 403) {
            return null;
        } else if (r.getResponseCode() == 200) {
            return gson.fromJson(r.getBody(), LightMessage.class);
        } else {
            throw new IllegalStateException("Unknown status code for full state request");
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (config.id.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "ID not set");
            return;
        }

        if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            WebSocketConnection webSocketConnection = connection;
            if (webSocketConnection != null) {
                webSocketConnection.unregisterLightListener(config.id);
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
        DeconzBridgeHandler bridgeHandler = (DeconzBridgeHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        final WebSocketConnection webSocketConnection = bridgeHandler.getWebsocketConnection();
        this.connection = webSocketConnection;
        this.http = bridgeHandler.getHttp();
        this.bridgeConfig = bridgeHandler.getBridgeConfig();

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING);

        // Real-time data
        webSocketConnection.registerLightListener(config.id, this);

        requestState();
    }

    /**
     * Perform a request to the REST API for retrieving the full light state with all data and configuration.
     */
    public void requestState() {
        AsyncHttpClient asyncHttpClient = http;
        if (asyncHttpClient == null) {
            return;
        }
        String url = url(bridgeConfig.host, bridgeConfig.httpPort, bridgeConfig.apikey, "lights", config.id);
        // Get initial data
        asyncHttpClient.get(url, bridgeConfig.timeout).thenApply(this::parseStateResponse).exceptionally(e -> {
            if (e instanceof SocketTimeoutException || e instanceof TimeoutException
                    || e instanceof CompletionException) {
                logger.debug("Get new state failed", e);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }

            stopTimer();
            scheduledFuture = scheduler.schedule(this::requestState, 10, TimeUnit.SECONDS);

            return null;
        }).thenAccept(newState -> {
            if (newState == null) {
                return;
            }

            lightConfig = new LightConfig(newState);
            LightState lightState = newState.state;
            if (lightState != null) {
                updateChannels(lightState);
            }

            updateStatus(ThingStatus.ONLINE);
        });
    }

    @Override
    public void dispose() {
        stopTimer();
        WebSocketConnection webSocketConnection = connection;
        if (webSocketConnection != null) {
            webSocketConnection.unregisterLightListener(config.id);
        }
        super.dispose();
    }

    @Override
    public void initialize() {
        config = getConfigAs(ThingConfig.class);

        Bridge bridge = getBridge();
        if (bridge != null) {
            bridgeStatusChanged(bridge.getStatusInfo());
        }
    }

    public void valueUpdated(String channelId, LightState newState) {
        logger.debug("{} received {}", thing.getUID(), newState);
        switch (channelId) {
            case CHANNEL_SWITCH:
                if (newState.on != null) {
                    updateState(channelId, OnOffType.from(newState.on));
                }
                break;
            case CHANNEL_BRIGHTNESS:
                if (newState.bri != null && newState.on != null && newState.on == true) {
                    BigDecimal brightness = new BigDecimal(newState.bri / 2.55);
                    updateState(channelId, new PercentType(brightness));
                } else {
                    updateState(channelId, OnOffType.OFF);
                }
                break;
            case CHANNEL_COLOR_TEMPERATURE:
                if (newState.ct != null) {
                    updateState(channelId, new DecimalType(scaleColorTemperature(newState.ct)));
                }
                break;
            case CHANNEL_POSITION:
                if (newState.bri != null) {
                    BigDecimal position = new BigDecimal(newState.bri / 2.55);
                    updateState(channelId, new PercentType(position));
                }
            default:
        }
    }

    private int scaleColorTemperature(double ct) {
        return (int) (ct / 100.0 * (500 - 153) + 153);
    }

    private double scaleColorTemperature(int ct) {
        return 100.0 * (ct - 153) / (500 - 153);
    }

    @Override
    public void messageReceived(String sensorID, DeconzRestMessage message) {
        if (message instanceof LightMessage) {
            LightMessage lightMessage = (LightMessage) message;

            if (lightMessage.hascolor != null) { // property "hascolor" is always present for config
                this.lightConfig = new LightConfig(lightMessage);
            }

            LightState lightState = lightMessage.state;
            if (lightState != null) {
                updateChannels(lightState);
            }
        }
    }

    private void updateChannels(LightState newState) {
        lightState = newState;
        thing.getChannels().stream().map(c -> c.getUID().getId()).forEach(c -> valueUpdated(c, newState));
    }
}
