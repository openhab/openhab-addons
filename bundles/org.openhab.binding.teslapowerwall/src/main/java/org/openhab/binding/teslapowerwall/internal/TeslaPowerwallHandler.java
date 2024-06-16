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
package org.openhab.binding.teslapowerwall.internal;

import java.io.IOException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.teslapowerwall.internal.api.BatterySOE;
import org.openhab.binding.teslapowerwall.internal.api.GridStatus;
import org.openhab.binding.teslapowerwall.internal.api.MeterAggregates;
import org.openhab.binding.teslapowerwall.internal.api.Operations;
import org.openhab.binding.teslapowerwall.internal.api.SystemStatus;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TeslaPowerwallHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class TeslaPowerwallHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TeslaPowerwallHandler.class);

    private @NonNullByDefault({}) TeslaPowerwallConfiguration config;
    private @NonNullByDefault({}) TeslaPowerwallWebTargets webTargets;
    private @Nullable ScheduledFuture<?> pollFuture;

    public TeslaPowerwallHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.warn("This binding is read only");
    }

    @Override
    public void initialize() {
        config = getConfigAs(TeslaPowerwallConfiguration.class);
        logger.debug("config.hostname = {}, refresh = {}", config.hostname, config.refresh);
        if (config.hostname.isBlank() || config.email.isBlank() || config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hostname/IP address must be set");
            return;
        } else {
            webTargets = new TeslaPowerwallWebTargets(config.hostname);
            updateStatus(ThingStatus.UNKNOWN);
            schedulePoll();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        stopPoll();
    }

    private void schedulePoll() {
        ScheduledFuture<?> pollFuture = this.pollFuture;
        if (pollFuture != null) {
            pollFuture.cancel(false);
        }
        logger.debug("Scheduling poll for 1 second out, then every {} s", config.refresh);
        this.pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 1, config.refresh, TimeUnit.SECONDS);
    }

    private void poll() {
        try {
            logger.debug("Polling for state");
            pollStatus();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void stopPoll() {
        final Future<?> future = pollFuture;
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
            pollFuture = null;
        }
    }

    private void pollStatus() throws IOException {
        Operations operations = null;
        BatterySOE batterySOE = null;
        GridStatus gridStatus = null;
        SystemStatus systemStatus = null;
        MeterAggregates meterAggregates = null;
        try {
            operations = webTargets.getOperations(config.email, config.password);
            batterySOE = webTargets.getBatterySOE(config.email, config.password);
            gridStatus = webTargets.getGridStatus(config.email, config.password);
            systemStatus = webTargets.getSystemStatus(config.email, config.password);
            meterAggregates = webTargets.getMeterAggregates(config.email, config.password);
            updateStatus(ThingStatus.ONLINE);
        } catch (TeslaPowerwallCommunicationException e) {
            logger.debug("Unexpected error connecting to Tesla Powerwall", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            return;
        }

        updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_MODE, new StringType(operations.mode));
        updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_RESERVE,
                new QuantityType<>(((operations.reserve / 0.95) - (5 / 0.95)), Units.PERCENT));
        updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_BATTERYSOE,
                new QuantityType<>(((batterySOE.soe / 0.95) - (5 / 0.95)), Units.PERCENT));
        updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_GRIDSTATUS,
                new StringType(gridStatus.grid_status));
        updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_GRIDSERVICES,
                (gridStatus.grid_services ? OnOffType.ON : OnOffType.OFF));
        updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_FULL_PACK_ENERGY,
                new QuantityType<>(systemStatus.full_pack_energy, Units.WATT_HOUR));
        if (systemStatus.full_pack_energy < 13500) {
            updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_DEGRADATION,
                    new QuantityType<>(
                            (TeslaPowerwallBindingConstants.TESLA_POWERWALL_CAPACITY - systemStatus.full_pack_energy)
                                    / TeslaPowerwallBindingConstants.TESLA_POWERWALL_CAPACITY * 100,
                            Units.PERCENT));
        } else {
            updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_DEGRADATION,
                    new QuantityType<>(0, Units.PERCENT));
        }
        updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_GRID_INSTPOWER,
                new QuantityType<>(meterAggregates.grid_instpower, MetricPrefix.KILO(Units.WATT)));
        updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_GRID_ENERGYEXPORTED,
                new QuantityType<>(meterAggregates.grid_energyexported, Units.KILOWATT_HOUR));
        updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_GRID_ENERGYIMPORTED,
                new QuantityType<>(meterAggregates.grid_energyimported, Units.KILOWATT_HOUR));
        updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_BATTERY_INSTPOWER,
                new QuantityType<>(meterAggregates.battery_instpower, MetricPrefix.KILO(Units.WATT)));
        updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_BATTERY_ENERGYEXPORTED,
                new QuantityType<>(meterAggregates.battery_energyexported, Units.KILOWATT_HOUR));
        updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_BATTERY_ENERGYIMPORTED,
                new QuantityType<>(meterAggregates.battery_energyimported, Units.KILOWATT_HOUR));
        updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_HOME_INSTPOWER,
                new QuantityType<>(meterAggregates.home_instpower, MetricPrefix.KILO(Units.WATT)));
        updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_HOME_ENERGYEXPORTED,
                new QuantityType<>(meterAggregates.home_energyexported, Units.KILOWATT_HOUR));
        updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_HOME_ENERGYIMPORTED,
                new QuantityType<>(meterAggregates.home_energyimported, Units.KILOWATT_HOUR));
        updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_SOLAR_INSTPOWER,
                new QuantityType<>(meterAggregates.solar_instpower, MetricPrefix.KILO(Units.WATT)));
        updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_SOLAR_ENERGYEXPORTED,
                new QuantityType<>(meterAggregates.solar_energyexported, Units.KILOWATT_HOUR));
        updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_SOLAR_ENERGYIMPORTED,
                new QuantityType<>(meterAggregates.solar_energyimported, Units.KILOWATT_HOUR));
    }
}
