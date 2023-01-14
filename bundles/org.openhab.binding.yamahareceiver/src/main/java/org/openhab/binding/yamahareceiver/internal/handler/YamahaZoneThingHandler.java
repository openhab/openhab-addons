/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.yamahareceiver.internal.handler;

import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.*;
import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Inputs.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.yamahareceiver.internal.ChannelsTypeProviderAvailableInputs;
import org.openhab.binding.yamahareceiver.internal.ChannelsTypeProviderPreset;
import org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Feature;
import org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Zone;
import org.openhab.binding.yamahareceiver.internal.config.YamahaZoneConfig;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.IStateUpdatable;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithNavigationControl;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithPlayControl;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithPresetControl;
import org.openhab.binding.yamahareceiver.internal.protocol.InputWithTunerBandControl;
import org.openhab.binding.yamahareceiver.internal.protocol.ProtocolFactory;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.protocol.ZoneAvailableInputs;
import org.openhab.binding.yamahareceiver.internal.protocol.ZoneControl;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.InputWithNavigationControlXML;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.InputWithPlayControlXML;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.ZoneControlXML;
import org.openhab.binding.yamahareceiver.internal.state.AvailableInputState;
import org.openhab.binding.yamahareceiver.internal.state.AvailableInputStateListener;
import org.openhab.binding.yamahareceiver.internal.state.DabBandState;
import org.openhab.binding.yamahareceiver.internal.state.DabBandStateListener;
import org.openhab.binding.yamahareceiver.internal.state.DeviceInformationState;
import org.openhab.binding.yamahareceiver.internal.state.NavigationControlState;
import org.openhab.binding.yamahareceiver.internal.state.NavigationControlStateListener;
import org.openhab.binding.yamahareceiver.internal.state.PlayInfoState;
import org.openhab.binding.yamahareceiver.internal.state.PlayInfoStateListener;
import org.openhab.binding.yamahareceiver.internal.state.PresetInfoState;
import org.openhab.binding.yamahareceiver.internal.state.PresetInfoStateListener;
import org.openhab.binding.yamahareceiver.internal.state.ZoneControlState;
import org.openhab.binding.yamahareceiver.internal.state.ZoneControlStateListener;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YamahaZoneThingHandler} is managing one zone of a Yamaha AVR.
 * It has a state consisting of the zone, the current input ID, {@link ZoneControlState}
 * and some more state objects and uses the zone control protocol
 * class {@link ZoneControlXML}, {@link InputWithPlayControlXML} and {@link InputWithNavigationControlXML}
 * for communication.
 *
 * @author David Graeff <david.graeff@web.de>
 * @author Tomasz Maruszak - [yamaha] Tuner band selection and preset feature for dual band models (RX-S601D), added
 *         config object
 */
