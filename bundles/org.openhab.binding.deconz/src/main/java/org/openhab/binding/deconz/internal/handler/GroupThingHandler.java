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

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.deconz.internal.dto.*;
import org.openhab.binding.deconz.internal.netutils.AsyncHttpClient;
import org.openhab.binding.deconz.internal.netutils.WebSocketConnection;
import org.openhab.core.library.types.*;
import org.openhab.core.thing.*;
import org.openhab.core.types.*;
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
 * in {@link #groupStateCache}. Every field that got received by the REST API for this specific
 * sensor is published to the framework.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class GroupThingHandler extends DeconzBaseThingHandler<GroupMessage> {
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Collections.singleton(THING_TYPE_LIGHTGROUP);

    private static final double HUE_FACTOR = 65535 / 360.0;
    private static final double BRIGHTNESS_FACTOR = 2.54;

    private final Logger logger = LoggerFactory.getLogger(GroupThingHandler.class);

    private long lastCommandExpireTimestamp = 0;
    private boolean needsPropertyUpdate = false;

    /**
     * The group state.
     */
    private GroupState groupStateCache = new GroupState();
    private GroupAction lastCommand = new GroupAction();

    public GroupThingHandler(Thing thing, Gson gson) {
        super(thing, gson);
    }

    @Override
    protected void registerListener() {
        WebSocketConnection conn = connection;
        if (conn != null) {
            conn.registerGroupListener(config.id, this);
        }
    }

    @Override
    protected void unregisterListener() {
        WebSocketConnection conn = connection;
        if (conn != null) {
            conn.unregisterGroupListener(config.id);
        }
    }

    @Override
    protected void requestState() {
        requestState("groups");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            valueUpdated(channelUID.getId(), groupStateCache);
            return;
        }

        GroupAction newGroupAction = new GroupAction();
        // TODO: enable actions
        /*
         * switch (channelUID.getId()) {
         * case CHANNEL_ALERT:
         * if (command instanceof OnOffType) {
         * newLightState.alert = command == OnOffType.ON ? "alert" : "none";
         * } else {
         * return;
         * }
         * case CHANNEL_SWITCH:
         * if (command instanceof OnOffType) {
         * newLightState.on = (command == OnOffType.ON);
         * } else {
         * return;
         * }
         * break;
         * case CHANNEL_BRIGHTNESS:
         * case CHANNEL_COLOR:
         * if (command instanceof OnOffType) {
         * newLightState.on = (command == OnOffType.ON);
         * } else if (command instanceof HSBType) {
         * HSBType hsbCommand = (HSBType) command;
         * 
         * if ("xy".equals(groupStateCache.colormode)) {
         * PercentType[] xy = hsbCommand.toXY();
         * if (xy.length < 2) {
         * logger.warn("Failed to convert {} to xy-values", command);
         * }
         * newLightState.xy = new double[] { xy[0].doubleValue() / 100.0, xy[1].doubleValue() / 100.0 };
         * newLightState.bri = fromPercentType(hsbCommand.getBrightness());
         * } else {
         * // default is colormode "hs" (used when colormode "hs" is set or colormode is unknown)
         * newLightState.bri = fromPercentType(hsbCommand.getBrightness());
         * newLightState.hue = (int) (hsbCommand.getHue().doubleValue() * HUE_FACTOR);
         * newLightState.sat = fromPercentType(hsbCommand.getSaturation());
         * }
         * } else if (command instanceof PercentType) {
         * newLightState.bri = fromPercentType((PercentType) command);
         * } else if (command instanceof DecimalType) {
         * newLightState.bri = ((DecimalType) command).intValue();
         * } else {
         * return;
         * }
         * 
         * // send on/off state together with brightness if not already set or unknown
         * Integer newBri = newLightState.bri;
         * if ((newBri != null) && ((currentOn == null) || ((newBri > 0) != currentOn))) {
         * newLightState.on = (newBri > 0);
         * }
         * 
         * // fix sending bri=0 when light is already off
         * if (newBri != null && newBri == 0 && currentOn != null && !currentOn) {
         * return;
         * }
         * 
         * Double transitiontime = config.transitiontime;
         * if (transitiontime != null) {
         * // value is in 1/10 seconds
         * newLightState.transitiontime = (int) Math.round(10 * transitiontime);
         * }
         * break;
         * case CHANNEL_COLOR_TEMPERATURE:
         * if (command instanceof DecimalType) {
         * int miredValue = kelvinToMired(((DecimalType) command).intValue());
         * newLightState.ct = constrainToRange(miredValue, ctMin, ctMax);
         * 
         * if (currentOn != null && !currentOn) {
         * // sending new color temperature is only allowed when light is on
         * newLightState.on = true;
         * }
         * } else {
         * return;
         * }
         * break;
         * case CHANNEL_POSITION:
         * if (command instanceof UpDownType) {
         * newLightState.on = (command == UpDownType.DOWN);
         * } else if (command == StopMoveType.STOP) {
         * if (currentOn != null && currentOn && currentBri != null && currentBri <= 254) {
         * // going down or currently stop (254 because of rounding error)
         * newLightState.on = true;
         * } else if (currentOn != null && !currentOn && currentBri != null && currentBri > 0) {
         * // going up or currently stopped
         * newLightState.on = false;
         * }
         * } else if (command instanceof PercentType) {
         * newLightState.bri = fromPercentType((PercentType) command);
         * } else {
         * return;
         * }
         * break;
         * default:
         * // no supported command
         * return;
         * }
         * 
         * AsyncHttpClient asyncHttpClient = http;
         * if (asyncHttpClient == null) {
         * return;
         * }
         * String url = buildUrl(bridgeConfig.host, bridgeConfig.httpPort, bridgeConfig.apikey, "lights", config.id,
         * "state");
         * 
         * if (newLightState.on != null && !newLightState.on) {
         * // if light shall be off, no other commands are allowed, so reset the new light state
         * newLightState.clear();
         * newLightState.on = false;
         * }
         * 
         * String json = gson.toJson(newLightState);
         * logger.trace("Sending {} to light {} via {}", json, config.id, url);
         * 
         * asyncHttpClient.put(url, json, bridgeConfig.timeout).thenAccept(v -> {
         * lastCommandExpireTimestamp = System.currentTimeMillis()
         * + (newLightState.transitiontime != null ? newLightState.transitiontime
         * : DEFAULT_COMMAND_EXPIRY_TIME);
         * lastCommand = newLightState;
         * logger.trace("Result code={}, body={}", v.getResponseCode(), v.getBody());
         * }).exceptionally(e -> {
         * logger.debug("Sending command {} to channel {} failed:", command, channelUID, e);
         * return null;
         * });
         */
    }

    @Override
    protected @Nullable GroupMessage parseStateResponse(AsyncHttpClient.Result r) {
        if (r.getResponseCode() == 403) {
            return null;
        } else if (r.getResponseCode() == 200) {
            GroupMessage groupMessage = gson.fromJson(r.getBody(), GroupMessage.class);
            return groupMessage;
        } else {
            throw new IllegalStateException("Unknown status code " + r.getResponseCode() + " for full state request");
        }
    }

    @Override
    protected void processStateResponse(@Nullable GroupMessage stateResponse) {
        if (stateResponse == null) {
            return;
        }

        messageReceived(config.id, stateResponse);
    }

    private void valueUpdated(String channelId, GroupState newState) {
        // TODO: enable value handling
        /*
         * switch (channelId) {
         * case CHANNEL_ALERT:
         * updateState(channelId, "alert".equals(newState.alert) ? OnOffType.ON : OnOffType.OFF);
         * break;
         * case CHANNEL_SWITCH:
         * if (on != null) {
         * updateState(channelId, OnOffType.from(on));
         * }
         * break;
         * case CHANNEL_COLOR:
         * if (on != null && on == false) {
         * updateState(channelId, OnOffType.OFF);
         * } else if (bri != null && newState.colormode != null && newState.colormode.equals("xy")) {
         * final double @Nullable [] xy = newState.xy;
         * if (xy != null && xy.length == 2) {
         * HSBType color = HSBType.fromXY((float) xy[0], (float) xy[1]);
         * updateState(channelId, new HSBType(color.getHue(), color.getSaturation(), toPercentType(bri)));
         * }
         * } else if (bri != null && newState.hue != null && newState.sat != null) {
         * final Integer hue = newState.hue;
         * final Integer sat = newState.sat;
         * updateState(channelId,
         * new HSBType(new DecimalType(hue / HUE_FACTOR), toPercentType(sat), toPercentType(bri)));
         * }
         * break;
         * case CHANNEL_BRIGHTNESS:
         * if (bri != null && on != null && on) {
         * updateState(channelId, toPercentType(bri));
         * } else {
         * updateState(channelId, OnOffType.OFF);
         * }
         * break;
         * case CHANNEL_COLOR_TEMPERATURE:
         * Integer ct = newState.ct;
         * if (ct != null && ct >= ctMin && ct <= ctMax) {
         * updateState(channelId, new DecimalType(miredToKelvin(ct)));
         * }
         * break;
         * case CHANNEL_POSITION:
         * if (bri != null) {
         * updateState(channelId, toPercentType(bri));
         * }
         * default:
         * }
         */
    }

    @Override
    public void messageReceived(String sensorID, DeconzBaseMessage message) {
        if (message instanceof GroupMessage) {
            GroupMessage groupMessage = (GroupMessage) message;
            logger.trace("{} received {}", thing.getUID(), groupMessage);
            GroupState groupState = groupMessage.state;
            if (groupState != null) {
                thing.getChannels().stream().map(c -> c.getUID().getId()).forEach(c -> valueUpdated(c, groupState));
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
        logger.debug("val = '{}', scaledValue = '{}'", val, scaledValue);
        return new PercentType(scaledValue);
    }

    private int fromPercentType(PercentType val) {
        return (int) Math.floor(val.doubleValue() * BRIGHTNESS_FACTOR);
    }
}
