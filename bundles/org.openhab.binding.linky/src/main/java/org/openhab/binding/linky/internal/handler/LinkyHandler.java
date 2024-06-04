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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
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
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
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

    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable LinkyConfiguration config;
    private @Nullable EnedisHttpApi enedisApi;

    private enum Target {
        FIRST,
        LAST,
        ALL
    }

    public LinkyHandler(Thing thing, LocaleProvider localeProvider) {
        super(thing);

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

        /*
         * this.cachedPowerData = new ExpiringDayCache<>("power cache", REFRESH_FIRST_HOUR_OF_DAY, () -> {
         * // We request data for yesterday and the day before yesterday, even if the data for the day before yesterday
         * // is not needed by the binding. This is only a workaround to an API bug that will return
         * // INTERNAL_SERVER_ERROR rather than the expected data with a NaN value when the data for yesterday is not
         * // yet available.
         * // By requesting two days, the API is not failing and you get the expected NaN value for yesterday when the
         * // data is not yet available.
         * LocalDate today = LocalDate.now();
         * Consumption consumption = getPowerData(today.minusDays(2), today);
         * if (consumption != null) {
         * logData(consumption.aggregats.days, "Day (peak)", true, DateTimeFormatter.ISO_LOCAL_DATE_TIME,
         * Target.ALL);
         * consumption = getConsumptionAfterChecks(consumption, Target.LAST);
         * }
         * return consumption;
         * });
         *
         *
         * this.cachedMonthlyData = new ExpiringDayCache<>("monthly cache", REFRESH_FIRST_HOUR_OF_DAY, () -> {
         * LocalDate today = LocalDate.now();
         * Consumption consumption = getConsumptionData(today.withDayOfMonth(1).minusMonths(1), today);
         * if (consumption != null) {
         * logData(consumption.aggregats.months, "Month", true, DateTimeFormatter.ISO_LOCAL_DATE_TIME, Target.ALL);
         * consumption = getConsumptionAfterChecks(consumption, Target.LAST);
         * }
         * return consumption;
         * });
         *
         * this.cachedYearlyData = new ExpiringDayCache<>("yearly cache", REFRESH_FIRST_HOUR_OF_DAY, () -> {
         * LocalDate today = LocalDate.now();
         * Consumption consumption = getConsumptionData(LocalDate.of(today.getYear() - 1, 1, 1), today);
         * if (consumption != null) {
         * logData(consumption.aggregats.years, "Year", true, DateTimeFormatter.ISO_LOCAL_DATE_TIME, Target.ALL);
         * consumption = getConsumptionAfterChecks(consumption, Target.LAST);
         * }
         * return consumption;
         * });
         */
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

        updateStatus(ThingStatus.UNKNOWN);

        config = getConfigAs(LinkyConfiguration.class);
        if (config.seemsValid()) {
            scheduler.submit(() -> {
                try {

                    EnedisHttpApi api = this.enedisApi;
                    LinkyConfiguration config = this.config;

                    if (api != null && config != null) {
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
        LinkyConfiguration config = this.config;

        if (api != null && config != null) {
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

        updateDailyWeeklyData();

        // updatePowerData();
        // updateMonthlyData();
        // updateYearlyData();

        // String tempoData = getTempoData();

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

        if (!connectedBefore && isConnected()) {
            disconnect();
        }
    }

    /*
     * private synchronized void updatePowerData() {
     * if (isLinked(PEAK_POWER) || isLinked(PEAK_TIMESTAMP)) {
     * cachedPowerData.getValue().ifPresentOrElse(values -> {
     * Aggregate days = values.aggregats.days;
     * updateVAChannel(PEAK_POWER, days.datas.get(days.datas.size() - 1));
     * updateState(PEAK_TIMESTAMP, new DateTimeType(days.periodes.get(days.datas.size() - 1).dateDebut));
     * }, () -> {
     * updateKwhChannel(PEAK_POWER, Double.NaN);
     * updateState(PEAK_TIMESTAMP, UnDefType.UNDEF);
     * });
     * }
     * }
     */

    private void setCurrentAndPrevious(Aggregate periods, String currentChannel, String previousChannel) {
        double currentValue = 0.0;
        double previousValue = 0.0;
        if (!periods.datas.isEmpty()) {
            currentValue = periods.datas.get(periods.datas.size() - 1);
            if (periods.datas.size() > 1) {
                previousValue = periods.datas.get(periods.datas.size() - 2);
            }
        }
        updateKwhChannel(currentChannel, currentValue);
        updateKwhChannel(previousChannel, previousValue);
    }

    /**
     * Request new dayly/weekly data and updates channels
     */

    private synchronized void updateDailyWeeklyData() {
        if (isLinked(YESTERDAY) || isLinked(LAST_WEEK) || isLinked(THIS_WEEK)) {
            dailyConsumption.getValue().ifPresentOrElse(values -> {
                int dSize = values.intervalReading.length;
                updateKwhChannel(YESTERDAY, values.intervalReading[dSize - 1].value / 1000.00);

                LocalDate currentDt = LocalDate.now();
                int idxCurrentYear = currentDt.getYear() - 2021;

                int currentWeek = (currentDt.getDayOfYear() / 7) + 1;
                int currentMonth = currentDt.getMonthValue();

                int idxCurrentWeek = (52 * idxCurrentYear) + currentWeek;
                int idxCurrentMonth = (12 * idxCurrentYear) + currentMonth;

                int idxPreviousWeek = idxCurrentWeek - 1;
                int idxPreviousMonth = idxCurrentMonth - 1;
                int idxPreviousYear = idxCurrentYear - 1;

                updateKwhChannel(THIS_WEEK, values.WeekValue[idxCurrentWeek].value / 1000.00);
                updateKwhChannel(LAST_WEEK, values.WeekValue[idxPreviousWeek].value / 1000.00);

                updateKwhChannel(THIS_MONTH, values.MonthValue[idxCurrentMonth].value / 1000.00);
                updateKwhChannel(LAST_MONTH, values.WeekValue[idxPreviousMonth].value / 1000.00);

                updateKwhChannel(THIS_YEAR, values.YearValue[idxCurrentYear].value / 1000.00);
                updateKwhChannel(LAST_YEAR, values.YearValue[idxPreviousYear].value / 1000.00);
            }, () -> {
                updateKwhChannel(YESTERDAY, Double.NaN);
                updateKwhChannel(THIS_WEEK, Double.NaN);
                updateKwhChannel(LAST_WEEK, Double.NaN);
            });
        }
    }

    /**
     * Request new monthly data and updates channels
     */

    /*
     * private synchronized void updateMonthlyData() {
     * if (isLinked(LAST_MONTH) || isLinked(THIS_MONTH)) {
     * cachedMonthlyData.getValue().ifPresentOrElse(values -> {
     * Aggregate months = values.aggregats.months;
     * updateKwhChannel(LAST_MONTH, months.datas.get(0));
     * if (months.datas.size() > 1) {
     * updateKwhChannel(THIS_MONTH, months.datas.get(1));
     * } else {
     * updateKwhChannel(THIS_MONTH, 0.0);
     * }
     * }, () -> {
     * if (ZonedDateTime.now().getDayOfMonth() == 1) {
     * updateKwhChannel(THIS_MONTH, 0.0);
     * updateKwhChannel(LAST_MONTH, Double.NaN);
     * } else {
     * updateKwhChannel(THIS_MONTH, Double.NaN);
     * }
     * });
     * }
     * }
     */


    /**
     * Request new yearly data and updates channels
     */

    /*
     * private synchronized void updateYearlyData() {
     * if (isLinked(LAST_YEAR) || isLinked(THIS_YEAR)) {
     * cachedYearlyData.getValue().ifPresentOrElse(values -> {
     * Aggregate years = values.aggregats.years;
     * updateKwhChannel(LAST_YEAR, years.datas.get(0));
     * if (years.datas.size() > 1) {
     * updateKwhChannel(THIS_YEAR, years.datas.get(1));
     * } else {
     * updateKwhChannel(THIS_YEAR, 0.0);
     * }
     * }, () -> {
     * if (ZonedDateTime.now().getDayOfYear() == 1) {
     * updateKwhChannel(THIS_YEAR, 0.0);
     * updateKwhChannel(LAST_YEAR, Double.NaN);
     * } else {
     * updateKwhChannel(THIS_YEAR, Double.NaN);
     * }
     * });
     * }
     * }
     */

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

    /*
     * private @Nullable String getTempoData() {
     * logger.debug("getTempoData from");
     *
     * EnedisHttpApi api = this.enedisApi;
     * if (api != null) {
     * try {
     * String result = api.getTempoData();
     * updateStatus(ThingStatus.ONLINE);
     * return result;
     * } catch (LinkyException e) {
     * logger.debug("Exception when getting power data: {}", e.getMessage(), e);
     * updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
     * }
     * }
     * return null;
     * }
     */

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
            switch (channelUID.getId()) {
                case YESTERDAY:
                case LAST_WEEK:
                case THIS_WEEK:
                    updateDailyWeeklyData();
                    break;
                case LAST_MONTH:
                case THIS_MONTH:
                    // updateMonthlyData();
                    break;
                case LAST_YEAR:
                case THIS_YEAR:
                    // updateYearlyData();
                    break;
                case PEAK_POWER:
                case PEAK_TIMESTAMP:
                    // updatePowerData();
                    break;
                default:
                    break;
            }
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
