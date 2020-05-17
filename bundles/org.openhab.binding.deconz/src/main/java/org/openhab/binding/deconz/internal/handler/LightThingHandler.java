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
import java.util.Set;
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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.deconz.internal.dto.DeconzRestMessage;
import org.openhab.binding.deconz.internal.dto.LightMessage;
import org.openhab.binding.deconz.internal.dto.LightState;
import org.openhab.binding.deconz.internal.netutils.AsyncHttpClient;
import org.openhab.binding.deconz.internal.netutils.WebSocketConnection;
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
public class LightThingHandler extends DeconzBaseThingHandler<LightMessage> {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Stream
            .of(THING_TYPE_COLOR_TEMPERATURE_LIGHT, THING_TYPE_DIMMABLE_LIGHT, THING_TYPE_COLOR_LIGHT,
                    THING_TYPE_EXTENDED_COLOR_LIGHT, THING_TYPE_ONOFF_LIGHT, THING_TYPE_WINDOW_COVERING)
            .collect(Collectors.toSet());
    private final Logger logger = LoggerFactory.getLogger(LightThingHandler.class);

    /**
     * The light state. Contains all possible fields for all supported lights
     */
    private LightState lightState = new LightState();

    public LightThingHandler(Thing thing, Gson gson) {
        super(thing, gson);
    }

    @Override
    protected void registerListener() {
        @Nullable WebSocketConnection conn = connection;
        if (conn != null) {
            conn.registerLightListener(config.id, this);
        }
    }

    @Override
    protected void unregisterListener() {
        @Nullable WebSocketConnection conn = connection;
        if (conn != null) {
            conn.unregisterLightListener(config.id);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateChannels(lightState);
            return;
        }

        LightState newLightState = new LightState();
        @Nullable Boolean currentOn = lightState.on;
        @Nullable Integer currentBri = lightState.bri;

        switch (channelUID.getId()) {
            case CHANNEL_SWITCH:
                if (command instanceof OnOffType) {
                    newLightState.on = (command == OnOffType.ON);
                } else {
                    return;
                }
                break;
            case CHANNEL_BRIGHTNESS:
            case CHANNEL_COLOR:
                if (command instanceof OnOffType) {
                    newLightState.on = (command == OnOffType.ON);
                } else if (command instanceof HSBType) {
                    HSBType hsbCommand = (HSBType) command;

                    @Nullable String colormode = lightState.colormode;
                    if (colormode == null) {
                        // default color mode to hsb
                        colormode = "hs";
                    }

                    switch (colormode) {
                        case "xy":
                            PercentType[] xy = hsbCommand.toXY();
                            if (xy.length < 2) {
                                logger.warn("Failed to convert {} to xy-values", command);
                            }
                            newLightState.xy = new Double[] { xy[0].doubleValue() / 100.0,
                                    xy[1].doubleValue() / 100.0 };
                            break;
                        case "hs":
                            newLightState.bri = (int) (hsbCommand.getBrightness().doubleValue() * 2.55);
                            newLightState.hue = (int) (hsbCommand.getHue().doubleValue() * (65535 / 360));
                            newLightState.sat = (int) (hsbCommand.getSaturation().doubleValue() * 2.55);
                            break;
                        default:
                            return;
                    }
                } else if (command instanceof PercentType) {
                    newLightState.bri = (int) (((PercentType) command).doubleValue() * 2.55);
                } else if (command instanceof DecimalType) {
                    newLightState.bri = ((DecimalType) command).intValue();
                } else {
                    return;
                }

                // send on/off state together with brightness if not already set or unknown
                @Nullable Integer newBri = newLightState.bri;
                if ((newBri != null) && ((currentOn == null) || ((newBri > 0) != currentOn))) {
                    newLightState.on = (newBri > 0);
                }
                break;
            case CHANNEL_COLOR_TEMPERATURE:
                if (command instanceof DecimalType) {
                    newLightState.colormode = "ct";
                    newLightState.ct = scaleColorTemperature(((DecimalType) command).doubleValue());
                } else {
                    return;
                }
                break;
            case CHANNEL_POSITION:
                if (command instanceof UpDownType) {
                    newLightState.on = (command == UpDownType.DOWN);
                } else if (command == StopMoveType.STOP) {
                    if (currentOn != null && currentOn && currentBri != null && currentBri <= 254) {
                        // going down or currently stop (254 because of rounding error)
                        newLightState.on = true;
                    } else if (currentOn != null && !currentOn && currentBri != null && currentBri > 0) {
                        // going up or currently stopped
                        newLightState.on = false;
                    }
                } else if (command instanceof PercentType) {
                    newLightState.bri = (int) (((PercentType) command).doubleValue() * 2.55);
                } else {
                    return;
                }
                break;
            default:
                // no supported command
                return;
        }

        @Nullable AsyncHttpClient asyncHttpClient = http;
        if (asyncHttpClient == null) {
            return;
        }
        String url = buildUrl(bridgeConfig.host, bridgeConfig.httpPort, bridgeConfig.apikey, "lights", config.id,
                "state");

        String json = gson.toJson(newLightState);
        logger.trace("Sending {} to light {}", json, config.id);

        asyncHttpClient.put(url, json, bridgeConfig.timeout)
                .thenAccept(v -> logger.trace("Result code={}, body={}", v.getResponseCode(), v.getBody()))
                .exceptionally(e -> {
                    logger.debug("exception:", e);
                    return null;
                });
    }