public class YamahaZoneThingHandler extends BaseThingHandler
        implements ZoneControlStateListener, NavigationControlStateListener, PlayInfoStateListener,
        AvailableInputStateListener, PresetInfoStateListener, DabBandStateListener {

    private final Logger logger = LoggerFactory.getLogger(YamahaZoneThingHandler.class);

    private YamahaZoneConfig zoneConfig;

    /// ChannelType providers
    public @NonNullByDefault({}) ChannelsTypeProviderPreset channelsTypeProviderPreset;
    public @NonNullByDefault({}) ChannelsTypeProviderAvailableInputs channelsTypeProviderAvailableInputs;

    /// State
    protected ZoneControlState zoneState = new ZoneControlState();
    protected PresetInfoState presetInfoState = new PresetInfoState();
    protected DabBandState dabBandState = new DabBandState();
    protected PlayInfoState playInfoState = new PlayInfoState();
    protected NavigationControlState navigationInfoState = new NavigationControlState();

    /// Control
    protected ZoneControl zoneControl;
    protected InputWithPlayControl inputWithPlayControl;
    protected InputWithNavigationControl inputWithNavigationControl;
    protected ZoneAvailableInputs zoneAvailableInputs;
    protected InputWithPresetControl inputWithPresetControl;
    protected InputWithTunerBandControl inputWithDabBandControl;

    public YamahaZoneThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections
                .unmodifiableList(Stream.of(ChannelsTypeProviderAvailableInputs.class, ChannelsTypeProviderPreset.class)
                        .collect(Collectors.toList()));
    }

    /**
     * Sets the {@link DeviceInformationState} for the handler.
     */
    public DeviceInformationState getDeviceInformationState() {
        return getBridgeHandler().getDeviceInformationState();
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        validateConfigurationParameters(configurationParameters);

        Configuration configuration = editConfiguration();
        for (Entry<String, Object> configurationParameter : configurationParameters.entrySet()) {
            configuration.put(configurationParameter.getKey(), configurationParameter.getValue());
        }

        updateConfiguration(configuration);

        zoneConfig = configuration.as(YamahaZoneConfig.class);
        logger.trace("Updating configuration of {} with zone '{}'", getThing().getLabel(), zoneConfig.getZoneValue());
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

        zoneConfig = getConfigAs(YamahaZoneConfig.class);
        logger.trace("Initialize {} with zone '{}'", getThing().getLabel(), zoneConfig.getZoneValue());

        Bridge bridge = getBridge();
        initializeThing(bridge != null ? bridge.getStatus() : null);
    }

    protected YamahaBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        return (YamahaBridgeHandler) bridge.getHandler();
    }

    protected ProtocolFactory getProtocolFactory() {
        return getBridgeHandler().getProtocolFactory();
    }

    protected AbstractConnection getConnection() {
        return getBridgeHandler().getConnection();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        initializeThing(bridgeStatusInfo.getStatus());
    }

    private void initializeThing(ThingStatus bridgeStatus) {
        YamahaBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null && bridgeStatus != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                if (zoneConfig == null || zoneConfig.getZone() == null) {
                    String msg = String.format(
                            "Zone not set or invalid zone name used: '%s'. It needs to be on of: '%s'",
                            zoneConfig.getZoneValue(), Arrays.toString(Zone.values()));
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
                    logger.info("{}", msg);
                } else {
                    if (zoneControl == null) {
                        YamahaBridgeHandler brHandler = getBridgeHandler();

                        zoneControl = getProtocolFactory().ZoneControl(getConnection(), zoneConfig, this,
                                brHandler::getInputConverter, getDeviceInformationState());
                        zoneAvailableInputs = getProtocolFactory().ZoneAvailableInputs(getConnection(), zoneConfig,
                                this, brHandler::getInputConverter, getDeviceInformationState());

                        updateZoneInformation();
                    }

                    updateStatus(ThingStatus.ONLINE);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
                zoneControl = null;
                zoneAvailableInputs = null;
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    /**
     * Return true if the zone is set, and zoneControl and zoneAvailableInputs objects have been created.
     */
    boolean isCorrectlyInitialized() {
        return zoneConfig != null && zoneConfig.getZone() != null && zoneAvailableInputs != null && zoneControl != null;
    }

    /**
     * Request new zone and available input information
     */
    void updateZoneInformation() {
        updateAsyncMakeOfflineIfFail(zoneAvailableInputs);
        updateAsyncMakeOfflineIfFail(zoneControl);

        if (inputWithPlayControl != null) {
            updateAsyncMakeOfflineIfFail(inputWithPlayControl);
        }

        if (inputWithNavigationControl != null) {
            updateAsyncMakeOfflineIfFail(inputWithNavigationControl);
        }

        if (inputWithPresetControl != null) {
            updateAsyncMakeOfflineIfFail(inputWithPresetControl);
        }

        if (inputWithDabBandControl != null) {
            updateAsyncMakeOfflineIfFail(inputWithDabBandControl);
        }
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
                case CHANNEL_POWER:
                    zoneControl.setPower(((OnOffType) command) == OnOffType.ON);
                    break;
                case CHANNEL_INPUT:
                    zoneControl.setInput(((StringType) command).toString());
                    break;
                case CHANNEL_SURROUND:
                    zoneControl.setSurroundProgram(((StringType) command).toString());
                    break;
                case CHANNEL_VOLUME_DB:
                    zoneControl.setVolumeDB(((DecimalType) command).floatValue());
                    break;
                case CHANNEL_VOLUME:
                    if (command instanceof DecimalType) {
                        zoneControl.setVolume(((DecimalType) command).floatValue());
                    } else if (command instanceof IncreaseDecreaseType) {
                        zoneControl.setVolumeRelative(zoneState,
                                (((IncreaseDecreaseType) command) == IncreaseDecreaseType.INCREASE ? 1 : -1)
                                        * zoneConfig.getVolumeRelativeChangeFactor());
                    }
                    break;
                case CHANNEL_MUTE:
                    zoneControl.setMute(((OnOffType) command) == OnOffType.ON);
                    break;
                case CHANNEL_SCENE:
                    zoneControl.setScene(((StringType) command).toString());
                    break;
                case CHANNEL_DIALOGUE_LEVEL:
                    zoneControl.setDialogueLevel(((DecimalType) command).intValue());
                    break;

                case CHANNEL_HDMI1OUT:
                    zoneControl.setHDMI1Out(((OnOffType) command) == OnOffType.ON);
                    break;

                case CHANNEL_HDMI2OUT:
                    zoneControl.setHDMI2Out(((OnOffType) command) == OnOffType.ON);
                    break;

                case CHANNEL_NAVIGATION_MENU:
                    if (inputWithNavigationControl == null) {
                        logger.warn("Channel {} not working with {} input!", id, zoneState.inputID);
                        return;
                    }

                    String path = ((StringType) command).toFullString();
                    inputWithNavigationControl.selectItemFullPath(path);
                    break;

                case CHANNEL_NAVIGATION_UPDOWN:
                    if (inputWithNavigationControl == null) {
                        logger.warn("Channel {} not working with {} input!", id, zoneState.inputID);
                        return;
                    }
                    if (((UpDownType) command) == UpDownType.UP) {
                        inputWithNavigationControl.goUp();
                    } else {
                        inputWithNavigationControl.goDown();
                    }
                    break;

                case CHANNEL_NAVIGATION_LEFTRIGHT:
                    if (inputWithNavigationControl == null) {
                        logger.warn("Channel {} not working with {} input!", id, zoneState.inputID);
                        return;
                    }
                    if (((UpDownType) command) == UpDownType.UP) {
                        inputWithNavigationControl.goLeft();
                    } else {
                        inputWithNavigationControl.goRight();
                    }
                    break;

                case CHANNEL_NAVIGATION_SELECT:
                    if (inputWithNavigationControl == null) {
                        logger.warn("Channel {} not working with {} input!", id, zoneState.inputID);
                        return;
                    }
                    inputWithNavigationControl.selectCurrentItem();
                    break;

                case CHANNEL_NAVIGATION_BACK:
                    if (inputWithNavigationControl == null) {
                        logger.warn("Channel {} not working with {} input!", id, zoneState.inputID);
                        return;
                    }
                    inputWithNavigationControl.goBack();
                    break;

                case CHANNEL_NAVIGATION_BACKTOROOT:
                    if (inputWithNavigationControl == null) {
                        logger.warn("Channel {} not working with {} input!", id, zoneState.inputID);
                        return;
                    }
                    inputWithNavigationControl.goToRoot();
                    break;

                case CHANNEL_PLAYBACK_PRESET:
                    if (inputWithPresetControl == null) {
                        logger.warn("Channel {} not working with {} input!", id, zoneState.inputID);
                        return;
                    }

                    if (command instanceof DecimalType) {
                        inputWithPresetControl.selectItemByPresetNumber(((DecimalType) command).intValue());
                    } else if (command instanceof StringType) {
                        try {
                            int v = Integer.valueOf(((StringType) command).toString());
                            inputWithPresetControl.selectItemByPresetNumber(v);
                        } catch (NumberFormatException e) {
                            logger.warn("Provide a number for {}", id);
                        }
                    }
                    break;

                case CHANNEL_TUNER_BAND:
                    if (inputWithDabBandControl == null) {
                        logger.warn("Channel {} not working with {} input!", id, zoneState.inputID);
                        return;
                    }

                    if (command instanceof StringType) {
                        inputWithDabBandControl.selectBandByName(command.toString());
                    } else {
                        logger.warn("Provide a string for {}", id);
                    }
                    break;

                case CHANNEL_PLAYBACK:
                    if (inputWithPlayControl == null) {
                        logger.warn("Channel {} not working with {} input!", id, zoneState.inputID);
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
                                inputWithPlayControl.nextTrack();
                                break;
                            case "Previous":
                                inputWithPlayControl.previousTrack();
                                break;
                        }
                    }
                    break;
                default:
                    logger.warn("Channel {} not supported!", id);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (ReceivedMessageParseException e) {
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

        if (id.equals(grpZone(CHANNEL_POWER))) {
            updateState(channelUID, zoneState.power ? OnOffType.ON : OnOffType.OFF);

        } else if (id.equals(grpZone(CHANNEL_VOLUME_DB))) {
            updateState(channelUID, new DecimalType(zoneState.volumeDB));
        } else if (id.equals(grpZone(CHANNEL_VOLUME))) {
            updateState(channelUID, new PercentType((int) zoneConfig.getVolumePercentage(zoneState.volumeDB)));
        } else if (id.equals(grpZone(CHANNEL_MUTE))) {
            updateState(channelUID, zoneState.mute ? OnOffType.ON : OnOffType.OFF);
        } else if (id.equals(grpZone(CHANNEL_INPUT))) {
            updateState(channelUID, new StringType(zoneState.inputID));
        } else if (id.equals(grpZone(CHANNEL_SURROUND))) {
            updateState(channelUID, new StringType(zoneState.surroundProgram));
        } else if (id.equals(grpZone(CHANNEL_SCENE))) {
            // no state updates available
        } else if (id.equals(grpZone(CHANNEL_DIALOGUE_LEVEL))) {
            updateState(channelUID, new DecimalType(zoneState.dialogueLevel));
        } else if (id.equals(grpZone(CHANNEL_HDMI1OUT))) {
            updateState(channelUID, zoneState.hdmi1Out ? OnOffType.ON : OnOffType.OFF);
        } else if (id.equals(grpZone(CHANNEL_HDMI2OUT))) {
            updateState(channelUID, zoneState.hdmi2Out ? OnOffType.ON : OnOffType.OFF);

        } else if (id.equals(grpPlayback(CHANNEL_PLAYBACK))) {
            updateState(channelUID, new StringType(playInfoState.playbackMode));
        } else if (id.equals(grpPlayback(CHANNEL_PLAYBACK_STATION))) {
            updateState(channelUID, new StringType(playInfoState.station));
        } else if (id.equals(grpPlayback(CHANNEL_PLAYBACK_ARTIST))) {
            updateState(channelUID, new StringType(playInfoState.artist));
        } else if (id.equals(grpPlayback(CHANNEL_PLAYBACK_ALBUM))) {
            updateState(channelUID, new StringType(playInfoState.album));
        } else if (id.equals(grpPlayback(CHANNEL_PLAYBACK_SONG))) {
            updateState(channelUID, new StringType(playInfoState.song));
        } else if (id.equals(grpPlayback(CHANNEL_PLAYBACK_SONG_IMAGE_URL))) {
            updateState(channelUID, new StringType(playInfoState.songImageUrl));
        } else if (id.equals(grpPlayback(CHANNEL_PLAYBACK_PRESET))) {
            updateState(channelUID, new DecimalType(presetInfoState.presetChannel));
        } else if (id.equals(grpPlayback(CHANNEL_TUNER_BAND))) {
            updateState(channelUID, new StringType(dabBandState.band));

        } else if (id.equals(grpNav(CHANNEL_NAVIGATION_MENU))) {
            updateState(channelUID, new StringType(navigationInfoState.getCurrentItemName()));
        } else if (id.equals(grpNav(CHANNEL_NAVIGATION_LEVEL))) {
            updateState(channelUID, new DecimalType(navigationInfoState.menuLayer));
        } else if (id.equals(grpNav(CHANNEL_NAVIGATION_CURRENT_ITEM))) {
            updateState(channelUID, new DecimalType(navigationInfoState.currentLine));
        } else if (id.equals(grpNav(CHANNEL_NAVIGATION_TOTAL_ITEMS))) {
            updateState(channelUID, new DecimalType(navigationInfoState.maxLine));
        } else {
            logger.warn("Channel {} not implemented!", id);
        }
    }

    @Override
    public void zoneStateChanged(ZoneControlState msg) {
        boolean inputChanged = !msg.inputID.equals(zoneState.inputID);
        zoneState = msg;

        updateStatus(ThingStatus.ONLINE);

        updateState(grpZone(CHANNEL_POWER), zoneState.power ? OnOffType.ON : OnOffType.OFF);
        updateState(grpZone(CHANNEL_INPUT), new StringType(zoneState.inputID));
        updateState(grpZone(CHANNEL_SURROUND), new StringType(zoneState.surroundProgram));
        updateState(grpZone(CHANNEL_VOLUME_DB), new DecimalType(zoneState.volumeDB));
        updateState(grpZone(CHANNEL_VOLUME), new PercentType((int) zoneConfig.getVolumePercentage(zoneState.volumeDB)));
        updateState(grpZone(CHANNEL_MUTE), zoneState.mute ? OnOffType.ON : OnOffType.OFF);
        updateState(grpZone(CHANNEL_DIALOGUE_LEVEL), new DecimalType(zoneState.dialogueLevel));
        updateState(grpZone(CHANNEL_HDMI1OUT), zoneState.hdmi1Out ? OnOffType.ON : OnOffType.OFF);
        updateState(grpZone(CHANNEL_HDMI2OUT), zoneState.hdmi2Out ? OnOffType.ON : OnOffType.OFF);

        // If the input changed
        if (inputChanged) {
            inputChanged();
        }
    }

    /**
     * Called by {@link #zoneStateChanged(ZoneControlState)} if the input has changed.
     * Will request updates from {@see InputWithNavigationControl} and {@see InputWithPlayControl}.
     */
    private void inputChanged() {
        logger.debug("Input changed to {}", zoneState.inputID);

        if (!isInputSupported(zoneState.inputID)) {
            // for now just emit a warning in logs
            logger.warn("Input {} is not supported on your AVR model", zoneState.inputID);
        }

        inputChangedCheckForNavigationControl();
        // Note: the DAB band needs to be initialized before preset and playback
        inputChangedCheckForDabBand();
        inputChangedCheckForPlaybackControl();
        inputChangedCheckForPresetControl();
    }

    /**
     * Checks if the specified input is supported given the detected device feature information.
     *
     * @param inputID - the input name
     * @return true when input is supported
     */
    private boolean isInputSupported(String inputID) {
        switch (inputID) {
            case INPUT_SPOTIFY:
                return getDeviceInformationState().features.contains(Feature.SPOTIFY);

            case INPUT_TUNER:
                return getDeviceInformationState().features.contains(Feature.TUNER)
                        || getDeviceInformationState().features.contains(Feature.DAB);

            // Note: add more inputs here in the future
        }
        return true;
    }

    private void inputChangedCheckForNavigationControl() {
        boolean includeInputWithNavigationControl = false;

        for (String channelName : CHANNELS_NAVIGATION) {
            if (isLinked(grpNav(channelName))) {
                includeInputWithNavigationControl = true;
                break;
            }
        }

        if (includeInputWithNavigationControl) {
            includeInputWithNavigationControl = InputWithNavigationControl.SUPPORTED_INPUTS.contains(zoneState.inputID);
            if (!includeInputWithNavigationControl) {
                logger.debug("Navigation control not supported by {}", zoneState.inputID);
            }
        }

        logger.trace("Navigation control requested by channel");

        if (!includeInputWithNavigationControl) {
            inputWithNavigationControl = null;
            navigationInfoState.invalidate();
            navigationUpdated(navigationInfoState);
            return;
        }

        inputWithNavigationControl = getProtocolFactory().InputWithNavigationControl(getConnection(),
                navigationInfoState, zoneState.inputID, this, getDeviceInformationState());

        updateAsyncMakeOfflineIfFail(inputWithNavigationControl);
    }

    private void inputChangedCheckForPlaybackControl() {
        boolean includeInputWithPlaybackControl = false;

        for (String channelName : CHANNELS_PLAYBACK) {
            if (isLinked(grpPlayback(channelName))) {
                includeInputWithPlaybackControl = true;
                break;
            }
        }

        logger.trace("Playback control requested by channel");

        if (includeInputWithPlaybackControl) {
            includeInputWithPlaybackControl = InputWithPlayControl.SUPPORTED_INPUTS.contains(zoneState.inputID);
            if (!includeInputWithPlaybackControl) {
                logger.debug("Playback control not supported by {}", zoneState.inputID);
            }
        }

        if (!includeInputWithPlaybackControl) {
            inputWithPlayControl = null;
            playInfoState.invalidate();
            playInfoUpdated(playInfoState);
            return;
        }

        /**
         * The {@link inputChangedCheckForDabBand} needs to be called first before this method, in case the AVR Supports
         * DAB
         */
        if (inputWithDabBandControl != null) {
            // When input is Tuner DAB there is no playback control
            inputWithPlayControl = null;
        } else {
            inputWithPlayControl = getProtocolFactory().InputWithPlayControl(getConnection(), zoneState.inputID, this,
                    getBridgeHandler().getConfiguration(), getDeviceInformationState());

            updateAsyncMakeOfflineIfFail(inputWithPlayControl);
        }
    }

    private void inputChangedCheckForPresetControl() {
        boolean includeInput = isLinked(grpPlayback(CHANNEL_PLAYBACK_PRESET));

        logger.trace("Preset control requested by channel");

        if (includeInput) {
            includeInput = InputWithPresetControl.SUPPORTED_INPUTS.contains(zoneState.inputID);
            if (!includeInput) {
                logger.debug("Preset control not supported by {}", zoneState.inputID);
            }
        }

        if (!includeInput) {
            inputWithPresetControl = null;
            presetInfoState.invalidate();
            presetInfoUpdated(presetInfoState);
            return;
        }

        /**
         * The {@link inputChangedCheckForDabBand} needs to be called first before this method, in case the AVR Supports
         * DAB
         */
        if (inputWithDabBandControl != null) {
            // When the input is Tuner DAB the control also provides preset functionality
            inputWithPresetControl = (InputWithPresetControl) inputWithDabBandControl;
            // Note: No need to update state - it will be already called for DabBand control (see
            // inputChangedCheckForDabBand)
        } else {
            inputWithPresetControl = getProtocolFactory().InputWithPresetControl(getConnection(), zoneState.inputID,
                    this, getDeviceInformationState());

            updateAsyncMakeOfflineIfFail(inputWithPresetControl);
        }
    }

    private void inputChangedCheckForDabBand() {
        boolean includeInput = isLinked(grpPlayback(CHANNEL_TUNER_BAND));

        logger.trace("Band control requested by channel");

        if (includeInput) {
            // Check if TUNER input is DAB - dual bands radio tuner
            includeInput = InputWithTunerBandControl.SUPPORTED_INPUTS.contains(zoneState.inputID)
                    && getDeviceInformationState().features.contains(Feature.DAB);
            if (!includeInput) {
                logger.debug("Band control not supported by {}", zoneState.inputID);
            }
        }

        if (!includeInput) {
            inputWithDabBandControl = null;
            dabBandState.invalidate();
            dabBandUpdated(dabBandState);
            return;
        }

        logger.debug("InputWithTunerBandControl created for {}", zoneState.inputID);
        inputWithDabBandControl = getProtocolFactory().InputWithDabBandControl(zoneState.inputID, getConnection(), this,
                this, this, getDeviceInformationState());

        updateAsyncMakeOfflineIfFail(inputWithDabBandControl);
    }

    protected void updateAsyncMakeOfflineIfFail(IStateUpdatable stateUpdatable) {
        scheduler.submit(() -> {
            try {
                stateUpdatable.update();
            } catch (IOException e) {
                logger.debug("State update error. Changing thing to offline", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            } catch (ReceivedMessageParseException e) {
                String message = e.getMessage();
                updateProperty(PROPERTY_LAST_PARSE_ERROR, message != null ? message : "");
                // Some AVRs send unexpected responses. We log parser exceptions therefore.
                logger.debug("Parse error!", e);
            }
        });
    }

    /**
     * Once this thing is set up and the AVR is connected, the available inputs for this zone are requested.
     * The thing is updated with a new CHANNEL_AVAILABLE_INPUT which lists the available inputs for the current zone..
     */
    @Override
    public void availableInputsChanged(AvailableInputState msg) {
        // Update channel type provider with a list of available inputs
        channelsTypeProviderAvailableInputs.changeAvailableInputs(msg.availableInputs);

        // Remove the old channel and add the new channel. The channel will be requested from the
        // yamahaChannelTypeProvider.
        ChannelUID inputChannelUID = new ChannelUID(thing.getUID(), CHANNEL_GROUP_ZONE, CHANNEL_INPUT);
        Channel channel = ChannelBuilder.create(inputChannelUID, "String")
                .withType(channelsTypeProviderAvailableInputs.getChannelTypeUID()).build();
        updateThing(editThing().withoutChannel(inputChannelUID).withChannel(channel).build());
    }

    private String grpPlayback(String channelIDWithoutGroup) {
        return new ChannelUID(thing.getUID(), CHANNEL_GROUP_PLAYBACK, channelIDWithoutGroup).getId();
    }

    private String grpNav(String channelIDWithoutGroup) {
        return new ChannelUID(thing.getUID(), CHANNEL_GROUP_NAVIGATION, channelIDWithoutGroup).getId();
    }

    private String grpZone(String channelIDWithoutGroup) {
        return new ChannelUID(thing.getUID(), CHANNEL_GROUP_ZONE, channelIDWithoutGroup).getId();
    }

    @Override
    public void playInfoUpdated(PlayInfoState msg) {
        playInfoState = msg;

        updateState(grpPlayback(CHANNEL_PLAYBACK), new StringType(msg.playbackMode));
        updateState(grpPlayback(CHANNEL_PLAYBACK_STATION), new StringType(msg.station));
        updateState(grpPlayback(CHANNEL_PLAYBACK_ARTIST), new StringType(msg.artist));
        updateState(grpPlayback(CHANNEL_PLAYBACK_ALBUM), new StringType(msg.album));
        updateState(grpPlayback(CHANNEL_PLAYBACK_SONG), new StringType(msg.song));
        updateState(grpPlayback(CHANNEL_PLAYBACK_SONG_IMAGE_URL), new StringType(msg.songImageUrl));
    }

    @Override
    public void presetInfoUpdated(PresetInfoState msg) {
        presetInfoState = msg;

        if (msg.presetChannelNamesChanged) {
            msg.presetChannelNamesChanged = false;

            channelsTypeProviderPreset.changePresetNames(msg.presetChannelNames);

            // Remove the old channel and add the new channel. The channel will be requested from the
            // channelsTypeProviderPreset.
            ChannelUID inputChannelUID = new ChannelUID(thing.getUID(), CHANNEL_GROUP_PLAYBACK,
                    CHANNEL_PLAYBACK_PRESET);
            Channel channel = ChannelBuilder.create(inputChannelUID, "Number")
                    .withType(channelsTypeProviderPreset.getChannelTypeUID()).build();
            updateThing(editThing().withoutChannel(inputChannelUID).withChannel(channel).build());
        }

        updateState(grpPlayback(CHANNEL_PLAYBACK_PRESET), new DecimalType(msg.presetChannel));
    }

    @Override
    public void dabBandUpdated(DabBandState msg) {
        dabBandState = msg;
        updateState(grpPlayback(CHANNEL_TUNER_BAND), new StringType(msg.band));
    }

    @Override
    public void navigationUpdated(NavigationControlState msg) {
        navigationInfoState = msg;
        updateState(grpNav(CHANNEL_NAVIGATION_MENU), new StringType(msg.menuName));
        updateState(grpNav(CHANNEL_NAVIGATION_LEVEL), new DecimalType(msg.menuLayer));
        updateState(grpNav(CHANNEL_NAVIGATION_CURRENT_ITEM), new DecimalType(msg.currentLine));
        updateState(grpNav(CHANNEL_NAVIGATION_TOTAL_ITEMS), new DecimalType(msg.maxLine));
    }

    @Override
    public void navigationError(String msg) {
        updateProperty(PROPERTY_MENU_ERROR, msg);
        logger.warn("Navigation error: {}", msg);
    }
}
