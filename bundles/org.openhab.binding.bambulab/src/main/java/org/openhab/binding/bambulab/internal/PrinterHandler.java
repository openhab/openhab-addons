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

import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.Channel.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.DECIBEL_MILLIWATTS;
import static org.openhab.core.thing.ThingStatus.*;
import static org.openhab.core.thing.ThingStatusDetail.CONFIGURATION_ERROR;
import static org.openhab.core.types.UnDefType.*;
import static pl.grzeslowski.jbambuapi.PrinterClient.Channel.PushingCommand.defaultPushingCommand;
import static pl.grzeslowski.jbambuapi.PrinterClientConfig.requiredFields;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
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
import pl.grzeslowski.jbambuapi.PrinterState;
import pl.grzeslowski.jbambuapi.PrinterWatcher;

/**
 * The {@link PrinterHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class PrinterHandler extends BaseThingHandler implements PrinterWatcher.PrinterStateSubscriber {
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
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "@token/handler.printer.init.noSerial");
            return;
        }
        logger = LoggerFactory.getLogger(PrinterHandler.class.getName() + "." + config.serial);

        if (config.hostname.isEmpty()) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "@token/handler.printer.init.noHostname");
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
                    "@token/handler.printer.init.invalidHostname[\"" + rawUri + "\"]");
            return;
        }

        if (config.accessCode.isEmpty()) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "@token/handler.printer.init.noAccessCode");
            return;
        }

        if (config.username.isEmpty()) {
            updateStatus(OFFLINE, CONFIGURATION_ERROR, "@token/handler.printer.init.noUsername");
            return;
        }

        updateStatus(UNKNOWN);

        try {
            client = new PrinterClient(
                    requiredFields(uri, config.username, config.serial, config.accessCode.toCharArray()));
        } catch (Exception e) {
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
    public void newPrinterState(@Nullable PrinterState delta, @Nullable PrinterState fullState) {
        // only need to update channels from delta
        // do not need to use full state, because at some point in past channels was already updated with its values
        if (delta == null) {
            return;
        }
        updatePrinterChannels(delta);
    }

    private void updatePrinterChannels(PrinterState state) {
        // PrintDetails
        var details = state.printDetails();
        updateCelsiusState(NOZZLE_TEMPERATURE_CHANNEL, details.nozzleTemperature());
        updateCelsiusState(NOZZLE_TARGET_TEMPERATURE_CHANNEL, details.nozzleTargetTemperature());
        updateCelsiusState(BED_TEMPERATURE_CHANNEL, details.nozzleTargetTemperature());
        updateCelsiusState(BED_TARGET_TEMPERATURE_CHANNEL, details.bedTargetTemperature());
        updateCelsiusState(CHAMBER_TEMPERATURE_CHANNEL, details.chamberTemperature());
        updateStringState(MC_PRINT_STAGE_CHANNEL, details.mcPrintStage());
        updatePercentState(MC_PERCENT_CHANNEL, details.mcPercent());
        updateDecimalState(MC_REMAINING_TIME_CHANNEL, details.mcRemainingTime());
        if (details.wifiSignal() != null) {
            updateState(WIFI_SIGNAL_CHANNEL, parseWifiChannel(details.wifiSignal()));
        }
        updateStringState(COMMAND_CHANNEL, details.command());
        updateDecimalState(MESSAGE_CHANNEL, details.message());
        updateStringState(SEQUENCE_ID_CHANNEL, details.sequenceId());

        // UpgradeState
        // var upgradeState = details.upgradeState();
        // updateState(Channel., new DecimalType(upgradeState.));
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

    private void updatePercentState(String channelId, @Nullable Integer integer) {
        if (integer == null) {
            return;
        }
        updateState(channelId, new PercentType(integer));
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
