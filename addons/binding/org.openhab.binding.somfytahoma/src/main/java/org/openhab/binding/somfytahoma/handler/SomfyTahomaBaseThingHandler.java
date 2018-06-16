/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.somfytahoma.handler;

import org.eclipse.smarthome.core.cache.ExpiringCache;
import org.eclipse.smarthome.core.library.types.*;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.somfytahoma.internal.model.SomfyTahomaState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static org.openhab.binding.somfytahoma.SomfyTahomaBindingConstants.*;

/**
 * The {@link SomfyTahomaBaseThingHandler} is base thing handler for all things.
 *
 * @author Ondrej Pecta - Initial contribution
 */
public abstract class SomfyTahomaBaseThingHandler extends BaseThingHandler implements SomfyTahomaThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SomfyTahomaBaseThingHandler.class);
    private Hashtable<String, Integer> typeTable = new Hashtable<>();

    //cache
    private ExpiringCache<List<SomfyTahomaState>> thingStates;

    public SomfyTahomaBaseThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        thingStates = new ExpiringCache<>(CACHE_EXPIRY, () -> getThingStates());

        SomfyTahomaState state = getCachedThingState(STATUS_STATE);
        updateThingStatus(state);
    }

    private synchronized SomfyTahomaState getCachedThingState(String state) {
        logger.debug("Getting cached state: {} for url: {}", state, getURL());
        List<SomfyTahomaState> states = thingStates.getValue();
        if (states != null) {
            for (SomfyTahomaState st : states) {
                if (st.getName().equals(state)) {
                    logger.debug("Returning cached value: {} for state name: {}", st.getValue(), st.getName());
                    return st;
                }
            }
        }
        return null;
    }

    protected boolean isAlwaysOnline() {
        return false;
    }

    protected List<SomfyTahomaState> getThingStates() {
        return getBridgeHandler() != null ? getBridgeHandler().getAllStates(getStateNames().values(), getURL()) : null;
    }

    private SomfyTahomaBridgeHandler getBridgeHandler() {
        return this.getBridge() != null ? (SomfyTahomaBridgeHandler) this.getBridge().getHandler() : null;
    }

    private String getURL() {
        return getThing().getConfiguration().get("url") != null ? getThing().getConfiguration().get("url").toString() : "";
    }

    private void setAvailable() {
        if (!thing.getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void setUnavailable() {
        if (!thing.getStatus().equals(ThingStatus.OFFLINE) && !isAlwaysOnline()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, UNAVAILABLE);
        }
    }

    private boolean isChannelLinked(Channel channel) {
        return isLinked(channel.getUID().getId());
    }

    protected void sendCommand(String cmd, String param) {
        if (getBridgeHandler() != null) {
            getBridgeHandler().sendCommand(getURL(), cmd, param);
        }
    }

    protected void executeActionGroup() {
        if (getBridgeHandler() != null) {
            getBridgeHandler().executeActionGroup(getURL());
        }
    }

    protected String getCurrentExecutions() {
        if (getBridgeHandler() != null) {
            return getBridgeHandler().getCurrentExecutions(getURL());
        }
        return null;
    }

    protected void cancelExecution(String executionId) {
        if (getBridgeHandler() != null) {
            getBridgeHandler().cancelExecution(executionId);
        }
    }

    protected String getTahomaVersion(String id) {
        if (getBridgeHandler() != null) {
            return getBridgeHandler().getTahomaVersion(id);
        }
        return "N/A";
    }

    protected String getTahomaStatus(String id) {
        if (getBridgeHandler() != null) {
            return getBridgeHandler().getTahomaStatus(id);
        }
        return "N/A";
    }

    private State getChannelState(Channel channel,
                                  List<SomfyTahomaState> channelStates) {
        ChannelTypeUID channelUID = channel.getChannelTypeUID();
        if (channelUID == null) {
            return null;
        }

        String stateName = getStateNames().get(channelUID.getId());
        for (SomfyTahomaState state : channelStates) {
            if (state.getName().equals(stateName)) {
                logger.trace("Parsing state for channel: {} with state name: {}", channel.getUID().getId(), state.getName());
                return parseTahomaState(channel.getAcceptedItemType(), state);
            }
        }
        return null;
    }

    private void cacheStateType(SomfyTahomaState state) {
        if (state.getType() > 0 && !typeTable.contains(state.getName())) {
            typeTable.put(state.getName(), state.getType());
        }
    }

    private State parseTahomaState(String acceptedState, SomfyTahomaState state) {

        if (state == null) {
            return UnDefType.NULL;
        }

        int type = state.getType();

        try {
            if (type == 0) {
                type = typeTable.get(state.getName());
            }

            cacheStateType(state);

            logger.trace("Value to parse: {}", state.getValue());
            switch (type) {
                case TYPE_PERCENT:
                    Double valPct = Double.parseDouble(state.getValue().toString());
                    return new PercentType(valPct.intValue());
                case TYPE_DECIMAL:
                    Double valDec = Double.parseDouble(state.getValue().toString());
                    return new DecimalType(valDec);
                case TYPE_STRING:
                    String value = state.getValue().toString().toLowerCase();
                    if (acceptedState.equals("String")) {
                        return new StringType(value);
                    } else {
                        return parseStringState(value);
                    }
                default:
                    return null;
            }
        } catch (NumberFormatException ex) {
            logger.debug("Error while parsing Tahoma state! Value: {} type: {}", state.getValue(), type, ex);
        }
        return null;
    }

    private State parseStringState(String value) {
        switch (value) {
            case "on":
                return OnOffType.ON;
            case "off":
                return OnOffType.OFF;
            case "notdetected":
            case "nopersoninside":
            case "closed":
            case "locked":
                return OpenClosedType.CLOSED;
            case "detected":
            case "personinside":
            case "open":
            case "unlocked":
                return OpenClosedType.OPEN;
            default:
                logger.debug("Unknown thing state returned: {}", value);
                return UnDefType.UNDEF;
        }
    }

    public void updateThingStatus(List<SomfyTahomaState> states) {
        SomfyTahomaState state = getStatusState(states);
        updateThingStatus(state);
    }

    private SomfyTahomaState getStatusState(List<SomfyTahomaState> states) {
        for (SomfyTahomaState state : states) {
            if (state.getName().equals(STATUS_STATE) && state.getType() == TYPE_STRING) {
                return state;
            }
        }
        return null;
    }

    private void updateThingStatus(SomfyTahomaState state) {
        if (state == null) {
            //Most probably we are dealing with RTS device which does not return states
            //so we have to setup ONLINE status manually
            setAvailable();
            return;
        }
        if (state.getName().equals(STATUS_STATE) && state.getType() == TYPE_STRING) {
            if (state.getValue().equals(UNAVAILABLE)) {
                setUnavailable();
            } else {
                setAvailable();
            }
        }
    }

    public void updateChannelState(ChannelUID channelUID) {
        String url = getURL();
        if (getStateNames() != null) {
            String stateName = getStateNames().get(channelUID.getId());
            Channel channel = getChannelByChannelUID(channelUID);
            if (channel == null) {
                return;
            }
            SomfyTahomaState tahomaState = getCachedThingState(stateName);
            State state = parseTahomaState(channel.getAcceptedItemType(), tahomaState);

            if (state.equals(UnDefType.NULL)) {
                // relogin
                getBridgeHandler().login();
                tahomaState = getCachedThingState(stateName);
                state = parseTahomaState(channel.getAcceptedItemType(), tahomaState);
            }

            updateState(channelUID, state);
        }
    }

    private Channel getChannelByChannelUID(ChannelUID channelUID) {
        if (thing.getChannel(channelUID.getId()) != null) {
            return thing.getChannel(channelUID.getId());
        }
        logger.debug("Cannot find channel for UID: {}", channelUID.getId());
        return null;
    }

    public void updateThingChannels(ArrayList<SomfyTahomaState> states) {
        // update channel values
        for (Channel channel : thing.getChannels()) {
            if (isChannelLinked(channel)) {
                State channelState = getChannelState(channel, states);
                if (channelState != null) {
                    updateState(channel.getUID(), channelState);
                } else {
                    logger.debug("Cannot find state for channel {}", channel.getUID());
                }
            }
        }
    }
}
