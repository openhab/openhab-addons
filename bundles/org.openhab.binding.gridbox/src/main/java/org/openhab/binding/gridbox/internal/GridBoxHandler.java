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
package org.openhab.binding.gridbox.internal;

import java.io.IOException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gridbox.internal.api.GridBoxApi;
import org.openhab.binding.gridbox.internal.api.GridBoxApi.GridBoxApiAutheticationException;
import org.openhab.binding.gridbox.internal.api.GridBoxApi.GridBoxApiException;
import org.openhab.binding.gridbox.internal.api.GridBoxApi.GridBoxApiSystemNotFoundException;
import org.openhab.binding.gridbox.internal.model.BatterySummary;
import org.openhab.binding.gridbox.internal.model.EvChargingStationSummary;
import org.openhab.binding.gridbox.internal.model.LiveData;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;

/**
 * The {@link GridBoxHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Benedikt Kuntz - Initial contribution
 */
@NonNullByDefault
public class GridBoxHandler extends BaseThingHandler {

    private static final int CONNECTION_RETRY_PERIOD = 10;

    private static final GridBoxApi API = new GridBoxApi();

    private GridBoxConfiguration config = new GridBoxConfiguration();

    private @Nullable ScheduledFuture<?> updateScheduledFuture;

    public GridBoxHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            scheduler.execute(this::update);
        }
    }

    @Override
    public void handleRemoval() {
        stopUpdater();
        super.handleRemoval();
    }

    public void handleLiveDataResponse(LiveData newLiveData) {
        BatterySummary battery = newLiveData.getBattery();
        if (battery != null) {
            updateState(GridBoxBindingConstants.BATTERY_CAPACITY,
                    new QuantityType<>(battery.getCapacity(), Units.WATT_HOUR));
            updateState(GridBoxBindingConstants.BATTERY_NOMINAL_CAPACITY,
                    new QuantityType<>(battery.getNominalCapacity(), Units.WATT_HOUR));
            updateState(GridBoxBindingConstants.BATTERY_POWER, new QuantityType<>(battery.getPower(), Units.WATT));
            updateState(GridBoxBindingConstants.BATTERY_REMAINING_CHARGE,
                    new QuantityType<>(battery.getRemainingCharge(), Units.WATT_HOUR));
            updateState(GridBoxBindingConstants.BATTERY_STATE_OF_CHARGE, new DecimalType(battery.getStateOfCharge()));
            double batteryLevel = ((double) battery.getRemainingCharge()) / battery.getCapacity() * 100;
            updateState(GridBoxBindingConstants.BATTERY_LEVEL, new QuantityType<>(batteryLevel, Units.PERCENT));
        }

        updateState(GridBoxBindingConstants.CONSUMPTION, new QuantityType<>(newLiveData.getConsumption(), Units.WATT));
        updateState(GridBoxBindingConstants.DIRECT_CONSUMPTION,
                new QuantityType<>(newLiveData.getDirectConsumption(), Units.WATT));
        updateState(GridBoxBindingConstants.DIRECT_CONSUMPTION_EV,
                new QuantityType<>(newLiveData.getDirectConsumptionEV(), Units.WATT));
        updateState(GridBoxBindingConstants.DIRECT_CONSUMPTION_HEAT_PUMP,
                new QuantityType<>(newLiveData.getDirectConsumptionHeatPump(), Units.WATT));
        updateState(GridBoxBindingConstants.DIRECT_CONSUMPTION_HEATER,
                new QuantityType<>(newLiveData.getDirectConsumptionHeater(), Units.WATT));
        updateState(GridBoxBindingConstants.DIRECT_CONSUMPTION_HOUSEHOLD,
                new QuantityType<>(newLiveData.getDirectConsumptionHousehold(), Units.WATT));
        updateState(GridBoxBindingConstants.DIRECT_CONSUMPTION_RATE,
                new QuantityType<>(newLiveData.getDirectConsumptionRate() * 100, Units.PERCENT));

        EvChargingStationSummary evChargingStation = newLiveData.getEvChargingStation();
        if (evChargingStation != null) {
            updateState(GridBoxBindingConstants.EV_CHARGING_STATION_POWER,
                    new QuantityType<>(evChargingStation.getPower(), Units.WATT));
        }

        updateState(GridBoxBindingConstants.HEAT_PUMP_POWER, new QuantityType<>(newLiveData.getHeatPump(), Units.WATT));
        updateState(GridBoxBindingConstants.PHOTOVOLTAIC_PRODUCTION,
                new QuantityType<>(newLiveData.getPhotovoltaic(), Units.WATT));
        updateState(GridBoxBindingConstants.PRODUCTION, new QuantityType<>(newLiveData.getProduction(), Units.WATT));
        updateState(GridBoxBindingConstants.SELF_CONSUMPTION,
                new QuantityType<>(newLiveData.getSelfConsumption(), Units.WATT));
        updateState(GridBoxBindingConstants.SELF_CONSUMPTION_RATE,
                new QuantityType<>(newLiveData.getSelfConsumptionRate() * 100, Units.PERCENT));
        updateState(GridBoxBindingConstants.SELF_SUFFICIENCY_RATE,
                new QuantityType<>(newLiveData.getSelfSufficiencyRate() * 100, Units.PERCENT));
        updateState(GridBoxBindingConstants.SELF_SUPPLY, new QuantityType<>(newLiveData.getSelfSupply(), Units.WATT));
        updateState(GridBoxBindingConstants.TOTAL_CONSUMPTION,
                new QuantityType<>(newLiveData.getTotalConsumption(), Units.WATT));
    }

    @Override
    public void initialize() {
        config = getConfigAs(GridBoxConfiguration.class);
        String user = config.email;
        if (user == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "User ID not provided");
            return;
        }
        String password = config.password;
        if (password == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "User password not provided");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(this::initializeApi);
    }

    private void initializeApi() {
        updateStatus(ThingStatus.UNKNOWN);

        try {
            String idToken = config.idToken;
            if (idToken == null || idToken.isBlank()) {
                config.idToken = API.getIdToken(config);
            }

            String systemId = config.systemId;
            if (systemId == null || systemId.isBlank()) {
                config.systemId = API.getSystemId(config);
            }

            updateScheduledFuture = scheduler.scheduleWithFixedDelay(this::update, 0, config.refreshInterval,
                    TimeUnit.SECONDS);
        } catch (GridBoxApiAutheticationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Username or password invalid?");
        } catch (IOException | InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Connection to GridBox lost, try to re-connect in " + CONNECTION_RETRY_PERIOD + " seconds");
            scheduler.schedule(this::initialize, CONNECTION_RETRY_PERIOD, TimeUnit.SECONDS);
        } catch (GridBoxApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Unable to initialize GridBox thing, invalid response from API");
        }
    }

    private void update() {
        try {
            API.retrieveLiveData(config, this::handleLiveDataResponse);
            updateStatus(ThingStatus.ONLINE);
        } catch (GridBoxApiAutheticationException e) {
            // maybe the authentication is no longer valid, so try to re-authenticate
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING,
                    "Authentication lost, trying to re-authenticate");
            stopUpdater();
            config.idToken = null;
            scheduler.execute(this::initialize);
        } catch (GridBoxApiSystemNotFoundException e) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.HANDLER_CONFIGURATION_PENDING,
                    "System ID not known, try to re-acquire it");
            stopUpdater();
            config.systemId = null;
            scheduler.execute(this::initialize);
        } catch (IOException | InterruptedException | GridBoxApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Connection to GridBox lost, try to re-establish connection in " + CONNECTION_RETRY_PERIOD
                            + " seconds");
            stopUpdater();
            scheduler.schedule(this::initialize, CONNECTION_RETRY_PERIOD, TimeUnit.SECONDS);
        }
    }

    private void stopUpdater() {
        if (updateScheduledFuture != null) {
            updateScheduledFuture.cancel(true);
            updateScheduledFuture = null;
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        stopUpdater();
    }
}
