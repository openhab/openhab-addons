/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.handler;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.types.UpDownType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.internal.YamahaChannelsTypeProvider;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithNavigationControl;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithPlayControl;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithPlayControl.PlayControlState;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithPlayControl.PlayInfoState;
import org.openhab.binding.yamahareceiver.internal.protocol.ZoneControl;
import org.openhab.binding.yamahareceiver.internal.protocol.ZoneControl.AvailableInputState;
import org.openhab.binding.yamahareceiver.internal.protocol.ZoneControl.Listener;
import org.openhab.binding.yamahareceiver.internal.protocol.ZoneControl.State;
import org.openhab.binding.yamahareceiver.internal.protocol.ZoneControl.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * The {@link YamahaZoneThingHandler} is managing one zone of an Yamaha AVR.
 * It has a state consisting of the zone, the current input ID, {@link ZoneControl.State}
 * and some more state objects and uses the zone control protocol
 * class {@link ZoneControl}, {@link InputWithPlayControl} and {@link InputWithNavigationControl}
 * for communication.
 *
 * @author David Graeff <david.graeff@web.de>
 */
public class YamahaZoneThingHandler extends BaseThingHandler
        implements Listener, InputWithNavigationControl.Listener, InputWithPlayControl.Listener {
    private Logger logger = LoggerFactory.getLogger(YamahaZoneThingHandler.class);

    //// State
    private Zone zone;
    private String currentInputID = null;
    private ZoneControl.State zoneState = new ZoneControl.State();
    private InputWithNavigationControl.State naviState = new InputWithNavigationControl.State();
    private InputWithPlayControl.PlayControlState playControlState = new InputWithPlayControl.PlayControlState();
    private InputWithPlayControl.PlayInfoState playInfoState = new InputWithPlayControl.PlayInfoState();

    /// Control
    private ZoneControl zoneControl;
    private InputWithPlayControl inputWithPlayControl;
    private InputWithNavigationControl inputWithNavigationControl;

    private final YamahaChannelsTypeProvider yamahaChannelTypeProvider;

    public YamahaZoneThingHandler(Thing thing, YamahaChannelsTypeProvider availableInputChannelTypeProvider) {
        super(thing);
        this.yamahaChannelTypeProvider = availableInputChannelTypeProvider;
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        validateConfigurationParameters(configurationParameters);

        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParmeter : configurationParameters.entrySet()) {
            configuration.put(configurationParmeter.getKey(), configurationParmeter.getValue());
        }

        updateConfiguration(configuration);
    }

    /**
     * We handle updates of this thing ourself.
     */
    @Override
    public void thingUpdated(Thing thing) {
        this.thing = thing;
    }

    /**
     * Calls createCommunicationObject if the host name is configured correctly.
     */
    @Override
    public void initialize() {
        // Determine the zone of this thing
        String zoneName = (String) thing.getConfiguration().get(YamahaReceiverBindingConstants.CONFIG_ZONE);
        if (zoneName == null) {
            zoneName = thing.getProperties().get(YamahaReceiverBindingConstants.CONFIG_ZONE);
        }
        zone = zoneName != null ? ZoneControl.Zone.valueOf(zoneName) : null;
        if (zoneName == null || zone == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Zone not set!");
            return;
        }

        if (getBridge() != null) {
            initializeIfBridgeAvailable();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            initializeIfBridgeAvailable();
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            zoneControl = null;
        }
    }

    private void initializeIfBridgeAvailable() {
        // Do nothing if zone property is not set
        if (zone == null) {
            return;
        }
        if (zoneControl == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                return;
            }

            YamahaBridgeHandler brHandler = (YamahaBridgeHandler) bridge.getHandler();

            zoneControl = new ZoneControl(brHandler.getCommunication(), zone, this);
            try {
                zoneControl.fetchAvailableInputs();
                updateZoneInformation();
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                return;
            } catch (ParserConfigurationException | SAXException e) {
                // Some AVRs send unexpected responses. We log parser exceptions therefore.
                logger.debug("Parse error!", e);
            }
        }

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (zoneControl == null) {
            return;
        }

        String id = channelUID.getIdWithoutGroup();

        try {
            if (command instanceof RefreshType) {
                refreshFromState(channelUID);
                return;
            }

            switch (id) {
                case YamahaReceiverBindingConstants.CHANNEL_POWER:
                    zoneControl.setPower(((OnOffType) command) == OnOffType.ON);
                    break;
                case YamahaReceiverBindingConstants.CHANNEL_INPUT:
                    zoneControl.setInput(((StringType) command).toString());
                    break;
                case YamahaReceiverBindingConstants.CHANNEL_SURROUND:
                    zoneControl.setSurroundProgram(((StringType) command).toString());
                    break;
                case YamahaReceiverBindingConstants.CHANNEL_VOLUME_DB:
                    zoneControl.setVolumeDB(((DecimalType) command).floatValue());
                    break;
                case YamahaReceiverBindingConstants.CHANNEL_VOLUME:
                    if (command instanceof DecimalType) {
                        zoneControl.setVolume(((DecimalType) command).floatValue());
                    } else if (command instanceof IncreaseDecreaseType) {
                        YamahaBridgeHandler brHandler = (YamahaBridgeHandler) getBridge().getHandler();
                        float relativeVolumeChangeFactor = brHandler.getRelativeVolumeChangeFactor();
                        zoneControl.setVolumeRelative(zoneState,
                                ((IncreaseDecreaseType) command) == IncreaseDecreaseType.INCREASE
                                        ? relativeVolumeChangeFactor : -relativeVolumeChangeFactor);
                    }
                    break;
                case YamahaReceiverBindingConstants.CHANNEL_MUTE:
                    zoneControl.setMute(((OnOffType) command) == OnOffType.ON);
                    break;

                case YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_MENU:
                    if (inputWithNavigationControl == null) {
                        logger.error("Channel {} not working with this input!", id);
                        return;
                    }

                    String path = ((StringType) command).toFullString();
                    inputWithNavigationControl.selectItemFullPath(path);
                    break;

                case YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_UPDOWN:
                    if (inputWithNavigationControl == null) {
                        logger.error("Channel {} not working with this input!", id);
                        return;
                    }
                    if (((UpDownType) command) == UpDownType.UP) {
                        inputWithNavigationControl.goUp();
                    } else {
                        inputWithNavigationControl.goDown();
                    }
                    break;

                case YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_LEFTRIGHT:
                    if (inputWithNavigationControl == null) {
                        logger.error("Channel {} not working with this input!", id);
                        return;
                    }
                    if (((UpDownType) command) == UpDownType.UP) {
                        inputWithNavigationControl.goLeft();
                    } else {
                        inputWithNavigationControl.goRight();
                    }
                    break;

                case YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_SELECT:
                    if (inputWithNavigationControl == null) {
                        logger.error("Channel {} not working with this input!", id);
                        return;
                    }
                    inputWithNavigationControl.selectCurrentItem();
                    break;
                case YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_BACK:
                    if (inputWithNavigationControl == null) {
                        logger.error("Channel {} not working with this input!", id);
                        return;
                    }
                    inputWithNavigationControl.goBack();
                    break;

                case YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_BACKTOROOT:
                    if (inputWithNavigationControl == null) {
                        logger.error("Channel {} not working with this input!", id);
                        return;
                    }
                    inputWithNavigationControl.goToRoot();
                    break;

                case YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_PRESET:
                    if (inputWithPlayControl == null) {
                        logger.error("Channel {} not working with this input!", id);
                        return;
                    }

                    if (command instanceof DecimalType) {
                        inputWithPlayControl.selectItemByPresetNumber(((DecimalType) command).intValue());
                    } else if (command instanceof StringType) {
                        try {
                            int v = Integer.valueOf(((StringType) command).toString());
                            inputWithPlayControl.selectItemByPresetNumber(v);
                        } catch (NumberFormatException e) {
                            logger.warn("Provide a number for {}", id);
                        }
                    }
                    break;

                case YamahaReceiverBindingConstants.CHANNEL_PLAYBACK:
                    if (inputWithPlayControl == null) {
                        logger.error("Channel {} not working with this input!", id);
                        return;
                    }

                    if (command instanceof PlayPauseType) {
                        PlayPauseType t = ((PlayPauseType) command);
                        switch (t) {
                            case PAUSE:
                                inputWithPlayControl.pause();
                                break;
                            case PLAY:
                                inputWithPlayControl.play();
                                break;
                        }
                    } else if (command instanceof NextPreviousType) {
                        NextPreviousType t = ((NextPreviousType) command);
                        switch (t) {
                            case NEXT:
                                inputWithPlayControl.nextTrack();
                                break;
                            case PREVIOUS:
                                inputWithPlayControl.previousTrack();
                                break;
                        }
                    } else if (command instanceof DecimalType) {
                        int v = ((DecimalType) command).intValue();
                        if (v < 0) {
                            inputWithPlayControl.skipREV();
                        } else if (v > 0) {
                            inputWithPlayControl.skipFF();
                        }
                    } else if (command instanceof StringType) {
                        String v = ((StringType) command).toFullString();
                        switch (v) {
                            case "Play":
                                inputWithPlayControl.play();
                                break;
                            case "Pause":
                                inputWithPlayControl.pause();
                                break;
                            case "Stop":
                                inputWithPlayControl.stop();
                                break;
                            case "Rewind":
                                inputWithPlayControl.skipREV();
                                break;
                            case "FastForward":
                                inputWithPlayControl.skipFF();
                                break;
                            case "Next":
                                inputWithPlayControl.skipREV();
                                break;
                            case "Previous":
                                inputWithPlayControl.skipFF();
                                break;
                        }
                    }
                    break;
                default:
                    logger.error("Channel {} not supported!", id);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (ParserConfigurationException | SAXException e) {
            // Some AVRs send unexpected responses. We log parser exceptions therefore.
            logger.debug("Parse error!", e);
        }
    }

    /**
     * Called by handleCommand() if a RefreshType command was received. It will update
     * the given channel with the correct state.
     *
     * @param channelUID The channel
     */
    private void refreshFromState(ChannelUID channelUID) {
        String id = channelUID.getId();
        if (id == null) {
            return;
        }
        if (id.equals(grpZone(YamahaReceiverBindingConstants.CHANNEL_POWER))) {
            updateState(channelUID, zoneState.power ? OnOffType.ON : OnOffType.OFF);
        } else if (id.equals(grpZone(YamahaReceiverBindingConstants.CHANNEL_INPUT))) {
            updateState(channelUID, new StringType(zoneState.inputID));
        } else if (id.equals(grpZone(YamahaReceiverBindingConstants.CHANNEL_SURROUND))) {
            updateState(channelUID, new StringType(zoneState.surroundProgram));
        } else if (id.equals(grpZone(YamahaReceiverBindingConstants.CHANNEL_SURROUND))) {
            updateState(channelUID, new StringType(zoneState.surroundProgram));
        } else if (id.equals(grpZone(YamahaReceiverBindingConstants.CHANNEL_VOLUME))) {
            updateState(channelUID, new PercentType((int) zoneState.volume));
        } else if (id.equals(grpZone(YamahaReceiverBindingConstants.CHANNEL_MUTE))) {
            updateState(channelUID, zoneState.mute ? OnOffType.ON : OnOffType.OFF);

        } else if (id.equals(grpPlayback(YamahaReceiverBindingConstants.CHANNEL_PLAYBACK))) {
            updateState(channelUID, new StringType(playInfoState.playbackMode));

        } else if (id.equals(grpPlayback(YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_STATION))) {
            updateState(channelUID, new StringType(playInfoState.station));

        } else if (id.equals(grpPlayback(YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_ARTIST))) {
            updateState(channelUID, new StringType(playInfoState.artist));

        } else if (id.equals(grpPlayback(YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_ALBUM))) {
            updateState(channelUID, new StringType(playInfoState.album));

        } else if (id.equals(grpPlayback(YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_SONG))) {
            updateState(channelUID, new StringType(playInfoState.song));

        } else if (id.equals(grpPlayback(YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_PRESET))) {
            updateState(channelUID, new DecimalType(playControlState.presetChannel));

        } else if (id.equals(grpNav(YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_MENU))) {
            updateState(channelUID, new StringType(naviState.getCurrentItemName()));

        } else if (id.equals(grpNav(YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_LEVEL))) {
            updateState(channelUID, new DecimalType(naviState.menuLayer));

        } else if (id.equals(grpNav(YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_CURRENT_ITEM))) {
            updateState(channelUID, new DecimalType(naviState.currentLine));

        } else if (id.equals(grpNav(YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_TOTAL_ITEMS))) {
            updateState(channelUID, new DecimalType(naviState.maxLine));
        } else {
            logger.error("Channel {} not implemented!", id);
        }
    }

    /**
     * Call zoneControl.updateState() and will put the zone thing offline if there was a communication
     * error. This is called by the bridge periodically.
     */
    public void updateZoneInformation() {
        if (zone == null || zoneControl == null) {
            return;
        }
        try {
            zoneControl.updateState();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return;
        } catch (ParserConfigurationException | SAXException e) {
            // Some AVRs send unexpected responses. We log parser exceptions therefore.
            logger.debug("Parse error!", e);
        }
    }

    @Override
    public void zoneStateChanged(State msg) {
        zoneState = msg;
        updateStatus(ThingStatus.ONLINE);
        updateState(grpZone(YamahaReceiverBindingConstants.CHANNEL_POWER),
                zoneState.power ? OnOffType.ON : OnOffType.OFF);
        updateState(grpZone(YamahaReceiverBindingConstants.CHANNEL_INPUT), new StringType(zoneState.inputID));
        updateState(grpZone(YamahaReceiverBindingConstants.CHANNEL_SURROUND),
                new StringType(zoneState.surroundProgram));
        updateState(grpZone(YamahaReceiverBindingConstants.CHANNEL_VOLUME), new PercentType((int) zoneState.volume));
        updateState(grpZone(YamahaReceiverBindingConstants.CHANNEL_MUTE),
                zoneState.mute ? OnOffType.ON : OnOffType.OFF);
        logger.debug("Zone state updated!");

        if (zoneState.inputID == null || zoneState.inputID.isEmpty()) {
            logger.error("Expected inputID. Failed to read Input/Input_Sel_Item_Info/Src_Name");
            return;
        }

        // If the input changed
        if (!zoneState.inputID.equals(currentInputID)) {
            inputChanged();
        }
    }

    /**
     * Called by {@link zoneStateChanged} if the input has changed.
     * Will request updates from {@see InputWithNavigationControl} and {@see InputWithPlayControl}.
     */
    private void inputChanged() {
        currentInputID = zoneState.inputID;
        YamahaBridgeHandler brHandler = (YamahaBridgeHandler) getBridge().getHandler();
        logger.debug("Input changed to {}", currentInputID);

        inputChangedCheckForNavigationControl(brHandler);
        inputChangedCheckForPlaybackControl(brHandler);
    }

    private void inputChangedCheckForNavigationControl(YamahaBridgeHandler brHandler) {
        boolean includeInputWithNavigationControl = false;

        for (String channelName : YamahaReceiverBindingConstants.CHANNELS_NAVIGATION) {
            if (isLinked(grpNav(channelName))) {
                includeInputWithNavigationControl = true;
                break;
            }
        }

        if (includeInputWithNavigationControl) {
            includeInputWithNavigationControl = InputWithNavigationControl.supportedInputs.contains(currentInputID);
            if (!includeInputWithNavigationControl) {
                logger.debug("navigation control not supported by {}", currentInputID);
            }
        }

        logger.trace("navigation control requested by channel");

        if (!includeInputWithNavigationControl) {
            inputWithNavigationControl = null;
            naviState.invalidate();
            navigationUpdated(naviState);
            return;
        }

        inputWithNavigationControl = new InputWithNavigationControl(currentInputID, brHandler.getCommunication(), this);
        try {
            inputWithNavigationControl.updateNavigationState();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (ParserConfigurationException | SAXException e) {
            // Some AVRs send unexpected responses. We log parser exceptions therefore.
            logger.debug("Parse error!", e);
        }
    }

    private void inputChangedCheckForPlaybackControl(YamahaBridgeHandler brHandler) {
        boolean includeInputWithPlaybackControl = false;

        for (String channelName : YamahaReceiverBindingConstants.CHANNELS_PLAYBACK) {
            if (isLinked(grpPlayback(channelName))) {
                includeInputWithPlaybackControl = true;
                break;
            }
        }

        logger.trace("playback control requested by channel");

        if (includeInputWithPlaybackControl) {
            includeInputWithPlaybackControl = InputWithPlayControl.supportedInputs.contains(currentInputID);
            if (!includeInputWithPlaybackControl) {
                logger.debug("playback control not supported by {}", currentInputID);
            }
        }

        if (!includeInputWithPlaybackControl) {
            inputWithPlayControl = null;
            playControlState.invalidate();
            playInfoState.invalidate();
            playControlUpdated(playControlState);
            playInfoUpdated(playInfoState);
            return;
        }

        inputWithPlayControl = new InputWithPlayControl(currentInputID, brHandler.getCommunication(), this);
        try {
            inputWithPlayControl.updatePlaybackInformation();
            inputWithPlayControl.updatePresetInformation();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        } catch (ParserConfigurationException | SAXException e) {
            // Some AVRs send unexpected responses. We log parser exceptions therefore.
            logger.debug("Parse error!", e);
        }
    }

    /**
     * Once this thing is set up and the AVR is connected, the available inputs for this zone are requested.
     * The thing is updated with a new CHANNEL_AVAILABLE_INPUT which lists the available inputs for the current zone..
     */
    @Override
    public void availableInputsChanged(AvailableInputState msg) {
        // Update channel type provider with a list of available inputs
        yamahaChannelTypeProvider.changeAvailableInputs(msg.availableInputs);

        // Remove the old channel and add the new channel. The channel will be requested from the
        // yamahaChannelTypeProvider.
        ChannelUID inputChannelUID = new ChannelUID(thing.getUID(), YamahaReceiverBindingConstants.CHANNEL_GROUP_ZONE,
                YamahaReceiverBindingConstants.CHANNEL_INPUT);
        Channel channel = ChannelBuilder.create(inputChannelUID, "String")
                .withType(new ChannelTypeUID(YamahaReceiverBindingConstants.BINDING_ID,
                        YamahaReceiverBindingConstants.CHANNEL_INPUT_TYPE_AVAILABLE))
                .build();
        updateThing(editThing().withoutChannel(inputChannelUID).withChannel(channel).build());
    }

    private String grpPlayback(String channelIDWithoutGroup) {
        return new ChannelUID(thing.getUID(), YamahaReceiverBindingConstants.CHANNEL_GROUP_PLAYBACK,
                channelIDWithoutGroup).getId();
    }

    private String grpNav(String channelIDWithoutGroup) {
        return new ChannelUID(thing.getUID(), YamahaReceiverBindingConstants.CHANNEL_GROUP_NAVIGATION,
                channelIDWithoutGroup).getId();
    }

    private String grpZone(String channelIDWithoutGroup) {
        return new ChannelUID(thing.getUID(), YamahaReceiverBindingConstants.CHANNEL_GROUP_ZONE, channelIDWithoutGroup)
                .getId();
    }

    @Override
    public void playInfoUpdated(PlayInfoState msg) {
        playInfoState = msg;
        updateState(grpPlayback(YamahaReceiverBindingConstants.CHANNEL_PLAYBACK), new StringType(msg.playbackMode));
        updateState(grpPlayback(YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_STATION), new StringType(msg.station));
        updateState(grpPlayback(YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_ARTIST), new StringType(msg.artist));
        updateState(grpPlayback(YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_ALBUM), new StringType(msg.album));
        updateState(grpPlayback(YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_SONG), new StringType(msg.song));
    }

    @Override
    public void playControlUpdated(PlayControlState msg) {
        playControlState = msg;
        if (msg.presetChannelNamesChanged) {
            msg.presetChannelNamesChanged = false;
            yamahaChannelTypeProvider.changePresetNames(msg.presetChannelNames);

            // Remove the old channel and add the new channel. The channel will be requested from the
            // yamahaChannelTypeProvider.
            ChannelUID inputChannelUID = new ChannelUID(thing.getUID(),
                    YamahaReceiverBindingConstants.CHANNEL_GROUP_PLAYBACK,
                    YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_PRESET);
            Channel channel = ChannelBuilder.create(inputChannelUID, "Number")
                    .withType(new ChannelTypeUID(YamahaReceiverBindingConstants.BINDING_ID,
                            YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_PRESET_TYPE_NAMED))
                    .build();
            updateThing(editThing().withoutChannel(inputChannelUID).withChannel(channel).build());
        }

        updateState(grpPlayback(YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_PRESET),
                new DecimalType(msg.presetChannel));
    }

    @Override
    public void navigationUpdated(InputWithNavigationControl.State msg) {
        naviState = msg;
        updateState(grpNav(YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_MENU), new StringType(msg.menuName));
        updateState(grpNav(YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_LEVEL), new DecimalType(msg.menuLayer));
        updateState(grpNav(YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_CURRENT_ITEM),
                new DecimalType(msg.currentLine));
        updateState(grpNav(YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_TOTAL_ITEMS),
                new DecimalType(msg.maxLine));
    }

    @Override
    public void navigationError(String msg) {
        logger.warn("Navigation error: {}", msg);
    }
}
