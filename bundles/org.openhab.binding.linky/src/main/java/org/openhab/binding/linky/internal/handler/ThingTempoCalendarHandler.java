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
package org.openhab.binding.linky.internal.handler;

import static org.openhab.binding.linky.internal.constants.LinkyBindingConstants.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linky.internal.api.EnedisHttpApi;
import org.openhab.binding.linky.internal.api.ExpiringDayCache;
import org.openhab.binding.linky.internal.dto.ResponseTempo;
import org.openhab.binding.linky.internal.types.LinkyException;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.TimeSeries.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ThingTempoCalendarHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent Arnal - Initial contribution
 */

@NonNullByDefault
@SuppressWarnings("null")
public class ThingTempoCalendarHandler extends ThingBaseRemoteHandler {

    private static final Random RANDOM_NUMBERS = new Random();
    private static final int REFRESH_HOUR_OF_DAY = 1;
    private static final int REFRESH_MINUTE_OF_DAY = RANDOM_NUMBERS.nextInt(60);
    private static final int REFRESH_INTERVAL_IN_MIN = 120;

    private final Logger logger = LoggerFactory.getLogger(ThingTempoCalendarHandler.class);

    private final ExpiringDayCache<ResponseTempo> tempoInformation;

    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable EnedisHttpApi enedisApi;

    public String userId = "";

    private @Nullable ScheduledFuture<?> pollingJob = null;

    public ThingTempoCalendarHandler(Thing thing) {
        super(thing);

        // Read Tempo Information
        this.tempoInformation = new ExpiringDayCache<>("tempoInformation", REFRESH_HOUR_OF_DAY, REFRESH_MINUTE_OF_DAY,
                () -> {
                    LocalDate today = LocalDate.now();

                    ResponseTempo tempoData = getTempoData(today.minusDays(1095), today.plusDays(1));
                    return tempoData;
                });
    }

    @Override
    public synchronized void initialize() {
        logger.debug("Initializing Linky tempo handler");

        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }

        BridgeRemoteBaseHandler bridgeHandler = (BridgeRemoteBaseHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            return;
        }
        enedisApi = bridgeHandler.getEnedisApi();

        updateStatus(ThingStatus.UNKNOWN);

        pollingJob = scheduler.schedule(this::pollingCode, 5, TimeUnit.SECONDS);
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
    }

    public boolean supportNewApiFormat() throws LinkyException {
        Bridge bridge = getBridge();
        if (bridge == null) {
            throw new LinkyException("Unable to get bridge in supportNewApiFormat()");
        }

        BridgeRemoteBaseHandler bridgeHandler = (BridgeRemoteBaseHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            throw new LinkyException("Unable to get bridgeHandler in supportNewApiFormat()");
        }

        return bridgeHandler.supportNewApiFormat();
    }

    private void pollingCode() {
        try {
            EnedisHttpApi api = this.enedisApi;

            if (api != null) {
                Bridge lcBridge = getBridge();
                ScheduledFuture<?> lcPollingJob = pollingJob;

                if (lcBridge == null || lcBridge.getStatus() != ThingStatus.ONLINE) {
                    return;
                }

                BridgeRemoteBaseHandler bridgeHandler = (BridgeRemoteBaseHandler) lcBridge.getHandler();
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
        } catch (LinkyException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    /**
     * Request new data and updates channels
     */
    private synchronized void updateData() {
        // If one of the cache is expired, force also a metaData refresh to prevent 500 error from Enedis servers !
        logger.info("updateData() called");
        logger.info("updateTempoData() called");
        updateTempoTimeSeries();
    }

    private synchronized void updateTempoTimeSeries() {
        tempoInformation.getValue().ifPresentOrElse(values -> {
            TimeSeries timeSeries = new TimeSeries(Policy.REPLACE);

            values.forEach((k, v) -> {
                try {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = df.parse(k);
                    long epoch = date.getTime();
                    Instant timestamp = Instant.ofEpochMilli(epoch);

                    timeSeries.add(timestamp, new DecimalType(getTempoIdx(v)));
                } catch (ParseException ex) {
                }
            });

            int size = values.size();
            Object[] tempoValues = values.values().toArray();

            updateTempoChannel(LINKY_TEMPO_CALENDAR_GROUP, CHANNEL_TEMPO_TODAY_INFO,
                    getTempoIdx((String) tempoValues[size - 2]));
            updateTempoChannel(LINKY_TEMPO_CALENDAR_GROUP, CHANNEL_TEMPO_TOMORROW_INFO,
                    getTempoIdx((String) tempoValues[size - 1]));

            sendTimeSeries(LINKY_TEMPO_CALENDAR_GROUP, CHANNEL_TEMPO_TEMPO_INFO_TIME_SERIES, timeSeries);
            updateState(LINKY_TEMPO_CALENDAR_GROUP, CHANNEL_TEMPO_TEMPO_INFO_TIME_SERIES,
                    new DecimalType(getTempoIdx((String) tempoValues[size - 2])));
        }, () -> {
            updateTempoChannel(LINKY_TEMPO_CALENDAR_GROUP, CHANNEL_TEMPO_TODAY_INFO, -1);
            updateTempoChannel(LINKY_TEMPO_CALENDAR_GROUP, CHANNEL_TEMPO_TOMORROW_INFO, -1);
        });
    }

    private int getTempoIdx(String color) {
        int val = 0;
        if ("BLUE".equals(color)) {
            val = 0;
        }
        if ("WHITE".equals(color)) {
            val = 1;
        }
        if ("RED".equals(color)) {
            val = 2;
        }

        return val;
    }

    private void updateTempoChannel(String groupId, String channelId, int tempoValue) {
        logger.debug("Update channel ({}) {} with {}", config.prmId, channelId, tempoValue);
        updateState(groupId + "#" + channelId, new DecimalType(tempoValue));
    }

    protected void updateState(String groupId, String channelID, State state) {
        super.updateState(groupId + "#" + channelID, state);
    }

    protected void sendTimeSeries(String groupId, String channelID, TimeSeries timeSeries) {
        super.sendTimeSeries(groupId + "#" + channelID, timeSeries);
    }

    private @Nullable ResponseTempo getTempoData(LocalDate from, LocalDate to) {
        logger.debug("getTempoData from");

        EnedisHttpApi api = this.enedisApi;
        if (api != null) {
            try {
                ResponseTempo result = api.getTempoData(this, from, to);
                return result;
            } catch (LinkyException e) {
                logger.debug("Exception when getting tempo data: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Linky handler {}", config.prmId);
        ScheduledFuture<?> job = this.refreshJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            refreshJob = null;
        }

        ScheduledFuture<?> lcPollingJob = pollingJob;
        if (lcPollingJob != null) {
            lcPollingJob.cancel(true);
            pollingJob = null;
        }
        enedisApi = null;
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refreshing channel {} {}", config.prmId, channelUID.getId());
            updateData();
        } else {
            logger.debug("The Linky binding is read-only and can not handle command {}", command);
        }
    }
}
