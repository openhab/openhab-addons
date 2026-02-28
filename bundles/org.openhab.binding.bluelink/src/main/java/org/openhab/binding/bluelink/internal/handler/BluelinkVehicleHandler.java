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

import static java.time.format.DateTimeFormatter.ISO_ZONED_DATE_TIME;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.openhab.binding.bluelink.internal.BluelinkBindingConstants.*;
import static org.openhab.core.library.CoreItemFactory.SWITCH;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluelink.internal.api.BluelinkApiException;
import org.openhab.binding.bluelink.internal.config.BluelinkVehicleConfiguration;
import org.openhab.binding.bluelink.internal.dto.VehicleInfo;
import org.openhab.binding.bluelink.internal.dto.VehicleStatus;
import org.openhab.binding.bluelink.internal.dto.VehicleStatus.VehicleStatusData;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
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

    private volatile @Nullable ScheduledFuture<?> refreshJob;
    private volatile @Nullable ScheduledFuture<?> forceRefreshJob;
    private volatile @Nullable ScheduledFuture<?> initTask;
    private volatile @Nullable VehicleInfo vehicle;
    private @Nullable Duration forceRefreshInterval;

    public BluelinkVehicleHandler(final Thing thing) {
        super(thing);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(VehicleActions.class);
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
                updateProperty(PROPERTY_MODEL, v.modelCode());
                updateProperty(PROPERTY_ENGINE_TYPE, v.evStatus());
                createDynamicChannels();
                updateStatus(ThingStatus.ONLINE);
            }, () -> updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/vehicle-handler.initialize.vehicle-not-found")

            );
        } catch (final BluelinkApiException e) {
            logger.debug("error loading vehicle: {}", e.getMessage());
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
        final VehicleInfo vehicle = this.vehicle;
        final var bridgeHnd = getBridgeHandler();
        if (vehicle == null || bridgeHnd == null) {
            return false;
        }

        return bridgeHnd.climateStart(vehicle, temperature, heat, defrost);
    }

    public boolean climateStop() throws BluelinkApiException {
        final VehicleInfo vehicle = this.vehicle;
        final var bridgeHnd = getBridgeHandler();
        if (vehicle == null || bridgeHnd == null) {
            return false;
        }

        return bridgeHnd.climateStop(vehicle);
    }

    public void refreshVehicleStatus(boolean forceRefresh) {
        final VehicleInfo vehicle = this.vehicle;
        final var bridgeHnd = getBridgeHandler();
        if (vehicle == null || bridgeHnd == null) {
            return;
        }

        try {
            logger.debug("refreshing vehicle status");
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
        final VehicleStatusData data = status.vehicleStatus();
        final VehicleInfo info = vehicle;
        if (data == null || info == null) {
            return;
        }

        // Status group
        updateState(GROUP_STATUS, CHANNEL_LOCKED, OnOffType.from(data.doorLock()));
        updateState(GROUP_STATUS, CHANNEL_ENGINE_RUNNING, OnOffType.from(data.engine()));
        updateState(GROUP_STATUS, CHANNEL_ODOMETER, new QuantityType<>(info.odometer(), ImperialUnits.MILE));
        if (data.dateTime() != null) {
            try {
                final Instant lastUpdate = ISO_ZONED_DATE_TIME.parse(data.dateTime(), Instant::from);
                updateState(GROUP_STATUS, CHANNEL_LAST_UPDATE, new DateTimeType(lastUpdate));
            } catch (final DateTimeParseException e) {
                logger.warn("unexpected dateTime format: {}", data.dateTime());
            }
        }

        // 12V Battery
        if (data.battery() != null) {
            updateState(GROUP_STATUS, CHANNEL_BATTERY_LEVEL,
                    new QuantityType<>(data.battery().stateOfCharge(), Units.PERCENT));
        }

        // Doors
        final var doorOpen = data.doorOpen();
        if (doorOpen != null) {
            updateState(GROUP_DOORS, CHANNEL_DOOR_FRONT_LEFT, toOpenClosed(doorOpen.frontLeft()));
            updateState(GROUP_DOORS, CHANNEL_DOOR_FRONT_RIGHT, toOpenClosed(doorOpen.frontRight()));
            updateState(GROUP_DOORS, CHANNEL_DOOR_REAR_LEFT, toOpenClosed(doorOpen.backLeft()));
            updateState(GROUP_DOORS, CHANNEL_DOOR_REAR_RIGHT, toOpenClosed(doorOpen.backRight()));
        }
        updateState(GROUP_DOORS, CHANNEL_TRUNK, toOpenClosed(data.trunkOpen()));
        updateState(GROUP_DOORS, CHANNEL_HOOD, toOpenClosed(data.hoodOpen()));

        // Windows
        final var windowOpen = data.windowOpen();
        if (windowOpen != null) {
            updateState(GROUP_WINDOWS, CHANNEL_WINDOW_FRONT_LEFT, toOpenClosed(windowOpen.frontLeft()));
            updateState(GROUP_WINDOWS, CHANNEL_WINDOW_FRONT_RIGHT, toOpenClosed(windowOpen.frontRight()));
            updateState(GROUP_WINDOWS, CHANNEL_WINDOW_REAR_LEFT, toOpenClosed(windowOpen.backLeft()));
            updateState(GROUP_WINDOWS, CHANNEL_WINDOW_REAR_RIGHT, toOpenClosed(windowOpen.backRight()));
        }

        // Climate
        updateState(GROUP_CLIMATE, CHANNEL_HVAC_ON, OnOffType.from(data.airCtrlOn()));
        updateState(GROUP_CLIMATE, CHANNEL_DEFROST, OnOffType.from(data.defrost()));
        final var airTemp = data.airTemp();
        if (airTemp != null) {
            updateState(GROUP_CLIMATE, CHANNEL_TEMPERATURE, airTemp.getTemperature());
        }
        updateState(GROUP_CLIMATE, CHANNEL_STEERING_HEATER, OnOffType.from(data.steerWheelHeat() > 0));
        updateState(GROUP_CLIMATE, CHANNEL_REAR_WINDOW_HEATER, OnOffType.from(data.sideBackWindowHeat() > 0));
        updateState(GROUP_CLIMATE, CHANNEL_SIDE_MIRROR_HEATER, OnOffType.from(data.sideMirrorHeat() > 0));

        final var heater = data.seatHeaterVentState();
        if (heater != null) {
            updateState(GROUP_CLIMATE, CHANNEL_SEAT_FRONT_LEFT, new DecimalType(heater.frontLeft()));
            updateState(GROUP_CLIMATE, CHANNEL_SEAT_FRONT_RIGHT, new DecimalType(heater.frontRight()));
            updateState(GROUP_CLIMATE, CHANNEL_SEAT_REAR_LEFT, new DecimalType(heater.rearLeft()));
            updateState(GROUP_CLIMATE, CHANNEL_SEAT_REAR_RIGHT, new DecimalType(heater.rearRight()));
        }

        // EV-specific
        final var evStatus = data.evStatus();
        if (info.isElectric() && evStatus != null) {
            updateState(GROUP_CHARGING, CHANNEL_EV_BATTERY_SOC,
                    new QuantityType<>(evStatus.batteryStatus(), Units.PERCENT));
            updateState(GROUP_CHARGING, CHANNEL_EV_CHARGING, OnOffType.from(evStatus.batteryCharge()));
            updateState(GROUP_CHARGING, CHANNEL_EV_PLUGGED_IN, OnOffType.from(evStatus.batteryPlugin() > 0));

            // Driving ranges
            if (evStatus.drvDistance() != null && !evStatus.drvDistance().isEmpty()) {
                final var rangeByFuel = evStatus.drvDistance().getFirst().rangeByFuel();
                if (rangeByFuel != null) {
                    if (rangeByFuel.totalAvailableRange() != null) {
                        updateState(GROUP_RANGE, CHANNEL_TOTAL_RANGE, rangeByFuel.totalAvailableRange().getRange());
                    }
                    if (rangeByFuel.evModeRange() != null) {
                        updateState(GROUP_RANGE, CHANNEL_EV_RANGE, rangeByFuel.evModeRange().getRange());
                    }
                    if (rangeByFuel.gasModeRange() != null) {
                        updateState(GROUP_RANGE, CHANNEL_FUEL_RANGE, rangeByFuel.gasModeRange().getRange());
                    }
                }
            }

            // Charge limits
            final var reservChargeInfos = evStatus.reservChargeInfos();
            if (reservChargeInfos != null && reservChargeInfos.targetSocList() != null) {
                for (final VehicleStatus.TargetSOC target : reservChargeInfos.targetSocList()) {
                    if (target.plugType() == 0) {
                        updateState(GROUP_CHARGING, CHANNEL_CHARGE_LIMIT_DC,
                                new QuantityType<>(target.targetSocLevel(), Units.PERCENT));
                    } else if (target.plugType() == 1) {
                        updateState(GROUP_CHARGING, CHANNEL_CHARGE_LIMIT_AC,
                                new QuantityType<>(target.targetSocLevel(), Units.PERCENT));
                    }
                }
            }

            // Charge times
            final var remainTime2 = evStatus.remainTime2();
            if (remainTime2 != null) {
                if (remainTime2.atc() != null) {
                    updateState(GROUP_CHARGING, CHANNEL_TIME_TO_FULL_CURRENT,
                            new QuantityType<>(remainTime2.atc().value(), Units.MINUTE));
                }
                if (remainTime2.etc1() != null) {
                    updateState(GROUP_CHARGING, CHANNEL_TIME_TO_FULL_FAST,
                            new QuantityType<>(remainTime2.etc1().value(), Units.MINUTE));
                }
                if (remainTime2.etc2() != null) {
                    updateState(GROUP_CHARGING, CHANNEL_TIME_TO_FULL_PORTABLE,
                            new QuantityType<>(remainTime2.etc2().value(), Units.MINUTE));
                }
                if (remainTime2.etc3() != null) {
                    updateState(GROUP_CHARGING, CHANNEL_TIME_TO_FULL_STATION,
                            new QuantityType<>(remainTime2.etc3().value(), Units.MINUTE));
                }
            }
        }

        // Fuel (ICE only)
        if (!info.isElectric()) {
            updateState(GROUP_FUEL, CHANNEL_FUEL_LEVEL, new QuantityType<>(data.fuelLevel(), Units.PERCENT));
            updateState(GROUP_FUEL, CHANNEL_FUEL_LOW_WARNING, OnOffType.from(data.lowFuelLight()));
            if (data.dte() != null) {
                updateState(GROUP_RANGE, CHANNEL_FUEL_RANGE,
                        new QuantityType<>(data.dte().value(), ImperialUnits.MILE));
            }
        }

        // Warnings
        updateState(GROUP_WARNINGS, CHANNEL_WASHER_FLUID_WARNING, OnOffType.from(data.washerFluidStatus()));
        updateState(GROUP_WARNINGS, CHANNEL_BRAKE_FLUID_WARNING, OnOffType.from(data.brakeOilStatus()));
        updateState(GROUP_WARNINGS, CHANNEL_SMART_KEY_BATTERY_WARNING, OnOffType.from(data.smartKeyBatteryWarning()));
        final var tirePressureWarning = data.tirePressureWarning();
        if (tirePressureWarning != null) {
            updateState(GROUP_WARNINGS, CHANNEL_TIRE_PRESSURE_WARNING, OnOffType.from(tirePressureWarning.all() > 0));
            updateState(GROUP_WARNINGS, CHANNEL_TIRE_PRESSURE_WARNING_FR,
                    OnOffType.from(tirePressureWarning.frontRight() > 0));
            updateState(GROUP_WARNINGS, CHANNEL_TIRE_PRESSURE_WARNING_FL,
                    OnOffType.from(tirePressureWarning.frontLeft() > 0));
            updateState(GROUP_WARNINGS, CHANNEL_TIRE_PRESSURE_WARNING_RR,
                    OnOffType.from(tirePressureWarning.rearRight() > 0));
            updateState(GROUP_WARNINGS, CHANNEL_TIRE_PRESSURE_WARNING_RL,
                    OnOffType.from(tirePressureWarning.rearLeft() > 0));
        }

        // Location
        final var location = data.vehicleLocation();
        if (location != null && location.coord() != null) {
            updateState(GROUP_STATUS, CHANNEL_LOCATION, new PointType(new DecimalType(location.coord().latitude()),
                    new DecimalType(location.coord().longitude()), new DecimalType(location.coord().altitude())));
        }
    }

    private void updateState(final String group, final String channel, final State state) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), state);
    }

    private OpenClosedType toOpenClosed(final int value) {
        return value > 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
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
        if (vehicle.isElectric()) {
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
