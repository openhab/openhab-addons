/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evcc.internal.dto.Loadpoint;
import org.openhab.binding.evcc.internal.dto.Result;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
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
 */
@NonNullByDefault
public class EvccHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(EvccHandler.class);
    private @Nullable EvccAPI evccAPI;
    private @Nullable ScheduledFuture<?> statePollingJob;

    private @Nullable EvccConfiguration config;

    private @Nullable Result result;

    private int numberOfLoadpoints = 0;
    private @Nullable String sitename;
    private boolean batteryConfigured = false;
    private double batteryPower = 0;
    private int batterySoC = 0;
    private int batteryPrioritySoC = 0;
    private boolean gridConfigured = false;
    private double gridPower = 0;
    private double homePower = 0;
    private boolean pvConfigured = false;
    private double pvPower = 0;

    private int activePhases = 3;
    private double chargeCurrent = 0;
    private double chargeDuration = 0;
    private double chargePower = 0;
    private double chargeRemainingDuration = 0;
    private double chargeRemainingEnergy = 0;
    private double chargedEnergy = 0;
    private boolean charging = false;
    private boolean connected = false;
    private double connectedDuration = 0;
    private boolean enabled = false;
    private boolean hasVehicle = false;
    private double maxCurrent = 16;
    private double minCurrent = 0;
    private int minSoC = 0;
    private String mode = "off";
    private int phases = 3;
    private int targetSoC = 100;
    private @Nullable String targetTime = "null";
    private boolean targetTimeEnabled = false;
    private ZonedDateTime targetTimeZDT = ZonedDateTime.now();
    private String title = "";
    private double vehicleCapacity = 0;
    private double vehicleOdometer = 0;
    private boolean vehiclePresent = false;
    private double vehicleRange = 0;
    private int vehicleSoC = 0;
    private String vehicleTitle = "";

    public EvccHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command.equals(RefreshType.REFRESH)) {
            refresh();
        } else {
            logger.debug("Handling command {} for channel {}", command, channelUID);
            String groupId = channelUID.getGroupId();
            if (groupId == null)
                return;
            String channelIdWithoutGroup = channelUID.getIdWithoutGroup();
            int loadpoint = Integer.parseInt(groupId.toString().substring(9));
            EvccAPI evccAPI = this.evccAPI;
            if (evccAPI == null)
                return;
            switch (channelIdWithoutGroup) {
                case CHANNEL_LOADPOINT_MODE:
                    evccAPI.setMode(loadpoint, command.toString());
                    break;
                case CHANNEL_LOADPOINT_MIN_SOC:
                    evccAPI.setMinSoC(loadpoint, Integer.parseInt(command.toString().replaceAll(" %", "")));
                    break;
                case CHANNEL_LOADPOINT_TARGET_SOC:
                    evccAPI.setTargetSoC(loadpoint, Integer.parseInt(command.toString().replaceAll(" %", "")));
                    break;
                case CHANNEL_LOADPOINT_TARGET_TIME:
                    if (targetTimeEnabled == true) {
                        targetTimeZDT = new DateTimeType(command.toString()).getZonedDateTime();
                        evccAPI.setTargetCharge(loadpoint, targetSoC, targetTimeZDT);
                        ChannelUID channel = new ChannelUID(getThing().getUID(), "loadpoint" + loadpoint,
                                CHANNEL_LOADPOINT_TARGET_TIME);
                        updateState(channel, new DateTimeType(targetTimeZDT));
                    }
                    break;
                case CHANNEL_LOADPOINT_TARGET_TIME_ENABLED:
                    if (command == OnOffType.ON) {
                        targetTimeEnabled = true;
                        evccAPI.setTargetCharge(loadpoint, targetSoC, targetTimeZDT);
                    } else {
                        targetTimeEnabled = false;
                        evccAPI.unsetTargetCharge(loadpoint);
                    }
                    break;
                case CHANNEL_LOADPOINT_PHASES:
                    evccAPI.setPhases(loadpoint, Integer.parseInt(command.toString()));
                    break;
                case CHANNEL_LOADPOINT_MIN_CURRENT:
                    evccAPI.setMinCurrent(loadpoint, Integer.parseInt(command.toString().replaceAll(" A", "")));
                    break;
                case CHANNEL_LOADPOINT_MAX_CURRENT:
                    evccAPI.setMaxCurrent(loadpoint, Integer.parseInt(command.toString().replaceAll(" A", "")));
                    break;
                default:
                    return;
            }
            refresh();
        }
    }

    @Override
    public void initialize() {
        this.config = getConfigAs(EvccConfiguration.class);
        EvccConfiguration config = this.config;
        if (config != null) {
            if (config.url == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                        "@text/offline.configuration-error.no-host");
            } else {
                this.evccAPI = new EvccAPI(config.url);
                logger.debug("Setting up refresh job ...");
                statePollingJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, 15, TimeUnit.SECONDS);
            }
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
        EvccConfiguration config = this.config;
        if (config == null)
            return;
        if (config.url == null)
            return;
        EvccAPI evccAPI = this.evccAPI;
        if (evccAPI == null)
            return;
        this.result = evccAPI.getResult();
        Result result = this.result;
        if (result == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "@text/offline.communication-error.request-failed");
        } else {
            this.sitename = result.getSiteTitle();
            this.numberOfLoadpoints = result.getLoadpoints().length;
            logger.debug("Found {} loadpoints on site {} (host: {}).", this.numberOfLoadpoints, this.sitename,
                    config.url);
            updateStatus(ThingStatus.ONLINE);
            batteryConfigured = result.getBatteryConfigured();
            gridConfigured = result.getGridConfigured();
            pvConfigured = result.getPvConfigured();
            createChannelsGeneral();
            updateChannelsGeneral();
            for (int i = 0; i < this.numberOfLoadpoints; i++) {
                createChannelsLoadpoint(i);
                updateChannelsLoadpoint(i);
            }
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> statePollingJob = this.statePollingJob;
        if (statePollingJob != null) {
            statePollingJob.cancel(true);
            statePollingJob = null;
        }
    }

    // Utility functions
    private void createChannelsGeneral() {
        if (batteryConfigured == true) {
            createChannel(CHANNEL_BATTERY_POWER, "general", CHANNEL_TYPE_UID_BATTERY_POWER, "Number:Power");
            createChannel(CHANNEL_BATTERY_SOC, "general", CHANNEL_TYPE_UID_BATTERY_SOC, "Number:Dimensionless");
            createChannel(CHANNEL_BATTERY_PRIORITY_SOC, "general", CHANNEL_TYPE_UID_BATTERY_PRIORITY_SOC,
                    "Number:Dimensionless");
        }
        if (gridConfigured == true) {
            createChannel(CHANNEL_GRID_POWER, "general", CHANNEL_TYPE_UID_GRID_POWER, "Number:Power");
        }
        createChannel(CHANNEL_HOME_POWER, "general", CHANNEL_TYPE_UID_HOME_POWER, "Number:Power");
        if (pvConfigured == true) {
            createChannel(CHANNEL_PV_POWER, "general", CHANNEL_TYPE_UID_PV_POWER, "Number:Power");
        }
    }

    private void createChannelsLoadpoint(int loadpointId) {
        String loadpointName = "loadpoint" + loadpointId;
        createChannel(CHANNEL_LOADPOINT_ACTIVE_PHASES, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_ACTIVE_PHASES,
                "Number");
        createChannel(CHANNEL_LOADPOINT_CHARGE_CURRENT, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_CHARGE_CURRENT,
                "Number:ElectricCurrent");
        createChannel(CHANNEL_LOADPOINT_CHARGE_DURATION, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_CHARGE_DURATION,
                "Number:Time");
        createChannel(CHANNEL_LOADPOINT_CHARGE_POWER, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_CHARGE_POWER,
                "Number:Power");
        createChannel(CHANNEL_LOADPOINT_CHARGE_REMAINING_DURATION, loadpointName,
                CHANNEL_TYPE_UID_LOADPOINT_CHARGE_REMAINING_DURATION, "Number:Time");
        createChannel(CHANNEL_LOADPOINT_CHARGE_REMAINING_ENERGY, loadpointName,
                CHANNEL_TYPE_UID_LOADPOINT_CHARGE_REMAINING_ENERGY, "Number:Energy");
        createChannel(CHANNEL_LOADPOINT_CHARGED_ENERGY, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_CHARGED_ENERGY,
                "Number:Energy");
        createChannel(CHANNEL_LOADPOINT_CHARGING, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_CHARGING, "Switch");
        createChannel(CHANNEL_LOADPOINT_CONNECTED, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_CONNECTED, "Switch");
        createChannel(CHANNEL_LOADPOINT_CONNECTED_DURATION, loadpointName,
                CHANNEL_TYPE_UID_LOADPOINT_CONNECTED_DURATION, "Number:Time");
        createChannel(CHANNEL_LOADPOINT_ENABLED, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_ENABLED, "Switch");
        createChannel(CHANNEL_LOADPOINT_HAS_VEHICLE, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_HAS_VEHICLE, "Switch");
        createChannel(CHANNEL_LOADPOINT_MAX_CURRENT, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_MAX_CURRENT,
                "Number:ElectricCurrent");
        createChannel(CHANNEL_LOADPOINT_MIN_CURRENT, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_MIN_CURRENT,
                "Number:ElectricCurrent");
        createChannel(CHANNEL_LOADPOINT_MIN_SOC, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_MIN_SOC,
                "Number:Dimensionless");
        createChannel(CHANNEL_LOADPOINT_MODE, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_MODE, "String");
        createChannel(CHANNEL_LOADPOINT_PHASES, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_PHASES, "Number");
        createChannel(CHANNEL_LOADPOINT_TARGET_SOC, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_TARGET_SOC,
                "Number:Dimensionless");
        createChannel(CHANNEL_LOADPOINT_TARGET_TIME, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_TARGET_TIME, "DateTime");
        createChannel(CHANNEL_LOADPOINT_TARGET_TIME_ENABLED, loadpointName,
                CHANNEL_TYPE_UID_LOADPOINT_TARGET_TIME_ENABLED, "Switch");
        createChannel(CHANNEL_LOADPOINT_TITLE, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_TITLE, "String");
        createChannel(CHANNEL_LOADPOINT_VEHICLE_CAPACITY, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_CAPACITY,
                "Number:Energy");
        createChannel(CHANNEL_LOADPOINT_VEHICLE_ODOMETER, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_ODOMETER,
                "Number:Length");
        createChannel(CHANNEL_LOADPOINT_VEHICLE_PRESENT, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_PRESENT,
                "Switch");
        createChannel(CHANNEL_LOADPOINT_VEHICLE_RANGE, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_RANGE,
                "Number:Length");
        createChannel(CHANNEL_LOADPOINT_VEHICLE_SOC, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_SOC,
                "Number:Dimensionless");
        createChannel(CHANNEL_LOADPOINT_VEHICLE_TITLE, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_VEHICLE_TITLE,
                "String");
    }

    // Units and description for vars: https://docs.evcc.io/docs/reference/configuration/messaging/#msg
    private void updateChannelsGeneral() {
        Result result = this.result;
        if (result == null)
            return;
        ChannelUID channel;
        boolean batteryConfigured = this.batteryConfigured;
        if (batteryConfigured == true) {
            batteryPower = result.getBatteryPower();
            channel = new ChannelUID(getThing().getUID(), "general", CHANNEL_BATTERY_POWER);
            updateState(channel, new QuantityType<>(batteryPower, Units.WATT));
            batterySoC = result.getBatterySoC();
            channel = new ChannelUID(getThing().getUID(), "general", CHANNEL_BATTERY_SOC);
            updateState(channel, new QuantityType<>(batterySoC, Units.PERCENT));
            batteryPrioritySoC = result.getBatterySoC();
            channel = new ChannelUID(getThing().getUID(), "general", CHANNEL_BATTERY_PRIORITY_SOC);
            updateState(channel, new QuantityType<>(batteryPrioritySoC, Units.PERCENT));
        }
        boolean gridConfigured = this.gridConfigured;
        if (gridConfigured == true) {
            gridPower = result.getGridPower();
            channel = new ChannelUID(getThing().getUID(), "general", CHANNEL_GRID_POWER);
            updateState(channel, new QuantityType<>(gridPower, Units.WATT));
        }
        homePower = result.getHomePower();
        channel = new ChannelUID(getThing().getUID(), "general", CHANNEL_HOME_POWER);
        updateState(channel, new QuantityType<>(homePower, Units.WATT));
        boolean pvConfigured = this.pvConfigured;
        if (pvConfigured == true) {
            pvPower = result.getPvPower();
            channel = new ChannelUID(getThing().getUID(), "general", CHANNEL_PV_POWER);
            updateState(channel, new QuantityType<>(pvPower, Units.WATT));
        }
    }

    private void updateChannelsLoadpoint(int loadpointId) {
        Result result = this.result;
        if (result == null)
            return;
        String loadpointName = "loadpoint" + loadpointId;
        ChannelUID channel;
        Loadpoint loadpoint = result.getLoadpoints()[loadpointId];
        activePhases = loadpoint.getActivePhases();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_ACTIVE_PHASES);
        updateState(channel, new DecimalType(activePhases));
        chargeCurrent = loadpoint.getChargeCurrent();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CHARGE_CURRENT);
        updateState(channel, new QuantityType<>(chargeCurrent, Units.AMPERE));
        chargeDuration = loadpoint.getChargeDuration();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CHARGE_DURATION);
        updateState(channel, new QuantityType<>(chargeDuration, MetricPrefix.NANO(Units.SECOND)));
        chargePower = loadpoint.getChargePower();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CHARGE_POWER);
        updateState(channel, new QuantityType<>(chargePower, Units.WATT));
        chargeRemainingDuration = loadpoint.getChargeRemainingDuration();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CHARGE_REMAINING_DURATION);
        updateState(channel, new QuantityType<>(chargeRemainingDuration, MetricPrefix.NANO(Units.SECOND)));
        chargeRemainingEnergy = loadpoint.getChargeRemainingEnergy();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CHARGE_REMAINING_ENERGY);
        updateState(channel, new QuantityType<>(chargeRemainingEnergy, Units.WATT_HOUR));
        chargedEnergy = loadpoint.getChargedEnergy();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CHARGED_ENERGY);
        updateState(channel, new QuantityType<>(chargedEnergy, Units.WATT_HOUR));
        charging = loadpoint.getCharging();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CHARGING);
        updateState(channel, OnOffType.from(charging));
        connected = loadpoint.getConnected();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CONNECTED);
        updateState(channel, OnOffType.from(connected));
        connectedDuration = loadpoint.getConnectedDuration();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CONNECTED_DURATION);
        updateState(channel, new QuantityType<>(connectedDuration, MetricPrefix.NANO(Units.SECOND)));
        enabled = loadpoint.getEnabled();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_ENABLED);
        updateState(channel, OnOffType.from(enabled));
        hasVehicle = loadpoint.getHasVehicle();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_HAS_VEHICLE);
        updateState(channel, OnOffType.from(hasVehicle));
        maxCurrent = loadpoint.getMaxCurrent();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_MAX_CURRENT);
        updateState(channel, new QuantityType<>(maxCurrent, Units.AMPERE));
        minCurrent = loadpoint.getMinCurrent();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_MIN_CURRENT);
        updateState(channel, new QuantityType<>(minCurrent, Units.AMPERE));
        minSoC = loadpoint.getMinSoC();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_MIN_SOC);
        updateState(channel, new QuantityType<>(minSoC, Units.PERCENT));
        mode = loadpoint.getMode();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_MODE);
        updateState(channel, new StringType(mode));
        phases = loadpoint.getPhases();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_PHASES);
        updateState(channel, new DecimalType(phases));
        targetSoC = loadpoint.getTargetSoC();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_TARGET_SOC);
        updateState(channel, new QuantityType<>(targetSoC, Units.PERCENT));
        this.targetTime = loadpoint.getTargetTime();
        String targetTime = this.targetTime;
        if (targetTime == null) {
            channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_TARGET_TIME_ENABLED);
            updateState(channel, OnOffType.OFF);
            targetTimeEnabled = false;
        } else {
            ZonedDateTime targetTimeZDT = ZonedDateTime.parse(targetTime);
            channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_TARGET_TIME);
            updateState(channel, new DateTimeType(targetTimeZDT));
            channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_TARGET_TIME_ENABLED);
            updateState(channel, OnOffType.ON);
            targetTimeEnabled = true;
        }
        title = loadpoint.getTitle();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_TITLE);
        updateState(channel, new StringType(title));
        vehicleCapacity = loadpoint.getVehicleCapacity();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_VEHICLE_CAPACITY);
        updateState(channel, new QuantityType<>(vehicleCapacity, Units.WATT_HOUR));
        vehicleOdometer = loadpoint.getVehicleOdometer();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_VEHICLE_ODOMETER);
        updateState(channel, new QuantityType<>(vehicleOdometer, MetricPrefix.KILO(SIUnits.METRE)));
        vehiclePresent = loadpoint.getVehiclePresent();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_VEHICLE_PRESENT);
        updateState(channel, OnOffType.from(vehiclePresent));
        vehicleRange = loadpoint.getVehicleRange();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_VEHICLE_RANGE);
        updateState(channel, new QuantityType<>(vehicleRange, MetricPrefix.KILO(SIUnits.METRE)));
        vehicleSoC = loadpoint.getVehicleSoC();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_VEHICLE_SOC);
        updateState(channel, new QuantityType<>(vehicleSoC, Units.PERCENT));
        vehicleTitle = loadpoint.getVehicleTitle();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_VEHICLE_TITLE);
        updateState(channel, new StringType(vehicleTitle));
    }

    private void createChannel(String channel, String channelGroupId, ChannelTypeUID channelTypeUID, String itemType) {
        ChannelUID channelToCheck = new ChannelUID(thing.getUID(), channelGroupId, channel);
        if (thing.getChannel(channelToCheck) == null) {
            ThingBuilder thingBuilder = editThing();
            Channel testchannel = ChannelBuilder
                    .create(new ChannelUID(getThing().getUID(), channelGroupId, channel), itemType)
                    .withType(channelTypeUID).build();
            thingBuilder.withChannel(testchannel);
            updateThing(thingBuilder.build());
        }
    }
}
