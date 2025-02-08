/**
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

import static org.openhab.binding.linky.internal.LinkyBindingConstants.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
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
import org.openhab.binding.linky.internal.dto.MetaData;
import org.openhab.binding.linky.internal.dto.MeterReading;
import org.openhab.binding.linky.internal.dto.PrmDetail;
import org.openhab.binding.linky.internal.dto.PrmInfo;
import org.openhab.binding.linky.internal.dto.ResponseTempo;
import org.openhab.binding.linky.internal.dto.UsagePoint;
import org.openhab.binding.linky.internal.dto.UserInfo;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
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
 * The {@link LinkyRemoteHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */

@NonNullByDefault
@SuppressWarnings("null")
public class LinkyRemoteHandler extends BaseThingHandler {
    private final TimeZoneProvider timeZoneProvider;
    private ZoneId zoneId = ZoneId.systemDefault();

    private static final Random randomNumbers = new Random();
    private static final int REFRESH_HOUR_OF_DAY = 1;
    private static final int REFRESH_MINUTE_OF_DAY = randomNumbers.nextInt(60);
    private static final int REFRESH_INTERVAL_IN_MIN = 120;

    private final Logger logger = LoggerFactory.getLogger(LinkyRemoteHandler.class);

    private final ExpiringDayCache<MetaData> metaData;
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

