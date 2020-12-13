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
package org.openhab.binding.clearone.internal.handler;

import static org.openhab.binding.clearone.internal.ClearOneBindingConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.openhab.binding.clearone.internal.ClearOneDynamicConfigOptionProvider;
import org.openhab.binding.clearone.internal.ClearOneDynamicStateDescriptionProvider;
import org.openhab.binding.clearone.internal.Message;
import org.openhab.binding.clearone.internal.config.ZoneConfiguration;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a class for handling a Zone type Thing.
 *
 * @author Garry Mitchell - Initial Contribution
 */
public class ClearOneZoneHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ClearOneZoneHandler.class);

    /** Bridge Handler for the Thing. */
    public ClearOneUnitHandler bridgeHandler = null;

    /** Alarm Properties. */

    private boolean thingHandlerInitialized = false;

    /** Zone Number. */
    private int zone;

    private int channels;

    private int maxInputs;
    private char maxProcessing;

    private List<String> selectableInputs;

    ArrayList<StateOption> sources = new ArrayList<StateOption>();
    private ClearOneDynamicStateDescriptionProvider stateDescriptionProvider;

    ArrayList<ParameterOption> sourcesConfig = new ArrayList<ParameterOption>();
    private ClearOneDynamicConfigOptionProvider configOptionProvider;

    /**
     * Constructor.
     *
     * @param thing
     * @param stateDescriptionProvider
     * @param configOptionProvider
     */
    public ClearOneZoneHandler(Thing thing, ClearOneDynamicStateDescriptionProvider stateDescriptionProvider,
            ClearOneDynamicConfigOptionProvider configOptionProvider) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.configOptionProvider = configOptionProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Thing handler - Thing ID: {}.", this.getThing().getUID());

        getBridgeHandler();

        getConfiguration();

        // set the Thing offline for now
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void dispose() {
        logger.debug("Thing {} disposed.", getThing().getUID());

        this.setThingHandlerInitialized(false);

        super.dispose();
    }

    /**
     * Method to Initialize Thing Handler.
     */
    public void initializeThingHandler() {
        if (getBridgeHandler() != null) {
            if (getThing().getStatus().equals(ThingStatus.ONLINE)) {
                Thing thing = getThing();
                List<Channel> channels = thing.getChannels();
                logger.debug("initializeThingHandler(): Initialize Thing Handler - {}", thing.getUID());

                maxInputs = 8;
                maxProcessing = 'H';
                if (bridgeHandler.getTypeId().equals(XAP_UNIT_TYPE_XAP800)) {
                    maxInputs = 12;
                    maxProcessing = 'D';
                }

                for (Channel channel : channels) {
                    updateChannel(channel.getUID(), 0);
                }

                this.setThingHandlerInitialized(true);

                refreshZoneChannels();

                logger.debug("initializeThingHandler(): Thing Handler Initialized - {}", thing.getUID());
            } else {
                logger.debug("initializeThingHandler(): Thing '{}' Unable To Initialize Thing Handler!: Status - {}",
                        thing.getUID(), thing.getStatus());
            }
        }
    }

    /**
     * Get the Bridge Handler.
     *
     * @return bridgeHandler
     */
    public synchronized ClearOneUnitHandler getBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();

            if (bridge == null) {
                logger.warn("getBridgeHandler(): Unable to get bridge!");
                return null;
            }

            logger.debug("getBridgeHandler(): Bridge for '{}' - '{}'", getThing().getUID(), bridge.getUID());

            ThingHandler handler = bridge.getHandler();

            if (handler instanceof ClearOneUnitHandler) {
                this.bridgeHandler = (ClearOneUnitHandler) handler;
            } else {
                logger.warn("getBridgeHandler(): Unable to get bridge handler!");
            }
        }

        return this.bridgeHandler;
    }

    public void updateChannel(ChannelUID channelUID, Object state) {
        logger.debug("updateChannel(): Zone Channel UID: {} {}", channelUID, state);

        OnOffType onOffType;

        if (channelUID != null) {
            int percentRange = 0;
            switch (channelUID.getId()) {
                case VOLUME:
                    percentRange = 100;
                    break;
                case MUTE:
                    onOffType = (Integer.parseInt(state.toString()) > 0) ? OnOffType.ON : OnOffType.OFF;
                    updateState(channelUID, onOffType);
                    break;
                case SOURCE:
                    updateState(channelUID, new StringType(state.toString()));
                    break;
                default:
                    logger.debug("updateChannel(): Zone Channel not updated - {}.", channelUID);
                    break;
            }
            if (percentRange != 0) {
                updateState(channelUID, new PercentType(
                        (int) Math.round((Integer.parseInt(state.toString())) / (double) percentRange * 100)));
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand(): Command Received - {} {}.", channelUID, command);

        if (command instanceof RefreshType) {
            if (!getThing().getStatus().equals(ThingStatus.ONLINE)) {
                if (getBridgeHandler() != null && bridgeHandler.isConnected()) {
                    updateStatus(ThingStatus.ONLINE);
                    this.initializeThingHandler();
                }
            } else {
                if (getBridgeHandler() != null && bridgeHandler.isConnected()) {
                    switch (channelUID.getId()) {
                        case SOURCE:
                            for (int input = 1; input <= maxInputs; input++) {
                                bridgeHandler.sendCommand(XAP_CMD_MTRX, String.format("%d I %d O", input, zone));
                            }
                            for (char expansion = 'O'; expansion <= 'Z'; expansion++) {
                                bridgeHandler.sendCommand(XAP_CMD_MTRX, String.format("%s E %d O", expansion, zone));
                            }
                            for (char processing = 'A'; processing <= maxProcessing; processing++) {
                                bridgeHandler.sendCommand(XAP_CMD_MTRX, String.format("%s P %d O", processing, zone));
                            }
                            break;
                        case VOLUME:
                            bridgeHandler.sendCommand(XAP_CMD_GAIN, String.format("%d O", zone));
                            break;
                        case MUTE:
                            bridgeHandler.sendCommand(XAP_CMD_MUTE, String.format("%d O", zone));
                            break;
                    }
                }
            }
            return;
        }

        if (bridgeHandler != null && bridgeHandler.isConnected()) {
            String ampCommand = "";
            String params = "";
            int value = 0;
            switch (channelUID.getId()) {
                case SOURCE:
                    changeSource(command.toString());
                    break;
                case VOLUME:
                    ampCommand = XAP_CMD_GAIN;
                    value = ((DecimalType) command).intValue();
                    for (int channel = zone; channel < zone + channels; channel++) {
                        params = String.format("%d O %s A", channel, String
                                .valueOf(roundToHalf(((DecimalType) command).floatValue() / 100.0 * 85.0 - 65.0)));
                        bridgeHandler.sendCommand(ampCommand, params);
                    }
                    updateChannel(channelUID, value);
                    break;
                case MUTE:
                    ampCommand = XAP_CMD_MUTE;
                    value = ((OnOffType) command == OnOffType.ON) ? 1 : 0;
                    if (command instanceof OnOffType) {
                        for (int channel = zone; channel < zone + channels; channel++) {
                            params = String.format("%d O %s", channel,
                                    ((OnOffType) command == OnOffType.ON) ? "1" : "0");
                            bridgeHandler.sendCommand(ampCommand, params);
                        }
                        updateChannel(channelUID, value);
                    }
                    break;
            }
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(bridgeStatusInfo.getStatus());
            this.initializeThingHandler();
        } else {
            this.setThingHandlerInitialized(false);
        }

        logger.debug("bridgeStatusChanged(): Bridge Status: '{}' - Thing '{}' Status: '{}'!", bridgeStatusInfo,
                getThing().getUID(), getThing().getStatus());
    }

    /**
     * Get the thing configuration.
     */
    private void getConfiguration() {
        ZoneConfiguration zoneConfiguration = getConfigAs(ZoneConfiguration.class);
        setZone(zoneConfiguration.zone.intValue());
        setChannels(zoneConfiguration.channels.intValue());
        setSelectableInputs(zoneConfiguration.selectableInputs);
        updateSourceConfigOptions();
    }

    /**
     * Get Zone.
     *
     * @return zone
     */
    public int getZone() {
        return zone;
    }

    /**
     * Set Zone.
     *
     * @param zone
     */
    public void setZone(int zone) {
        this.zone = zone;
    }

    /**
     * Get Selectable Sources.
     *
     * @return selectableInputs
     */
    public List<String> getSelectableInputs() {
        updateSourceConfigOptions();
        return this.selectableInputs;
    }

    /**
     * Set Selectable Sources.
     *
     * @param selectableInputs
     */
    public void setSelectableInputs(List<String> sources) {
        this.selectableInputs = sources;
    }

    /**
     * Get Channel by ChannelUID.
     *
     * @param channelUID
     */
    public Channel getChannel(ChannelUID channelUID) {
        Channel channel = null;

        List<Channel> channels = getThing().getChannels();

        for (Channel ch : channels) {
            if (channelUID == ch.getUID()) {
                channel = ch;
                break;
            }
        }

        return channel;
    }

    /**
     * Get Thing Handler refresh status.
     *
     * @return thingRefresh
     */
    public boolean isThingHandlerInitialized() {
        return thingHandlerInitialized;
    }

    /**
     * Set Thing Handler refresh status.
     * 
     * @param refreshed
     */
    public void setThingHandlerInitialized(boolean refreshed) {
        this.thingHandlerInitialized = refreshed;
    }

    /**
     * A serial event has been passed from the Unit
     * 
     * @param message
     * @param thing
     */
    public void eventReceived(Message message, Thing thing) {
        if (thing != null) {
            if (getThing().equals(thing)) {
                ChannelUID channelUID = null;

                String[] data = message.data.split("\\s+");

                switch (message.commandId) {
                    case XAP_CMD_GAIN:
                        channelUID = new ChannelUID(getThing().getUID(), VOLUME);
                        int volume = (int) (((Double.parseDouble(data[2]) + 65.0) / 85.0) * 100.0);
                        updateChannel(channelUID, volume);
                        break;
                    case XAP_CMD_MUTE:
                        channelUID = new ChannelUID(getThing().getUID(), MUTE);
                        updateChannel(channelUID, Integer.valueOf(data[2]));
                        break;
                    case XAP_CMD_MTRX:
                        channelUID = new ChannelUID(getThing().getUID(), SOURCE);
                        String source = String.format("%s %s", data[0], data[1]);
                        if ((selectableInputs.contains(source))
                                && (Integer.valueOf(data[4]) != XAP_MTRX_MODE_CROSSPOINT_OFF)) {
                            updateChannel(channelUID, source);
                        }
                        break;
                    case XAP_CMD_LABEL:
                        break;
                    default:
                        return;
                }
            }
        }
    }

    /**
     * Refresh all Unit specific channels direct from the unit
     */
    public void refreshZoneChannels() {
        bridgeHandler.sendCommand(XAP_CMD_MUTE, String.format("%d O", zone));
        bridgeHandler.sendCommand(XAP_CMD_GAIN, String.format("%d O", zone));
        for (int input = 1; input <= maxInputs; input++) {
            bridgeHandler.sendCommand(XAP_CMD_MTRX, String.format("%d I %d O", input, zone));
        }
        for (char expansion = 'O'; expansion <= 'Z'; expansion++) {
            bridgeHandler.sendCommand(XAP_CMD_MTRX, String.format("%s E %d O", expansion, zone));
        }
        for (char processing = 'A'; processing <= maxProcessing; processing++) {
            bridgeHandler.sendCommand(XAP_CMD_MTRX, String.format("%s P %d O", processing, zone));
        }
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), SOURCE);
        if (isLinked(channelUID)) {
            updateSourceStateOptions(channelUID);
        }
    }

    private static double roundToHalf(double d) {
        return Math.round(d * 2) / 2.0;
    }

    /**
     * Changes current source to the specified one
     * 
     * @param source
     */
    public void changeSource(String source) {
        if (selectableInputs.contains(source)) {
            ChannelUID channelUID = new ChannelUID(getThing().getUID(), SOURCE);

            int mixMode;
            String params = "";
            mixMode = XAP_MTRX_MODE_CROSSPOINT_OFF;
            String[] data = source.split("\\s+"); // data[0] Input (1-12, A-H, O-Z); data[1] Group (I, E, P)

            // Disable existing source
            for (String selInput : selectableInputs) {
                if (!selInput.equals(XAP_SOURCE_NONE)) {
                    int index = bridgeHandler.getIndexOfSource(selInput);
                    for (int i = 0; i < channels; i++) {
                        params = String.format("%s %d O %d", bridgeHandler.sources.get(index + i).getValue(), zone + i,
                                mixMode);
                        bridgeHandler.sendCommand(XAP_CMD_MTRX, params);
                    }
                }
            }

            // Set new source
            if (!source.equals(XAP_SOURCE_NONE)) {
                mixMode = data[1].contentEquals("I") ? XAP_MTRX_MODE_NON_GATED : XAP_MTRX_MODE_CROSSPOINT_ON;
                if (data[1].contentEquals("I")) {
                    mixMode = XAP_MTRX_MODE_NON_GATED;
                } else {
                    mixMode = XAP_MTRX_MODE_CROSSPOINT_ON;
                }
                int index = bridgeHandler.getIndexOfSource(source);
                for (int i = 0; i < channels; i++) {
                    params = String.format("%s %d O %d", bridgeHandler.sources.get(index + i).getValue(), zone + i,
                            mixMode);
                    bridgeHandler.sendCommand(XAP_CMD_MTRX, params);
                }
                updateChannel(channelUID, source);
            }
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        // can be overridden by subclasses
        // standard behavior is to refresh the linked channel,
        // so the newly linked items will receive a state update.
        switch (channelUID.getId()) {
            case SOURCE:
                updateSourceStateOptions(channelUID);
        }
        handleCommand(channelUID, RefreshType.REFRESH);
    }

    private void updateSourceStateOptions(ChannelUID channelUID) {
        this.sources.clear();
        if (getBridgeHandler() != null) {
            this.sources.addAll(bridgeHandler.sources);
        }
        ArrayList<StateOption> sourcesToRemove = new ArrayList<StateOption>();
        for (StateOption source : sources) {
            if ((selectableInputs != null) && !selectableInputs.contains(source.getValue())) {
                int index = getIndexOfSource(source.getValue());
                if (index >= 0) {
                    sourcesToRemove.add(source);
                }
            }
        }
        sources.removeAll(sourcesToRemove);
        stateDescriptionProvider.setStateOptions(channelUID, sources);
    }

    private void updateSourceConfigOptions() {
        this.sourcesConfig.clear();
        if (bridgeHandler != null) {
            for (StateOption source : bridgeHandler.sources) {
                sourcesConfig.add(new ParameterOption(source.getValue(), source.getLabel()));
            }
        }
        configOptionProvider.setParameterOptions(ZoneConfiguration.SELECTABLE_INPUTS, sourcesConfig);
    }

    public void updateUnitSourceNames() {
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), SOURCE);
        if (isLinked(channelUID)) {
            updateSourceStateOptions(channelUID);
        }
        updateSourceConfigOptions();
    }

    public int getChannels() {
        return channels;
    }

    public void setChannels(int channels) {
        this.channels = channels;
    }

    private int getIndexOfSource(String input) {
        for (StateOption source : sources) {
            String value = source.getValue();
            if (value.equals(input)) {
                return sources.indexOf(source);
            }
        }
        return -1;
    }
}