    @Override
    protected @Nullable LightMessage parseStateResponse(AsyncHttpClient.Result r) {
        if (r.getResponseCode() == 403) {
            return null;
        } else if (r.getResponseCode() == 200) {
            return gson.fromJson(r.getBody(), LightMessage.class);
        } else {
            throw new IllegalStateException("Unknown status code for full state request");
        }
    }

    @Override
    protected void processStateResponse(@Nullable LightMessage stateResponse) {
        if (stateResponse == null) {
            return;
        }
        updateStatus(ThingStatus.ONLINE);
    }

    public void valueUpdated(String channelId, LightState newState) {
        logger.debug("{} received {}", thing.getUID(), newState);
        @Nullable Integer bri = newState.bri;
        @Nullable Integer ct = newState.ct;
        @Nullable Boolean on = newState.on;
        Double @Nullable [] xy = newState.xy;
        @Nullable Integer hue = newState.hue;
        @Nullable Integer sat = newState.sat;

        switch (channelId) {
            case CHANNEL_SWITCH:
                if (on != null) {
                    updateState(channelId, OnOffType.from(on));
                }
                break;
            case CHANNEL_COLOR:
                if ("xy".equals(newState.colormode)) {
                    if (xy != null && xy.length == 2) {
                        updateState(channelId, HSBType.fromXY(xy[0].floatValue(), xy[1].floatValue()));
                    }
                } else if ("hs".equals(newState.colormode)) {
                    if (hue != null && sat != null && bri != null) {
                        updateState(channelId, new HSBType(new DecimalType(hue / 65535 * 360),
                                new PercentType(new BigDecimal(sat / 2.55)),
                                new PercentType(new BigDecimal(bri / 2.55))));

                    }
                }
                break;
            case CHANNEL_BRIGHTNESS:
                if (bri != null && on != null && on) {
                    updateState(channelId, new PercentType(new BigDecimal(bri / 2.55)));
                } else {
                    updateState(channelId, OnOffType.OFF);
                }
                break;
            case CHANNEL_COLOR_TEMPERATURE:
                if (ct != null) {
                    updateState(channelId, new DecimalType(scaleColorTemperature(ct)));
                }
                break;
            case CHANNEL_POSITION:
                if (bri != null) {
                    updateState(channelId, new PercentType(new BigDecimal(bri / 2.55)));
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
            @Nullable LightState lightState = lightMessage.state;
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
