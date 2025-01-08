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
import org.openhab.binding.linky.internal.dto.Contact;
import org.openhab.binding.linky.internal.dto.Contract;
import org.openhab.binding.linky.internal.dto.Identity;
import org.openhab.binding.linky.internal.dto.IntervalReading;
import org.openhab.binding.linky.internal.dto.MeterReading;
import org.openhab.binding.linky.internal.dto.PrmDetail;
import org.openhab.binding.linky.internal.dto.PrmInfo;
import org.openhab.binding.linky.internal.dto.ResponseTempo;
import org.openhab.binding.linky.internal.dto.UsagePoint;
import org.openhab.binding.linky.internal.dto.UserInfo;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
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
    private final ExpiringDayCache<MeterReading> loadCurveConsumption;
    private final ExpiringDayCache<ResponseTempo> tempoInformation;

    private @Nullable ScheduledFuture<?> refreshJob;
    private LinkyConfiguration config;
    private @Nullable EnedisHttpApi enedisApi;
    private double divider = 1.00;

    public String userId = "";

    private @Nullable ScheduledFuture<?> pollingJob = null;

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
            if (meterReading != null) {
                logData(meterReading.baseValue, "Day", DateTimeFormatter.ISO_LOCAL_DATE, Target.ALL);
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
                        logData(meterReading.baseValue, "Day (peak)", DateTimeFormatter.ISO_LOCAL_DATE, Target.ALL);
                    }
                    return meterReading;
                });

        // Read Tempo Information
        this.tempoInformation = new ExpiringDayCache<>("tempoInformation", REFRESH_FIRST_HOUR_OF_DAY, () -> {
            LocalDate today = LocalDate.now();

            ResponseTempo tempoData = getTempoData(today.minusDays(1095), today.plusDays(1));
            return tempoData;
        });

        // Comsuption Load Curve
        this.loadCurveConsumption = new ExpiringDayCache<>("loadCurveConsumption", REFRESH_FIRST_HOUR_OF_DAY, () -> {
            LocalDate today = LocalDate.now();
            MeterReading meterReading = getLoadCurveConsumption(today.minusDays(6), today);
            meterReading = getMeterReadingAfterChecks(meterReading);
            if (meterReading != null) {
                logData(meterReading.baseValue, "Day (peak)", DateTimeFormatter.ISO_LOCAL_DATE, Target.ALL);
            }
            return meterReading;
        });
    }

    @Override
    public synchronized void initialize() {
        logger.debug("Initializing Linky handler.");

        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }

        LinkyBridgeHandler bridgeHandler = (LinkyBridgeHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            return;
        }
        enedisApi = bridgeHandler.getEnedisApi();
        divider = bridgeHandler.getDivider();

        updateStatus(ThingStatus.UNKNOWN);

        if (config.seemsValid()) {
            bridgeHandler.registerNewPrmId(config.prmId);
            pollingJob = scheduler.scheduleWithFixedDelay(this::pollingCode, 0, 5, TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-mandatory-settings");
        }
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

        LinkyBridgeHandler bridgeHandler = (LinkyBridgeHandler) bridge.getHandler();
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

                LinkyBridgeHandler bridgeHandler = (LinkyBridgeHandler) lcBridge.getHandler();
                if (bridgeHandler == null) {
                    return;
                }

                if (!bridgeHandler.isConnected()) {
                    bridgeHandler.connectionInit();
                }

                if (supportNewApiFormat()) {
                    Identity identity = api.getIdentity(this, config.prmId);
                    Contact contact = api.getContact(this, config.prmId);
                    Contract contract = api.getContract(this, config.prmId);
                    UsagePoint usagePoint = api.getUsagePoint(this, config.prmId);

                    updateMetaData(identity, contact, contract, usagePoint);

                    updateProperties(
                            Map.of(USER_ID, "", PUISSANCE, contract.subscribedPower, PRM_ID, usagePoint.usagePointId));
                } else {
                    UserInfo userInfo = api.getUserInfo(this);
                    PrmInfo prmInfo = api.getPrmInfo(this, userInfo.userProperties.internId, config.prmId);
                    PrmDetail details = api.getPrmDetails(this, userInfo.userProperties.internId, prmInfo.idPrm);

                    Identity identity = Identity.convertFromUserInfo(userInfo);
                    Contact contact = Contact.convertFromUserInfo(userInfo);
                    Contract contract = Contract.convertFromPrmDetail(details);
                    UsagePoint usagePoint = UsagePoint.convertFromPrmDetail(prmInfo, details);

                    this.userId = userInfo.userProperties.internId;

                    updateMetaData(identity, contact, contract, usagePoint);

                    updateProperties(Map.of(USER_ID, userInfo.userProperties.internId, PUISSANCE,
                            details.situationContractuelleDtos[0].structureTarifaire().puissanceSouscrite().valeur()
                                    + " kVA",
                            PRM_ID, prmInfo.idPrm));
                }

                updateData();

                final LocalDateTime now = LocalDateTime.now();
                final LocalDateTime nextDayFirstTimeUpdate = now.plusDays(1).withHour(REFRESH_FIRST_HOUR_OF_DAY)
                        .truncatedTo(ChronoUnit.HOURS);

                if (this.getThing().getStatusInfo().getStatusDetail() != ThingStatusDetail.COMMUNICATION_ERROR) {
                    updateStatus(ThingStatus.ONLINE);
                }

                if (lcPollingJob != null) {
                    lcPollingJob.cancel(false);
                }

                refreshJob = scheduler.scheduleWithFixedDelay(this::updateData,
                        ChronoUnit.MINUTES.between(now, nextDayFirstTimeUpdate) % REFRESH_INTERVAL_IN_MIN + 1,
                        REFRESH_INTERVAL_IN_MIN, TimeUnit.MINUTES);
            }
        } catch (LinkyException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    public @Nullable LinkyConfiguration getLinkyConfig() {
        return config;
    }

    private synchronized void updateMetaData(Identity identity, Contact contact, Contract contract,
            UsagePoint usagePoint) {
        String title = identity.title;
        String firstName = identity.firstname;
        String lastName = identity.lastname;

        updateState(MAIN_GROUP, MAIN_IDENTITY, new StringType(title + " " + firstName + " " + lastName));

        updateState(MAIN_GROUP, MAIN_CONTRACT_SEGMENT, new StringType(contract.segment));
        updateState(MAIN_GROUP, MAIN_CONTRACT_CONTRACT_STATUS, new StringType(contract.contractStatus));
        updateState(MAIN_GROUP, MAIN_CONTRACT_CONTRACT_TYPE, new StringType(contract.contractType));
        updateState(MAIN_GROUP, MAIN_CONTRACT_DISTRIBUTION_TARIFF, new StringType(contract.distributionTariff));
        updateState(MAIN_GROUP, MAIN_CONTRACT_LAST_ACTIVATION_DATE, new StringType(contract.lastActivationDate));
        updateState(MAIN_GROUP, MAIN_CONTRACT_LAST_DISTRIBUTION_TARIFF_CHANGE_DATE,
                new StringType(contract.lastDistributionTariffChangeDate));
        updateState(MAIN_GROUP, MAIN_CONTRACT_OFF_PEAK_HOURS, new StringType(contract.offpeakHours));
        updateState(MAIN_GROUP, MAIN_CONTRACT_SEGMENT, new StringType(contract.segment));
        updateState(MAIN_GROUP, MAIN_CONTRACT_SUBSCRIBED_POWER, new StringType(contract.subscribedPower));

        updateState(MAIN_GROUP, MAIN_USAGEPOINT_ID, new StringType(usagePoint.usagePointId));
        updateState(MAIN_GROUP, MAIN_USAGEPOINT_STATUS, new StringType(usagePoint.usagePointStatus));
        updateState(MAIN_GROUP, MAIN_USAGEPOINT_METER_TYPE, new StringType(usagePoint.meterType));

        updateState(MAIN_GROUP, MAIN_USAGEPOINT_METER_ADDRESS_CITY,
                new StringType(usagePoint.usagePointAddresses.city));
        updateState(MAIN_GROUP, MAIN_USAGEPOINT_METER_ADDRESS_COUNTRY,
                new StringType(usagePoint.usagePointAddresses.country));
        updateState(MAIN_GROUP, MAIN_USAGEPOINT_METER_ADDRESS_POSTAL_CODE,
                new StringType(usagePoint.usagePointAddresses.postalCode));
        updateState(MAIN_GROUP, MAIN_USAGEPOINT_METER_ADDRESS_INSEE_CODE,
                new StringType(usagePoint.usagePointAddresses.inseeCode));
        updateState(MAIN_GROUP, MAIN_USAGEPOINT_METER_ADDRESS_STREET,
                new StringType(usagePoint.usagePointAddresses.street));

        updateState(MAIN_GROUP, MAIN_CONTACT_MAIL, new StringType(contact.email));
        updateState(MAIN_GROUP, MAIN_CONTACT_PHONE, new StringType(contact.phone));
    }

    /**
     * Request new data and updates channels
     */
    private synchronized void updateData() {
        updateEnergyData();
        updatePowerData();
        updateTempoTimeSeries();
        updateLoadCurveData();
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

            updateTempoChannel(TEMPO_GROUP, TEMPO_TODAY_INFO, getTempoIdx((String) tempoValues[size - 2]));
            updateTempoChannel(TEMPO_GROUP, TEMPO_TOMORROW_INFO, getTempoIdx((String) tempoValues[size - 1]));

            sendTimeSeries(TEMPO_GROUP, TEMPO_TEMPO_INFO_TIME_SERIES, timeSeries);
            updateState(TEMPO_GROUP, TEMPO_TEMPO_INFO_TIME_SERIES,
                    new DecimalType(getTempoIdx((String) tempoValues[size - 2])));
        }, () -> {
            updateTempoChannel(TEMPO_GROUP, TEMPO_TODAY_INFO, -1);
            updateTempoChannel(TEMPO_GROUP, TEMPO_TOMORROW_INFO, -1);
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

    private synchronized void updatePowerData() {
        dailyConsumptionMaxPower.getValue().ifPresentOrElse(values -> {
            int dSize = values.baseValue.length;

            updatekVAChannel(DAILY_GROUP, PEAK_POWER_DAY_MINUS_1, values.baseValue[dSize - 1].value);
            updateState(DAILY_GROUP, PEAK_POWER_TS_DAY_MINUS_1,
                    new DateTimeType(values.baseValue[dSize - 1].date.atZone(ZoneId.systemDefault())));

            updatekVAChannel(DAILY_GROUP, PEAK_POWER_DAY_MINUS_2, values.baseValue[dSize - 2].value);
            updateState(DAILY_GROUP, PEAK_POWER_TS_DAY_MINUS_2,
                    new DateTimeType(values.baseValue[dSize - 2].date.atZone(ZoneId.systemDefault())));

            updatekVAChannel(DAILY_GROUP, PEAK_POWER_DAY_MINUS_3, values.baseValue[dSize - 3].value);
            updateState(DAILY_GROUP, PEAK_POWER_TS_DAY_MINUS_3,
                    new DateTimeType(values.baseValue[dSize - 3].date.atZone(ZoneId.systemDefault())));

            updatePowerTimeSeries(DAILY_GROUP, MAX_POWER_CHANNEL, values.baseValue);
            updatePowerTimeSeries(WEEKLY_GROUP, MAX_POWER_CHANNEL, values.weekValue);
            updatePowerTimeSeries(MONTHLY_GROUP, MAX_POWER_CHANNEL, values.monthValue);
            updatePowerTimeSeries(YEARLY_GROUP, MAX_POWER_CHANNEL, values.yearValue);

        }, () -> {
            updateKwhChannel(DAILY_GROUP, PEAK_POWER_DAY_MINUS_1, Double.NaN);
            updateState(DAILY_GROUP, PEAK_POWER_TS_DAY_MINUS_1, UnDefType.UNDEF);

            updateKwhChannel(DAILY_GROUP, PEAK_POWER_DAY_MINUS_2, Double.NaN);
            updateState(DAILY_GROUP, PEAK_POWER_TS_DAY_MINUS_2, UnDefType.UNDEF);

            updateKwhChannel(DAILY_GROUP, PEAK_POWER_DAY_MINUS_3, Double.NaN);
            updateState(DAILY_GROUP, PEAK_POWER_TS_DAY_MINUS_3, UnDefType.UNDEF);
        });
    }

    /**
     * Request new daily/weekly data and updates channels
     */
    private synchronized void updateEnergyData() {
        dailyConsumption.getValue().ifPresentOrElse(values -> {
            int dSize = values.baseValue.length;

            updateKwhChannel(DAILY_GROUP, DAY_MINUS_1, values.baseValue[dSize - 1].value);
            updateKwhChannel(DAILY_GROUP, DAY_MINUS_2, values.baseValue[dSize - 2].value);
            updateKwhChannel(DAILY_GROUP, DAY_MINUS_3, values.baseValue[dSize - 3].value);

            int idxCurrentYear = values.yearValue.length - 1;
            int idxCurrentWeek = values.weekValue.length - 1;
            int idxCurrentMonth = values.monthValue.length - 1;

            updateKwhChannel(WEEKLY_GROUP, WEEK_MINUS_0, values.weekValue[idxCurrentWeek].value);
            updateKwhChannel(WEEKLY_GROUP, WEEK_MINUS_1, values.weekValue[idxCurrentWeek - 1].value);
            updateKwhChannel(WEEKLY_GROUP, WEEK_MINUS_2, values.weekValue[idxCurrentWeek - 2].value);

            updateKwhChannel(MONTHLY_GROUP, MONTH_MINUS_0, values.monthValue[idxCurrentMonth].value);
            updateKwhChannel(MONTHLY_GROUP, MONTH_MINUS_1, values.monthValue[idxCurrentMonth - 1].value);
            updateKwhChannel(MONTHLY_GROUP, MONTH_MINUS_2, values.monthValue[idxCurrentMonth - 2].value);

            updateKwhChannel(YEARLY_GROUP, YEAR_MINUS_0, values.yearValue[idxCurrentYear].value);
            updateKwhChannel(YEARLY_GROUP, YEAR_MINUS_1, values.yearValue[idxCurrentYear - 1].value);
            updateKwhChannel(YEARLY_GROUP, YEAR_MINUS_2, values.yearValue[idxCurrentYear - 2].value);

            updateConsumptionTimeSeries(DAILY_GROUP, CONSUMPTION_CHANNEL, values.baseValue);
            updateConsumptionTimeSeries(WEEKLY_GROUP, CONSUMPTION_CHANNEL, values.weekValue);
            updateConsumptionTimeSeries(MONTHLY_GROUP, CONSUMPTION_CHANNEL, values.monthValue);
            updateConsumptionTimeSeries(YEARLY_GROUP, CONSUMPTION_CHANNEL, values.yearValue);
        }, () -> {
            updateKwhChannel(DAILY_GROUP, DAY_MINUS_1, Double.NaN);
            updateKwhChannel(DAILY_GROUP, DAY_MINUS_2, Double.NaN);
            updateKwhChannel(DAILY_GROUP, DAY_MINUS_3, Double.NaN);

            updateKwhChannel(WEEKLY_GROUP, WEEK_MINUS_0, Double.NaN);
            updateKwhChannel(WEEKLY_GROUP, WEEK_MINUS_1, Double.NaN);
            updateKwhChannel(WEEKLY_GROUP, WEEK_MINUS_2, Double.NaN);

            updateKwhChannel(MONTHLY_GROUP, MONTH_MINUS_0, Double.NaN);
            updateKwhChannel(MONTHLY_GROUP, MONTH_MINUS_1, Double.NaN);
            updateKwhChannel(MONTHLY_GROUP, MONTH_MINUS_2, Double.NaN);

            updateKwhChannel(YEARLY_GROUP, YEAR_MINUS_0, Double.NaN);
            updateKwhChannel(YEARLY_GROUP, YEAR_MINUS_1, Double.NaN);
            updateKwhChannel(YEARLY_GROUP, YEAR_MINUS_2, Double.NaN);
        });
    }

    /**
     * Request new loadCurve data and updates channels
     */
    private synchronized void updateLoadCurveData() {
        loadCurveConsumption.getValue().ifPresentOrElse(values -> {
            updatePowerTimeSeries(LOAD_CURVE_GROUP, POWER_CHANNEL, values.baseValue);
        }, () -> {
        });
    }

    private synchronized void updatePowerTimeSeries(String groupId, String channelId, IntervalReading[] iv) {
        TimeSeries timeSeries = new TimeSeries(Policy.REPLACE);

        for (int i = 0; i < iv.length; i++) {
            try {
                if (iv[i].date == null) {
                    continue;
                }

                Instant timestamp = iv[i].date.toInstant(ZoneOffset.UTC);

                if (Double.isNaN(iv[i].value)) {
                    continue;
                }
                timeSeries.add(timestamp, new DecimalType(iv[i].value));
            } catch (Exception ex) {
                logger.debug("aa");
            }
        }

        sendTimeSeries(groupId, channelId, timeSeries);
    }

    private synchronized void updateConsumptionTimeSeries(String groupId, String channelId, IntervalReading[] iv) {
        TimeSeries timeSeries = new TimeSeries(Policy.REPLACE);

        for (int i = 0; i < iv.length; i++) {
            if (iv[i].date == null) {
                continue;
            }

            Instant timestamp = iv[i].date.toInstant(ZoneOffset.UTC);

            if (Double.isNaN(iv[i].value)) {
                continue;
            }
            timeSeries.add(timestamp, new DecimalType(iv[i].value));
        }

        sendTimeSeries(groupId, channelId, timeSeries);
    }

    private void updateKwhChannel(String groupId, String channelId, double consumption) {
        logger.debug("Update channel {} with {}", channelId, consumption);
        updateState(groupId, channelId,
                Double.isNaN(consumption) ? UnDefType.UNDEF : new QuantityType<>(consumption, Units.KILOWATT_HOUR));
    }

    private void updatekVAChannel(String groupId, String channelId, double power) {
        logger.debug("Update channel {} with {}", channelId, power);
        updateState(groupId, channelId, Double.isNaN(power) ? UnDefType.UNDEF
                : new QuantityType<>(power, MetricPrefix.KILO(Units.VOLT_AMPERE)));
    }

    private void updateTempoChannel(String groupId, String channelId, int tempoValue) {
        logger.debug("Update channel {} with {}", channelId, tempoValue);
        updateState(groupId + "#" + channelId, new DecimalType(tempoValue));
    }

    protected void updateState(String groupId, String channelID, State state) {
        super.updateState(groupId + "#" + channelID, state);
    }

    protected void sendTimeSeries(String groupId, String channelID, TimeSeries timeSeries) {
        super.sendTimeSeries(groupId + "#" + channelID, timeSeries);
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
        return report;
    }

    private List<String> buildReport(LocalDate startDay, LocalDate endDay, @Nullable String separator) {
        List<String> report = new ArrayList<>();
        if (startDay.getYear() == endDay.getYear() && startDay.getMonthValue() == endDay.getMonthValue()) {
            // All values in the same month
            MeterReading meterReading = getConsumptionData(startDay, endDay.plusDays(1));
            if (meterReading != null) {
                IntervalReading[] days = meterReading.baseValue;

                int size = days.length;

                for (int i = 0; i < size; i++) {
                    double consumption = days[i].value;
                    LocalDate day = days[i].date.toLocalDate();
                    // Filter data in case it contains data from dates outside the requested period
                    if (day.isBefore(startDay) || day.isAfter(endDay)) {
                        continue;
                    }
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
                MeterReading meterReading = api.getEnergyData(this, this.userId, config.prmId, from, to);
                return meterReading;
            } catch (LinkyException e) {
                logger.debug("Exception when getting consumption data: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }

        return null;
    }

    private @Nullable MeterReading getLoadCurveConsumption(LocalDate from, LocalDate to) {
        logger.debug("getLoadCurveConsumption from {} to {}", from.format(DateTimeFormatter.ISO_LOCAL_DATE),
                to.format(DateTimeFormatter.ISO_LOCAL_DATE));

        EnedisHttpApi api = this.enedisApi;
        if (api != null) {
            try {
                MeterReading meterReading = api.getLoadCurveData(this, this.userId, config.prmId, from, to);
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
                MeterReading meterReading = api.getPowerData(this, this.userId, config.prmId, from, to);
                return meterReading;
            } catch (LinkyException e) {
                logger.debug("Exception when getting power data: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }

        return null;
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

    private boolean isConnected() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            LinkyBridgeHandler bridgeHandler = (LinkyBridgeHandler) bridge.getHandler();
            if (bridgeHandler != null) {
                return bridgeHandler.isConnected();
            }
        }
        return false;
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Linky handler.");
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
            logger.debug("Refreshing channel {}", channelUID.getId());
            updateData();
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
                LocalDate startDate = meterReading.baseValue[0].date.toLocalDate();
                LocalDate endDate = meterReading.baseValue[meterReading.baseValue.length - 1].date.toLocalDate();

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

                int size = meterReading.baseValue.length;
                int baseYear = meterReading.baseValue[0].date.getYear();
                int baseMonth = meterReading.baseValue[0].date.getMonthValue();
                int baseWeek = meterReading.baseValue[0].date.get(WeekFields.of(Locale.FRANCE).weekOfYear());

                for (int idx = 0; idx < size; idx++) {
                    IntervalReading ir = meterReading.baseValue[idx];
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
            if (meterReading.baseValue.length == 0) {
                throw new LinkyException("Invalid meterReading data: no day period");
            }
        }
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
