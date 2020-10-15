/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.openhab.binding.linky.internal.LinkyBindingConstants.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.linky.internal.LinkyConfiguration;
import org.openhab.binding.linky.internal.LinkyException;
import org.openhab.binding.linky.internal.api.EnedisHttpApi;
import org.openhab.binding.linky.internal.api.ExpiringDayCache;
import org.openhab.binding.linky.internal.dto.ConsumptionReport.Aggregate;
import org.openhab.binding.linky.internal.dto.ConsumptionReport.Consumption;
import org.openhab.binding.linky.internal.dto.PrmInfo;
import org.openhab.binding.linky.internal.dto.UserInfo;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SmartHomeUnits;
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

import com.google.gson.Gson;

/**
 * The {@link LinkyHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 */

@NonNullByDefault
public class LinkyHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(LinkyHandler.class);

    private static final int REFRESH_FIRST_HOUR_OF_DAY = 5;
    private static final int REFRESH_INTERVAL_IN_MIN = 360;

    private final HttpClient httpClient;
    private final Gson gson;

    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable EnedisHttpApi enedisApi;
    private final WeekFields weekFields;

    private final ExpiringDayCache<Consumption> cachedDaylyData;
    private final ExpiringDayCache<Consumption> cachedPowerData;
    private final ExpiringDayCache<Consumption> cachedMonthlyData;
    private final ExpiringDayCache<Consumption> cachedYearlyData;

    private @NonNullByDefault({}) String prmId;
    private @NonNullByDefault({}) String userId;

    public LinkyHandler(Thing thing, LocaleProvider localeProvider, Gson gson, HttpClient httpClient) {
        super(thing);
        this.gson = gson;
        this.httpClient = httpClient;

        this.weekFields = WeekFields.of(localeProvider.getLocale());

        this.cachedDaylyData = new ExpiringDayCache<>("daily cache", REFRESH_FIRST_HOUR_OF_DAY, () -> {
            LocalDate today = LocalDate.now();
            return getConsumptionData(today.minusDays(13), today);
        });

        this.cachedPowerData = new ExpiringDayCache<>("power cache", REFRESH_FIRST_HOUR_OF_DAY, () -> {
            LocalDate to = LocalDate.now().plusDays(1);
            LocalDate from = to.minusDays(2);
            return getPowerData(from, to);
        });

        this.cachedMonthlyData = new ExpiringDayCache<>("monthly cache", REFRESH_FIRST_HOUR_OF_DAY, () -> {
            LocalDate today = LocalDate.now();
            return getConsumptionData(today.withDayOfMonth(1).minusMonths(1), today);
        });

        this.cachedYearlyData = new ExpiringDayCache<>("yearly cache", REFRESH_FIRST_HOUR_OF_DAY, () -> {
            LocalDate today = LocalDate.now();
            return getConsumptionData(LocalDate.of(today.getYear() - 1, 1, 1), today);
        });
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Linky handler.");
        updateStatus(ThingStatus.UNKNOWN);

        LinkyConfiguration config = getConfigAs(LinkyConfiguration.class);
        enedisApi = new EnedisHttpApi(config, gson, httpClient);

        try {
            enedisApi.initialize();
            updateStatus(ThingStatus.ONLINE);

            if (thing.getProperties().isEmpty()) {
                Map<String, String> properties = discoverAttributes();
                updateProperties(properties);
            }

            prmId = thing.getProperties().get(PRM_ID);
            userId = thing.getProperties().get(USER_ID);

            final LocalDateTime now = LocalDateTime.now();
            final LocalDateTime nextDayFirstTimeUpdate = now.plusDays(1).withHour(REFRESH_FIRST_HOUR_OF_DAY)
                    .truncatedTo(ChronoUnit.HOURS);

            updateData();

            refreshJob = scheduler.scheduleWithFixedDelay(this::updateData,
                    ChronoUnit.MINUTES.between(now, nextDayFirstTimeUpdate) % REFRESH_INTERVAL_IN_MIN + 1,
                    REFRESH_INTERVAL_IN_MIN, TimeUnit.MINUTES);

        } catch (LinkyException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private Map<String, String> discoverAttributes() throws LinkyException {
        Map<String, String> properties = new HashMap<>();
        EnedisHttpApi api = this.enedisApi;
        if (api != null) {
            PrmInfo prmInfo = api.getPrmInfo();
            UserInfo userInfo = api.getUserInfo();
            properties.put(USER_ID, userInfo.userProperties.internId);
            properties.put(PUISSANCE, prmInfo.puissanceSouscrite + " kVA");
            properties.put(PRM_ID, prmInfo.prmId);
        }

        return properties;
    }

    /**
     * Request new data and updates channels
     */
    private void updateData() {
        updatePowerData();
        updateDailyData();
        updateMonthlyData();
        updateYearlyData();
    }

    private synchronized void updatePowerData() {
        if (isLinked(PEAK_POWER) || isLinked(PEAK_TIMESTAMP)) {
            Consumption result = cachedPowerData.getValue();
            if (result != null) {
                updateVAChannel(PEAK_POWER, result.aggregats.days.datas.get(0));
                updateState(PEAK_TIMESTAMP, new DateTimeType(result.aggregats.days.periodes.get(0).dateDebut));
            }
        }
    }

    /**
     * Request new dayly/weekly data and updates channels
     */
    private synchronized void updateDailyData() {
        if (isLinked(YESTERDAY) || isLinked(LAST_WEEK) || isLinked(THIS_WEEK)) {
            Consumption result = cachedDaylyData.getValue();
            if (result != null) {
                Aggregate days = result.aggregats.days;

                int maxValue = days.periodes.size() - 1;
                int thisWeekNumber = days.periodes.get(maxValue).dateDebut.get(weekFields.weekOfWeekBasedYear());
                double yesterday = days.datas.get(maxValue);
                double lastWeek = 0.0;
                double thisWeek = 0.0;

                for (int i = maxValue; i >= 0; i--) {
                    int weekNumber = days.periodes.get(i).dateDebut.get(weekFields.weekOfWeekBasedYear());
                    if (weekNumber == thisWeekNumber) {
                        thisWeek += days.datas.get(i);
                    } else if (weekNumber == thisWeekNumber - 1) {
                        lastWeek += days.datas.get(i);
                    } else {
                        break;
                    }
                }

                updateKwhChannel(YESTERDAY, yesterday);
                updateKwhChannel(THIS_WEEK, thisWeek);
                updateKwhChannel(LAST_WEEK, lastWeek);
            }
        }
    }

    /**
     * Request new monthly data and updates channels
     */
    private synchronized void updateMonthlyData() {
        if (isLinked(LAST_MONTH) || isLinked(THIS_MONTH)) {
            Consumption result = cachedMonthlyData.getValue();
            if (result != null) {
                Aggregate months = result.aggregats.months;
                updateKwhChannel(LAST_MONTH, months.datas.get(0));
                updateKwhChannel(THIS_MONTH, months.datas.get(1));
            }
        }
    }

    /**
     * Request new yearly data and updates channels
     */
    private synchronized void updateYearlyData() {
        if (isLinked(LAST_YEAR) || isLinked(THIS_YEAR)) {
            Consumption result = cachedYearlyData.getValue();
            if (result != null) {
                Aggregate years = result.aggregats.years;
                updateKwhChannel(LAST_YEAR, years.datas.get(0));
                updateKwhChannel(THIS_YEAR, years.datas.get(1));
            }
        }
    }

    private void updateKwhChannel(String channelId, double consumption) {
        logger.debug("Update channel {} with {}", channelId, consumption);
        updateState(channelId,
                !Double.isNaN(consumption) ? new QuantityType<>(consumption, SmartHomeUnits.KILOWATT_HOUR)
                        : UnDefType.UNDEF);
    }

    private void updateVAChannel(String channelId, double power) {
        logger.debug("Update channel {} with {}", channelId, power);
        updateState(channelId,
                !Double.isNaN(power) ? new QuantityType<>(power, SmartHomeUnits.VOLT_AMPERE) : UnDefType.UNDEF);
    }

    /**
     * Produce a report of all daily values between two dates
     *
     * @param startDay the start day of the report
     * @param endDay the end day of the report
     * @param separator the separator to be used betwwen the date and the value
     *
     * @return the report as a string
     */
    public List<String> reportValues(LocalDate startDay, LocalDate endDay, @Nullable String separator) {
        List<String> report = new ArrayList<>();
        if (startDay.getYear() == endDay.getYear() && startDay.getMonthValue() == endDay.getMonthValue()) {
            // All values in the same month
            Consumption result = getConsumptionData(startDay, endDay);
            if (result != null) {
                Aggregate days = result.aggregats.days;
                for (int i = 0; i < days.datas.size(); i++) {
                    double consumption = days.datas.get(i);
                    String line = days.periodes.get(i).dateDebut.format(DateTimeFormatter.ISO_LOCAL_DATE) + separator;
                    if (consumption >= 0) {
                        line += String.valueOf(consumption);
                    }
                    report.add(line);
                }
            } else {
                LocalDate currentDay = startDay;
                while (!currentDay.isAfter(endDay)) {
                    report.add(currentDay.format(DateTimeFormatter.ISO_LOCAL_DATE) + separator);
                    currentDay = currentDay.plusDays(1);
                }
            }
        } else {
            // Concatenate the report produced for each month between the two dates
            LocalDate first = startDay;
            do {
                LocalDate last = first.withDayOfMonth(first.lengthOfMonth());
                if (last.isAfter(endDay)) {
                    last = endDay;
                }
                report.addAll(reportValues(first, last, separator));
                first = last.plusDays(1);
            } while (!first.isAfter(endDay));
        }
        return report;
    }

    private @Nullable Consumption getConsumptionData(LocalDate from, LocalDate to) {
        EnedisHttpApi api = this.enedisApi;
        if (api != null) {
            try {
                return api.getEnergyData(userId, prmId, from, to);
            } catch (LinkyException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }
        return null;
    }

    private @Nullable Consumption getPowerData(LocalDate from, LocalDate to) {
        EnedisHttpApi api = this.enedisApi;
        if (api != null) {
            try {
                return api.getPowerData(userId, prmId, from, to);
            } catch (LinkyException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }
        return null;
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Linky handler.");
        ScheduledFuture<?> job = this.refreshJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            refreshJob = null;
        }
        EnedisHttpApi api = this.enedisApi;
        if (api != null) {
            try {
                api.dispose();
                enedisApi = null;
            } catch (LinkyException ignore) {
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refreshing channel {}", channelUID.getId());
            switch (channelUID.getId()) {
                case YESTERDAY:
                case LAST_WEEK:
                case THIS_WEEK:
                    updateDailyData();
                    break;
                case LAST_MONTH:
                case THIS_MONTH:
                    updateMonthlyData();
                    break;
                case LAST_YEAR:
                case THIS_YEAR:
                    updateYearlyData();
                    break;
                default:
                    break;
            }
        } else {
            logger.debug("The Linky binding is read-only and can not handle command {}", command);
        }
    }
}
