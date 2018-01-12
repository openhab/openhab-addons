/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.handler;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

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
import org.eclipse.smarthome.core.thing.type.ChannelTypeProvider;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.internal.ChannelsTypeProviderAvailableInputs;
import org.openhab.binding.yamahareceiver.internal.ChannelsTypeProviderPreset;
import org.openhab.binding.yamahareceiver.internal.protocol.*;
import org.openhab.binding.yamahareceiver.internal.protocol.AbstractConnection;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.InputWithNavigationControlXML;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.InputWithPlayControlXML;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.ZoneControlXML;
import org.openhab.binding.yamahareceiver.internal.state.*;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link YamahaZoneThingHandler} is managing one zone of an Yamaha AVR.
 * It has a state consisting of the zone, the current input ID, {@link ZoneControlState}
 * and some more state objects and uses the zone control protocol
 * class {@link ZoneControlXML}, {@link InputWithPlayControlXML} and {@link InputWithNavigationControlXML}
 * for communication.
 *
 * @author David Graeff <david.graeff@web.de>
 * @author Tomasz Maruszak - [yamaha] Tuner band selection and preset feature for dual band models (RX-S601D)
 */
public class YamahaZoneThingHandler extends BaseThingHandler implements ZoneControlStateListener,
        NavigationControlStateListener, PlayInfoStateListener, AvailableInputStateListener, PresetInfoStateListener,
        DabBandStateListener {
    private Logger logger = LoggerFactory.getLogger(YamahaZoneThingHandler.class);

    /// ChannelType providers
    protected ChannelsTypeProviderPreset channelsTypeProviderPreset;
    protected ChannelsTypeProviderAvailableInputs channelsTypeProviderAvailableInputs;
    private ServiceRegistration<?> servicePreset;
    private ServiceRegistration<?> serviceAvailableInputs;

    /// State
    protected DeviceInformationState deviceInformationState;
    protected YamahaReceiverBindingConstants.Zone zone;
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
    protected InputWithDabBandControl inputWithDabBandControl;

    public YamahaZoneThingHandler(Thing thing) {
        super(thing);
    }

    /**
     * Sets the {@link DeviceInformationState} for the handler.
     */
    public void setDeviceInformationState(DeviceInformationState deviceInformationState) {
        this.deviceInformationState = deviceInformationState;
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
        zone = zoneName != null ? YamahaReceiverBindingConstants.Zone.valueOf(zoneName) : null;
        if (zoneName == null || zone == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Zone not set!");
            return;
        }

        channelsTypeProviderPreset = new ChannelsTypeProviderPreset(thing.getUID());
        channelsTypeProviderAvailableInputs = new ChannelsTypeProviderAvailableInputs(thing.getUID());
        // Allow bundleContext to be null for tests
        if (bundleContext != null) {
            servicePreset = bundleContext.registerService(ChannelTypeProvider.class.getName(),
                    channelsTypeProviderPreset, new Hashtable<>());
            serviceAvailableInputs = bundleContext.registerService(ChannelTypeProvider.class.getName(),
                    channelsTypeProviderAvailableInputs, new Hashtable<>());
        }

        YamahaBridgeHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler != null) {
            bridgeStatusChanged(bridgeHandler.getThing().getStatusInfo());
        }
    }

    @Override
    public void dispose() {
        if (serviceAvailableInputs != null) {
            serviceAvailableInputs.unregister();
            channelsTypeProviderAvailableInputs = null;
            serviceAvailableInputs = null;
        }
        if (servicePreset != null) {
            servicePreset.unregister();
            channelsTypeProviderPreset = null;
            servicePreset = null;
        }
    }

    protected YamahaBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        return (YamahaBridgeHandler) bridge.getHandler();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            if (zoneControl == null) {
                YamahaBridgeHandler brHandler = getBridgeHandler();
                zoneControl = ProtocolFactory.ZoneControl(brHandler.getCommunication(), zone, this);
                zoneAvailableInputs = ProtocolFactory.ZoneAvailableInputs(brHandler.getCommunication(), zone, this);

                updateZoneInformation();
            }

            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            zoneControl = null;
            zoneAvailableInputs = null;
        }
    }

    /**
     * Return true if the zone is set, and zoneControl and zoneAvailableInputs objects have been created.
     */
    boolean isCorrectlyInitialized() {
        return zone != null && zoneAvailableInputs != null && zoneControl != null;
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
                                        ? relativeVolumeChangeFactor
                                        : -relativeVolumeChangeFactor);
                    }
                    break;
                case YamahaReceiverBindingConstants.CHANNEL_MUTE:
                    zoneControl.setMute(((OnOffType) command) == OnOffType.ON);
                    break;

                case YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_MENU:
                    if (inputWithNavigationControl == null) {
                        logger.warn("Channel {} not working with this input!", id);
                        return;
                    }

                    String path = ((StringType) command).toFullString();
                    inputWithNavigationControl.selectItemFullPath(path);
                    break;

                case YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_UPDOWN:
                    if (inputWithNavigationControl == null) {
                        logger.warn("Channel {} not working with this input!", id);
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
                        logger.warn("Channel {} not working with this input!", id);
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
                        logger.warn("Channel {} not working with this input!", id);
                        return;
                    }
                    inputWithNavigationControl.selectCurrentItem();
                    break;

                case YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_BACK:
                    if (inputWithNavigationControl == null) {
                        logger.warn("Channel {} not working with this input!", id);
                        return;
                    }
                    inputWithNavigationControl.goBack();
                    break;

                case YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_BACKTOROOT:
                    if (inputWithNavigationControl == null) {
                        logger.warn("Channel {} not working with this input!", id);
                        return;
                    }
                    inputWithNavigationControl.goToRoot();
                    break;

                case YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_PRESET:
                    if (inputWithPresetControl == null) {
                        logger.warn("Channel {} not working with this input!", id);
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

                case YamahaReceiverBindingConstants.CHANNEL_TUNER_BAND:
                    if (inputWithDabBandControl == null) {
                        logger.warn("Channel {} not working with this input!", id);
                        return;
                    }

                    if (command instanceof StringType) {
                        inputWithDabBandControl.selectBandByName(command.toString());
                    } else {
                        logger.warn("Provide a string for {}", id);
                    }
                    break;

                case YamahaReceiverBindingConstants.CHANNEL_PLAYBACK:
                    if (inputWithPlayControl == null) {
                        logger.warn("Channel {} not working with this input!", id);
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
            updateState(channelUID, new DecimalType(presetInfoState.presetChannel));

        } else if (id.equals(grpPlayback(YamahaReceiverBindingConstants.CHANNEL_TUNER_BAND))) {
            updateState(channelUID, new StringType(dabBandState.band));

        } else if (id.equals(grpNav(YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_MENU))) {
            updateState(channelUID, new StringType(navigationInfoState.getCurrentItemName()));

        } else if (id.equals(grpNav(YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_LEVEL))) {
            updateState(channelUID, new DecimalType(navigationInfoState.menuLayer));

        } else if (id.equals(grpNav(YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_CURRENT_ITEM))) {
            updateState(channelUID, new DecimalType(navigationInfoState.currentLine));

        } else if (id.equals(grpNav(YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_TOTAL_ITEMS))) {
            updateState(channelUID, new DecimalType(navigationInfoState.maxLine));
        } else {
            logger.error("Channel {} not implemented!", id);
        }
    }

    @Override
    public void zoneStateChanged(ZoneControlState msg) {
        boolean inputChanged = !msg.inputID.equals(zoneState.inputID);
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

        AbstractConnection connection = getBridgeHandler().getCommunication();
        inputChangedCheckForNavigationControl(connection);
        // Note: the DAB band needs to be initialized before preset and playback
        inputChangedCheckForDabBand(connection);
        inputChangedCheckForPlaybackControl(connection);
        inputChangedCheckForPresetControl(connection);
    }

    /**
     * Checks if the specified input is supported given the detected device feature information.
     * @param inputID - the input name
     * @return true when input is supported
     */
    private boolean isInputSupported(String inputID) {
        switch (inputID) {
            case YamahaReceiverBindingConstants.INPUT_SPOTIFY:
                return deviceInformationState.supportSpotify;

            case YamahaReceiverBindingConstants.INPUT_TUNER:
                return deviceInformationState.supportTuner || deviceInformationState.supportDAB;

            // Note: add more inputs here in the future
        }
        return true;
    }

    private void inputChangedCheckForNavigationControl(AbstractConnection connection) {
        boolean includeInputWithNavigationControl = false;

        for (String channelName : YamahaReceiverBindingConstants.CHANNELS_NAVIGATION) {
            if (isLinked(grpNav(channelName))) {
                includeInputWithNavigationControl = true;
                break;
            }
        }

        if (includeInputWithNavigationControl) {
            includeInputWithNavigationControl = InputWithNavigationControl.SUPPORTED_INPUTS.contains(zoneState.inputID);
            if (!includeInputWithNavigationControl) {
                logger.debug("navigation control not supported by {}", zoneState.inputID);
            }
        }

        logger.trace("navigation control requested by channel");

        if (!includeInputWithNavigationControl) {
            inputWithNavigationControl = null;
            navigationInfoState.invalidate();
            navigationUpdated(navigationInfoState);
            return;
        }

        inputWithNavigationControl = ProtocolFactory.InputWithNavigationControl(connection, navigationInfoState,
                zoneState.inputID, this);

        updateAsyncMakeOfflineIfFail(inputWithNavigationControl);
    }

    private void inputChangedCheckForPlaybackControl(AbstractConnection connection) {
        boolean includeInputWithPlaybackControl = false;

        for (String channelName : YamahaReceiverBindingConstants.CHANNELS_PLAYBACK) {
            if (isLinked(grpPlayback(channelName))) {
                includeInputWithPlaybackControl = true;
                break;
            }
        }

        logger.trace("playback control requested by channel");

        if (includeInputWithPlaybackControl) {
            includeInputWithPlaybackControl = InputWithPlayControl.SUPPORTED_INPUTS.contains(zoneState.inputID);
            if (!includeInputWithPlaybackControl) {
                logger.debug("playback control not supported by {}", zoneState.inputID);
            }
        }

        if (!includeInputWithPlaybackControl) {
            inputWithPlayControl = null;
            playInfoState.invalidate();
            playInfoUpdated(playInfoState);
            return;
        }

        /**
         * The {@link inputChangedCheckForDabBand} needs to be called first before this method, in case the AVR Supports DAB
         */
        if (inputWithDabBandControl != null) {
            // When input is Tuner DAB there is no playback control
            inputWithPlayControl = null;
        } else {
            inputWithPlayControl = ProtocolFactory.InputWithPlayControl(connection, zoneState.inputID, this);

            updateAsyncMakeOfflineIfFail(inputWithPlayControl);
        }
    }
    private void inputChangedCheckForPresetControl(AbstractConnection connection) {
        boolean includeInput = isLinked(grpPlayback(YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_PRESET));

        logger.trace("preset control requested by channel");

        if (includeInput) {
            includeInput = InputWithPresetControl.SUPPORTED_INPUTS.contains(zoneState.inputID);
            if (!includeInput) {
                logger.debug("preset control not supported by {}", zoneState.inputID);
            }
        }

        if (!includeInput) {
            inputWithPresetControl = null;
            presetInfoState.invalidate();
            presetInfoUpdated(presetInfoState);
            return;
        }

        /**
         * The {@link inputChangedCheckForDabBand} needs to be called first before this method, in case the AVR Supports DAB
         */
        if (inputWithDabBandControl != null) {
            // When the input is Tuner DAB the control also provides preset functionality
            inputWithPresetControl = (InputWithPresetControl) inputWithDabBandControl;
            // Note: No need to update state - it will be already called for DabBand control (see inputChangedCheckForDabBand)
        } else {
            inputWithPresetControl = ProtocolFactory.InputWithPresetControl(connection, zoneState.inputID, this);

            updateAsyncMakeOfflineIfFail(inputWithPresetControl);
        }
    }

    private void inputChangedCheckForDabBand(AbstractConnection connection) {
        boolean includeInput = isLinked(grpPlayback(YamahaReceiverBindingConstants.CHANNEL_TUNER_BAND));

        logger.trace("InputWithDabBandControl requested by channel");

        if (includeInput) {
            // Check if TUNER input is DAB - dual bands radio tuner
            includeInput = InputWithDabBandControl.SUPPORTED_INPUTS.contains(zoneState.inputID)
                            && deviceInformationState.supportDAB;
            if (!includeInput) {
                logger.debug("band control not supported by {}", zoneState.inputID);
            }
        }

        if (!includeInput) {
            inputWithDabBandControl = null;
            dabBandState.invalidate();
            dabBandUpdated(dabBandState);
            return;
        }

        logger.debug("InputWithDabBandControl created for {}", zoneState.inputID);
        inputWithDabBandControl = ProtocolFactory.InputWithDabBandControl(zoneState.inputID, connection,
                this, this, this);

        updateAsyncMakeOfflineIfFail(inputWithDabBandControl);
    }

    protected void updateAsyncMakeOfflineIfFail(IStateUpdatable stateUpdateable) {
        scheduler.submit(() -> {
            try {
                stateUpdateable.update();
            } catch (IOException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            } catch (ReceivedMessageParseException e) {
                updateProperty(YamahaReceiverBindingConstants.PROPERTY_LAST_PARSE_ERROR, e.getMessage());
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
        ChannelUID inputChannelUID = new ChannelUID(thing.getUID(), YamahaReceiverBindingConstants.CHANNEL_GROUP_ZONE,
                YamahaReceiverBindingConstants.CHANNEL_INPUT);
        Channel channel = ChannelBuilder.create(inputChannelUID, "String")
                .withType(channelsTypeProviderAvailableInputs.getChannelTypeUID()).build();
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
        updateState(grpPlayback(YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_SONG_IMAGE_URL), new StringType(msg.songImageUrl));
    }

    @Override
    public void presetInfoUpdated(PresetInfoState msg) {
        presetInfoState = msg;
        if (msg.presetChannelNamesChanged) {
            msg.presetChannelNamesChanged = false;
            channelsTypeProviderPreset.changePresetNames(msg.presetChannelNames);

            // Remove the old channel and add the new channel. The channel will be requested from the
            // channelsTypeProviderPreset.
            ChannelUID inputChannelUID = new ChannelUID(thing.getUID(),
                    YamahaReceiverBindingConstants.CHANNEL_GROUP_PLAYBACK,
                    YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_PRESET);
            Channel channel = ChannelBuilder.create(inputChannelUID, "Number")
                    .withType(channelsTypeProviderPreset.getChannelTypeUID()).build();
            updateThing(editThing().withoutChannel(inputChannelUID).withChannel(channel).build());
        }

        updateState(grpPlayback(YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_PRESET),
                new DecimalType(msg.presetChannel));
    }

    @Override
    public void dabBandUpdated(DabBandState msg) {
        dabBandState = msg;
        updateState(grpPlayback(YamahaReceiverBindingConstants.CHANNEL_TUNER_BAND), new StringType(msg.band));
    }

    @Override
    public void navigationUpdated(NavigationControlState msg) {
        navigationInfoState = msg;
        updateState(grpNav(YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_MENU), new StringType(msg.menuName));
        updateState(grpNav(YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_LEVEL), new DecimalType(msg.menuLayer));
        updateState(grpNav(YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_CURRENT_ITEM),
                new DecimalType(msg.currentLine));
        updateState(grpNav(YamahaReceiverBindingConstants.CHANNEL_NAVIGATION_TOTAL_ITEMS),
                new DecimalType(msg.maxLine));
    }

    @Override
    public void navigationError(String msg) {
        updateProperty(YamahaReceiverBindingConstants.PROPERTY_MENU_ERROR, msg);
        logger.warn("Navigation error: {}", msg);
    }
}
