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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Quantity;
import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.linky.internal.api.EnedisHttpApi;
import org.openhab.binding.linky.internal.api.ExpiringDayCache;
import org.openhab.binding.linky.internal.config.LinkyThingRemoteConfiguration;
import org.openhab.binding.linky.internal.dto.Contact;
import org.openhab.binding.linky.internal.dto.Contract;
import org.openhab.binding.linky.internal.dto.Identity;
import org.openhab.binding.linky.internal.dto.IntervalReading;
import org.openhab.binding.linky.internal.dto.MetaData;
import org.openhab.binding.linky.internal.dto.MeterReading;
import org.openhab.binding.linky.internal.dto.PrmDetail;
import org.openhab.binding.linky.internal.dto.PrmInfo;
import org.openhab.binding.linky.internal.dto.UsagePoint;
import org.openhab.binding.linky.internal.dto.UserInfo;
import org.openhab.binding.linky.internal.types.LinkyException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.MetricPrefix;
import org.openhab.core.library.unit.Units;
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
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ThingLinkyRemoteHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */

@NonNullByDefault
public class ThingLinkyRemoteHandler extends ThingBaseRemoteHandler {
    private static final Random RANDOM_NUMBERS = new Random();
    private static final int REFRESH_HOUR_OF_DAY = 1;
    private static final int REFRESH_MINUTE_OF_DAY = RANDOM_NUMBERS.nextInt(60);
    private static final int REFRESH_INTERVAL_IN_MIN = 120;

    private final TimeZoneProvider timeZoneProvider;
    private final Logger logger = LoggerFactory.getLogger(ThingLinkyRemoteHandler.class);

    private final ExpiringDayCache<MetaData> metaData;
    private final ExpiringDayCache<MeterReading> dailyConsumption;
    private final ExpiringDayCache<MeterReading> dailyConsumptionMaxPower;
    private final ExpiringDayCache<MeterReading> loadCurveConsumption;

    private ZoneId zoneId = ZoneId.systemDefault();
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable EnedisHttpApi enedisApi;
    private double divider = 1.00;

    public String userId = "";

    private @Nullable ScheduledFuture<?> pollingJob = null;

    private enum Target {
        FIRST,
        LAST,
        ALL
    }

    public ThingLinkyRemoteHandler(Thing thing, LocaleProvider localeProvider, TimeZoneProvider timeZoneProvider) {
        super(thing);

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

        // We request data for yesterday and the day before yesterday
        // even if the data for the day before yesterday
        // This is only a workaround to an API bug that will return INTERNAL_SERVER_ERROR rather
        // than the expected data with a NaN value when the data for yesterday is not yet available.
        // By requesting two days, the API is not failing and you get the expected NaN value for yesterday
        // when the data is not yet available.
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

        // reread config to update timezone field
        config = getConfigAs(LinkyThingRemoteConfiguration.class);

        Bridge bridge = getBridge();
        if (bridge == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "@text/offline.missing-bridge");
            return;
        }