    public LinkyRemoteHandler(Thing thing, LocaleProvider localeProvider, TimeZoneProvider timeZoneProvider) {
        super(thing);

        config = getConfigAs(LinkyConfiguration.class);
        this.timeZoneProvider = timeZoneProvider;

        this.metaData = new ExpiringDayCache<>("metaData", REFRESH_HOUR_OF_DAY, REFRESH_MINUTE_OF_DAY, () -> {
            MetaData metaData = getMetaData();
            return metaData;
        });

        this.dailyConsumption = new ExpiringDayCache<>("dailyConsumption", REFRESH_HOUR_OF_DAY, REFRESH_MINUTE_OF_DAY,
                () -> {
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
        this.dailyConsumptionMaxPower = new ExpiringDayCache<>("dailyConsumptionMaxPower", REFRESH_HOUR_OF_DAY,
                REFRESH_MINUTE_OF_DAY, () -> {
                    LocalDate today = LocalDate.now();
                    MeterReading meterReading = getPowerData(today.minusDays(1095), today);
                    meterReading = getMeterReadingAfterChecks(meterReading);
                    if (meterReading != null) {
                        logData(meterReading.baseValue, "Day (peak)", DateTimeFormatter.ISO_LOCAL_DATE, Target.ALL);
                    }
                    return meterReading;
                });

        // Read Tempo Information
        this.tempoInformation = new ExpiringDayCache<>("tempoInformation", REFRESH_HOUR_OF_DAY, REFRESH_MINUTE_OF_DAY,
                () -> {
                    LocalDate today = LocalDate.now();

                    ResponseTempo tempoData = getTempoData(today.minusDays(1095), today.plusDays(1));
                    return tempoData;
                });

        // Comsuption Load Curve
        this.loadCurveConsumption = new ExpiringDayCache<>("loadCurveConsumption", REFRESH_HOUR_OF_DAY,
                REFRESH_MINUTE_OF_DAY, () -> {
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
        logger.debug("Initializing Linky handler for {}", config.prmId);

        // update the timezone if not set to default to openhab default timezone
        Configuration thingConfig = getConfig();

        Object val = thingConfig.get("timezone");
        if (val == null || "".equals(val)) {
            zoneId = this.timeZoneProvider.getTimeZone();
            thingConfig.put("timezone", zoneId.getId());
        } else {
            zoneId = ZoneId.of((String) val);
        }

        saveConfiguration(thingConfig);

        // reread config to update timezone field
        config = getConfigAs(LinkyConfiguration.class);

        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }

        BridgeLinkyHandler bridgeHandler = (BridgeLinkyHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            return;
        }
        enedisApi = bridgeHandler.getEnedisApi();
        divider = bridgeHandler.getDivider();

        updateStatus(ThingStatus.UNKNOWN);

        if (config.seemsValid()) {
            bridgeHandler.registerNewPrmId(config.prmId);
            pollingJob = scheduler.schedule(this::pollingCode, 5, TimeUnit.SECONDS);
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

        BridgeLinkyHandler bridgeHandler = (BridgeLinkyHandler) bridge.getHandler();
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

                BridgeLinkyHandler bridgeHandler = (BridgeLinkyHandler) lcBridge.getHandler();
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

    public @Nullable LinkyConfiguration getLinkyConfig() {
        return config;
    }

    private synchronized void updateMetaData() {
        metaData.getValue().ifPresentOrElse(values -> {
            String title = values.identity.title;
            String firstName = values.identity.firstname;
            String lastName = values.identity.lastname;

            updateState(GROUP_MAIN, CHANNEL_IDENTITY, new StringType(title + " " + firstName + " " + lastName));

            updateState(GROUP_MAIN, CHANNEL_CONTRACT_SEGMENT, new StringType(values.contract.segment));
            updateState(GROUP_MAIN, CHANNEL_CONTRACT_CONTRACT_STATUS, new StringType(values.contract.contractStatus));
            updateState(GROUP_MAIN, CHANNEL_CONTRACT_CONTRACT_TYPE, new StringType(values.contract.contractType));
            updateState(GROUP_MAIN, CHANNEL_CONTRACT_DISTRIBUTION_TARIFF,
                    new StringType(values.contract.distributionTariff));
            updateState(GROUP_MAIN, CHANNEL_CONTRACT_LAST_ACTIVATION_DATE,
                    new StringType(values.contract.lastActivationDate));
            updateState(GROUP_MAIN, CHANNEL_CONTRACT_LAST_DISTRIBUTION_TARIFF_CHANGE_DATE,
                    new StringType(values.contract.lastDistributionTariffChangeDate));
            updateState(GROUP_MAIN, CHANNEL_CONTRACT_OFF_PEAK_HOURS, new StringType(values.contract.offpeakHours));
            updateState(GROUP_MAIN, CHANNEL_CONTRACT_SEGMENT, new StringType(values.contract.segment));
            updateState(GROUP_MAIN, CHANNEL_CONTRACT_SUBSCRIBED_POWER, new StringType(values.contract.subscribedPower));

            updateState(GROUP_MAIN, CHANNEL_USAGEPOINT_ID, new StringType(values.usagePoint.usagePointId));
            updateState(GROUP_MAIN, CHANNEL_USAGEPOINT_STATUS, new StringType(values.usagePoint.usagePointStatus));
            updateState(GROUP_MAIN, CHANNEL_USAGEPOINT_METER_TYPE, new StringType(values.usagePoint.meterType));

            updateState(GROUP_MAIN, CHANNEL_USAGEPOINT_METER_ADDRESS_CITY,
                    new StringType(values.usagePoint.usagePointAddresses.city));
            updateState(GROUP_MAIN, CHANNEL_USAGEPOINT_METER_ADDRESS_COUNTRY,
                    new StringType(values.usagePoint.usagePointAddresses.country));
            updateState(GROUP_MAIN, CHANNEL_USAGEPOINT_METER_ADDRESS_POSTAL_CODE,
                    new StringType(values.usagePoint.usagePointAddresses.postalCode));
            updateState(GROUP_MAIN, CHANNEL_USAGEPOINT_METER_ADDRESS_INSEE_CODE,
                    new StringType(values.usagePoint.usagePointAddresses.inseeCode));
            updateState(GROUP_MAIN, CHANNEL_USAGEPOINT_METER_ADDRESS_STREET,
                    new StringType(values.usagePoint.usagePointAddresses.street));

            updateState(GROUP_MAIN, CHANNEL_CONTACT_MAIL, new StringType(values.contact.email));
            updateState(GROUP_MAIN, CHANNEL_CONTACT_PHONE, new StringType(values.contact.phone));

            userId = values.identity.internId;
            updateProperties(Map.of(USER_ID, userId, PUISSANCE, values.contract.subscribedPower + " kVA", PRM_ID,
                    values.usagePoint.usagePointId));
        }, () -> {

            updateState(GROUP_MAIN, CHANNEL_IDENTITY, UnDefType.UNDEF);

            updateState(GROUP_MAIN, CHANNEL_CONTRACT_SEGMENT, UnDefType.UNDEF);
            updateState(GROUP_MAIN, CHANNEL_CONTRACT_CONTRACT_STATUS, UnDefType.UNDEF);
            updateState(GROUP_MAIN, CHANNEL_CONTRACT_CONTRACT_TYPE, UnDefType.UNDEF);
            updateState(GROUP_MAIN, CHANNEL_CONTRACT_DISTRIBUTION_TARIFF, UnDefType.UNDEF);
            updateState(GROUP_MAIN, CHANNEL_CONTRACT_LAST_ACTIVATION_DATE, UnDefType.UNDEF);
            updateState(GROUP_MAIN, CHANNEL_CONTRACT_LAST_DISTRIBUTION_TARIFF_CHANGE_DATE, UnDefType.UNDEF);
            updateState(GROUP_MAIN, CHANNEL_CONTRACT_OFF_PEAK_HOURS, UnDefType.UNDEF);
            updateState(GROUP_MAIN, CHANNEL_CONTRACT_SEGMENT, UnDefType.UNDEF);
            updateState(GROUP_MAIN, CHANNEL_CONTRACT_SUBSCRIBED_POWER, UnDefType.UNDEF);

            updateState(GROUP_MAIN, CHANNEL_USAGEPOINT_ID, UnDefType.UNDEF);
            updateState(GROUP_MAIN, CHANNEL_USAGEPOINT_STATUS, UnDefType.UNDEF);
            updateState(GROUP_MAIN, CHANNEL_USAGEPOINT_METER_TYPE, UnDefType.UNDEF);

            updateState(GROUP_MAIN, CHANNEL_USAGEPOINT_METER_ADDRESS_CITY, UnDefType.UNDEF);
            updateState(GROUP_MAIN, CHANNEL_USAGEPOINT_METER_ADDRESS_COUNTRY, UnDefType.UNDEF);
            updateState(GROUP_MAIN, CHANNEL_USAGEPOINT_METER_ADDRESS_POSTAL_CODE, UnDefType.UNDEF);
            updateState(GROUP_MAIN, CHANNEL_USAGEPOINT_METER_ADDRESS_INSEE_CODE, UnDefType.UNDEF);
            updateState(GROUP_MAIN, CHANNEL_USAGEPOINT_METER_ADDRESS_STREET, UnDefType.UNDEF);

            updateState(GROUP_MAIN, CHANNEL_CONTACT_MAIL, UnDefType.UNDEF);
            updateState(GROUP_MAIN, CHANNEL_CONTACT_PHONE, UnDefType.UNDEF);

        });
    }

    private synchronized @Nullable MetaData getMetaData() {
        try {
            EnedisHttpApi api = this.enedisApi;
            MetaData result = new MetaData();
            if (api != null) {
                if (supportNewApiFormat()) {
                    result.identity = api.getIdentity(this, config.prmId);
                    result.contact = api.getContact(this, config.prmId);
                    result.contract = api.getContract(this, config.prmId);
                    result.usagePoint = api.getUsagePoint(this, config.prmId);
                } else {
                    UserInfo userInfo = api.getUserInfo(this);
                    PrmInfo prmInfo = api.getPrmInfo(this, userInfo.userProperties.internId, config.prmId);
                    PrmDetail details = api.getPrmDetails(this, userInfo.userProperties.internId, prmInfo.idPrm);

                    result.identity = Identity.convertFromUserInfo(userInfo);
                    result.contact = Contact.convertFromUserInfo(userInfo);
                    result.contract = Contract.convertFromPrmDetail(details);
                    result.usagePoint = UsagePoint.convertFromPrmDetail(prmInfo, details);
                }
            }
            return result;
        } catch (LinkyException e) {
            logger.error("Exception occurs during data update for {} : {}", config.prmId, e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }

        return null;
    }

    /**
     * Request new data and updates channels
     */
    private synchronized void updateData() {
        updateMetaData();
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

            updateTempoChannel(GROUP_TEMPO, CHANNEL_TEMPO_TODAY_INFO, getTempoIdx((String) tempoValues[size - 2]));
            updateTempoChannel(GROUP_TEMPO, CHANNEL_TEMPO_TOMORROW_INFO, getTempoIdx((String) tempoValues[size - 1]));

            sendTimeSeries(GROUP_TEMPO, CHANNEL_TEMPO_TEMPO_INFO_TIME_SERIES, timeSeries);
            updateState(GROUP_TEMPO, CHANNEL_TEMPO_TEMPO_INFO_TIME_SERIES,
                    new DecimalType(getTempoIdx((String) tempoValues[size - 2])));
        }, () -> {
            updateTempoChannel(GROUP_TEMPO, CHANNEL_TEMPO_TODAY_INFO, -1);
            updateTempoChannel(GROUP_TEMPO, CHANNEL_TEMPO_TOMORROW_INFO, -1);
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

            updatekVAChannel(GROUP_DAILY, CHANNEL_PEAK_POWER_DAY_MINUS_1, values.baseValue[dSize - 1].value);
            updateState(GROUP_DAILY, CHANNEL_PEAK_POWER_TS_DAY_MINUS_1,
                    new DateTimeType(values.baseValue[dSize - 1].date.atZone(zoneId)));

            updatekVAChannel(GROUP_DAILY, CHANNEL_PEAK_POWER_DAY_MINUS_2, values.baseValue[dSize - 2].value);
            updateState(GROUP_DAILY, CHANNEL_PEAK_POWER_TS_DAY_MINUS_2,
                    new DateTimeType(values.baseValue[dSize - 2].date.atZone(zoneId)));

            updatekVAChannel(GROUP_DAILY, CHANNEL_PEAK_POWER_DAY_MINUS_3, values.baseValue[dSize - 3].value);
            updateState(GROUP_DAILY, CHANNEL_PEAK_POWER_TS_DAY_MINUS_3,
                    new DateTimeType(values.baseValue[dSize - 3].date.atZone(zoneId)));

            updatePowerTimeSeries(GROUP_DAILY, CHANNEL_MAX_POWER, values.baseValue);
            updatePowerTimeSeries(GROUP_WEEKLY, CHANNEL_MAX_POWER, values.weekValue);
            updatePowerTimeSeries(GROUP_MONTHLY, CHANNEL_MAX_POWER, values.monthValue);
            updatePowerTimeSeries(GROUP_YEARLY, CHANNEL_MAX_POWER, values.yearValue);

        }, () -> {
            updateKwhChannel(GROUP_DAILY, CHANNEL_PEAK_POWER_DAY_MINUS_1, Double.NaN);
            updateState(GROUP_DAILY, CHANNEL_PEAK_POWER_TS_DAY_MINUS_1, UnDefType.UNDEF);

            updateKwhChannel(GROUP_DAILY, CHANNEL_PEAK_POWER_DAY_MINUS_2, Double.NaN);
            updateState(GROUP_DAILY, CHANNEL_PEAK_POWER_TS_DAY_MINUS_2, UnDefType.UNDEF);

            updateKwhChannel(GROUP_DAILY, CHANNEL_PEAK_POWER_DAY_MINUS_3, Double.NaN);
            updateState(GROUP_DAILY, CHANNEL_PEAK_POWER_TS_DAY_MINUS_3, UnDefType.UNDEF);
        });
    }

    /**
     * Request new daily/weekly data and updates channels
     */
    private synchronized void updateEnergyData() {
        dailyConsumption.getValue().ifPresentOrElse(values -> {
            int dSize = values.baseValue.length;

            updateKwhChannel(GROUP_DAILY, CHANNEL_DAY_MINUS_1, values.baseValue[dSize - 1].value);
            updateKwhChannel(GROUP_DAILY, CHANNEL_DAY_MINUS_2, values.baseValue[dSize - 2].value);
            updateKwhChannel(GROUP_DAILY, CHANNEL_DAY_MINUS_3, values.baseValue[dSize - 3].value);

            int idxCurrentYear = values.yearValue.length - 1;
            int idxCurrentWeek = values.weekValue.length - 1;
            int idxCurrentMonth = values.monthValue.length - 1;

            updateKwhChannel(GROUP_WEEKLY, CHANNEL_WEEK_MINUS_0, values.weekValue[idxCurrentWeek].value);
            updateKwhChannel(GROUP_WEEKLY, CHANNEL_WEEK_MINUS_1, values.weekValue[idxCurrentWeek - 1].value);
            if (idxCurrentWeek - 2 >= 0) {
                updateKwhChannel(GROUP_WEEKLY, CHANNEL_WEEK_MINUS_2, values.weekValue[idxCurrentWeek - 2].value);
            }

            updateKwhChannel(GROUP_MONTHLY, CHANNEL_MONTH_MINUS_0, values.monthValue[idxCurrentMonth].value);
            updateKwhChannel(GROUP_MONTHLY, CHANNEL_MONTH_MINUS_1, values.monthValue[idxCurrentMonth - 1].value);
            if (idxCurrentMonth - 2 >= 0) {
                updateKwhChannel(GROUP_MONTHLY, CHANNEL_MONTH_MINUS_2, values.monthValue[idxCurrentMonth - 2].value);
            }

            updateKwhChannel(GROUP_YEARLY, CHANNEL_YEAR_MINUS_0, values.yearValue[idxCurrentYear].value);
            updateKwhChannel(GROUP_YEARLY, CHANNEL_YEAR_MINUS_1, values.yearValue[idxCurrentYear - 1].value);
            if (idxCurrentYear - 2 >= 0) {
                updateKwhChannel(GROUP_YEARLY, CHANNEL_YEAR_MINUS_2, values.yearValue[idxCurrentYear - 2].value);
            }

            updateConsumptionTimeSeries(GROUP_DAILY, CHANNEL_CONSUMPTION, values.baseValue);
            updateConsumptionTimeSeries(GROUP_WEEKLY, CHANNEL_CONSUMPTION, values.weekValue);
            updateConsumptionTimeSeries(GROUP_MONTHLY, CHANNEL_CONSUMPTION, values.monthValue);
            updateConsumptionTimeSeries(GROUP_YEARLY, CHANNEL_CONSUMPTION, values.yearValue);
        }, () -> {
            updateKwhChannel(GROUP_DAILY, CHANNEL_DAY_MINUS_1, Double.NaN);
            updateKwhChannel(GROUP_DAILY, CHANNEL_DAY_MINUS_2, Double.NaN);
            updateKwhChannel(GROUP_DAILY, CHANNEL_DAY_MINUS_3, Double.NaN);

            updateKwhChannel(GROUP_WEEKLY, CHANNEL_WEEK_MINUS_0, Double.NaN);
            updateKwhChannel(GROUP_WEEKLY, CHANNEL_WEEK_MINUS_1, Double.NaN);
            updateKwhChannel(GROUP_WEEKLY, CHANNEL_WEEK_MINUS_2, Double.NaN);

            updateKwhChannel(GROUP_MONTHLY, CHANNEL_MONTH_MINUS_0, Double.NaN);
            updateKwhChannel(GROUP_MONTHLY, CHANNEL_MONTH_MINUS_1, Double.NaN);
            updateKwhChannel(GROUP_MONTHLY, CHANNEL_MONTH_MINUS_2, Double.NaN);

            updateKwhChannel(GROUP_YEARLY, CHANNEL_YEAR_MINUS_0, Double.NaN);
            updateKwhChannel(GROUP_YEARLY, CHANNEL_YEAR_MINUS_1, Double.NaN);
            updateKwhChannel(GROUP_YEARLY, CHANNEL_YEAR_MINUS_2, Double.NaN);
        });
    }

    /**
     * Request new loadCurve data and updates channels
     */
    private synchronized void updateLoadCurveData() {
        loadCurveConsumption.getValue().ifPresentOrElse(values -> {
            updatePowerTimeSeries(GROUP_LOAD_CURVE, CHANNEL_POWER, values.baseValue);
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

                Instant timestamp = iv[i].date.atZone(zoneId).toInstant();

                if (Double.isNaN(iv[i].value)) {
                    continue;
                }
                timeSeries.add(timestamp, new DecimalType(iv[i].value));
            } catch (Exception ex) {
                logger.error("error occurs durring updatePowerTimeSeries for {} : {}", config.prmId, ex.getMessage(),
                        ex);
                ;
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

            Instant timestamp = iv[i].date.atZone(zoneId).toInstant();

            if (Double.isNaN(iv[i].value)) {
                continue;
            }
            timeSeries.add(timestamp, new DecimalType(iv[i].value));
        }

        sendTimeSeries(groupId, channelId, timeSeries);
    }

    private void updateKwhChannel(String groupId, String channelId, double consumption) {
        logger.debug("Update channel ({}) {} with {}", config.prmId, channelId, consumption);
        updateState(groupId, channelId,
                Double.isNaN(consumption) ? UnDefType.UNDEF : new QuantityType<>(consumption, Units.KILOWATT_HOUR));
    }

    private void updatekVAChannel(String groupId, String channelId, double power) {
        logger.debug("Update channel ({}) {} with {}", config.prmId, channelId, power);
        updateState(groupId, channelId, Double.isNaN(power) ? UnDefType.UNDEF
                : new QuantityType<>(power, MetricPrefix.KILO(Units.VOLT_AMPERE)));
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
        logger.debug("getConsumptionData for {} from {} to {}", config.prmId,
                from.format(DateTimeFormatter.ISO_LOCAL_DATE), to.format(DateTimeFormatter.ISO_LOCAL_DATE));

        EnedisHttpApi api = this.enedisApi;
        if (api != null) {
            try {
                MeterReading meterReading = api.getEnergyData(this, this.userId, config.prmId, from, to);
                return meterReading;
            } catch (LinkyException e) {
                logger.debug("Exception when getting consumption data for {} : {}", config.prmId, e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }

        return null;
    }

    private @Nullable MeterReading getLoadCurveConsumption(LocalDate from, LocalDate to) {
        logger.debug("getLoadCurveConsumption for {} from {} to {}", config.prmId,
                from.format(DateTimeFormatter.ISO_LOCAL_DATE), to.format(DateTimeFormatter.ISO_LOCAL_DATE));

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
        logger.debug("getPowerData for {} from {} to {}", config.prmId, from.format(DateTimeFormatter.ISO_LOCAL_DATE),
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

    private @Nullable MeterReading getMeterReadingAfterChecks(@Nullable MeterReading meterReading) {
        try {
            checkData(meterReading);
        } catch (LinkyException e) {
            logger.debug("Consumption data: {} {}", config.prmId, e.getMessage());
            return null;
        }

        if (!isDataLastDayAvailable(meterReading)) {
            logger.debug("Data including yesterday are not yet available");
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

    private boolean isDataLastDayAvailable(@Nullable MeterReading meterReading) {

        IntervalReading[] iv = meterReading.baseValue;

        logData(iv, "Last day", DateTimeFormatter.ISO_LOCAL_DATE, Target.LAST);
        return iv != null && iv.length != 0 && !iv[iv.length - 1].value.isNaN();
    }

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
                for (int i = size - 3; i < size; i++) {
                    logData(ivArray, i, title, dateTimeFormatter);
                }
            }
        }
    }

    private void logData(IntervalReading[] ivArray, int index, String title, DateTimeFormatter dateTimeFormatter) {
        try {
            IntervalReading iv = ivArray[index];
            String date = "";
            if (iv.date != null) {
                date = iv.date.format(dateTimeFormatter);
            }
            logger.debug("({}) {} {} value {}", config.prmId, title, date, iv.value);
        } catch (Exception e) {
            logger.error("error during logData", e);
        }
    }

    public void saveConfiguration(Configuration config) {
        updateConfiguration(config);
    }
}
