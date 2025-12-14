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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sedif.internal.api.ExpiringDayCache;
import org.openhab.binding.sedif.internal.api.helpers.MeterReadingHelper;
import org.openhab.binding.sedif.internal.config.SedifConfiguration;
import org.openhab.binding.sedif.internal.dto.Contract;
import org.openhab.binding.sedif.internal.dto.ContractDetail;
import org.openhab.binding.sedif.internal.dto.ContractDetail.CompteInfo;
import org.openhab.binding.sedif.internal.dto.MeterReading;
import org.openhab.binding.sedif.internal.dto.MeterReading.Data.Consommation;
import org.openhab.binding.sedif.internal.dto.SedifState;
import org.openhab.binding.sedif.internal.types.CommunicationFailedException;
import org.openhab.binding.sedif.internal.types.InvalidSessionException;
import org.openhab.binding.sedif.internal.types.SedifException;
import org.openhab.core.OpenHAB;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.TimeSeries.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link ThingSedifHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent Arnal - Initial contribution
 */

@NonNullByDefault
public class ThingSedifHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ThingSedifHandler.class);
    private @Nullable ScheduledFuture<?> refreshJob;

    private static final String JSON_DIR = OpenHAB.getUserDataFolder() + File.separatorChar + "sedif";

    private static final Random RANDOM_NUMBERS = new Random();
    private static final int REFRESH_HOUR_OF_DAY = 1;
    private static final int REFRESH_MINUTE_OF_DAY = RANDOM_NUMBERS.nextInt(60);
    private static final int REFRESH_INTERVAL_IN_MIN = 120;
    private static final int HISTORICAL_LOOKBACK_DAYS = 89;

    private String contractName;
    private String contractId;
    private String numCompteur;

    private @Nullable CompteInfo currentMeterInfo;

    private SedifState sedifState;

    private final ExpiringDayCache<ContractDetail> contractDetail;
    private final ExpiringDayCache<MeterReading> consumption;

    private final Gson gson;

    public ThingSedifHandler(Thing thing, LocaleProvider localeProvider, TimeZoneProvider timeZoneProvider, Gson gson) {
        super(thing);

        contractName = "";
        contractId = "";
        numCompteur = "";

        this.gson = gson;

        this.sedifState = new SedifState();

        this.contractDetail = new ExpiringDayCache<ContractDetail>("contractDetail", REFRESH_HOUR_OF_DAY,
                REFRESH_MINUTE_OF_DAY, () -> {
                    try {
                        Bridge lcBridge = getBridge();
                        if (lcBridge != null && lcBridge.getHandler() instanceof BridgeSedifWebHandler bridgeSedif) {
                            return bridgeSedif.getContractDetails(contractId);
                        }

                        return null;
                    } catch (CommunicationFailedException ex) {
                        // We return null, ExpiringDayCache logic will retry the operation later.
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
                        logger.error("Failed to get contract details", ex);
                        return null;
                    } catch (InvalidSessionException ex) {
                        // In case of session error, we force the bridge to reconnect after a delay
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());

                        Bridge lcBridge = getBridge();
                        if (lcBridge != null && lcBridge.getHandler() instanceof BridgeSedifWebHandler bridgeSedif) {
                            bridgeSedif.scheduleReconnect();
                        }
                        return null;
                    } catch (SedifException ex) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
                        return null;
                    }
                });

        this.consumption = new ExpiringDayCache<MeterReading>("dailyConsumption", REFRESH_HOUR_OF_DAY,
                REFRESH_MINUTE_OF_DAY, () -> {
                    LocalDate today = LocalDate.now();

                    try {
                        MeterReading meterReading = getConsumptionData(today.minusDays(HISTORICAL_LOOKBACK_DAYS), today,
                                false);
                        if (meterReading != null) {
                            MeterReadingHelper.calcAgregat(meterReading);
                        }
                        return meterReading;
                    } catch (CommunicationFailedException ex) {
                        // We return null, EpxiringDayCache logic will retry the operation later.
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
                        logger.warn("Failed to get consumption data", ex);
                        return null;
                    } catch (InvalidSessionException ex) {
                        // In case of session error, we force the bridge to reconnect after a delay
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
                        Bridge lcBridge = getBridge();
                        if (lcBridge != null && lcBridge.getHandler() instanceof BridgeSedifWebHandler bridgeSedif) {
                            bridgeSedif.scheduleReconnect();
                        }
                        return null;
                    } catch (SedifException ex) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, ex.getMessage());
                        return null;
                    }
                });
    }

    @Override
    public void initialize() {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof BridgeSedifWebHandler bridgeHandler) {
            initialize(bridgeHandler, bridge.getStatus());
        } else {
            initialize(null, bridge == null ? null : bridge.getStatus());
        }
    }

    private void initialize(@Nullable BridgeSedifWebHandler bridgeHandler, @Nullable ThingStatus bridgeStatus) {
        if (bridgeHandler == null || bridgeStatus == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
            return;
        }
        if (bridgeStatus != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        SedifConfiguration lcConfig = getConfigAs(SedifConfiguration.class);

        if (!lcConfig.seemsValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-mandatory-settings");
            return;
        }

        contractName = lcConfig.contractId;
        numCompteur = lcConfig.meterId;

        contractDetail.invalidate();
        consumption.invalidate();

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.submit(() -> {
            initializeAndSetupRefreshJob(bridgeHandler);
        });
    }

    private synchronized void initializeAndSetupRefreshJob(BridgeSedifWebHandler bridgeHandler) {
        loadSedifState();

        if (sedifState.getLastIndexDate() == null) {
            sedifState.setLastIndexDate(LocalDate.of(1980, 1, 1));
            saveSedifState();
        }

        setupRefreshJob(bridgeHandler);
    }

    private void loadSedifState() {
        File folder = new File(JSON_DIR);

        if (!folder.exists()) {
            logger.debug("Creating directory {}", folder);
            folder.mkdirs();
        }

        File file = null;
        try {
            file = new File(JSON_DIR + File.separator + "sedif_" + numCompteur + ".json");

            if (file.exists()) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String content = new String(bytes, StandardCharsets.UTF_8);
                SedifState newState = gson.fromJson(content, SedifState.class);
                if (newState != null) {
                    sedifState = newState;
                }
                logger.debug("Sedif MetaData information read from {}", file.getAbsolutePath());
            }
        } catch (InterruptedIOException ioe) {
            logger.warn("Couldn't read Sedif MetaData information from file '{}'.", file.getAbsolutePath());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Couldn't read Sedif MetaData information from file " + file.getAbsolutePath());
        } catch (IOException ioe) {
            logger.warn("Couldn't read Sedif MetaData information from file '{}'.", file.getAbsolutePath());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Couldn't read Sedif MetaData information from file " + file.getAbsolutePath());
        }
    }

    private void saveSedifState() {
        File file = null;

        if (!sedifState.hasModifications()) {
            return;
        }

        try {
            file = new File(JSON_DIR + File.separator + "sedif_" + numCompteur + ".json");

            if (!file.exists()) {
                File parentFile = file.getParentFile();
                if (parentFile != null) {
                    parentFile.mkdirs();
                }
                file.createNewFile();
            }

            try (FileOutputStream os = new FileOutputStream(file)) {
                String js = gson.toJson(sedifState);

                byte[] bt = js.getBytes();
                os.write(bt);
                os.flush();
            }
            logger.debug("Sedif MetaData information written to {}", file.getAbsolutePath());
        } catch (IOException ioe) {
            logger.warn("Couldn't write Sedif MetaData information to file '{}'.", file.getAbsolutePath());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Couldn't write Sedif MetaData information to file " + file.getAbsolutePath());
        }
    }

    public void cancelRefreshJob() {
        ScheduledFuture<?> job = this.refreshJob;
        if (job != null && !job.isCancelled()) {
            job.cancel(true);
            refreshJob = null;
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Sedif handler {}", numCompteur);
        cancelRefreshJob();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Refreshing channel {}", channelUID.getId());
            updateData();
        } else {
            logger.debug("The Sedif binding is read-only and can not handle command {}", command);
        }
    }

    /**
     * Request new data and updates channels
     */
    private void updateData() {
        if (thing.getStatus() == ThingStatus.OFFLINE
                && thing.getStatusInfo().getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR) {
            updateStatus(ThingStatus.UNKNOWN);
        }
        updateContractDetail();
        updateHistoricalConsumptionData();
        updateConsumptionData();
        saveSedifState();

        if (this.getThing().getStatus() == ThingStatus.UNKNOWN) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    private void updateHistoricalConsumptionData() {
        logger.trace("updateHistoricalConsumptionData() called");

        int periodLength = 90;
        LocalDate currentDate = LocalDate.now();
        currentDate = currentDate.minusDays(periodLength);

        LocalDate lastUpdateDate = sedifState.getLastIndexDate();

        if (!currentDate.isAfter(lastUpdateDate)) {
            return;
        }

        LocalDate newLastUpdateDate = lastUpdateDate;
        boolean hasData = true;
        boolean hasAlreadyRetrieveData = false;

        while ((hasData || !hasAlreadyRetrieveData) && currentDate.isAfter(lastUpdateDate)) {
            LocalDate startDate = currentDate.minusDays(periodLength - 1);
            try {
                logger.trace("Retrieve data from {} to {}:", startDate, currentDate);
                MeterReading meterReading = getConsumptionData(startDate, currentDate, true);
                if (meterReading != null) {
                    Consommation[] consommation = meterReading.data.consommation;
                    if (consommation != null) {
                        newLastUpdateDate = consommation[consommation.length - 1].dateIndex.toLocalDate();
                    }
                    hasData = true;
                } else {
                    hasData = false;
                }

                if (hasData) {
                    hasAlreadyRetrieveData = true;
                } else {
                    currentDate = startDate;
                }

                if (hasAlreadyRetrieveData) {
                    if (!hasData) {
                        continue;
                    }
                }
            } catch (SedifException ex) {
                logger.warn("Unable to retrieve data from {} to {}:", startDate, currentDate, ex);
            }
            currentDate = startDate;
        }

        sedifState.setLastIndexDate(newLastUpdateDate);
        saveSedifState();
    }

    public @Nullable MeterReading getConsumptionData(LocalDate startDate, LocalDate currentDate,
            boolean updateHistorical) throws SedifException {
        logger.trace("startDate: {}, currentDate: {}", startDate, currentDate);

        Bridge lcBridge = getBridge();
        if (lcBridge != null && lcBridge.getHandler() instanceof BridgeSedifWebHandler bridgeSedif) {
            CompteInfo lcCurrentMeterInfo = currentMeterInfo;

            if (lcCurrentMeterInfo != null) {
                MeterReading meterReading = bridgeSedif.getConsumptionData(contractId, lcCurrentMeterInfo, startDate,
                        currentDate);

                if (updateHistorical && meterReading == null) {
                    return null;
                }

                if (meterReading != null) {
                    return sedifState.updateMeterReading(meterReading);
                } else {
                    return sedifState.getMeterReading();
                }
            } else {
                throw new SedifException("currentMeterInfo is null");
            }
        }

        return null;
    }

    /**
     * Request new daily/weekly data and updates channels
     */
    private void updateContractDetail() {
        logger.trace("updateContractDetail() called");
        contractDetail.getValue().ifPresentOrElse(values -> {

            CompteInfo meterInfo = null;

            for (CompteInfo compteInfo : values.compteInfo) {
                if (compteInfo.numCompteur.equals(numCompteur)) {
                    meterInfo = compteInfo;
                }
            }

            if (meterInfo == null || meterInfo.eLma.isBlank()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/cant-find-meter");
                return;
            }

            this.currentMeterInfo = meterInfo;

            Map<String, String> props = this.editProperties();

            Contract contrat = values.contrat;
            if (contrat != null) {
                addProps(props, PROPERTY_CONTRACT_ORGANIZING_AUTHORITY, contrat.autoriteOrganisatrice);
                addProps(props, PROPERTY_CONTRACT_DATE_SORTIE_EPT, contrat.dateSortieEPT);
                addProps(props, PROPERTY_CONTRACT_ICL_ACTIVE, "" + contrat.iclActive);
                addProps(props, PROPERTY_CONTRACT_NAME, contrat.name);
            }
            addProps(props, PROPERTY_CONTRACT_BALANCE, "" + values.solde);

            CompteInfo comptInfo = values.compteInfo.get(0);
            addProps(props, PROPERTY_ELMA, comptInfo.eLma);
            addProps(props, PROPERTY_ELMB, comptInfo.eLmb);
            addProps(props, PROPERTY_ID_PDS, comptInfo.idPds);
            addProps(props, PROPERTY_NUM_METER, comptInfo.numCompteur);

            updateProperties(props);
        }, () -> {
            updateState(GROUP_BASE, CHANNEL_CONSUMPTION, new QuantityType<>(0.00, Units.LITRE));
        });
    }

    private void addProps(Map<String, String> props, String key, @Nullable String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        props.put(key, value);
    }

    private float getConso(MeterReading mr, String dtKey) {
        Consommation conso = mr.data.getEntries(dtKey);
        if (conso != null) {
            return conso.consommation;
        }

        return 0;
    }

    /**
     * Request new daily/weekly data and updates channels
     */
    private void updateConsumptionData() {
        logger.trace("updateConsumptionData() called");

        consumption.getValue().ifPresentOrElse(values -> {
            logger.trace("updateConsumptionData:getValue()");
            LocalDate now = LocalDate.now();
            Consommation[] consommation = values.data.consommation;

            // ===========================
            // Daily conso
            // ===========================
            double yesterdayConso = 0;
            double dayConsoMinus2 = 0;
            double dayConsoMinus3 = 0;

            String yesterday = now.minusDays(1).toString();
            String dayMinus2 = now.minusDays(2).toString();
            String dayMinus3 = now.minusDays(3).toString();

            yesterdayConso = getConso(values, yesterday);
            dayConsoMinus2 = getConso(values, dayMinus2);
            dayConsoMinus3 = getConso(values, dayMinus3);

            logger.trace("updateConsumptionData> updateState/yesterdayConso : {}", yesterdayConso);
            logger.trace("updateConsumptionData> updateState/dayConsoMinus2 : {}", dayConsoMinus2);
            logger.trace("updateConsumptionData> updateState/dayConsoMinus3 : {}", dayConsoMinus3);

            updateState(GROUP_DAILY_CONSUMPTION, CHANNEL_DAILY_YESTERDAY_CONSUMPTION,
                    new QuantityType<>(yesterdayConso, Units.LITRE));
            updateState(GROUP_DAILY_CONSUMPTION, CHANNEL_DAILY_DAY_MINUS_2_CONSUMPTION,
                    new QuantityType<>(dayConsoMinus2, Units.LITRE));
            updateState(GROUP_DAILY_CONSUMPTION, CHANNEL_DAILY_DAY_MINUS_3_CONSUMPTION,
                    new QuantityType<>(dayConsoMinus3, Units.LITRE));

            // ===========================
            // Week conso
            // ===========================
            double thisWeekConso = 0;
            double lastWeekConso = 0;
            double weekConsoMinus2 = 0;

            Consommation[] weekConso = values.data.weekConso;

            LocalDate thisWeek = now;
            LocalDate lastWeek = now.minusWeeks(1);
            LocalDate weekMinus2 = now.minusWeeks(2);

            String thisWeekKey = thisWeek.get(WeekFields.ISO.weekBasedYear()) + "-w-"
                    + thisWeek.get(WeekFields.ISO.weekOfWeekBasedYear());
            String lastWeekKey = lastWeek.get(WeekFields.ISO.weekBasedYear()) + "-w-"
                    + lastWeek.get(WeekFields.ISO.weekOfWeekBasedYear());
            String weekMinus2Key = weekMinus2.get(WeekFields.ISO.weekBasedYear()) + "-w-"
                    + weekMinus2.get(WeekFields.ISO.weekOfWeekBasedYear());

            thisWeekConso = getConso(values, thisWeekKey);
            lastWeekConso = getConso(values, lastWeekKey);
            weekConsoMinus2 = getConso(values, weekMinus2Key);

            updateState(GROUP_WEEKLY_CONSUMPTION, CHANNEL_WEEKLY_THIS_WEEK_CONSUMPTION,
                    new QuantityType<>(thisWeekConso, Units.LITRE));
            updateState(GROUP_WEEKLY_CONSUMPTION, CHANNEL_WEEKLY_LAST_WEEK_CONSUMPTION,
                    new QuantityType<>(lastWeekConso, Units.LITRE));
            updateState(GROUP_WEEKLY_CONSUMPTION, CHANNEL_WEEKLY_WEEK_MINUS_2_CONSUMPTION,
                    new QuantityType<>(weekConsoMinus2, Units.LITRE));

            // ===========================
            // Month conso
            // ===========================
            double thisMonthConso = 0;
            double lastMonthConso = 0;
            double monthConsoMinus2 = 0;

            LocalDate thisMonth = now;
            LocalDate lastMonth = now.minusMonths(1);
            LocalDate monthMinus2 = now.minusMonths(2);

            String thisMonthKey = thisMonth.getYear() + "-" + thisMonth.getMonthValue();
            String lastMonthKey = lastMonth.getYear() + "-" + lastMonth.getMonthValue();
            String monthMinus2Key = monthMinus2.getYear() + "-" + monthMinus2.getMonthValue();

            thisMonthConso = getConso(values, thisMonthKey);
            lastMonthConso = getConso(values, lastMonthKey);
            monthConsoMinus2 = getConso(values, monthMinus2Key);

            updateState(GROUP_MONTHLY_CONSUMPTION, CHANNEL_MONTHLY_THIS_MONTH_CONSUMPTION,
                    new QuantityType<>(thisMonthConso, Units.LITRE));
            updateState(GROUP_MONTHLY_CONSUMPTION, CHANNEL_MONTHLY_LAST_MONTH_CONSUMPTION,
                    new QuantityType<>(lastMonthConso, Units.LITRE));
            updateState(GROUP_MONTHLY_CONSUMPTION, CHANNEL_MONTHLY_MONTH_MINUS_2_CONSUMPTION,
                    new QuantityType<>(monthConsoMinus2, Units.LITRE));

            // ===========================
            // Year conso
            // ===========================
            double thisYearConso = 0;
            double lastYearConso = 0;
            double yearConsoMinus2 = 0;

            String thisYearKey = "" + now.getYear();
            String lastYearKey = "" + now.minusYears(1).getYear();
            String yearMinus2Key = "" + now.minusYears(2).getYear();

            thisYearConso = getConso(values, thisYearKey);
            lastYearConso = getConso(values, lastYearKey);
            yearConsoMinus2 = getConso(values, yearMinus2Key);

            updateState(GROUP_YEARLY_CONSUMPTION, CHANNEL_YEARLY_THIS_YEAR_CONSUMPTION,
                    new QuantityType<>(thisYearConso, Units.LITRE));
            updateState(GROUP_YEARLY_CONSUMPTION, CHANNEL_YEARLY_LAST_YEAR_CONSUMPTION,
                    new QuantityType<>(lastYearConso, Units.LITRE));
            updateState(GROUP_YEARLY_CONSUMPTION, CHANNEL_YEARLY_YEAR_MINUS_2_CONSUMPTION,
                    new QuantityType<>(yearConsoMinus2, Units.LITRE));

            updateState(GROUP_BASE, CHANNEL_MEAN_WATER_PRICE,
                    new QuantityType<>(values.prixMoyenEau, CurrencyUnits.BASE_CURRENCY));

            if (consommation != null && consommation.length > 0) {
                sedifState.setLastIndexDate(consommation[consommation.length - 1].dateIndex.toLocalDate());
            }
            updateConsumptionTimeSeries(GROUP_DAILY_CONSUMPTION, CHANNEL_CONSUMPTION, values.data.consommation);
            updateConsumptionTimeSeries(GROUP_WEEKLY_CONSUMPTION, CHANNEL_CONSUMPTION, values.data.weekConso);
            updateConsumptionTimeSeries(GROUP_MONTHLY_CONSUMPTION, CHANNEL_CONSUMPTION, values.data.monthConso);
            updateConsumptionTimeSeries(GROUP_YEARLY_CONSUMPTION, CHANNEL_CONSUMPTION, values.data.yearConso);

            logger.trace("updateConsumptionData:getValue() end");

        }, () -> {
            logger.trace("updateConsumptionData:getValue():noValuePresent");
            updateState(GROUP_BASE, CHANNEL_CONSUMPTION, new QuantityType<>(0.00, Units.LITRE));
        });
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof BridgeSedifWebHandler bridgeHandler) {
            initialize(bridgeHandler, bridgeStatusInfo.getStatus());
        } else {
            initialize(null, bridgeStatusInfo.getStatus());
        }
    }

    private void setupRefreshJob(BridgeSedifWebHandler bridgeHandler) {
        Contract contract = bridgeHandler.getContract(contractName);
        if (contract != null) {
            contractId = Objects.requireNonNull(contract.id);

            updateData();

            final LocalDateTime now = LocalDateTime.now();
            final LocalDateTime nextDayFirstTimeUpdate = now.plusDays(1).withHour(REFRESH_HOUR_OF_DAY)
                    .withMinute(REFRESH_MINUTE_OF_DAY).truncatedTo(ChronoUnit.MINUTES);

            cancelRefreshJob();
            long initialDelay = ChronoUnit.MINUTES.between(now, nextDayFirstTimeUpdate) % REFRESH_INTERVAL_IN_MIN + 1;
            long delay = REFRESH_INTERVAL_IN_MIN;

            if (!consumption.isPresent() || !contractDetail.isPresent()) {
                initialDelay = 20;
            }
            refreshJob = scheduler.scheduleWithFixedDelay(this::updateData, initialDelay, delay, TimeUnit.MINUTES);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.missing-or-invalid-contract");
        }
    }

    protected void updateState(String groupId, String channelID, State state) {
        super.updateState(groupId + "#" + channelID, state);
    }

    protected void sendTimeSeries(String groupId, String channelID, TimeSeries timeSeries) {
        super.sendTimeSeries(groupId + "#" + channelID, timeSeries);
    }

    private void updateConsumptionTimeSeries(String groupId, String channelId, Consommation[] consoTab) {
        TimeSeries timeSeries = new TimeSeries(Policy.REPLACE);

        for (int i = 0; i < consoTab.length; i++) {
            Consommation conso = consoTab[i];
            if (conso == null) {
                continue;
            }

            LocalDateTime dt = conso.dateIndex;
            float consommation = conso.consommation;

            if (dt == null) {
                // dt can be null if we have no value in initial container day for this month !
                continue;
            }

            Instant timestamp = dt.toInstant(ZoneOffset.UTC);

            if (Double.isNaN(consommation)) {
                continue;
            }
            timeSeries.add(timestamp, new DecimalType(consommation));
        }

        sendTimeSeries(groupId, channelId, timeSeries);
    }

    public String getContractId() {
        return contractId;
    }

    public String getNumCompteur() {
        return numCompteur;
    }
}
