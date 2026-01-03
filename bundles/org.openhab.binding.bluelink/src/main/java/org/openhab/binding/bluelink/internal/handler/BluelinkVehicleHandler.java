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
package org.openhab.binding.bluelink.internal.handler;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.openhab.binding.bluelink.internal.BluelinkBindingConstants.*;
import static org.openhab.core.library.CoreItemFactory.SWITCH;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluelink.internal.api.BluelinkApiException;
import org.openhab.binding.bluelink.internal.api.Vehicle;
import org.openhab.binding.bluelink.internal.api.VehicleStatus;
import org.openhab.binding.bluelink.internal.config.BluelinkVehicleConfiguration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BluelinkVehicleHandler} handles a single Bluelink vehicle.
 *
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public class BluelinkVehicleHandler extends BaseThingHandler {

    private static final Duration DEFAULT_REFRESH_INTERVAL = Duration.ofMinutes(30);
    private static final Duration DEFAULT_FORCE_REFRESH_INTERVAL = Duration.ofMinutes(240);

    private final Logger logger = LoggerFactory.getLogger(BluelinkVehicleHandler.class);
    private final BaseThingHandlerFactory thingHandlerFactory;

    private volatile @Nullable ScheduledFuture<?> refreshJob;
    private volatile @Nullable ScheduledFuture<?> forceRefreshJob;
    private volatile @Nullable ScheduledFuture<?> initTask;
    private volatile @Nullable Vehicle vehicle;
    private @Nullable Duration forceRefreshInterval;

    public BluelinkVehicleHandler(final Thing thing, final BaseThingHandlerFactory thingHandlerFactory) {
        super(thing);
        this.thingHandlerFactory = thingHandlerFactory;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Bluelink vehicle handler");

        final BluelinkVehicleConfiguration config = getConfigAs(BluelinkVehicleConfiguration.class);
        final String vin = config.vin;
        if (vin == null || vin.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/vehicle-handler.initialize.vin-required");
            return;
        }

        final Duration refreshInterval = config.refreshInterval >= 1 ? Duration.ofMinutes(config.refreshInterval)
                : DEFAULT_REFRESH_INTERVAL;
        final Duration forceRefreshInterval = config.forceRefreshInterval >= 1
                ? Duration.ofMinutes(config.forceRefreshInterval)
                : DEFAULT_FORCE_REFRESH_INTERVAL;
        this.forceRefreshInterval = forceRefreshInterval;
        refreshJob = scheduler.scheduleWithFixedDelay(() -> refreshVehicleStatus(false), 5, refreshInterval.toSeconds(),
                TimeUnit.SECONDS);
        forceRefreshJob = scheduler.scheduleWithFixedDelay(() -> refreshVehicleStatus(true), 30,
                forceRefreshInterval.toSeconds(), TimeUnit.SECONDS);

        updateStatus(ThingStatus.UNKNOWN);
        initTask = scheduler.schedule(() -> loadVehicle(vin), 0, TimeUnit.MILLISECONDS);

        final var bridgeHnd = getBridgeHandler();
        if (bridgeHnd != null) {
            if (bridgeHnd.supportsControlActions()) {
                thingHandlerFactory.registerService(this, VehicleControlActions.class);
            } else {
                thingHandlerFactory.registerService(this, BaseVehicleActions.class);
            }
        }
    }

    private void loadVehicle(final String vin) {
        final var bridgeHnd = getBridgeHandler();
        if (bridgeHnd == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }

        try {
            bridgeHnd.getVehicles().stream().filter(v -> vin.equals(v.vin())).findFirst().ifPresentOrElse(v -> {
                this.vehicle = v;
                updateProperty(PROPERTY_MODEL, v.model());
                updateProperty(PROPERTY_ENGINE_TYPE, v.engineType());
                createDynamicChannels();
                updateStatus(ThingStatus.ONLINE);
            }, () -> updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/vehicle-handler.initialize.vehicle-not-found")

            );
        } catch (final BluelinkApiException e) {
            logger.debug("Error loading vehicle: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/vehicle-handler.initialize.vehicle-data-error");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Bluelink vehicle handler");

        ScheduledFuture<?> job = refreshJob;
        if (job != null) {
            job.cancel(true);
            refreshJob = null;
        }
        job = forceRefreshJob;
        if (job != null) {
            job.cancel(true);
            forceRefreshJob = null;
        }
        job = initTask;
        if (job != null) {
            job.cancel(true);
            initTask = null;
        }

        vehicle = null;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        if (command instanceof RefreshType) {
            // we do not force a refresh from the vehicle because of the low rate limit
            refreshVehicleStatus(false);
        }
    }

    public boolean lock() throws BluelinkApiException {
        final var bridgeHnd = getBridgeHandler();
        final var vehicle = this.vehicle;
        if (vehicle == null || bridgeHnd == null) {
            return false;
        }
        final boolean res = bridgeHnd.lockVehicle(vehicle);
        if (res) {
            scheduleForceRefresh();
        }
        return res;
    }

    public boolean unlock() throws BluelinkApiException {
        final var bridgeHnd = getBridgeHandler();
        final var vehicle = this.vehicle;
        if (vehicle == null || bridgeHnd == null) {
            return false;
        }
        final boolean res = bridgeHnd.unlockVehicle(vehicle);
        if (res) {
            scheduleForceRefresh();
        }
        return res;
    }

    public boolean startCharging() throws BluelinkApiException {
        final var bridgeHnd = getBridgeHandler();
        final var vehicle = this.vehicle;
        if (vehicle == null || bridgeHnd == null) {
            return false;
        }
        final boolean res = bridgeHnd.startCharging(vehicle);
        if (res) {
            scheduleForceRefresh();
        }
        return res;
    }

    public boolean stopCharging() throws BluelinkApiException {
        final var bridgeHnd = getBridgeHandler();
        final var vehicle = this.vehicle;
        if (vehicle == null || bridgeHnd == null) {
            return false;
        }
        final boolean res = bridgeHnd.stopCharging(vehicle);
        if (res) {
            scheduleForceRefresh();
        }
        return res;
    }

    public boolean climateStart(final QuantityType<Temperature> temperature, final boolean heat, final boolean defrost)
            throws BluelinkApiException {
        final Vehicle vehicle = this.vehicle;
        final var bridgeHnd = getBridgeHandler();
        if (vehicle == null || bridgeHnd == null) {
            return false;
        }

        return bridgeHnd.climateStart(vehicle, temperature, heat, defrost);
    }

    public boolean climateStop() throws BluelinkApiException {
        final Vehicle vehicle = this.vehicle;
        final var bridgeHnd = getBridgeHandler();
        if (vehicle == null || bridgeHnd == null) {
            return false;
        }

        return bridgeHnd.climateStop(vehicle);
    }

    public void refreshVehicleStatus(boolean forceRefresh) {
        final Vehicle vehicle = this.vehicle;
        final var bridgeHnd = getBridgeHandler();
        if (vehicle == null || bridgeHnd == null) {
            return;
        }

        try {
            logger.debug("{} vehicle status", forceRefresh ? "Force refreshing" : "Refreshing");
            final VehicleStatus status = bridgeHnd.getVehicleStatus(vehicle, forceRefresh);
            if (status != null) {
                updateStatus(ThingStatus.ONLINE);
                updateChannels(status);
            }
        } catch (final BluelinkApiException e) {
            logger.debug("Failed to refresh vehicle status: {}", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void updateChannels(final VehicleStatus status) {
        final Vehicle info = vehicle;
        if (info == null) {
            return;
        }

        // Status group
        updateState(GROUP_STATUS, CHANNEL_LOCKED, OnOffType.from(status.doorsLocked()));
        updateState(GROUP_STATUS, CHANNEL_ENGINE_RUNNING, OnOffType.from(status.engineOn()));
        var odometer = status.odometer();
        if (odometer != null) {
            updateState(GROUP_STATUS, CHANNEL_ODOMETER, odometer);
        }
        if (status.lastUpdate() != null) {
            updateState(GROUP_STATUS, CHANNEL_LAST_UPDATE, new DateTimeType(status.lastUpdate()));
        }

        // 12V Battery
        updateState(GROUP_STATUS, CHANNEL_BATTERY_LEVEL, new QuantityType<>(status.batterySoC(), Units.PERCENT));

        // Doors
        final var doorOpen = status.doorOpen();
        if (doorOpen != null) {
            updateState(GROUP_DOORS, CHANNEL_DOOR_FRONT_LEFT, toOpenClosed(doorOpen.frontLeft()));
            updateState(GROUP_DOORS, CHANNEL_DOOR_FRONT_RIGHT, toOpenClosed(doorOpen.frontRight()));
            updateState(GROUP_DOORS, CHANNEL_DOOR_REAR_LEFT, toOpenClosed(doorOpen.rearLeft()));
            updateState(GROUP_DOORS, CHANNEL_DOOR_REAR_RIGHT, toOpenClosed(doorOpen.rearRight()));
        }
        updateState(GROUP_DOORS, CHANNEL_TRUNK, toOpenClosed(status.trunkOpen()));
        updateState(GROUP_DOORS, CHANNEL_HOOD, toOpenClosed(status.hoodOpen()));

        // Windows
        final var windowOpen = status.windowOpen();
        if (windowOpen != null) {
            updateState(GROUP_WINDOWS, CHANNEL_WINDOW_FRONT_LEFT, toOpenClosed(windowOpen.frontLeft()));
            updateState(GROUP_WINDOWS, CHANNEL_WINDOW_FRONT_RIGHT, toOpenClosed(windowOpen.frontRight()));
            updateState(GROUP_WINDOWS, CHANNEL_WINDOW_REAR_LEFT, toOpenClosed(windowOpen.rearLeft()));
            updateState(GROUP_WINDOWS, CHANNEL_WINDOW_REAR_RIGHT, toOpenClosed(windowOpen.rearRight()));
        }

        // Climate
        updateState(GROUP_CLIMATE, CHANNEL_HVAC_ON, OnOffType.from(status.airControlOn()));
        updateState(GROUP_CLIMATE, CHANNEL_DEFROST, OnOffType.from(status.defrost()));
        updateState(GROUP_CLIMATE, CHANNEL_TEMPERATURE, status.airTemp());
        updateState(GROUP_CLIMATE, CHANNEL_STEERING_HEATER, OnOffType.from(status.steerWheelHeat()));
        updateState(GROUP_CLIMATE, CHANNEL_REAR_WINDOW_HEATER, OnOffType.from(status.sideBackWindowHeat()));
        Boolean sideMirrorHeat = status.sideMirrorHeat();
        if (sideMirrorHeat != null) {
            updateState(GROUP_CLIMATE, CHANNEL_SIDE_MIRROR_HEATER, OnOffType.from(sideMirrorHeat));
        }

        final var heater = status.seatHeaterVent();
        if (heater != null) {
            updateState(GROUP_CLIMATE, CHANNEL_SEAT_FRONT_LEFT, new DecimalType(heater.frontLeft()));
            updateState(GROUP_CLIMATE, CHANNEL_SEAT_FRONT_RIGHT, new DecimalType(heater.frontRight()));
            updateState(GROUP_CLIMATE, CHANNEL_SEAT_REAR_LEFT, new DecimalType(heater.rearLeft()));
            updateState(GROUP_CLIMATE, CHANNEL_SEAT_REAR_RIGHT, new DecimalType(heater.rearRight()));
        }

        // EV-specific
        final var evStatus = status.evStatus();
        if (info.electric() && evStatus != null) {
            updateState(GROUP_CHARGING, CHANNEL_EV_BATTERY_SOC,
                    new QuantityType<>(evStatus.batterySoC(), Units.PERCENT));
            updateState(GROUP_CHARGING, CHANNEL_EV_CHARGING, OnOffType.from(evStatus.isCharging()));
            updateState(GROUP_CHARGING, CHANNEL_EV_PLUGGED_IN, OnOffType.from(evStatus.isPluggedIn()));

            // Driving ranges
            final var rangeByFuel = evStatus.range();
            if (rangeByFuel != null) {
                updateState(GROUP_RANGE, CHANNEL_TOTAL_RANGE, rangeByFuel.total());
                updateState(GROUP_RANGE, CHANNEL_EV_RANGE, rangeByFuel.ev());
                updateState(GROUP_RANGE, CHANNEL_FUEL_RANGE, rangeByFuel.gas());
            }

            // Charge limits
            final var targetSoCs = evStatus.targetSoCs();
            if (targetSoCs != null && !targetSoCs.isEmpty()) {
                for (final var target : targetSoCs) {
                    if ("DC".equals(target.plugType())) {
                        updateState(GROUP_CHARGING, CHANNEL_CHARGE_LIMIT_DC,
                                new QuantityType<>(target.level(), Units.PERCENT));
                    } else if ("AC".equals(target.plugType())) {
                        updateState(GROUP_CHARGING, CHANNEL_CHARGE_LIMIT_AC,
                                new QuantityType<>(target.level(), Units.PERCENT));
                    }
                }
            }

            // Charge times
            final var chargeRemainingTime = evStatus.chargeRemainingTime();
            if (chargeRemainingTime != null) {
                updateState(GROUP_CHARGING, CHANNEL_TIME_TO_FULL_CURRENT,
                        new QuantityType<>(chargeRemainingTime.currentMinutes(), Units.MINUTE));
                updateState(GROUP_CHARGING, CHANNEL_TIME_TO_FULL_FAST,
                        new QuantityType<>(chargeRemainingTime.fastMinutes(), Units.MINUTE));
                updateState(GROUP_CHARGING, CHANNEL_TIME_TO_FULL_PORTABLE,
                        new QuantityType<>(chargeRemainingTime.portableMinutes(), Units.MINUTE));
                updateState(GROUP_CHARGING, CHANNEL_TIME_TO_FULL_STATION,
                        new QuantityType<>(chargeRemainingTime.stationMinutes(), Units.MINUTE));
            }
        }

        // Fuel (ICE only)
        if (!info.electric()) {
            updateState(GROUP_FUEL, CHANNEL_FUEL_LEVEL, new QuantityType<>(status.fuelLevel(), Units.PERCENT));
            updateState(GROUP_FUEL, CHANNEL_FUEL_LOW_WARNING, OnOffType.from(status.lowFuelWarning()));
            updateState(GROUP_RANGE, CHANNEL_FUEL_RANGE, status.fuelRange());
        }

        // Warnings
        updateState(GROUP_WARNINGS, CHANNEL_WASHER_FLUID_WARNING, OnOffType.from(status.washerFluidLow()));
        updateState(GROUP_WARNINGS, CHANNEL_BRAKE_FLUID_WARNING, OnOffType.from(status.brakeOilWarning()));
        updateState(GROUP_WARNINGS, CHANNEL_SMART_KEY_BATTERY_WARNING, OnOffType.from(status.smartKeyBatteryWarning()));
        final var tirePressureWarning = status.tirePressureWarning();
        if (tirePressureWarning != null) {
            updateState(GROUP_WARNINGS, CHANNEL_TIRE_PRESSURE_WARNING, OnOffType.from(tirePressureWarning.all()));
            updateState(GROUP_WARNINGS, CHANNEL_TIRE_PRESSURE_WARNING_FR,
                    OnOffType.from(tirePressureWarning.frontRight()));
            updateState(GROUP_WARNINGS, CHANNEL_TIRE_PRESSURE_WARNING_FL,
                    OnOffType.from(tirePressureWarning.frontLeft()));
            updateState(GROUP_WARNINGS, CHANNEL_TIRE_PRESSURE_WARNING_RR,
                    OnOffType.from(tirePressureWarning.rearRight()));
            updateState(GROUP_WARNINGS, CHANNEL_TIRE_PRESSURE_WARNING_RL,
                    OnOffType.from(tirePressureWarning.rearLeft()));
        }

        // Location
        final var location = status.location();
        if (location != null) {
            updateState(GROUP_STATUS, CHANNEL_LOCATION, new PointType(new DecimalType(location.latitude()),
                    new DecimalType(location.longitude()), new DecimalType(location.altitude())));
        }
    }

    private void updateState(final String group, final String channel, final State state) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), state);
    }

    private OpenClosedType toOpenClosed(final boolean value) {
        return value ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
    }

    private void createDynamicChannels() {
        final List<ChannelBuilder> newChannels;
        final String group;
        final var vehicle = this.vehicle;
        if (vehicle == null) {
            return;
        }
        if (vehicle.electric()) {
            group = GROUP_CHARGING;
            newChannels = List.of(
                    buildChannel(GROUP_CHARGING, CHANNEL_EV_BATTERY_SOC, NUMBER_DIMENSIONLESS, CHANNEL_TYPE_EV_SOC),
                    buildChannel(GROUP_CHARGING, CHANNEL_EV_CHARGING, SWITCH, CHANNEL_TYPE_EV_CHARGING),
                    buildChannel(GROUP_CHARGING, CHANNEL_EV_PLUGGED_IN, SWITCH, CHANNEL_TYPE_EV_PLUGGED_IN),
                    buildChannel(GROUP_CHARGING, CHANNEL_CHARGE_LIMIT_DC, NUMBER_DIMENSIONLESS,
                            CHANNEL_TYPE_CHARGE_LIMIT_DC),
                    buildChannel(GROUP_CHARGING, CHANNEL_CHARGE_LIMIT_AC, NUMBER_DIMENSIONLESS,
                            CHANNEL_TYPE_CHARGE_LIMIT_AC),
                    buildChannel(GROUP_CHARGING, CHANNEL_TIME_TO_FULL_CURRENT, NUMBER_TIME,
                            CHANNEL_TYPE_CHARGE_TIME_CURRENT),
                    buildChannel(GROUP_CHARGING, CHANNEL_TIME_TO_FULL_FAST, NUMBER_TIME, CHANNEL_TYPE_CHARGE_TIME_FAST),
                    buildChannel(GROUP_CHARGING, CHANNEL_TIME_TO_FULL_PORTABLE, NUMBER_TIME,
                            CHANNEL_TYPE_CHARGE_TIME_PORTABLE),
                    buildChannel(GROUP_CHARGING, CHANNEL_TIME_TO_FULL_STATION, NUMBER_TIME,
                            CHANNEL_TYPE_CHARGE_TIME_STATION));
        } else {
            group = GROUP_FUEL;
            newChannels = List.of(
                    buildChannel(GROUP_FUEL, CHANNEL_FUEL_LEVEL, NUMBER_DIMENSIONLESS, CHANNEL_TYPE_FUEL_LEVEL),
                    buildChannel(GROUP_FUEL, CHANNEL_FUEL_LOW_WARNING, SWITCH, CHANNEL_TYPE_FUEL_WARNING));
        }
        final Set<ChannelUID> currentChannels = getThing().getChannelsOfGroup(group).stream().map(Channel::getUID)
                .collect(toUnmodifiableSet());
        final ThingBuilder thingBuilder = editThing();
        newChannels.stream().map(ChannelBuilder::build).filter(c -> !currentChannels.contains(c.getUID()))
                .forEach(thingBuilder::withChannel);
        updateThing(thingBuilder.build());
    }

    private ChannelBuilder buildChannel(final String group, final String channelId, final String itemType,
            final String channelTypeId) {
        final ChannelUID channelUID = new ChannelUID(getThing().getUID(), group, channelId);
        final ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channelTypeId);
        return ChannelBuilder.create(channelUID, itemType).withType(channelTypeUID);
    }

    private @Nullable BluelinkAccountHandler getBridgeHandler() {
        final var bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        return (BluelinkAccountHandler) bridge.getHandler();
    }

    /**
     * Reschedule the forced refresh so the first execution starts in 10 seconds.
     */
    private void scheduleForceRefresh() {
        final ScheduledFuture<?> job = forceRefreshJob;
        final Duration forceRefreshInterval = this.forceRefreshInterval;
        if (job == null || forceRefreshInterval == null) {
            return;
        }
        job.cancel(false);
        scheduler.scheduleWithFixedDelay(() -> refreshVehicleStatus(true), 10, forceRefreshInterval.toSeconds(),
                TimeUnit.SECONDS);
    }
}
