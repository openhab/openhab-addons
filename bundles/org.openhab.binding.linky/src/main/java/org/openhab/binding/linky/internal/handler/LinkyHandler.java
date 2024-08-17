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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linky.internal.LinkyConfiguration;
import org.openhab.binding.linky.internal.LinkyException;
import org.openhab.binding.linky.internal.api.EnedisHttpApi;
import org.openhab.binding.linky.internal.api.ExpiringDayCache;
import org.openhab.binding.linky.internal.dto.IntervalReading;
import org.openhab.binding.linky.internal.dto.MeterReading;
import org.openhab.binding.linky.internal.dto.PrmInfo;
import org.openhab.binding.linky.internal.dto.TempoResponse;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
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
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.TimeSeries.Policy;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.extra.Months;
import org.threeten.extra.Weeks;
import org.threeten.extra.Years;

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

    private final ExpiringDayCache<MeterReading> dailyConsumption;
    private final ExpiringDayCache<MeterReading> dailyConsumptionMaxPower;
    private final ExpiringDayCache<TempoResponse> tempoInformation;

    private @Nullable ScheduledFuture<?> refreshJob;
    private LinkyConfiguration config;
    private @Nullable EnedisHttpApi enedisApi;
    private double divider = 1.00;

    private enum Target {
        FIRST,
        LAST,
        ALL
    }

    public LinkyHandler(Thing thing, LocaleProvider localeProvider) {
        super(thing);

        config = getConfigAs(LinkyConfiguration.class);

        this.dailyConsumption = new ExpiringDayCache<>("dailyConsumption", REFRESH_FIRST_HOUR_OF_DAY, () -> {
            LocalDate today = LocalDate.now();
            MeterReading meterReading = getConsumptionData(today.minusDays(1095), today);
            meterReading = getMeterReadingAfterChecks(meterReading);
            /*
             * if (consumption != null) {
             * logData(consumption.aggregats.days, "Day", false, DateTimeFormatter.ISO_LOCAL_DATE, Target.ALL);
             * logData(consumption.aggregats.weeks, "Week", true, DateTimeFormatter.ISO_LOCAL_DATE_TIME, Target.ALL);
             *
             * }
             */
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

        // Read Tempo Information
        this.tempoInformation = new ExpiringDayCache<>("tempoInformation", REFRESH_FIRST_HOUR_OF_DAY, () -> {
            LocalDate today = LocalDate.now();

            TempoResponse tempoData = getTempoData(today.minusDays(1095), today.plusDays(1));
            return tempoData;
        });
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Linky handler.");

        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }

        ApiBridgeHandler bridgeHandler = (ApiBridgeHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            return;
        }
        enedisApi = bridgeHandler.getEnedisApi();
        divider = bridgeHandler.getDivider();

        updateStatus(ThingStatus.UNKNOWN);

        if (config.seemsValid()) {
            scheduler.submit(() -> {
                try {

                    EnedisHttpApi api = this.enedisApi;

                    if (api != null) {
                        PrmInfo prmInfo = api.getPrmInfo(this, config.prmId);
                        updateProperties(Map.of(USER_ID, prmInfo.customerId, PUISSANCE,
                                prmInfo.contractInfo.subscribedPower, PRM_ID, prmInfo.prmId));

                        updateMetaData();
                        updateData();

                        disconnect();

                        final LocalDateTime now = LocalDateTime.now();
                        final LocalDateTime nextDayFirstTimeUpdate = now.plusDays(1).withHour(REFRESH_FIRST_HOUR_OF_DAY)
                                .truncatedTo(ChronoUnit.HOURS);

                        refreshJob = scheduler.scheduleWithFixedDelay(this::updateData,
                                ChronoUnit.MINUTES.between(now, nextDayFirstTimeUpdate) % REFRESH_INTERVAL_IN_MIN + 1,
                                REFRESH_INTERVAL_IN_MIN, TimeUnit.MINUTES);
                    }
                } catch (LinkyException e) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            });
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-mandatory-settings");
        }
    }

    public @Nullable LinkyConfiguration getLinkyConfig() {
        return config;
    }

    private synchronized void updateMetaData() {
        EnedisHttpApi api = this.enedisApi;

        if (api != null) {
            try {
                PrmInfo info = api.getPrmInfo(this, config.prmId);
                String title = info.identityInfo.title;
                String firstName = info.identityInfo.firstname;
                String lastName = info.identityInfo.lastname;

                updateState(MAIN_IDENTITY, new StringType(title + " " + firstName + " " + lastName));

                updateState(MAIN_CONTRACT_SEGMENT, new StringType(info.contractInfo.segment));
                updateState(MAIN_CONTRACT_CONTRACT_STATUS, new StringType(info.contractInfo.contractStatus));
                updateState(MAIN_CONTRACT_CONTRACT_TYPE, new StringType(info.contractInfo.contractType));
                updateState(MAIN_CONTRACT_DISTRIBUTION_TARIFF, new StringType(info.contractInfo.distributionTariff));
                updateState(MAIN_CONTRACT_LAST_ACTIVATION_DATE, new StringType(info.contractInfo.lastActivationDate));
                updateState(MAIN_CONTRACT_LAST_DISTRIBUTION_TARIFF_CHANGE_DATE,
                        new StringType(info.contractInfo.lastDistributionTariffChangeDate));
                updateState(MAIN_CONTRACT_OFF_PEAK_HOURS, new StringType(info.contractInfo.offpeakHours));
                updateState(MAIN_CONTRACT_SEGMENT, new StringType(info.contractInfo.segment));
                updateState(MAIN_CONTRACT_SUBSCRIBED_POWER, new StringType(info.contractInfo.subscribedPower));

                updateState(MAIN_USAGEPOINT_ID, new StringType(info.usagePointInfo.usagePointId));
                updateState(MAIN_USAGEPOINT_STATUS, new StringType(info.usagePointInfo.usagePointStatus));
                updateState(MAIN_USAGEPOINT_METER_TYPE, new StringType(info.usagePointInfo.meterType));

                updateState(MAIN_USAGEPOINT_METER_ADDRESS_CITY, new StringType(info.addressInfo.city));
                updateState(MAIN_USAGEPOINT_METER_ADDRESS_COUNTRY, new StringType(info.addressInfo.country));
                updateState(MAIN_USAGEPOINT_METER_ADDRESS_POSTAL_CODE, new StringType(info.addressInfo.postalCode));
                updateState(MAIN_USAGEPOINT_METER_ADDRESS_INSEE_CODE, new StringType(info.addressInfo.inseeCode));
                updateState(MAIN_USAGEPOINT_METER_ADDRESS_STREET, new StringType(info.addressInfo.street));

                updateState(MAIN_CONTACT_MAIL, new StringType(info.contactInfo.email));
                updateState(MAIN_CONTACT_PHONE, new StringType(info.contactInfo.phone));

            } catch (LinkyException e) {
                logger.debug("Exception when getting consumption data: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    /**
     * Request new data and updates channels
     */
    private synchronized void updateData() {
        boolean connectedBefore = isConnected();

        updateEnergyData();
        updatePowerData();
        updateTempoTimeSeries();

        if (!connectedBefore && isConnected()) {
            disconnect();
        }
    }

    private synchronized void updateTempoTimeSeries() {
        tempoInformation.getValue().ifPresentOrElse(values -> {
            LocalDate today = LocalDate.now();
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

            int tomorrowDayIdx = values.size();
            Object[] tempoValues = values.values().toArray();

            updateTempoChannel(TEMPO_TODAY_INFO, getTempoIdx((String) tempoValues[tomorrowDayIdx - 2]));
            updateTempoChannel(TEMPO_TOMORROW_INFO, getTempoIdx((String) tempoValues[tomorrowDayIdx - 1]));

            sendTimeSeries(TEMPO_TEMPO_INFO_TIME_SERIES, timeSeries);
            updateState(TEMPO_TEMPO_INFO_TIME_SERIES,
                    new DecimalType(getTempoIdx((String) tempoValues[tomorrowDayIdx - 2])));
        }, () -> {
            updateTempoChannel(TEMPO_TODAY_INFO, -1);
            updateTempoChannel(TEMPO_TOMORROW_INFO, -1);
        });
    }

    private int getTempoIdx(String color) {
        int val = 0;
        if (color.equals("BLUE")) {
            val = 0;
        }
        if (color.equals("WHITE")) {
            val = 1;
        }
        if (color.equals("RED")) {
            val = 2;
        }

        return val;
    }

    private synchronized void updatePowerData() {
        dailyConsumptionMaxPower.getValue().ifPresentOrElse(values -> {
            int dSize = values.dayValue.length;

            updateVAChannel(PEAK_POWER_DAY_MINUS_1, values.dayValue[dSize - 1].value);
            updateState(PEAK_POWER_TS_DAY_MINUS_1,
                    new DateTimeType(values.dayValue[dSize - 1].date.atZone(ZoneId.systemDefault())));

            updateVAChannel(PEAK_POWER_DAY_MINUS_2, values.dayValue[dSize - 2].value);
            updateState(PEAK_POWER_TS_DAY_MINUS_2,
                    new DateTimeType(values.dayValue[dSize - 2].date.atZone(ZoneId.systemDefault())));

            updateVAChannel(PEAK_POWER_DAY_MINUS_3, values.dayValue[dSize - 3].value);
            updateState(PEAK_POWER_TS_DAY_MINUS_3,
                    new DateTimeType(values.dayValue[dSize - 3].date.atZone(ZoneId.systemDefault())));

            updateMaxPowerTimeSeries(PEAK_POWER_DAILY, values.dayValue);
            updateMaxPowerTimeSeries(PEAK_POWER_WEEKLY, values.weekValue);
            updateMaxPowerTimeSeries(PEAK_POWER_MONTHLY, values.monthValue);
            updateMaxPowerTimeSeries(PEAK_POWER_YEARLY, values.yearValue);

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

            updateKwhChannel(DAY_MINUS_1, values.dayValue[dSize - 1].value);
            updateKwhChannel(DAY_MINUS_2, values.dayValue[dSize - 2].value);
            updateKwhChannel(DAY_MINUS_3, values.dayValue[dSize - 3].value);

            LocalDate currentDt = LocalDate.now();
            int idxCurrentYear = values.yearValue.length - 1;
            int idxCurrentWeek = values.weekValue.length - 1;
            int idxCurrentMonth = values.monthValue.length - 1;

            updateKwhChannel(WEEK_MINUS_0, values.weekValue[idxCurrentWeek].value);
            updateKwhChannel(WEEK_MINUS_1, values.weekValue[idxCurrentWeek - 1].value);
            updateKwhChannel(WEEK_MINUS_2, values.weekValue[idxCurrentWeek - 2].value);

            updateKwhChannel(MONTH_MINUS_0, values.monthValue[idxCurrentMonth].value);
            updateKwhChannel(MONTH_MINUS_1, values.monthValue[idxCurrentMonth - 1].value);
            updateKwhChannel(MONTH_MINUS_2, values.monthValue[idxCurrentMonth - 2].value);

            updateKwhChannel(YEAR_MINUS_0, values.yearValue[idxCurrentYear].value);
            updateKwhChannel(YEAR_MINUS_1, values.yearValue[idxCurrentYear - 1].value);
            updateKwhChannel(YEAR_MINUS_2, values.yearValue[idxCurrentYear - 2].value);

            updateConsumptionTimeSeries(DAYLY_CONSUMPTION, values.dayValue);
            updateConsumptionTimeSeries(WEEKLY_CONSUMPTION, values.weekValue);
            updateConsumptionTimeSeries(MONTHLY_CONSUMPTION, values.monthValue);
            updateConsumptionTimeSeries(YEARLY_CONSUMPTION, values.yearValue);
        }, () -> {
            updateKwhChannel(DAY_MINUS_1, Double.NaN);
            updateKwhChannel(DAY_MINUS_2, Double.NaN);
            updateKwhChannel(DAY_MINUS_3, Double.NaN);

                updateKwhChannel(THIS_YEAR, values.YearValue[idxCurrentYear].value / 1000.00);
                updateKwhChannel(LAST_YEAR, values.YearValue[idxPreviousYear].value / 1000.00);
            }, () -> {
                updateKwhChannel(YESTERDAY, Double.NaN);
                updateKwhChannel(THIS_WEEK, Double.NaN);
                updateKwhChannel(LAST_WEEK, Double.NaN);
            });
        }
    }

    private synchronized void updateMaxPowerTimeSeries(String channel, IntervalReading[] iv) {
        TimeSeries timeSeries = new TimeSeries(Policy.REPLACE);
        LocalDate today = LocalDate.now();

        for (int i = 0; i < iv.length; i++) {
            try {
                if (Double.isNaN(iv[i].value)) {
                    continue;
                }
                Instant timestamp = iv[i].date.toInstant(ZoneOffset.UTC);
                timeSeries.add(timestamp, new DecimalType(iv[i].value));
            } catch (Exception ex) {
                logger.debug("aa");
            }
        }

        sendTimeSeries(channel, timeSeries);
        updateState(channel, new DecimalType(iv[iv.length - 1].value));
    }

    private synchronized void updateConsumptionTimeSeries(String channel, IntervalReading[] iv) {
        TimeSeries timeSeries = new TimeSeries(Policy.REPLACE);
        LocalDate today = LocalDate.now();

        for (int i = 0; i < iv.length; i++) {
            if (iv[i].date == null) {
                continue;
            }

            Instant timestamp = iv[i].date.toInstant(ZoneOffset.UTC);
            timeSeries.add(timestamp, new DecimalType(iv[i].value));
        }

        sendTimeSeries(channel, timeSeries);
        updateState(channel, new DecimalType(iv[iv.length - 1].value));
    }

    private void updateKwhChannel(String channelId, double consumption) {
        logger.debug("Update channel {} with {}", channelId, consumption);
        updateState(channelId,
                Double.isNaN(consumption) ? UnDefType.UNDEF : new QuantityType<>(consumption, Units.KILOWATT_HOUR));
    }

    private void updatekVAChannel(String channelId, double power) {
        logger.debug("Update channel {} with {}", channelId, power);
        updateState(channelId, Double.isNaN(power) ? UnDefType.UNDEF
                : new QuantityType<>(power, MetricPrefix.KILO(Units.VOLT_AMPERE)));
    }

    private void updateTempoChannel(String channelId, int tempoValue) {
        logger.debug("Update channel {} with {}", channelId, tempoValue);
        updateState(channelId, new DecimalType(tempoValue));
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
                MeterReading meterReading = api.getEnergyData(this, config.prmId, from, to);
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
                MeterReading meterReading = api.getPowerData(this, config.prmId, from, to);
                updateStatus(ThingStatus.ONLINE);
                return meterReading;
            } catch (LinkyException e) {
                logger.debug("Exception when getting power data: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }

        return null;
    }

    private @Nullable TempoResponse getTempoData(LocalDate from, LocalDate to) {
        logger.debug("getTempoData from");

        EnedisHttpApi api = this.enedisApi;
        if (api != null) {
            try {
                TempoResponse result = api.getTempoData(this, from, to);
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
        Bridge bridge = getBridge();
        if (bridge != null) {
            ApiBridgeHandler bridgeHandler = (ApiBridgeHandler) bridge.getHandler();
            if (bridgeHandler == null) {
                return false;
            }

            return bridgeHandler.isConnected();
        }
        return false;
    }

    private void disconnect() {
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
            updatePowerData();
            updateTempoTimeSeries();

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

        if (meterReading != null) {
            if (meterReading.weekValue == null) {
                LocalDate startDate = meterReading.dayValue[0].date.toLocalDate();
                LocalDate endDate = meterReading.dayValue[meterReading.dayValue.length - 1].date.toLocalDate();

                int weeksNum = Weeks.between(startDate, endDate).getAmount() + 2;
                int monthsNum = Months.between(startDate, endDate).getAmount() + 2;
                int yearsNum = Years.between(startDate, endDate).getAmount() + 2;

                meterReading.weekValue = new IntervalReading[weeksNum];
                meterReading.monthValue = new IntervalReading[monthsNum];
                meterReading.yearValue = new IntervalReading[yearsNum];

                for (int idx = 0; idx < weeksNum; idx++) {
                    meterReading.weekValue[idx] = new IntervalReading();
                }
                for (int idx = 0; idx < monthsNum; idx++) {
                    meterReading.monthValue[idx] = new IntervalReading();
                }
                for (int idx = 0; idx < yearsNum; idx++) {
                    meterReading.yearValue[idx] = new IntervalReading();
                }

                int size = meterReading.dayValue.length;
                int baseYear = meterReading.dayValue[0].date.getYear();
                int baseMonth = meterReading.dayValue[0].date.getMonthValue();
                int baseDayOfYear = meterReading.dayValue[0].date.getDayOfYear();
                int baseWeek = ((baseDayOfYear - 1) / 7) + 1;

                for (int idx = 0; idx < size; idx++) {
                    IntervalReading ir = meterReading.dayValue[idx];
                    LocalDateTime dt = ir.date;
                    double value = ir.value;
                    value = value / divider;
                    ir.value = value;

                    int idxYear = dt.getYear() - baseYear;
                    int month = dt.getMonthValue();
                    int weekOfYear = dt.get(WeekFields.of(Locale.FRANCE).weekOfYear());

                    int idxMonth = (idxYear * 12) + month - baseMonth;
                    int idxWeek = (idxYear * 52) + weekOfYear - baseWeek;

                    if (idxWeek < weeksNum) {
                        meterReading.weekValue[idxWeek].value += value;
                        if (meterReading.weekValue[idxWeek].date == null) {
                            meterReading.weekValue[idxWeek].date = dt;
                        }
                    }
                    if (idxMonth < monthsNum) {
                        meterReading.monthValue[idxMonth].value += value;
                        if (meterReading.monthValue[idxMonth].date == null) {
                            meterReading.monthValue[idxMonth].date = LocalDateTime.of(dt.getYear(), month, 1, 0, 0);
                        }
                    }

                    if (idxYear < yearsNum) {
                        meterReading.yearValue[idxYear].value += value;

                        if (meterReading.yearValue[idxYear].date == null) {
                            meterReading.yearValue[idxYear].date = LocalDateTime.of(dt.getYear(), 1, 1, 0, 0);
                        }
                    }
                }
            }
        }

        return meterReading;
    }

    private void checkData(@Nullable MeterReading meterReading) throws LinkyException {
        if (meterReading != null) {
            if (meterReading.dayValue.length == 0) {
                throw new LinkyException("Invalid meterReading data: no day period");
            }
        }
    }

    /*
     * private @Nullable Consumption getConsumptionAfterChecks(Consumption consumption, Target target) {
     *
     *
     *
     * return consumption;
     * }
     *
     *
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
     *
     *
     * private void logData(Aggregate aggregate, String title, boolean withDateFin, DateTimeFormatter dateTimeFormatter,
     * Target target) {
     * if (logger.isDebugEnabled()) {
     * int size = (aggregate.datas == null || aggregate.periodes == null) ? 0
     * : (aggregate.datas.size() <= aggregate.periodes.size() ? aggregate.datas.size()
     * : aggregate.periodes.size());
     * if (target == Target.FIRST) {
     * if (size > 0) {
     * logData(aggregate, 0, title, withDateFin, dateTimeFormatter);
     * }
     * } else if (target == Target.LAST) {
     * if (size > 0) {
     * logData(aggregate, size - 1, title, withDateFin, dateTimeFormatter);
     * }
     * } else {
     * for (int i = 0; i < size; i++) {
     * logData(aggregate, i, title, withDateFin, dateTimeFormatter);
     * }
     * }
     * }
     * }
     *
     * private void logData(Aggregate aggregate, int index, String title, boolean withDateFin,
     * DateTimeFormatter dateTimeFormatter) {
     * if (withDateFin) {
     * logger.debug("{} {} {} value {}", title, aggregate.periodes.get(index).dateDebut.format(dateTimeFormatter),
     * aggregate.periodes.get(index).dateFin.format(dateTimeFormatter), aggregate.datas.get(index));
     * } else {
     * logger.debug("{} {} value {}", title, aggregate.periodes.get(index).dateDebut.format(dateTimeFormatter),
     * aggregate.datas.get(index));
     * }
     * }
     *
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
