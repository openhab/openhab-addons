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
package org.openhab.binding.renault.internal.handler;

import static org.openhab.binding.renault.internal.RenaultBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.KILO;
import static org.openhab.core.library.unit.SIUnits.METRE;
import static org.openhab.core.library.unit.Units.*;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.renault.internal.RenaultBindingConstants;
import org.openhab.binding.renault.internal.RenaultConfiguration;
import org.openhab.binding.renault.internal.api.Car;
import org.openhab.binding.renault.internal.api.Car.ChargingMode;
import org.openhab.binding.renault.internal.api.Car.LockStatus;
import org.openhab.binding.renault.internal.api.MyRenaultHttpSession;
import org.openhab.binding.renault.internal.api.exceptions.RenaultException;
import org.openhab.binding.renault.internal.api.exceptions.RenaultNotImplementedException;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
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
 * The {@link RenaultHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Doug Culnane - Initial contribution
 */
@NonNullByDefault
public class RenaultHandler extends BaseThingHandler {

    private interface RenaultRunner {
        void run() throws RenaultException, InterruptedException, ExecutionException, TimeoutException;
    }

    private interface RenaultConsumer<T> {
        void accept(T value) throws RenaultException, InterruptedException, ExecutionException, TimeoutException;
    }

    private static final Duration CACHE_INVALIDATION_TIMEOUT_SECONDS = Duration.ofSeconds(10);

    private final Logger logger = LoggerFactory.getLogger(RenaultHandler.class);

    private final ExpireCarCache carCache = new ExpireCarCache(this::refreshCar);
    private final Object lockObject = new Object();

    private final HttpClient httpClient;

    private RenaultConfiguration config = new RenaultConfiguration();

    private @Nullable ScheduledFuture<?> pollingJob;

    private @Nullable MyRenaultHttpSession httpSession;

    public RenaultHandler(Thing thing, HttpClient httpClient) {
        super(thing);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        // reset the car on initialize
        this.config = getConfigAs(RenaultConfiguration.class);

        // Validate configuration
        if (this.config.myRenaultUsername.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/thing-type.config.renault.car.error.username_empty");
            return;
        }
        if (this.config.myRenaultPassword.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/thing-type.config.renault.car.error.password_empty");
            return;
        }
        if (this.config.locale.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/thing-type.config.renault.car.error.location_empty");
            return;
        }
        if (this.config.vin.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/thing-type.config.renault.car.error.vin_empty");
            return;
        }
        if (this.config.refreshInterval < 1) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/thing-type.config.renault.car.error.refresh_interval_to_low");
            return;
        }
        updateStatus(ThingStatus.UNKNOWN);
        this.httpSession = new MyRenaultHttpSession(this.config, httpClient);
        reschedulePollingJob();
    }

    private void reschedulePollingJob() {
        synchronized (lockObject) {
            final ScheduledFuture<?> job = pollingJob;
            if (job != null) {
                job.cancel(true);
            }
            carCache.reset();
            pollingJob = scheduler.scheduleWithFixedDelay(carCache::getCar, 0, config.refreshInterval,
                    TimeUnit.MINUTES);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        final MyRenaultHttpSession httpSession = this.httpSession;
        if (httpSession == null) {
            return;
        }
        synchronized (lockObject) {
            final Car car = carCache.getCar();
            if (command instanceof RefreshType) {
                updateChannel(channelUID.getId(), car);
                return;
            }
            switch (channelUID.getId()) {
                case RenaultBindingConstants.CHANNEL_HVAC_TARGET_TEMPERATURE:
                    if (!car.isDisableHvac()) {
                        if (command instanceof DecimalType decimalCommand) {
                            car.setHvacTargetTemperature(decimalCommand.doubleValue());
                        } else if (command instanceof QuantityType) {
                            Optional.ofNullable(((QuantityType<Temperature>) command).toUnit(SIUnits.CELSIUS))
                                    .ifPresent(celsius -> car.setHvacTargetTemperature(celsius.doubleValue()));
                        }
                    }
                    break;
                case RenaultBindingConstants.CHANNEL_HVAC_STATUS:
                    if (!car.isDisableHvac()) {
                        if (command instanceof StringType && command.toString().equals(Car.HVAC_STATUS_ON)) {
                            // We can only trigger pre-conditioning of the car.
                            if (run(() -> {
                                updateState(CHANNEL_HVAC_STATUS, new StringType(Car.HVAC_STATUS_PENDING));
                                car.resetHVACStatus();
                                httpSession.initSesssion();
                                httpSession.actionHvacOn(car.getHvacTargetTemperature());
                            }, "Error during action HVAC on.", true)) {
                                refreshHvac(car);
                            }
                        }
                    }
                    break;
                case RenaultBindingConstants.CHANNEL_CHARGING_MODE:
                    if (command instanceof StringType) {
                        try {
                            final ChargingMode newMode = ChargingMode.valueOf(command.toString());
                            if ((newMode == ChargingMode.ALWAYS_CHARGING || newMode == ChargingMode.SCHEDULE_MODE)
                                    && run(() -> {
                                        httpSession.initSesssion();
                                        httpSession.actionChargeMode(newMode);
                                        car.setChargeMode(newMode);
                                    }, "Error during action set charge mode.", true)) {
                                refreshBattery(car);
                            }
                        } catch (IllegalArgumentException e) {
                            logger.warn("Invalid ChargingMode {}.", command.toString());
                            return;
                        }
                    }
                    break;
                case RenaultBindingConstants.CHANNEL_PAUSE:
                    if (command instanceof OnOffType) {
                        try {
                            if (run(() -> {
                                httpSession.initSesssion();
                                httpSession.actionPause(OnOffType.ON == command);
                            }, "Error during action set pause.", true)) {
                                refreshBattery(car);
                            }
                        } catch (IllegalArgumentException e) {
                            logger.warn("Invalid Pause Mode {}.", command.toString());
                            return;
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void dispose() {
        final ScheduledFuture<?> job = pollingJob;
        if (job != null) {
            job.cancel(true);
            pollingJob = null;
        }
        super.dispose();
    }

    private void refreshBattery(Car car) {
        runWithHttpSession(httpSession -> {
            updateBattery(httpSession, car);
            BATTERY_CHANNELS.forEach(c -> updateChannel(c, car));
        }, "Battery");
    }

    private void refreshHvac(Car car) {
        runWithHttpSession(httpSession -> {
            updateHvac(httpSession, car);
            HVAC_CHANNELS.forEach(c -> updateChannel(c, car));
        }, "HVAC.");
    }

    private Car refreshCar(Car car) {
        synchronized (lockObject) {
            runWithHttpSession(httpSession -> {
                httpSession.initSesssion();
                // Only get vehicle image once. Not other data from Vehicle is used.
                runIfNotDisabled(car.isDisableVehicle() || car.getImageURL() != null, () -> httpSession.getVehicle(car),
                        () -> car.setDisableVehicle(true), "imageURL");
                updateHvac(httpSession, car);
                runIfNotDisabled(car.isDisableLocation(), () -> httpSession.getLocation(car),
                        () -> car.setDisableLocation(true), "location");
                runIfNotDisabled(car.isDisableCockpit(), () -> httpSession.getCockpit(car),
                        () -> car.setDisableCockpit(true), "cockpit");
                updateBattery(httpSession, car);
                runIfNotDisabled(car.isDisableLockStatus(), () -> httpSession.getLockStatus(car),
                        () -> car.setDisableLockStatus(true), "lock");

                ALL_CHANNELS.forEach(c -> updateChannel(c, car));
                updateStatus(car);
            }, "all");
            return car;
        }
    }

    private void runWithHttpSession(RenaultConsumer<MyRenaultHttpSession> supplier, String type) {
        final MyRenaultHttpSession httpSession = this.httpSession;
        if (httpSession == null) {
            return;
        }
        logger.debug("Get Car {} data from cloud service and update channels.", type);
        run(() -> supplier.accept(httpSession), "Error My Renault Http Session.", true);
    }

    private void updateHvac(final MyRenaultHttpSession httpSession, Car car) {
        runIfNotDisabled(car.isDisableHvac(), () -> httpSession.getHvacStatus(car), () -> car.setDisableHvac(true),
                "HVAC");
    }

    private void updateBattery(final MyRenaultHttpSession httpSession, Car car) {
        runIfNotDisabled(car.isDisableBattery(), () -> httpSession.getBatteryStatus(car),
                () -> car.setDisableBattery(true), "battery");
    }

    private void updateStatus(Car car) {
        if (car.getImageURL() == null && car.isDisableHvac() && car.isDisableLocation() && car.isDisableCockpit()
                && car.isDisableBattery() && car.isDisableLockStatus()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/thing-type.renault.car.error.no_data");
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void updateChannel(String channelId, Car car) {
        if (CHANNEL_PAUSE.equals(channelId)) {
            return; // channel pause is command channel therefore no state set.
        }
        final @Nullable State state = switch (channelId) {
            case CHANNEL_IMAGE -> stringNotBlank(car.getImageURL());
            // ── Location ─────────────────────────────────────────────────────────
            case CHANNEL_LOCATION ->
                getIfNotDisabled(car.isDisableLocation(), () -> point(car.getGpsLatitude(), car.getGpsLongitude()));
            case CHANNEL_LOCATION_UPDATED ->
                getIfNotDisabled(car.isDisableLocation(), () -> dateTime(car.getLocationUpdated()));
            // ── Cockpit ──────────────────────────────────────────────────────────
            case CHANNEL_ODOMETER ->
                getIfNotDisabled(car.isDisableCockpit(), () -> quantity(car.getOdometer(), KILO(METRE)));
            // ── Lock ──────────────────────────────────────────────────────────
            case CHANNEL_LOCKED -> getIfNotDisabled(car.isDisableLockStatus(), () -> lock(car.getLockStatus()));
            // ── HVAC ─────────────────────────────────────────────────────────────
            case CHANNEL_HVAC_STATUS -> getIfNotDisabled(car.isDisableHvac(), () -> hvacStatus(car.getHvacstatus()));
            case CHANNEL_HVAC_TARGET_TEMPERATURE ->
                getIfNotDisabled(car.isDisableHvac(), () -> quantity(car.getHvacTargetTemperature(), SIUnits.CELSIUS));
            case CHANNEL_EXTERNAL_TEMPERATURE ->
                getIfNotDisabled(car.isDisableHvac(), () -> quantity(car.getExternalTemperature(), SIUnits.CELSIUS));
            // ── Battery ──────────────────────────────────────────────────────────
            case CHANNEL_PLUG_STATUS ->
                getIfNotDisabled(car.isDisableBattery(), () -> new StringType(car.getPlugStatus().name()));
            case CHANNEL_CHARGING_STATUS ->
                getIfNotDisabled(car.isDisableBattery(), () -> new StringType(car.getChargingStatus().name()));
            case CHANNEL_BATTERY_LEVEL ->
                getIfNotDisabled(car.isDisableBattery(), () -> decimal(car.getBatteryLevel()));
            case CHANNEL_ESTIMATED_RANGE ->
                getIfNotDisabled(car.isDisableBattery(), () -> quantity(car.getEstimatedRange(), KILO(METRE)));
            case CHANNEL_BATTERY_AVAILABLE_ENERGY -> getIfNotDisabled(car.isDisableBattery(),
                    () -> quantity(car.getBatteryAvailableEnergy(), KILOWATT_HOUR));
            case CHANNEL_CHARGING_REMAINING_TIME ->
                getIfNotDisabled(car.isDisableBattery(), () -> quantity(car.getChargingRemainingTime(), MINUTE));
            case CHANNEL_BATTERY_STATUS_UPDATED ->
                getIfNotDisabled(car.isDisableBattery(), () -> dateTime(car.getBatteryStatusUpdated()));
            case CHANNEL_CHARGING_MODE ->
                getIfNotDisabled(car.isDisableChargeMode(), () -> stringEnum(car.getChargingMode()));
            default -> null;
        };
        if (state == null) {
            logger.debug("updateChannel: unhandled channel '{}'", channelId);
        }
        updateState(channelId, state == null ? UnDefType.NULL : state);
    }

    private static State getIfNotDisabled(boolean disabled, Supplier<State> supplier) {
        return disabled ? UnDefType.UNDEF : supplier.get();
    }

    private State hvacStatus(@Nullable Boolean hvacStatus) {
        final String hvacString;
        if (hvacStatus == null) {
            hvacString = Car.HVAC_STATUS_PENDING;
        } else if (hvacStatus.booleanValue()) {
            hvacString = Car.HVAC_STATUS_ON;
        } else {
            hvacString = Car.HVAC_STATUS_OFF;
        }
        return new StringType(hvacString);
    }

    private static State lock(LockStatus lockStatus) {
        return switch (lockStatus) {
            case LOCKED -> OnOffType.ON;
            case UNLOCKED -> OnOffType.OFF;
            default -> UnDefType.UNDEF;
        };
    }

    private static State stringNotBlank(@Nullable String value) {
        return value == null || value.isBlank() ? UnDefType.UNDEF : new StringType(value);
    }

    private static State stringEnum(@Nullable Enum<?> value) {
        return value == null ? UnDefType.UNDEF : new StringType(value.name());
    }

    private static State point(@Nullable Double latitude, @Nullable Double longitude) {
        return latitude == null || longitude == null ? UnDefType.UNDEF
                : new PointType(new DecimalType(latitude.doubleValue()), new DecimalType(longitude.doubleValue()));
    }

    private static State dateTime(@Nullable ZonedDateTime value) {
        return value == null ? UnDefType.UNDEF : new DateTimeType(value);
    }

    private static <Q extends Quantity<Q>> State quantity(@Nullable Number value, Unit<Q> unit) {
        return value == null ? UnDefType.UNDEF : new QuantityType<>(value, unit);
    }

    private static State decimal(@Nullable Number value) {
        return value == null ? UnDefType.UNDEF : new DecimalType(value);
    }

    private void runIfNotDisabled(boolean disabled, RenaultRunner handler, Runnable disabler, String type) {
        if (disabled) {
            return;
        }
        run(() -> {
            try {
                handler.run();
            } catch (RenaultNotImplementedException e) {
                logger.debug("Disabling unsupported {} status update", type, e);
                disabler.run();
            }
        }, String.format("Error updating %s status.", type), false);
    }

    private boolean run(RenaultRunner handler, String logWarnMessage, boolean setOffline) {
        try {
            handler.run();
            return true;
        } catch (InterruptedException e) {
            logger.warn("Error My Renault Http Session.", e);
            Thread.currentThread().interrupt();
        } catch (RenaultException | ExecutionException | TimeoutException e) {
            logger.warn("{}", logWarnMessage, e);
            if (setOffline) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
        return false;
    }

    private static class ExpireCarCache {
        private final ExpiringCache<Car> carCache;
        private Car car = new Car();

        ExpireCarCache(Function<Car, Car> updateCar) {
            carCache = new ExpiringCache<>(CACHE_INVALIDATION_TIMEOUT_SECONDS, () -> updateCar.apply(car));
        }

        /**
         * Resets the Car object. Only call when binding (re)started.
         */
        public void reset() {
            car = new Car();
            carCache.invalidateValue();
        }

        /**
         * @return Returns an updated Car object when the expire timeout has been
         *         triggered.
         */
        public Car getCar() {
            return Optional.ofNullable(carCache.getValue()).orElseGet(() -> car);
        }
    }
}
