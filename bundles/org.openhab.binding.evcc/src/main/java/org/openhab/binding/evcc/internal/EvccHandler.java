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

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.evcc.internal.dto.Loadpoint;
import org.openhab.binding.evcc.internal.dto.Status;
import org.openhab.core.io.net.http.HttpUtil;
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

import com.google.gson.Gson;

/**
 * The {@link EvccHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class EvccHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(EvccHandler.class);
    private final Gson gson = new Gson();
    private @Nullable ScheduledFuture<?> statePollingJob;

    private @Nullable EvccConfiguration config;

    private @Nullable Status status;

    private int numberOfLoadpoints = 0;
    private @Nullable String sitename;
    private boolean batteryConfigured = false;
    private boolean gridConfigured = false;
    private boolean pvConfigured = false;

    private boolean targetTimeEnabled = false;
    private int targetSoC = 0;
    private ZonedDateTime targetTimeZDT = ZonedDateTime.now();

    public EvccHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command.equals(RefreshType.REFRESH)) {
            refresh();
        } else {
            logger.debug("Handling command {} for channel {}", command, channelUID);
            String channelIdWithoutGroup = channelUID.getIdWithoutGroup();
            int loadpoint = Integer.parseInt(channelUID.getGroupId().toString().substring(9));
            targetSoC = status.getResult().getLoadpoints()[loadpoint].getTargetSoC();
            switch (channelIdWithoutGroup) {
                case CHANNEL_LOADPOINT_MODE:
                    setMode(config.url, loadpoint, command.toString());
                    refresh();
                    break;
                case CHANNEL_LOADPOINT_MIN_SOC:
                    setMinSoC(config.url, loadpoint, Integer.parseInt(command.toString().replaceAll(" %", "")));
                    refresh();
                    break;
                case CHANNEL_LOADPOINT_TARGET_SOC:
                    setTargetSoC(config.url, loadpoint, Integer.parseInt(command.toString().replaceAll(" %", "")));
                    refresh();
                    break;
                case CHANNEL_LOADPOINT_TARGET_TIME:
                    if (targetTimeEnabled == true) {
                        targetTimeZDT = new DateTimeType(command.toString()).getZonedDateTime();
                        setTargetCharge(config.url, loadpoint, targetSoC, targetTimeZDT);
                        ChannelUID channel = new ChannelUID(getThing().getUID(), "loadpoint" + loadpoint,
                                CHANNEL_LOADPOINT_TARGET_TIME);
                        updateState(channel, new DateTimeType(targetTimeZDT));
                    }
                    refresh();
                    break;
                case CHANNEL_LOADPOINT_TARGET_TIME_ENABLED:
                    if (command == OnOffType.ON) {
                        targetTimeEnabled = true;
                        setTargetCharge(config.url, loadpoint, targetSoC, targetTimeZDT);
                    } else {
                        targetTimeEnabled = false;
                        unsetTargetCharge(config.url, loadpoint);
                    }
                    refresh();
                    break;
                case CHANNEL_LOADPOINT_PHASES:
                    setPhases(config.url, loadpoint, Integer.parseInt(command.toString()));
                    refresh();
                    break;
                case CHANNEL_LOADPOINT_MIN_CURRENT:
                    setMinCurrent(config.url, loadpoint, Integer.parseInt(command.toString().replaceAll(" A", "")));
                    refresh();
                    break;
                case CHANNEL_LOADPOINT_MAX_CURRENT:
                    setMaxCurrent(config.url, loadpoint, Integer.parseInt(command.toString().replaceAll(" A", "")));
                    refresh();
                    break;
            }
        }

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information:
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
    }

    @Override
    public void initialize() {
        config = getConfigAs(EvccConfiguration.class);
        updateStatus(ThingStatus.UNKNOWN);

        if ("".equals(config.url)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, "No host configured");
        } else {
            // Background initialization:
            scheduler.execute(() -> {
                status = getStatus(config.url);
                try {
                    sitename = status.getResult().getSiteTitle();
                    numberOfLoadpoints = status.getResult().getLoadpoints().length;
                    logger.debug("Found {} loadpoints on site {} (host: {}).", numberOfLoadpoints, sitename,
                            config.url);
                    updateStatus(ThingStatus.ONLINE);
                    refreshOnStartup();
                } catch (Exception e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                            "Failed to connect to evcc: " + e);
                }
            });
        }
    }

    private void statePolling() {
        logger.debug("Running polling job ...");
        refresh();
    }

    private void refresh() {
        status = getStatus(config.url);
        try {
            sitename = status.getResult().getSiteTitle();
            numberOfLoadpoints = status.getResult().getLoadpoints().length;
            logger.debug("Found {} loadpoints on site {} (host: {}).", numberOfLoadpoints, sitename, config.url);
            updateStatus(ThingStatus.ONLINE);
            batteryConfigured = status.getResult().getBatteryConfigured();
            gridConfigured = status.getResult().getGridConfigured();
            pvConfigured = status.getResult().getPvConfigured();
            updateChannelsGeneral();
            for (int i = 0; i < numberOfLoadpoints; i++) {
                updateChannelsLoadpoint(i);
            }
        } catch (Exception e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Failed to connect to evcc: " + e);
        }
    }

    private void refreshOnStartup() {
        batteryConfigured = status.getResult().getBatteryConfigured();
        gridConfigured = status.getResult().getGridConfigured();
        pvConfigured = status.getResult().getPvConfigured();
        createChannelsGeneral();
        updateChannelsGeneral();
        for (int i = 0; i < this.numberOfLoadpoints; i++) {
            createChannelsLoadpoint(i);
            updateChannelsLoadpoint(i);
        }
        logger.debug("Setting up polling job ...");
        statePollingJob = scheduler.scheduleWithFixedDelay(this::statePolling, 60, 60, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
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
        // createChannel(CHANNEL_LOADPOINT_PV_ACTION, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_PV_ACTION, "Switch");
        // createChannel(CHANNEL_LOADPOINT_PV_REMAINING, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_PV_REMAINING,
        // "Number");
        createChannel(CHANNEL_LOADPOINT_TARGET_SOC, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_TARGET_SOC,
                "Number:Dimensionless");
        createChannel(CHANNEL_LOADPOINT_TARGET_TIME, loadpointName, CHANNEL_TYPE_UID_LOADPOINT_TARGET_TIME, "DateTime");
        createChannel(CHANNEL_LOADPOINT_TARGET_TIME_ACTIVE, loadpointName,
                CHANNEL_TYPE_UID_LOADPOINT_TARGET_TIME_ACTIVE, "Switch");
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
        ChannelUID channel;
        if (batteryConfigured == true) {
            double batteryPower = status.getResult().getBatteryPower();
            channel = new ChannelUID(getThing().getUID(), "general", CHANNEL_BATTERY_POWER);
            updateState(channel, new QuantityType<>(batteryPower, Units.WATT));
            int batterySoC = status.getResult().getBatterySoC();
            channel = new ChannelUID(getThing().getUID(), "general", CHANNEL_BATTERY_SOC);
            updateState(channel, new QuantityType<>(batterySoC, Units.PERCENT));
            int batteryPrioritySoC = status.getResult().getBatterySoC();
            channel = new ChannelUID(getThing().getUID(), "general", CHANNEL_BATTERY_PRIORITY_SOC);
            updateState(channel, new QuantityType<>(batteryPrioritySoC, Units.PERCENT));
        }
        if (gridConfigured == true) {
            double gridPower = status.getResult().getGridPower();
            channel = new ChannelUID(getThing().getUID(), "general", CHANNEL_GRID_POWER);
            updateState(channel, new QuantityType<>(gridPower, Units.WATT));
        }
        double homePower = status.getResult().getHomePower();
        channel = new ChannelUID(getThing().getUID(), "general", CHANNEL_HOME_POWER);
        updateState(channel, new QuantityType<>(homePower, Units.WATT));
        if (pvConfigured == true) {
            double pvPower = status.getResult().getPvPower();
            channel = new ChannelUID(getThing().getUID(), "general", CHANNEL_PV_POWER);
            updateState(channel, new QuantityType<>(pvPower, Units.WATT));
        }
    }

    private void updateChannelsLoadpoint(int loadpointId) {
        String loadpointName = "loadpoint" + loadpointId;
        ChannelUID channel;
        Loadpoint loadpoint = status.getResult().getLoadpoints()[loadpointId];
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
        double chargedEnergy = loadpoint.getChargedEnergy();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CHARGED_ENERGY);
        updateState(channel, new QuantityType<>(chargedEnergy, Units.WATT_HOUR));
        boolean charging = loadpoint.getCharging();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CHARGING);
        if (charging == true) {
            updateState(channel, OnOffType.ON);
        } else {
            updateState(channel, OnOffType.OFF);
        }
        boolean connected = loadpoint.getConnected();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CONNECTED);
        if (connected == true) {
            updateState(channel, OnOffType.ON);
        } else {
            updateState(channel, OnOffType.OFF);
        }
        long connectedDuration = loadpoint.getConnectedDuration();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_CONNECTED_DURATION);
        updateState(channel, new QuantityType<>(connectedDuration, MetricPrefix.NANO(Units.SECOND)));
        boolean enabled = loadpoint.getEnabled();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_ENABLED);
        if (enabled == true) {
            updateState(channel, OnOffType.ON);
        } else {
            updateState(channel, OnOffType.OFF);
        }
        boolean hasVehicle = loadpoint.getHasVehicle();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_HAS_VEHICLE);
        if (hasVehicle == true) {
            updateState(channel, OnOffType.ON);
        } else {
            updateState(channel, OnOffType.OFF);
        }
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
        /*
         * String pvAction = loadpoint.getPvAction();
         * channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_PV_ACTION);
         * if (pvAction == "active") {
         * updateState(channel, OnOffType.ON);
         * } else {
         * updateState(channel, OnOffType.OFF);
         * }
         */
        targetSoC = loadpoint.getTargetSoC();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_TARGET_SOC);
        updateState(channel, new QuantityType<>(targetSoC, Units.PERCENT));
        String targetTime = loadpoint.getTargetTime();
        ZonedDateTime newTargetTimeZDT = ZonedDateTime.now().plusSeconds(30);
        try {
            Instant instant = Instant.parse(targetTime);
            newTargetTimeZDT = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
        } catch (Exception e) {
            try {
                newTargetTimeZDT = ZonedDateTime.parse(targetTime);
            } catch (Exception f) {
                logger.debug("Failed parsing targetTime {}. Error: {}.", targetTime, f.toString());
            }
        }
        if (newTargetTimeZDT.isAfter(ZonedDateTime.now())) {
            targetTimeZDT = newTargetTimeZDT;
            logger.debug("Updating targetTime to {}", targetTimeZDT);
            channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_TARGET_TIME);
            updateState(channel, new DateTimeType(targetTimeZDT));
            channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_TARGET_TIME_ENABLED);
            updateState(channel, OnOffType.ON);
            targetTimeEnabled = true;
        } else {
            channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_TARGET_TIME_ENABLED);
            updateState(channel, OnOffType.OFF);
            targetTimeEnabled = false;
        }
        boolean targetTimeActive = loadpoint.getTargetTimeActive();
        channel = new ChannelUID(getThing().getUID(), loadpointName, CHANNEL_LOADPOINT_TARGET_TIME_ACTIVE);
        if (targetTimeActive == true) {
            updateState(channel, OnOffType.ON);
        } else {
            updateState(channel, OnOffType.OFF);
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
        if (vehiclePresent == true) {
            updateState(channel, OnOffType.ON);
        } else {
            updateState(channel, OnOffType.OFF);
        }
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

    /**
     * Make a HTTP request.
     * 
     * @param description request description for logger
     * @param url full request URL
     * @param method reguest method, e.g. GET, POST
     * @return the response body or response_code 999 if request faild
     */
    private String httpRequest(@Nullable String description, String url, String method) {
        String response = "";
        try {
            response = HttpUtil.executeUrl(method, url, LONG_CONNECTION_TIMEOUT_MILLISEC);
            logger.trace("{} - {}, {} - {}", description, url, method, response);
            return response;
        } catch (IOException e) {
            logger.warn("IO Exception - {} - {}, {} - {}", description, url, method, e.toString());
            return "{\"response_code\":\"999\"}";
        }
    }
    // End utility functions

    // API calls to evcc
    /**
     * Get the status from evcc.
     * 
     * @param host hostname of IP address of the evcc instance
     * @return Status object or null if request failed
     */
    private @Nullable Status getStatus(@Nullable String host) {
        final String response = httpRequest("Status", host + EVCC_REST_API + "state", "GET");
        return gson.fromJson(response, Status.class);
    }

    // Loadpoint specific API calls.
    private @Nullable String setMode(@Nullable String host, int loadpoint, String mode) {
        return httpRequest("Set mode of loadpoint " + loadpoint,
                host + EVCC_REST_API + "loadpoints/" + loadpoint + "/mode/" + mode, "POST");
    }

    private @Nullable String setMinSoC(@Nullable String host, int loadpoint, int minSoC) {
        return httpRequest("Set minSoC of loadpoint " + loadpoint,
                host + EVCC_REST_API + "loadpoints/" + loadpoint + "/minsoc/" + minSoC, "POST");
    }

    private @Nullable String setTargetSoC(@Nullable String host, int loadpoint, int targetSoC) {
        return httpRequest("Set targetSoC of loadpoint " + loadpoint,
                host + EVCC_REST_API + "loadpoints/" + loadpoint + "/targetsoc/" + targetSoC, "POST");
    }

    private @Nullable String setPhases(@Nullable String host, int loadpoint, int phases) {
        return httpRequest("Set phases of loadpoint " + loadpoint,
                host + EVCC_REST_API + "loadpoints/" + loadpoint + "/phases/" + phases, "POST");
    }

    private @Nullable String setMinCurrent(@Nullable String host, int loadpoint, int minCurrent) {
        return httpRequest("Set minCurrent of loadpoint " + loadpoint,
                host + EVCC_REST_API + "loadpoints/" + loadpoint + "/mincurrent/" + minCurrent, "POST");
    }

    private @Nullable String setMaxCurrent(@Nullable String host, int loadpoint, int maxCurrent) {
        return httpRequest("Set maxCurrent of loadpoint " + loadpoint,
                host + EVCC_REST_API + "loadpoints/" + loadpoint + "/maxcurrent/" + maxCurrent, "POST");
    }

    private @Nullable String setTargetCharge(@Nullable String host, int loadpoint, int targetSoC,
            ZonedDateTime targetTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        return httpRequest("Set targetTime of loadpoint " + loadpoint, host + EVCC_REST_API + "loadpoints/" + loadpoint
                + "/targetcharge/" + targetSoC + "/" + targetTime.toLocalDateTime().format(formatter), "POST");
    }

    private @Nullable String unsetTargetCharge(@Nullable String host, int loadpoint) {
        return httpRequest("Unset targetTime of loadpoint " + loadpoint,
                host + EVCC_REST_API + "loadpoints/" + loadpoint + "/targetcharge", "DELETE");
    }
    // End loadpoint specific API calls
    // End API calls to evcc
}
