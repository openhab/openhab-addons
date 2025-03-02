/**
 * Copyright (c) 2010-2025 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.bambulab.internal;

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
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.grzeslowski.jbambuapi.PrinterClient;
import pl.grzeslowski.jbambuapi.PrinterClientConfig;
import pl.grzeslowski.jbambuapi.PrinterWatcher;
import pl.grzeslowski.jbambuapi.Report;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.Channel.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.DECIBEL_MILLIWATTS;
import static org.openhab.core.thing.ThingStatus.*;
import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.openhab.core.types.UnDefType.UNDEF;
import static pl.grzeslowski.jbambuapi.PrinterClient.Channel.PushingCommand.defaultPushingCommand;
import static pl.grzeslowski.jbambuapi.PrinterClientConfig.requiredFields;

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
        if (command instanceof RefreshType) {
            // there is no way of refreshing one channel
            return;
        }
    }

    @Override
    public void initialize() {
        var config = getConfigAs(PrinterConfiguration.class);

        if (config.serial.isEmpty()) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "@token/printer.handler.init.noSerial");
            return;
        }
        logger = LoggerFactory.getLogger(PrinterHandler.class.getName() + "." + config.serial);

        if (config.hostname.isEmpty()) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "@token/printer.handler.init.noHostname");
            return;
        }

        var scheme = config.scheme;
        var port = config.port;
        var rawUri = "%s%s:%d".formatted(scheme, config.hostname, port);
        URI uri;
        try {
            uri = new URI(rawUri);
        } catch (URISyntaxException e) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "@token/printer.handler.init.invalidHostname[\"" + rawUri + "\"]");
            return;
        }

        if (config.accessCode.isEmpty()) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "@token/printer.handler.init.noAccessCode");
            return;
        }

        if (config.username.isEmpty()) {
            config.username = PrinterClientConfig.LOCAL_USERNAME;
        }

        updateStatus(UNKNOWN);

        try {
            client = new PrinterClient(
                    requiredFields(uri, config.username, config.serial, config.accessCode.toCharArray()));
        } catch (Exception e) {
            logger.debug("Cannot create MQTT client", e);
            updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
            return;
        }
        scheduler.execute(() -> {
            try {
                client.connect();
                var printerWatcher = new PrinterWatcher();
                client.subscribe(printerWatcher);
                printerWatcher.subscribe(this);
                // send request to update all channels
                refreshChannels();
                updateStatus(ONLINE);
            } catch (Exception e) {
                logger.debug("Cannot connect to MQTT client", e);
                updateStatus(OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getLocalizedMessage());
            }
        });
    }

    void refreshChannels() {
        var localClient = client;
        if (localClient == null) {
            return;
        }
        localClient.getChannel().sendCommand(defaultPushingCommand());
    }

    @Override
    public void dispose() {
        {
            var localClient = client;
            client = null;
            if (localClient != null) {
                try {
                    localClient.close();
                } catch (Exception e) {
                    logger.warn("Could not correctly dispose PrinterClient", e);
                }
            }
        }
        logger = LoggerFactory.getLogger(PrinterHandler.class);
        super.dispose();
    }

    @Override
    public void newState(@Nullable Report delta, @Nullable Report fullState) {
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
        // tempers
        updateCelsiusState(NOZZLE_TEMPERATURE_CHANNEL, print.nozzleTemper());
        updateCelsiusState(NOZZLE_TARGET_TEMPERATURE_CHANNEL, print.nozzleTargetTemper());
        updateCelsiusState(BED_TEMPERATURE_CHANNEL, print.bedTemper());
        updateCelsiusState(BED_TARGET_TEMPERATURE_CHANNEL, print.bedTargetTemper());
        updateCelsiusState(CHAMBER_TEMPERATURE_CHANNEL, print.chamberTemper());
        // string
        updateStringState(MC_PRINT_STAGE_CHANNEL, print.mcPrintStage());
        updateStringState(BED_TYPE_CHANNEL, print.bedType());
        updateStringState(GCODE_fILE_CHANNEL, print.gcodeFile());
        updateStringState(GCODE_STATE_CHANNEL, print.gcodeState());
        updateStringState(REASON_CHANNEL, print.reason());
        updateStringState(RESULT_CHANNEL, print.result());
        // percent
        updatePercentState(MC_PERCENT_CHANNEL, print.mcPercent());
        updatePercentState(GCODE_FILE_PREPARE_PERCENT_CHANNEL, print.gcodeFilePreparePercent());
        // decimal
        updateDecimalState(MC_REMAINING_TIME_CHANNEL, print.mcRemainingTime());
        updateDecimalState(BIG_FAN_1_SPEED_CHANNEL, print.bigFan1Speed());
        updateDecimalState(BIG_FAN_2_SPEED_CHANNEL, print.bigFan2Speed());
        updateDecimalState(HEAT_BREAK_FAN_SPEED_CHANNEL, print.heatbreakFanSpeed());
        updateDecimalState(LAYER_NUM_CHANNEL, print.layerNum());
        updateDecimalState(SPEED_LEVEL_CHANNEL, print.spdLvl());
        // boolean
        updateBooleanState(TIMELAPS_CHANNEL, print.timelapse());
        updateBooleanState(USE_AMS_CHANNEL, print.useAms());
        updateBooleanState(VIBRATION_CALIBRATION_CHANNEL, print.vibrationCali());
        //other
        if (print.wifiSignal() != null) {
            updateState(WIFI_SIGNAL_CHANNEL, parseWifiChannel(print.wifiSignal()));
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

    private State parseWifiChannel(String wifi) {
        var matcher = DBM_PATTERN.matcher(wifi);
        if (!matcher.matches()) {
            return UNDEF;
        }

        var value = Integer.parseInt(matcher.group(1));
        return new QuantityType<>(value, DECIBEL_MILLIWATTS);
    }

    public void sendCommand(PrinterClient.Channel.Command command) {
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
