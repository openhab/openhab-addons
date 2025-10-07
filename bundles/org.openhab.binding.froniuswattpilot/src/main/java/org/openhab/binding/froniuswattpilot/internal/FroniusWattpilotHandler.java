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
package org.openhab.binding.froniuswattpilot.internal;

import static org.openhab.binding.froniuswattpilot.internal.FroniusWattpilotBindingConstants.*;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.io.EofException;
import org.eclipse.jetty.util.Utf8Appendable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.digiried.wattpilot.WattpilotClient;
import dev.digiried.wattpilot.WattpilotClientListener;
import dev.digiried.wattpilot.WattpilotInfo;
import dev.digiried.wattpilot.WattpilotStatus;
import dev.digiried.wattpilot.commands.SetBoostCommand;
import dev.digiried.wattpilot.commands.SetBoostSoCLimitCommand;
import dev.digiried.wattpilot.commands.SetChargingCurrentCommand;
import dev.digiried.wattpilot.commands.SetChargingModeCommand;
import dev.digiried.wattpilot.commands.SetEnforcedChargingStateCommand;
import dev.digiried.wattpilot.commands.SetSurplusPowerThresholdCommand;
import dev.digiried.wattpilot.commands.SetSurplusSoCThresholdCommand;
import dev.digiried.wattpilot.dto.ChargingMode;
import dev.digiried.wattpilot.dto.EnforcedChargingState;

