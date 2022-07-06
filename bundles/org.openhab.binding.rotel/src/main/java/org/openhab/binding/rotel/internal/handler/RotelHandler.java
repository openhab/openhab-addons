/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.rotel.internal.RotelException;
import org.openhab.binding.rotel.internal.RotelModel;
import org.openhab.binding.rotel.internal.RotelPlayStatus;
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

    private @Nullable ScheduledFuture<?> reconnectJob;
    private @Nullable ScheduledFuture<?> powerOnJob;
    private @Nullable ScheduledFuture<?> powerOffJob;
    private @Nullable ScheduledFuture<?> powerOnZone2Job;
    private @Nullable ScheduledFuture<?> powerOnZone3Job;
    private @Nullable ScheduledFuture<?> powerOnZone4Job;

    private RotelStateDescriptionOptionProvider stateDescriptionProvider;
    private SerialPortManager serialPortManager;

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
    private @Nullable Boolean power;
    private boolean powerZone2;
    private boolean powerZone3;
    private boolean powerZone4;
    private RotelSource source = RotelSource.CAT0_CD;
    private @Nullable RotelSource recordSource;
    private @Nullable RotelSource sourceZone2;
    private @Nullable RotelSource sourceZone3;
    private @Nullable RotelSource sourceZone4;
    private RotelDsp dsp = RotelDsp.CAT1_NONE;
    private int volume;
    private boolean mute;
    private boolean fixedVolumeZone2;
    private int volumeZone2;
    private boolean muteZone2;
    private boolean fixedVolumeZone3;
    private int volumeZone3;
    private boolean muteZone3;
    private boolean fixedVolumeZone4;
    private int volumeZone4;
    private boolean muteZone4;
    private int bass;
    private int treble;
    private RotelPlayStatus playStatus = RotelPlayStatus.STOPPED;
    private int track;
    private double frequency;
    private String frontPanelLine1 = "";
    private String frontPanelLine2 = "";
    private int brightness;
    private boolean tcbypass;
    private int balance;
    private int minBalanceLevel;
    private int maxBalanceLevel;
    private boolean speakera;
    private boolean speakerb;

    private Object sequenceLock = new Object();

    /**
     * Constructor
     */
    public RotelHandler(Thing thing, RotelStateDescriptionOptionProvider stateDescriptionProvider,
            SerialPortManager serialPortManager) {
        super(thing);
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.serialPortManager = serialPortManager;
        this.model = DEFAULT_MODEL;
        this.protocolHandler = new RotelHexProtocolHandler(model, Map.of());
        this.protocol = protocolHandler.getProtocol();
        this.connector = new RotelSimuConnector(model, protocolHandler, new HashMap<>(), "OH-binding-rotel");
    }

    @Override
    public void initialize() {
        logger.debug("Start initializing handler for thing {}", getThing().getUID());

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
                model = RotelModel.RA1592;
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
                model = RotelModel.RC1590;
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
            if (model.hasZone2SourceControl()) {
                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_ZONE2_SOURCE),
                        getStateOptions(model.getZone2Sources(), sourcesCustomLabels));
            }
            if (model.hasZone3SourceControl()) {
                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_ZONE3_SOURCE),
                        getStateOptions(model.getZone3Sources(), sourcesCustomLabels));
            }
            if (model.hasZone4SourceControl()) {
                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_ZONE4_SOURCE),
                        getStateOptions(model.getZone4Sources(), sourcesCustomLabels));
            }
            if (model.hasDspControl()) {
                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_DSP),
                        model.getDspStateOptions());
                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), CHANNEL_MAIN_DSP),
                        model.getDspStateOptions());
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
        cancelPowerOnJob();
        cancelPowerOnZone2Job();
        cancelPowerOnZone3Job();
        cancelPowerOnZone4Job();
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

        RotelSource src;
        RotelCommand cmd;
        boolean success = true;
        synchronized (sequenceLock) {
            try {
                switch (channel) {
                    case CHANNEL_POWER:
                    case CHANNEL_MAIN_POWER:
                        handlePowerCmd(channel, command, getPowerOnCommand(), getPowerOffCommand());
                        break;
                    case CHANNEL_ZONE2_POWER:
                        if (model.hasZone2Commands()) {
                            handlePowerCmd(channel, command, RotelCommand.ZONE2_POWER_ON, RotelCommand.ZONE2_POWER_OFF);
                        } else if (model.getNbAdditionalZones() == 1) {
                            if (isPowerOn() || powerZone2) {
                                selectZone(2, model.getZoneSelectCmd());
                            }
                            sendCommand(RotelCommand.ZONE_SELECT);
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_ZONE3_POWER:
                        if (model.hasZone3Commands()) {
                            handlePowerCmd(channel, command, RotelCommand.ZONE3_POWER_ON, RotelCommand.ZONE3_POWER_OFF);
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_ZONE4_POWER:
                        if (model.hasZone4Commands()) {
                            handlePowerCmd(channel, command, RotelCommand.ZONE4_POWER_ON, RotelCommand.ZONE4_POWER_OFF);
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_SOURCE:
                    case CHANNEL_MAIN_SOURCE:
                        if (!isPowerOn()) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: device in standby", command, channel);
                        } else {
                            src = model.getSourceFromName(command.toString());
                            cmd = model.hasOtherThanPrimaryCommands() ? src.getMainZoneCommand() : src.getCommand();
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
                    case CHANNEL_ZONE2_SOURCE:
                        if (!powerZone2) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: zone 2 in standby", command, channel);
                        } else if (model.hasZone2Commands()) {
                            src = model.getSourceFromName(command.toString());
                            cmd = src.getZone2Command();
                            if (cmd != null) {
                                sendCommand(cmd);
                            } else {
                                success = false;
                                logger.debug("Command {} from channel {} failed: undefined zone 2 source command",
                                        command, channel);
                            }
                        } else if (model.getNbAdditionalZones() >= 1) {
                            src = model.getSourceFromName(command.toString());
                            cmd = src.getCommand();
                            if (cmd != null) {
                                selectZone(2, model.getZoneSelectCmd());
                                sendCommand(cmd);
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
                    case CHANNEL_ZONE3_SOURCE:
                        if (!powerZone3) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: zone 3 in standby", command, channel);
                        } else if (model.hasZone3Commands()) {
                            src = model.getSourceFromName(command.toString());
                            cmd = src.getZone3Command();
                            if (cmd != null) {
                                sendCommand(cmd);
                            } else {
                                success = false;
                                logger.debug("Command {} from channel {} failed: undefined zone 3 source command",
                                        command, channel);
                            }
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_ZONE4_SOURCE:
                        if (!powerZone4) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: zone 4 in standby", command, channel);
                        } else if (model.hasZone4Commands()) {
                            src = model.getSourceFromName(command.toString());
                            cmd = src.getZone4Command();
                            if (cmd != null) {
                                sendCommand(cmd);
                            } else {
                                success = false;
                                logger.debug("Command {} from channel {} failed: undefined zone 4 source command",
                                        command, channel);
                            }
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
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
                        if (!isPowerOn()) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: device in standby", command, channel);
                        } else if (model.hasVolumeControl()) {
                            handleVolumeCmd(volume, channel, command, getVolumeUpCommand(), getVolumeDownCommand(),
                                    RotelCommand.VOLUME_SET);
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_MAIN_VOLUME_UP_DOWN:
                        if (!isPowerOn()) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: device in standby", command, channel);
                        } else if (model.hasVolumeControl()) {
                            handleVolumeCmd(volume, channel, command, getVolumeUpCommand(), getVolumeDownCommand(),
                                    null);
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_ZONE2_VOLUME:
                        if (!powerZone2) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: zone 2 in standby", command, channel);
                        } else if (fixedVolumeZone2) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: fixed volume in zone 2", command,
                                    channel);
                        } else if (model.hasVolumeControl() && model.getNbAdditionalZones() >= 1) {
                            if (model.hasZone2Commands()) {
                                handleVolumeCmd(volumeZone2, channel, command, RotelCommand.ZONE2_VOLUME_UP,
                                        RotelCommand.ZONE2_VOLUME_DOWN, RotelCommand.ZONE2_VOLUME_SET);
                            } else {
                                selectZone(2, model.getZoneSelectCmd());
                                handleVolumeCmd(volumeZone2, channel, command, RotelCommand.VOLUME_UP,
                                        RotelCommand.VOLUME_DOWN, RotelCommand.VOLUME_SET);
                            }
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_ZONE2_VOLUME_UP_DOWN:
                        if (!powerZone2) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: zone 2 in standby", command, channel);
                        } else if (fixedVolumeZone2) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: fixed volume in zone 2", command,
                                    channel);
                        } else if (model.hasVolumeControl() && model.getNbAdditionalZones() >= 1) {
                            if (model.hasZone2Commands()) {
                                handleVolumeCmd(volumeZone2, channel, command, RotelCommand.ZONE2_VOLUME_UP,
                                        RotelCommand.ZONE2_VOLUME_DOWN, null);
                            } else {
                                selectZone(2, model.getZoneSelectCmd());
                                handleVolumeCmd(volumeZone2, channel, command, RotelCommand.VOLUME_UP,
                                        RotelCommand.VOLUME_DOWN, null);
                            }
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_ZONE3_VOLUME:
                        if (!powerZone3) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: zone 3 in standby", command, channel);
                        } else if (fixedVolumeZone3) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: fixed volume in zone 3", command,
                                    channel);
                        } else if (model.hasVolumeControl() && model.hasZone3Commands()) {
                            handleVolumeCmd(volumeZone3, channel, command, RotelCommand.ZONE3_VOLUME_UP,
                                    RotelCommand.ZONE3_VOLUME_DOWN, RotelCommand.ZONE3_VOLUME_SET);
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_ZONE4_VOLUME:
                        if (!powerZone4) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: zone 4 in standby", command, channel);
                        } else if (fixedVolumeZone4) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: fixed volume in zone 4", command,
                                    channel);
                        } else if (model.hasVolumeControl() && model.hasZone4Commands()) {
                            handleVolumeCmd(volumeZone4, channel, command, RotelCommand.ZONE4_VOLUME_UP,
                                    RotelCommand.ZONE4_VOLUME_DOWN, RotelCommand.ZONE4_VOLUME_SET);
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_MUTE:
                    case CHANNEL_MAIN_MUTE:
                        if (!isPowerOn()) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: device in standby", command, channel);
                        } else if (model.hasVolumeControl()) {
                            handleMuteCmd(protocol == RotelProtocol.HEX, channel, command, getMuteOnCommand(),
                                    getMuteOffCommand(), getMuteToggleCommand());
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_ZONE2_MUTE:
                        if (!powerZone2) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: zone 2 in standby", command, channel);
                        } else if (model.hasVolumeControl() && model.hasZone2Commands()) {
                            handleMuteCmd(false, channel, command, RotelCommand.ZONE2_MUTE_ON,
                                    RotelCommand.ZONE2_MUTE_OFF, RotelCommand.ZONE2_MUTE_TOGGLE);
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_ZONE3_MUTE:
                        if (!powerZone3) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: zone 3 in standby", command, channel);
                        } else if (model.hasVolumeControl() && model.hasZone3Commands()) {
                            handleMuteCmd(false, channel, command, RotelCommand.ZONE3_MUTE_ON,
                                    RotelCommand.ZONE3_MUTE_OFF, RotelCommand.ZONE3_MUTE_TOGGLE);
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_ZONE4_MUTE:
                        if (!powerZone4) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: zone 4 in standby", command, channel);
                        } else if (model.hasVolumeControl() && model.hasZone4Commands()) {
                            handleMuteCmd(false, channel, command, RotelCommand.ZONE4_MUTE_ON,
                                    RotelCommand.ZONE4_MUTE_OFF, RotelCommand.ZONE4_MUTE_TOGGLE);
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        }
                        break;
                    case CHANNEL_BASS:
                    case CHANNEL_MAIN_BASS:
                        if (!isPowerOn()) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: device in standby", command, channel);
                        } else if (tcbypass) {
                            logger.debug("Command {} from channel {} ignored: tone control bypass is ON", command,
                                    channel);
                            updateChannelState(CHANNEL_BASS);
                        } else {
                            handleToneCmd(bass, channel, command, 2, RotelCommand.BASS_UP, RotelCommand.BASS_DOWN,
                                    RotelCommand.BASS_SET);
                        }
                        break;
                    case CHANNEL_TREBLE:
                    case CHANNEL_MAIN_TREBLE:
                        if (!isPowerOn()) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: device in standby", command, channel);
                        } else if (tcbypass) {
                            logger.debug("Command {} from channel {} ignored: tone control bypass is ON", command,
                                    channel);
                            updateChannelState(CHANNEL_TREBLE);
                        } else {
                            handleToneCmd(treble, channel, command, 1, RotelCommand.TREBLE_UP, RotelCommand.TREBLE_DOWN,
                                    RotelCommand.TREBLE_SET);
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
                            sendCommand(RotelCommand.TRACK_FORWARD);
                        } else if (command instanceof NextPreviousType && command == NextPreviousType.PREVIOUS) {
                            sendCommand(RotelCommand.TRACK_BACKWORD);
                        } else {
                            success = false;
                            logger.debug("Command {} from channel {} failed: invalid command value", command, channel);
                        }
                        break;
                    case CHANNEL_BRIGHTNESS:
                        if (!isPowerOn()) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: device in standby", command, channel);
                        } else if (!model.hasDimmerControl()) {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        } else if (command instanceof PercentType) {
                            int dimmer = (int) Math.round(((PercentType) command).doubleValue() / 100.0
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
                        if (!isPowerOn()) {
                            success = false;
                            logger.debug("Command {} from channel {} ignored: device in standby", command, channel);
                        } else if (!model.hasBalanceControl() || protocol == RotelProtocol.HEX) {
                            success = false;
                            logger.debug("Command {} from channel {} failed: unavailable feature", command, channel);
                        } else {
                            handleBalanceCmd(channel, command, RotelCommand.BALANCE_LEFT, RotelCommand.BALANCE_RIGHT,
                                    RotelCommand.BALANCE_SET);
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
        } else if (command instanceof DecimalType && setCmd == null) {
            int value = ((DecimalType) command).intValue();
            if (value >= minVolume && value <= maxVolume) {
                if (value > current) {
                    sendCommand(upCmd);
                } else if (value < current) {
                    sendCommand(downCmd);
                }
            }
        } else if (command instanceof PercentType && setCmd != null) {
            int value = (int) Math.round(((PercentType) command).doubleValue() / 100.0 * (maxVolume - minVolume))
                    + minVolume;
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
        } else if (command instanceof DecimalType) {
            int value = ((DecimalType) command).intValue();
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
                bass = 0;
                treble = 0;
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
        } else if (command instanceof DecimalType) {
            int value = ((DecimalType) command).intValue();
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
        if (protocol == RotelProtocol.HEX && model.getNbAdditionalZones() >= 1 && zone >= 1 && zone != currentZone
                && selectCommand != null) {
            int nbSelect;
            if (zone < currentZone) {
                nbSelect = zone + model.getNbAdditionalZones() - currentZone;
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
                    } else if (POWER_OFF_DELAYED.equalsIgnoreCase(value)) {
                        schedulePowerOffJob(false);
                    } else {
                        throw new RotelException("Invalid value");
                    }
                    break;
                case KEY_POWER_ZONE2:
                    if (POWER_ON.equalsIgnoreCase(value)) {
                        handlePowerOnZone2();
                    } else if (STANDBY.equalsIgnoreCase(value)) {
                        handlePowerOffZone2();
                    } else {
                        throw new RotelException("Invalid value");
                    }
                    break;
                case KEY_POWER_ZONE3:
                    if (POWER_ON.equalsIgnoreCase(value)) {
                        handlePowerOnZone3();
                    } else if (STANDBY.equalsIgnoreCase(value)) {
                        handlePowerOffZone3();
                    } else {
                        throw new RotelException("Invalid value");
                    }
                    break;
                case KEY_POWER_ZONE4:
                    if (POWER_ON.equalsIgnoreCase(value)) {
                        handlePowerOnZone4();
                    } else if (STANDBY.equalsIgnoreCase(value)) {
                        handlePowerOffZone4();
                    } else {
                        throw new RotelException("Invalid value");
                    }
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
                    if (MSG_VALUE_MIN.equalsIgnoreCase(value)) {
                        volume = minVolume;
                    } else if (MSG_VALUE_MAX.equalsIgnoreCase(value)) {
                        volume = maxVolume;
                    } else {
                        volume = Integer.parseInt(value);
                    }
                    updateChannelState(CHANNEL_VOLUME);
                    updateChannelState(CHANNEL_MAIN_VOLUME);
                    updateChannelState(CHANNEL_MAIN_VOLUME_UP_DOWN);
                    break;
                case KEY_MUTE:
                    if (MSG_VALUE_ON.equalsIgnoreCase(value)) {
                        mute = true;
                        updateChannelState(CHANNEL_MUTE);
                        updateChannelState(CHANNEL_MAIN_MUTE);
                    } else if (MSG_VALUE_OFF.equalsIgnoreCase(value)) {
                        mute = false;
                        updateChannelState(CHANNEL_MUTE);
                        updateChannelState(CHANNEL_MAIN_MUTE);
                    } else {
                        throw new RotelException("Invalid value");
                    }
                    break;
                case KEY_VOLUME_ZONE2:
                    fixedVolumeZone2 = false;
                    if (MSG_VALUE_FIX.equalsIgnoreCase(value)) {
                        fixedVolumeZone2 = true;
                    } else if (MSG_VALUE_MIN.equalsIgnoreCase(value)) {
                        volumeZone2 = minVolume;
                    } else if (MSG_VALUE_MAX.equalsIgnoreCase(value)) {
                        volumeZone2 = maxVolume;
                    } else {
                        volumeZone2 = Integer.parseInt(value);
                    }
                    updateChannelState(CHANNEL_ZONE2_VOLUME);
                    updateChannelState(CHANNEL_ZONE2_VOLUME_UP_DOWN);
                    break;
                case KEY_VOLUME_ZONE3:
                    fixedVolumeZone3 = false;
                    if (MSG_VALUE_FIX.equalsIgnoreCase(value)) {
                        fixedVolumeZone3 = true;
                    } else if (MSG_VALUE_MIN.equalsIgnoreCase(value)) {
                        volumeZone3 = minVolume;
                    } else if (MSG_VALUE_MAX.equalsIgnoreCase(value)) {
                        volumeZone3 = maxVolume;
                    } else {
                        volumeZone3 = Integer.parseInt(value);
                    }
                    updateChannelState(CHANNEL_ZONE3_VOLUME);
                    break;
                case KEY_VOLUME_ZONE4:
                    fixedVolumeZone4 = false;
                    if (MSG_VALUE_FIX.equalsIgnoreCase(value)) {
                        fixedVolumeZone4 = true;
                    } else if (MSG_VALUE_MIN.equalsIgnoreCase(value)) {
                        volumeZone4 = minVolume;
                    } else if (MSG_VALUE_MAX.equalsIgnoreCase(value)) {
                        volumeZone4 = maxVolume;
                    } else {
                        volumeZone4 = Integer.parseInt(value);
                    }
                    updateChannelState(CHANNEL_ZONE4_VOLUME);
                    break;
                case KEY_MUTE_ZONE2:
                    if (MSG_VALUE_ON.equalsIgnoreCase(value)) {
                        muteZone2 = true;
                        updateChannelState(CHANNEL_ZONE2_MUTE);
                    } else if (MSG_VALUE_OFF.equalsIgnoreCase(value)) {
                        muteZone2 = false;
                        updateChannelState(CHANNEL_ZONE2_MUTE);
                    } else {
                        throw new RotelException("Invalid value");
                    }
                    break;
                case KEY_MUTE_ZONE3:
                    if (MSG_VALUE_ON.equalsIgnoreCase(value)) {
                        muteZone3 = true;
                        updateChannelState(CHANNEL_ZONE3_MUTE);
                    } else if (MSG_VALUE_OFF.equalsIgnoreCase(value)) {
                        muteZone3 = false;
                        updateChannelState(CHANNEL_ZONE3_MUTE);
                    } else {
                        throw new RotelException("Invalid value");
                    }
                    break;
                case KEY_MUTE_ZONE4:
                    if (MSG_VALUE_ON.equalsIgnoreCase(value)) {
                        muteZone4 = true;
                        updateChannelState(CHANNEL_ZONE4_MUTE);
                    } else if (MSG_VALUE_OFF.equalsIgnoreCase(value)) {
                        muteZone4 = false;
                        updateChannelState(CHANNEL_ZONE4_MUTE);
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
                    if (MSG_VALUE_MIN.equalsIgnoreCase(value)) {
                        bass = minToneLevel;
                    } else if (MSG_VALUE_MAX.equalsIgnoreCase(value)) {
                        bass = maxToneLevel;
                    } else {
                        bass = Integer.parseInt(value);
                    }
                    updateChannelState(CHANNEL_BASS);
                    updateChannelState(CHANNEL_MAIN_BASS);
                    break;
                case KEY_TREBLE:
                    if (MSG_VALUE_MIN.equalsIgnoreCase(value)) {
                        treble = minToneLevel;
                    } else if (MSG_VALUE_MAX.equalsIgnoreCase(value)) {
                        treble = maxToneLevel;
                    } else {
                        treble = Integer.parseInt(value);
                    }
                    updateChannelState(CHANNEL_TREBLE);
                    updateChannelState(CHANNEL_MAIN_TREBLE);
                    break;
                case KEY_SOURCE:
                    source = model.getSourceFromCommand(RotelCommand.getFromAsciiCommand(value));
                    updateChannelState(CHANNEL_SOURCE);
                    updateChannelState(CHANNEL_MAIN_SOURCE);
                    break;
                case KEY_RECORD:
                    recordSource = model.getRecordSourceFromCommand(RotelCommand.getFromAsciiCommand(value));
                    updateChannelState(CHANNEL_MAIN_RECORD_SOURCE);
                    break;
                case KEY_SOURCE_ZONE2:
                    sourceZone2 = model.getZone2SourceFromCommand(RotelCommand.getFromAsciiCommand(value));
                    updateChannelState(CHANNEL_ZONE2_SOURCE);
                    break;
                case KEY_SOURCE_ZONE3:
                    sourceZone3 = model.getZone3SourceFromCommand(RotelCommand.getFromAsciiCommand(value));
                    updateChannelState(CHANNEL_ZONE3_SOURCE);
                    break;
                case KEY_SOURCE_ZONE4:
                    sourceZone4 = model.getZone4SourceFromCommand(RotelCommand.getFromAsciiCommand(value));
                    updateChannelState(CHANNEL_ZONE4_SOURCE);
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
                    if (source.getName().equals("CD") && !model.hasSourceControl()) {
                        track = Integer.parseInt(value);
                        updateChannelState(CHANNEL_TRACK);
                    }
                    break;
                case KEY_FREQ:
                    if (MSG_VALUE_OFF.equalsIgnoreCase(value)) {
                        frequency = 0.0;
                    } else {
                        // Suppress a potential ending "k" or "K"
                        if (value.toUpperCase().endsWith("K")) {
                            value = value.substring(0, value.length() - 1);
                        }
                        frequency = Double.parseDouble(value);
                    }
                    updateChannelState(CHANNEL_FREQUENCY);
                    break;
                case KEY_DIMMER:
                    brightness = Integer.parseInt(value);
                    updateChannelState(CHANNEL_BRIGHTNESS);
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
                    if (MSG_VALUE_MIN.equalsIgnoreCase(value)) {
                        balance = minBalanceLevel;
                    } else if (MSG_VALUE_MAX.equalsIgnoreCase(value)) {
                        balance = maxBalanceLevel;
                    } else if (value.toUpperCase().startsWith("L")) {
                        balance = -Integer.parseInt(value.substring(1));
                    } else if (value.toLowerCase().startsWith("R")) {
                        balance = Integer.parseInt(value.substring(1));
                    } else {
                        balance = Integer.parseInt(value);
                    }
                    updateChannelState(CHANNEL_BALANCE);
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
        Boolean prev = power;
        power = true;
        updateChannelState(CHANNEL_POWER);
        updateChannelState(CHANNEL_MAIN_POWER);
        if ((prev == null) || !prev) {
            schedulePowerOnJob();
        }
    }

    /**
     * Handle the received information that device power (main zone) is OFF
     */
    private void handlePowerOff() {
        cancelPowerOnJob();
        power = false;
        updateChannelState(CHANNEL_POWER);
        updateChannelState(CHANNEL_MAIN_POWER);
        updateChannelState(CHANNEL_SOURCE);
        updateChannelState(CHANNEL_MAIN_SOURCE);
        updateChannelState(CHANNEL_MAIN_RECORD_SOURCE);
        updateChannelState(CHANNEL_DSP);
        updateChannelState(CHANNEL_MAIN_DSP);
        updateChannelState(CHANNEL_VOLUME);
        updateChannelState(CHANNEL_MAIN_VOLUME);
        updateChannelState(CHANNEL_MAIN_VOLUME_UP_DOWN);
        updateChannelState(CHANNEL_MUTE);
        updateChannelState(CHANNEL_MAIN_MUTE);
        updateChannelState(CHANNEL_BASS);
        updateChannelState(CHANNEL_MAIN_BASS);
        updateChannelState(CHANNEL_TREBLE);
        updateChannelState(CHANNEL_MAIN_TREBLE);
        updateChannelState(CHANNEL_PLAY_CONTROL);
        updateChannelState(CHANNEL_TRACK);
        updateChannelState(CHANNEL_FREQUENCY);
        updateChannelState(CHANNEL_BRIGHTNESS);
        updateChannelState(CHANNEL_TCBYPASS);
        updateChannelState(CHANNEL_BALANCE);
        updateChannelState(CHANNEL_SPEAKER_A);
        updateChannelState(CHANNEL_SPEAKER_B);
    }

    /**
     * Handle the received information that zone 2 power is ON
     */
    private void handlePowerOnZone2() {
        boolean prev = powerZone2;
        powerZone2 = true;
        updateChannelState(CHANNEL_ZONE2_POWER);
        if (!prev) {
            schedulePowerOnZone2Job();
        }
    }

    /**
     * Handle the received information that zone 2 power is OFF
     */
    private void handlePowerOffZone2() {
        cancelPowerOnZone2Job();
        powerZone2 = false;
        updateChannelState(CHANNEL_ZONE2_POWER);
        updateChannelState(CHANNEL_ZONE2_SOURCE);
        updateChannelState(CHANNEL_ZONE2_VOLUME);
        updateChannelState(CHANNEL_ZONE2_VOLUME_UP_DOWN);
        updateChannelState(CHANNEL_ZONE2_MUTE);
    }

    /**
     * Handle the received information that zone 3 power is ON
     */
    private void handlePowerOnZone3() {
        boolean prev = powerZone3;
        powerZone3 = true;
        updateChannelState(CHANNEL_ZONE3_POWER);
        if (!prev) {
            schedulePowerOnZone3Job();
        }
    }

    /**
     * Handle the received information that zone 3 power is OFF
     */
    private void handlePowerOffZone3() {
        cancelPowerOnZone3Job();
        powerZone3 = false;
        updateChannelState(CHANNEL_ZONE3_POWER);
        updateChannelState(CHANNEL_ZONE3_SOURCE);
        updateChannelState(CHANNEL_ZONE3_VOLUME);
        updateChannelState(CHANNEL_ZONE3_MUTE);
    }

    /**
     * Handle the received information that zone 4 power is ON
     */
    private void handlePowerOnZone4() {
        boolean prev = powerZone4;
        powerZone4 = true;
        updateChannelState(CHANNEL_ZONE4_POWER);
        if (!prev) {
            schedulePowerOnZone4Job();
        }
    }

    /**
     * Handle the received information that zone 4 power is OFF
     */
    private void handlePowerOffZone4() {
        cancelPowerOnZone4Job();
        powerZone4 = false;
        updateChannelState(CHANNEL_ZONE4_POWER);
        updateChannelState(CHANNEL_ZONE4_SOURCE);
        updateChannelState(CHANNEL_ZONE4_VOLUME);
        updateChannelState(CHANNEL_ZONE4_MUTE);
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
                handlePowerOffZone2();
                handlePowerOffZone3();
                handlePowerOffZone4();
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
        cancelPowerOnJob();
        powerOnJob = scheduler.schedule(() -> {
            synchronized (sequenceLock) {
                logger.debug("Power ON job");
                try {
                    switch (protocol) {
                        case HEX:
                            if (model.getRespNbChars() <= 13 && model.hasVolumeControl()) {
                                sendCommand(getVolumeDownCommand());
                                Thread.sleep(100);
                                sendCommand(getVolumeUpCommand());
                                Thread.sleep(100);
                            }
                            if (model.getNbAdditionalZones() >= 1) {
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
                                    sendCommand(RotelCommand.TONE_CONTROLS);
                                    Thread.sleep(SLEEP_INTV);
                                }
                            }
                            if (model.hasBalanceControl()) {
                                sendCommand(RotelCommand.BALANCE);
                                Thread.sleep(SLEEP_INTV);
                            }
                            if (model.hasPlayControl()) {
                                if (model != RotelModel.RCD1570 && model != RotelModel.RCD1572
                                        && (model != RotelModel.RCX1500 || !source.getName().equals("CD"))) {
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
                            break;
                        case ASCII_V2:
                            sendCommand(RotelCommand.UPDATE_AUTO);
                            Thread.sleep(SLEEP_INTV);
                            if (model.hasSourceControl()) {
                                sendCommand(RotelCommand.SOURCE);
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
                                sendCommand(RotelCommand.TCBYPASS);
                                Thread.sleep(SLEEP_INTV);
                            }
                            if (model.hasBalanceControl()) {
                                sendCommand(RotelCommand.BALANCE);
                                Thread.sleep(SLEEP_INTV);
                            }
                            if (model.hasPlayControl()) {
                                sendCommand(RotelCommand.PLAY_STATUS);
                                Thread.sleep(SLEEP_INTV);
                                if (source.getName().equals("CD") && !model.hasSourceControl()) {
                                    sendCommand(RotelCommand.TRACK);
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
     * Cancel the job scheduled when the device power (main zone) switched ON
     */
    private void cancelPowerOnJob() {
        ScheduledFuture<?> powerOnJob = this.powerOnJob;
        if (powerOnJob != null && !powerOnJob.isCancelled()) {
            powerOnJob.cancel(true);
            this.powerOnJob = null;
        }
    }

    /**
     * Schedule the job to run with a few seconds delay when the zone 2 power switched ON
     */
    private void schedulePowerOnZone2Job() {
        logger.debug("Schedule power ON zone 2 job");
        cancelPowerOnZone2Job();
        powerOnZone2Job = scheduler.schedule(() -> {
            synchronized (sequenceLock) {
                logger.debug("Power ON zone 2 job");
                try {
                    if (protocol == RotelProtocol.HEX && model.getNbAdditionalZones() >= 1) {
                        selectZone(2, model.getZoneSelectCmd());
                        sendCommand(
                                model.hasZone2Commands() ? RotelCommand.ZONE2_VOLUME_DOWN : RotelCommand.VOLUME_DOWN);
                        Thread.sleep(100);
                        sendCommand(model.hasZone2Commands() ? RotelCommand.ZONE2_VOLUME_UP : RotelCommand.VOLUME_UP);
                        Thread.sleep(100);
                    }
                } catch (RotelException e) {
                    logger.debug("Init sequence zone 2 failed: {}", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.comm-error-init-sequence-zone [\"2\"]");
                    closeConnection();
                } catch (InterruptedException e) {
                    logger.debug("Init sequence zone 2 interrupted: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }, 2500, TimeUnit.MILLISECONDS);
    }

    /**
     * Cancel the job scheduled when the zone 2 power switched ON
     */
    private void cancelPowerOnZone2Job() {
        ScheduledFuture<?> powerOnZone2Job = this.powerOnZone2Job;
        if (powerOnZone2Job != null && !powerOnZone2Job.isCancelled()) {
            powerOnZone2Job.cancel(true);
            this.powerOnZone2Job = null;
        }
    }

    /**
     * Schedule the job to run with a few seconds delay when the zone 3 power switched ON
     */
    private void schedulePowerOnZone3Job() {
        logger.debug("Schedule power ON zone 3 job");
        cancelPowerOnZone3Job();
        powerOnZone3Job = scheduler.schedule(() -> {
            synchronized (sequenceLock) {
                logger.debug("Power ON zone 3 job");
                try {
                    if (protocol == RotelProtocol.HEX && model.getNbAdditionalZones() >= 2) {
                        selectZone(3, model.getZoneSelectCmd());
                        sendCommand(
                                model.hasZone3Commands() ? RotelCommand.ZONE3_VOLUME_DOWN : RotelCommand.VOLUME_DOWN);
                        Thread.sleep(100);
                        sendCommand(model.hasZone3Commands() ? RotelCommand.ZONE3_VOLUME_UP : RotelCommand.VOLUME_UP);
                        Thread.sleep(100);
                    }
                } catch (RotelException e) {
                    logger.debug("Init sequence zone 3 failed: {}", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.comm-error-init-sequence-zone [\"3\"]");
                    closeConnection();
                } catch (InterruptedException e) {
                    logger.debug("Init sequence zone 3 interrupted: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }, 2500, TimeUnit.MILLISECONDS);
    }

    /**
     * Cancel the job scheduled when the zone 3 power switched ON
     */
    private void cancelPowerOnZone3Job() {
        ScheduledFuture<?> powerOnZone3Job = this.powerOnZone3Job;
        if (powerOnZone3Job != null && !powerOnZone3Job.isCancelled()) {
            powerOnZone3Job.cancel(true);
            this.powerOnZone3Job = null;
        }
    }

    /**
     * Schedule the job to run with a few seconds delay when the zone 4 power switched ON
     */
    private void schedulePowerOnZone4Job() {
        logger.debug("Schedule power ON zone 4 job");
        cancelPowerOnZone4Job();
        powerOnZone4Job = scheduler.schedule(() -> {
            synchronized (sequenceLock) {
                logger.debug("Power ON zone 4 job");
                try {
                    if (protocol == RotelProtocol.HEX && model.getNbAdditionalZones() >= 3) {
                        selectZone(4, model.getZoneSelectCmd());
                        sendCommand(
                                model.hasZone4Commands() ? RotelCommand.ZONE4_VOLUME_DOWN : RotelCommand.VOLUME_DOWN);
                        Thread.sleep(100);
                        sendCommand(model.hasZone4Commands() ? RotelCommand.ZONE4_VOLUME_UP : RotelCommand.VOLUME_UP);
                        Thread.sleep(100);
                    }
                } catch (RotelException e) {
                    logger.debug("Init sequence zone 4 failed: {}", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.comm-error-init-sequence-zone [\"4\"]");
                    closeConnection();
                } catch (InterruptedException e) {
                    logger.debug("Init sequence zone 4 interrupted: {}", e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }, 2500, TimeUnit.MILLISECONDS);
    }

    /**
     * Cancel the job scheduled when the zone 4 power switched ON
     */
    private void cancelPowerOnZone4Job() {
        ScheduledFuture<?> powerOnZone4Job = this.powerOnZone4Job;
        if (powerOnZone4Job != null && !powerOnZone4Job.isCancelled()) {
            powerOnZone4Job.cancel(true);
            this.powerOnZone4Job = null;
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
                power = null;
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
                    handlePowerOffZone2();
                    handlePowerOffZone3();
                    handlePowerOffZone4();
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
        switch (channel) {
            case CHANNEL_POWER:
            case CHANNEL_MAIN_POWER:
                Boolean po = power;
                if (po != null) {
                    state = OnOffType.from(po.booleanValue());
                }
                break;
            case CHANNEL_ZONE2_POWER:
                state = OnOffType.from(powerZone2);
                break;
            case CHANNEL_ZONE3_POWER:
                state = OnOffType.from(powerZone3);
                break;
            case CHANNEL_ZONE4_POWER:
                state = OnOffType.from(powerZone4);
                break;
            case CHANNEL_SOURCE:
            case CHANNEL_MAIN_SOURCE:
                if (isPowerOn()) {
                    state = new StringType(source.getName());
                }
                break;
            case CHANNEL_MAIN_RECORD_SOURCE:
                RotelSource recordSource = this.recordSource;
                if (isPowerOn() && recordSource != null) {
                    state = new StringType(recordSource.getName());
                }
                break;
            case CHANNEL_ZONE2_SOURCE:
                RotelSource sourceZone2 = this.sourceZone2;
                if (powerZone2 && sourceZone2 != null) {
                    state = new StringType(sourceZone2.getName());
                }
                break;
            case CHANNEL_ZONE3_SOURCE:
                RotelSource sourceZone3 = this.sourceZone3;
                if (powerZone3 && sourceZone3 != null) {
                    state = new StringType(sourceZone3.getName());
                }
                break;
            case CHANNEL_ZONE4_SOURCE:
                RotelSource sourceZone4 = this.sourceZone4;
                if (powerZone4 && sourceZone4 != null) {
                    state = new StringType(sourceZone4.getName());
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
                if (isPowerOn()) {
                    long volumePct = Math
                            .round((double) (volume - minVolume) / (double) (maxVolume - minVolume) * 100.0);
                    state = new PercentType(BigDecimal.valueOf(volumePct));
                }
                break;
            case CHANNEL_MAIN_VOLUME_UP_DOWN:
                if (isPowerOn()) {
                    state = new DecimalType(volume);
                }
                break;
            case CHANNEL_ZONE2_VOLUME:
                if (powerZone2 && !fixedVolumeZone2) {
                    long volumePct = Math
                            .round((double) (volumeZone2 - minVolume) / (double) (maxVolume - minVolume) * 100.0);
                    state = new PercentType(BigDecimal.valueOf(volumePct));
                }
                break;
            case CHANNEL_ZONE2_VOLUME_UP_DOWN:
                if (powerZone2 && !fixedVolumeZone2) {
                    state = new DecimalType(volumeZone2);
                }
                break;
            case CHANNEL_ZONE3_VOLUME:
                if (powerZone3 && !fixedVolumeZone3) {
                    long volumePct = Math
                            .round((double) (volumeZone3 - minVolume) / (double) (maxVolume - minVolume) * 100.0);
                    state = new PercentType(BigDecimal.valueOf(volumePct));
                }
                break;
            case CHANNEL_ZONE4_VOLUME:
                if (powerZone4 && !fixedVolumeZone4) {
                    long volumePct = Math
                            .round((double) (volumeZone4 - minVolume) / (double) (maxVolume - minVolume) * 100.0);
                    state = new PercentType(BigDecimal.valueOf(volumePct));
                }
                break;
            case CHANNEL_MUTE:
            case CHANNEL_MAIN_MUTE:
                if (isPowerOn()) {
                    state = OnOffType.from(mute);
                }
                break;
            case CHANNEL_ZONE2_MUTE:
                if (powerZone2) {
                    state = OnOffType.from(muteZone2);
                }
                break;
            case CHANNEL_ZONE3_MUTE:
                if (powerZone3) {
                    state = OnOffType.from(muteZone3);
                }
                break;
            case CHANNEL_ZONE4_MUTE:
                if (powerZone4) {
                    state = OnOffType.from(muteZone4);
                }
                break;
            case CHANNEL_BASS:
            case CHANNEL_MAIN_BASS:
                if (isPowerOn()) {
                    state = new DecimalType(bass);
                }
                break;
            case CHANNEL_TREBLE:
            case CHANNEL_MAIN_TREBLE:
                if (isPowerOn()) {
                    state = new DecimalType(treble);
                }
                break;
            case CHANNEL_TRACK:
                if (track > 0 && isPowerOn()) {
                    state = new DecimalType(track);
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
            case CHANNEL_FREQUENCY:
                if (frequency > 0.0 && isPowerOn()) {
                    state = new DecimalType(frequency);
                }
                break;
            case CHANNEL_LINE1:
                state = new StringType(frontPanelLine1);
                break;
            case CHANNEL_LINE2:
                state = new StringType(frontPanelLine2);
                break;
            case CHANNEL_BRIGHTNESS:
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
                if (isPowerOn()) {
                    state = new DecimalType(balance);
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
     * Inform about the main zone power state
     *
     * @return true if main zone power state is known and known as ON
     */
    private boolean isPowerOn() {
        Boolean power = this.power;
        return power != null && power.booleanValue();
    }

    /**
     * Get the command to be used for main zone POWER ON
     *
     * @return the command
     */
    private RotelCommand getPowerOnCommand() {
        return model.hasOtherThanPrimaryCommands() ? RotelCommand.MAIN_ZONE_POWER_ON : RotelCommand.POWER_ON;
    }

    /**
     * Get the command to be used for main zone POWER OFF
     *
     * @return the command
     */
    private RotelCommand getPowerOffCommand() {
        return model.hasOtherThanPrimaryCommands() ? RotelCommand.MAIN_ZONE_POWER_OFF : RotelCommand.POWER_OFF;
    }

    /**
     * Get the command to be used for main zone VOLUME UP
     *
     * @return the command
     */
    private RotelCommand getVolumeUpCommand() {
        return model.hasOtherThanPrimaryCommands() ? RotelCommand.MAIN_ZONE_VOLUME_UP : RotelCommand.VOLUME_UP;
    }

    /**
     * Get the command to be used for main zone VOLUME DOWN
     *
     * @return the command
     */
    private RotelCommand getVolumeDownCommand() {
        return model.hasOtherThanPrimaryCommands() ? RotelCommand.MAIN_ZONE_VOLUME_DOWN : RotelCommand.VOLUME_DOWN;
    }

    /**
     * Get the command to be used for main zone MUTE ON
     *
     * @return the command
     */
    private RotelCommand getMuteOnCommand() {
        return model.hasOtherThanPrimaryCommands() ? RotelCommand.MAIN_ZONE_MUTE_ON : RotelCommand.MUTE_ON;
    }

    /**
     * Get the command to be used for main zone MUTE OFF
     *
     * @return the command
     */
    private RotelCommand getMuteOffCommand() {
        return model.hasOtherThanPrimaryCommands() ? RotelCommand.MAIN_ZONE_MUTE_OFF : RotelCommand.MUTE_OFF;
    }

    /**
     * Get the command to be used for main zone MUTE TOGGLE
     *
     * @return the command
     */
    private RotelCommand getMuteToggleCommand() {
        return model.hasOtherThanPrimaryCommands() ? RotelCommand.MAIN_ZONE_MUTE_TOGGLE : RotelCommand.MUTE_TOGGLE;
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
        connector.writeOutput(cmd.getName(), message);

        if (connector instanceof RotelSimuConnector) {
            if ((protocol == RotelProtocol.HEX && cmd.getHexType() != 0)
                    || (protocol == RotelProtocol.ASCII_V1 && cmd.getAsciiCommandV1() != null)
                    || (protocol == RotelProtocol.ASCII_V2 && cmd.getAsciiCommandV2() != null)) {
                ((RotelSimuConnector) connector).buildFeedbackMessage(cmd, value);
            }
        }
    }
}
