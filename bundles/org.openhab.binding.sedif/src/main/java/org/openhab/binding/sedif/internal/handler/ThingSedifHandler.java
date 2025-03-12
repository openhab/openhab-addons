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
package org.openhab.binding.sedif.internal.handler;

import static org.openhab.binding.sedif.internal.constants.SedifBindingConstants.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sedif.internal.api.ExpiringDayCache;
import org.openhab.binding.sedif.internal.api.SedifHttpApi;
import org.openhab.binding.sedif.internal.config.SedifConfiguration;
import org.openhab.binding.sedif.internal.dto.MeterReading;
import org.openhab.binding.sedif.internal.dto.MeterReading.Data.Consommation;
import org.openhab.binding.sedif.internal.types.SedifException;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.TimeSeries.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ThingSedifHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */

@NonNullByDefault
@SuppressWarnings("null")
public class ThingSedifHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ThingSedifHandler.class);
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable SedifHttpApi sedifApi;

    private static final Random RANDOM_NUMBERS = new Random();
    private static final int REFRESH_HOUR_OF_DAY = 1;
    private static final int REFRESH_MINUTE_OF_DAY = RANDOM_NUMBERS.nextInt(60);
    private static final int REFRESH_INTERVAL_IN_MIN = 120;

    private final ExpiringDayCache<MeterReading> dailyConsumption;

    private @Nullable ScheduledFuture<?> pollingJob = null;
    protected SedifConfiguration config;

    public ThingSedifHandler(Thing thing, LocaleProvider localeProvider, TimeZoneProvider timeZoneProvider) {
        super(thing);

        this.dailyConsumption = new ExpiringDayCache<MeterReading>("dailyConsumption", REFRESH_HOUR_OF_DAY,
                REFRESH_MINUTE_OF_DAY, () -> {
                    LocalDate today = LocalDate.now();
                    MeterReading meterReading = getConsumptionData(today.minusDays(1095), today);
                    return meterReading;
                });

        config = getConfigAs(SedifConfiguration.class);
    }

    @Override
    public synchronized void initialize() {
        updateStatus(ThingStatus.ONLINE);

        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }

        BridgeSedifWebHandler bridgeHandler = (BridgeSedifWebHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            return;
        }
        sedifApi = bridgeHandler.getSedifApi();

        if (!config.seemsValid()) {
            pollingJob = scheduler.schedule(this::pollingCode, 5, TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-mandatory-settings");
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    /**
     * Request new data and updates channels
     */
    private synchronized void updateData() {
        logger.info("updateEnergyData() called");
        updateConsumptionData();
    }

    /**
     * Request new daily/weekly data and updates channels
     */
    private synchronized void updateConsumptionData() {
        dailyConsumption.getValue().ifPresentOrElse(values -> {
            updateState(SEDIF_BASE_GROUP, CHANNEL_CONSUMPTION, new QuantityType<>(
                    values.data.consommation.get(values.data.consommation.size() - 1).consommation, Units.LITRE));

            updateConsumptionTimeSeries(SEDIF_BASE_GROUP, CHANNEL_CONSUMPTION, values);
        }, () -> {
            updateState(SEDIF_BASE_GROUP, CHANNEL_CONSUMPTION, new QuantityType<>(0.00, Units.LITRE));
        });
    }

    private @Nullable MeterReading getConsumptionData(LocalDate from, LocalDate to) {
        logger.debug("getConsumptionData for from {} to {}", from.format(DateTimeFormatter.ISO_LOCAL_DATE),
                to.format(DateTimeFormatter.ISO_LOCAL_DATE));

        SedifHttpApi api = this.sedifApi;
        if (api != null) {
            try {
                MeterReading meterReading = api.getConsumptionData(this, from, to);
                return meterReading;
            } catch (Exception e) {
                logger.debug("Exception when getting consumption data for : {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }

        return null;
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    private void pollingCode() {
        try {
            SedifHttpApi api = this.sedifApi;

            if (api != null) {
                Bridge lcBridge = getBridge();
                ScheduledFuture<?> lcPollingJob = pollingJob;

                if (lcBridge == null || lcBridge.getStatus() != ThingStatus.ONLINE) {
                    return;
                }

                BridgeSedifWebHandler bridgeHandler = (BridgeSedifWebHandler) lcBridge.getHandler();
                if (bridgeHandler == null) {
                    return;
                }

                if (!bridgeHandler.isConnected()) {
                    bridgeHandler.connectionInit();
                }

                updateData();

                final LocalDateTime now = LocalDateTime.now();
                final LocalDateTime nextDayFirstTimeUpdate = now.plusDays(1).withHour(REFRESH_HOUR_OF_DAY)
                        .withMinute(REFRESH_MINUTE_OF_DAY).truncatedTo(ChronoUnit.MINUTES);

                if (this.getThing().getStatusInfo().getStatusDetail() != ThingStatusDetail.COMMUNICATION_ERROR) {
                    updateStatus(ThingStatus.ONLINE);
                }

                if (lcPollingJob != null) {
                    lcPollingJob.cancel(false);
                    pollingJob = null;
                }

                refreshJob = scheduler.scheduleWithFixedDelay(this::updateData,
                        ChronoUnit.MINUTES.between(now, nextDayFirstTimeUpdate) % REFRESH_INTERVAL_IN_MIN + 1,
                        REFRESH_INTERVAL_IN_MIN, TimeUnit.MINUTES);
            }

        } catch (SedifException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    protected void updateState(String groupId, String channelID, State state) {
        super.updateState(groupId + "#" + channelID, state);
    }

    protected void sendTimeSeries(String groupId, String channelID, TimeSeries timeSeries) {
        super.sendTimeSeries(groupId + "#" + channelID, timeSeries);
    }

    private synchronized void updateConsumptionTimeSeries(String groupId, String channelId, MeterReading meterReading) {
        TimeSeries timeSeries = new TimeSeries(Policy.REPLACE);

        for (int i = 0; i < meterReading.data.consommation.size(); i++) {

            Consommation conso = meterReading.data.consommation.get(i);
            String date = conso.dateIndex;
            float consommation = conso.consommation;

            LocalDateTime dt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Instant timestamp = dt.toInstant(ZoneOffset.UTC);

            if (Double.isNaN(consommation)) {
                continue;
            }
            timeSeries.add(timestamp, new DecimalType(consommation));
        }

        sendTimeSeries(groupId, channelId, timeSeries);
    }

}
