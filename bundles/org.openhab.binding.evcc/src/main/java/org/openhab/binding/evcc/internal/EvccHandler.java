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
import java.time.format.DateTimeParseException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evcc.internal.api.EvccAPI;
import org.openhab.binding.evcc.internal.api.EvccApiException;
import org.openhab.binding.evcc.internal.api.dto.Loadpoint;
import org.openhab.binding.evcc.internal.api.dto.Result;
import org.openhab.core.library.CoreItemFactory;
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

    private @Nullable Result result;

    private boolean batteryConfigured = false;
    private boolean gridConfigured = false;
    private boolean pvConfigured = false;

    private int targetSoC = 100;
    private boolean targetTimeEnabled = false;
    private ZonedDateTime targetTimeZDT = ZonedDateTime.now().plusHours(12);

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
            String channelIdWithoutGroup = channelUID.getIdWithoutGroup();
            int loadpoint = Integer.parseInt(groupId.toString().substring(9));
            EvccAPI evccAPI = this.evccAPI;
            if (evccAPI == null) {
                return;
            }
            try {
                switch (channelIdWithoutGroup) {
                    case CHANNEL_LOADPOINT_MODE:
                        if (command instanceof StringType) {
                            evccAPI.setMode(loadpoint, command.toString());
                        }
                        break;
                    case CHANNEL_LOADPOINT_MIN_SOC:
                        if (command instanceof QuantityType) {
                            evccAPI.setMinSoC(loadpoint, ((QuantityType<?>) command).intValue());
                        }
                        break;
                    case CHANNEL_LOADPOINT_TARGET_SOC:
                        if (command instanceof QuantityType) {
                            evccAPI.setTargetSoC(loadpoint, ((QuantityType<?>) command).intValue());
                        }
                        break;
                    case CHANNEL_LOADPOINT_TARGET_TIME:
                        if (command instanceof DateTimeType) {
                            targetTimeZDT = ((DateTimeType) command).getZonedDateTime();
                            ChannelUID channel = new ChannelUID(getThing().getUID(), "loadpoint" + loadpoint,
                                    CHANNEL_LOADPOINT_TARGET_TIME);
                            updateState(channel, new DateTimeType(targetTimeZDT));
                            if (targetTimeEnabled) {
                                try {
                                    evccAPI.setTargetCharge(loadpoint, targetSoC, targetTimeZDT);
                                } catch (DateTimeParseException e) {
                                    logger.debug("Failed to set target charge", e);
                                }
                            }
                        }
                        break;
                    case CHANNEL_LOADPOINT_TARGET_TIME_ENABLED:
                        if (command == OnOffType.ON) {
                            evccAPI.setTargetCharge(loadpoint, targetSoC, targetTimeZDT);
                            targetTimeEnabled = true;
                        } else if (command == OnOffType.OFF) {
                            evccAPI.unsetTargetCharge(loadpoint);
                            targetTimeEnabled = false;
                        }
                        break;
                    case CHANNEL_LOADPOINT_PHASES:
                        if (command instanceof DecimalType) {
                            evccAPI.setPhases(loadpoint, ((DecimalType) command).intValue());
                        }
                        break;
                    case CHANNEL_LOADPOINT_MIN_CURRENT:
                        if (command instanceof QuantityType) {
                            evccAPI.setMinCurrent(loadpoint, ((QuantityType<?>) command).intValue());
                        }
                        break;
                    case CHANNEL_LOADPOINT_MAX_CURRENT:
                        if (command instanceof QuantityType) {
                            evccAPI.setMaxCurrent(loadpoint, ((QuantityType<?>) command).intValue());
                        }
                        break;
                    default:
                        return;
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
        EvccAPI evccAPI = null;
        evccAPI = this.evccAPI;
        if (evccAPI == null) {
            return;
        }
        try {
            this.result = evccAPI.getResult();
        } catch (EvccApiException e) {
            logger.debug("Failed to get state");
        }
        Result result = this.result;
        if (result == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                    "@text/offline.communication-error.request-failed");
        } else {
            String sitename = result.getSiteTitle();
            int numberOfLoadpoints = result.getLoadpoints().length;
            logger.debug("Found {} loadpoints on site {}.", numberOfLoadpoints, sitename);
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
        final String channelGroup = "general";
        if (batteryConfigured) {
            createChannel(CHANNEL_BATTERY_POWER, channelGroup, CHANNEL_TYPE_UID_BATTERY_POWER, "Number:Power");
            createChannel(CHANNEL_BATTERY_SOC, channelGroup, CHANNEL_TYPE_UID_BATTERY_SOC, "Number:Dimensionless");
            createChannel(CHANNEL_BATTERY_PRIORITY_SOC, channelGroup, CHANNEL_TYPE_UID_BATTERY_PRIORITY_SOC,
                    "Number:Dimensionless");
        }
        if (gridConfigured) {
            createChannel(CHANNEL_GRID_POWER, channelGroup, CHANNEL_TYPE_UID_GRID_POWER, "Number:Power");
        }
        createChannel(CHANNEL_HOME_POWER, channelGroup, CHANNEL_TYPE_UID_HOME_POWER, "Number:Power");
        if (pvConfigured) {
            createChannel(CHANNEL_PV_POWER, channelGroup, CHANNEL_TYPE_UID_PV_POWER, "Number:Power");
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
        createChannel(CHANNEL_LOADPOINT_HAS_VEHICLE, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_HAS_VEHICLE,
                CoreItemFactory.SWITCH);
        createChannel(CHANNEL_LOADPOINT_MAX_CURRENT, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_MAX_CURRENT,
                "Number:ElectricCurrent");
        createChannel(CHANNEL_LOADPOINT_MIN_CURRENT, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_MIN_CURRENT,
                "Number:ElectricCurrent");
        createChannel(CHANNEL_LOADPOINT_MIN_SOC, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_MIN_SOC,
                "Number:Dimensionless");
        createChannel(CHANNEL_LOADPOINT_MODE, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_MODE, CoreItemFactory.STRING);
        createChannel(CHANNEL_LOADPOINT_PHASES, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_PHASES,
                CoreItemFactory.NUMBER);
        createChannel(CHANNEL_LOADPOINT_TARGET_SOC, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_TARGET_SOC,
                "Number:Dimensionless");
        createChannel(CHANNEL_LOADPOINT_TARGET_TIME, channelGroup, CHANNEL_TYPE_UID_LOADPOINT_TARGET_TIME,
                CoreItemFactory.DATETIME);
        createChannel(CHANNEL_LOADPOINT_TARGET_TIME_ENABLED, channelGroup,
                CHANNEL_TYPE_UID_LOADPOINT_TARGET_TIME_ENABLED, CoreItemFactory.SWITCH);
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
    }

    // Units and description for vars: https://docs.evcc.io/docs/reference/configuration/messaging/#msg
    private void updateChannelsGeneral() {
        Result result = this.result;
        if (result == null) {
            return;
        }
        ChannelUID channel;
        boolean batteryConfigured = this.batteryConfigured;
        if (batteryConfigured) {
            double batteryPower = result.getBatteryPower();
            channel = new ChannelUID(getThing().getUID(), "general", CHANNEL_BATTERY_POWER);
            updateState(channel, new QuantityType<>(batteryPower, Units.WATT));
            int batterySoC = result.getBatterySoC();
            channel = new ChannelUID(getThing().getUID(), "general", CHANNEL_BATTERY_SOC);
            updateState(channel, new QuantityType<>(batterySoC, Units.PERCENT));
            int batteryPrioritySoC = result.getBatterySoC();
            channel = new ChannelUID(getThing().getUID(), "general", CHANNEL_BATTERY_PRIORITY_SOC);
            updateState(channel, new QuantityType<>(batteryPrioritySoC, Units.PERCENT));
        }
        boolean gridConfigured = this.gridConfigured;
        if (gridConfigured) {
            double gridPower = result.getGridPower();
            channel = new ChannelUID(getThing().getUID(), "general", CHANNEL_GRID_POWER);
            updateState(channel, new QuantityType<>(gridPower, Units.WATT));
        }
        double homePower = result.getHomePower();
        channel = new ChannelUID(getThing().getUID(), "general", CHANNEL_HOME_POWER);
        updateState(channel, new QuantityType<>(homePower, Units.WATT));
        boolean pvConfigured = this.pvConfigured;
        if (pvConfigured) {
            double pvPower = result.getPvPower();
            channel = new ChannelUID(getThing().getUID(), "general", CHANNEL_PV_POWER);
            updateState(channel, new QuantityType<>(pvPower, Units.WATT));
        }
    }

    private void updateChannelsLoadpoint(int loadpointId) {
        Result result = this.result;
        if (result == null) {
            return;
        }
        String loadpointName = "loadpoint" + loadpointId;
        ChannelUID channel;
        Loadpoint loadpoint = result.getLoadpoints()[loadpointId];
        int activePhases = loadpoint.getActivePhases();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_ACTIVE_PHASES);
        updateState(channel, new DecimalType(activePhases));
        double chargeCurrent = loadpoint.getChargeCurrent();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CHARGE_CURRENT);
        updateState(channel, new QuantityType<>(chargeCurrent, Units.AMPERE));
        long chargeDuration = loadpoint.getChargeDuration();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CHARGE_DURATION);
        updateState(channel, new QuantityType<>(chargeDuration, MetricPrefix.NANO(Units.SECOND)));
        double chargePower = loadpoint.getChargePower();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CHARGE_POWER);
        updateState(channel, new QuantityType<>(chargePower, Units.WATT));
        long chargeRemainingDuration = loadpoint.getChargeRemainingDuration();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CHARGE_REMAINING_DURATION);
        updateState(channel, new QuantityType<>(chargeRemainingDuration, MetricPrefix.NANO(Units.SECOND)));
        double chargeRemainingEnergy = loadpoint.getChargeRemainingEnergy();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CHARGE_REMAINING_ENERGY);
        updateState(channel, new QuantityType<>(chargeRemainingEnergy, Units.WATT_HOUR));
        double chargedEnergy = loadpoint.getChargedEnergy();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CHARGED_ENERGY);
        updateState(channel, new QuantityType<>(chargedEnergy, Units.WATT_HOUR));
        boolean charging = loadpoint.getCharging();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CHARGING);
        updateState(channel, OnOffType.from(charging));
        boolean connected = loadpoint.getConnected();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CONNECTED);
        updateState(channel, OnOffType.from(connected));
        long connectedDuration = loadpoint.getConnectedDuration();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CONNECTED_DURATION);
        updateState(channel, new QuantityType<>(connectedDuration, MetricPrefix.NANO(Units.SECOND)));
        boolean enabled = loadpoint.getEnabled();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_ENABLED);
        updateState(channel, OnOffType.from(enabled));
        boolean hasVehicle = loadpoint.getHasVehicle();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_HAS_VEHICLE);
        updateState(channel, OnOffType.from(hasVehicle));
        double maxCurrent = loadpoint.getMaxCurrent();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_MAX_CURRENT);
        updateState(channel, new QuantityType<>(maxCurrent, Units.AMPERE));
        double minCurrent = loadpoint.getMinCurrent();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_MIN_CURRENT);
        updateState(channel, new QuantityType<>(minCurrent, Units.AMPERE));
        int minSoC = loadpoint.getMinSoC();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_MIN_SOC);
        updateState(channel, new QuantityType<>(minSoC, Units.PERCENT));
        String mode = loadpoint.getMode();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_MODE);
        updateState(channel, new StringType(mode));
        int phases = loadpoint.getPhases();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_PHASES);
        updateState(channel, new DecimalType(phases));
        targetSoC = loadpoint.getTargetSoC();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_TARGET_SOC);
        updateState(channel, new QuantityType<>(targetSoC, Units.PERCENT));
        String targetTime = loadpoint.getTargetTime();
        if (targetTime == null) {
            channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_TARGET_TIME_ENABLED);
            updateState(channel, OnOffType.OFF);
            targetTimeEnabled = false;
        } else {
            this.targetTimeZDT = ZonedDateTime.parse(targetTime);
            channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_TARGET_TIME);
            updateState(channel, new DateTimeType(targetTimeZDT));
            channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_TARGET_TIME_ENABLED);
            updateState(channel, OnOffType.ON);
            targetTimeEnabled = true;
        }
        String title = loadpoint.getTitle();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_TITLE);
        updateState(channel, new StringType(title));
        double vehicleCapacity = loadpoint.getVehicleCapacity();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_VEHICLE_CAPACITY);
        updateState(channel, new QuantityType<>(vehicleCapacity, Units.WATT_HOUR));
        double vehicleOdometer = loadpoint.getVehicleOdometer();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_VEHICLE_ODOMETER);
        updateState(channel, new QuantityType<>(vehicleOdometer, MetricPrefix.KILO(SIUnits.METRE)));
        boolean vehiclePresent = loadpoint.getVehiclePresent();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_VEHICLE_PRESENT);
        updateState(channel, OnOffType.from(vehiclePresent));
        long vehicleRange = loadpoint.getVehicleRange();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_VEHICLE_RANGE);
        updateState(channel, new QuantityType<>(vehicleRange, MetricPrefix.KILO(SIUnits.METRE)));
        int vehicleSoC = loadpoint.getVehicleSoC();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_VEHICLE_SOC);
        updateState(channel, new QuantityType<>(vehicleSoC, Units.PERCENT));
        String vehicleTitle = loadpoint.getVehicleTitle();
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
