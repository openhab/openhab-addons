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
package org.openhab.binding.somfytahoma.internal.handler;

import static org.openhab.binding.somfytahoma.internal.SomfyTahomaBindingConstants.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaState;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SomfyTahomaBaseThingHandler} is base thing handler for all things.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public abstract class SomfyTahomaBaseThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaBaseThingHandler.class);
    private HashMap<String, Integer> typeTable = new HashMap<>();
    protected HashMap<String, String> stateNames = new HashMap<>();

    public SomfyTahomaBaseThingHandler(Thing thing) {
        super(thing);
    }

    public HashMap<String, String> getStateNames() {
        return stateNames;
    }

    private String url = "";

    @Override
    public void initialize() {
        url = getURL();
        if (getThing().getProperties().containsKey(RSSI_LEVEL_STATE)) {
            createRSSIChannel();
        }
        updateStatus(ThingStatus.ONLINE);
    }

    private void createRSSIChannel() {
        if (thing.getChannel(RSSI) == null) {
            logger.debug("{} Creating a rssi channel", url);
            createChannel(RSSI, "Number", "RSSI Level");
        }
    }

    private void createChannel(String name, String type, String label) {
        ThingBuilder thingBuilder = editThing();
        Channel channel = ChannelBuilder.create(new ChannelUID(thing.getUID(), name), type).withLabel(label).build();
        thingBuilder.withChannel(channel);
        updateThing(thingBuilder.build());
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("{} Received command {} for channel {}", url, command, channelUID);
        if (command instanceof RefreshType) {
            refresh(channelUID.getId());
        }
    }

    public Logger getLogger() {
        return logger;
    }

    protected boolean isAlwaysOnline() {
        return false;
    }

    protected @Nullable SomfyTahomaBridgeHandler getBridgeHandler() {
        return this.getBridge() != null ? (SomfyTahomaBridgeHandler) this.getBridge().getHandler() : null;
    }

    private String getURL() {
        return getThing().getConfiguration().get("url") != null ? getThing().getConfiguration().get("url").toString()
                : "";
    }

    private void setAvailable() {
        if (ThingStatus.ONLINE != thing.getStatus()) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void setUnavailable() {
        if (ThingStatus.OFFLINE != thing.getStatus() && !isAlwaysOnline()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, UNAVAILABLE);
        }
    }

    protected void sendCommand(String cmd) {
        sendCommand(cmd, "[]");
    }

    protected void sendCommand(String cmd, String param) {
        if (getBridgeHandler() != null) {
            getBridgeHandler().sendCommand(url, cmd, param);
        }
    }

    protected void refresh(String channel) {
        if (getBridgeHandler() != null && stateNames.containsKey(channel)) {
            getBridgeHandler().refresh(url, stateNames.get(channel));
        }
    }

    protected void executeActionGroup() {
        if (getBridgeHandler() != null) {
            getBridgeHandler().executeActionGroup(url);
        }
    }

    protected @Nullable String getCurrentExecutions() {
        if (getBridgeHandler() != null) {
            return getBridgeHandler().getCurrentExecutions(url);
        }
        return null;
    }

    protected void cancelExecution(String executionId) {
        if (getBridgeHandler() != null) {
            getBridgeHandler().cancelExecution(executionId);
        }
    }

    protected SomfyTahomaStatus getTahomaStatus(String id) {
        if (getBridgeHandler() != null) {
            return getBridgeHandler().getTahomaStatus(id);
        }
        return new SomfyTahomaStatus();
    }

    private void cacheStateType(SomfyTahomaState state) {
        if (state.getType() > 0 && !typeTable.containsKey(state.getName())) {
            typeTable.put(state.getName(), state.getType());
        }
    }

    protected void cacheStateType(String stateName, int type) {
        if (type > 0 && !typeTable.containsKey(stateName)) {
            typeTable.put(stateName, type);
        }
    }

    protected @Nullable State parseTahomaState(@Nullable SomfyTahomaState state) {
        return parseTahomaState(null, state);
    }

    protected @Nullable State parseTahomaState(@Nullable String acceptedState, @Nullable SomfyTahomaState state) {
        if (state == null) {
            return UnDefType.NULL;
        }

        int type = state.getType();

        try {
            if (typeTable.containsKey(state.getName())) {
                type = typeTable.get(state.getName());
            } else {
                cacheStateType(state);
            }

            if (type == 0) {
                logger.debug("{} Cannot recognize the state type for: {}!", url, state.getValue());
                return null;
            }

            logger.trace("Value to parse: {}, type: {}", state.getValue(), type);
            switch (type) {
                case TYPE_PERCENT:
                    Double valPct = Double.parseDouble(state.getValue().toString());
                    return new PercentType(valPct.intValue());
                case TYPE_DECIMAL:
                    Double valDec = Double.parseDouble(state.getValue().toString());
                    return new DecimalType(valDec);
                case TYPE_STRING:
                case TYPE_BOOLEAN:
                    String value = state.getValue().toString();
                    if ("String".equals(acceptedState)) {
                        return new StringType(value);
                    } else {
                        return parseStringState(value);
                    }
                default:
                    return null;
            }
        } catch (NumberFormatException ex) {
            logger.debug("{} Error while parsing Tahoma state! Value: {} type: {}", url, state.getValue(), type, ex);
        }
        return null;
    }

    private State parseStringState(String value) {
        if (value.endsWith("%")) {
            // convert "100%" to 100 decimal
            String val = value.replace("%", "");
            logger.trace("converting: {} to value: {}", value, val);
            Double valDec = Double.parseDouble(val);
            return new DecimalType(valDec);
        }
        switch (value.toLowerCase()) {
            case "on":
            case "true":
                return OnOffType.ON;
            case "off":
            case "false":
                return OnOffType.OFF;
            case "notdetected":
            case "nopersoninside":
            case "closed":
            case "locked":
                return OpenClosedType.CLOSED;
            case "detected":
            case "personinside":
            case "open":
            case "opened":
            case "unlocked":
                return OpenClosedType.OPEN;
            case "unknown":
                return UnDefType.UNDEF;
            default:
                logger.debug("{} Unknown thing state returned: {}", url, value);
                return UnDefType.UNDEF;
        }
    }

    public void updateThingStatus(List<SomfyTahomaState> states) {
        SomfyTahomaState state = getStatusState(states);
        updateThingStatus(state);
    }

    private @Nullable SomfyTahomaState getStatusState(List<SomfyTahomaState> states) {
        for (SomfyTahomaState state : states) {
            if (STATUS_STATE.equals(state.getName()) && state.getType() == TYPE_STRING) {
                return state;
            }
        }
        return null;
    }

    private void updateThingStatus(@Nullable SomfyTahomaState state) {
        if (state == null) {
            // Most probably we are dealing with RTS device which does not return states
            // so we have to setup ONLINE status manually
            setAvailable();
            return;
        }
        if (STATUS_STATE.equals(state.getName()) && state.getType() == TYPE_STRING) {
            if (UNAVAILABLE.equals(state.getValue())) {
                setUnavailable();
            } else {
                setAvailable();
            }
        }
    }

    public void updateThingChannels(List<SomfyTahomaState> states) {
        Map<String, String> properties = new HashMap<>();
        for (SomfyTahomaState state : states) {
            logger.trace("{} processing state: {} with value: {}", url, state.getName(), state.getValue());
            properties.put(state.getName(), state.getValue().toString());
            if (RSSI_LEVEL_STATE.equals(state.getName())) {
                // RSSI channel is a dynamic one
                updateRSSIChannel(state);
            } else {
                updateThingChannels(state);
            }
        }
        updateProperties(properties);
    }

    private void updateRSSIChannel(SomfyTahomaState state) {
        createRSSIChannel();
        Channel ch = thing.getChannel(RSSI);
        if (ch != null) {
            logger.debug("{} updating RSSI channel with value: {}", url, state.getValue());
            State newState = parseTahomaState(ch.getAcceptedItemType(), state);
            if (newState != null) {
                updateState(ch.getUID(), newState);
            }
        }
    }

    public void updateThingChannels(SomfyTahomaState state) {
        stateNames.forEach((k, v) -> {
            if (v.equals(state.getName())) {
                Channel ch = thing.getChannel(k);
                if (ch != null) {
                    logger.debug("{} updating channel: {} with value: {}", url, k, state.getValue());
                    State newState = parseTahomaState(ch.getAcceptedItemType(), state);
                    if (newState != null) {
                        updateState(ch.getUID(), newState);
                    }
                }
            }
        });
    }
}
