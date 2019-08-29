/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.library.unit.MetricPrefix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.openhab.binding.teslapowerwall.internal.TeslaPowerwallWebTargets;
import org.openhab.binding.teslapowerwall.internal.api.MeterAggregates;
import org.openhab.binding.teslapowerwall.internal.api.BatterySOE;
import org.openhab.binding.teslapowerwall.internal.api.GridStatus;
import org.openhab.binding.teslapowerwall.internal.api.Operations;
import org.openhab.binding.teslapowerwall.internal.TeslaPowerwallConfiguration;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * The {@link TeslaPowerwallHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class TeslaPowerwallHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TeslaPowerwallHandler.class);

    private long refreshInterval;

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
        TeslaPowerwallConfiguration config = getConfigAs(TeslaPowerwallConfiguration.class);
        logger.debug("config.hostname = {}, refresh = {}", config.hostname, config.refresh);
        if (config.hostname == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Hostname/IP address must be set");
        } else {
            webTargets = new TeslaPowerwallWebTargets(config.hostname);
            refreshInterval = config.refresh;

            schedulePoll();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        stopPoll();
    }

    private void schedulePoll() {
        if (pollFuture != null) {
            pollFuture.cancel(false);
        }
        logger.debug("Scheduling poll for 1 second out, then every {} s", refreshInterval);
        pollFuture = scheduler.scheduleWithFixedDelay(this::poll, 1, refreshInterval, TimeUnit.SECONDS);
    }

    private void poll() {
        try {
            logger.debug("Polling for state");
            pollStatus();
        } catch (IOException e) {
            logger.debug("Could not connect to Tesla Powerwall", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        } catch (RuntimeException e) {
            logger.warn("Unexpected error connecting to Tesla Powerwall", e);
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
        TeslaPowerwallConfiguration config = getConfigAs(TeslaPowerwallConfiguration.class);
        BatterySOE batterySOE = webTargets.getBatterySOE();
        GridStatus gridStatus = webTargets.getGridStatus();
        MeterAggregates meterAggregates = webTargets.getMeterAggregates();
        String token = webTargets.getToken(config.email, config.password);
        Operations operations = webTargets.getOperations(token);
        updateStatus(ThingStatus.ONLINE);
        if (batterySOE != null) {
            updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_BATTERYSOE, new QuantityType<>(batterySOE.soe, SmartHomeUnits.PERCENT));
        }
        if (operations != null) {
            updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_MODE, new StringType(operations.mode));
            updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_RESERVE, new QuantityType<>(operations.reserve, SmartHomeUnits.PERCENT));
        }
        if (gridStatus != null) {
            updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_GRIDSTATUS, new StringType(gridStatus.grid_status));
        }
        if (meterAggregates != null) {
            updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_GRID_INSTPOWER, new QuantityType<>(meterAggregates.grid_instpower, MetricPrefix.KILO(SmartHomeUnits.WATT)));
            updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_GRID_ENERGYEXPORTED, new QuantityType<>(meterAggregates.grid_energyexported,SmartHomeUnits.KILOWATT_HOUR));
            updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_GRID_ENERGYIMPORTED, new QuantityType<>(meterAggregates.grid_energyimported,SmartHomeUnits.KILOWATT_HOUR));
            updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_BATTERY_INSTPOWER, new QuantityType<>(meterAggregates.battery_instpower, MetricPrefix.KILO(SmartHomeUnits.WATT)));
            updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_BATTERY_ENERGYEXPORTED, new QuantityType<>(meterAggregates.battery_energyexported,SmartHomeUnits.KILOWATT_HOUR));
            updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_BATTERY_ENERGYIMPORTED, new QuantityType<>(meterAggregates.battery_energyimported,SmartHomeUnits.KILOWATT_HOUR));
            updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_HOME_INSTPOWER, new QuantityType<>(meterAggregates.home_instpower, MetricPrefix.KILO(SmartHomeUnits.WATT)));
            updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_HOME_ENERGYEXPORTED, new QuantityType<>(meterAggregates.home_energyexported,SmartHomeUnits.KILOWATT_HOUR));
            updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_HOME_ENERGYIMPORTED, new QuantityType<>(meterAggregates.home_energyimported,SmartHomeUnits.KILOWATT_HOUR));
            updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_SOLAR_INSTPOWER, new QuantityType<>(meterAggregates.solar_instpower, MetricPrefix.KILO(SmartHomeUnits.WATT)));
            updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_SOLAR_ENERGYEXPORTED, new QuantityType<>(meterAggregates.solar_energyexported,SmartHomeUnits.KILOWATT_HOUR));
            updateState(TeslaPowerwallBindingConstants.CHANNEL_TESLAPOWERWALL_SOLAR_ENERGYIMPORTED, new QuantityType<>(meterAggregates.solar_energyimported,SmartHomeUnits.KILOWATT_HOUR));
        }
    }
}
