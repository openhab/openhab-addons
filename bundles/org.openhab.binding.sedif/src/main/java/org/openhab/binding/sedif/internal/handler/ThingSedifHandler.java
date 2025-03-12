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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.sedif.internal.api.ExpiringDayCache;
import org.openhab.binding.sedif.internal.api.SedifHttpApi;
import org.openhab.binding.sedif.internal.config.SedifConfiguration;
import org.openhab.binding.sedif.internal.dto.ContractDetail;
import org.openhab.binding.sedif.internal.dto.ContractDetail.CompteInfo;
import org.openhab.binding.sedif.internal.dto.MeterReading;
import org.openhab.binding.sedif.internal.dto.MeterReading.Data.Consommation;
import org.openhab.binding.sedif.internal.types.SedifException;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
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
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.TimeSeries.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ThingSedifHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */

@NonNullByDefault
@SuppressWarnings("null")
public class ThingSedifHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(ThingSedifHandler.class);
    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable SedifHttpApi sedifApi;

    private static final Random RANDOM_NUMBERS = new Random();
    private static final int REFRESH_HOUR_OF_DAY = 1;
    private static final int REFRESH_MINUTE_OF_DAY = RANDOM_NUMBERS.nextInt(60);
    private static final int REFRESH_INTERVAL_IN_MIN = 120;

    private final ExpiringDayCache<ContractDetail> contractDetail;
    private final ExpiringDayCache<MeterReading> dailyConsumption;

    private @Nullable ScheduledFuture<?> pollingJob = null;
    protected SedifConfiguration config;

    public ThingSedifHandler(Thing thing, LocaleProvider localeProvider, TimeZoneProvider timeZoneProvider) {
        super(thing);

        this.contractDetail = new ExpiringDayCache<ContractDetail>("contractDetail", REFRESH_HOUR_OF_DAY,
                REFRESH_MINUTE_OF_DAY, () -> {
                    LocalDate today = LocalDate.now();
                    ContractDetail contractDetail = getContractDetail();
                    return contractDetail;
                });

        this.dailyConsumption = new ExpiringDayCache<MeterReading>("dailyConsumption", REFRESH_HOUR_OF_DAY,
                REFRESH_MINUTE_OF_DAY, () -> {
                    LocalDate today = LocalDate.now();
                    MeterReading meterReading = getConsumptionData(today.minusDays(1095), today);
                    return meterReading;
                });

        config = getConfigAs(SedifConfiguration.class);
    }

    @Override
    public synchronized void initialize() {
        updateStatus(ThingStatus.ONLINE);

        Bridge bridge = getBridge();
        if (bridge == null) {
            return;
        }

        BridgeSedifWebHandler bridgeHandler = (BridgeSedifWebHandler) bridge.getHandler();
        if (bridgeHandler == null) {
            return;
        }
        sedifApi = bridgeHandler.getSedifApi();

        if (!config.seemsValid()) {
            pollingJob = scheduler.schedule(this::pollingCode, 5, TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.config-error-mandatory-settings");
        }

    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

    /**
     * Request new data and updates channels
     */
    private synchronized void updateData() {

        logger.info("updateContractDetail() called");
        updateContractDetail();

        logger.info("updateEnergyData() called");
        updateConsumptionData();
    }

    /**
     * Request new daily/weekly data and updates channels
     */
    private synchronized void updateContractDetail() {
        contractDetail.getValue().ifPresentOrElse(values -> {

            updateState(SEDIF_CONTRAT_GROUP, CHANNEL_AUTORITE_ORGANISATRICE,
                    new StringType(values.contrat.AutoriteOrganisatrice));
            updateState(SEDIF_CONTRAT_GROUP, CHANNEL_DATE_SORTIE_EPT, new StringType(values.contrat.DateSortieEPT));
            // updateState(SEDIF_CONTRAT_GROUP, CHANNEL_EFACTURE, OnOffType.valueOf("" + values.contrat.eFacture));
            // updateState(SEDIF_CONTRAT_GROUP, CHANNEL_ICL_ACTIVE, OnOffType.valueOf("" + values.contrat.iclActive));
            // updateState(SEDIF_CONTRAT_GROUP, CHANNEL_PRELEVAUTO, OnOffType.valueOf("" + values.contrat.prelevAuto));
            updateState(SEDIF_CONTRAT_GROUP, CHANNEL_NAME, new StringType(values.contrat.Name));
            updateState(SEDIF_CONTRAT_GROUP, CHANNEL_STREET, new StringType(values.contrat.SITE_Rue));
            updateState(SEDIF_CONTRAT_GROUP, CHANNEL_CP, new StringType(values.contrat.SITE_CP));
            updateState(SEDIF_CONTRAT_GROUP, CHANNEL_TOWN, new StringType(values.contrat.SITE_Commune));
            updateState(SEDIF_CONTRAT_GROUP, CHANNEL_STATE, new StringType(values.contrat.Statut));
            updateState(SEDIF_CONTRAT_GROUP, CHANNEL_SOLDE, new DecimalType(values.solde));

            CompteInfo comptInfo = values.compteInfo.get(0);
            updateState(SEDIF_CONTRAT_METER_GROUP, CHANNEL_ELMA, new StringType(comptInfo.ELEMA));
            updateState(SEDIF_CONTRAT_METER_GROUP, CHANNEL_ELMB, new StringType(comptInfo.ELEMB));
            updateState(SEDIF_CONTRAT_METER_GROUP, CHANNEL_ID_PDS, new StringType(comptInfo.ID_PDS));
            updateState(SEDIF_CONTRAT_METER_GROUP, CHANNEL_NUM_METER, new StringType(comptInfo.NUM_COMPTEUR));

            updateState(SEDIF_CONTRAT_CLIENT_GROUP, CHANNEL_CONTRAT_BILLING_CITY,
                    new StringType(values.contratClient.BillingCity));
            updateState(SEDIF_CONTRAT_CLIENT_GROUP, CHANNEL_CONTRAT_BILLING_POSTAL_CODE,
                    new StringType(values.contratClient.BillingPostalCode));
            updateState(SEDIF_CONTRAT_CLIENT_GROUP, CHANNEL_CONTRAT_BILLING_STREET,
                    new StringType(values.contratClient.BillingStreet));
            updateState(SEDIF_CONTRAT_CLIENT_GROUP, CHANNEL_CONTRAT_FIRST_NAME,
                    new StringType(values.contratClient.FirstName));
            updateState(SEDIF_CONTRAT_CLIENT_GROUP, CHANNEL_CONTRAT_LAST_NAME,
                    new StringType(values.contratClient.LastName));
            updateState(SEDIF_CONTRAT_CLIENT_GROUP, CHANNEL_CONTRAT_NAME_SUP,
                    new StringType(values.contratClient.Name));
            updateState(SEDIF_CONTRAT_CLIENT_GROUP, CHANNEL_CONTRAT_EMAIL, new StringType(values.contratClient.Email));
            // updateState(SEDIF_CONTRAT_CLIENT_GROUP, CHANNEL_CONTRAT_GC,
            // OnOffType.valueOf("" + values.contratClient.GC));
            updateState(SEDIF_CONTRAT_CLIENT_GROUP, CHANNEL_CONTRAT_MOBILE_PHONE,
                    new StringType(values.contratClient.MobilePhone));
            updateState(SEDIF_CONTRAT_CLIENT_GROUP, CHANNEL_CONTRAT_SALUTATION,
                    new StringType(values.contratClient.Salutation));
            // updateState(SEDIF_CONTRAT_CLIENT_GROUP, CHANNEL_CONTRAT__VEROUILLAGE_FICHE,
            // OnOffType.valueOf("" + values.contratClient.VerrouillageFiche));

            updateState(SEDIF_CONTRAT_PAYER_GROUP, CHANNEL_PAYER_BILLING_CITY,
                    new StringType(values.payeurClient.BillingCity));
            updateState(SEDIF_CONTRAT_PAYER_GROUP, CHANNEL_PAYER_BILLING_POSTAL_CODE,
                    new StringType(values.payeurClient.BillingPostalCode));
            updateState(SEDIF_CONTRAT_PAYER_GROUP, CHANNEL_PAYER_BILLING_STREET,
                    new StringType(values.payeurClient.BillingStreet));
            updateState(SEDIF_CONTRAT_PAYER_GROUP, CHANNEL_PAYER_FIRST_NAME,
                    new StringType(values.payeurClient.FirstName));
            updateState(SEDIF_CONTRAT_PAYER_GROUP, CHANNEL_PAYER_LAST_NAME,
                    new StringType(values.payeurClient.LastName));
            updateState(SEDIF_CONTRAT_PAYER_GROUP, CHANNEL_PAYER_NAME_SUP, new StringType(values.payeurClient.Name));
            updateState(SEDIF_CONTRAT_PAYER_GROUP, CHANNEL_PAYER_EMAIL, new StringType(values.payeurClient.Email));
            // updateState(SEDIF_CONTRAT_PAYER_GROUP, CHANNEL_PAYER_GC, OnOffType.valueOf("" +
            // values.payeurClient.GC));
            updateState(SEDIF_CONTRAT_PAYER_GROUP, CHANNEL_PAYER_MOBILE_PHONE,
                    new StringType(values.payeurClient.MobilePhone));
            updateState(SEDIF_CONTRAT_PAYER_GROUP, CHANNEL_PAYER_SALUTATION,
                    new StringType(values.payeurClient.Salutation));
            // updateState(SEDIF_CONTRAT_PAYER_GROUP, CHANNEL_PAYER_VEROUILLAGE_FICHE,
            // OnOffType.valueOf("" + values.payeurClient.VerrouillageFiche));

        }, () -> {
            updateState(SEDIF_BASE_GROUP, CHANNEL_CONSUMPTION, new QuantityType<>(0.00, Units.LITRE));
        });
    }

    /**
     * Request new daily/weekly data and updates channels
     */
    private synchronized void updateConsumptionData() {
        dailyConsumption.getValue().ifPresentOrElse(values -> {
            updateState(SEDIF_BASE_GROUP, CHANNEL_CONSUMPTION, new QuantityType<>(
                    values.data.consommation.get(values.data.consommation.size() - 1).consommation, Units.LITRE));

            updateConsumptionTimeSeries(SEDIF_BASE_GROUP, CHANNEL_CONSUMPTION, values);
        }, () -> {
            updateState(SEDIF_BASE_GROUP, CHANNEL_CONSUMPTION, new QuantityType<>(0.00, Units.LITRE));
        });
    }

    private @Nullable ContractDetail getContractDetail() {
        SedifHttpApi api = this.sedifApi;
        if (api != null) {
            try {
                ContractDetail contractDetail = api.getContractDetails();
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
            SedifHttpApi api = this.sedifApi;

            if (api != null) {
                Bridge lcBridge = getBridge();
                ScheduledFuture<?> lcPollingJob = pollingJob;

                if (lcBridge == null || lcBridge.getStatus() != ThingStatus.ONLINE) {
                    return;
                }

                BridgeSedifWebHandler bridgeHandler = (BridgeSedifWebHandler) lcBridge.getHandler();
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

    private synchronized void updateConsumptionTimeSeries(String groupId, String channelId, MeterReading meterReading) {
        TimeSeries timeSeries = new TimeSeries(Policy.REPLACE);

        for (int i = 0; i < meterReading.data.consommation.size(); i++) {

            Consommation conso = meterReading.data.consommation.get(i);
            String date = conso.dateIndex;
            float consommation = conso.consommation;

            LocalDateTime dt = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            Instant timestamp = dt.toInstant(ZoneOffset.UTC);

            if (Double.isNaN(consommation)) {
                continue;
            }
            timeSeries.add(timestamp, new DecimalType(consommation));
        }

        sendTimeSeries(groupId, channelId, timeSeries);
    }

}
