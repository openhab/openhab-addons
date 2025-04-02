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
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
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

import com.florianhotze.wattpilot.WattpilotClient;
import com.florianhotze.wattpilot.WattpilotClientListener;
import com.florianhotze.wattpilot.WattpilotStatus;

/**
 * The {@link FroniusWattpilotHandler} is responsible for handling commands, which are
 * sent to one of the channels.
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

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(FroniusWattpilotConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        String hostname = config.hostname;
        String password = config.password;

        if (hostname == null || hostname.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hostname is missing");
            return;
        }

        if (password == null || password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Password is missing");
            return;
        }

        try {
            client.connect(config.hostname, config.password);
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        if (client.isConnected()) {
            client.disconnect();
        }
    }

    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        super.handleConfigurationUpdate(configurationParameters);

        if (client.isConnected()) {
            client.disconnect();
        }
        initialize();
    }

    @Override
    public void connected() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void disconnected(@Nullable String reason, @Nullable Throwable cause) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
        if (cause instanceof Exception e) {
            if (e.getCause() instanceof TimeoutException) {
                // TODO: Handle timeout
            }
        }
    }

    @Override
    public void statusChanged(@Nullable WattpilotStatus status) {
        if (status == null) {
            return;
        }
        updateChannelsStatus(status);
        updateChannelsMetrics(status);
    }

    private void updateChannelsStatus(WattpilotStatus status) {
        final ThingUID uid = getThing().getUID();
        ChannelUID channel;

        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_STATUS, CHANNEL_CHARGING_STATE);
        updateState(channel, new StringType(status.getChargingState().toString()));

        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_STATUS, CHANNEL_CHARGING_ALLOWED);
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

        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_METRICS, CHANNEL_CHARGED_ENERGY);
        updateState(channel, new QuantityType<>(status.getEnergyCounterSinceStart(), Units.WATT_HOUR));

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