        if (bridge.getHandler() instanceof BridgeRemoteBaseHandler bridgeHandler) {
            enedisApi = bridgeHandler.getEnedisApi();
            divider = bridgeHandler.getDivider();

            updateStatus(ThingStatus.UNKNOWN);

            if (config.seemsValid()) {
                if (config.timezone.isBlank()) {
                    zoneId = this.timeZoneProvider.getTimeZone();
                } else {
                    zoneId = ZoneId.of(config.timezone);
                }

                if (bridgeHandler instanceof BridgeRemoteApiHandler && config.prmId.isBlank()) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/offline.config-error-mandatory-settings");
                    return;
                }

                if (!config.prmId.isBlank()) {
                    bridgeHandler.registerNewPrmId(config.prmId);
                }
                pollingJob = scheduler.schedule(this::pollingCode, 5, TimeUnit.SECONDS);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/offline.config-error-mandatory-settings");
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    public boolean supportNewApiFormat() throws LinkyException {
        Bridge bridge = getBridge();
        if (bridge == null) {
            throw new LinkyException("Unable to get bridge in supportNewApiFormat()");
        }

        if (bridge.getHandler() instanceof BridgeRemoteBaseHandler bridgeHandler) {
            return bridgeHandler.supportNewApiFormat();
        } else {
            throw new LinkyException("Unable to get bridgeHandler in supportNewApiFormat()");
        }
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

                updateStatus(ThingStatus.ONLINE);

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

    private synchronized void updateMetaData() {
        metaData.getValue().ifPresentOrElse(values -> {
            String title = values.identity.title;
            String firstName = values.identity.firstname;
            String lastName = values.identity.lastname;

            Map<String, String> props = this.editProperties();

            if (values.identity.internId == null) {
                values.identity.internId = values.identity.firstname + " " + values.identity.lastname;
            }
            userId = values.identity.internId;

            addProps(props, USER_ID, userId);

            addProps(props, PROPERTY_USAGEPOINT_ID, values.usagePoint.usagePointId);

            addProps(props, PROPERTY_IDENTITY, title + " " + firstName + " " + lastName);

            addProps(props, PROPERTY_CONTRACT_SEGMENT, values.contract.segment);
            addProps(props, PROPERTY_CONTRACT_CONTRACT_STATUS, values.contract.contractStatus);
            addProps(props, PROPERTY_CONTRACT_CONTRACT_TYPE, values.contract.contractType);
            addProps(props, PROPERTY_CONTRACT_DISTRIBUTION_TARIFF, values.contract.distributionTariff);
            addProps(props, PROPERTY_CONTRACT_LAST_ACTIVATION_DATE, values.contract.lastActivationDate);
            addProps(props, PROPERTY_CONTRACT_LAST_DISTRIBUTION_TARIFF_CHANGE_DATE,
                    values.contract.lastDistributionTariffChangeDate);
            addProps(props, PROPERTY_CONTRACT_OFF_PEAK_HOURS, values.contract.offpeakHours);
            addProps(props, PROPERTY_CONTRACT_SUBSCRIBED_POWER, values.contract.subscribedPower + " kVA");

            addProps(props, PROPERTY_USAGEPOINT_STATUS, values.usagePoint.usagePointStatus);
            addProps(props, PROPERTY_USAGEPOINT_METER_TYPE, values.usagePoint.meterType);
            addProps(props, PROPERTY_USAGEPOINT_METER_ADDRESS_CITY, values.usagePoint.usagePointAddresses.city);
            addProps(props, PROPERTY_USAGEPOINT_METER_ADDRESS_COUNTRY, values.usagePoint.usagePointAddresses.country);
            addProps(props, PROPERTY_USAGEPOINT_METER_ADDRESS_POSTAL_CODE,
                    values.usagePoint.usagePointAddresses.postalCode);
            addProps(props, PROPERTY_USAGEPOINT_METER_ADDRESS_STREET, values.usagePoint.usagePointAddresses.street);

            addProps(props, PROPERTY_CONTACT_MAIL, values.contact.email);
            addProps(props, PROPERTY_CONTACT_PHONE, values.contact.phone);

            this.updateProperties(props);
        }, () -> {
        });
    }

    private void addProps(Map<String, String> props, String key, @Nullable String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        props.put(key, value);
    }

    private @Nullable MetaData getMetaData() {
        try {
            EnedisHttpApi api = this.enedisApi;
            MetaData result = new MetaData();
            if (api != null) {
                if (supportNewApiFormat()) {
                    if (config.prmId.isBlank()) {
                        throw new LinkyException("@text/offline.config-error-mandatory-settings");
                    }
                    result.identity = api.getIdentity(this, config.prmId);
                    result.contact = api.getContact(this, config.prmId);
                    result.contract = api.getContract(this, config.prmId);
                    result.usagePoint = api.getUsagePoint(this, config.prmId);
                } else {
                    UserInfo userInfo = api.getUserInfo(this);
                    PrmInfo prmInfo = api.getPrmInfo(this, userInfo.userProperties.internId, config.prmId);
                    PrmDetail details = api.getPrmDetails(this, userInfo.userProperties.internId, prmInfo.idPrm);

                    config.prmId = prmInfo.idPrm;
                    result.identity = Identity.convertFromUserInfo(userInfo);
                    result.contact = Contact.convertFromUserInfo(userInfo);
                    result.contract = Contract.convertFromPrmDetail(details);
                    result.usagePoint = UsagePoint.convertFromPrmDetail(prmInfo, details);
                }
            }
            return result;
        } catch (LinkyException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }

        return null;
    }

    /**
     * Request new data and updates channels
     */
    private synchronized void updateData() {
        // If one of the cache is expired, force also a metaData refresh to prevent 500 error from Enedis servers !
        logger.debug("updateData() called");
        logger.debug("Cache state {} {} {}", dailyConsumption.isPresent(), dailyConsumptionMaxPower.isPresent(),
                loadCurveConsumption.isPresent());

        if (!dailyConsumption.isPresent() || !dailyConsumptionMaxPower.isPresent()
                || !loadCurveConsumption.isPresent()) {
            logger.debug("invalidate metaData cache to force refresh");
            metaData.invalidate();
        }

        updateMetaData();
        // Stop there if we are not able to get Metadata
        if (thing.getStatus() == ThingStatus.OFFLINE) {
            return;
        }

        updateEnergyData();
        updatePowerData();
        updateLoadCurveData();
    }

    private synchronized void updatePowerData() {
        if (isLinkedPowerData()) {
            dailyConsumptionMaxPower.getValue().ifPresentOrElse(values -> {
                int dSize = values.baseValue.length;

                updatekVAChannel(LINKY_REMOTE_DAILY_GROUP, CHANNEL_PEAK_POWER_DAY_MINUS_1,
                        values.baseValue[dSize - 1].value);
                updateState(LINKY_REMOTE_DAILY_GROUP, CHANNEL_PEAK_POWER_TS_DAY_MINUS_1,
                        new DateTimeType(values.baseValue[dSize - 1].date.atZone(zoneId)));

                updatekVAChannel(LINKY_REMOTE_DAILY_GROUP, CHANNEL_PEAK_POWER_DAY_MINUS_2,
                        values.baseValue[dSize - 2].value);
                updateState(LINKY_REMOTE_DAILY_GROUP, CHANNEL_PEAK_POWER_TS_DAY_MINUS_2,
                        new DateTimeType(values.baseValue[dSize - 2].date.atZone(zoneId)));

                updatekVAChannel(LINKY_REMOTE_DAILY_GROUP, CHANNEL_PEAK_POWER_DAY_MINUS_3,
                        values.baseValue[dSize - 3].value);
                updateState(LINKY_REMOTE_DAILY_GROUP, CHANNEL_PEAK_POWER_TS_DAY_MINUS_3,
                        new DateTimeType(values.baseValue[dSize - 3].date.atZone(zoneId)));

                updateTimeSeries(LINKY_REMOTE_DAILY_GROUP, CHANNEL_MAX_POWER, values.baseValue,
                        MetricPrefix.KILO(Units.VOLT_AMPERE));

                updateTimeSeries(LINKY_REMOTE_WEEKLY_GROUP, CHANNEL_MAX_POWER, values.weekValue,
                        MetricPrefix.KILO(Units.VOLT_AMPERE));

                updateTimeSeries(LINKY_REMOTE_MONTHLY_GROUP, CHANNEL_MAX_POWER, values.monthValue,
                        MetricPrefix.KILO(Units.VOLT_AMPERE));

                updateTimeSeries(LINKY_REMOTE_YEARLY_GROUP, CHANNEL_MAX_POWER, values.yearValue,
                        MetricPrefix.KILO(Units.VOLT_AMPERE));
            }, () -> {
                updateKwhChannel(LINKY_REMOTE_DAILY_GROUP, CHANNEL_PEAK_POWER_DAY_MINUS_1, Double.NaN);
                updateState(LINKY_REMOTE_DAILY_GROUP, CHANNEL_PEAK_POWER_TS_DAY_MINUS_1, UnDefType.UNDEF);

                updateKwhChannel(LINKY_REMOTE_DAILY_GROUP, CHANNEL_PEAK_POWER_DAY_MINUS_2, Double.NaN);
                updateState(LINKY_REMOTE_DAILY_GROUP, CHANNEL_PEAK_POWER_TS_DAY_MINUS_2, UnDefType.UNDEF);

                updateKwhChannel(LINKY_REMOTE_DAILY_GROUP, CHANNEL_PEAK_POWER_DAY_MINUS_3, Double.NaN);
                updateState(LINKY_REMOTE_DAILY_GROUP, CHANNEL_PEAK_POWER_TS_DAY_MINUS_3, UnDefType.UNDEF);
            });
        }
    }

    /**
     * Request new daily/weekly data and updates channels
     */
    private synchronized void updateEnergyData() {
        dailyConsumption.getValue().ifPresentOrElse(values -> {
            int dSize = values.baseValue.length;

            updateKwhChannel(LINKY_REMOTE_DAILY_GROUP, CHANNEL_DAY_MINUS_1, values.baseValue[dSize - 1].value);
            updateKwhChannel(LINKY_REMOTE_DAILY_GROUP, CHANNEL_DAY_MINUS_2, values.baseValue[dSize - 2].value);
            updateKwhChannel(LINKY_REMOTE_DAILY_GROUP, CHANNEL_DAY_MINUS_3, values.baseValue[dSize - 3].value);

            int idxCurrentYear = values.yearValue.length - 1;
            int idxCurrentWeek = values.weekValue.length - 1;
            int idxCurrentMonth = values.monthValue.length - 1;

            updateKwhChannel(LINKY_REMOTE_WEEKLY_GROUP, CHANNEL_WEEK_MINUS_0, values.weekValue[idxCurrentWeek].value);
            updateKwhChannel(LINKY_REMOTE_WEEKLY_GROUP, CHANNEL_WEEK_MINUS_1,
                    values.weekValue[idxCurrentWeek - 1].value);
            if (idxCurrentWeek - 2 >= 0) {
                updateKwhChannel(LINKY_REMOTE_WEEKLY_GROUP, CHANNEL_WEEK_MINUS_2,
                        values.weekValue[idxCurrentWeek - 2].value);
            }

            updateKwhChannel(LINKY_REMOTE_MONTHLY_GROUP, CHANNEL_MONTH_MINUS_0,
                    values.monthValue[idxCurrentMonth].value);
            updateKwhChannel(LINKY_REMOTE_MONTHLY_GROUP, CHANNEL_MONTH_MINUS_1,
                    values.monthValue[idxCurrentMonth - 1].value);
            if (idxCurrentMonth - 2 >= 0) {
                updateKwhChannel(LINKY_REMOTE_MONTHLY_GROUP, CHANNEL_MONTH_MINUS_2,
                        values.monthValue[idxCurrentMonth - 2].value);
            }

            updateKwhChannel(LINKY_REMOTE_YEARLY_GROUP, CHANNEL_YEAR_MINUS_0, values.yearValue[idxCurrentYear].value);
            updateKwhChannel(LINKY_REMOTE_YEARLY_GROUP, CHANNEL_YEAR_MINUS_1,
                    values.yearValue[idxCurrentYear - 1].value);
            if (idxCurrentYear - 2 >= 0) {
                updateKwhChannel(LINKY_REMOTE_YEARLY_GROUP, CHANNEL_YEAR_MINUS_2,
                        values.yearValue[idxCurrentYear - 2].value);
            }

            updateTimeSeries(LINKY_REMOTE_DAILY_GROUP, CHANNEL_CONSUMPTION, values.baseValue, Units.KILOWATT_HOUR);
            updateTimeSeries(LINKY_REMOTE_WEEKLY_GROUP, CHANNEL_CONSUMPTION, values.weekValue, Units.KILOWATT_HOUR);
            updateTimeSeries(LINKY_REMOTE_MONTHLY_GROUP, CHANNEL_CONSUMPTION, values.monthValue, Units.KILOWATT_HOUR);
            updateTimeSeries(LINKY_REMOTE_YEARLY_GROUP, CHANNEL_CONSUMPTION, values.yearValue, Units.KILOWATT_HOUR);
        }, () -> {
            updateKwhChannel(LINKY_REMOTE_DAILY_GROUP, CHANNEL_DAY_MINUS_1, Double.NaN);
            updateKwhChannel(LINKY_REMOTE_DAILY_GROUP, CHANNEL_DAY_MINUS_2, Double.NaN);
            updateKwhChannel(LINKY_REMOTE_DAILY_GROUP, CHANNEL_DAY_MINUS_3, Double.NaN);

            updateKwhChannel(LINKY_REMOTE_WEEKLY_GROUP, CHANNEL_WEEK_MINUS_0, Double.NaN);
            updateKwhChannel(LINKY_REMOTE_WEEKLY_GROUP, CHANNEL_WEEK_MINUS_1, Double.NaN);
            updateKwhChannel(LINKY_REMOTE_WEEKLY_GROUP, CHANNEL_WEEK_MINUS_2, Double.NaN);

            updateKwhChannel(LINKY_REMOTE_MONTHLY_GROUP, CHANNEL_MONTH_MINUS_0, Double.NaN);
            updateKwhChannel(LINKY_REMOTE_MONTHLY_GROUP, CHANNEL_MONTH_MINUS_1, Double.NaN);
            updateKwhChannel(LINKY_REMOTE_MONTHLY_GROUP, CHANNEL_MONTH_MINUS_2, Double.NaN);

            updateKwhChannel(LINKY_REMOTE_YEARLY_GROUP, CHANNEL_YEAR_MINUS_0, Double.NaN);
            updateKwhChannel(LINKY_REMOTE_YEARLY_GROUP, CHANNEL_YEAR_MINUS_1, Double.NaN);
            updateKwhChannel(LINKY_REMOTE_YEARLY_GROUP, CHANNEL_YEAR_MINUS_2, Double.NaN);
        });
    }

    /**
     * Request new loadCurve data and updates channels
     */
    private synchronized void updateLoadCurveData() {
        if (isLinked(LINKY_REMOTE_LOAD_CURVE_GROUP, CHANNEL_POWER)) {
            loadCurveConsumption.getValue().ifPresentOrElse(values -> {
                updateTimeSeries(LINKY_REMOTE_LOAD_CURVE_GROUP, CHANNEL_POWER, values.baseValue,
                        MetricPrefix.KILO(Units.VOLT_AMPERE));
            }, () -> {
            });
        }
    }

    private synchronized <T extends Quantity<T>> void updateTimeSeries(String groupId, String channelId,
            IntervalReading[] iv, Unit<T> unit) {
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
                timeSeries.add(timestamp, new QuantityType<>(iv[i].value, unit));
            } catch (Exception ex) {
                logger.error("error occurs durring updatePowerTimeSeries for {} : {}", config.prmId, ex.getMessage(),
                        ex);
            }
        }

        sendTimeSeries(groupId, channelId, timeSeries);
    }

