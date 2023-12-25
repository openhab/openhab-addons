/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.evcc.internal;

import static org.openhab.binding.evcc.internal.EvccBindingConstants.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Triple;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evcc.internal.api.EvccAPI;
import org.openhab.binding.evcc.internal.api.EvccApiException;
import org.openhab.binding.evcc.internal.api.dto.Loadpoint;
import org.openhab.binding.evcc.internal.api.dto.Plan;
import org.openhab.binding.evcc.internal.api.dto.Result;
import org.openhab.binding.evcc.internal.api.dto.Vehicle;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EvccHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Florian Hotze - Initial contribution
 * @author Luca Arnecke - update to evcc version 0.123.1
 */
@NonNullByDefault
public class EvccHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(EvccHandler.class);
    private @Nullable EvccAPI evccAPI;
    private @Nullable ScheduledFuture<?> statePollingJob;

    private @Nullable Result result;

    private boolean batteryConfigured = false;
    private boolean gridConfigured = false;
    private boolean pvConfigured = false;
    Map<String, Triple<Boolean, Float, ZonedDateTime>> vehiclePlans = new HashMap<>();

    public EvccHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command.equals(RefreshType.REFRESH)) {
            refresh();
        } else {
            logger.debug("Handling command {} ({}) for channel {}", command, command.getClass(), channelUID);
            String groupId = channelUID.getGroupId();
            if (groupId == null) {
                return;
            }
            String channelGroupId = channelUID.getGroupId();
            String channelIdWithoutGroup = channelUID.getIdWithoutGroup();
            EvccAPI evccAPI = this.evccAPI;
            if (evccAPI == null) {
                return;
            }
            try {
                if (channelGroupId.equals(CHANNEL_GROUP_ID_GENERAL)) {
                    if (!channelIdWithoutGroup.equals(CHANNEL_BATTERY_PRIORITY_SOC)) {
                        return;
                    }
                    if (command instanceof QuantityType<?> qt) {
                        evccAPI.setBatteryPrioritySoC(qt.toUnit(Units.PERCENT).intValue());
                    } else if (command instanceof DecimalType dt) {
                        evccAPI.setBatteryPrioritySoC(dt.intValue());
                    } else {
                        logger.debug("Command has wrong type, QuantityType or DecimalType required!");
                    }
                } else if (channelGroupId.matches(CHANNEL_GROUP_ID_LOADPOINT)) {
                    int loadpoint = Integer.parseInt(groupId.substring(9)) + 1;
                    switch (channelIdWithoutGroup) {
                        case CHANNEL_LOADPOINT_MODE -> {
                            if (command instanceof StringType) {
                                evccAPI.setMode(loadpoint, command.toString());
                            } else {
                                logger.debug("Command has wrong type, StringType required!");
                            }
                        }
                        case CHANNEL_LOADPOINT_LIMIT_ENERGY -> {
                            if (command instanceof QuantityType<?> qt) {
                                evccAPI.setLimitEnergy(loadpoint, qt.toUnit(Units.WATT_HOUR).floatValue());
                            } else {
                                logger.debug("Command has wrong type, QuantityType required!");
                            }
                        }
                        case CHANNEL_LOADPOINT_LIMIT_SOC -> {
                            if (command instanceof QuantityType<?> qt) {
                                evccAPI.setLimitSoC(loadpoint, qt.toUnit(Units.PERCENT).intValue());
                            } else if (command instanceof DecimalType dt) {
                                evccAPI.setLimitSoC(loadpoint, dt.intValue());
                            } else {
                                logger.debug("Command has wrong type, QuantityType or DecimalType required!");
                            }
                        }
                        case CHANNEL_LOADPOINT_PHASES -> {
                            if (command instanceof DecimalType dt) {
                                evccAPI.setPhases(loadpoint, dt.intValue());
                            } else {
                                logger.debug("Command has wrong type, DecimalType required!");
                            }
                        }
                        case CHANNEL_LOADPOINT_MIN_CURRENT -> {
                            if (command instanceof QuantityType<?> qt) {
                                evccAPI.setMinCurrent(loadpoint, qt.toUnit(Units.AMPERE).intValue());
                            } else if (command instanceof DecimalType dt) {
                                evccAPI.setMinCurrent(loadpoint, dt.intValue());
                            } else {
                                logger.debug("Command has wrong type, QuantityType or DecimalType required!");
                            }
                        }
                        case CHANNEL_LOADPOINT_MAX_CURRENT -> {
                            if (command instanceof QuantityType<?> qt) {
                                evccAPI.setMaxCurrent(loadpoint, qt.toUnit(Units.AMPERE).intValue());
                            } else if (command instanceof DecimalType dt) {
                                evccAPI.setMaxCurrent(loadpoint, dt.intValue());
                            } else {
                                logger.debug("Command has wrong type, QuantityType or DecimalType required!");
                            }
                        }
                        default -> {
                            return;
                        }
                    }
                } else {
                    String vehicle = groupId.substring(7);
                    switch (channelIdWithoutGroup) {
                        case CHANNEL_VEHICLE_MIN_SOC -> {
                            if (command instanceof QuantityType<?> qt) {
                                evccAPI.setVehicleMinSoC(vehicle, qt.toUnit(Units.PERCENT).intValue());
                            } else if (command instanceof DecimalType dt) {
                                evccAPI.setVehicleMinSoC(vehicle, dt.intValue());
                            } else {
                                logger.debug("Command has wrong type, QuantityType or DecimalType required!");
                            }
                        }
                        case CHANNEL_VEHICLE_LIMIT_SOC -> {
                            if (command instanceof QuantityType<?> qt) {
                                evccAPI.setVehicleLimitSoC(vehicle, qt.toUnit(Units.PERCENT).intValue());
                            } else if (command instanceof DecimalType dt) {
                                evccAPI.setVehicleLimitSoC(vehicle, dt.intValue());
                            } else {
                                logger.debug("Command has wrong type, QuantityType or DecimalType required!");
                            }
                        }
                        case CHANNEL_VEHICLE_PLAN_ENABLED -> {
                            Triple<Boolean, Float, ZonedDateTime> planValues = vehiclePlans.get(vehicle);
                            if (command == OnOffType.ON) {
                                evccAPI.setVehiclePlan(vehicle, planValues.getMiddle().intValue(),
                                        planValues.getRight());
                                vehiclePlans.put(vehicle,
                                        Triple.of(true, planValues.getMiddle(), planValues.getRight()));
                            } else if (command == OnOffType.OFF) {
                                evccAPI.removeVehiclePlan(vehicle);
                                vehiclePlans.put(vehicle,
                                        Triple.of(false, planValues.getMiddle(), planValues.getRight()));
                            } else {
                                logger.debug("Command has wrong type, OnOffType required!");
                            }
                            updateChannelsVehicle(vehicle);
                        }
                        case CHANNEL_VEHICLE_PLAN_SOC -> {
                            Triple<Boolean, Float, ZonedDateTime> planValues = vehiclePlans.get(vehicle);
                            if (command instanceof QuantityType<?> qt) {
                                vehiclePlans.put(vehicle, Triple.of(planValues.getLeft(),
                                        qt.toUnit(Units.PERCENT).floatValue(), planValues.getRight()));
                                if (planValues.getLeft()) {
                                    evccAPI.setVehiclePlan(vehicle, qt.toUnit(Units.PERCENT).intValue(),
                                            planValues.getRight());
                                }
                            } else if (command instanceof DecimalType dt) {
                                vehiclePlans.put(vehicle,
                                        Triple.of(planValues.getLeft(), dt.floatValue(), planValues.getRight()));
                                if (planValues.getLeft()) {
                                    evccAPI.setVehiclePlan(vehicle, dt.intValue(), planValues.getRight());
                                }
                            } else {
                                logger.debug("Command has wrong type, QuantityType or DecimalType required!");
                            }
                            updateChannelsVehicle(vehicle);
                        }
                        case CHANNEL_VEHICLE_PLAN_TIME -> {
                            Triple<Boolean, Float, ZonedDateTime> planValues = vehiclePlans.get(vehicle);
                            if (command instanceof DateTimeType dtt) {
                                vehiclePlans.put(vehicle, Triple.of(planValues.getLeft(), planValues.getMiddle(),
                                        dtt.getZonedDateTime()));
                                if (planValues.getLeft()) {
                                    try {
                                        evccAPI.setVehiclePlan(vehicle, planValues.getMiddle().intValue(),
                                                dtt.getZonedDateTime());
                                    } catch (DateTimeParseException e) {
                                        logger.debug("Failed to set vehicle plan time: ", e);
                                    }
                                }
                            } else {
                                logger.debug("Command has wrong type, DateTimeType required!");
                            }
                            updateChannelsVehicle(vehicle);
                        }
                    }
                }
            } catch (EvccApiException e) {
                Throwable cause = e.getCause();
                if (cause == null) {
                    logger.debug("Failed to handle command {} for channel {}: {}", command, channelUID, e.getMessage());
                } else {
                    logger.debug("Failed to handle command {} for channel {}: {} -> {}", command, channelUID,
                            e.getMessage(), cause.getMessage());
                }
            }
            refresh();
        }
    }

    @Override
    public void initialize() {
        EvccConfiguration config = getConfigAs(EvccConfiguration.class);
        String url = config.url;
        if (url == null || url.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.no-host");
        } else {
            this.evccAPI = new EvccAPI(url);
            logger.debug("Setting up refresh job ...");
            statePollingJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, config.refreshInterval,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Refreshes from evcc.
     *
     * First, checks connection and updates Thing status.
     * Second, creates all available channels.
     * Third, updates all channels.
     */
    private void refresh() {
        logger.debug("Running refresh job ...");
        EvccAPI evccAPI;
        evccAPI = this.evccAPI;
        if (evccAPI == null) {
            return;
        }
        try {
            this.result = evccAPI.getResult();
        } catch (EvccApiException e) {
            logger.debug("Failed to get state: ", e);
        }
        Result result = this.result;
        if (result == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "@text/offline.communication-error.request-failed");
        } else {
            String sitename = result.getSiteTitle();
            int numberOfLoadpoints = result.getLoadpoints().length;
            logger.debug("Found {} loadpoints on site {}.", numberOfLoadpoints, sitename);
            Map<String, Vehicle> vehicles = result.getVehicles();
            logger.debug("Found {} vehicles on site {}.", vehicles.size(), sitename);
            updateStatus(ThingStatus.ONLINE);
            batteryConfigured = result.getBatteryConfigured();
            gridConfigured = result.getGridConfigured();
            pvConfigured = result.getPvConfigured();
            createChannelsGeneral();
            updateChannelsGeneral();
            for (int i = 0; i < numberOfLoadpoints; i++) {
                createChannelsLoadpoint(i);
                updateChannelsLoadpoint(i);
            }
            for (String vehicleId : vehicles.keySet()) {
                createChannelsVehicle(vehicleId);
                updateChannelsVehicle(vehicleId);
            }
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> statePollingJob = this.statePollingJob;
        if (statePollingJob != null) {
            statePollingJob.cancel(true);
            this.statePollingJob = null;
        }
    }

    // Utility functions
    private void createChannelsGeneral() {
        if (batteryConfigured) {
            createChannel(CHANNEL_BATTERY_CAPACITY, CHANNEL_GROUP_ID_GENERAL, CHANNEL_TYPE_UID_BATTERY_CAPACITY,
                    "Number:Energy");
            createChannel(CHANNEL_BATTERY_POWER, CHANNEL_GROUP_ID_GENERAL, CHANNEL_TYPE_UID_BATTERY_POWER,
                    "Number:Power");
            createChannel(CHANNEL_BATTERY_SOC, CHANNEL_GROUP_ID_GENERAL, CHANNEL_TYPE_UID_BATTERY_SOC,
                    "Number:Dimensionless");
            createChannel(CHANNEL_BATTERY_PRIORITY_SOC, CHANNEL_GROUP_ID_GENERAL, CHANNEL_TYPE_UID_BATTERY_PRIORITY_SOC,
                    "Number:Dimensionless");
        }
        if (gridConfigured) {
            createChannel(CHANNEL_GRID_POWER, CHANNEL_GROUP_ID_GENERAL, CHANNEL_TYPE_UID_GRID_POWER, "Number:Power");
        }
        createChannel(CHANNEL_HOME_POWER, CHANNEL_GROUP_ID_GENERAL, CHANNEL_TYPE_UID_HOME_POWER, "Number:Power");
        if (pvConfigured) {
            createChannel(CHANNEL_PV_POWER, CHANNEL_GROUP_ID_GENERAL, CHANNEL_TYPE_UID_PV_POWER, "Number:Power");
        }
    }

    private void createChannelsLoadpoint(int loadpointId) {
        final String channelGroup = "loadpoint" + loadpointId;
        createChannel(CHANNEL_LOADPOINT_ACTIVE_PHASES, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_ACTIVE_PHASES,
                CoreItemFactory.NUMBER);
        createChannel(CHANNEL_LOADPOINT_CHARGE_CURRENT, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_CHARGE_CURRENT,
                "Number:ElectricCurrent");
        createChannel(CHANNEL_LOADPOINT_CHARGE_DURATION, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_CHARGE_DURATION,
                "Number:Time");
        createChannel(CHANNEL_LOADPOINT_CHARGE_POWER, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_CHARGE_POWER,
                "Number:Power");
        createChannel(CHANNEL_LOADPOINT_CHARGE_REMAINING_DURATION, channelGroup,
                CHANNEL_TYPE_UID_LOADPOINT_CHARGE_REMAINING_DURATION, "Number:Time");
        createChannel(CHANNEL_LOADPOINT_CHARGE_REMAINING_ENERGY, channelGroup,
                CHANNEL_TYPE_UID_LOADPOINT_CHARGE_REMAINING_ENERGY, "Number:Energy");
        createChannel(CHANNEL_LOADPOINT_CHARGED_ENERGY, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_CHARGED_ENERGY,
                "Number:Energy");
        createChannel(CHANNEL_LOADPOINT_CHARGING, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_CHARGING,
                CoreItemFactory.SWITCH);
        createChannel(CHANNEL_LOADPOINT_CONNECTED, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_CONNECTED,
                CoreItemFactory.SWITCH);
        createChannel(CHANNEL_LOADPOINT_CONNECTED_DURATION, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_CONNECTED_DURATION,
                "Number:Time");
        createChannel(CHANNEL_LOADPOINT_ENABLED, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_ENABLED,
                CoreItemFactory.SWITCH);
        createChannel(CHANNEL_LOADPOINT_MAX_CURRENT, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_MAX_CURRENT,
                "Number:ElectricCurrent");
        createChannel(CHANNEL_LOADPOINT_MIN_CURRENT, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_MIN_CURRENT,
                "Number:ElectricCurrent");
        createChannel(CHANNEL_LOADPOINT_MODE, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_MODE, CoreItemFactory.STRING);
        createChannel(CHANNEL_LOADPOINT_PHASES, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_PHASES,
                CoreItemFactory.NUMBER);
        createChannel(CHANNEL_LOADPOINT_LIMIT_ENERGY, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_LIMIT_ENERGY,
                "Number:Energy");
        createChannel(CHANNEL_LOADPOINT_LIMIT_SOC, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_LIMIT_SOC,
                "Number:Dimensionless");
        createChannel(CHANNEL_LOADPOINT_TITLE, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_TITLE, CoreItemFactory.STRING);
        createChannel(CHANNEL_LOADPOINT_VEHICLE_CAPACITY, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_CAPACITY,
                "Number:Energy");
        createChannel(CHANNEL_LOADPOINT_VEHICLE_ODOMETER, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_ODOMETER,
                "Number:Length");
        createChannel(CHANNEL_LOADPOINT_VEHICLE_PRESENT, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_PRESENT,
                CoreItemFactory.SWITCH);
        createChannel(CHANNEL_LOADPOINT_VEHICLE_RANGE, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_RANGE,
                "Number:Length");
        createChannel(CHANNEL_LOADPOINT_VEHICLE_SOC, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_SOC,
                "Number:Dimensionless");
        createChannel(CHANNEL_LOADPOINT_VEHICLE_TITLE, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_TITLE,
                CoreItemFactory.STRING);

        removeChannel(CHANNEL_LOADPOINT_HAS_VEHICLE, channelGroup);
    }

    private void createChannelsVehicle(String vehicleId) {
        final String channelGroup = "vehicle" + vehicleId;
        createChannel(CHANNEL_VEHICLE_TITLE, channelGroup, CHANNEL_TYPE_UID_VEHICLE_TITLE, CoreItemFactory.STRING);
        createChannel(CHANNEL_VEHICLE_MIN_SOC, channelGroup, CHANNEL_TYPE_UID_VEHICLE_MIN_SOC, "Number:Dimensionless");
        createChannel(CHANNEL_VEHICLE_LIMIT_SOC, channelGroup, CHANNEL_TYPE_UID_VEHICLE_LIMIT_SOC,
                "Number:Dimensionless");
        createChannel(CHANNEL_VEHICLE_PLAN_SOC, channelGroup, CHANNEL_TYPE_UID_VEHICLE_PLAN_SOC,
                "Number:Dimensionless");
        createChannel(CHANNEL_VEHICLE_PLAN_TIME, channelGroup, CHANNEL_TYPE_UID_VEHICLE_PLAN_TIME,
                CoreItemFactory.DATETIME);
        createChannel(CHANNEL_VEHICLE_PLAN_ENABLED, channelGroup, CHANNEL_TYPE_UID_VEHICLE_PLAN_ENABLED,
                CoreItemFactory.SWITCH);
        createChannel(CHANNEL_VEHICLE_PLAN_SOC, channelGroup, CHANNEL_TYPE_UID_VEHICLE_PLAN_SOC,
                "Number:Dimensionless");
        createChannel(CHANNEL_VEHICLE_PLAN_TIME, channelGroup, CHANNEL_TYPE_UID_VEHICLE_PLAN_TIME,
                CoreItemFactory.DATETIME);
    }

    // Units and description for vars: https://docs.evcc.io/docs/reference/configuration/messaging/#msg
    private void updateChannelsGeneral() {
        final Result result = this.result;
        if (result == null) {
            return;
        }
        final ThingUID uid = getThing().getUID();
        ChannelUID channel;
        boolean batteryConfigured = this.batteryConfigured;
        if (batteryConfigured) {
            float batteryCapacity = result.getBatteryCapacity();
            channel = new ChannelUID(uid, CHANNEL_GROUP_ID_GENERAL, CHANNEL_BATTERY_CAPACITY);
            updateState(channel, new QuantityType<>(batteryCapacity, Units.KILOWATT_HOUR));

            float batteryPower = result.getBatteryPower();
            channel = new ChannelUID(uid, CHANNEL_GROUP_ID_GENERAL, CHANNEL_BATTERY_POWER);
            updateState(channel, new QuantityType<>(batteryPower, Units.WATT));

            float batterySoC = result.getBatterySoC();
            channel = new ChannelUID(uid, CHANNEL_GROUP_ID_GENERAL, CHANNEL_BATTERY_SOC);
            updateState(channel, new QuantityType<>(batterySoC, Units.PERCENT));

            float batteryPrioritySoC = result.getBatteryPrioritySoC();
            channel = new ChannelUID(uid, CHANNEL_GROUP_ID_GENERAL, CHANNEL_BATTERY_PRIORITY_SOC);
            updateState(channel, new QuantityType<>(batteryPrioritySoC, Units.PERCENT));
        }
        boolean gridConfigured = this.gridConfigured;
        if (gridConfigured) {
            float gridPower = result.getGridPower();
            channel = new ChannelUID(uid, CHANNEL_GROUP_ID_GENERAL, CHANNEL_GRID_POWER);
            updateState(channel, new QuantityType<>(gridPower, Units.WATT));
        }
        float homePower = result.getHomePower();
        channel = new ChannelUID(uid, CHANNEL_GROUP_ID_GENERAL, CHANNEL_HOME_POWER);
        updateState(channel, new QuantityType<>(homePower, Units.WATT));
        boolean pvConfigured = this.pvConfigured;
        if (pvConfigured) {
            float pvPower = result.getPvPower();
            channel = new ChannelUID(uid, CHANNEL_GROUP_ID_GENERAL, CHANNEL_PV_POWER);
            updateState(channel, new QuantityType<>(pvPower, Units.WATT));
        }
    }

    private void updateChannelsLoadpoint(int loadpointId) {
        final Result result = this.result;
        if (result == null) {
            return;
        }
        final ThingUID uid = getThing().getUID();
        final String loadpointName = "loadpoint" + loadpointId;
        ChannelUID channel;
        Loadpoint loadpoint = result.getLoadpoints()[loadpointId];

        int activePhases = loadpoint.getActivePhases();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_ACTIVE_PHASES);
        updateState(channel, new DecimalType(activePhases));

        float chargeCurrent = loadpoint.getChargeCurrent();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_CHARGE_CURRENT);
        updateState(channel, new QuantityType<>(chargeCurrent, Units.AMPERE));

        long chargeDuration = loadpoint.getChargeDuration();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_CHARGE_DURATION);
        updateState(channel, new QuantityType<>(chargeDuration, MetricPrefix.NANO(Units.SECOND)));

        float chargePower = loadpoint.getChargePower();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_CHARGE_POWER);
        updateState(channel, new QuantityType<>(chargePower, Units.WATT));

        long chargeRemainingDuration = loadpoint.getChargeRemainingDuration();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_CHARGE_REMAINING_DURATION);
        updateState(channel, new QuantityType<>(chargeRemainingDuration, MetricPrefix.NANO(Units.SECOND)));

        float chargeRemainingEnergy = loadpoint.getChargeRemainingEnergy();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_CHARGE_REMAINING_ENERGY);
        updateState(channel, new QuantityType<>(chargeRemainingEnergy, Units.WATT_HOUR));

        float chargedEnergy = loadpoint.getChargedEnergy();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_CHARGED_ENERGY);
        updateState(channel, new QuantityType<>(chargedEnergy, Units.WATT_HOUR));

        boolean charging = loadpoint.getCharging();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_CHARGING);
        updateState(channel, OnOffType.from(charging));

        boolean connected = loadpoint.getConnected();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_CONNECTED);
        updateState(channel, OnOffType.from(connected));

        long connectedDuration = loadpoint.getConnectedDuration();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_CONNECTED_DURATION);
        updateState(channel, new QuantityType<>(connectedDuration, MetricPrefix.NANO(Units.SECOND)));

        boolean enabled = loadpoint.getEnabled();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_ENABLED);
        updateState(channel, OnOffType.from(enabled));

        float maxCurrent = loadpoint.getMaxCurrent();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_MAX_CURRENT);
        updateState(channel, new QuantityType<>(maxCurrent, Units.AMPERE));

        float minCurrent = loadpoint.getMinCurrent();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_MIN_CURRENT);
        updateState(channel, new QuantityType<>(minCurrent, Units.AMPERE));

        String mode = loadpoint.getMode();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_MODE);
        updateState(channel, new StringType(mode));

        int phases = loadpoint.getPhases();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_PHASES);
        updateState(channel, new DecimalType(phases));

        float limitEnergy = loadpoint.getLimitEnergy();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_LIMIT_ENERGY);
        updateState(channel, new QuantityType<>(limitEnergy, Units.WATT_HOUR));

        float limitSoC = loadpoint.getLimitSoC();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_LIMIT_SOC);
        updateState(channel, new QuantityType<>(limitSoC, Units.PERCENT));

        String title = loadpoint.getTitle();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_TITLE);
        updateState(channel, new StringType(title));

        float vehicleCapacity = loadpoint.getVehicleCapacity();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_VEHICLE_CAPACITY);
        updateState(channel, new QuantityType<>(vehicleCapacity, Units.KILOWATT_HOUR));

        float vehicleOdometer = loadpoint.getVehicleOdometer();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_VEHICLE_ODOMETER);
        updateState(channel, new QuantityType<>(vehicleOdometer, MetricPrefix.KILO(SIUnits.METRE)));

        boolean vehiclePresent = loadpoint.getVehiclePresent();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_VEHICLE_PRESENT);
        updateState(channel, OnOffType.from(vehiclePresent));

        float vehicleRange = loadpoint.getVehicleRange();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_VEHICLE_RANGE);
        updateState(channel, new QuantityType<>(vehicleRange, MetricPrefix.KILO(SIUnits.METRE)));

        float vehicleSoC = loadpoint.getVehicleSoC();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_VEHICLE_SOC);
        updateState(channel, new QuantityType<>(vehicleSoC, Units.PERCENT));

        String vehicleTitle = loadpoint.getVehicleTitle();
        channel = new ChannelUID(uid, loadpointName, CHANNEL_LOADPOINT_VEHICLE_TITLE);
        updateState(channel, new StringType(vehicleTitle));
    }

    private void updateChannelsVehicle(String vehicleId) {
        final Result result = this.result;
        if (result == null) {
            return;
        }
        final ThingUID uid = getThing().getUID();
        final String vehicleName = "vehicle" + vehicleId;
        ChannelUID channel;
        Vehicle vehicle = result.getVehicles().get(vehicleId);

        String title = vehicle.getTitle();
        channel = new ChannelUID(uid, vehicleName, CHANNEL_VEHICLE_TITLE);
        updateState(channel, new StringType(title));

        float minSoC = vehicle.getMinSoC();
        channel = new ChannelUID(uid, vehicleName, CHANNEL_VEHICLE_MIN_SOC);
        updateState(channel, new QuantityType<>(minSoC, Units.PERCENT));

        float limitSoC = vehicle.getLimitSoC();
        channel = new ChannelUID(uid, vehicleName, CHANNEL_VEHICLE_LIMIT_SOC);
        updateState(channel, new QuantityType<>(limitSoC, Units.PERCENT));

        Plan plan = vehicle.getPlan();
        if (plan == null && vehiclePlans.get(vehicleId) == null) {
            vehiclePlans.put(vehicleId, Triple.of(false, 100f, ZonedDateTime.now().plusHours(12)));
        } else if (plan != null) {
            vehiclePlans.put(vehicleId, Triple.of(true, plan.getSoC(), ZonedDateTime.parse(plan.getTime())));
        }
        updateVehiclePlanChannel(vehicleId, uid, vehicleName, channel);
    }

    private void updateVehiclePlanChannel(String vehicleId, ThingUID uid, String vehicleName, ChannelUID channel) {
        Triple<Boolean, Float, ZonedDateTime> planValues = vehiclePlans.get(vehicleId);

        channel = new ChannelUID(uid, vehicleName, CHANNEL_VEHICLE_PLAN_ENABLED);
        updateState(channel, planValues.getLeft() ? OnOffType.ON : OnOffType.OFF);
        channel = new ChannelUID(uid, vehicleName, CHANNEL_VEHICLE_PLAN_SOC);
        updateState(channel, new QuantityType<>(planValues.getMiddle(), Units.PERCENT));
        channel = new ChannelUID(uid, vehicleName, CHANNEL_VEHICLE_PLAN_TIME);
        updateState(channel, new DateTimeType(planValues.getRight()));
    }

    private void createChannel(String channel, String channelGroupId, ChannelTypeUID channelTypeUID, String itemType) {
        ChannelUID channelUid = new ChannelUID(thing.getUID(), channelGroupId, channel);
        if (thing.getChannel(channelUid) == null) {
            updateThing(editThing()
                    .withChannel(ChannelBuilder.create(channelUid, itemType).withType(channelTypeUID).build()).build());
        }
    }

    private void removeChannel(String channel, String channelGroupId) {
        ChannelUID channelUid = new ChannelUID(thing.getUID(), channelGroupId, channel);
        if (thing.getChannel(channelUid) != null) {
            updateThing(editThing().withoutChannel(channelUid).build());
        }
    }
}
