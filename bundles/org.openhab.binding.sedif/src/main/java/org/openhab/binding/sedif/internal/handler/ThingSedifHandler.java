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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sedif.internal.api.ExpiringDayCache;
import org.openhab.binding.sedif.internal.api.SedifHttpApi;
import org.openhab.binding.sedif.internal.config.SedifConfiguration;
import org.openhab.binding.sedif.internal.dto.Contract;
import org.openhab.binding.sedif.internal.dto.ContractDetail;
import org.openhab.binding.sedif.internal.dto.ContractDetail.CompteInfo;
import org.openhab.binding.sedif.internal.dto.MeterReading;
import org.openhab.binding.sedif.internal.dto.MeterReading.Data.Consommation;
import org.openhab.binding.sedif.internal.dto.SedifState;
import org.openhab.binding.sedif.internal.types.SedifException;
import org.openhab.core.OpenHAB;
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

import com.google.gson.Gson;

/**
 * The {@link ThingSedifHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Laurent Arnal - Initial contribution
 */

@NonNullByDefault
@SuppressWarnings("null")
public class ThingSedifHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ThingSedifHandler.class);
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable SedifHttpApi sedifApi;

    private static final String JSON_DIR = OpenHAB.getUserDataFolder() + File.separatorChar + "sedif";

    private static final Random RANDOM_NUMBERS = new Random();
    private static final int REFRESH_HOUR_OF_DAY = 1;
    private static final int REFRESH_MINUTE_OF_DAY = RANDOM_NUMBERS.nextInt(60);
    private static final int REFRESH_INTERVAL_IN_MIN = 120;

    private String contractName;
    private String contractId;
    private String meterIdA;
    private String meterIdB;
    private String idPds;
    private String numCompteur;

    private @Nullable SedifState sedifState;

    private final ExpiringDayCache<ContractDetail> contractDetail;
    private final ExpiringDayCache<MeterReading> consumption;

    protected final Gson gson;

    private @Nullable ScheduledFuture<?> pollingJob = null;
    protected SedifConfiguration config;

    public ThingSedifHandler(Thing thing, LocaleProvider localeProvider, TimeZoneProvider timeZoneProvider, Gson gson) {
        super(thing);

        contractName = "";
        contractId = "";
        meterIdA = "";
        meterIdB = "";
        idPds = "";
        numCompteur = "";

        this.gson = gson;

        this.contractDetail = new ExpiringDayCache<ContractDetail>("contractDetail", REFRESH_HOUR_OF_DAY,
                REFRESH_MINUTE_OF_DAY, () -> {
                    ContractDetail contractDetail = getContractDetail();
                    return contractDetail;
                });

        this.consumption = new ExpiringDayCache<MeterReading>("dailyConsumption", REFRESH_HOUR_OF_DAY,
                REFRESH_MINUTE_OF_DAY, () -> {
                    LocalDate today = LocalDate.now();

                    try {
                        MeterReading meterReading = updateConsumptionData(today.minusDays(89), today, false);
                        meterReading.calcAgregat();
                        return meterReading;
                    } catch (SedifException ex) {
                        return null;
                    }
                });

        config = getConfigAs(SedifConfiguration.class);
    }

    @Override
    public synchronized void initialize() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }

        loadSedifState();
        if (sedifState.getLastIndexDate() == null) {
            sedifState.setLastIndexDate(LocalDate.of(1980, 1, 1));
        }

        // force reread data if we pause / start the thing
        this.contractDetail.invalidate();
        this.consumption.invalidate();

        BridgeSedifWebHandler bridgeHandler = (BridgeSedifWebHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            return;
        }

        if (config.seemsValid()) {
            contractName = config.contractId;
            numCompteur = config.meterId;

            pollingJob = scheduler.schedule(this::pollingCode, 5, TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-mandatory-settings");
        }

        saveSedifState();
    }

    private void loadSedifState() {
        File folder = new File(JSON_DIR);

        if (!folder.exists()) {
            logger.debug("Creating directory {}", folder);
            folder.mkdirs();
        }

        File file = null;
        try {
            file = new File(JSON_DIR + File.separator + "sedif.json");

            if (file.exists()) {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String content = new String(bytes, StandardCharsets.UTF_8);
                sedifState = gson.fromJson(content, SedifState.class);
            }
        } catch (IOException ioe) {
            logger.warn("Couldn't read Siemens MetaData information from file '{}'.", file.getAbsolutePath());
        }

        if (sedifState == null) {
            sedifState = new SedifState();
        }
    }

    private void saveSedifState() {
        File file = null;

        if (!sedifState.hasModifications()) {
            return;
        }

        try {
            file = new File(JSON_DIR + File.separator + "sedif.json");

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            try (FileOutputStream os = new FileOutputStream(file)) {
                String js = gson.toJson(sedifState);

                byte[] bt = js.getBytes();
                os.write(bt);
                os.flush();
            }
        } catch (IOException ioe) {
            logger.warn("Couldn't write Siemens MetaData information to file '{}'.", file.getAbsolutePath());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing the Linky handler {}", numCompteur);
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
        sedifApi = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Request new data and updates channels
     */
    private synchronized void updateData() {
        logger.trace("updateContractDetail() called");
        updateContractDetail();

        logger.trace("updateHistoricalConsumptionData() called");
        updateHistoricalConsumptionData();

        logger.trace("updateEnergyData() called");
        updateConsumptionData();
        saveSedifState();
    }

    private synchronized void updateHistoricalConsumptionData() {
        int periodLength = 90;
        LocalDate currentDate = LocalDate.now();
        currentDate = currentDate.minusDays(periodLength);

        LocalDate lastUpdateDate = sedifState.getLastIndexDate();
        LocalDate newLastUpdateDate = lastUpdateDate;
        boolean hasData = true;
        boolean hasAlreadyRetrieveData = false;
        int idx = 0;
        while (hasData && currentDate.isAfter(lastUpdateDate) /* && idx < 2 */) {
            LocalDate startDate = currentDate.minusDays(periodLength - 1);

            try {
                MeterReading meterReading = updateConsumptionData(startDate, currentDate, true);
                if (meterReading != null) {
                    newLastUpdateDate = meterReading.data.consommation[meterReading.data.consommation.length
                            - 1].dateIndex.toLocalDate();
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
            } catch (Exception ex) {
                logger.debug("aa:", ex);
            }
            currentDate = startDate;
            idx++;
        }

        sedifState.setLastIndexDate(newLastUpdateDate);
        saveSedifState();
    }

    public @Nullable MeterReading updateConsumptionData(LocalDate startDate, LocalDate currentDate,
            boolean updateHistorical) throws SedifException {
        logger.trace("startDate: {}, currentDate: {}", startDate, currentDate);

        MeterReading meterReading = getConsumptionData(startDate, currentDate);
        if (updateHistorical && meterReading == null) {
            return null;
        }
        meterReading = sedifState.updateMeterReading(meterReading);
        return meterReading;
    }

    /**
     * Request new daily/weekly data and updates channels
     */
    private synchronized void updateContractDetail() {
        contractDetail.getValue().ifPresentOrElse(values -> {

            for (CompteInfo compteInfo : values.compteInfo) {
                if (compteInfo.NUM_COMPTEUR.equals(numCompteur)) {
                    meterIdA = compteInfo.ELEMA;
                    meterIdB = compteInfo.ELEMB;
                    idPds = compteInfo.ID_PDS;
                }
            }

            if (meterIdA.isBlank()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        String.format("Can't find meter for meterId {}", numCompteur));
                return;
            }

            Map<String, String> props = this.editProperties();

            addProps(props, THING_WATER_METER_PROPERTY_CONTRACT_ORGANIZING_AUTHORITY,
                    values.contrat.AutoriteOrganisatrice);
            addProps(props, THING_WATER_METER_PROPERTY_CONTRACT_DATE_SORTIE_EPT, values.contrat.DateSortieEPT);
            addProps(props, THING_WATER_METER_PROPERTY_CONTRACT_EINVOICE, "" + values.contrat.eFacture);
            addProps(props, THING_WATER_METER_PROPERTY_CONTRACT_ICL_ACTIVE, "" + values.contrat.iclActive);
            addProps(props, THING_WATER_METER_PROPERTY_CONTRACT_DIRECT_DEBIT, "" + values.contrat.prelevAuto);
            addProps(props, THING_WATER_METER_PROPERTY_CONTRACT_NAME, values.contrat.Name);
            addProps(props, THING_WATER_METER_PROPERTY_CONTRACT_STREET, values.contrat.SITE_Rue);
            addProps(props, THING_WATER_METER_PROPERTY_CONTRACT_POST_CODE, values.contrat.SITE_CP);
            addProps(props, THING_WATER_METER_PROPERTY_CONTRACT_TOWN, values.contrat.SITE_Commune);
            addProps(props, THING_WATER_METER_PROPERTY_CONTRACT_STATE, values.contrat.Statut);
            addProps(props, THING_WATER_METER_PROPERTY_CONTRACT_BALANCE, "" + values.solde);

            CompteInfo comptInfo = values.compteInfo.get(0);
            addProps(props, THING_WATER_METER_PROPERTY_ELMA, comptInfo.ELEMA);
            addProps(props, THING_WATER_METER_PROPERTY_ELMB, comptInfo.ELEMB);
            addProps(props, THING_WATER_METER_PROPERTY_ID_PDS, comptInfo.ID_PDS);
            addProps(props, THING_WATER_METER_PROPERTY_NUM_METER, comptInfo.NUM_COMPTEUR);

            addProps(props, THING_WATER_METER_PROPERTY_CUSTOMER_BILLING_TOWN, values.contratClient.BillingCity);
            addProps(props, THING_WATER_METER_PROPERTY_CUSTOMER_BILLING_POST_CODE,
                    values.contratClient.BillingPostalCode);
            addProps(props, THING_WATER_METER_PROPERTY_CUSTOMER_BILLING_STREET, values.contratClient.BillingStreet);
            addProps(props, THING_WATER_METER_PROPERTY_CUSTOMER_FIRST_NAME, values.contratClient.FirstName);
            addProps(props, THING_WATER_METER_PROPERTY_CUSTOMER_LAST_NAME, values.contratClient.LastName);
            addProps(props, THING_WATER_METER_PROPERTY_CUSTOMER_NAME_SUP, values.contratClient.Name);
            addProps(props, THING_WATER_METER_PROPERTY_CUSTOMER_EMAIL, values.contratClient.Email);
            addProps(props, THING_WATER_METER_PROPERTY_CUSTOMER_GC, "" + values.contratClient.GC);
            addProps(props, THING_WATER_METER_PROPERTY_CUSTOMER_MOBILE_PHONE, values.contratClient.MobilePhone);
            addProps(props, THING_WATER_METER_PROPERTY_CUSTOMER_TITLE, values.contratClient.Salutation);
            addProps(props, THING_WATER_METER_PROPERTY_CUSTOMER_LOCK, "" + values.contratClient.VerrouillageFiche);

            addProps(props, THING_WATER_METER_PROPERTY_PAYER_BILLING_CITY, values.payeurClient.BillingCity);
            addProps(props, THING_WATER_METER_PROPERTY_PAYER_BILLING_POSTAL_CODE,
                    values.payeurClient.BillingPostalCode);
            addProps(props, THING_WATER_METER_PROPERTY_PAYER_BILLING_STREET, values.payeurClient.BillingStreet);
            addProps(props, THING_WATER_METER_PROPERTY_PAYER_FIRST_NAME, values.payeurClient.FirstName);
            addProps(props, THING_WATER_METER_PROPERTY_PAYER_LAST_NAME, values.payeurClient.LastName);
            addProps(props, THING_WATER_METER_PROPERTY_PAYER_NAME_SUP, values.payeurClient.Name);
            addProps(props, THING_WATER_METER_PROPERTY_PAYER_EMAIL, values.payeurClient.Email);
            addProps(props, THING_WATER_METER_PROPERTY_PAYER_GC, "" + values.payeurClient.GC);
            addProps(props, THING_WATER_METER_PROPERTY_PAYER_MOBILE_PHONE, values.payeurClient.MobilePhone);
            addProps(props, THING_WATER_METER_PROPERTY_PAYER_TITLE, values.payeurClient.Salutation);
            addProps(props, THING_WATER_METER_PROPERTY_PAYER_LOCK, "" + values.payeurClient.VerrouillageFiche);

            updateProperties(props);
        }, () -> {
            updateState(SEDIF_BASE_GROUP, CHANNEL_CONSUMPTION, new QuantityType<>(0.00, Units.LITRE));
        });
    }

    private void addProps(Map<String, String> props, String key, @Nullable String value) {
        if (value == null || value.isBlank()) {
            return;
        }

        props.put(key, value);
    }

    /**
     * Request new daily/weekly data and updates channels
     */
    private synchronized void updateConsumptionData() {
        consumption.getValue().ifPresentOrElse(values -> {

            // ===========================
            // Daily conso
            // ===========================
            double yesterdayConso = 0;
            double dayConsoMinus2 = 0;
            double dayConsoMinus3 = 0;

            if (values.data.consommation.length - 1 >= 0) {
                yesterdayConso = values.data.consommation[values.data.consommation.length - 1].consommation;
            }
            if (values.data.consommation.length - 2 >= 0) {
                dayConsoMinus2 = values.data.consommation[values.data.consommation.length - 2].consommation;
            }
            if (values.data.consommation.length - 3 >= 0) {
                dayConsoMinus3 = values.data.consommation[values.data.consommation.length - 3].consommation;
            }

            updateState(SEDIF_DAILY_CONSUMPTION_GROUP, CHANNEL_DAILY_YESTERDAY_CONSUMPTION,
                    new QuantityType<>(yesterdayConso, Units.LITRE));
            updateState(SEDIF_DAILY_CONSUMPTION_GROUP, CHANNEL_DAILY_DAY_MINUS_2_CONSUMPTION,
                    new QuantityType<>(dayConsoMinus2, Units.LITRE));
            updateState(SEDIF_DAILY_CONSUMPTION_GROUP, CHANNEL_DAILY_DAY_MINUS_3_CONSUMPTION,
                    new QuantityType<>(dayConsoMinus3, Units.LITRE));

            // ===========================
            // Week conso
            // ===========================
            double thisWeekConso = 0;
            double lastWeekConso = 0;
            double weekConsoMinus2 = 0;

            if (values.data.weekConso.length - 1 >= 0) {
                if (values.data.weekConso[values.data.weekConso.length - 1] != null) {
                    thisWeekConso = values.data.weekConso[values.data.weekConso.length - 1].consommation;
                }
            }
            if (values.data.weekConso.length - 2 >= 0) {
                if (values.data.weekConso[values.data.weekConso.length - 2] != null) {
                    lastWeekConso = values.data.weekConso[values.data.weekConso.length - 2].consommation;
                }
            }
            if (values.data.weekConso.length - 3 >= 0) {
                if (values.data.weekConso[values.data.weekConso.length - 3] != null) {
                    weekConsoMinus2 = values.data.weekConso[values.data.weekConso.length - 3].consommation;
                }
            }

            updateState(SEDIF_WEEKLY_CONSUMPTION_GROUP, CHANNEL_WEEKLY_THIS_WEEK_CONSUMPTION,
                    new QuantityType<>(thisWeekConso, Units.LITRE));
            updateState(SEDIF_WEEKLY_CONSUMPTION_GROUP, CHANNEL_WEEKLY_LAST_WEEK_CONSUMPTION,
                    new QuantityType<>(lastWeekConso, Units.LITRE));
            updateState(SEDIF_WEEKLY_CONSUMPTION_GROUP, CHANNEL_WEEKLY_WEEK_MINUS_2_CONSUMPTION,
                    new QuantityType<>(weekConsoMinus2, Units.LITRE));

            // ===========================
            // Month conso
            // ===========================
            double thisMonthConso = 0;
            double lastMonthConso = 0;
            double monthConsoMinus2 = 0;

            if (values.data.monthConso.length - 1 >= 0) {
                if (values.data.monthConso[values.data.monthConso.length - 1] != null) {
                    thisMonthConso = values.data.monthConso[values.data.monthConso.length - 1].consommation;
                }
            }
            if (values.data.monthConso.length - 2 >= 0) {
                if (values.data.monthConso[values.data.monthConso.length - 2] != null) {
                    lastMonthConso = values.data.monthConso[values.data.monthConso.length - 2].consommation;
                }
            }
            if (values.data.monthConso.length - 3 >= 0) {
                if (values.data.monthConso[values.data.monthConso.length - 3] != null) {
                    monthConsoMinus2 = values.data.monthConso[values.data.monthConso.length - 3].consommation;
                }
            }

            updateState(SEDIF_MONTHLY_CONSUMPTION_GROUP, CHANNEL_MONTHLY_THIS_MONTH_CONSUMPTION,
                    new QuantityType<>(thisMonthConso, Units.LITRE));
            updateState(SEDIF_MONTHLY_CONSUMPTION_GROUP, CHANNEL_MONTHLY_LAST_MONTH_CONSUMPTION,
                    new QuantityType<>(lastMonthConso, Units.LITRE));
            updateState(SEDIF_MONTHLY_CONSUMPTION_GROUP, CHANNEL_MONTHLY_MONTH_MINUS_2_CONSUMPTION,
                    new QuantityType<>(monthConsoMinus2, Units.LITRE));

            // ===========================
            // Year conso
            // ===========================
            double thisYearConso = 0;
            double lastYearConso = 0;
            double yearConsoMinus2 = 0;

            if (values.data.yearConso.length - 1 >= 0) {
                if (values.data.yearConso[values.data.yearConso.length - 1] != null) {
                    thisYearConso = values.data.yearConso[values.data.yearConso.length - 1].consommation;
                }
            }
            if (values.data.yearConso.length - 2 >= 0) {
                if (values.data.yearConso[values.data.yearConso.length - 2] != null) {
                    lastYearConso = values.data.yearConso[values.data.yearConso.length - 2].consommation;
                }
            }
            if (values.data.yearConso.length - 3 >= 0) {
                if (values.data.yearConso[values.data.yearConso.length - 3] != null) {
                    yearConsoMinus2 = values.data.yearConso[values.data.yearConso.length - 3].consommation;
                }
            }

            updateState(SEDIF_YEARLY_CONSUMPTION_GROUP, CHANNEL_YEARLY_THIS_YEAR_CONSUMPTION,
                    new QuantityType<>(thisYearConso, Units.LITRE));
            updateState(SEDIF_YEARLY_CONSUMPTION_GROUP, CHANNEL_YEARLY_LAST_YEAR_CONSUMPTION,
                    new QuantityType<>(lastYearConso, Units.LITRE));
            updateState(SEDIF_YEARLY_CONSUMPTION_GROUP, CHANNEL_YEARLY_YEAR_MINUS_2_CONSUMPTION,
                    new QuantityType<>(yearConsoMinus2, Units.LITRE));

            updateState(SEDIF_BASE_GROUP, CHANNEL_PRIX_MOYEN_EAU, new DecimalType(values.prixMoyenEau));

            updateConsumptionTimeSeries(SEDIF_DAILY_CONSUMPTION_GROUP, CHANNEL_CONSUMPTION, values.data.consommation);
            updateConsumptionTimeSeries(SEDIF_WEEKLY_CONSUMPTION_GROUP, CHANNEL_CONSUMPTION, values.data.weekConso);
            updateConsumptionTimeSeries(SEDIF_MONTHLY_CONSUMPTION_GROUP, CHANNEL_CONSUMPTION, values.data.monthConso);
            updateConsumptionTimeSeries(SEDIF_YEARLY_CONSUMPTION_GROUP, CHANNEL_CONSUMPTION, values.data.yearConso);

            logger.debug("end");

        }, () -> {
            updateState(SEDIF_BASE_GROUP, CHANNEL_CONSUMPTION, new QuantityType<>(0.00, Units.LITRE));
        });
    }

    private @Nullable ContractDetail getContractDetail() {
        SedifHttpApi api = this.sedifApi;
        if (api != null) {
            try {
                ContractDetail contractDetail = api.getContractDetails(contractId);
                return contractDetail;
            } catch (Exception e) {
                logger.debug("Exception when getting consumption data for : {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
        }

        return null;
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
            Bridge bridge = getBridge();
            if (bridge == null) {
                return;
            }

            BridgeSedifWebHandler bridgeHandler = (BridgeSedifWebHandler) bridge.getHandler();
            if (bridgeHandler == null) {
                return;
            }

            int idx = 0;
            while (bridge.getStatus() != ThingStatus.ONLINE && idx < 5) {
                idx++;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {

                }
            }

            if (bridge.getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                        "Bridge take too much time to initialize");
                return;
            }

            updateStatus(ThingStatus.ONLINE);

            Contract contract = bridgeHandler.getContract(contractName);
            if (contract != null) {
                contractId = Objects.requireNonNull(contract.Id);
            }
            sedifApi = bridgeHandler.getSedifApi();

            SedifHttpApi api = this.sedifApi;

            if (api != null) {
                ScheduledFuture<?> lcPollingJob = pollingJob;

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

    private synchronized void updateConsumptionTimeSeries(String groupId, String channelId, Consommation[] consoTab) {
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

    public String getMeterIdA() {
        return meterIdA;
    }

    public String getMeterIdB() {
        return meterIdB;
    }

    public String getIdPds() {
        return idPds;
    }

    public String getNumCompteur() {
        return idPds;
    }
}
