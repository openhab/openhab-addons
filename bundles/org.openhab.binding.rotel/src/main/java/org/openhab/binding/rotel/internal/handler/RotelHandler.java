/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.rotel.internal.handler;

import static org.openhab.binding.rotel.internal.RotelBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rotel.internal.RotelBindingConstants;
import org.openhab.binding.rotel.internal.RotelCommandDescriptionOptionProvider;
import org.openhab.binding.rotel.internal.RotelException;
import org.openhab.binding.rotel.internal.RotelModel;
import org.openhab.binding.rotel.internal.RotelPlayStatus;
import org.openhab.binding.rotel.internal.RotelRepeatMode;
import org.openhab.binding.rotel.internal.RotelStateDescriptionOptionProvider;
import org.openhab.binding.rotel.internal.communication.RotelCommand;
import org.openhab.binding.rotel.internal.communication.RotelConnector;
import org.openhab.binding.rotel.internal.communication.RotelDsp;
import org.openhab.binding.rotel.internal.communication.RotelIpConnector;
import org.openhab.binding.rotel.internal.communication.RotelSerialConnector;
import org.openhab.binding.rotel.internal.communication.RotelSimuConnector;
import org.openhab.binding.rotel.internal.communication.RotelSource;
import org.openhab.binding.rotel.internal.configuration.RotelThingConfiguration;
import org.openhab.binding.rotel.internal.protocol.RotelAbstractProtocolHandler;
import org.openhab.binding.rotel.internal.protocol.RotelMessageEvent;
import org.openhab.binding.rotel.internal.protocol.RotelMessageEventListener;
import org.openhab.binding.rotel.internal.protocol.RotelProtocol;
import org.openhab.binding.rotel.internal.protocol.ascii.RotelAsciiV1ProtocolHandler;
import org.openhab.binding.rotel.internal.protocol.ascii.RotelAsciiV2ProtocolHandler;
import org.openhab.binding.rotel.internal.protocol.hex.RotelHexProtocolHandler;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RotelHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RotelHandler extends BaseThingHandler implements RotelMessageEventListener {

    private final Logger logger = LoggerFactory.getLogger(RotelHandler.class);

    private static final RotelModel DEFAULT_MODEL = RotelModel.RSP1066;
    private static final long POLLING_INTERVAL = TimeUnit.SECONDS.toSeconds(60);
    private static final boolean USE_SIMULATED_DEVICE = false;
    private static final int SLEEP_INTV = 30;

    private final RotelStateDescriptionOptionProvider stateDescriptionProvider;
    private final RotelCommandDescriptionOptionProvider commandDescriptionProvider;
    private final SerialPortManager serialPortManager;

    private @Nullable ScheduledFuture<?> reconnectJob;
    private @Nullable ScheduledFuture<?> powerOffJob;
    private @Nullable ScheduledFuture<?>[] powerOnZoneJobs = { null, null, null, null, null };

    private RotelModel model;
    private RotelProtocol protocol;
    private RotelAbstractProtocolHandler protocolHandler;
    private RotelConnector connector;

    private int minVolume;
    private int maxVolume;
    private int minToneLevel;
    private int maxToneLevel;

    private int currentZone = 1;
    private boolean selectingRecord;
    private @Nullable Boolean[] powers = { null, false, false, false, false };
    private boolean powerControlPerZone;
    private @Nullable RotelSource recordSource;
    private @Nullable RotelSource[] sources = { RotelSource.CAT0_CD, null, null, null, null };
    private RotelDsp dsp = RotelDsp.CAT1_NONE;
    private boolean[] fixedVolumeZones = { false, false, false, false, false };
    private int[] volumes = { 0, 0, 0, 0, 0 };
    private boolean[] mutes = { false, false, false, false, false };
    private int[] basses = { 0, 0, 0, 0, 0 };
    private int[] trebles = { 0, 0, 0, 0, 0 };
    private RotelPlayStatus playStatus = RotelPlayStatus.STOPPED;
    private int track;
    private boolean randomMode;
    private RotelRepeatMode repeatMode = RotelRepeatMode.OFF;
    private int radioPreset;
    private double[] frequencies = { 0.0, 0.0, 0.0, 0.0, 0.0 };
    private String frontPanelLine1 = "";
    private String frontPanelLine2 = "";
    private int brightness;
    private boolean tcbypass;
    private int[] balances = { 0, 0, 0, 0, 0 };
    private int minBalanceLevel;
    private int maxBalanceLevel;
    private boolean speakera;
    private boolean speakerb;

    private Object sequenceLock = new Object();

    /**
     * Constructor
     */
    public RotelHandler(Thing thing, RotelStateDescriptionOptionProvider stateDescriptionProvider,
            RotelCommandDescriptionOptionProvider commandDescriptionProvider, SerialPortManager serialPortManager) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.commandDescriptionProvider = commandDescriptionProvider;
        this.serialPortManager = serialPortManager;
        this.model = DEFAULT_MODEL;
        this.protocolHandler = new RotelHexProtocolHandler(model, Map.of());
        this.protocol = protocolHandler.getProtocol();
        this.connector = new RotelSimuConnector(model, protocolHandler, new HashMap<>(), "OH-binding-rotel");
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing handler for thing {}", getThing().getUID());

        RotelThingConfiguration config = getConfigAs(RotelThingConfiguration.class);

        protocol = RotelProtocol.HEX;
        if (config.protocol != null && !config.protocol.isEmpty()) {
            try {
                protocol = RotelProtocol.getFromName(config.protocol);
            } catch (RotelException e) {
                // Invalid protocol name in configuration, HEX will be considered by default
            }
        } else {
            Map<String, String> properties = editProperties();
            String property = properties.get(RotelBindingConstants.PROPERTY_PROTOCOL);
            if (property != null && !property.isEmpty()) {
                try {
                    protocol = RotelProtocol.getFromName(property);
                } catch (RotelException e) {
                    // Invalid protocol name in thing property, HEX will be considered by default
                }
            }
        }
        logger.debug("rotelProtocol {}", protocol.getName());

        switch (getThing().getThingTypeUID().getId()) {
            case THING_TYPE_ID_RSP1066:
                model = RotelModel.RSP1066;
                break;
            case THING_TYPE_ID_RSP1068:
                model = RotelModel.RSP1068;
                break;
            case THING_TYPE_ID_RSP1069:
                model = RotelModel.RSP1069;
                break;
            case THING_TYPE_ID_RSP1098:
                model = RotelModel.RSP1098;
                break;
            case THING_TYPE_ID_RSP1570:
                model = RotelModel.RSP1570;
                break;
            case THING_TYPE_ID_RSP1572:
                model = RotelModel.RSP1572;
                break;
            case THING_TYPE_ID_RSX1055:
                model = RotelModel.RSX1055;
                break;
            case THING_TYPE_ID_RSX1056:
                model = RotelModel.RSX1056;
                break;
            case THING_TYPE_ID_RSX1057:
                model = RotelModel.RSX1057;
                break;
            case THING_TYPE_ID_RSX1058:
                model = RotelModel.RSX1058;
                break;
            case THING_TYPE_ID_RSX1065:
                model = RotelModel.RSX1065;
                break;
            case THING_TYPE_ID_RSX1067:
                model = RotelModel.RSX1067;
                break;
            case THING_TYPE_ID_RSX1550:
                model = RotelModel.RSX1550;
                break;
            case THING_TYPE_ID_RSX1560:
                model = RotelModel.RSX1560;
                break;
            case THING_TYPE_ID_RSX1562:
                model = RotelModel.RSX1562;
                break;
            case THING_TYPE_ID_A11:
                model = RotelModel.A11;
                break;
            case THING_TYPE_ID_A12:
                model = RotelModel.A12;
                break;
            case THING_TYPE_ID_A14:
                model = RotelModel.A14;
                break;
            case THING_TYPE_ID_CD11:
                model = RotelModel.CD11;
                break;
            case THING_TYPE_ID_CD14:
                model = RotelModel.CD14;
                break;
            case THING_TYPE_ID_RA11:
                model = RotelModel.RA11;
                break;
            case THING_TYPE_ID_RA12:
                model = RotelModel.RA12;
                break;
            case THING_TYPE_ID_RA1570:
                model = RotelModel.RA1570;
                break;
            case THING_TYPE_ID_RA1572:
                model = RotelModel.RA1572;
                break;
            case THING_TYPE_ID_RA1592:
                if (protocol == RotelProtocol.ASCII_V1) {
                    model = RotelModel.RA1592_V1;
                } else {
                    model = RotelModel.RA1592_V2;
                }
                break;
            case THING_TYPE_ID_RAP1580:
                model = RotelModel.RAP1580;
                break;
            case THING_TYPE_ID_RC1570:
                model = RotelModel.RC1570;
                break;
            case THING_TYPE_ID_RC1572:
                model = RotelModel.RC1572;
                break;
            case THING_TYPE_ID_RC1590:
                if (protocol == RotelProtocol.ASCII_V1) {
                    model = RotelModel.RC1590_V1;
                } else {
                    model = RotelModel.RC1590_V2;
                }
                break;
            case THING_TYPE_ID_RCD1570:
                model = RotelModel.RCD1570;
                break;
            case THING_TYPE_ID_RCD1572:
                model = RotelModel.RCD1572;
                break;
            case THING_TYPE_ID_RCX1500:
                model = RotelModel.RCX1500;
                break;
            case THING_TYPE_ID_RDD1580:
                model = RotelModel.RDD1580;
                break;
            case THING_TYPE_ID_RDG1520:
            case THING_TYPE_ID_RT09:
                model = RotelModel.RDG1520;
                break;
            case THING_TYPE_ID_RSP1576:
                model = RotelModel.RSP1576;
                break;
            case THING_TYPE_ID_RSP1582:
                model = RotelModel.RSP1582;
                break;
            case THING_TYPE_ID_RT11:
                model = RotelModel.RT11;
                break;
            case THING_TYPE_ID_RT1570:
                model = RotelModel.RT1570;
                break;
            case THING_TYPE_ID_T11:
                model = RotelModel.T11;
                break;
            case THING_TYPE_ID_T14:
                model = RotelModel.T14;
                break;
            case THING_TYPE_ID_C8:
                model = RotelModel.C8;
                break;
            case THING_TYPE_ID_M8:
                model = RotelModel.M8;
                break;
            case THING_TYPE_ID_P5:
                model = RotelModel.P5;
                break;
            case THING_TYPE_ID_S5:
                model = RotelModel.S5;
                break;
            case THING_TYPE_ID_X3:
                model = RotelModel.X3;
                break;
            case THING_TYPE_ID_X5:
                model = RotelModel.X5;
                break;
            default:
                model = DEFAULT_MODEL;
                break;
        }

        Map<RotelSource, String> sourcesCustomLabels = new HashMap<>();
        Map<RotelSource, String> sourcesLabels = new HashMap<>();

        String readerThreadName = "OH-binding-" + getThing().getUID().getAsString();

        if (model.hasVolumeControl()) {
            maxVolume = model.getVolumeMax();
            if (!model.hasDirectVolumeControl()) {
                logger.info(
                        "Set minValue to {} and maxValue to {} for your sitemap widget attached to your volume item.",
                        minVolume, maxVolume);
            }
        }
        if (model.hasToneControl()) {
            maxToneLevel = model.getToneLevelMax();
            minToneLevel = -maxToneLevel;
            logger.info(
                    "Set minValue to {} and maxValue to {} for your sitemap widget attached to your bass or treble item.",
                    minToneLevel, maxToneLevel);
        }
        if (model.hasBalanceControl()) {
            maxBalanceLevel = model.getBalanceLevelMax();
            minBalanceLevel = -maxBalanceLevel;
            logger.info("Set minValue to {} and maxValue to {} for your sitemap widget attached to your balance item.",
                    minBalanceLevel, maxBalanceLevel);
        }

        powerControlPerZone = model.hasPowerControlPerZone();

        // Check configuration settings
        String configError = null;
        if ((config.serialPort == null || config.serialPort.isEmpty())
                && (config.host == null || config.host.isEmpty())) {
            configError = "@text/offline.config-error-unknown-serialport-and-host";
        } else if (config.host == null || config.host.isEmpty()) {
            if (config.serialPort.toLowerCase().startsWith("rfc2217")) {
                configError = "@text/offline.config-error-invalid-serial-over-ip";
            }
        } else {
            if (config.port == null) {
                configError = "@text/offline.config-error-unknown-port";
            } else if (config.port <= 0) {
                configError = "@text/offline.config-error-invalid-port";
            }
        }

        if (configError != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, configError);
        } else {
            for (RotelSource src : model.getSources()) {
                // Consider custom input labels
                String label = null;
                switch (src.getName()) {
                    case "CD":
                        label = config.inputLabelCd;
                        break;
                    case "TUNER":
                        label = config.inputLabelTuner;
                        break;
                    case "TAPE":
                        label = config.inputLabelTape;
                        break;
                    case "PHONO":
                        label = config.inputLabelPhono;
                        break;
                    case "VIDEO1":
                        label = config.inputLabelVideo1;
                        break;
                    case "VIDEO2":
                        label = config.inputLabelVideo2;
                        break;
                    case "VIDEO3":
                        label = config.inputLabelVideo3;
                        break;
                    case "VIDEO4":
                        label = config.inputLabelVideo4;
                        break;
                    case "VIDEO5":
                        label = config.inputLabelVideo5;
                        break;
                    case "VIDEO6":
                        label = config.inputLabelVideo6;
                        break;
                    case "USB":
                        label = config.inputLabelUsb;
                        break;
                    case "MULTI":
                        label = config.inputLabelMulti;
                        break;
                    default:
                        break;
                }
                if (label != null && !label.isEmpty()) {
                    sourcesCustomLabels.put(src, label);
                }
                sourcesLabels.put(src, (label == null || label.isEmpty()) ? src.getLabel() : label);
            }

            if (protocol == RotelProtocol.HEX) {
                protocolHandler = new RotelHexProtocolHandler(model, sourcesLabels);
            } else if (protocol == RotelProtocol.ASCII_V1) {
                protocolHandler = new RotelAsciiV1ProtocolHandler(model);
            } else {
                protocolHandler = new RotelAsciiV2ProtocolHandler(model);
            }

            if (USE_SIMULATED_DEVICE) {
                connector = new RotelSimuConnector(model, protocolHandler, sourcesLabels, readerThreadName);
            } else if (config.serialPort != null) {
                connector = new RotelSerialConnector(serialPortManager, config.serialPort, model.getBaudRate(),
                        protocolHandler, readerThreadName);
            } else {
                connector = new RotelIpConnector(config.host, config.port, protocolHandler, readerThreadName);
            }

            if (model.hasSourceControl()) {
                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_SOURCE),
                        getStateOptions(model.getSources(), sourcesCustomLabels));
                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_MAIN_SOURCE),
                        getStateOptions(model.getSources(), sourcesCustomLabels));
                stateDescriptionProvider.setStateOptions(
                        new ChannelUID(getThing().getUID(), CHANNEL_MAIN_RECORD_SOURCE),
                        getStateOptions(model.getRecordSources(), sourcesCustomLabels));
            }
            if (model.hasZoneSourceControl(1)) {
                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_ZONE1_SOURCE),
                        getStateOptions(model.getZoneSources(1), sourcesCustomLabels));
            }
            if (model.hasZoneSourceControl(2)) {
                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_ZONE2_SOURCE),
                        getStateOptions(model.getZoneSources(2), sourcesCustomLabels));
            }
            if (model.hasZoneSourceControl(3)) {
                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_ZONE3_SOURCE),
                        getStateOptions(model.getZoneSources(3), sourcesCustomLabels));
            }
            if (model.hasZoneSourceControl(4)) {
                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_ZONE4_SOURCE),
                        getStateOptions(model.getZoneSources(4), sourcesCustomLabels));
            }
            if (model.hasDspControl()) {
                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_DSP),
                        model.getDspStateOptions());
                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_MAIN_DSP),
                        model.getDspStateOptions());
            }

            List<CommandOption> options = model.getOtherCommandsOptions(protocol);
            if (!options.isEmpty()) {
                commandDescriptionProvider.setCommandOptions(new ChannelUID(getThing().getUID(), CHANNEL_OTHER_COMMAND),
                        options);
                commandDescriptionProvider
                        .setCommandOptions(new ChannelUID(getThing().getUID(), CHANNEL_MAIN_OTHER_COMMAND), options);
            }

            updateStatus(ThingStatus.UNKNOWN);

            scheduleReconnectJob();
        }

        logger.debug("Finished initializing!");
    }

    @Override
    public void dispose() {
        logger.debug("Disposing handler for thing {}", getThing().getUID());
        cancelPowerOffJob();
        for (int zone = 0; zone <= model.getNumberOfZones(); zone++) {
            cancelPowerOnZoneJob(zone);
        }
        cancelReconnectJob();
        closeConnection();
        super.dispose();
    }

    public List<StateOption> getStateOptions(List<RotelSource> list, Map<RotelSource, String> sourcesLabels) {
        List<StateOption> options = new ArrayList<>();
        for (RotelSource item : list) {
            String label = sourcesLabels.get(item);
            options.add(new StateOption(item.getName(), label == null ? ("@text/source." + item.getName()) : label));
        }
        return options;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getId();

        if (getThing().getStatus() != ThingStatus.ONLINE) {
            logger.debug("Thing is not ONLINE; command {} from channel {} is ignored", command, channel);
            return;
        }

        if (command instanceof RefreshType) {
            updateChannelState(channel);
            return;
        }

        if (!connector.isConnected()) {
            logger.debug("Command {} from channel {} is ignored: connection not established", command, channel);
            return;
        }

        int numZone = 0;
        switch (channel) {
            case CHANNEL_ZONE1_SOURCE:
            case CHANNEL_ZONE1_VOLUME:
            case CHANNEL_ZONE1_MUTE:
            case CHANNEL_ZONE1_BASS:
            case CHANNEL_ZONE1_TREBLE:
            case CHANNEL_ZONE1_BALANCE:
                numZone = 1;
                break;
            case CHANNEL_ZONE2_POWER:
            case CHANNEL_ZONE2_SOURCE:
            case CHANNEL_ZONE2_VOLUME:
            case CHANNEL_ZONE2_VOLUME_UP_DOWN:
            case CHANNEL_ZONE2_MUTE:
            case CHANNEL_ZONE2_BASS:
            case CHANNEL_ZONE2_TREBLE:
            case CHANNEL_ZONE2_BALANCE:
                numZone = 2;
                break;
            case CHANNEL_ZONE3_POWER:
            case CHANNEL_ZONE3_SOURCE:
            case CHANNEL_ZONE3_VOLUME:
            case CHANNEL_ZONE3_MUTE:
            case CHANNEL_ZONE3_BASS:
            case CHANNEL_ZONE3_TREBLE:
            case CHANNEL_ZONE3_BALANCE:
                numZone = 3;
                break;
            case CHANNEL_ZONE4_POWER:
            case CHANNEL_ZONE4_SOURCE:
            case CHANNEL_ZONE4_VOLUME:
            case CHANNEL_ZONE4_MUTE:
            case CHANNEL_ZONE4_BASS:
            case CHANNEL_ZONE4_TREBLE:
            case CHANNEL_ZONE4_BALANCE:
                numZone = 4;
                break;
            default:
                break;
        }

        RotelSource src;
        RotelCommand cmd;
        boolean success = true;
        synchronized (sequenceLock) {
            try {
                switch (channel) {
                    case CHANNEL_POWER:
                    case CHANNEL_MAIN_POWER:
                    case CHANNEL_ZONE2_POWER:
                    case CHANNEL_ZONE3_POWER:
                    case CHANNEL_ZONE4_POWER:
                        if (numZone == 0 || model.hasZoneCommands(numZone)) {
                            handlePowerCmd(channel, command, getPowerOnCommand(numZone), getPowerOffCommand(numZone));
                        } else if (numZone == 2 && model.getNumberOfZones() == 2) {
                            if (isPowerOn() || isPowerOn(numZone)) {
                                selectZone(2, model.getZoneSelectCmd());
                            }
                            sendCommand(RotelCommand.ZONE_SELECT);
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_ALL_POWER:
                        handlePowerCmd(channel, command, RotelCommand.POWER_ON, RotelCommand.POWER_OFF);
                        break;
                    case CHANNEL_SOURCE:
                    case CHANNEL_MAIN_SOURCE:
                    case CHANNEL_ZONE1_SOURCE:
                    case CHANNEL_ZONE2_SOURCE:
                    case CHANNEL_ZONE3_SOURCE:
                    case CHANNEL_ZONE4_SOURCE:
                        if (!isPowerOn(numZone)) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: {} in standby", command, channel,
                                    numZone == 0 ? "device" : "zone " + numZone);
                        } else if (numZone == 0 || model.hasZoneCommands(numZone)) {
                            src = model.getSourceFromName(command.toString());
                            if (numZone == 0) {
                                cmd = model.hasOtherThanPrimaryCommands() ? src.getZoneCommand(1) : src.getCommand();
                            } else {
                                cmd = src.getZoneCommand(numZone);
                            }
                            if (cmd != null) {
                                sendCommand(cmd);
                                if (model.canGetFrequency()) {
                                    // send <new-source> returns
                                    // 1.) the selected <new-source>
                                    // 2.) the used frequency
                                    // BUT:
                                    // at response-time the frequency has the value of <old-source>
                                    // so we must wait a short moment to get the frequency of <new-source>
                                    Thread.sleep(1000);
                                    sendCommand(RotelCommand.FREQUENCY);
                                    Thread.sleep(100);
                                    updateChannelState(CHANNEL_FREQUENCY);
                                }
                            } else {
                                success = false;
                                logger.debug("Command {} from channel {} failed: undefined source command", command,
                                        channel);
                            }
                        } else if (numZone == 2 && model.getNumberOfZones() > 1) {
                            src = model.getSourceFromName(command.toString());
                            cmd = src.getCommand();
                            if (cmd != null) {
                                selectZone(2, model.getZoneSelectCmd());
                                sendCommand(cmd);
                                if (model.canGetFrequency()) {
                                    // send <new-source> returns
                                    // 1.) the selected <new-source>
                                    // 2.) the used frequency
                                    // BUT:
                                    // at response-time the frequency has the value of <old-source>
                                    // so we must wait a short moment to get the frequency of <new-source>
                                    Thread.sleep(1000);
                                    sendCommand(RotelCommand.FREQUENCY);
                                    Thread.sleep(100);
                                    updateChannelState(CHANNEL_FREQUENCY);
                                }
                            } else {
                                success = false;
                                logger.debug("Command {} from channel {} failed: undefined source command", command,
                                        channel);
                            }
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_MAIN_RECORD_SOURCE:
                        if (!isPowerOn()) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: device in standby", command, channel);
                        } else if (model.hasOtherThanPrimaryCommands()) {
                            src = model.getSourceFromName(command.toString());
                            cmd = src.getRecordCommand();
                            if (cmd != null) {
                                sendCommand(cmd);
                            } else {
                                success = false;
                                logger.debug("Command {} from channel {} failed: undefined record source command",
                                        command, channel);
                            }
                        } else {
                            src = model.getSourceFromName(command.toString());
                            cmd = src.getCommand();
                            if (cmd != null) {
                                sendCommand(RotelCommand.RECORD_FONCTION_SELECT);
                                Thread.sleep(100);
                                sendCommand(cmd);
                            } else {
                                success = false;
                                logger.debug("Command {} from channel {} failed: undefined source command", command,
                                        channel);
                            }
                        }
                        break;
                    case CHANNEL_DSP:
                    case CHANNEL_MAIN_DSP:
                        if (!isPowerOn()) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: device in standby", command, channel);
                        } else {
                            sendCommand(model.getCommandFromDspName(command.toString()));
                        }
                        break;
                    case CHANNEL_VOLUME:
                    case CHANNEL_MAIN_VOLUME:
                    case CHANNEL_MAIN_VOLUME_UP_DOWN:
                    case CHANNEL_ZONE1_VOLUME:
                    case CHANNEL_ZONE2_VOLUME:
                    case CHANNEL_ZONE2_VOLUME_UP_DOWN:
                    case CHANNEL_ZONE3_VOLUME:
                    case CHANNEL_ZONE4_VOLUME:
                        if (!isPowerOn(numZone)) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: zone {} in standby", command, channel,
                                    numZone == 0 ? "device" : "zone " + numZone);
                        } else if (fixedVolumeZones[numZone]) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: fixed volume", command, channel);
                        } else if (model.hasVolumeControl() && (numZone == 0 || model.hasZoneCommands(numZone))) {
                            handleVolumeCmd(volumes[numZone], channel, command, getVolumeUpCommand(numZone),
                                    getVolumeDownCommand(numZone),
                                    CHANNEL_MAIN_VOLUME_UP_DOWN.equals(channel)
                                            || CHANNEL_ZONE2_VOLUME_UP_DOWN.equals(channel) ? null
                                                    : getVolumeSetCommand(numZone));
                        } else if (numZone == 2 && model.hasVolumeControl() && model.getNumberOfZones() > 1) {
                            selectZone(2, model.getZoneSelectCmd());
                            handleVolumeCmd(volumes[numZone], channel, command, RotelCommand.VOLUME_UP,
                                    RotelCommand.VOLUME_DOWN,
                                    CHANNEL_ZONE2_VOLUME_UP_DOWN.equals(channel) ? null : RotelCommand.VOLUME_SET);
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_MUTE:
                    case CHANNEL_MAIN_MUTE:
                    case CHANNEL_ZONE1_MUTE:
                    case CHANNEL_ZONE2_MUTE:
                    case CHANNEL_ZONE3_MUTE:
                    case CHANNEL_ZONE4_MUTE:
                        if (!isPowerOn(numZone)) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: zone {} in standby", command, channel,
                                    numZone == 0 ? "device" : "zone " + numZone);
                        } else if (model.hasVolumeControl() && (numZone == 0 || model.hasZoneCommands(numZone))) {
                            handleMuteCmd(numZone == 0 && protocol == RotelProtocol.HEX, channel, command,
                                    getMuteOnCommand(numZone), getMuteOffCommand(numZone),
                                    getMuteToggleCommand(numZone));
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_BASS:
                    case CHANNEL_MAIN_BASS:
                    case CHANNEL_ZONE1_BASS:
                    case CHANNEL_ZONE2_BASS:
                    case CHANNEL_ZONE3_BASS:
                    case CHANNEL_ZONE4_BASS:
                        if (!isPowerOn(numZone)) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: zone {} in standby", command, channel,
                                    numZone == 0 ? "device" : "zone " + numZone);
                        } else if (tcbypass) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: tone control bypass is ON", command,
                                    channel);
                        } else if (model.hasToneControl() && (numZone == 0 || model.hasZoneCommands(numZone))) {
                            handleToneCmd(basses[numZone], channel, command, 2, getBassUpCommand(numZone),
                                    getBassDownCommand(numZone), getBassSetCommand(numZone));
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_TREBLE:
                    case CHANNEL_MAIN_TREBLE:
                    case CHANNEL_ZONE1_TREBLE:
                    case CHANNEL_ZONE2_TREBLE:
                    case CHANNEL_ZONE3_TREBLE:
                    case CHANNEL_ZONE4_TREBLE:
                        if (!isPowerOn(numZone)) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: zone {} in standby", command, channel,
                                    numZone == 0 ? "device" : "zone " + numZone);
                        } else if (tcbypass) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: tone control bypass is ON", command,
                                    channel);
                        } else if (model.hasToneControl() && (numZone == 0 || model.hasZoneCommands(numZone))) {
                            handleToneCmd(trebles[numZone], channel, command, 1, getTrebleUpCommand(numZone),
                                    getTrebleDownCommand(numZone), getTrebleSetCommand(numZone));
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_PLAY_CONTROL:
                        if (!isPowerOn()) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: device in standby", command, channel);
                        } else if (command instanceof PlayPauseType && command == PlayPauseType.PLAY) {
                            sendCommand(RotelCommand.PLAY);
                        } else if (command instanceof PlayPauseType && command == PlayPauseType.PAUSE) {
                            sendCommand(RotelCommand.PAUSE);
                            if (protocol == RotelProtocol.ASCII_V1 && model != RotelModel.RCD1570
                                    && model != RotelModel.RCD1572 && model != RotelModel.RCX1500) {
                                Thread.sleep(SLEEP_INTV);
                                sendCommand(RotelCommand.PLAY_STATUS);
                            }
                        } else if (command instanceof NextPreviousType && command == NextPreviousType.NEXT) {
                            sendCommand(RotelCommand.TRACK_FWD);
                        } else if (command instanceof NextPreviousType && command == NextPreviousType.PREVIOUS) {
                            sendCommand(RotelCommand.TRACK_BACK);
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: invalid command value", command, channel);
                        }
                        break;
                    case CHANNEL_RANDOM:
                        if (!isPowerOn()) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: device in standby", command, channel);
                        } else if (command instanceof OnOffType) {
                            sendCommand(RotelCommand.RANDOM_TOGGLE);
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: invalid command value", command, channel);
                        }
                        break;
                    case CHANNEL_REPEAT:
                        if (!isPowerOn()) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: device in standby", command, channel);
                        } else {
                            RotelRepeatMode currentMode = repeatMode;
                            RotelRepeatMode mode = RotelRepeatMode.OFF;
                            try {
                                mode = RotelRepeatMode.getFromName(command.toString());
                                if (mode == currentMode) {
                                    success = false;
                                    logger.debug("Command {} from channel {} ignored: no change requested", command,
                                            channel);
                                }
                            } catch (RotelException e) {
                                success = false;
                                logger.debug("Command {} from channel {} failed: invalid command value", command,
                                        channel);
                            }
                            if (success) {
                                // Toggle TRACK -> DISC -> OFF
                                sendCommand(RotelCommand.REPEAT_TOGGLE);
                                if ((mode == RotelRepeatMode.OFF && currentMode == RotelRepeatMode.TRACK)
                                        || (mode == RotelRepeatMode.TRACK && currentMode == RotelRepeatMode.DISC)
                                        || (mode == RotelRepeatMode.DISC && currentMode == RotelRepeatMode.OFF)) {
                                    Thread.sleep(SLEEP_INTV);
                                    sendCommand(RotelCommand.REPEAT_TOGGLE);
                                }
                            }
                        }
                        break;
                    case CHANNEL_RADIO_PRESET:
                        if (!isPowerOn()) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: device in standby", command, channel);
                        } else {
                            int value = 0;
                            if (radioPreset > 0 && command instanceof IncreaseDecreaseType
                                    && command == IncreaseDecreaseType.INCREASE) {
                                value = radioPreset + 1;
                            } else if (radioPreset > 0 && command instanceof IncreaseDecreaseType
                                    && command == IncreaseDecreaseType.DECREASE) {
                                value = radioPreset - 1;
                            } else if (command instanceof DecimalType decimalCommand) {
                                value = decimalCommand.intValue();
                            }
                            if (value >= 1 && value <= 30) {
                                RotelSource source = sources[0];
                                RotelCommand presetCallCmd = source == null ? null : getRadioPresetCallCommand(source);
                                if (presetCallCmd != null) {
                                    sendCommand(presetCallCmd, value);
                                    // In ASCII V2, the previous command will return nothing
                                    RotelCommand presetGetCmd = source == null ? null
                                            : getRadioPresetGetCommand(source);
                                    if (protocol == RotelProtocol.ASCII_V2 && presetGetCmd != null) {
                                        Thread.sleep(SLEEP_INTV);
                                        sendCommand(presetGetCmd);
                                    }
                                } else {
                                    success = false;
                                    logger.debug("Command {} from channel {} ignored: current source is not radio",
                                            command, channel);
                                }
                            } else {
                                success = false;
                                logger.debug("Command {} from channel {} ignored: value out of bounds", command,
                                        channel);
                            }
                        }
                        break;
                    case CHANNEL_BRIGHTNESS:
                    case CHANNEL_ALL_BRIGHTNESS:
                        if (!isPowerOn()) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: device in standby", command, channel);
                        } else if (!model.hasDimmerControl()) {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        } else if (command instanceof PercentType percentCommand) {
                            int dimmer = (int) Math.round(percentCommand.doubleValue() / 100.0
                                    * (model.getDimmerLevelMax() - model.getDimmerLevelMin()))
                                    + model.getDimmerLevelMin();
                            sendCommand(RotelCommand.DIMMER_LEVEL_SET, dimmer);
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: invalid command value", command, channel);
                        }
                        break;
                    case CHANNEL_TCBYPASS:
                        if (!isPowerOn()) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: device in standby", command, channel);
                        } else if (!model.hasToneControl() || protocol == RotelProtocol.HEX) {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        } else {
                            handleTcbypassCmd(channel, command,
                                    protocol == RotelProtocol.ASCII_V1 ? RotelCommand.TONE_CONTROLS_OFF
                                            : RotelCommand.TCBYPASS_ON,
                                    protocol == RotelProtocol.ASCII_V1 ? RotelCommand.TONE_CONTROLS_ON
                                            : RotelCommand.TCBYPASS_OFF);
                        }
                        break;
                    case CHANNEL_BALANCE:
                    case CHANNEL_ZONE1_BALANCE:
                    case CHANNEL_ZONE2_BALANCE:
                    case CHANNEL_ZONE3_BALANCE:
                    case CHANNEL_ZONE4_BALANCE:
                        if (!isPowerOn(numZone)) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: zone {} in standby", command, channel,
                                    numZone == 0 ? "device" : "zone " + numZone);
                        } else if (!model.hasBalanceControl() || protocol == RotelProtocol.HEX) {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        } else {
                            handleBalanceCmd(channel, command, getBalanceLeftCommand(numZone),
                                    getBalanceRightCommand(numZone), getBalanceSetCommand(numZone));
                        }
                        break;
                    case CHANNEL_SPEAKER_A:
                        if (!isPowerOn()) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: device in standby", command, channel);
                        } else {
                            handleSpeakerCmd(protocol == RotelProtocol.HEX, channel, command, RotelCommand.SPEAKER_A_ON,
                                    RotelCommand.SPEAKER_A_OFF, RotelCommand.SPEAKER_A_TOGGLE);
                        }
                        break;
                    case CHANNEL_SPEAKER_B:
                        if (!isPowerOn()) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: device in standby", command, channel);
                        } else {
                            handleSpeakerCmd(protocol == RotelProtocol.HEX, channel, command, RotelCommand.SPEAKER_B_ON,
                                    RotelCommand.SPEAKER_B_OFF, RotelCommand.SPEAKER_B_TOGGLE);
                        }
                        break;
                    case CHANNEL_OTHER_COMMAND:
                    case CHANNEL_MAIN_OTHER_COMMAND:
                        if (!isPowerOn()) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: device in standby", command, channel);
                        } else {
                            try {
                                cmd = RotelCommand.getFromName(command.toString());
                            } catch (RotelException e) {
                                success = false;
                                logger.debug("Command {} from channel {} failed: undefined command", command, channel);
                                cmd = null;
                            }
                            if (cmd != null) {
                                sendCommand(cmd);
                            }
                        }
                        break;
                    default:
                        success = false;
                        logger.debug("Command {} from channel {} failed: nnexpected command", command, channel);
                        break;
                }
                if (success) {
                    logger.debug("Command {} from channel {} succeeded", command, channel);
                } else {
                    updateChannelState(channel);
                }
            } catch (RotelException e) {
                logger.debug("Command {} from channel {} failed: {}", command, channel, e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.comm-error-sending-command");
                closeConnection();
                scheduleReconnectJob();
            } catch (InterruptedException e) {
                logger.debug("Command {} from channel {} interrupted: {}", command, channel, e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Handle a power ON/OFF command
     *
     * @param channel the channel
     * @param command the received channel command (OnOffType)
     * @param onCmd the command to be sent to the device to power it ON
     * @param offCmd the command to be sent to the device to power it OFF
     *
     * @throws RotelException in case of communication error with the device
     */
    private void handlePowerCmd(String channel, Command command, RotelCommand onCmd, RotelCommand offCmd)
            throws RotelException {
        if (command instanceof OnOffType && command == OnOffType.ON) {
            sendCommand(onCmd);
        } else if (command instanceof OnOffType && command == OnOffType.OFF) {
            sendCommand(offCmd);
        } else {
            logger.debug("Command {} from channel {} failed: invalid command value", command, channel);
        }
    }

    /**
     * Handle a volume command
     *
     * @param current the current volume
     * @param channel the channel
     * @param command the received channel command (IncreaseDecreaseType or DecimalType)
     * @param upCmd the command to be sent to the device to increase the volume
     * @param downCmd the command to be sent to the device to decrease the volume
     * @param setCmd the command to be sent to the device to set the volume at a value
     *
     * @throws RotelException in case of communication error with the device
     */
    private void handleVolumeCmd(int current, String channel, Command command, RotelCommand upCmd, RotelCommand downCmd,
            @Nullable RotelCommand setCmd) throws RotelException {
        if (command instanceof IncreaseDecreaseType && command == IncreaseDecreaseType.INCREASE) {
            sendCommand(upCmd);
        } else if (command instanceof IncreaseDecreaseType && command == IncreaseDecreaseType.DECREASE) {
            sendCommand(downCmd);
        } else if (command instanceof DecimalType decimalCommand && setCmd == null) {
            int value = decimalCommand.intValue();
            if (value >= minVolume && value <= maxVolume) {
                if (value > current) {
                    sendCommand(upCmd);
                } else if (value < current) {
                    sendCommand(downCmd);
                }
            }
        } else if (command instanceof PercentType percentCommand && setCmd != null) {
            int value = (int) Math.round(percentCommand.doubleValue() / 100.0 * (maxVolume - minVolume)) + minVolume;
            sendCommand(setCmd, value);
        } else {
            logger.debug("Command {} from channel {} failed: invalid command value", command, channel);
        }
    }

    /**
     * Handle a mute command
     *
     * @param onlyToggle true if only the toggle command must be used
     * @param channel the channel
     * @param command the received channel command (OnOffType)
     * @param onCmd the command to be sent to the device to mute
     * @param offCmd the command to be sent to the device to unmute
     * @param toggleCmd the command to be sent to the device to toggle the mute state
     *
     * @throws RotelException in case of communication error with the device
     */
    private void handleMuteCmd(boolean onlyToggle, String channel, Command command, RotelCommand onCmd,
            RotelCommand offCmd, RotelCommand toggleCmd) throws RotelException {
        if (command instanceof OnOffType) {
            if (onlyToggle) {
                sendCommand(toggleCmd);
            } else if (command == OnOffType.ON) {
                sendCommand(onCmd);
            } else if (command == OnOffType.OFF) {
                sendCommand(offCmd);
            }
        } else {
            logger.debug("Command {} from channel {} failed: invalid command value", command, channel);
        }
    }

    /**
     * Handle a tone level adjustment command (bass or treble)
     *
     * @param current the current tone level
     * @param channel the channel
     * @param command the received channel command (IncreaseDecreaseType or DecimalType)
     * @param nbSelect the number of TONE_CONTROL_SELECT commands to be run to display the right tone (bass or treble)
     * @param upCmd the command to be sent to the device to increase the tone level
     * @param downCmd the command to be sent to the device to decrease the tone level
     * @param setCmd the command to be sent to the device to set the tone level at a value
     *
     * @throws RotelException in case of communication error with the device
     * @throws InterruptedException in case of interruption during a thread sleep
     */
    private void handleToneCmd(int current, String channel, Command command, int nbSelect, RotelCommand upCmd,
            RotelCommand downCmd, RotelCommand setCmd) throws RotelException, InterruptedException {
        if (command instanceof IncreaseDecreaseType && command == IncreaseDecreaseType.INCREASE) {
            selectToneControl(nbSelect);
            sendCommand(upCmd);
        } else if (command instanceof IncreaseDecreaseType && command == IncreaseDecreaseType.DECREASE) {
            selectToneControl(nbSelect);
            sendCommand(downCmd);
        } else if (command instanceof DecimalType decimalCommand) {
            int value = decimalCommand.intValue();
            if (value >= minToneLevel && value <= maxToneLevel) {
                if (protocol != RotelProtocol.HEX) {
                    sendCommand(setCmd, value);
                } else if (value > current) {
                    selectToneControl(nbSelect);
                    sendCommand(upCmd);
                } else if (value < current) {
                    selectToneControl(nbSelect);
                    sendCommand(downCmd);
                }
            }
        } else {
            logger.debug("Command {} from channel {} failed: invalid command value", command, channel);
        }
    }

    /**
     * Handle a tcbypass command (only for ASCII protocol)
     *
     * @param channel the channel
     * @param command the received channel command (OnOffType)
     * @param onCmd the command to be sent to the device to bypass_on
     * @param offCmd the command to be sent to the device to bypass_off
     *
     * @throws RotelException in case of communication error with the device
     */
    private void handleTcbypassCmd(String channel, Command command, RotelCommand onCmd, RotelCommand offCmd)
            throws RotelException, InterruptedException {
        if (command instanceof OnOffType) {
            if (command == OnOffType.ON) {
                sendCommand(onCmd);
                basses[0] = 0;
                trebles[0] = 0;
                updateChannelState(CHANNEL_BASS);
                updateChannelState(CHANNEL_TREBLE);
            } else if (command == OnOffType.OFF) {
                sendCommand(offCmd);
                Thread.sleep(200);
                sendCommand(RotelCommand.BASS);
                Thread.sleep(200);
                sendCommand(RotelCommand.TREBLE);
            }
        } else {
            logger.debug("Command {} from channel {} failed: invalid command value", command, channel);
        }
    }

    /**
     * Handle a speaker command
     *
     * @param onlyToggle true if only the toggle command must be used
     * @param channel the channel
     * @param command the received channel command (OnOffType)
     * @param onCmd the command to be sent to the device to speaker_x_on
     * @param offCmd the command to be sent to the device to speaker_x_off
     * @param toggleCmd the command to be sent to the device to toggle the speaker_x state
     *
     * @throws RotelException in case of communication error with the device
     */
    private void handleSpeakerCmd(boolean onlyToggle, String channel, Command command, RotelCommand onCmd,
            RotelCommand offCmd, RotelCommand toggleCmd) throws RotelException {
        if (command instanceof OnOffType) {
            if (onlyToggle) {
                sendCommand(toggleCmd);
            } else if (command == OnOffType.ON) {
                sendCommand(onCmd);
            } else if (command == OnOffType.OFF) {
                sendCommand(offCmd);
            }
        } else {
            logger.debug("Command {} from channel {} failed: invalid command value", command, channel);
        }
    }

    /**
     * Handle a tone balance adjustment command (left or right) (only for ASCII protocol)
     *
     * @param channel the channel
     * @param command the received channel command (IncreaseDecreaseType or DecimalType)
     * @param rightCmd the command to be sent to the device to "increase" balance (shift to the right side)
     * @param leftCmd the command to be sent to the device to "decrease" balance (shift to the left side)
     * @param setCmd the command to be sent to the device to set the balance at a value
     *
     * @throws RotelException in case of communication error with the device
     * @throws InterruptedException in case of interruption during a thread sleep
     */
    private void handleBalanceCmd(String channel, Command command, RotelCommand leftCmd, RotelCommand rightCmd,
            RotelCommand setCmd) throws RotelException, InterruptedException {
        if (command instanceof IncreaseDecreaseType && command == IncreaseDecreaseType.INCREASE) {
            sendCommand(rightCmd);
        } else if (command instanceof IncreaseDecreaseType && command == IncreaseDecreaseType.DECREASE) {
            sendCommand(leftCmd);
        } else if (command instanceof DecimalType decimalCommand) {
            int value = decimalCommand.intValue();
            if (value >= minBalanceLevel && value <= maxBalanceLevel) {
                sendCommand(setCmd, value);
            }
        } else {
            logger.debug("Command {} from channel {} failed: invalid command value", command, channel);
        }
    }

    /**
     * Run a sequence of commands to display the current tone level (bass or treble) on the device front panel
     *
     * @param nbSelect the number of TONE_CONTROL_SELECT commands to be run to display the right tone (bass or treble)
     *
     * @throws RotelException in case of communication error with the device
     * @throws InterruptedException in case of interruption during a thread sleep
     */
    private void selectToneControl(int nbSelect) throws RotelException, InterruptedException {
        // No tone control select command for RSX-1065
        if (protocol == RotelProtocol.HEX && model != RotelModel.RSX1065) {
            selectFeature(nbSelect, RotelCommand.RECORD_FONCTION_SELECT, RotelCommand.TONE_CONTROL_SELECT);
        }
    }

    /**
     * Run a sequence of commands to display a particular zone on the device front panel
     *
     * @param zone the zone to be displayed (1 for main zone)
     * @param selectCommand the command to be sent to the device to switch the display between zones
     *
     * @throws RotelException in case of communication error with the device
     * @throws InterruptedException in case of interruption during a thread sleep
     */
    private void selectZone(int zone, @Nullable RotelCommand selectCommand)
            throws RotelException, InterruptedException {
        if (protocol == RotelProtocol.HEX && model.getNumberOfZones() > 1 && zone >= 1 && zone != currentZone
                && selectCommand != null) {
            int nbSelect;
            if (zone < currentZone) {
                nbSelect = zone + model.getNumberOfZones() - 1 - currentZone;
                if (isPowerOn() && selectCommand == RotelCommand.RECORD_FONCTION_SELECT) {
                    nbSelect++;
                }
            } else {
                nbSelect = zone - currentZone;
                if (isPowerOn() && currentZone == 1 && selectCommand == RotelCommand.RECORD_FONCTION_SELECT
                        && !selectingRecord) {
                    nbSelect++;
                }
            }
            selectFeature(nbSelect, null, selectCommand);
        }
    }

    /**
     * Run a sequence of commands to display a particular feature on the device front panel
     *
     * @param nbSelect the number of select commands to be run
     * @param preCmd the initial command to be sent to the device (before the select commands)
     * @param selectCmd the select command to be sent to the device
     *
     * @throws RotelException in case of communication error with the device
     * @throws InterruptedException in case of interruption during a thread sleep
     */
    private void selectFeature(int nbSelect, @Nullable RotelCommand preCmd, RotelCommand selectCmd)
            throws RotelException, InterruptedException {
        if (protocol == RotelProtocol.HEX) {
            if (preCmd != null) {
                sendCommand(preCmd);
                Thread.sleep(100);
            }
            for (int i = 1; i <= nbSelect; i++) {
                sendCommand(selectCmd);
                Thread.sleep(200);
            }
        }
    }

    /**
     * Open the connection with the Rotel device
     *
     * @return true if the connection is opened successfully or flase if not
     */
    private synchronized boolean openConnection() {
        protocolHandler.addEventListener(this);
        try {
            connector.open();
        } catch (RotelException e) {
            logger.debug("openConnection() failed", e);
        }
        logger.debug("openConnection(): {}", connector.isConnected() ? "connected" : "disconnected");
        return connector.isConnected();
    }

    /**
     * Close the connection with the Rotel device
     */
    private synchronized void closeConnection() {
        connector.close();
        protocolHandler.removeEventListener(this);
        logger.debug("closeConnection(): disconnected");
    }

    @Override
    public void onNewMessageEvent(EventObject event) {
        cancelPowerOffJob();

        RotelMessageEvent evt = (RotelMessageEvent) event;
        logger.debug("onNewMessageEvent: key {} = {}", evt.getKey(), evt.getValue());

        String key = evt.getKey();
        String value = evt.getValue().trim();
        if (!KEY_ERROR.equals(key)) {
            updateStatus(ThingStatus.ONLINE);
        }
        int numZone = 0;
        switch (key) {
            case KEY_INPUT_ZONE1:
            case KEY_VOLUME_ZONE1:
            case KEY_MUTE_ZONE1:
            case KEY_BASS_ZONE1:
            case KEY_TREBLE_ZONE1:
            case KEY_BALANCE_ZONE1:
            case KEY_FREQ_ZONE1:
                numZone = 1;
                break;
            case KEY_POWER_ZONE2:
            case KEY_SOURCE_ZONE2:
            case KEY_INPUT_ZONE2:
            case KEY_VOLUME_ZONE2:
            case KEY_MUTE_ZONE2:
            case KEY_BASS_ZONE2:
            case KEY_TREBLE_ZONE2:
            case KEY_BALANCE_ZONE2:
            case KEY_FREQ_ZONE2:
                numZone = 2;
                break;
            case KEY_POWER_ZONE3:
            case KEY_SOURCE_ZONE3:
            case KEY_INPUT_ZONE3:
            case KEY_VOLUME_ZONE3:
            case KEY_MUTE_ZONE3:
            case KEY_BASS_ZONE3:
            case KEY_TREBLE_ZONE3:
            case KEY_BALANCE_ZONE3:
            case KEY_FREQ_ZONE3:
                numZone = 3;
                break;
            case KEY_POWER_ZONE4:
            case KEY_SOURCE_ZONE4:
            case KEY_INPUT_ZONE4:
            case KEY_VOLUME_ZONE4:
            case KEY_MUTE_ZONE4:
            case KEY_BASS_ZONE4:
            case KEY_TREBLE_ZONE4:
            case KEY_BALANCE_ZONE4:
            case KEY_FREQ_ZONE4:
                numZone = 4;
                break;
            default:
                break;
        }
        int preset = 0;
        if (key.startsWith(KEY_FM_PRESET)) {
            try {
                preset = Integer.parseInt(key.substring(KEY_FM_PRESET.length()));
            } catch (NumberFormatException e) {
                // Considering the Rotel protocol, the parsing could not fail in practice.
                // In case it would fail, 0 will be considered as preset, meaning undefined.
            }
            key = KEY_FM_PRESET;
        } else if (key.startsWith(KEY_DAB_PRESET)) {
            try {
                preset = Integer.parseInt(key.substring(KEY_DAB_PRESET.length()));
            } catch (NumberFormatException e) {
                // Considering the Rotel protocol, the parsing could not fail in practice.
                // In case it would fail, 0 will be considered as preset, meaning undefined.
            }
            key = KEY_DAB_PRESET;
        } else if (key.startsWith(KEY_IRADIO_PRESET)) {
            try {
                preset = Integer.parseInt(key.substring(KEY_IRADIO_PRESET.length()));
            } catch (NumberFormatException e) {
                // Considering the Rotel protocol, the parsing could not fail in practice.
                // In case it would fail, 0 will be considered as preset, meaning undefined.
            }
            key = KEY_IRADIO_PRESET;
        }
        RotelSource source;
        try {
            switch (key) {
                case KEY_ERROR:
                    logger.debug("Reading feedback message failed");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.comm-error-reading-thread");
                    closeConnection();
                    break;
                case KEY_LINE1:
                    frontPanelLine1 = value;
                    updateChannelState(CHANNEL_LINE1);
                    break;
                case KEY_LINE2:
                    frontPanelLine2 = value;
                    updateChannelState(CHANNEL_LINE2);
                    break;
                case KEY_ZONE:
                    currentZone = Integer.parseInt(value);
                    break;
                case KEY_RECORD_SEL:
                    selectingRecord = MSG_VALUE_ON.equalsIgnoreCase(value);
                    break;
                case KEY_POWER:
                    if (POWER_ON.equalsIgnoreCase(value)) {
                        handlePowerOn();
                    } else if (STANDBY.equalsIgnoreCase(value)) {
                        handlePowerOff();
                        if (model.getNumberOfZones() > 1 && !powerControlPerZone) {
                            for (int zone = 1; zone <= model.getNumberOfZones(); zone++) {
                                handlePowerOffZone(zone);
                            }
                        }
                    } else if (POWER_OFF_DELAYED.equalsIgnoreCase(value)) {
                        schedulePowerOffJob(false);
                    } else {
                        throw new RotelException("Invalid value");
                    }
                    break;
                case KEY_POWER_ZONE2:
                case KEY_POWER_ZONE3:
                case KEY_POWER_ZONE4:
                    if (POWER_ON.equalsIgnoreCase(value)) {
                        handlePowerOnZone(numZone);
                    } else if (STANDBY.equalsIgnoreCase(value)) {
                        handlePowerOffZone(numZone);
                    } else {
                        throw new RotelException("Invalid value");
                    }
                    break;
                case KEY_POWER_MODE:
                    logger.debug("Power mode is set to {}", value);
                    break;
                case KEY_VOLUME_MIN:
                    minVolume = Integer.parseInt(value);
                    if (!model.hasDirectVolumeControl()) {
                        logger.info("Set minValue to {} for your sitemap widget attached to your volume item.",
                                minVolume);
                    }
                    break;
                case KEY_VOLUME_MAX:
                    maxVolume = Integer.parseInt(value);
                    if (!model.hasDirectVolumeControl()) {
                        logger.info("Set maxValue to {} for your sitemap widget attached to your volume item.",
                                maxVolume);
                    }
                    break;
                case KEY_VOLUME:
                case KEY_VOLUME_ZONE1:
                case KEY_VOLUME_ZONE2:
                case KEY_VOLUME_ZONE3:
                case KEY_VOLUME_ZONE4:
                    fixedVolumeZones[numZone] = false;
                    if (MSG_VALUE_FIX.equalsIgnoreCase(value)) {
                        fixedVolumeZones[numZone] = true;
                    } else if (MSG_VALUE_MIN.equalsIgnoreCase(value)) {
                        volumes[numZone] = minVolume;
                    } else if (MSG_VALUE_MAX.equalsIgnoreCase(value)) {
                        volumes[numZone] = maxVolume;
                    } else {
                        volumes[numZone] = Integer.parseInt(value);
                    }
                    if (numZone == 0) {
                        updateChannelState(CHANNEL_VOLUME);
                        updateChannelState(CHANNEL_MAIN_VOLUME);
                        updateChannelState(CHANNEL_MAIN_VOLUME_UP_DOWN);
                    } else {
                        updateGroupChannelState(numZone, CHANNEL_VOLUME);
                        updateGroupChannelState(numZone, CHANNEL_VOLUME_UP_DOWN);
                    }
                    break;
                case KEY_MUTE:
                case KEY_MUTE_ZONE1:
                case KEY_MUTE_ZONE2:
                case KEY_MUTE_ZONE3:
                case KEY_MUTE_ZONE4:
                    if (MSG_VALUE_ON.equalsIgnoreCase(value)) {
                        mutes[numZone] = true;
                        if (numZone == 0) {
                            updateChannelState(CHANNEL_MUTE);
                            updateChannelState(CHANNEL_MAIN_MUTE);
                        } else {
                            updateGroupChannelState(numZone, CHANNEL_MUTE);
                        }
                    } else if (MSG_VALUE_OFF.equalsIgnoreCase(value)) {
                        mutes[numZone] = false;
                        if (numZone == 0) {
                            updateChannelState(CHANNEL_MUTE);
                            updateChannelState(CHANNEL_MAIN_MUTE);
                        } else {
                            updateGroupChannelState(numZone, CHANNEL_MUTE);
                        }
                    } else {
                        throw new RotelException("Invalid value");
                    }
                    break;
                case KEY_TONE_MAX:
                    maxToneLevel = Integer.parseInt(value);
                    minToneLevel = -maxToneLevel;
                    logger.info(
                            "Set minValue to {} and maxValue to {} for your sitemap widget attached to your bass or treble item.",
                            minToneLevel, maxToneLevel);
                    break;
                case KEY_BASS:
                case KEY_BASS_ZONE1:
                case KEY_BASS_ZONE2:
                case KEY_BASS_ZONE3:
                case KEY_BASS_ZONE4:
                    if (MSG_VALUE_MIN.equalsIgnoreCase(value)) {
                        basses[numZone] = minToneLevel;
                    } else if (MSG_VALUE_MAX.equalsIgnoreCase(value)) {
                        basses[numZone] = maxToneLevel;
                    } else {
                        basses[numZone] = Integer.parseInt(value);
                    }
                    if (numZone == 0) {
                        updateChannelState(CHANNEL_BASS);
                        updateChannelState(CHANNEL_MAIN_BASS);
                    } else {
                        updateGroupChannelState(numZone, CHANNEL_BASS);
                    }
                    break;
                case KEY_TREBLE:
                case KEY_TREBLE_ZONE1:
                case KEY_TREBLE_ZONE2:
                case KEY_TREBLE_ZONE3:
                case KEY_TREBLE_ZONE4:
                    if (MSG_VALUE_MIN.equalsIgnoreCase(value)) {
                        trebles[numZone] = minToneLevel;
                    } else if (MSG_VALUE_MAX.equalsIgnoreCase(value)) {
                        trebles[numZone] = maxToneLevel;
                    } else {
                        trebles[numZone] = Integer.parseInt(value);
                    }
                    if (numZone == 0) {
                        updateChannelState(CHANNEL_TREBLE);
                        updateChannelState(CHANNEL_MAIN_TREBLE);
                    } else {
                        updateGroupChannelState(numZone, CHANNEL_TREBLE);
                    }
                    break;
                case KEY_SOURCE:
                    source = model.getSourceFromCommand(RotelCommand.getFromAsciiCommand(value));
                    sources[0] = source;
                    updateChannelState(CHANNEL_SOURCE);
                    updateChannelState(CHANNEL_MAIN_SOURCE);
                    RotelCommand presetGetCmd = getRadioPresetGetCommand(source);
                    if (presetGetCmd != null) {
                        // Request current preset (with a delay)
                        scheduler.schedule(() -> {
                            try {
                                sendCommand(presetGetCmd);
                            } catch (RotelException e) {
                                logger.debug("Getting the radio preset failed: {}", e.getMessage());
                            }
                        }, 250, TimeUnit.MILLISECONDS);
                    } else {
                        radioPreset = 0;
                        updateChannelState(CHANNEL_RADIO_PRESET);
                    }
                    break;
                case KEY_RECORD:
                    recordSource = model.getRecordSourceFromCommand(RotelCommand.getFromAsciiCommand(value));
                    updateChannelState(CHANNEL_MAIN_RECORD_SOURCE);
                    break;
                case KEY_SOURCE_ZONE2:
                case KEY_SOURCE_ZONE3:
                case KEY_SOURCE_ZONE4:
                case KEY_INPUT_ZONE1:
                case KEY_INPUT_ZONE2:
                case KEY_INPUT_ZONE3:
                case KEY_INPUT_ZONE4:
                    sources[numZone] = model.getZoneSourceFromCommand(RotelCommand.getFromAsciiCommand(value), numZone);
                    updateGroupChannelState(numZone, CHANNEL_SOURCE);
                    break;
                case KEY_DSP_MODE:
                    if ("dolby_pliix_movie".equals(value)) {
                        value = "dolby_plii_movie";
                    } else if ("dolby_pliix_music".equals(value)) {
                        value = "dolby_plii_music";
                    } else if ("dolby_pliix_game".equals(value)) {
                        value = "dolby_plii_game";
                    }
                    dsp = model.getDspFromFeedback(value);
                    logger.debug("DSP {}", dsp.getName());
                    updateChannelState(CHANNEL_DSP);
                    updateChannelState(CHANNEL_MAIN_DSP);
                    break;
                case KEY1_PLAY_STATUS:
                case KEY2_PLAY_STATUS:
                    if (PLAY.equalsIgnoreCase(value)) {
                        playStatus = RotelPlayStatus.PLAYING;
                        updateChannelState(CHANNEL_PLAY_CONTROL);
                    } else if (PAUSE.equalsIgnoreCase(value)) {
                        playStatus = RotelPlayStatus.PAUSED;
                        updateChannelState(CHANNEL_PLAY_CONTROL);
                    } else if (STOP.equalsIgnoreCase(value)) {
                        playStatus = RotelPlayStatus.STOPPED;
                        updateChannelState(CHANNEL_PLAY_CONTROL);
                    } else {
                        throw new RotelException("Invalid value");
                    }
                    break;
                case KEY_TRACK:
                    source = sources[0];
                    if (source != null && "CD".equals(source.getName()) && !model.hasSourceControl()) {
                        track = Integer.parseInt(value);
                        updateChannelState(CHANNEL_TRACK);
                    }
                    break;
                case KEY_RANDOM:
                    if (MSG_VALUE_ON.equalsIgnoreCase(value)) {
                        randomMode = true;
                        updateChannelState(CHANNEL_RANDOM);
                    } else if (MSG_VALUE_OFF.equalsIgnoreCase(value)) {
                        randomMode = false;
                        updateChannelState(CHANNEL_RANDOM);
                    } else {
                        throw new RotelException("Invalid value");
                    }
                    break;
                case KEY_REPEAT:
                    if (TRACK.equalsIgnoreCase(value)) {
                        repeatMode = RotelRepeatMode.TRACK;
                        updateChannelState(CHANNEL_REPEAT);
                    } else if (DISC.equalsIgnoreCase(value)) {
                        repeatMode = RotelRepeatMode.DISC;
                        updateChannelState(CHANNEL_REPEAT);
                    } else if (MSG_VALUE_OFF.equalsIgnoreCase(value)) {
                        repeatMode = RotelRepeatMode.OFF;
                        updateChannelState(CHANNEL_REPEAT);
                    } else {
                        throw new RotelException("Invalid value");
                    }
                    break;
                case KEY_PRESET_FM:
                case KEY_PRESET_DAB:
                case KEY_PRESET_IRADIO:
                    preset = Integer.parseInt(value);
                case KEY_FM_PRESET:
                case KEY_DAB_PRESET:
                case KEY_IRADIO_PRESET:
                    if (preset >= 1 && preset <= 30) {
                        radioPreset = preset;
                    } else {
                        radioPreset = 0;
                    }
                    updateChannelState(CHANNEL_RADIO_PRESET);
                    break;
                case KEY_FM:
                case KEY_DAB:
                    preset = Integer.parseInt(value);
                    if (preset >= 1 && preset <= 30) {
                        radioPreset = preset;
                        updateChannelState(CHANNEL_RADIO_PRESET);
                    }
                    break;
                case KEY_FREQ:
                case KEY_FREQ_ZONE1:
                case KEY_FREQ_ZONE2:
                case KEY_FREQ_ZONE3:
                case KEY_FREQ_ZONE4:
                    if (MSG_VALUE_OFF.equalsIgnoreCase(value) || MSG_VALUE_NONE.equalsIgnoreCase(value)) {
                        frequencies[numZone] = 0.0;
                    } else {
                        // Suppress a potential ending "k" or "K"
                        if (value.toUpperCase().endsWith("K")) {
                            value = value.substring(0, value.length() - 1);
                        }
                        frequencies[numZone] = Double.parseDouble(value);
                    }
                    if (numZone == 0) {
                        updateChannelState(CHANNEL_FREQUENCY);
                    } else {
                        updateGroupChannelState(numZone, CHANNEL_FREQUENCY);
                    }
                    break;
                case KEY_DIMMER:
                    brightness = Integer.parseInt(value);
                    updateChannelState(CHANNEL_BRIGHTNESS);
                    updateChannelState(CHANNEL_ALL_BRIGHTNESS);
                    break;
                case KEY_UPDATE_MODE:
                case KEY_DISPLAY_UPDATE:
                    break;
                case KEY_TONE:
                    if (MSG_VALUE_ON.equalsIgnoreCase(value)) {
                        tcbypass = false;
                        updateChannelState(CHANNEL_TCBYPASS);
                    } else if (MSG_VALUE_OFF.equalsIgnoreCase(value)) {
                        tcbypass = true;
                        updateChannelState(CHANNEL_TCBYPASS);
                    } else {
                        throw new RotelException("Invalid value");
                    }
                    break;
                case KEY_TCBYPASS:
                    if (MSG_VALUE_ON.equalsIgnoreCase(value)) {
                        tcbypass = true;
                        updateChannelState(CHANNEL_TCBYPASS);
                    } else if (MSG_VALUE_OFF.equalsIgnoreCase(value)) {
                        tcbypass = false;
                        updateChannelState(CHANNEL_TCBYPASS);
                    } else {
                        throw new RotelException("Invalid value");
                    }
                    break;
                case KEY_BALANCE:
                case KEY_BALANCE_ZONE1:
                case KEY_BALANCE_ZONE2:
                case KEY_BALANCE_ZONE3:
                case KEY_BALANCE_ZONE4:
                    if (MSG_VALUE_MIN.equalsIgnoreCase(value)) {
                        balances[numZone] = minBalanceLevel;
                    } else if (MSG_VALUE_MAX.equalsIgnoreCase(value)) {
                        balances[numZone] = maxBalanceLevel;
                    } else if (value.toUpperCase().startsWith("L")) {
                        balances[numZone] = -Integer.parseInt(value.substring(1));
                    } else if (value.toUpperCase().startsWith("R")) {
                        balances[numZone] = Integer.parseInt(value.substring(1));
                    } else {
                        balances[numZone] = Integer.parseInt(value);
                    }
                    if (numZone == 0) {
                        updateChannelState(CHANNEL_BALANCE);
                    } else {
                        updateGroupChannelState(numZone, CHANNEL_BALANCE);
                    }
                    break;
                case KEY_SPEAKER:
                    if (MSG_VALUE_SPEAKER_A.equalsIgnoreCase(value)) {
                        speakera = true;
                        speakerb = false;
                        updateChannelState(CHANNEL_SPEAKER_A);
                        updateChannelState(CHANNEL_SPEAKER_B);
                    } else if (MSG_VALUE_SPEAKER_B.equalsIgnoreCase(value)) {
                        speakera = false;
                        speakerb = true;
                        updateChannelState(CHANNEL_SPEAKER_A);
                        updateChannelState(CHANNEL_SPEAKER_B);
                    } else if (MSG_VALUE_SPEAKER_AB.equalsIgnoreCase(value)) {
                        speakera = true;
                        speakerb = true;
                        updateChannelState(CHANNEL_SPEAKER_A);
                        updateChannelState(CHANNEL_SPEAKER_B);
                    } else if (MSG_VALUE_OFF.equalsIgnoreCase(value)) {
                        speakera = false;
                        speakerb = false;
                        updateChannelState(CHANNEL_SPEAKER_A);
                        updateChannelState(CHANNEL_SPEAKER_B);
                    } else {
                        throw new RotelException("Invalid value");
                    }
                    break;
                case KEY_SUB_LEVEL:
                    logger.debug("Sub level is set to {}", value);
                    break;
                case KEY_CENTER_LEVEL:
                    logger.debug("Center level is set to {}", value);
                    break;
                case KEY_SURROUND_RIGHT_LEVEL:
                    logger.debug("Surround right level is set to {}", value);
                    break;
                case KEY_SURROUND_LEFT_LEVEL:
                    logger.debug("Surround left level is set to {}", value);
                    break;
                case KEY_CENTER_BACK_RIGHT_LEVEL:
                    logger.debug("Center back right level is set to {}", value);
                    break;
                case KEY_CENTER_BACK_LEFT_LEVEL:
                    logger.debug("Center back left level is set to {}", value);
                    break;
                case KEY_CEILING_FRONT_RIGHT_LEVEL:
                    logger.debug("Ceiling front right level is set to {}", value);
                    break;
                case KEY_CEILING_FRONT_LEFT_LEVEL:
                    logger.debug("Ceiling front left level is set to {}", value);
                    break;
                case KEY_CEILING_REAR_RIGHT_LEVEL:
                    logger.debug("Ceiling rear right level is set to {}", value);
                    break;
                case KEY_CEILING_REAR_LEFT_LEVEL:
                    logger.debug("Ceiling rear left level is set to {}", value);
                    break;
                case KEY_PCUSB_CLASS:
                    logger.debug("PC-USB Audio Class is set to {}", value);
                    break;
                case KEY_PRODUCT_TYPE:
                case KEY_MODEL:
                    getThing().setProperty(Thing.PROPERTY_MODEL_ID, value);
                    break;
                case KEY_PRODUCT_VERSION:
                case KEY_VERSION:
                    getThing().setProperty(Thing.PROPERTY_FIRMWARE_VERSION, value);
                    break;
                default:
                    logger.debug("onNewMessageEvent: unhandled key {}", key);
                    break;
            }
        } catch (NumberFormatException | RotelException e) {
            logger.debug("Invalid value {} for key {}", value, key);
        }
    }

    /**
     * Handle the received information that device power (main zone) is ON
     */
    private void handlePowerOn() {
        Boolean prev = powers[0];
        powers[0] = true;
        updateChannelState(CHANNEL_POWER);
        updateChannelState(CHANNEL_MAIN_POWER);
        updateChannelState(CHANNEL_ALL_POWER);
        if ((prev == null) || !prev) {
            schedulePowerOnJob();
        }
    }

    /**
     * Handle the received information that device power (main zone) is OFF
     */
    private void handlePowerOff() {
        cancelPowerOnZoneJob(0);
        powers[0] = false;
        updateChannelState(CHANNEL_POWER);
        updateChannelState(CHANNEL_SOURCE);
        updateChannelState(CHANNEL_DSP);
        updateChannelState(CHANNEL_VOLUME);
        updateChannelState(CHANNEL_MUTE);
        updateChannelState(CHANNEL_BASS);
        updateChannelState(CHANNEL_TREBLE);
        updateChannelState(CHANNEL_PLAY_CONTROL);
        updateChannelState(CHANNEL_TRACK);
        updateChannelState(CHANNEL_RANDOM);
        updateChannelState(CHANNEL_REPEAT);
        updateChannelState(CHANNEL_RADIO_PRESET);
        updateChannelState(CHANNEL_FREQUENCY);
        updateChannelState(CHANNEL_BRIGHTNESS);
        updateChannelState(CHANNEL_TCBYPASS);
        updateChannelState(CHANNEL_BALANCE);
        updateChannelState(CHANNEL_SPEAKER_A);
        updateChannelState(CHANNEL_SPEAKER_B);

        updateChannelState(CHANNEL_MAIN_POWER);
        updateChannelState(CHANNEL_MAIN_SOURCE);
        updateChannelState(CHANNEL_MAIN_RECORD_SOURCE);
        updateChannelState(CHANNEL_MAIN_DSP);
        updateChannelState(CHANNEL_MAIN_VOLUME);
        updateChannelState(CHANNEL_MAIN_VOLUME_UP_DOWN);
        updateChannelState(CHANNEL_MAIN_MUTE);
        updateChannelState(CHANNEL_MAIN_BASS);
        updateChannelState(CHANNEL_MAIN_TREBLE);

        updateChannelState(CHANNEL_ALL_POWER);
        updateChannelState(CHANNEL_ALL_BRIGHTNESS);
    }

    /**
     * Handle the received information that a zone power is ON
     */
    private void handlePowerOnZone(int numZone) {
        Boolean prev = powers[numZone];
        powers[numZone] = true;
        updateGroupChannelState(numZone, CHANNEL_POWER);
        if ((prev == null) || !prev) {
            schedulePowerOnZoneJob(numZone, getVolumeDownCommand(numZone), getVolumeUpCommand(numZone));
        }
    }

    /**
     * Handle the received information that a zone power is OFF
     */
    private void handlePowerOffZone(int numZone) {
        cancelPowerOnZoneJob(numZone);
        powers[numZone] = false;
        updateGroupChannelState(numZone, CHANNEL_POWER);
        updateGroupChannelState(numZone, CHANNEL_SOURCE);
        updateGroupChannelState(numZone, CHANNEL_VOLUME);
        updateGroupChannelState(numZone, CHANNEL_MUTE);
        updateGroupChannelState(numZone, CHANNEL_BASS);
        updateGroupChannelState(numZone, CHANNEL_TREBLE);
        updateGroupChannelState(numZone, CHANNEL_BALANCE);
        updateGroupChannelState(numZone, CHANNEL_FREQUENCY);
        updateGroupChannelState(numZone, CHANNEL_VOLUME_UP_DOWN);
    }

    /**
     * Schedule the job that will consider the device as OFF if no new event is received before its running
     *
     * @param switchOffAllZones true if all zones have to be considered as OFF
     */
    private void schedulePowerOffJob(boolean switchOffAllZones) {
        logger.debug("Schedule power OFF job");
        cancelPowerOffJob();
        powerOffJob = scheduler.schedule(() -> {
            logger.debug("Power OFF job");
            handlePowerOff();
            if (switchOffAllZones) {
                for (int zone = 1; zone <= model.getNumberOfZones(); zone++) {
                    handlePowerOffZone(zone);
                }
            }
        }, 2000, TimeUnit.MILLISECONDS);
    }

    /**
     * Cancel the job that will consider the device as OFF
     */
    private void cancelPowerOffJob() {
        ScheduledFuture<?> powerOffJob = this.powerOffJob;
        if (powerOffJob != null && !powerOffJob.isCancelled()) {
            powerOffJob.cancel(true);
            this.powerOffJob = null;
        }
    }

    /**
     * Schedule the job to run with a few seconds delay when the device power (main zone) switched ON
     */
    private void schedulePowerOnJob() {
        logger.debug("Schedule power ON job");
        cancelPowerOnZoneJob(0);
        powerOnZoneJobs[0] = scheduler.schedule(() -> {
            synchronized (sequenceLock) {
                logger.debug("Power ON job");
                try {
                    switch (protocol) {
                        case HEX:
                            if (model.getRespNbChars() <= 13 && model.hasVolumeControl()) {
                                sendCommand(getVolumeDownCommand(0));
                                Thread.sleep(100);
                                sendCommand(getVolumeUpCommand(0));
                                Thread.sleep(100);
                            }
                            if (model.getNumberOfZones() > 1) {
                                if (currentZone != 1
                                        && model.getZoneSelectCmd() == RotelCommand.RECORD_FONCTION_SELECT) {
                                    selectZone(1, model.getZoneSelectCmd());
                                } else if (!selectingRecord) {
                                    sendCommand(RotelCommand.RECORD_FONCTION_SELECT);
                                    Thread.sleep(100);
                                }
                            } else {
                                sendCommand(RotelCommand.RECORD_FONCTION_SELECT);
                                Thread.sleep(100);
                            }
                            if (model.hasToneControl()) {
                                if (model == RotelModel.RSX1065) {
                                    // No tone control select command
                                    sendCommand(RotelCommand.TREBLE_DOWN);
                                    Thread.sleep(100);
                                    sendCommand(RotelCommand.TREBLE_UP);
                                    Thread.sleep(100);
                                    sendCommand(RotelCommand.BASS_DOWN);
                                    Thread.sleep(100);
                                    sendCommand(RotelCommand.BASS_UP);
                                    Thread.sleep(100);
                                } else {
                                    selectFeature(2, null, RotelCommand.TONE_CONTROL_SELECT);
                                }
                            }
                            break;
                        case ASCII_V1:
                            if (model != RotelModel.RAP1580 && model != RotelModel.RDD1580
                                    && model != RotelModel.RSP1576 && model != RotelModel.RSP1582) {
                                sendCommand(RotelCommand.UPDATE_AUTO);
                                Thread.sleep(SLEEP_INTV);
                            }
                            if (model.hasSourceControl()) {
                                sendCommand(RotelCommand.SOURCE);
                                Thread.sleep(SLEEP_INTV);
                            }
                            if (model.hasVolumeControl() || model.hasToneControl()) {
                                if (model.hasVolumeControl() && model != RotelModel.RAP1580
                                        && model != RotelModel.RSP1576 && model != RotelModel.RSP1582) {
                                    sendCommand(RotelCommand.VOLUME_GET_MIN);
                                    Thread.sleep(SLEEP_INTV);
                                    sendCommand(RotelCommand.VOLUME_GET_MAX);
                                    Thread.sleep(SLEEP_INTV);
                                }
                                if (model.hasToneControl()) {
                                    sendCommand(RotelCommand.TONE_MAX);
                                    Thread.sleep(SLEEP_INTV);
                                }
                                // Wait enough to be sure to get the min/max values requested just before
                                Thread.sleep(250);
                                if (model.hasVolumeControl()) {
                                    sendCommand(RotelCommand.VOLUME_GET);
                                    Thread.sleep(SLEEP_INTV);
                                    if (model != RotelModel.RA11 && model != RotelModel.RA12
                                            && model != RotelModel.RCX1500) {
                                        sendCommand(RotelCommand.MUTE);
                                        Thread.sleep(SLEEP_INTV);
                                    }
                                }
                                if (model.hasToneControl()) {
                                    sendCommand(RotelCommand.BASS);
                                    Thread.sleep(SLEEP_INTV);
                                    sendCommand(RotelCommand.TREBLE);
                                    Thread.sleep(SLEEP_INTV);
                                    if (model.canGetBypassStatus()) {
                                        sendCommand(RotelCommand.TONE_CONTROLS);
                                        Thread.sleep(SLEEP_INTV);
                                    }
                                }
                            }
                            if (model.hasBalanceControl()) {
                                sendCommand(RotelCommand.BALANCE);
                                Thread.sleep(SLEEP_INTV);
                            }
                            if (model.hasPlayControl()) {
                                RotelSource source = sources[0];
                                if (model != RotelModel.RCD1570 && model != RotelModel.RCD1572
                                        && (model != RotelModel.RCX1500 || source == null
                                                || !"CD".equals(source.getName()))) {
                                    sendCommand(RotelCommand.PLAY_STATUS);
                                    Thread.sleep(SLEEP_INTV);
                                } else {
                                    sendCommand(RotelCommand.CD_PLAY_STATUS);
                                    Thread.sleep(SLEEP_INTV);
                                }
                            }
                            if (model.hasDspControl()) {
                                sendCommand(RotelCommand.DSP_MODE);
                                Thread.sleep(SLEEP_INTV);
                            }
                            if (model.canGetFrequency()) {
                                sendCommand(RotelCommand.FREQUENCY);
                                Thread.sleep(SLEEP_INTV);
                            }
                            if (model.hasDimmerControl() && model.canGetDimmerLevel()) {
                                sendCommand(RotelCommand.DIMMER_LEVEL_GET);
                                Thread.sleep(SLEEP_INTV);
                            }
                            if (model.hasSpeakerGroups()) {
                                sendCommand(RotelCommand.SPEAKER);
                                Thread.sleep(SLEEP_INTV);
                            }
                            if (model != RotelModel.RAP1580 && model != RotelModel.RSP1576
                                    && model != RotelModel.RSP1582) {
                                sendCommand(RotelCommand.MODEL);
                                Thread.sleep(SLEEP_INTV);
                                sendCommand(RotelCommand.VERSION);
                                Thread.sleep(SLEEP_INTV);
                            }
                            break;
                        case ASCII_V2:
                            sendCommand(RotelCommand.UPDATE_AUTO);
                            Thread.sleep(SLEEP_INTV);
                            if (model.hasSourceControl()) {
                                if (model.getNumberOfZones() > 1) {
                                    sendCommand(RotelCommand.INPUT);
                                } else {
                                    sendCommand(RotelCommand.SOURCE);
                                }
                                Thread.sleep(SLEEP_INTV);
                            }
                            if (model.hasVolumeControl()) {
                                sendCommand(RotelCommand.VOLUME_GET);
                                Thread.sleep(SLEEP_INTV);
                                sendCommand(RotelCommand.MUTE);
                                Thread.sleep(SLEEP_INTV);
                            }
                            if (model.hasToneControl()) {
                                sendCommand(RotelCommand.BASS);
                                Thread.sleep(SLEEP_INTV);
                                sendCommand(RotelCommand.TREBLE);
                                Thread.sleep(SLEEP_INTV);
                                if (model.canGetBypassStatus()) {
                                    sendCommand(RotelCommand.TCBYPASS);
                                    Thread.sleep(SLEEP_INTV);
                                }
                            }
                            if (model.hasBalanceControl()) {
                                sendCommand(RotelCommand.BALANCE);
                                Thread.sleep(SLEEP_INTV);
                            }
                            if (model.hasPlayControl()) {
                                sendCommand(RotelCommand.PLAY_STATUS);
                                Thread.sleep(SLEEP_INTV);
                                RotelSource source = sources[0];
                                if (source != null && "CD".equals(source.getName()) && !model.hasSourceControl()) {
                                    sendCommand(RotelCommand.TRACK);
                                    Thread.sleep(SLEEP_INTV);
                                    sendCommand(RotelCommand.RANDOM_MODE);
                                    Thread.sleep(SLEEP_INTV);
                                    sendCommand(RotelCommand.REPEAT_MODE);
                                    Thread.sleep(SLEEP_INTV);
                                }
                            }
                            if (model.hasDspControl()) {
                                sendCommand(RotelCommand.DSP_MODE);
                                Thread.sleep(SLEEP_INTV);
                            }
                            if (model.canGetFrequency()) {
                                sendCommand(RotelCommand.FREQUENCY);
                                Thread.sleep(SLEEP_INTV);
                            }
                            if (model.hasDimmerControl() && model.canGetDimmerLevel()) {
                                sendCommand(RotelCommand.DIMMER_LEVEL_GET);
                                Thread.sleep(SLEEP_INTV);
                            }
                            if (model.hasSpeakerGroups()) {
                                sendCommand(RotelCommand.SPEAKER);
                                Thread.sleep(SLEEP_INTV);
                            }
                            sendCommand(RotelCommand.MODEL);
                            Thread.sleep(SLEEP_INTV);
                            sendCommand(RotelCommand.VERSION);
                            Thread.sleep(SLEEP_INTV);
                            break;
                    }
                } catch (RotelException e) {
                    logger.debug("Init sequence failed: {}", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.comm-error-init-sequence");
                    closeConnection();
                } catch (InterruptedException e) {
                    logger.debug("Init sequence interrupted: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }, 2500, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedule the job to run with a few seconds delay when the zone power switched ON
     */
    private void schedulePowerOnZoneJob(int numZone, RotelCommand volumeDown, RotelCommand volumeUp) {
        logger.debug("Schedule power ON zone {} job", numZone);
        cancelPowerOnZoneJob(numZone);
        powerOnZoneJobs[numZone] = scheduler.schedule(() -> {
            synchronized (sequenceLock) {
                logger.debug("Power ON zone {} job", numZone);
                try {
                    if (protocol == RotelProtocol.HEX && model.getNumberOfZones() >= numZone) {
                        selectZone(numZone, model.getZoneSelectCmd());
                        sendCommand(model.hasZoneCommands(numZone) ? volumeDown : RotelCommand.VOLUME_DOWN);
                        Thread.sleep(100);
                        sendCommand(model.hasZoneCommands(numZone) ? volumeUp : RotelCommand.VOLUME_UP);
                        Thread.sleep(100);
                    }
                } catch (RotelException e) {
                    logger.debug("Init sequence zone {} failed: {}", numZone, e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            String.format("@text/offline.comm-error-init-sequence-zone [\"%d\"]", numZone));
                    closeConnection();
                } catch (InterruptedException e) {
                    logger.debug("Init sequence zone {} interrupted: {}", numZone, e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }, 2500, TimeUnit.MILLISECONDS);
    }

    /**
     * Cancel the job scheduled when the device power (main zone) or a zone power switched ON
     */
    private void cancelPowerOnZoneJob(int numZone) {
        ScheduledFuture<?> powerOnZoneJob = powerOnZoneJobs[numZone];
        if (powerOnZoneJob != null && !powerOnZoneJob.isCancelled()) {
            powerOnZoneJob.cancel(true);
            powerOnZoneJobs[numZone] = null;
        }
    }

    /**
     * Schedule the reconnection job
     */
    private void scheduleReconnectJob() {
        logger.debug("Schedule reconnect job");
        cancelReconnectJob();
        reconnectJob = scheduler.scheduleWithFixedDelay(() -> {
            if (!connector.isConnected()) {
                logger.debug("Trying to reconnect...");
                closeConnection();
                powers[0] = null;
                String error = null;
                if (openConnection()) {
                    synchronized (sequenceLock) {
                        schedulePowerOffJob(true);
                        try {
                            sendCommand(model.getPowerStateCmd());
                        } catch (RotelException e) {
                            error = "@text/offline.comm-error-first-command-after-reconnection";
                            logger.debug("First command after connection failed", e);
                            cancelPowerOffJob();
                            closeConnection();
                        }
                    }
                } else {
                    error = "@text/offline.comm-error-reconnection";
                }
                if (error != null) {
                    handlePowerOff();
                    for (int zone = 1; zone <= model.getNumberOfZones(); zone++) {
                        handlePowerOffZone(zone);
                    }
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
                } else {
                    updateStatus(ThingStatus.ONLINE);
                }
            }
        }, 1, POLLING_INTERVAL, TimeUnit.SECONDS);
    }

    /**
     * Cancel the reconnection job
     */
    private void cancelReconnectJob() {
        ScheduledFuture<?> reconnectJob = this.reconnectJob;
        if (reconnectJob != null && !reconnectJob.isCancelled()) {
            reconnectJob.cancel(true);
            this.reconnectJob = null;
        }
    }

    private void updateGroupChannelState(int numZone, String channel) {
        updateChannelState(String.format("zone%d#%s", numZone, channel));
    }

    /**
     * Update the state of a channel
     *
     * @param channel the channel
     */
    private void updateChannelState(String channel) {
        if (!isLinked(channel)) {
            return;
        }
        State state = UnDefType.UNDEF;
        RotelSource localSource;
        int numZone = 0;
        switch (channel) {
            case CHANNEL_ZONE1_SOURCE:
            case CHANNEL_ZONE1_VOLUME:
            case CHANNEL_ZONE1_MUTE:
            case CHANNEL_ZONE1_BASS:
            case CHANNEL_ZONE1_TREBLE:
            case CHANNEL_ZONE1_BALANCE:
            case CHANNEL_ZONE1_FREQUENCY:
                numZone = 1;
                break;
            case CHANNEL_ZONE2_POWER:
            case CHANNEL_ZONE2_SOURCE:
            case CHANNEL_ZONE2_VOLUME:
            case CHANNEL_ZONE2_VOLUME_UP_DOWN:
            case CHANNEL_ZONE2_MUTE:
            case CHANNEL_ZONE2_BASS:
            case CHANNEL_ZONE2_TREBLE:
            case CHANNEL_ZONE2_BALANCE:
            case CHANNEL_ZONE2_FREQUENCY:
                numZone = 2;
                break;
            case CHANNEL_ZONE3_POWER:
            case CHANNEL_ZONE3_SOURCE:
            case CHANNEL_ZONE3_VOLUME:
            case CHANNEL_ZONE3_MUTE:
            case CHANNEL_ZONE3_BASS:
            case CHANNEL_ZONE3_TREBLE:
            case CHANNEL_ZONE3_BALANCE:
            case CHANNEL_ZONE3_FREQUENCY:
                numZone = 3;
                break;
            case CHANNEL_ZONE4_POWER:
            case CHANNEL_ZONE4_SOURCE:
            case CHANNEL_ZONE4_VOLUME:
            case CHANNEL_ZONE4_MUTE:
            case CHANNEL_ZONE4_BASS:
            case CHANNEL_ZONE4_TREBLE:
            case CHANNEL_ZONE4_BALANCE:
            case CHANNEL_ZONE4_FREQUENCY:
                numZone = 4;
                break;
            default:
                break;
        }
        switch (channel) {
            case CHANNEL_POWER:
            case CHANNEL_MAIN_POWER:
            case CHANNEL_ALL_POWER:
            case CHANNEL_ZONE2_POWER:
            case CHANNEL_ZONE3_POWER:
            case CHANNEL_ZONE4_POWER:
                Boolean powerZone = powers[numZone];
                if (powerZone != null) {
                    state = OnOffType.from(powerZone.booleanValue());
                }
                break;
            case CHANNEL_SOURCE:
            case CHANNEL_MAIN_SOURCE:
            case CHANNEL_ZONE1_SOURCE:
            case CHANNEL_ZONE2_SOURCE:
            case CHANNEL_ZONE3_SOURCE:
            case CHANNEL_ZONE4_SOURCE:
                localSource = sources[numZone];
                if (isPowerOn(numZone) && localSource != null) {
                    state = new StringType(localSource.getName());
                }
                break;
            case CHANNEL_MAIN_RECORD_SOURCE:
                localSource = recordSource;
                if (isPowerOn() && localSource != null) {
                    state = new StringType(localSource.getName());
                }
                break;
            case CHANNEL_DSP:
            case CHANNEL_MAIN_DSP:
                if (isPowerOn()) {
                    state = new StringType(dsp.getName());
                }
                break;
            case CHANNEL_VOLUME:
            case CHANNEL_MAIN_VOLUME:
            case CHANNEL_ZONE1_VOLUME:
            case CHANNEL_ZONE2_VOLUME:
            case CHANNEL_ZONE3_VOLUME:
            case CHANNEL_ZONE4_VOLUME:
                if (isPowerOn(numZone) && !fixedVolumeZones[numZone]) {
                    long volumePct = Math
                            .round((double) (volumes[numZone] - minVolume) / (double) (maxVolume - minVolume) * 100.0);
                    state = new PercentType(BigDecimal.valueOf(volumePct));
                }
                break;
            case CHANNEL_MAIN_VOLUME_UP_DOWN:
            case CHANNEL_ZONE2_VOLUME_UP_DOWN:
                if (isPowerOn(numZone) && !fixedVolumeZones[numZone]) {
                    state = new DecimalType(volumes[numZone]);
                }
                break;
            case CHANNEL_MUTE:
            case CHANNEL_MAIN_MUTE:
            case CHANNEL_ZONE1_MUTE:
            case CHANNEL_ZONE2_MUTE:
            case CHANNEL_ZONE3_MUTE:
            case CHANNEL_ZONE4_MUTE:
                if (isPowerOn(numZone)) {
                    state = OnOffType.from(mutes[numZone]);
                }
                break;
            case CHANNEL_BASS:
            case CHANNEL_MAIN_BASS:
            case CHANNEL_ZONE1_BASS:
            case CHANNEL_ZONE2_BASS:
            case CHANNEL_ZONE3_BASS:
            case CHANNEL_ZONE4_BASS:
                if (isPowerOn(numZone)) {
                    state = new DecimalType(basses[numZone]);
                }
                break;
            case CHANNEL_TREBLE:
            case CHANNEL_MAIN_TREBLE:
            case CHANNEL_ZONE1_TREBLE:
            case CHANNEL_ZONE2_TREBLE:
            case CHANNEL_ZONE3_TREBLE:
            case CHANNEL_ZONE4_TREBLE:
                if (isPowerOn(numZone)) {
                    state = new DecimalType(trebles[numZone]);
                }
                break;
            case CHANNEL_TRACK:
                if (isPowerOn() && track > 0) {
                    state = new DecimalType(track);
                }
                break;
            case CHANNEL_RANDOM:
                if (isPowerOn()) {
                    state = OnOffType.from(randomMode);
                }
                break;
            case CHANNEL_REPEAT:
                if (isPowerOn()) {
                    state = new StringType(repeatMode.name());
                }
                break;
            case CHANNEL_PLAY_CONTROL:
                if (isPowerOn()) {
                    switch (playStatus) {
                        case PLAYING:
                            state = PlayPauseType.PLAY;
                            break;
                        case PAUSED:
                        case STOPPED:
                            state = PlayPauseType.PAUSE;
                            break;
                    }
                }
                break;
            case CHANNEL_RADIO_PRESET:
                if (isPowerOn()) {
                    state = radioPreset == 0 ? UnDefType.UNDEF : new DecimalType(radioPreset);
                }
                break;
            case CHANNEL_FREQUENCY:
            case CHANNEL_ZONE1_FREQUENCY:
            case CHANNEL_ZONE2_FREQUENCY:
            case CHANNEL_ZONE3_FREQUENCY:
            case CHANNEL_ZONE4_FREQUENCY:
                if (isPowerOn(numZone) && frequencies[numZone] > 0.0) {
                    state = new DecimalType(frequencies[numZone]);
                }
                break;
            case CHANNEL_LINE1:
                state = new StringType(frontPanelLine1);
                break;
            case CHANNEL_LINE2:
                state = new StringType(frontPanelLine2);
                break;
            case CHANNEL_BRIGHTNESS:
            case CHANNEL_ALL_BRIGHTNESS:
                if (isPowerOn() && model.hasDimmerControl()) {
                    long dimmerPct = Math.round((double) (brightness - model.getDimmerLevelMin())
                            / (double) (model.getDimmerLevelMax() - model.getDimmerLevelMin()) * 100.0);
                    state = new PercentType(BigDecimal.valueOf(dimmerPct));
                }
                break;
            case CHANNEL_TCBYPASS:
                if (isPowerOn()) {
                    state = OnOffType.from(tcbypass);
                }
                break;
            case CHANNEL_BALANCE:
            case CHANNEL_ZONE1_BALANCE:
            case CHANNEL_ZONE2_BALANCE:
            case CHANNEL_ZONE3_BALANCE:
            case CHANNEL_ZONE4_BALANCE:
                if (isPowerOn(numZone)) {
                    state = new DecimalType(balances[numZone]);
                }
                break;
            case CHANNEL_SPEAKER_A:
                if (isPowerOn()) {
                    state = OnOffType.from(speakera);
                }
                break;
            case CHANNEL_SPEAKER_B:
                if (isPowerOn()) {
                    state = OnOffType.from(speakerb);
                }
                break;
            default:
                break;
        }
        updateState(channel, state);
    }

    /**
     * Inform about the device / main zone power state
     *
     * @return true if device / main zone power state is known and known as ON
     */
    private boolean isPowerOn() {
        return isPowerOn(0);
    }

    /**
     * Inform about the power state
     *
     * @param numZone the zone number (1-4) or 0 for the device or main zone
     *
     * @return true if power state is known and known as ON
     */
    private boolean isPowerOn(int numZone) {
        if (numZone < 0 || numZone > MAX_NUMBER_OF_ZONES) {
            throw new IllegalArgumentException("numZone must be in range 0-" + MAX_NUMBER_OF_ZONES);
        }
        Boolean power = powers[numZone];
        return (numZone > 0 && !powerControlPerZone) ? isPowerOn(0) : power != null && power.booleanValue();
    }

    /**
     * Get the command to be used for POWER ON
     *
     * @param numZone the zone number (2-4) or 0 for the device or main zone
     *
     * @return the command
     */
    private RotelCommand getPowerOnCommand(int numZone) {
        switch (numZone) {
            case 0:
                return model.hasOtherThanPrimaryCommands() ? RotelCommand.MAIN_ZONE_POWER_ON : RotelCommand.POWER_ON;
            case 2:
                return RotelCommand.ZONE2_POWER_ON;
            case 3:
                return RotelCommand.ZONE3_POWER_ON;
            case 4:
                return RotelCommand.ZONE4_POWER_ON;
            default:
                throw new IllegalArgumentException("No power ON command defined for zone " + numZone);
        }
    }

    /**
     * Get the command to be used for POWER OFF
     *
     * @param numZone the zone number (2-4) or 0 for the device or main zone
     *
     * @return the command
     */
    private RotelCommand getPowerOffCommand(int numZone) {
        switch (numZone) {
            case 0:
                return model.hasOtherThanPrimaryCommands() ? RotelCommand.MAIN_ZONE_POWER_OFF : RotelCommand.POWER_OFF;
            case 2:
                return RotelCommand.ZONE2_POWER_OFF;
            case 3:
                return RotelCommand.ZONE3_POWER_OFF;
            case 4:
                return RotelCommand.ZONE4_POWER_OFF;
            default:
                throw new IllegalArgumentException("No power OFF command defined for zone " + numZone);
        }
    }

    /**
     * Get the command to be used for VOLUME UP
     *
     * @param numZone the zone number (1-4) or 0 for the device or main zone
     *
     * @return the command
     */
    private RotelCommand getVolumeUpCommand(int numZone) {
        switch (numZone) {
            case 0:
                return model.hasOtherThanPrimaryCommands() ? RotelCommand.MAIN_ZONE_VOLUME_UP : RotelCommand.VOLUME_UP;
            case 1:
                return RotelCommand.ZONE1_VOLUME_UP;
            case 2:
                return RotelCommand.ZONE2_VOLUME_UP;
            case 3:
                return RotelCommand.ZONE3_VOLUME_UP;
            case 4:
                return RotelCommand.ZONE4_VOLUME_UP;
            default:
                throw new IllegalArgumentException("No VOLUME UP command defined for zone " + numZone);
        }
    }

    /**
     * Get the command to be used for VOLUME DOWN
     *
     * @param numZone the zone number (1-4) or 0 for the device or main zone
     *
     * @return the command
     */
    private RotelCommand getVolumeDownCommand(int numZone) {
        switch (numZone) {
            case 0:
                return model.hasOtherThanPrimaryCommands() ? RotelCommand.MAIN_ZONE_VOLUME_DOWN
                        : RotelCommand.VOLUME_DOWN;
            case 1:
                return RotelCommand.ZONE1_VOLUME_DOWN;
            case 2:
                return RotelCommand.ZONE2_VOLUME_DOWN;
            case 3:
                return RotelCommand.ZONE3_VOLUME_DOWN;
            case 4:
                return RotelCommand.ZONE4_VOLUME_DOWN;
            default:
                throw new IllegalArgumentException("No VOLUME DOWN command defined for zone " + numZone);
        }
    }

    /**
     * Get the command to be used for VOLUME SET
     *
     * @param numZone the zone number (1-4) or 0 for the device
     *
     * @return the command
     */
    private RotelCommand getVolumeSetCommand(int numZone) {
        switch (numZone) {
            case 0:
                return RotelCommand.VOLUME_SET;
            case 1:
                return RotelCommand.ZONE1_VOLUME_SET;
            case 2:
                return RotelCommand.ZONE2_VOLUME_SET;
            case 3:
                return RotelCommand.ZONE3_VOLUME_SET;
            case 4:
                return RotelCommand.ZONE4_VOLUME_SET;
            default:
                throw new IllegalArgumentException("No VOLUME SET command defined for zone " + numZone);
        }
    }

    /**
     * Get the command to be used for MUTE ON
     *
     * @param numZone the zone number (1-4) or 0 for the device or main zone
     *
     * @return the command
     */
    private RotelCommand getMuteOnCommand(int numZone) {
        switch (numZone) {
            case 0:
                return model.hasOtherThanPrimaryCommands() ? RotelCommand.MAIN_ZONE_MUTE_ON : RotelCommand.MUTE_ON;
            case 1:
                return RotelCommand.ZONE1_MUTE_ON;
            case 2:
                return RotelCommand.ZONE2_MUTE_ON;
            case 3:
                return RotelCommand.ZONE3_MUTE_ON;
            case 4:
                return RotelCommand.ZONE4_MUTE_ON;
            default:
                throw new IllegalArgumentException("No MUTE ON command defined for zone " + numZone);
        }
    }

    /**
     * Get the command to be used for MUTE OFF
     *
     * @param numZone the zone number (1-4) or 0 for the device or main zone
     *
     * @return the command
     */
    private RotelCommand getMuteOffCommand(int numZone) {
        switch (numZone) {
            case 0:
                return model.hasOtherThanPrimaryCommands() ? RotelCommand.MAIN_ZONE_MUTE_OFF : RotelCommand.MUTE_OFF;
            case 1:
                return RotelCommand.ZONE1_MUTE_OFF;
            case 2:
                return RotelCommand.ZONE2_MUTE_OFF;
            case 3:
                return RotelCommand.ZONE3_MUTE_OFF;
            case 4:
                return RotelCommand.ZONE4_MUTE_OFF;
            default:
                throw new IllegalArgumentException("No MUTE OFF command defined for zone " + numZone);
        }
    }

    /**
     * Get the command to be used for MUTE TOGGLE
     *
     * @param numZone the zone number (1-4) or 0 for the device or main zone
     *
     * @return the command
     */
    private RotelCommand getMuteToggleCommand(int numZone) {
        switch (numZone) {
            case 0:
                return model.hasOtherThanPrimaryCommands() ? RotelCommand.MAIN_ZONE_MUTE_TOGGLE
                        : RotelCommand.MUTE_TOGGLE;
            case 1:
                return RotelCommand.ZONE1_MUTE_TOGGLE;
            case 2:
                return RotelCommand.ZONE2_MUTE_TOGGLE;
            case 3:
                return RotelCommand.ZONE3_MUTE_TOGGLE;
            case 4:
                return RotelCommand.ZONE4_MUTE_TOGGLE;
            default:
                throw new IllegalArgumentException("No MUTE TOGGLE command defined for zone " + numZone);
        }
    }

    /**
     * Get the command to be used for BASS UP
     *
     * @param numZone the zone number (1-4) or 0 for the device
     *
     * @return the command
     */
    private RotelCommand getBassUpCommand(int numZone) {
        switch (numZone) {
            case 0:
                return RotelCommand.BASS_UP;
            case 1:
                return RotelCommand.ZONE1_BASS_UP;
            case 2:
                return RotelCommand.ZONE2_BASS_UP;
            case 3:
                return RotelCommand.ZONE3_BASS_UP;
            case 4:
                return RotelCommand.ZONE4_BASS_UP;
            default:
                throw new IllegalArgumentException("No BASS UP command defined for zone " + numZone);
        }
    }

    /**
     * Get the command to be used for BASS DOWN
     *
     * @param numZone the zone number (1-4) or 0 for the device
     *
     * @return the command
     */
    private RotelCommand getBassDownCommand(int numZone) {
        switch (numZone) {
            case 0:
                return RotelCommand.BASS_DOWN;
            case 1:
                return RotelCommand.ZONE1_BASS_DOWN;
            case 2:
                return RotelCommand.ZONE2_BASS_DOWN;
            case 3:
                return RotelCommand.ZONE3_BASS_DOWN;
            case 4:
                return RotelCommand.ZONE4_BASS_DOWN;
            default:
                throw new IllegalArgumentException("No BASS DOWN command defined for zone " + numZone);
        }
    }

    /**
     * Get the command to be used for BASS SET
     *
     * @param numZone the zone number (1-4) or 0 for the device
     *
     * @return the command
     */
    private RotelCommand getBassSetCommand(int numZone) {
        switch (numZone) {
            case 0:
                return RotelCommand.BASS_SET;
            case 1:
                return RotelCommand.ZONE1_BASS_SET;
            case 2:
                return RotelCommand.ZONE2_BASS_SET;
            case 3:
                return RotelCommand.ZONE3_BASS_SET;
            case 4:
                return RotelCommand.ZONE4_BASS_SET;
            default:
                throw new IllegalArgumentException("No BASS SET command defined for zone " + numZone);
        }
    }

    /**
     * Get the command to be used for TREBLE UP
     *
     * @param numZone the zone number (1-4) or 0 for the device
     *
     * @return the command
     */
    private RotelCommand getTrebleUpCommand(int numZone) {
        switch (numZone) {
            case 0:
                return RotelCommand.TREBLE_UP;
            case 1:
                return RotelCommand.ZONE1_TREBLE_UP;
            case 2:
                return RotelCommand.ZONE2_TREBLE_UP;
            case 3:
                return RotelCommand.ZONE3_TREBLE_UP;
            case 4:
                return RotelCommand.ZONE4_TREBLE_UP;
            default:
                throw new IllegalArgumentException("No TREBLE UP command defined for zone " + numZone);
        }
    }

    /**
     * Get the command to be used for TREBLE DOWN
     *
     * @param numZone the zone number (1-4) or 0 for the device
     *
     * @return the command
     */
    private RotelCommand getTrebleDownCommand(int numZone) {
        switch (numZone) {
            case 0:
                return RotelCommand.TREBLE_DOWN;
            case 1:
                return RotelCommand.ZONE1_TREBLE_DOWN;
            case 2:
                return RotelCommand.ZONE2_TREBLE_DOWN;
            case 3:
                return RotelCommand.ZONE3_TREBLE_DOWN;
            case 4:
                return RotelCommand.ZONE4_TREBLE_DOWN;
            default:
                throw new IllegalArgumentException("No TREBLE DOWN command defined for zone " + numZone);
        }
    }

    /**
     * Get the command to be used for TREBLE SET
     *
     * @param numZone the zone number (1-4) or 0 for the device
     *
     * @return the command
     */
    private RotelCommand getTrebleSetCommand(int numZone) {
        switch (numZone) {
            case 0:
                return RotelCommand.TREBLE_SET;
            case 1:
                return RotelCommand.ZONE1_TREBLE_SET;
            case 2:
                return RotelCommand.ZONE2_TREBLE_SET;
            case 3:
                return RotelCommand.ZONE3_TREBLE_SET;
            case 4:
                return RotelCommand.ZONE4_TREBLE_SET;
            default:
                throw new IllegalArgumentException("No TREBLE SET command defined for zone " + numZone);
        }
    }

    /**
     * Get the command to be used for BALANCE LEFT
     *
     * @param numZone the zone number (1-4) or 0 for the device
     *
     * @return the command
     */
    private RotelCommand getBalanceLeftCommand(int numZone) {
        switch (numZone) {
            case 0:
                return RotelCommand.BALANCE_LEFT;
            case 1:
                return RotelCommand.ZONE1_BALANCE_LEFT;
            case 2:
                return RotelCommand.ZONE2_BALANCE_LEFT;
            case 3:
                return RotelCommand.ZONE3_BALANCE_LEFT;
            case 4:
                return RotelCommand.ZONE4_BALANCE_LEFT;
            default:
                throw new IllegalArgumentException("No BALANCE LEFT command defined for zone " + numZone);
        }
    }

    /**
     * Get the command to be used for BALANCE RIGHT
     *
     * @param numZone the zone number (1-4) or 0 for the device
     *
     * @return the command
     */
    private RotelCommand getBalanceRightCommand(int numZone) {
        switch (numZone) {
            case 0:
                return RotelCommand.BALANCE_RIGHT;
            case 1:
                return RotelCommand.ZONE1_BALANCE_RIGHT;
            case 2:
                return RotelCommand.ZONE2_BALANCE_RIGHT;
            case 3:
                return RotelCommand.ZONE3_BALANCE_RIGHT;
            case 4:
                return RotelCommand.ZONE4_BALANCE_RIGHT;
            default:
                throw new IllegalArgumentException("No BALANCE RIGHT command defined for zone " + numZone);
        }
    }

    /**
     * Get the command to be used for BALANCE SET
     *
     * @param numZone the zone number (1-4) or 0 for the device
     *
     * @return the command
     */
    private RotelCommand getBalanceSetCommand(int numZone) {
        switch (numZone) {
            case 0:
                return RotelCommand.BALANCE_SET;
            case 1:
                return RotelCommand.ZONE1_BALANCE_SET;
            case 2:
                return RotelCommand.ZONE2_BALANCE_SET;
            case 3:
                return RotelCommand.ZONE3_BALANCE_SET;
            case 4:
                return RotelCommand.ZONE4_BALANCE_SET;
            default:
                throw new IllegalArgumentException("No BALANCE SET command defined for zone " + numZone);
        }
    }

    private @Nullable RotelCommand getRadioPresetGetCommand(RotelSource source) {
        if (protocol == RotelProtocol.ASCII_V1) {
            switch (source.getName()) {
                case "FM":
                case "DAB":
                case "IRADIO":
                    return RotelCommand.PRESET;
                default:
                    break;
            }
        } else if (protocol == RotelProtocol.ASCII_V2) {
            switch (source.getName()) {
                case "FM":
                    return RotelCommand.FM_PRESET;
                case "DAB":
                    return RotelCommand.DAB_PRESET;
                default:
                    break;
            }
        }
        return null;
    }

    private @Nullable RotelCommand getRadioPresetCallCommand(RotelSource source) {
        switch (source.getName()) {
            case "FM":
                return RotelCommand.CALL_FM_PRESET;
            case "DAB":
                return RotelCommand.CALL_DAB_PRESET;
            case "IRADIO":
                return RotelCommand.CALL_IRADIO_PRESET;
            default:
                break;
        }
        return null;
    }

    private void sendCommand(RotelCommand cmd) throws RotelException {
        sendCommand(cmd, null);
    }

    /**
     * Request the Rotel device to execute a command
     *
     * @param cmd the command to execute
     * @param value the integer value to consider for volume, bass or treble adjustment
     *
     * @throws RotelException - In case of any problem
     */
    private void sendCommand(RotelCommand cmd, @Nullable Integer value) throws RotelException {
        byte[] message;
        try {
            message = protocolHandler.buildCommandMessage(cmd, value);
        } catch (RotelException e) {
            // Command not supported
            logger.debug("sendCommand: {}", e.getMessage());
            return;
        }
        connector.writeOutput(cmd, message);

        if (connector instanceof RotelSimuConnector simuConnector) {
            if ((protocol == RotelProtocol.HEX && cmd.getHexType() != 0)
                    || (protocol == RotelProtocol.ASCII_V1 && cmd.getAsciiCommandV1() != null)
                    || (protocol == RotelProtocol.ASCII_V2 && cmd.getAsciiCommandV2() != null)) {
                simuConnector.buildFeedbackMessage(cmd, value);
            }
        }
    }
}