    private void updateKwhChannel(String groupId, String channelId, double consumption) {
        updateState(groupId, channelId,
                Double.isNaN(consumption) ? UnDefType.UNDEF : new QuantityType<>(consumption, Units.KILOWATT_HOUR));
    }

    private void updatekVAChannel(String groupId, String channelId, double power) {
        updateState(groupId, channelId, Double.isNaN(power) ? UnDefType.UNDEF
                : new QuantityType<>(power, MetricPrefix.KILO(Units.VOLT_AMPERE)));
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
        return buildReport(startDay, endDay, separator);
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
                return api.getEnergyData(this, this.userId, config.prmId, from, to);
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
                return api.getLoadCurveData(this, this.userId, config.prmId, from, to);
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
                return api.getPowerData(this, this.userId, config.prmId, from, to);
            } catch (LinkyException e) {
                logger.debug("Exception when getting power data: {}", e.getMessage(), e);
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
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refreshing channel {} {}", config.prmId, channelUID.getId());
            updateData();
        } else {
            logger.debug("The Linky binding is read-only and can not handle command {}", command);
        }
    }

    public @Nullable MeterReading getMeterReadingAfterChecks(@Nullable MeterReading meterReading) {
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

                int startWeek = startDate.get(WeekFields.of(Locale.FRANCE).weekOfYear());
                int endWeek = endDate.get(WeekFields.of(Locale.FRANCE).weekOfYear());

                int yearsNum = endDate.getYear() - startDate.getYear() + 1;
                int monthsNum = (endDate.getYear() - startDate.getYear()) * 12 + endDate.getMonthValue()
                        - startDate.getMonthValue() + 1;

                int weeksNum = (endDate.getYear() - startDate.getYear()) * 52 + endWeek - startWeek + 1;

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
                int baseYear = startDate.getYear();
                int baseMonth = startDate.getMonthValue();
                int baseWeek = startWeek;

                for (int idx = 0; idx < size; idx++) {
                    IntervalReading ir = meterReading.baseValue[idx];
                    LocalDateTime dt = ir.date;
                    double value = ir.value;
                    value = value / divider;
                    ir.value = value;

                    int idxYear = dt.getYear() - baseYear;
                    int idxMonth = idxYear * 12 + dt.getMonthValue() - baseMonth;
                    int dtWeek = dt.get(WeekFields.of(Locale.FRANCE).weekOfYear());
                    int idxWeek = (idxYear * 52) + dtWeek - baseWeek;
                    int month = dt.getMonthValue();

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
        } else {
            throw new LinkyException("Invalid meterReading == null");
        }
    }

    private boolean isDataLastDayAvailable(@Nullable MeterReading meterReading) {
        if (meterReading != null) {
            IntervalReading[] iv = meterReading.baseValue;

            logData(iv, "Last day", DateTimeFormatter.ISO_LOCAL_DATE, Target.LAST);
            return iv.length != 0 && !iv[iv.length - 1].value.isNaN();
        }

        return false;
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

    private boolean isLinkedPowerData() {
        return (isLinked(LINKY_REMOTE_DAILY_GROUP, CHANNEL_PEAK_POWER_DAY_MINUS_1)
                || isLinked(LINKY_REMOTE_DAILY_GROUP, CHANNEL_PEAK_POWER_TS_DAY_MINUS_1)
                || isLinked(LINKY_REMOTE_DAILY_GROUP, CHANNEL_PEAK_POWER_DAY_MINUS_2)
                || isLinked(LINKY_REMOTE_DAILY_GROUP, CHANNEL_PEAK_POWER_TS_DAY_MINUS_2)
                || isLinked(LINKY_REMOTE_DAILY_GROUP, CHANNEL_PEAK_POWER_DAY_MINUS_3)
                || isLinked(LINKY_REMOTE_DAILY_GROUP, CHANNEL_PEAK_POWER_TS_DAY_MINUS_3)
                || isLinked(LINKY_REMOTE_DAILY_GROUP, CHANNEL_MAX_POWER)
                || isLinked(LINKY_REMOTE_WEEKLY_GROUP, CHANNEL_MAX_POWER)
                || isLinked(LINKY_REMOTE_MONTHLY_GROUP, CHANNEL_MAX_POWER)
                || isLinked(LINKY_REMOTE_YEARLY_GROUP, CHANNEL_MAX_POWER));
    }

    private boolean isLinked(String groupName, String channelName) {
        return isLinked(groupName + "#" + channelName);
    }
}
