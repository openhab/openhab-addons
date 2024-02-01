/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
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
import org.openhab.binding.linky.internal.dto.IntervalReading;
import org.openhab.binding.linky.internal.dto.MeterReading;
import org.openhab.binding.linky.internal.dto.PrmInfo;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
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
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */

@NonNullByDefault
public class LinkyHandler extends BaseThingHandler {
    private static final int REFRESH_FIRST_HOUR_OF_DAY = 1;
    private static final int REFRESH_INTERVAL_IN_MIN = 120;

    private final Logger logger = LoggerFactory.getLogger(LinkyHandler.class);
    private final HttpClient httpClient;
    private final Gson gson;
    private final WeekFields weekFields;

    private final ExpiringDayCache<MeterReading> dailyConsumption;
    private final ExpiringDayCache<MeterReading> dailyConsumptionMaxPower;

    private @Nullable LinkyConfiguration config;

    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable EnedisHttpApi enedisApi;

    private final OAuthFactory oAuthFactory;

    private @NonNullByDefault({}) String prmId;
    private @NonNullByDefault({}) String userId;

    private enum Target {
        FIRST,
        LAST,
        ALL
    }

    public LinkyHandler(Thing thing, LocaleProvider localeProvider, Gson gson, HttpClient httpClient,
            OAuthFactory oAuthFactory) {
        super(thing);
        this.gson = gson;
        this.httpClient = httpClient;
        this.weekFields = WeekFields.of(localeProvider.getLocale());
        this.oAuthFactory = oAuthFactory;

        this.dailyConsumption = new ExpiringDayCache<>("dailyConsumption", REFRESH_FIRST_HOUR_OF_DAY, () -> {
            LocalDate today = LocalDate.now();
            MeterReading meterReading = getConsumptionData(today.minusDays(1095), today);
            meterReading = getMeterReadingAfterChecks(meterReading);
            if (meterReading != null) {
                logData(meterReading.dayValue, "Day", DateTimeFormatter.ISO_LOCAL_DATE, Target.ALL);
                logData(meterReading.weekValue, "Week", DateTimeFormatter.ISO_LOCAL_DATE_TIME, Target.ALL);
            }
            return meterReading;
        });

        // We request data for yesterday and the day before yesterday, even if the data for the day before yesterday
        // is not needed by the binding. This is only a workaround to an API bug that will return
        // INTERNAL_SERVER_ERROR rather than the expected data with a NaN value when the data for yesterday is not yet
        // available.
        // By requesting two days, the API is not failing and you get the expected NaN value for yesterday when the data
        // is not yet available.
        this.dailyConsumptionMaxPower = new ExpiringDayCache<>("dailyConsumptionMaxPower", REFRESH_FIRST_HOUR_OF_DAY,
                () -> {
                    LocalDate today = LocalDate.now();
                    MeterReading meterReading = getPowerData(today.minusDays(1095), today);
                    meterReading = getMeterReadingAfterChecks(meterReading);
                    if (meterReading != null) {
                        logData(meterReading.dayValue, "Day (peak)", DateTimeFormatter.ISO_LOCAL_DATE, Target.ALL);
                    }
                    return meterReading;
                });
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Linky handler.");
        updateStatus(ThingStatus.UNKNOWN);

        config = getConfigAs(LinkyConfiguration.class);
        if (config.seemsValid()) {
            enedisApi = new EnedisHttpApi(config, gson, httpClient);

            scheduler.submit(() -> {
                try {
                    enedisApi.initialize();
                    updateStatus(ThingStatus.ONLINE);

                    PrmInfo prmInfo = enedisApi.getPrmInfo();
                    updateProperties(Map.of(USER_ID, prmInfo.customerId, PUISSANCE,
                            prmInfo.contractInfo.subscribedPower, PRM_ID, prmInfo.prmId));

                    prmId = thing.getProperties().get(PRM_ID);
                    userId = thing.getProperties().get(USER_ID);

                    updateData();

                    disconnect();

                    final LocalDateTime now = LocalDateTime.now();
                    final LocalDateTime nextDayFirstTimeUpdate = now.plusDays(1).withHour(REFRESH_FIRST_HOUR_OF_DAY)
                            .truncatedTo(ChronoUnit.HOURS);

                    refreshJob = scheduler.scheduleWithFixedDelay(this::updateData,
                            ChronoUnit.MINUTES.between(now, nextDayFirstTimeUpdate) % REFRESH_INTERVAL_IN_MIN + 1,
                            REFRESH_INTERVAL_IN_MIN, TimeUnit.MINUTES);
                } catch (LinkyException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            });
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-mandatory-settings");
        }
    }

    /**
     * Request new data and updates channels
     */
    private synchronized void updateData() {
        boolean connectedBefore = isConnected();

        updateEnergyData();
        updatePowerData();

        String tempoData = getTempoData();

        // LinkedTreeMap<String, String> obj = gson.fromJson(tempoData, LinkedTreeMap.class);

        /*
         *
         *
         * ArrayList<Object> list = new ArrayList<Object>();
         * for (Object key : obj.keySet()) {
         * Object val = obj.get(key);
         *
         * Pair<String, String> keyValue = new ImmutablePair(key, val);
         * list.add(keyValue);
         * }
         */

        updateState(TEST_SELECT, new StringType(tempoData));

        if (!connectedBefore && isConnected()) {
            disconnect();
        }
    }

    private synchronized void updatePowerData() {
        dailyConsumptionMaxPower.getValue().ifPresentOrElse(values -> {
            int dSize = values.dayValue.length;

            updateVAChannel(PEAK_POWER_DAY_MINUS_1, values.dayValue[dSize - 1].value / 1000.00);
            updateState(PEAK_POWER_TS_DAY_MINUS_1,
                    new DateTimeType(values.dayValue[dSize - 1].date.atZone(ZoneId.systemDefault())));

            updateVAChannel(PEAK_POWER_DAY_MINUS_2, values.dayValue[dSize - 2].value / 1000.00);
            updateState(PEAK_POWER_TS_DAY_MINUS_2,
                    new DateTimeType(values.dayValue[dSize - 2].date.atZone(ZoneId.systemDefault())));

            updateVAChannel(PEAK_POWER_DAY_MINUS_3, values.dayValue[dSize - 3].value / 1000.00);
            updateState(PEAK_POWER_TS_DAY_MINUS_3,
                    new DateTimeType(values.dayValue[dSize - 3].date.atZone(ZoneId.systemDefault())));

        }, () -> {
            updateKwhChannel(PEAK_POWER_DAY_MINUS_1, Double.NaN);
            updateState(PEAK_POWER_TS_DAY_MINUS_1, UnDefType.UNDEF);

            updateKwhChannel(PEAK_POWER_DAY_MINUS_2, Double.NaN);
            updateState(PEAK_POWER_TS_DAY_MINUS_2, UnDefType.UNDEF);

            updateKwhChannel(PEAK_POWER_DAY_MINUS_3, Double.NaN);
            updateState(PEAK_POWER_TS_DAY_MINUS_3, UnDefType.UNDEF);
        });
    }

    /**
     * Request new dayly/weekly data and updates channels
     */

    private synchronized void updateEnergyData() {
        dailyConsumption.getValue().ifPresentOrElse(values -> {
            int dSize = values.dayValue.length;
            updateKwhChannel(DAY_MINUS_1, values.dayValue[dSize - 1].value / 1000.00);
            updateKwhChannel(DAY_MINUS_2, values.dayValue[dSize - 2].value / 1000.00);
            updateKwhChannel(DAY_MINUS_3, values.dayValue[dSize - 3].value / 1000.00);

            LocalDate currentDt = LocalDate.now();
            int idxCurrentYear = currentDt.getYear() - values.dayValue[0].date.getYear();

            int currentWeek = ((currentDt.getDayOfYear() - 1) / 7) + 1;
            int currentMonth = currentDt.getMonthValue();

            int idxCurrentWeek = (52 * idxCurrentYear) + currentWeek;
            int idxCurrentMonth = (12 * idxCurrentYear) + currentMonth;

            updateKwhChannel(WEEK_MINUS_0, values.weekValue[idxCurrentWeek].value / 1000.00);
            updateKwhChannel(WEEK_MINUS_1, values.weekValue[idxCurrentWeek - 1].value / 1000.00);
            updateKwhChannel(WEEK_MINUS_2, values.weekValue[idxCurrentWeek - 2].value / 1000.00);

            updateKwhChannel(MONTH_MINUS_0, values.monthValue[idxCurrentMonth].value / 1000.00);
            updateKwhChannel(MONTH_MINUS_1, values.monthValue[idxCurrentMonth - 1].value / 1000.00);
            updateKwhChannel(MONTH_MINUS_2, values.monthValue[idxCurrentMonth - 2].value / 1000.00);

            updateKwhChannel(YEAR_MINUS_0, values.yearValue[idxCurrentYear].value / 1000.00);
            updateKwhChannel(YEAR_MINUS_1, values.yearValue[idxCurrentYear - 1].value / 1000.00);
            updateKwhChannel(YEAR_MINUS_2, values.yearValue[idxCurrentYear - 2].value / 1000.00);
        }, () -> {
            updateKwhChannel(DAY_MINUS_1, Double.NaN);
            updateKwhChannel(DAY_MINUS_2, Double.NaN);
            updateKwhChannel(DAY_MINUS_3, Double.NaN);

            updateKwhChannel(WEEK_MINUS_0, Double.NaN);
            updateKwhChannel(WEEK_MINUS_1, Double.NaN);
            updateKwhChannel(WEEK_MINUS_2, Double.NaN);

            updateKwhChannel(MONTH_MINUS_0, Double.NaN);
            updateKwhChannel(MONTH_MINUS_1, Double.NaN);
            updateKwhChannel(MONTH_MINUS_2, Double.NaN);

            updateKwhChannel(YEAR_MINUS_0, Double.NaN);
            updateKwhChannel(YEAR_MINUS_1, Double.NaN);
            updateKwhChannel(YEAR_MINUS_2, Double.NaN);
        });
    }

    private void updateKwhChannel(String channelId, double consumption) {
        logger.debug("Update channel {} with {}", channelId, consumption);
        updateState(channelId,
                Double.isNaN(consumption) ? UnDefType.UNDEF : new QuantityType<>(consumption, Units.KILOWATT_HOUR));
    }

    private void updateVAChannel(String channelId, double power) {
        logger.debug("Update channel {} with {}", channelId, power);
        updateState(channelId, Double.isNaN(power) ? UnDefType.UNDEF : new QuantityType<>(power, Units.VOLT_AMPERE));
    }

    /**
     * Produce a report of all daily values between two dates
     *
     * @param startDay the start day of the report
     * @param endDay the end day of the report
     * @param separator the separator to be used betwwen the date and the value
     *
     * @return the report as a list of string
     */

    public synchronized List<String> reportValues(LocalDate startDay, LocalDate endDay, @Nullable String separator) {
        List<String> report = buildReport(startDay, endDay, separator);
        disconnect();
        return report;
    }

    private List<String> buildReport(LocalDate startDay, LocalDate endDay, @Nullable String separator) {
        List<String> report = new ArrayList<>();
        if (startDay.getYear() == endDay.getYear() && startDay.getMonthValue() == endDay.getMonthValue()) {
            // All values in the same month
            MeterReading meterReading = getConsumptionData(startDay, endDay.plusDays(1));
            if (meterReading != null) {

                IntervalReading[] days = meterReading.dayValue;

                int size = days.length;

                for (int i = 0; i < size; i++) {
                    double consumption = days[i].value;
                    String line = days[i].date.format(DateTimeFormatter.ISO_LOCAL_DATE) + separator;
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
                report.addAll(buildReport(first, last, separator));
                first = last.plusDays(1);
            } while (!first.isAfter(endDay));
        }
        return report;
    }

    private @Nullable MeterReading getConsumptionData(LocalDate from, LocalDate to) {
        logger.debug("getConsumptionData from {} to {}", from.format(DateTimeFormatter.ISO_LOCAL_DATE),
                to.format(DateTimeFormatter.ISO_LOCAL_DATE));

        EnedisHttpApi api = this.enedisApi;
        if (api != null) {
            try {
                MeterReading meterReading = api.getEnergyData(userId, prmId, from, to);
                updateStatus(ThingStatus.ONLINE);
                return meterReading;
            } catch (LinkyException e) {
                logger.debug("Exception when getting consumption data: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }
        return null;
    }

    private @Nullable MeterReading getPowerData(LocalDate from, LocalDate to) {
        logger.debug("getPowerData from {} to {}", from.format(DateTimeFormatter.ISO_LOCAL_DATE),
                to.format(DateTimeFormatter.ISO_LOCAL_DATE));

        EnedisHttpApi api = this.enedisApi;
        if (api != null) {
            try {
                MeterReading meterReading = api.getPowerData(userId, prmId, from, to);
                updateStatus(ThingStatus.ONLINE);
                return meterReading;
            } catch (LinkyException e) {
                logger.debug("Exception when getting power data: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }
        return null;
    }

    private @Nullable String getTempoData() {
        logger.debug("getTempoData from");

        EnedisHttpApi api = this.enedisApi;
        if (api != null) {
            try {
                String result = api.getTempoData();
                updateStatus(ThingStatus.ONLINE);
                return result;
            } catch (LinkyException e) {
                logger.debug("Exception when getting power data: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }
        return null;
    }

    private boolean isConnected() {
        EnedisHttpApi api = this.enedisApi;
        return api == null ? false : api.isConnected();
    }

    private void disconnect() {
        EnedisHttpApi api = this.enedisApi;
        if (api != null) {
            try {
                api.dispose();
            } catch (LinkyException e) {
                logger.debug("disconnect: {}", e.getMessage());
            }
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Linky handler.");
        ScheduledFuture<?> job = this.refreshJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            refreshJob = null;
        }
        disconnect();
        enedisApi = null;
    }

    @Override
    public synchronized void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refreshing channel {}", channelUID.getId());
            boolean connectedBefore = isConnected();

            updateEnergyData();

            if (!connectedBefore && isConnected()) {
                disconnect();
            }
        } else {
            logger.debug("The Linky binding is read-only and can not handle command {}", command);
        }
    }

    private @Nullable MeterReading getMeterReadingAfterChecks(@Nullable MeterReading meterReading) {
        try {
            checkData(meterReading);
        } catch (LinkyException e) {
            logger.debug("Consumption data: {}", e.getMessage());
            return null;
        }

        meterReading.weekValue = new IntervalReading[208];
        meterReading.monthValue = new IntervalReading[48];
        meterReading.yearValue = new IntervalReading[4];

        for (int idx = 0; idx < 208; idx++) {
            meterReading.weekValue[idx] = new IntervalReading();
        }
        for (int idx = 0; idx < 48; idx++) {
            meterReading.monthValue[idx] = new IntervalReading();
        }
        for (int idx = 0; idx < 4; idx++) {
            meterReading.yearValue[idx] = new IntervalReading();
        }

        int size = meterReading.dayValue.length;
        int baseYear = meterReading.dayValue[0].date.getYear();

        for (int idx = 0; idx < size; idx++) {
            IntervalReading ir = meterReading.dayValue[idx];
            LocalDateTime dt = ir.date;
            double value = ir.value;

            int idxYear = dt.getYear() - baseYear;

            int dayOfYear = dt.getDayOfYear();
            int week = ((dayOfYear - 1) / 7) + 1;
            int month = dt.getMonthValue();

            int idxMonth = (idxYear * 12) + month;
            int idxWeek = (idxYear * 52) + week;

            meterReading.weekValue[idxWeek].value += value;
            meterReading.monthValue[idxMonth].value += value;
            meterReading.yearValue[idxYear].value += value;
        }

        return meterReading;
    }

    private void checkData(@Nullable MeterReading meterReading) throws LinkyException {
        if (meterReading.dayValue.length == 0) {
            throw new LinkyException("Invalid meterReading data: no day period");
        }
        // if (meterReading.intervalReading.length != 1095) {
        // throw new LinkyException("Imcomplete meterReading data < 1095 days");
        // }
    }

    /*
     *
     * private boolean isDataFirstDayAvailable(Consumption consumption) {
     * Aggregate days = consumption.aggregats.days;
     * logData(days, "First day", false, DateTimeFormatter.ISO_LOCAL_DATE, Target.FIRST);
     * return days.datas != null && !days.datas.isEmpty() && !days.datas.get(0).isNaN();
     * }
     *
     * private boolean isDataLastDayAvailable(Consumption consumption) {
     * Aggregate days = consumption.aggregats.days;
     * logData(days, "Last day", false, DateTimeFormatter.ISO_LOCAL_DATE, Target.LAST);
     * return days.datas != null && !days.datas.isEmpty() && !days.datas.get(days.datas.size() - 1).isNaN();
     * }
     */

    private void logData(IntervalReading[] ivArray, String title, DateTimeFormatter dateTimeFormatter, Target target) {

        if (logger.isDebugEnabled()) {
            int size = ivArray.length;

            if (target == Target.FIRST) {
                if (size > 0) {
                    logData(ivArray, 0, title, dateTimeFormatter);
                }
            } else if (target == Target.LAST) {
                if (size > 0) {
                    logData(ivArray, size - 1, title, dateTimeFormatter);
                }
            } else {
                for (int i = 0; i < size; i++) {
                    logData(ivArray, i, title, dateTimeFormatter);
                }
            }
        }
    }

    private void logData(IntervalReading[] ivArray, int index, String title, DateTimeFormatter dateTimeFormatter) {
        IntervalReading iv = ivArray[index];
        logger.debug("{} {} value {}", title, iv.date.format(dateTimeFormatter), iv.value);
    }

    public void saveConfiguration(Configuration config) {
        updateConfiguration(config);
    }
}
