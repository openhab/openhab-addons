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

import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.Channel.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.DECIBEL_MILLIWATTS;
import static org.openhab.core.thing.ThingStatus.*;
import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.openhab.core.types.UnDefType.UNDEF;
import static pl.grzeslowski.jbambuapi.PrinterClient.Channel.LedControlCommand.*;
import static pl.grzeslowski.jbambuapi.PrinterClient.Channel.LedControlCommand.LedNode.*;
import static pl.grzeslowski.jbambuapi.PrinterClient.Channel.PushingCommand.defaultPushingCommand;
import static pl.grzeslowski.jbambuapi.PrinterClientConfig.requiredFields;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
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
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.grzeslowski.jbambuapi.PrinterClient;
import pl.grzeslowski.jbambuapi.PrinterClientConfig;
import pl.grzeslowski.jbambuapi.PrinterWatcher;
import pl.grzeslowski.jbambuapi.Report;

/**
 * The {@link PrinterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class PrinterHandler extends BaseThingHandler implements PrinterWatcher.StateSubscriber {
    private static final Pattern DBM_PATTERN = Pattern.compile("^(-?\\d+)dBm$");
    private Logger logger = LoggerFactory.getLogger(PrinterHandler.class);

    private @Nullable PrinterClient client;

    public PrinterHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!CHANNEL_LED_CHAMBER_LIGHT.getName().equals(channelUID.getId())
                && !CHANNEL_LED_WORK_LIGHT.getName().equals(channelUID.getId())) {
            return;
        }
        var ledNode = CHANNEL_LED_CHAMBER_LIGHT.getName().equals(channelUID.getId()) ? CHAMBER_LIGHT : WORK_LIGHT;
        var bambuCommand = "ON".equals(command.toFullString()) ? on(ledNode) : off(ledNode);
        sendCommand(bambuCommand);
    }

    @Override
    public void initialize() {
        var config = getConfigAs(PrinterConfiguration.class);

        if (config.serial.isBlank()) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "@text/printer.handler.init.noSerial");
            return;
        }
        logger = LoggerFactory.getLogger(PrinterHandler.class.getName() + "." + config.serial);

        if (config.hostname.isBlank()) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "@text/printer.handler.init.noHostname");
            return;
        }

        var scheme = config.scheme;
        var port = config.port;
        var rawUri = "%s%s:%d".formatted(scheme, config.hostname, port);
        URI uri;
        try {
            uri = new URI(rawUri);
        } catch (URISyntaxException e) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR,
                    "@text/printer.handler.init.invalidHostname[\"%s\"]".formatted(rawUri));
            return;
        }

        if (config.accessCode.isBlank()) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "@text/printer.handler.init.noAccessCode");
            return;
        }

        if (config.username.isBlank()) {
            config.username = PrinterClientConfig.LOCAL_USERNAME;
        }

        updateStatus(UNKNOWN);

        PrinterClient localClient;
        try {
            localClient = client = new PrinterClient(
                    requiredFields(uri, config.username, config.serial, config.accessCode.toCharArray()));
        } catch (Exception e) {
            logger.debug("Cannot create MQTT client", e);
            updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
            return;
        }
        try {
            scheduler.execute(() -> {
                try {
                    logger.debug("Trying to connect to the printer broker");
                    localClient.connect();
                    var printerWatcher = new PrinterWatcher();
                    localClient.subscribe(printerWatcher);
                    printerWatcher.subscribe(this);
                    // send request to update all channels
                    refreshChannels();
                    updateStatus(ONLINE);
                } catch (Exception e) {
                    logger.debug("Cannot connect to MQTT client", e);
                    updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
                }
            });
        } catch (RejectedExecutionException ex) {
            logger.debug("Task was rejected", ex);
            updateStatus(OFFLINE, CONFIGURATION_ERROR, ex.getLocalizedMessage());
        }
    }

    void refreshChannels() {
        sendCommand(defaultPushingCommand());
    }

    @Override
    public void dispose() {
        var localClient = client;
        client = null;
        if (localClient != null) {
            try {
                localClient.close();
            } catch (Exception e) {
                logger.warn("Could not correctly dispose PrinterClient", e);
            }
        }
        logger = LoggerFactory.getLogger(PrinterHandler.class);
        super.dispose();
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
        updateDecimalState(CHANNEL_SPEED_LEVEL.getName(), print.spdLvl());
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
            List<Map<String, String>> lights) {
        lights.stream()//
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
            updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
        }
    }
}