/**
 * The {@link FroniusWattpilotHandler} is responsible for handling commands, which are
 * sent to one of the channels, and updating the channels with the current states.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class FroniusWattpilotHandler extends BaseThingHandler implements WattpilotClientListener {
    private final Logger logger = LoggerFactory.getLogger(FroniusWattpilotHandler.class);
    private final WattpilotClient client;

    private @Nullable FroniusWattpilotConfiguration config;

    public FroniusWattpilotHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        client = new WattpilotClient(httpClient);
        client.addListener(this);
    }

    private @Nullable Integer getPercent(Command command) {
        switch (command) {
            case QuantityType<?> qt -> {
                qt = qt.toUnit(Units.PERCENT);
                if (qt == null) {
                    logger.debug("Failed to convert QuantityType to PERCENT!");
                    return null;
                }
                return qt.intValue();
            }
            case DecimalType dt -> {
                return dt.intValue();
            }
            default -> {
                logger.debug("Command has wrong type, QuantityType or DecimalType required!");
                return null;
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String groupId = channelUID.getGroupId();
        if (!(CHANNEL_GROUP_ID_CONTROL.equals(groupId))) {
            return;
        }
        String channelId = channelUID.getIdWithoutGroup();
        try {
            switch (channelId) {
                case CHANNEL_CHARGING_ALLOWED:
                    if (command instanceof OnOffType oft) {
                        client.sendCommand(new SetEnforcedChargingStateCommand(
                                oft == OnOffType.OFF ? EnforcedChargingState.OFF : EnforcedChargingState.NEUTRAL));
                    } else {
                        logger.debug("Command has wrong type, OnOffType required!");
                    }
                    break;
                case CHANNEL_CHARGING_MODE:
                    if (command instanceof StringType st) {
                        client.sendCommand(new SetChargingModeCommand(ChargingMode.valueOf(st.toString())));
                    } else {
                        logger.debug("Command has wrong type, StringType required!");
                    }
                    break;
                case CHANNEL_CHARGING_CURRENT:
                    int ampere;
                    switch (command) {
                        case QuantityType<?> qt -> {
                            qt = qt.toUnit(Units.AMPERE);
                            if (qt == null) {
                                logger.debug("Failed to convert QuantityType to AMPERE!");
                                return;
                            }
                            ampere = qt.intValue();
                        }
                        case DecimalType dt -> ampere = dt.intValue();
                        default -> {
                            logger.debug("Command has wrong type, QuantityType or DecimalType required!");
                            return;
                        }
                    }
                    client.sendCommand(new SetChargingCurrentCommand(ampere));
                    break;
                case CHANNEL_PV_SURPLUS_THRESHOLD:
                    int watts;
                    switch (command) {
                        case QuantityType<?> qt -> {
                            qt = qt.toUnit(Units.WATT);
                            if (qt == null) {
                                logger.debug("Failed to convert QuantityType to WATT!");
                                return;
                            }
                            watts = qt.intValue();
                        }
                        case DecimalType dt -> watts = dt.intValue();
                        default -> {
                            logger.debug("Command has wrong type, QuantityType or DecimalType required!");
                            return;
                        }
                    }
                    client.sendCommand(new SetSurplusPowerThresholdCommand(watts));
                    break;
                case CHANNEL_PV_SURPLUS_SOC:
                    Integer surplusSoC = getPercent(command);
                    if (surplusSoC != null) {
                        client.sendCommand(new SetSurplusSoCThresholdCommand(surplusSoC));
                    }
                    break;
                case CHANNEL_BOOST_ENABLED:
                    if (command instanceof OnOffType oft) {
                        client.sendCommand(new SetBoostCommand(oft == OnOffType.ON));
                    } else {
                        logger.debug("Command has wrong type, OnOffType required!");
                    }
                    break;
                case CHANNEL_BOOST_SOC:
                    Integer boostSoc = getPercent(command);
                    if (boostSoc != null) {
                        client.sendCommand(new SetBoostSoCLimitCommand(boostSoc));
                    }
                    break;
                default:
                    logger.debug("Unknown channel id: {}", channelId);
            }
        } catch (IllegalArgumentException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                logger.debug("Failed to handle command {} for channel {}: {}", command, channelUID, e.getMessage());
            } else {
                logger.debug("Failed to handle command {} for channel {}: {} -> {}", command, channelUID,
                        e.getMessage(), cause.getMessage());
            }
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(FroniusWattpilotConfiguration.class);

        FroniusWattpilotConfiguration config = this.config;
        if (config == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }

        if (config.hostname.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.no-host");
            return;
        }

        if (config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.no-password");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        try {
            client.connect(config.hostname, config.password);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        try {
            client.disconnect().get(3, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.error("Failed to disconnect", e);
        }
        client.removeListener(this);
    }

    @Override
    public void connected() {
        updateStatus(ThingStatus.ONLINE);
        updateDeviceProperties(client.getDeviceInfo());
    }

    @Override
    public void disconnected(@Nullable String reason, @Nullable Throwable cause) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
        if (cause instanceof TimeoutException || cause instanceof NoRouteToHostException
                || cause instanceof EofException || cause instanceof Utf8Appendable.NotUtf8Exception) {
            this.logger.debug("Connection to Wattpilot lost, scheduling reconnection attempt.");
            this.scheduler.schedule(this::initialize, 30L, TimeUnit.SECONDS);
        }
    }

    @Override
    public void statusChanged(@Nullable WattpilotStatus status) {
        if (status == null) {
            return;
        }
        updateChannelsControl(status);
        updateChannelsStatus(status);
        updateChannelsMetrics(status);
    }

    private void updateDeviceProperties(WattpilotInfo deviceInfo) {
        Map<String, String> properties = editProperties();
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, deviceInfo.serial());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, deviceInfo.firmwareVersion());
        updateProperties(properties);
    }

    private void updateChannelsControl(WattpilotStatus status) {
        final ThingUID uid = getThing().getUID();
        ChannelUID channel;

        // generic charging control
        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_CONTROL, CHANNEL_CHARGING_ALLOWED);
        updateState(channel,
                status.getEnforcedChargingState() == EnforcedChargingState.OFF ? OnOffType.OFF : OnOffType.ON);

        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_CONTROL, CHANNEL_CHARGING_MODE);
        updateState(channel, new StringType(status.getChargingMode().toString()));

        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_CONTROL, CHANNEL_CHARGING_CURRENT);
        updateState(channel, new QuantityType<>(status.getChargingCurrent(), Units.AMPERE));

        // PV surplus charging
        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_CONTROL, CHANNEL_PV_SURPLUS_THRESHOLD);
        updateState(channel, new QuantityType<>(status.getSurplusPowerThreshold(), Units.WATT));

        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_CONTROL, CHANNEL_PV_SURPLUS_SOC);
        updateState(channel, new DecimalType(status.getSurplusSoCThreshold()));

        // boost charging
        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_CONTROL, CHANNEL_BOOST_ENABLED);
        updateState(channel, status.isBoostEnabled() ? OnOffType.ON : OnOffType.OFF);

        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_CONTROL, CHANNEL_BOOST_SOC);
        updateState(channel, new DecimalType(status.getBoostSoCLimit()));
    }

    private void updateChannelsStatus(WattpilotStatus status) {
        final ThingUID uid = getThing().getUID();
        ChannelUID channel;

        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_STATUS, CHANNEL_CHARGING_STATE);
        updateState(channel, new StringType(status.getChargingState().toString()));

        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_STATUS, CHANNEL_CHARGING_POSSIBLE);
        updateState(channel, status.isChargingAllowed() ? OnOffType.ON : OnOffType.OFF);

        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_STATUS, CHANNEL_CHARGING_SINGLE_PHASE);
        updateState(channel, status.isChargingSinglePhase() ? OnOffType.ON : OnOffType.OFF);
    }

    private void updateChannelsMetrics(WattpilotStatus status) {
        final ThingUID uid = getThing().getUID();
        ChannelUID channel;

        // total
        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_METRICS, CHANNEL_POWER);
        updateState(channel, new QuantityType<>(status.getChargingMetrics().power(), Units.WATT));

        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_METRICS, CHANNEL_CHARGED_ENERGY_SESSION);
        updateState(channel, new QuantityType<>(status.getEnergyCounterSinceStart(), Units.WATT_HOUR));

        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_METRICS, CHANNEL_CHARGED_ENERGY_TOTAL);
        updateState(channel, new QuantityType<>(status.getEnergyCounterTotal(), Units.WATT_HOUR));

        // phase 1
        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_METRICS, PREFIX_PHASE_1 + CHANNEL_POWER);
        updateState(channel, new QuantityType<>(status.getChargingMetrics().power1(), Units.WATT));

        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_METRICS, PREFIX_PHASE_1 + CHANNEL_VOLTAGE);
        updateState(channel, new QuantityType<>(status.getChargingMetrics().voltage1(), Units.VOLT));

        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_METRICS, PREFIX_PHASE_1 + CHANNEL_CURRENT);
        updateState(channel, new QuantityType<>(status.getChargingMetrics().amperage1(), Units.AMPERE));

        // phase 2
        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_METRICS, PREFIX_PHASE_2 + CHANNEL_POWER);
        updateState(channel, new QuantityType<>(status.getChargingMetrics().power2(), Units.WATT));

        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_METRICS, PREFIX_PHASE_2 + CHANNEL_VOLTAGE);
        updateState(channel, new QuantityType<>(status.getChargingMetrics().voltage2(), Units.VOLT));

        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_METRICS, PREFIX_PHASE_2 + CHANNEL_CURRENT);
        updateState(channel, new QuantityType<>(status.getChargingMetrics().amperage2(), Units.AMPERE));

        // phase 3
        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_METRICS, PREFIX_PHASE_3 + CHANNEL_POWER);
        updateState(channel, new QuantityType<>(status.getChargingMetrics().power3(), Units.WATT));

        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_METRICS, PREFIX_PHASE_3 + CHANNEL_VOLTAGE);
        updateState(channel, new QuantityType<>(status.getChargingMetrics().voltage3(), Units.VOLT));

        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_METRICS, PREFIX_PHASE_3 + CHANNEL_CURRENT);
        updateState(channel, new QuantityType<>(status.getChargingMetrics().amperage3(), Units.AMPERE));
    }
}
