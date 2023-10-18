/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.energidataservice.internal.handler;

import static org.openhab.binding.energidataservice.internal.EnergiDataServiceBindingConstants.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.energidataservice.internal.ApiController;
import org.openhab.binding.energidataservice.internal.CacheManager;
import org.openhab.binding.energidataservice.internal.DatahubTariff;
import org.openhab.binding.energidataservice.internal.action.EnergiDataServiceActions;
import org.openhab.binding.energidataservice.internal.api.ChargeType;
import org.openhab.binding.energidataservice.internal.api.ChargeTypeCode;
import org.openhab.binding.energidataservice.internal.api.DatahubTariffFilter;
import org.openhab.binding.energidataservice.internal.api.DatahubTariffFilterFactory;
import org.openhab.binding.energidataservice.internal.api.DateQueryParameter;
import org.openhab.binding.energidataservice.internal.api.DateQueryParameterType;
import org.openhab.binding.energidataservice.internal.api.GlobalLocationNumber;
import org.openhab.binding.energidataservice.internal.api.dto.DatahubPricelistRecord;
import org.openhab.binding.energidataservice.internal.api.dto.ElspotpriceRecord;
import org.openhab.binding.energidataservice.internal.config.DatahubPriceConfiguration;
import org.openhab.binding.energidataservice.internal.config.EnergiDataServiceConfiguration;
import org.openhab.binding.energidataservice.internal.exception.DataServiceException;
import org.openhab.binding.energidataservice.internal.retry.RetryPolicyFactory;
import org.openhab.binding.energidataservice.internal.retry.RetryStrategy;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link EnergiDataServiceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class EnergiDataServiceHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EnergiDataServiceHandler.class);
    private final TimeZoneProvider timeZoneProvider;
    private final ApiController apiController;
    private final CacheManager cacheManager;
    private final Gson gson = new Gson();

    private EnergiDataServiceConfiguration config;
    private RetryStrategy retryPolicy = RetryPolicyFactory.initial();
    private @Nullable ScheduledFuture<?> refreshFuture;
    private @Nullable ScheduledFuture<?> priceUpdateFuture;

    private record Price(String hourStart, BigDecimal spotPrice, String spotPriceCurrency,
            @Nullable BigDecimal netTariff, @Nullable BigDecimal systemTariff, @Nullable BigDecimal electricityTax,
            @Nullable BigDecimal reducedElectricityTax, @Nullable BigDecimal transmissionNetTariff) {
    }

    public EnergiDataServiceHandler(Thing thing, HttpClient httpClient, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
        this.apiController = new ApiController(httpClient, timeZoneProvider);
        this.cacheManager = new CacheManager();

        // Default configuration
        this.config = new EnergiDataServiceConfiguration();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            return;
        }

        if (ELECTRICITY_CHANNELS.contains(channelUID.getId())) {
            refreshElectricityPrices();
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(EnergiDataServiceConfiguration.class);

        if (config.priceArea.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.no-price-area");
            return;
        }
        GlobalLocationNumber gln = config.getGridCompanyGLN();
        if (!gln.isEmpty() && !gln.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.invalid-grid-company-gln");
            return;
        }
        gln = config.getEnerginetGLN();
        if (!gln.isEmpty() && !gln.isValid()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR,
                    "@text/offline.conf-error.invalid-energinet-gln");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        refreshFuture = scheduler.schedule(this::refreshElectricityPrices, 0, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> refreshFuture = this.refreshFuture;
        if (refreshFuture != null) {
            refreshFuture.cancel(true);
            this.refreshFuture = null;
        }
        ScheduledFuture<?> priceUpdateFuture = this.priceUpdateFuture;
        if (priceUpdateFuture != null) {
            priceUpdateFuture.cancel(true);
            this.priceUpdateFuture = null;
        }

        cacheManager.clear();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(EnergiDataServiceActions.class);
    }

    private void refreshElectricityPrices() {
        RetryStrategy retryPolicy;
        try {
            if (isLinked(CHANNEL_SPOT_PRICE) || isLinked(CHANNEL_HOURLY_PRICES)) {
                downloadSpotPrices();
            }

            for (DatahubTariff datahubTariff : DatahubTariff.values()) {
                if (isLinked(datahubTariff.getChannelId()) || isLinked(CHANNEL_HOURLY_PRICES)) {
                    downloadTariffs(datahubTariff);
                }
            }

            updateStatus(ThingStatus.ONLINE);
            updatePrices();

            if (isLinked(CHANNEL_SPOT_PRICE) || isLinked(CHANNEL_HOURLY_PRICES)) {
                if (cacheManager.getNumberOfFutureSpotPrices() < 13) {
                    retryPolicy = RetryPolicyFactory.whenExpectedSpotPriceDataMissing(DAILY_REFRESH_TIME_CET,
                            NORD_POOL_TIMEZONE);
                } else {
                    retryPolicy = RetryPolicyFactory.atFixedTime(DAILY_REFRESH_TIME_CET, NORD_POOL_TIMEZONE);
                }
            } else {
                retryPolicy = RetryPolicyFactory.atFixedTime(LocalTime.MIDNIGHT, timeZoneProvider.getTimeZone());
            }
        } catch (DataServiceException e) {
            if (e.getHttpStatus() != 0) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        HttpStatus.getCode(e.getHttpStatus()).getMessage());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
            if (e.getCause() != null) {
                logger.debug("Error retrieving prices", e);
            }
            retryPolicy = RetryPolicyFactory.fromThrowable(e);
        } catch (InterruptedException e) {
            logger.debug("Refresh job interrupted");
            Thread.currentThread().interrupt();
            return;
        }

        rescheduleRefreshJob(retryPolicy);
    }

    private void downloadSpotPrices() throws InterruptedException, DataServiceException {
        if (cacheManager.areSpotPricesFullyCached()) {
            logger.debug("Cached spot prices still valid, skipping download.");
            return;
        }
        DateQueryParameter start;
        if (cacheManager.areHistoricSpotPricesCached()) {
            start = DateQueryParameter.of(DateQueryParameterType.UTC_NOW);
        } else {
            start = DateQueryParameter.of(DateQueryParameterType.UTC_NOW,
                    Duration.ofHours(-CacheManager.NUMBER_OF_HISTORIC_HOURS));
        }
        Map<String, String> properties = editProperties();
        ElspotpriceRecord[] spotPriceRecords = apiController.getSpotPrices(config.priceArea, config.getCurrency(),
                start, properties);
        cacheManager.putSpotPrices(spotPriceRecords, config.getCurrency());
        updateProperties(properties);
    }

    private void downloadTariffs(DatahubTariff datahubTariff) throws InterruptedException, DataServiceException {
        GlobalLocationNumber globalLocationNumber = switch (datahubTariff) {
            case NET_TARIFF -> config.getGridCompanyGLN();
            default -> config.getEnerginetGLN();
        };
        if (globalLocationNumber.isEmpty()) {
            return;
        }
        if (cacheManager.areTariffsValidTomorrow(datahubTariff)) {
            logger.debug("Cached tariffs of type {} still valid, skipping download.", datahubTariff);
            cacheManager.updateTariffs(datahubTariff);
        } else {
            DatahubTariffFilter filter = switch (datahubTariff) {
                case NET_TARIFF -> getNetTariffFilter();
                case SYSTEM_TARIFF -> DatahubTariffFilterFactory.getSystemTariff();
                case ELECTRICITY_TAX -> DatahubTariffFilterFactory.getElectricityTax();
                case REDUCED_ELECTRICITY_TAX -> DatahubTariffFilterFactory.getReducedElectricityTax();
                case TRANSMISSION_NET_TARIFF -> DatahubTariffFilterFactory.getTransmissionNetTariff();
            };
            cacheManager.putTariffs(datahubTariff, downloadPriceLists(globalLocationNumber, filter));
        }
    }

    private Collection<DatahubPricelistRecord> downloadPriceLists(GlobalLocationNumber globalLocationNumber,
            DatahubTariffFilter filter) throws InterruptedException, DataServiceException {
        Map<String, String> properties = editProperties();
        Collection<DatahubPricelistRecord> records = apiController.getDatahubPriceLists(globalLocationNumber,
                ChargeType.Tariff, filter, properties);
        updateProperties(properties);

        return records;
    }

    private DatahubTariffFilter getNetTariffFilter() {
        Channel channel = getThing().getChannel(CHANNEL_NET_TARIFF);
        if (channel == null) {
            return DatahubTariffFilterFactory.getNetTariffByGLN(config.gridCompanyGLN);
        }

        DatahubPriceConfiguration datahubPriceConfiguration = channel.getConfiguration()
                .as(DatahubPriceConfiguration.class);

        if (!datahubPriceConfiguration.hasAnyFilterOverrides()) {
            return DatahubTariffFilterFactory.getNetTariffByGLN(config.gridCompanyGLN);
        }

        DateQueryParameter start = datahubPriceConfiguration.getStart();
        if (start == null) {
            logger.warn("Invalid channel configuration parameter 'start' or 'offset': {} (offset: {})",
                    datahubPriceConfiguration.start, datahubPriceConfiguration.offset);
            return DatahubTariffFilterFactory.getNetTariffByGLN(config.gridCompanyGLN);
        }

        Set<ChargeTypeCode> chargeTypeCodes = datahubPriceConfiguration.getChargeTypeCodes();
        Set<String> notes = datahubPriceConfiguration.getNotes();
        DatahubTariffFilter filter;
        if (!chargeTypeCodes.isEmpty() || !notes.isEmpty()) {
            // Completely override filter.
            filter = new DatahubTariffFilter(chargeTypeCodes, notes, start);
        } else {
            // Only override start date in pre-configured filter.
            filter = new DatahubTariffFilter(DatahubTariffFilterFactory.getNetTariffByGLN(config.gridCompanyGLN),
                    start);
        }

        return new DatahubTariffFilter(filter, DateQueryParameter.of(filter.getDateQueryParameter(),
                Duration.ofHours(-CacheManager.NUMBER_OF_HISTORIC_HOURS)));
    }

    private void updatePrices() {
        cacheManager.cleanup();

        updateCurrentSpotPrice();
        Arrays.stream(DatahubTariff.values())
                .forEach(tariff -> updateCurrentTariff(tariff.getChannelId(), cacheManager.getTariff(tariff)));
        updateHourlyPrices();

        reschedulePriceUpdateJob();
    }

    private void updateCurrentSpotPrice() {
        if (!isLinked(CHANNEL_SPOT_PRICE)) {
            return;
        }
        BigDecimal spotPrice = cacheManager.getSpotPrice();
        updateState(CHANNEL_SPOT_PRICE, spotPrice != null ? new DecimalType(spotPrice) : UnDefType.UNDEF);
    }

    private void updateCurrentTariff(String channelId, @Nullable BigDecimal tariff) {
        if (!isLinked(channelId)) {
            return;
        }
        updateState(channelId, tariff != null ? new DecimalType(tariff) : UnDefType.UNDEF);
    }

    private void updateHourlyPrices() {
        if (!isLinked(CHANNEL_HOURLY_PRICES)) {
            return;
        }
        Map<Instant, BigDecimal> spotPriceMap = cacheManager.getSpotPrices();
        Price[] targetPrices = new Price[spotPriceMap.size()];
        List<Entry<Instant, BigDecimal>> sourcePrices = spotPriceMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).toList();

        int i = 0;
        for (Entry<Instant, BigDecimal> sourcePrice : sourcePrices) {
            Instant hourStart = sourcePrice.getKey();
            BigDecimal netTariff = cacheManager.getTariff(DatahubTariff.NET_TARIFF, hourStart);
            BigDecimal systemTariff = cacheManager.getTariff(DatahubTariff.SYSTEM_TARIFF, hourStart);
            BigDecimal electricityTax = cacheManager.getTariff(DatahubTariff.ELECTRICITY_TAX, hourStart);
            BigDecimal reducedElectricityTax = cacheManager.getTariff(DatahubTariff.REDUCED_ELECTRICITY_TAX, hourStart);
            BigDecimal transmissionNetTariff = cacheManager.getTariff(DatahubTariff.TRANSMISSION_NET_TARIFF, hourStart);
            targetPrices[i++] = new Price(hourStart.toString(), sourcePrice.getValue(), config.currencyCode, netTariff,
                    systemTariff, electricityTax, reducedElectricityTax, transmissionNetTariff);
        }
        updateState(CHANNEL_HOURLY_PRICES, new StringType(gson.toJson(targetPrices)));
    }

    /**
     * Get the configured {@link Currency} for spot prices.
     * 
     * @return Spot price currency
     */
    public Currency getCurrency() {
        return config.getCurrency();
    }

    /**
     * Get cached spot prices or try once to download them if not cached
     * (usually if no items are linked).
     *
     * @return Map of future spot prices
     */
    public Map<Instant, BigDecimal> getSpotPrices() {
        try {
            downloadSpotPrices();
        } catch (DataServiceException e) {
            if (logger.isDebugEnabled()) {
                logger.warn("Error retrieving spot prices", e);
            } else {
                logger.warn("Error retrieving spot prices: {}", e.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return cacheManager.getSpotPrices();
    }

    /**
     * Return cached tariffs or try once to download them if not cached
     * (usually if no items are linked).
     *
     * @return Map of future tariffs
     */
    public Map<Instant, BigDecimal> getTariffs(DatahubTariff datahubTariff) {
        try {
            downloadTariffs(datahubTariff);
        } catch (DataServiceException e) {
            if (logger.isDebugEnabled()) {
                logger.warn("Error retrieving tariffs", e);
            } else {
                logger.warn("Error retrieving tariffs of type {}: {}", datahubTariff, e.getMessage());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return cacheManager.getTariffs(datahubTariff);
    }

    /**
     * Return whether reduced electricity tax is set in configuration.
     *
     * @return true if reduced electricity tax applies
     */
    public boolean isReducedElectricityTax() {
        return config.reducedElectricityTax;
    }

    private void reschedulePriceUpdateJob() {
        ScheduledFuture<?> priceUpdateJob = this.priceUpdateFuture;
        if (priceUpdateJob != null) {
            // Do not interrupt ourselves.
            priceUpdateJob.cancel(false);
            this.priceUpdateFuture = null;
        }

        Instant now = Instant.now();
        long millisUntilNextClockHour = Duration
                .between(now, now.plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS)).toMillis() + 1;
        this.priceUpdateFuture = scheduler.schedule(this::updatePrices, millisUntilNextClockHour,
                TimeUnit.MILLISECONDS);
        logger.debug("Price update job rescheduled in {} milliseconds", millisUntilNextClockHour);
    }

    private void rescheduleRefreshJob(RetryStrategy retryPolicy) {
        // Preserve state of previous retry policy when configuration is the same.
        if (!retryPolicy.equals(this.retryPolicy)) {
            this.retryPolicy = retryPolicy;
        }

        ScheduledFuture<?> refreshJob = this.refreshFuture;

        long secondsUntilNextRefresh = this.retryPolicy.getDuration().getSeconds();
        Instant timeOfNextRefresh = Instant.now().plusSeconds(secondsUntilNextRefresh);
        this.refreshFuture = scheduler.schedule(this::refreshElectricityPrices, secondsUntilNextRefresh,
                TimeUnit.SECONDS);
        logger.debug("Refresh job rescheduled in {} seconds: {}", secondsUntilNextRefresh, timeOfNextRefresh);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PROPERTY_DATETIME_FORMAT);
        updateProperty(PROPERTY_NEXT_CALL, LocalDateTime.ofInstant(timeOfNextRefresh, timeZoneProvider.getTimeZone())
                .truncatedTo(ChronoUnit.SECONDS).format(formatter));

        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
    }
}
