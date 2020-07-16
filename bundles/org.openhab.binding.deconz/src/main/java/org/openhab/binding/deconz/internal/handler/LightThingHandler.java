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
import static org.openhab.binding.deconz.internal.Util.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import org.eclipse.smarthome.core.types.StateDescription;
import org.eclipse.smarthome.core.types.StateDescriptionFragmentBuilder;
import org.openhab.binding.deconz.internal.StateDescriptionProvider;
import org.openhab.binding.deconz.internal.Util;
import org.openhab.binding.deconz.internal.dto.DeconzBaseMessage;
import org.openhab.binding.deconz.internal.dto.LightMessage;
import org.openhab.binding.deconz.internal.dto.LightState;
import org.openhab.binding.deconz.internal.netutils.AsyncHttpClient;
import org.openhab.binding.deconz.internal.netutils.WebSocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * This light thing doesn't establish any connections, that is done by the bridge Thing.
 *
 * It waits for the bridge to come online, grab the websocket connection and bridge configuration
 * and registers to the websocket connection as a listener.
 *
 * A REST API call is made to get the initial light/rollershutter state.
 *
 * Every light and rollershutter is supported by this Thing, because a unified state is kept
 * in {@link #lightStateCache}. Every field that got received by the REST API for this specific
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

    private static final double HUE_FACTOR = 65535 / 360.0;
    private static final double BRIGHTNESS_FACTOR = 2.54;
    private static final long DEFAULT_COMMAND_EXPIRY_TIME = 250; // in ms

    private final Logger logger = LoggerFactory.getLogger(LightThingHandler.class);

    private final StateDescriptionProvider stateDescriptionProvider;

    private long lastCommandExpireTimestamp = 0;
    private boolean needsPropertyUpdate = false;

    /**
     * The light state. Contains all possible fields for all supported lights
     */
    private LightState lightStateCache = new LightState();
    private LightState lastCommand = new LightState();

    // set defaults, we can override them later if we receive better values
    private int ctMax = ZCL_CT_MAX;
    private int ctMin = ZCL_CT_MIN;

    public LightThingHandler(Thing thing, Gson gson, StateDescriptionProvider stateDescriptionProvider) {
        super(thing, gson);

        this.stateDescriptionProvider = stateDescriptionProvider;
    }

    @Override
    public void initialize() {
        if (thing.getThingTypeUID().equals(THING_TYPE_COLOR_TEMPERATURE_LIGHT)
                || thing.getThingTypeUID().equals(THING_TYPE_EXTENDED_COLOR_LIGHT)) {
            try {
                Map<String, String> properties = thing.getProperties();
                ctMax = Integer.parseInt(properties.get(PROPERTY_CT_MAX));
                ctMin = Integer.parseInt(properties.get(PROPERTY_CT_MIN));

                // minimum and maximum are inverted due to mired/kelvin conversion!
                StateDescription stateDescription = StateDescriptionFragmentBuilder.create()
                        .withMinimum(new BigDecimal(miredToKelvin(ctMax)))
                        .withMaximum(new BigDecimal(miredToKelvin(ctMin))).build().toStateDescription();
                if (stateDescription != null) {
                    stateDescriptionProvider.setDescription(new ChannelUID(thing.getUID(), CHANNEL_COLOR_TEMPERATURE),
                            stateDescription);
                } else {
                    logger.warn("Failed to create state description in thing {}", thing.getUID());
                }
            } catch (NumberFormatException e) {
                needsPropertyUpdate = true;
            }
        }
        super.initialize();
    }

    @Override
    protected void registerListener() {
        WebSocketConnection conn = connection;
        if (conn != null) {
            conn.registerLightListener(config.id, this);
        }
    }

    @Override
    protected void unregisterListener() {
        WebSocketConnection conn = connection;
        if (conn != null) {
            conn.unregisterLightListener(config.id);
        }
    }

    @Override
    protected void requestState() {
        requestState("lights");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            valueUpdated(channelUID.getId(), lightStateCache);
            return;
        }

        LightState newLightState = new LightState();
        Boolean currentOn = lightStateCache.on;
        Integer currentBri = lightStateCache.bri;

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

                    if ("xy".equals(lightStateCache.colormode)) {
                        PercentType[] xy = hsbCommand.toXY();
                        if (xy.length < 2) {
                            logger.warn("Failed to convert {} to xy-values", command);
                        }
                        newLightState.xy = new double[] { xy[0].doubleValue() / 100.0, xy[1].doubleValue() / 100.0 };
                        newLightState.bri = fromPercentType(hsbCommand.getBrightness());
                    } else {
                        // default is colormode "hs" (used when colormode "hs" is set or colormode is unknown)
                        newLightState.bri = fromPercentType(hsbCommand.getBrightness());
                        newLightState.hue = (int) (hsbCommand.getHue().doubleValue() * HUE_FACTOR);
                        newLightState.sat = fromPercentType(hsbCommand.getSaturation());
                    }
                } else if (command instanceof PercentType) {
                    newLightState.bri = fromPercentType((PercentType) command);
                } else if (command instanceof DecimalType) {
                    newLightState.bri = ((DecimalType) command).intValue();
                } else {
                    return;
                }

                // send on/off state together with brightness if not already set or unknown
                Integer newBri = newLightState.bri;
                if ((newBri != null) && ((currentOn == null) || ((newBri > 0) != currentOn))) {
                    newLightState.on = (newBri > 0);
                }

                // fix sending bri=0 when light is already off
                if (newBri != null && newBri == 0 && currentOn != null && !currentOn) {
                    return;
                }

                Double transitiontime = config.transitiontime;
                if (transitiontime != null) {
                    // value is in 1/10 seconds
                    newLightState.transitiontime = (int) Math.round(10 * transitiontime);
                }
                break;
            case CHANNEL_COLOR_TEMPERATURE:
                if (command instanceof DecimalType) {
                    int miredValue = kelvinToMired(((DecimalType) command).intValue());
                    newLightState.ct = constrainToRange(miredValue, ctMin, ctMax);

                    if (currentOn != null && !currentOn) {
                        // sending new color temperature is only allowed when light is on
                        newLightState.on = true;
                    }
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
                    newLightState.bri = fromPercentType((PercentType) command);
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
        String url = buildUrl(bridgeConfig.host, bridgeConfig.httpPort, bridgeConfig.apikey, "lights", config.id,
                "state");

        if (newLightState.on != null && !newLightState.on) {
            // if light shall be off, no other commands are allowed, so reset the new light state
            newLightState.clear();
            newLightState.on = false;
        }

        String json = gson.toJson(newLightState);
        logger.trace("Sending {} to light {} via {}", json, config.id, url);

        asyncHttpClient.put(url, json, bridgeConfig.timeout).thenAccept(v -> {
            lastCommandExpireTimestamp = System.currentTimeMillis()
                    + (newLightState.transitiontime != null ? newLightState.transitiontime
                            : DEFAULT_COMMAND_EXPIRY_TIME);
            lastCommand = newLightState;
            logger.trace("Result code={}, body={}", v.getResponseCode(), v.getBody());
        }).exceptionally(e -> {
            logger.debug("Sending command {} to channel {} failed:", command, channelUID, e);
            return null;
        });
    }

    @Override
    protected @Nullable LightMessage parseStateResponse(AsyncHttpClient.Result r) {
        if (r.getResponseCode() == 403) {
            return null;
        } else if (r.getResponseCode() == 200) {
            LightMessage lightMessage = gson.fromJson(r.getBody(), LightMessage.class);
            if (lightMessage != null && needsPropertyUpdate) {
                // if we did not receive an ctmin/ctmax, then we probably don't need it
                needsPropertyUpdate = false;

                if (lightMessage.ctmin != null && lightMessage.ctmax != null) {
                    Map<String, String> properties = new HashMap<>(thing.getProperties());
                    properties.put(PROPERTY_CT_MAX,
                            Integer.toString(Util.constrainToRange(lightMessage.ctmax, ZCL_CT_MIN, ZCL_CT_MAX)));
                    properties.put(PROPERTY_CT_MIN,
                            Integer.toString(Util.constrainToRange(lightMessage.ctmin, ZCL_CT_MIN, ZCL_CT_MAX)));

                    logger.warn("properties new {}", properties);
                    updateProperties(properties);
                }
            }
            return lightMessage;
        } else {
            throw new IllegalStateException("Unknown status code " + r.getResponseCode() + " for full state request");
        }
    }

    @Override
    protected void processStateResponse(@Nullable LightMessage stateResponse) {
        if (stateResponse == null) {
            return;
        }
        messageReceived(config.id, stateResponse);

        updateStatus(ThingStatus.ONLINE);
    }

    private void valueUpdated(String channelId, LightState newState) {
        Integer bri = newState.bri;
        Boolean on = newState.on;

        switch (channelId) {
            case CHANNEL_SWITCH:
                if (on != null) {
                    updateState(channelId, OnOffType.from(on));
                }
                break;
            case CHANNEL_COLOR:
                if (on != null && on == false) {
                    updateState(channelId, OnOffType.OFF);
                } else if (bri != null && newState.colormode != null && newState.colormode.equals("xy")) {
                    final double @Nullable [] xy = newState.xy;
                    if (xy != null && xy.length == 2) {
                        HSBType color = HSBType.fromXY((float) xy[0], (float) xy[1]);
                        updateState(channelId, new HSBType(color.getHue(), color.getSaturation(), toPercentType(bri)));
                    }
                } else if (bri != null && newState.hue != null && newState.sat != null) {
                    final Integer hue = newState.hue;
                    final Integer sat = newState.sat;
                    updateState(channelId,
                            new HSBType(new DecimalType(hue / HUE_FACTOR), toPercentType(sat), toPercentType(bri)));
                }
                break;
            case CHANNEL_BRIGHTNESS:
                if (bri != null && on != null && on) {
                    updateState(channelId, toPercentType(bri));
                } else {
                    updateState(channelId, OnOffType.OFF);
                }
                break;
            case CHANNEL_COLOR_TEMPERATURE:
                Integer ct = newState.ct;
                if (ct != null && ct >= ctMin && ct <= ctMax) {
                    updateState(channelId, new DecimalType(miredToKelvin(ct)));
                }
                break;
            case CHANNEL_POSITION:
                if (bri != null) {
                    updateState(channelId, toPercentType(bri));
                }
            default:
        }
    }

    @Override
    public void messageReceived(String sensorID, DeconzBaseMessage message) {
        if (message instanceof LightMessage) {
            LightMessage lightMessage = (LightMessage) message;
            logger.trace("{} received {}", thing.getUID(), lightMessage);
            LightState lightState = lightMessage.state;
            if (lightState != null) {
                if (lastCommandExpireTimestamp > System.currentTimeMillis()
                        && !lightState.equalsIgnoreNull(lastCommand)) {
                    // skip for SKIP_UPDATE_TIMESPAN after last command if lightState is different from command
                    logger.trace("Ignoring differing update after last command until {}", lastCommandExpireTimestamp);
                    return;
                }
                lightStateCache = lightState;
                thing.getChannels().stream().map(c -> c.getUID().getId()).forEach(c -> valueUpdated(c, lightState));
            }
        }
    }

    private PercentType toPercentType(int val) {
        int scaledValue = (int) Math.ceil(val / BRIGHTNESS_FACTOR);
        if (scaledValue < 0 || scaledValue > 100) {
            logger.trace("received value {} (converted to {}). Coercing.", val, scaledValue);
            scaledValue = scaledValue < 0 ? 0 : scaledValue;
            scaledValue = scaledValue > 100 ? 100 : scaledValue;
        }
        return new PercentType(scaledValue);
    }

    private int fromPercentType(PercentType val) {
        return (int) Math.floor(val.doubleValue() * BRIGHTNESS_FACTOR);
    }
}
