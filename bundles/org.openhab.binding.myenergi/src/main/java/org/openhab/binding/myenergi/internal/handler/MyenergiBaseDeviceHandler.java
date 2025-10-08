/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.myenergi.internal.handler;

import static org.openhab.core.thing.ThingStatus.ONLINE;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Frequency;
import javax.measure.quantity.Power;
import javax.measure.quantity.Temperature;
import javax.measure.quantity.Time;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.myenergi.internal.MyenergiDeviceConfiguration;
import org.openhab.binding.myenergi.internal.exception.ApiException;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.config.core.status.ConfigStatusCallback;
import org.openhab.core.config.core.status.ConfigStatusSource;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MyenergiBaseDeviceHandler} is an abstract base class for any
 * MyEnergi things. It contains an expiring
 * cache which ensures thing properties are not unnecessarily updated.
 *
 * @author Rene Scherer - Initial Contribution
 */
@NonNullByDefault
public abstract class MyenergiBaseDeviceHandler extends BaseThingHandler implements ConfigStatusCallback {

    private static final int UPDATE_THING_CACHE_TIMEOUT = 3000; // 3 secs

    private final Logger logger = LoggerFactory.getLogger(MyenergiBaseDeviceHandler.class);
    private @Nullable ScheduledFuture<?> measurementPollingJob = null;

    protected long serialNumber;

    protected ExpiringCache<Integer> updateThingCache = new ExpiringCache<Integer>(UPDATE_THING_CACHE_TIMEOUT,
            this::refreshCache);

    public MyenergiBaseDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing MyEnergiBaseDeviceHandler");

        serialNumber = Long.parseLong(getThing().getUID().getId());

