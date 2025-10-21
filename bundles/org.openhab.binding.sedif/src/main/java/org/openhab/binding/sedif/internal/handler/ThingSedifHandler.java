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
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
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
    private String numCompteur;

    private @Nullable CompteInfo currentMeterInfo;

    private @Nullable SedifState sedifState;

    private final ExpiringDayCache<ContractDetail> contractDetail;
    private final ExpiringDayCache<MeterReading> consumption;

    protected final Gson gson;

    protected SedifConfiguration config;

    public ThingSedifHandler(Thing thing, LocaleProvider localeProvider, TimeZoneProvider timeZoneProvider, Gson gson) {
        super(thing);

        contractName = "";
        contractId = "";
        numCompteur = "";

        this.gson = gson;

        this.contractDetail = new ExpiringDayCache<ContractDetail>("contractDetail", REFRESH_HOUR_OF_DAY,
                REFRESH_MINUTE_OF_DAY, () -> {
                    try {
                        ContractDetail contractDetail = getContractDetails();
                        return contractDetail;
                    } catch (CommunicationFailedException ex) {
                        // We just return null, EpiringDayCache logic will retry the operation later.
                        logger.error("Failed to get contract details", ex);
                        return null;
                    } catch (InvalidSessionException ex) {
                        // In case of session error, we force the bridge to reconnect after a delay
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                        if (getBridge().getHandler() instanceof BridgeSedifWebHandler bridgeSedif) {
                            bridgeSedif.scheduleReconnect();
                        }
                        return null;
                    } catch (SedifException ex) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                                ex.getMessage());
                        return null;
                    }
                });

        this.consumption = new ExpiringDayCache<MeterReading>("dailyConsumption", REFRESH_HOUR_OF_DAY,
                REFRESH_MINUTE_OF_DAY, () -> {
                    LocalDate today = LocalDate.now();

                    try {
                        MeterReading meterReading = updateConsumptionData(today.minusDays(89), today, false);
                        if (meterReading != null) {
                            MeterReadingHelper.calcAgregat(meterReading);
                        }
                        return meterReading;
                    } catch (CommunicationFailedException ex) {
                        // We just return null, EpxiringDayCache logic will retry the operation later.
                        logger.error("Failed to get consumption data", ex);
                        return null;
                    } catch (InvalidSessionException ex) {
                        // In case of session error, we force the bridge to reconnect after a delay
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                        if (getBridge().getHandler() instanceof BridgeSedifWebHandler bridgeSedif) {
                            bridgeSedif.scheduleReconnect();
                        }
                        return null;
                    } catch (SedifException ex) {
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                                ex.getMessage());
                        return null;
                    }
                });

        config = getConfigAs(SedifConfiguration.class);
    }

    @Override
    public synchronized void initialize() {
        loadSedifState();
        if (sedifState.getLastIndexDate() == null) {
            sedifState.setLastIndexDate(LocalDate.of(1980, 1, 1));
        }

        // force reread data if we pause / start the thing
        this.contractDetail.invalidate();
        this.consumption.invalidate();

        if (config.seemsValid()) {
            contractName = config.contractId;
            numCompteur = config.meterId;
            updateStatus(ThingStatus.UNKNOWN);
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
        } catch (InterruptedIOException ioe) {
            logger.warn("Couldn't read Sedif MetaData information from file '{}'.", file.getAbsolutePath());
        } catch (IOException ioe) {
            logger.warn("Couldn't read Sedif MetaData information from file '{}'.", file.getAbsolutePath());
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
            logger.warn("Couldn't write Sedif MetaData information to file '{}'.", file.getAbsolutePath());
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
        sedifApi = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Request new data and updates channels
     */
    private void updateData() {
        updateContractDetail();
        updateHistoricalConsumptionData();
        updateConsumptionData();
        saveSedifState();
    }

    private void updateHistoricalConsumptionData() {
        logger.trace("updateHistoricalConsumptionData() called");

        int periodLength = 90;
        LocalDate currentDate = LocalDate.now();
        currentDate = currentDate.minusDays(periodLength);

        LocalDate lastUpdateDate = sedifState.getLastIndexDate();
        LocalDate newLastUpdateDate = lastUpdateDate;
        boolean hasData = true;
        boolean hasAlreadyRetrieveData = false;

        while ((hasData || !hasAlreadyRetrieveData) && currentDate.isAfter(lastUpdateDate)) {
            LocalDate startDate = currentDate.minusDays(periodLength - 1);
            try {
                logger.debug("Retrieve data from {} to {}:", startDate, currentDate);
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
            } catch (SedifException ex) {
                logger.warn("Unable to retrieve data from {} to {}:", startDate, currentDate, ex);
            }
            currentDate = startDate;
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

        if (meterReading != null) {
            return sedifState.updateMeterReading(meterReading);
        }
        return null;
    }

    /**
     * Request new daily/weekly data and updates channels
     */
    private void updateContractDetail() {
        logger.trace("updateContractDetail() called");
        contractDetail.getValue().ifPresentOrElse(values -> {

            for (CompteInfo compteInfo : values.compteInfo) {
                if (compteInfo.numCompteur.equals(numCompteur)) {
                    currentMeterInfo = compteInfo;
                }
            }

            if (currentMeterInfo == null || currentMeterInfo.eLma.isBlank()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        String.format("Can't find meter for meterId {}", numCompteur));
                return;
            }

            Map<String, String> props = this.editProperties();

            addProps(props, PROPERTY_CONTRACT_ORGANIZING_AUTHORITY, values.contrat.autoriteOrganisatrice);
            addProps(props, PROPERTY_CONTRACT_DATE_SORTIE_EPT, values.contrat.dateSortieEPT);
            addProps(props, PROPERTY_CONTRACT_EINVOICE, "" + values.contrat.eFacture);
            addProps(props, PROPERTY_CONTRACT_ICL_ACTIVE, "" + values.contrat.iclActive);
            addProps(props, PROPERTY_CONTRACT_DIRECT_DEBIT, "" + values.contrat.prelevAuto);
            addProps(props, PROPERTY_CONTRACT_NAME, values.contrat.name);
            addProps(props, PROPERTY_CONTRACT_STREET, values.contrat.siteRue);
            addProps(props, PROPERTY_CONTRACT_POST_CODE, values.contrat.siteCp);
            addProps(props, PROPERTY_CONTRACT_TOWN, values.contrat.siteCommune);
            addProps(props, PROPERTY_CONTRACT_STATE, values.contrat.statut);
            addProps(props, PROPERTY_CONTRACT_BALANCE, "" + values.solde);

            CompteInfo comptInfo = values.compteInfo.get(0);
            addProps(props, PROPERTY_ELMA, comptInfo.eLma);
            addProps(props, PROPERTY_ELMB, comptInfo.eLmb);
            addProps(props, PROPERTY_ID_PDS, comptInfo.idPds);
            addProps(props, PROPERTY_NUM_METER, comptInfo.numCompteur);

            addProps(props, PROPERTY_CUSTOMER_BILLING_TOWN, values.contratClient.billingCity);
            addProps(props, PROPERTY_CUSTOMER_BILLING_POST_CODE, values.contratClient.billingPostalCode);
            addProps(props, PROPERTY_CUSTOMER_BILLING_STREET, values.contratClient.billingStreet);
            addProps(props, PROPERTY_CUSTOMER_FIRST_NAME, values.contratClient.firstName);
            addProps(props, PROPERTY_CUSTOMER_LAST_NAME, values.contratClient.lastName);
            addProps(props, PROPERTY_CUSTOMER_NAME_SUP, values.contratClient.name);
            addProps(props, PROPERTY_CUSTOMER_EMAIL, values.contratClient.email);
            addProps(props, PROPERTY_CUSTOMER_GC, "" + values.contratClient.gC);
            addProps(props, PROPERTY_CUSTOMER_MOBILE_PHONE, values.contratClient.mobilePhone);
            addProps(props, PROPERTY_CUSTOMER_TITLE, values.contratClient.salutation);
            addProps(props, PROPERTY_CUSTOMER_LOCK, "" + values.contratClient.verrouillageFiche);

            addProps(props, PROPERTY_PAYER_BILLING_CITY, values.payeurClient.billingCity);
            addProps(props, PROPERTY_PAYER_BILLING_POSTAL_CODE, values.payeurClient.billingPostalCode);
            addProps(props, PROPERTY_PAYER_BILLING_STREET, values.payeurClient.billingStreet);
            addProps(props, PROPERTY_PAYER_FIRST_NAME, values.payeurClient.firstName);
            addProps(props, PROPERTY_PAYER_LAST_NAME, values.payeurClient.lastName);
            addProps(props, PROPERTY_PAYER_NAME_SUP, values.payeurClient.name);
            addProps(props, PROPERTY_PAYER_EMAIL, values.payeurClient.email);
            addProps(props, PROPERTY_PAYER_GC, "" + values.payeurClient.gC);
            addProps(props, PROPERTY_PAYER_MOBILE_PHONE, values.payeurClient.mobilePhone);
            addProps(props, PROPERTY_PAYER_TITLE, values.payeurClient.salutation);
            addProps(props, PROPERTY_PAYER_LOCK, "" + values.payeurClient.verrouillageFiche);

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

    /**
     * Request new daily/weekly data and updates channels
     */
    private void updateConsumptionData() {
        logger.trace("updateEnergyData() called");

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

            updateState(GROUP_YEARLY_CONSUMPTION, CHANNEL_YEARLY_THIS_YEAR_CONSUMPTION,
                    new QuantityType<>(thisYearConso, Units.LITRE));
            updateState(GROUP_YEARLY_CONSUMPTION, CHANNEL_YEARLY_LAST_YEAR_CONSUMPTION,
                    new QuantityType<>(lastYearConso, Units.LITRE));
            updateState(GROUP_YEARLY_CONSUMPTION, CHANNEL_YEARLY_YEAR_MINUS_2_CONSUMPTION,
                    new QuantityType<>(yearConsoMinus2, Units.LITRE));

            updateState(GROUP_BASE, CHANNEL_MEAN_WATER_PRICE, new DecimalType(values.prixMoyenEau));

            sedifState.setLastIndexDate(
                    values.data.consommation[values.data.consommation.length - 1].dateIndex.toLocalDate());
            updateConsumptionTimeSeries(GROUP_DAILY_CONSUMPTION, CHANNEL_CONSUMPTION, values.data.consommation);
            updateConsumptionTimeSeries(GROUP_WEEKLY_CONSUMPTION, CHANNEL_CONSUMPTION, values.data.weekConso);
            updateConsumptionTimeSeries(GROUP_MONTHLY_CONSUMPTION, CHANNEL_CONSUMPTION, values.data.monthConso);
            updateConsumptionTimeSeries(GROUP_YEARLY_CONSUMPTION, CHANNEL_CONSUMPTION, values.data.yearConso);

            logger.debug("end");

        }, () -> {
            updateState(GROUP_BASE, CHANNEL_CONSUMPTION, new QuantityType<>(0.00, Units.LITRE));
        });
    }

    private @Nullable ContractDetail getContractDetails() throws SedifException {
        SedifHttpApi api = this.sedifApi;
        if (api != null) {
            ContractDetail contractDetail = api.getContractDetails(contractId);
            return contractDetail;
        }

        return null;
    }

    private @Nullable MeterReading getConsumptionData(LocalDate from, LocalDate to) throws SedifException {
        logger.debug("getConsumptionData for from {} to {}", from.format(DateTimeFormatter.ISO_LOCAL_DATE),
                to.format(DateTimeFormatter.ISO_LOCAL_DATE));

        SedifHttpApi api = this.sedifApi;
        if (api != null) {
            MeterReading meterReading = api.getConsumptionData(contractId, currentMeterInfo, from, to);
            return meterReading;
        }

        return null;
    }

    @Override
    protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
        super.updateStatus(status, statusDetail, description);
        if (getBridge().getStatus() == ThingStatus.ONLINE && status == ThingStatus.UNKNOWN) {
            setupRefreshJob();
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.UNKNOWN);
            }
            setupRefreshJob();
        }
    }

    private void setupRefreshJob() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }

        if (bridge.getHandler() instanceof BridgeSedifWebHandler bridgeHandler) {
            Contract contract = bridgeHandler.getContract(contractName);
            if (contract != null) {
                contractId = Objects.requireNonNull(contract.id);
            }

            sedifApi = bridgeHandler.getSedifApi();
            if (sedifApi != null) {
                updateData();

                final LocalDateTime now = LocalDateTime.now();
                final LocalDateTime nextDayFirstTimeUpdate = now.plusDays(1).withHour(REFRESH_HOUR_OF_DAY)
                        .withMinute(REFRESH_MINUTE_OF_DAY).truncatedTo(ChronoUnit.MINUTES);

                cancelRefreshJob();
                long initialDelay = ChronoUnit.MINUTES.between(now, nextDayFirstTimeUpdate) % REFRESH_INTERVAL_IN_MIN
                        + 1;
                long delay = REFRESH_INTERVAL_IN_MIN;

                if (!consumption.isPresent() || !contractDetail.isPresent()) {
                    initialDelay = 20;
                }
                refreshJob = scheduler.scheduleWithFixedDelay(this::updateData, initialDelay, delay, TimeUnit.MINUTES);

                if (this.getThing().getStatusInfo().getStatusDetail() != ThingStatusDetail.COMMUNICATION_ERROR) {
                    updateStatus(ThingStatus.ONLINE);
                }
            }
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
