/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bambulab.internal;

import static java.util.Objects.requireNonNull;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.Channel.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.DECIBEL_MILLIWATTS;
import static org.openhab.core.thing.ThingStatus.OFFLINE;
import static org.openhab.core.thing.ThingStatus.ONLINE;
import static org.openhab.core.thing.ThingStatus.UNKNOWN;
import static org.openhab.core.thing.ThingStatusDetail.*;
import static org.openhab.core.types.UnDefType.UNDEF;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.LedControlCommand.*;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.LedControlCommand.LedNode.*;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.PushingCommand.defaultPushingCommand;
import static pl.grzeslowski.jbambuapi.mqtt.PrinterClientConfig.requiredFields;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.grzeslowski.jbambuapi.mqtt.PrinterClient;
import pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.GCodeFileCommand;
import pl.grzeslowski.jbambuapi.mqtt.PrinterClient.Channel.PrintSpeedCommand;
import pl.grzeslowski.jbambuapi.mqtt.PrinterWatcher;
import pl.grzeslowski.jbambuapi.mqtt.Report;

/**
 * The {@link PrinterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class PrinterHandler extends BaseThingHandler implements PrinterWatcher.StateSubscriber, BambuHandler {
    private static final Pattern DBM_PATTERN = Pattern.compile("^(-?\\d+)dBm$");
    private Logger logger = LoggerFactory.getLogger(PrinterHandler.class);

    private @Nullable PrinterClient client;
    private @Nullable Camera camera;

    public PrinterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (CHANNEL_LED_CHAMBER_LIGHT.is(channelUID) || CHANNEL_LED_WORK_LIGHT.is(channelUID)) {
            var ledNode = CHANNEL_LED_CHAMBER_LIGHT.getName().equals(channelUID.getId()) ? CHAMBER_LIGHT : WORK_LIGHT;
            var bambuCommand = "ON".equals(command.toFullString()) ? on(ledNode) : off(ledNode);
            sendCommand(bambuCommand);
        } else if (CHANNEL_GCODE_FILE.is(channelUID)) {
            var bambuCommand = new GCodeFileCommand(command.toString());
            sendCommand(bambuCommand);
        } else if (CHANNEL_SPEED_LEVEL.is(channelUID)) {
            var bambuCommand = PrintSpeedCommand.valueOf(command.toString());
            sendCommand(bambuCommand);
        } else if (CHANNEL_CAMERA_RECORD.is(channelUID) && command instanceof OnOffType onOffCommand) {
            requireNonNull(camera).handleCommand(onOffCommand);
        }
    }

    @Override
    public void initialize() {
        try {
            internalInitialize();
        } catch (InitializationException e) {
            updateStatus(OFFLINE, e.getThingStatusDetail(), e.getDescription());
        }
    }

    private void internalInitialize() throws InitializationException {
        PrinterConfiguration config = getConfigAs(PrinterConfiguration.class);

        config.validateSerial();
        logger = LoggerFactory.getLogger(PrinterHandler.class.getName() + "." + config.serial);
        config.validateHostname();
        config.validateAccessCode();
        config.validateUsername();
        var uri = config.buildUri();

        // always turn off camera recording when starting thing
        camera = new Camera(config, this);
        updateState(CHANNEL_CAMERA_RECORD.getName(), OnOffType.OFF);
        updateState(CHANNEL_CAMERA_IMAGE.getName(), UNDEF);

        var localClient = client = buildLocalClient(uri, config);

        // the status will be unknown until the first message form MQTT arrives
        updateStatus(UNKNOWN);
        try {
            scheduler.execute(() -> initMqtt(localClient));
        } catch (RejectedExecutionException ex) {
            logger.debug("Task was rejected", ex);
            throw new InitializationException(CONFIGURATION_ERROR, ex);
        }
    }

    private void initMqtt(PrinterClient client) {
        try {
            logger.debug("Trying to connect to the printer broker");
            client.connect();
            var printerWatcher = new PrinterWatcher();
            client.subscribe(printerWatcher);
            printerWatcher.subscribe(this);
            // send request to update all channels
            refreshChannels();
            updateStatus(ONLINE);
        } catch (Exception e) {
            logger.debug("Cannot connect to MQTT client", e);
            updateStatus(OFFLINE, COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
    }

    private PrinterClient buildLocalClient(URI uri, PrinterConfiguration config) throws InitializationException {
        try {
            return new PrinterClient(
                    requiredFields(uri, config.username, config.serial, config.accessCode.toCharArray()));
        } catch (Exception e) {
            logger.debug("Cannot create MQTT client", e);
            throw new InitializationException(COMMUNICATION_ERROR, e);
        }
    }

    void refreshChannels() {
        sendCommand(defaultPushingCommand());
    }

    @Override
    public void dispose() {
        try {
            var localClient = client;
            client = null;
            if (localClient != null) {
                try {
                    localClient.close();
                } catch (Exception e) {
                    logger.warn("Could not correctly dispose PrinterClient", e);
                }
            }
            var localCamera = camera;
            camera = null;
            if (localCamera != null) {
                localCamera.close();
            }
        } finally {
            logger = LoggerFactory.getLogger(PrinterHandler.class);
        }
    }

    @Override
    public void newState(@Nullable Report delta, @Nullable Report fullState) {
        logger.trace("New Printer state from delta {}", delta);
        // only need to update channels from delta
        // do not need to use full state, because at some point in past channels was already updated with its values
        if (delta == null) {
            return;
        }
        updatePrinterChannels(delta);
        // if got new Printer state (and not failed) then make sure that thing status in ONLINE
        updateStatus(ONLINE);
    }

    private void updatePrinterChannels(Report state) {
        // Print
        var print = state.print();
        if (print == null) {
            return;
        }
        // tempers
        updateCelsiusState(CHANNEL_NOZZLE_TEMPERATURE.getName(), print.nozzleTemper());
        updateCelsiusState(CHANNEL_NOZZLE_TARGET_TEMPERATURE.getName(), print.nozzleTargetTemper());
        updateCelsiusState(CHANNEL_BED_TEMPERATURE.getName(), print.bedTemper());
        updateCelsiusState(CHANNEL_BED_TARGET_TEMPERATURE.getName(), print.bedTargetTemper());
        updateCelsiusState(CHANNEL_CHAMBER_TEMPERATURE.getName(), print.chamberTemper());
        // string
        updateStringState(CHANNEL_MC_PRINT_STAGE.getName(), print.mcPrintStage());
        updateStringState(CHANNEL_BED_TYPE.getName(), print.bedType());
        updateStringState(CHANNEL_GCODE_FILE.getName(), print.gcodeFile());
        updateStringState(CHANNEL_GCODE_STATE.getName(), print.gcodeState());
        updateStringState(CHANNEL_REASON.getName(), print.reason());
        updateStringState(CHANNEL_RESULT.getName(), print.result());
        // percent
        updatePercentState(CHANNEL_MC_PERCENT.getName(), print.mcPercent());
        updatePercentState(CHANNEL_GCODE_FILE_PREPARE_PERCENT.getName(), print.gcodeFilePreparePercent());
        // decimal
        updateDecimalState(CHANNEL_MC_REMAINING_TIME.getName(), print.mcRemainingTime());
        updateDecimalState(CHANNEL_BIG_FAN_1_SPEED.getName(), print.bigFan1Speed());
        updateDecimalState(CHANNEL_BIG_FAN_2_SPEED.getName(), print.bigFan2Speed());
        updateDecimalState(CHANNEL_HEAT_BREAK_FAN_SPEED.getName(), print.heatbreakFanSpeed());
        updateDecimalState(CHANNEL_LAYER_NUM.getName(), print.layerNum());
        if (print.spdLvl() != null) {
            var speedLevel = PrintSpeedCommand.findByLevel(print.spdLvl());
            updateState(CHANNEL_SPEED_LEVEL.getName(), new StringType(speedLevel.toString()));
        }
        // boolean
        updateBooleanState(CHANNEL_TIME_LAPS.getName(), print.timelapse());
        updateBooleanState(CHANNEL_USE_AMS.getName(), print.useAms());
        updateBooleanState(CHANNEL_VIBRATION_CALIBRATION.getName(), print.vibrationCali());
        // lights
        updateLightState("chamber_light", CHANNEL_LED_CHAMBER_LIGHT, print.lightsReport());
        updateLightState("work_light", CHANNEL_LED_WORK_LIGHT, print.lightsReport());
        // other
        if (print.wifiSignal() != null) {
            updateState(CHANNEL_WIFI_SIGNAL.getName(), parseWifiChannel(print.wifiSignal()));
        }
    }

    private void updateCelsiusState(String channelId, @Nullable Double temperature) {
        if (temperature == null) {
            return;
        }
        updateState(channelId, new QuantityType<>(temperature, CELSIUS));
    }

    private void updateStringState(String channelId, @Nullable String string) {
        if (string == null) {
            return;
        }
        updateState(channelId, new StringType(string));
    }

    private void updateDecimalState(String channelId, @Nullable Number number) {
        if (number == null) {
            return;
        }
        updateState(channelId, new DecimalType(number));
    }

    private void updateDecimalState(String channelId, @Nullable String number) {
        if (number == null) {
            return;
        }
        try {
            var state = new DecimalType(Double.parseDouble(number));
            updateState(channelId, state);
        } catch (NumberFormatException e) {
            logger.debug("Cannot parse decimal number {}", number, e);
            updateState(channelId, UNDEF);
        }
    }

    private void updateBooleanState(String channelId, @Nullable Boolean bool) {
        if (bool == null) {
            return;
        }
        updateState(channelId, OnOffType.from(bool));
    }

    private void updatePercentState(String channelId, @Nullable Integer integer) {
        if (integer == null) {
            return;
        }
        updateState(channelId, new PercentType(integer));
    }

    private void updatePercentState(String channelId, @Nullable String integer) {
        if (integer == null) {
            return;
        }
        try {
            var state = new PercentType(integer);
            updateState(channelId, state);
        } catch (NumberFormatException e) {
            logger.debug("Cannot parse percent number {}", integer, e);
            updateState(channelId, UNDEF);
        }
    }

    private void updateLightState(String lightName, BambuLabBindingConstants.Channel channel,
            @Nullable List<Map<String, String>> lights) {
        Optional.ofNullable(lights)//
                .stream()//
                .flatMap(Collection::stream)//
                .filter(map -> lightName.equalsIgnoreCase(map.get("node")))//
                .map(map -> map.get("mode"))//
                .filter(Objects::nonNull)//
                .map(OnOffType::from)//
                .findAny()//
                .ifPresent(command -> updateState(channel.getName(), command));
    }

    private State parseWifiChannel(String wifi) {
        var matcher = DBM_PATTERN.matcher(wifi);
        if (!matcher.matches()) {
            return UNDEF;
        }

        var integer = matcher.group(1);
        try {
            var value = Integer.parseInt(integer);
            return new QuantityType<>(value, DECIBEL_MILLIWATTS);
        } catch (NumberFormatException e) {
            logger.debug("Cannot parse integer {} from wifi {}", integer, wifi, e);
            return UNDEF;
        }
    }

    public void sendCommand(PrinterClient.Channel.Command command) {
        logger.debug("Sending command {}", command);
        var localClient = client;
        if (localClient == null) {
            logger.warn("Client not connected. Cannot send command {}", command);
            return;
        }
        try {
            localClient.getChannel().sendCommand(command);
        } catch (Exception e) {
            updateStatus(OFFLINE, COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
    }

    @Override
    public void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    @Override
    public void updateState(String channelUID, State state) {
        super.updateState(channelUID, state);
    }

    @Override
    public void updateStatus(ThingStatus status) {
        super.updateStatus(status);
    }

    @Override
    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
