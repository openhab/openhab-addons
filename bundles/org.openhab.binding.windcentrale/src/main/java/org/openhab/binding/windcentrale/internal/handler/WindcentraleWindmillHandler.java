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
package org.openhab.binding.windcentrale.internal.handler;

import static org.openhab.binding.windcentrale.internal.WindcentraleBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.KILO;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.windcentrale.internal.api.WindcentraleAPI;
import org.openhab.binding.windcentrale.internal.config.WindmillConfiguration;
import org.openhab.binding.windcentrale.internal.dto.Windmill;
import org.openhab.binding.windcentrale.internal.dto.WindmillStatus;
import org.openhab.binding.windcentrale.internal.exception.FailedGettingDataException;
import org.openhab.binding.windcentrale.internal.exception.InvalidAccessTokenException;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link WindcentraleWindmillHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 * @author Wouter Born - Add null annotations
 * @author Wouter Born - Add support for new API with authentication
 */
@NonNullByDefault
public class WindcentraleWindmillHandler extends BaseThingHandler {

    private static final long CACHE_EXPIRY = TimeUnit.SECONDS.toMillis(5);

    private final Logger logger = LoggerFactory.getLogger(WindcentraleWindmillHandler.class);

    private @NonNullByDefault({}) WindmillConfiguration config;
    private @Nullable Windmill windmill;

    private @Nullable ScheduledFuture<?> pollingJob;

    private final ExpiringCache<@Nullable WindmillStatus> statusCache = new ExpiringCache<>(CACHE_EXPIRY, () -> {
        try {
            WindcentraleAPI api = getAPI();
            Windmill windmill = this.windmill;
            return api == null || windmill == null ? null : api.getLiveData(windmill);
        } catch (FailedGettingDataException | InvalidAccessTokenException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            return null;
        }
    });

    public WindcentraleWindmillHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Windcentrale handler '{}'", getThing().getUID());
        final ScheduledFuture<?> pollingJob = this.pollingJob;
        if (pollingJob != null) {
            pollingJob.cancel(true);
            this.pollingJob = null;
        }
    }

    protected @Nullable WindcentraleAPI getAPI() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return null;
        }
        WindcentraleAccountHandler accountHandler = ((WindcentraleAccountHandler) bridge.getHandler());
        return accountHandler == null ? null : accountHandler.getAPI();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            logger.debug("Refreshing {}", channelUID);
            updateData();
        } else {
            logger.debug("This binding is a read-only binding and cannot handle commands");
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Windcentrale handler '{}'", getThing().getUID());

        WindmillConfiguration config = getConfig().as(WindmillConfiguration.class);
        this.config = config;

        Windmill windmill = Windmill.fromName(config.name);
        this.windmill = windmill;

        if (windmill == null) {
            // only occurs when a mismatch is introduced between config parameter options and enum values
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "Invalid windmill name: " + config.name);
            return;
        }

        updateProperties(getWindmillProperties(windmill));
        updateStatus(ThingStatus.UNKNOWN);

        pollingJob = scheduler.scheduleWithFixedDelay(this::updateData, 0, config.refreshInterval, TimeUnit.SECONDS);
        logger.debug("Polling job scheduled to run every {} sec. for '{}'", config.refreshInterval,
                getThing().getUID());
    }

    public static Map<String, String> getWindmillProperties(Windmill windmill) {
        Map<String, String> properties = new HashMap<>();

        properties.put(Thing.PROPERTY_VENDOR, "Windcentrale");
        properties.put(Thing.PROPERTY_MODEL_ID, windmill.getType());
        properties.put(Thing.PROPERTY_SERIAL_NUMBER, Integer.toString(windmill.getId()));

        properties.put(PROPERTY_PROJECT_CODE, windmill.getProjectCode());
        properties.put(PROPERTY_TOTAL_SHARES, Integer.toString(windmill.getTotalShares()));
        properties.put(PROPERTY_BUILD_YEAR, Integer.toString(windmill.getBuildYear()));
        properties.put(PROPERTY_MUNICIPALITY, windmill.getMunicipality());
        properties.put(PROPERTY_PROVINCE, windmill.getProvince());
        properties.put(PROPERTY_COORDINATES, windmill.getCoordinates());
        properties.put(PROPERTY_DETAILS_URL, windmill.getDetailsUrl());

        return properties;
    }

    private double yearRuntimePercentage(double yearRuntime) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Amsterdam"));
        ZonedDateTime startOfThisYear = now.withDayOfMonth(1).withMonth(1).truncatedTo(ChronoUnit.DAYS);
        long hoursThisYear = Duration.between(startOfThisYear, now).toHours();
        // prevent divide by zero when the year has just started
        return 100 * (hoursThisYear > 0 ? yearRuntime / hoursThisYear : 1);
    }

    private synchronized void updateData() {
        logger.debug("Updating windmill data '{}'", getThing().getUID());

        WindmillStatus status = statusCache.getValue();
        if (status == null) {
            return;
        }

        logger.trace("Retrieved updated windmill status: {}", status);

        updateState(CHANNEL_ENERGY_TOTAL, new QuantityType<>(status.yearProduction, Units.KILOWATT_HOUR));
        updateState(CHANNEL_POWER_RELATIVE, new QuantityType<>(status.powerPercentage, Units.PERCENT));
        updateState(CHANNEL_POWER_SHARES, new QuantityType<>(
                new BigDecimal(status.powerPerShare).multiply(new BigDecimal(config.shares)), Units.WATT));
        updateState(CHANNEL_POWER_TOTAL, new QuantityType<>(status.power, KILO(Units.WATT)));
        updateState(CHANNEL_RUN_PERCENTAGE,
                status.yearRuntime >= 0 ? new QuantityType<>(yearRuntimePercentage(status.yearRuntime), Units.PERCENT)
                        : UnDefType.UNDEF);
        updateState(CHANNEL_RUN_TIME,
                status.yearRuntime >= 0 ? new QuantityType<>(new BigDecimal(status.yearRuntime), Units.HOUR)
                        : UnDefType.UNDEF);
        updateState(CHANNEL_WIND_DIRECTION, new StringType(status.windDirection));
        updateState(CHANNEL_WIND_SPEED, new DecimalType(status.windPower));
        updateState(CHANNEL_TIMESTAMP, new DateTimeType(status.timestamp));

        if (!ThingStatus.ONLINE.equals(getThing().getStatus())) {
            updateStatus(ThingStatus.ONLINE);
        }
    }
}