        MyenergiDeviceConfiguration config = getConfigAs(MyenergiDeviceConfiguration.class);
        if (config.refreshInterval < 10) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.conf-error-invalid-refresh-intervals");
            return;
        }

        ScheduledFuture<?> job = measurementPollingJob;
        if (job == null || job.isCancelled()) {
            measurementPollingJob = scheduler.scheduleWithFixedDelay(() -> {
                try {
                    refreshMeasurements();
                    updateThing();
                    if ((getThing().getStatus() == ThingStatus.OFFLINE) && (getThing().getStatusInfo()
                            .getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR)) {
                        // if previous status was COMMUNICATION_ERROR, we now reestablished the comms
                        updateStatus(ThingStatus.ONLINE);
                    }
                } catch (ApiException e) {
                    logger.warn("Exception from API - {}", getThing().getUID(), e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.comm-error-general");
                }
            }, config.refreshInterval, config.refreshInterval, TimeUnit.SECONDS);
            logger.debug("Device polling job every {} seconds", config.refreshInterval);
        }

        updateStatus(ONLINE);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> job = measurementPollingJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            measurementPollingJob = null;
            logger.debug("Stopped MyEnergi measurement job");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            updateThingCache.getValue();
        }
    }

    @Override
    public synchronized void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.info("bridgeStatusChanged for {}. Reseting handler", this.getThing().getUID());
        logger.info("bridgeStatusChanged to: {}", bridgeStatusInfo.getStatus().toString());
        logger.info("bridgeStatusChanged to: {}", bridgeStatusInfo.getStatus().toString());
        if (bridgeStatusInfo.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "@text/offline.bridge-offline");
            // this.dispose();
        } else {
            updateStatus(ThingStatus.ONLINE);
            // this.initialize();
        }
    }

    @Override
    public void configUpdated(@Nullable ConfigStatusSource configStatusSource) {
        logger.debug("Configuration has been updated for {}", serialNumber);
    }

    @Override
    public void updateProperties(@Nullable Map<String, String> properties) {
        logger.debug("Updating thing properties");
        super.updateProperties(properties);
    }

    protected synchronized @Nullable MyenergiBridgeHandler getBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        if (bridge.getHandler() instanceof MyenergiBridgeHandler bridgeHandler) {
            return bridgeHandler;
        }
        return null;
    }

    protected void updatePowerState(final String channelId, @Nullable Integer value, Unit<Power> unit) {
        QuantityType<Power> quantity;
        if (value != null) {
            quantity = new QuantityType<>(value, unit);
        } else {
            quantity = new QuantityType<>(0, unit);
        }
        updateState(channelId, quantity);
    }

    protected void updateEnergyState(final String channelId, @Nullable Double value, Unit<Energy> unit) {
        QuantityType<Energy> quantity;
        if (value != null) {
            quantity = new QuantityType<>(value, unit);
        } else {
            quantity = new QuantityType<>(0, unit);
        }
        updateState(channelId, quantity);
    }

    protected void updateElectricPotentialState(final String channelId, @Nullable Float value,
            Unit<ElectricPotential> unit) {
        QuantityType<ElectricPotential> quantity;
        if (value != null) {
            quantity = new QuantityType<>(value, unit);
        } else {
            quantity = new QuantityType<>(0, unit);
        }
        updateState(channelId, quantity);
    }

    protected void updateFrequencyState(final String channelId, @Nullable Float value, Unit<Frequency> unit) {
        QuantityType<Frequency> quantity;
        if (value != null) {
            quantity = new QuantityType<>(value, unit);
        } else {
            quantity = new QuantityType<>(0, unit);
        }
        updateState(channelId, quantity);
    }

    protected void updateTemperatureState(final String channelId, @Nullable Float value, Unit<Temperature> unit) {
        QuantityType<Temperature> quantity;
        if (value != null) {
            quantity = new QuantityType<>(value, unit);
        } else {
            quantity = new QuantityType<>(0, unit);
        }
        updateState(channelId, quantity);
    }

    protected void updateDurationState(final String channelId, @Nullable Integer hours, @Nullable Integer minutes) {
        if (hours != null && minutes != null) {
            updateState(channelId, new QuantityType<Time>(hours * 60 + minutes, Units.MINUTE));
        } else {
            updateState(channelId, new QuantityType<Time>(0, Units.MINUTE));
        }
    }

    protected void updateShortDurationState(final String channelId, @Nullable Integer seconds) {
        if (seconds != null) {
            updateState(channelId, new QuantityType<Time>(seconds, Units.SECOND));
        } else {
            updateState(channelId, new QuantityType<Time>(0, Units.SECOND));
        }
    }

    protected void updatePercentageState(final String channelId, @Nullable Integer percent) {
        if (percent != null) {
            updateState(channelId, new QuantityType<Dimensionless>(percent, Units.PERCENT));
        } else {
            updateState(channelId, new QuantityType<Dimensionless>(0, Units.PERCENT));
        }
    }

    protected void updateStringState(final String channelId, @Nullable String value) {
        if (value != null) {
            updateState(channelId, new StringType(value));
        }
    }

    protected void updateStringState(final String channelId, @Nullable Integer value) {
        if (value != null) {
            updateState(channelId, new StringType(value.toString()));
        }
    }

    protected void updateSwitchState(final String channelId, @Nullable String value) {
        if (value != null) {
            updateState(channelId, OnOffType.from(value));
        }
    }

    protected void updateSwitchState(final String channelId, boolean value) {
        updateState(channelId, value ? OnOffType.ON : OnOffType.OFF);
    }

    protected void updateIntegerState(final String channelId, @Nullable Integer value) {
        if (value != null) {
            updateState(channelId, new DecimalType(value));
        }
    }

    protected void updateLongState(final String channelId, @Nullable Long value) {
        if (value != null) {
            updateState(channelId, new DecimalType(value));
        }
    }

    protected void updateDateTimeState(final String channelId, @Nullable ZonedDateTime value) {
        if (value != null) {
            updateState(channelId, new DateTimeType(value));
        }
    }

    /**
     * Updates all channels of a thing.
     */
    protected abstract void updateThing();

    /**
     * Refreshes the cache data via the Api.
     */
    protected abstract void refreshMeasurements() throws ApiException;

    private Integer refreshCache() {
        logger.debug("cache has timed out, we refresh the values in the thing");
        updateThing();
        // we don't care about the cache content, we just return a zero
        return 0;
    }
}
