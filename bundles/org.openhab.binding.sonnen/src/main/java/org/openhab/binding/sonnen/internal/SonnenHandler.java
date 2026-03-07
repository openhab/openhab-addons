/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.sonnen.internal;

import static org.openhab.binding.sonnen.internal.SonnenBindingConstants.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sonnen.internal.communication.SonnenJSONCommunication;
import org.openhab.binding.sonnen.internal.communication.SonnenJsonDataDTO;
import org.openhab.binding.sonnen.internal.communication.SonnenJsonPowerMeterDataDTO;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonnenHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Christian Feininger - Initial contribution
 */
@NonNullByDefault
public class SonnenHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SonnenHandler.class);

    private SonnenConfiguration config = new SonnenConfiguration();

    private @Nullable ScheduledFuture<?> refreshJob;

    private SonnenJSONCommunication serviceCommunication;

    private boolean automaticRefreshing = false;

    private boolean sonnenAPIV2 = false;

    private Map<String, Boolean> linkedChannels = new HashMap<>();

    private int chargeRate = 0;
    private int dischargeRate = 0;

    public SonnenHandler(Thing thing) {
        super(thing);
        serviceCommunication = new SonnenJSONCommunication();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing sonnen handler for thing {}", getThing().getUID());
        config = getConfigAs(SonnenConfiguration.class);
        if (config.refreshInterval < 0 || config.refreshInterval > 1000) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Parameter 'refresh Rate' must be in the range 0-1000.");
            return;
        }
        if (config.hostIP.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "IP Address must be configured!");
            return;
        }

        if (!config.authToken.isEmpty()) {
            sonnenAPIV2 = true;
        }
        if (config.chargingPower != -1) {
            chargeRate = config.chargingPower;
        }

        serviceCommunication.setConfig(config);
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.submit(() -> {
            if (updateBatteryData()) {
                for (Channel channel : getThing().getChannels()) {
                    if (isLinked(channel.getUID().getId())) {
                        channelLinked(channel.getUID());
                    }
                }
            }
        });
    }

    /**
     * Calls the service to update the battery data
     *
     * @return true if the update succeeded, false otherwise
     */
    private boolean updateBatteryData() {
        String error = "";
        if (sonnenAPIV2) {
            error = serviceCommunication.refreshBatteryConnectionAPICALLV2(arePowerMeterChannelsLinked());
        } else {
            error = serviceCommunication.refreshBatteryConnectionAPICALLV1();
        }
        if (error.isEmpty()) {
            if (ThingStatus.OFFLINE.equals(getThing().getStatus())) {
                updateStatus(ThingStatus.UNKNOWN);
            }
            if (!ThingStatus.ONLINE.equals(getThing().getStatus())) {
                updateStatus(ThingStatus.ONLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
            return false;
        }
        return error.isEmpty();
    }

    private void verifyLinkedChannel(String channelID) {
        if (isLinked(channelID) && !linkedChannels.containsKey(channelID)) {
            linkedChannels.put(channelID, true);
        }
    }

    @Override
    public void dispose() {
        stopAutomaticRefresh();
        linkedChannels.clear();
        automaticRefreshing = false;
    }

    private void stopAutomaticRefresh() {
        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
        }
        refreshJob = null;
    }

    /**
     * Start the job refreshing the battery status
     */
    private void startAutomaticRefresh() {
        ScheduledFuture<?> job = refreshJob;
        if (job == null || job.isCancelled()) {
            refreshJob = scheduler.scheduleWithFixedDelay(this::refreshChannels, 0, config.refreshInterval,
                    TimeUnit.SECONDS);
        }
    }

    private void refreshChannels() {
        if (updateBatteryData()) {
            for (Channel channel : getThing().getChannels()) {
                updateChannel(channel.getUID().getId(), null);
            }
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        if (!automaticRefreshing) {
            logger.debug("Start automatic refreshing");
            startAutomaticRefresh();
            automaticRefreshing = true;
        }
        verifyLinkedChannel(channelUID.getId());
        updateChannel(channelUID.getId(), null);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        linkedChannels.remove(channelUID.getId());
        if (linkedChannels.isEmpty()) {
            automaticRefreshing = false;
            stopAutomaticRefresh();
            logger.debug("Stop automatic refreshing");
        }
    }

    private void updateChannel(String channelId, @Nullable String putData) {
        if (isLinked(channelId)) {
            State state = null;
            SonnenJsonDataDTO data = serviceCommunication.getBatteryData();
            // The sonnen API has two sub-channels, e.g. 4_1 and 4_2, one representing consumption and the
            // other production. E.g. 4_1.kwh_imported represents the total production since the
            // battery was installed.
            SonnenJsonPowerMeterDataDTO[] dataPM = null;
            if (arePowerMeterChannelsLinked()) {
                dataPM = serviceCommunication.getPowerMeterData();
            }

            if (dataPM != null && dataPM.length >= 2) {
                switch (channelId) {
                    case CHANNEL_ENERGY_IMPORTED_STATE_PRODUCTION:
                        state = new QuantityType<>(dataPM[0].getKwhImported(), Units.KILOWATT_HOUR);
                        update(state, channelId);
                        break;
                    case CHANNEL_ENERGY_EXPORTED_STATE_PRODUCTION:
                        state = new QuantityType<>(dataPM[0].getKwhExported(), Units.KILOWATT_HOUR);
                        update(state, channelId);
                        break;
                    case CHANNEL_ENERGY_IMPORTED_STATE_CONSUMPTION:
                        state = new QuantityType<>(dataPM[1].getKwhImported(), Units.KILOWATT_HOUR);
                        update(state, channelId);
                        break;
                    case CHANNEL_ENERGY_EXPORTED_STATE_CONSUMPTION:
                        state = new QuantityType<>(dataPM[1].getKwhExported(), Units.KILOWATT_HOUR);
                        update(state, channelId);
                        break;
                }
            }

            if (data != null) {
                switch (channelId) {
                    case CHANNEL_BATTERY_DISCHARGING_STATE:
                        update(OnOffType.from(data.isBatteryDischarging()), channelId);
                        break;
                    case CHANNEL_BATTERY_CHARGING_STATE:
                        update(OnOffType.from(data.isBatteryCharging()), channelId);
                        break;
                    case CHANNEL_CONSUMPTION:
                        state = new QuantityType<>(data.getConsumptionHouse(), Units.WATT);
                        update(state, channelId);
                        break;
                    case CHANNEL_BATTERY_DISCHARGING:
                        state = new QuantityType<>(data.getbatteryCurrent() > 0 ? data.getbatteryCurrent() : 0,
                                Units.WATT);
                        update(state, channelId);
                        break;
                    case CHANNEL_BATTERY_CHARGING:
                        state = new QuantityType<>(data.getbatteryCurrent() <= 0 ? (data.getbatteryCurrent() * -1) : 0,
                                Units.WATT);
                        update(state, channelId);
                        break;
                    case CHANNEL_GRID_FEED_IN:
                        state = new QuantityType<>(data.getGridValue() > 0 ? data.getGridValue() : 0, Units.WATT);
                        update(state, channelId);
                        break;
                    case CHANNEL_GRID_CONSUMPTION:
                        state = new QuantityType<>(data.getGridValue() <= 0 ? (data.getGridValue() * -1) : 0,
                                Units.WATT);
                        update(state, channelId);
                        break;
                    case CHANNEL_SOLAR_PRODUCTION:
                        state = new QuantityType<>(data.getSolarProduction(), Units.WATT);
                        update(state, channelId);
                        break;
                    case CHANNEL_BATTERY_LEVEL:
                        state = new QuantityType<>(data.getBatteryChargingLevel(), Units.PERCENT);
                        update(state, channelId);
                        break;
                    case CHANNEL_FLOW_CONSUMPTION_BATTERY_STATE:
                        update(OnOffType.from(data.isFlowConsumptionBattery()), channelId);
                        break;
                    case CHANNEL_FLOW_CONSUMPTION_GRID_STATE:
                        update(OnOffType.from(data.isFlowConsumptionGrid()), channelId);
                        break;
                    case CHANNEL_FLOW_CONSUMPTION_PRODUCTION_STATE:
                        update(OnOffType.from(data.isFlowConsumptionProduction()), channelId);
                        break;
                    case CHANNEL_FLOW_GRID_BATTERY_STATE:
                        update(OnOffType.from(data.isFlowGridBattery()), channelId);
                        break;
                    case CHANNEL_FLOW_PRODUCTION_BATTERY_STATE:
                        update(OnOffType.from(data.isFlowProductionBattery()), channelId);
                        break;
                    case CHANNEL_FLOW_PRODUCTION_GRID_STATE:
                        update(OnOffType.from(data.isFlowProductionGrid()), channelId);
                        break;
                    case CHANNEL_BATTERY_CHARGING_GRID:
                        if (putData != null) {
                            String result = serviceCommunication.startStopBatteryCharging(putData, chargeRate);
                            if (!result.isEmpty()) {
                                // put it to true as switch was turned on if it goes into manual mode
                                if (putData.contains("1")) {
                                    update(OnOffType.from(true), channelId);
                                } else if (putData.contains("2")) {
                                    update(OnOffType.from(false), channelId);
                                }
                            }
                        } else {
                            // Reflect the status of operation mode in the switch
                            update(OnOffType.from(!data.isInAutomaticMode()), channelId);
                        }
                        break;
                    case CHANNEL_BATTERY_DISCHARGING_GRID:
                        if (putData != null) {
                            String result = serviceCommunication.startStopBatteryDischarging(putData, dischargeRate);
                            if (!result.isEmpty()) {
                                // put it to true as switch was turned on if it goes into manual mode
                                if (putData.contains("1")) {
                                    update(OnOffType.from(true), channelId);
                                } else if (putData.contains("2")) {
                                    update(OnOffType.from(false), channelId);
                                }
                            }
                        } else {
                            // Reflect the status of operation mode in the switch
                            update(OnOffType.from(!data.isInAutomaticMode()), channelId);
                        }
                        break;
                    case CHANNEL_BATTERY_OPERATION_MODE:
                        if (!data.isInAutomaticMode()) {
                            state = new StringType("Manual");
                        } else if (data.isInAutomaticMode()) {
                            state = new StringType("Automatic");
                        }
                        update(state, channelId);
                        break;
                }
            }
        } else {
            update(null, channelId);
        }
    }

    private boolean arePowerMeterChannelsLinked() {
        if (isLinked(CHANNEL_ENERGY_IMPORTED_STATE_PRODUCTION)) {
            return true;
        } else if (isLinked(CHANNEL_ENERGY_EXPORTED_STATE_PRODUCTION)) {
            return true;
        } else if (isLinked(CHANNEL_ENERGY_IMPORTED_STATE_CONSUMPTION)) {
            return true;
        } else if (isLinked(CHANNEL_ENERGY_EXPORTED_STATE_CONSUMPTION)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Updates the State of the given channel
     *
     * @param state Given state
     * @param channelId the refereed channelID
     */
    private void update(@Nullable State state, String channelId) {
        logger.debug("Update channel {} with state {}", channelId, (state == null) ? "null" : state.toString());
        updateState(channelId, state != null ? state : UnDefType.UNDEF);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            if (updateBatteryData()) {
                updateChannel(channelUID.getId(), null);
            }
        }
        if (channelUID.getId().equals(CHANNEL_BATTERY_CHARGING_GRID)) {
            String putData = null;
            if (command.equals(OnOffType.ON)) {
                // Set battery to manual mode with 1
                putData = "EM_OperatingMode=1";
            } else if (command.equals(OnOffType.OFF)) {
                // set battery to automatic mode with 2
                putData = "EM_OperatingMode=2";
            }
            if (putData != null) {
                logger.debug("Executing {} command, putData = {}", CHANNEL_BATTERY_CHARGING_GRID, putData);
                updateChannel(channelUID.getId(), putData);
            }
        }
        if (channelUID.getId().equals(CHANNEL_BATTERY_DISCHARGING_GRID)) {
            String putData = null;
            if (command.equals(OnOffType.ON)) {
                // Set battery to manual mode with 1
                putData = "EM_OperatingMode=1";
            } else if (command.equals(OnOffType.OFF)) {
                // set battery to automatic mode with 2
                putData = "EM_OperatingMode=2";
            }
            if (putData != null) {
                logger.debug("Executing {} command, putData = {}", CHANNEL_BATTERY_DISCHARGING_GRID, putData);
                updateChannel(channelUID.getId(), putData);
            }
        }
        if (channelUID.getId().equals(CHANNEL_BATTERY_CHARGE_RATE)) {
            if (command instanceof QuantityType<?> quantityCommand) {
                QuantityType<?> powerInWatt = quantityCommand.toUnit(Units.WATT);
                if (powerInWatt != null) {
                    chargeRate = powerInWatt.intValue();
                    serviceCommunication.startStopBatteryCharging(null, chargeRate);
                    updateState(channelUID, powerInWatt);
                } else {
                    logger.debug("Unable to convert {} command {} to {}", CHANNEL_BATTERY_CHARGE_RATE, quantityCommand,
                            Units.WATT);
                }
            }
        }
        if (channelUID.getId().equals(CHANNEL_BATTERY_DISCHARGE_RATE)) {
            if (command instanceof QuantityType<?> quantityCommand) {
                QuantityType<?> powerInWatt = quantityCommand.toUnit(Units.WATT);
                if (powerInWatt != null) {
                    dischargeRate = powerInWatt.intValue();
                    serviceCommunication.startStopBatteryDischarging(null, dischargeRate);
                    updateState(channelUID, powerInWatt);
                } else {
                    logger.debug("Unable to convert {} command {} to {}", CHANNEL_BATTERY_DISCHARGE_RATE,
                            quantityCommand, Units.WATT);
                }
            }
        }
    }
}
