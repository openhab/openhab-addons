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
package org.openhab.binding.energidataservice.internal.handler;

import static org.openhab.binding.energidataservice.internal.EnergiDataServiceBindingConstants.*;
import static org.openhab.core.types.TimeSeries.Policy.REPLACE;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.energidataservice.internal.ApiController;
import org.openhab.binding.energidataservice.internal.CacheManager;
import org.openhab.binding.energidataservice.internal.DatahubTariff;
import org.openhab.binding.energidataservice.internal.EnergiDataServiceBindingConstants;
import org.openhab.binding.energidataservice.internal.PriceListParser;
import org.openhab.binding.energidataservice.internal.action.EnergiDataServiceActions;
import org.openhab.binding.energidataservice.internal.api.ChargeType;
import org.openhab.binding.energidataservice.internal.api.ChargeTypeCode;
import org.openhab.binding.energidataservice.internal.api.DatahubTariffFilter;
import org.openhab.binding.energidataservice.internal.api.DatahubTariffFilterFactory;
import org.openhab.binding.energidataservice.internal.api.Dataset;
import org.openhab.binding.energidataservice.internal.api.DateQueryParameter;
import org.openhab.binding.energidataservice.internal.api.DateQueryParameterType;
import org.openhab.binding.energidataservice.internal.api.GlobalLocationNumber;
import org.openhab.binding.energidataservice.internal.api.dto.CO2EmissionRecord;
import org.openhab.binding.energidataservice.internal.api.dto.DatahubPricelistRecord;
import org.openhab.binding.energidataservice.internal.api.dto.ElspotpriceRecord;
import org.openhab.binding.energidataservice.internal.config.DatahubPriceConfiguration;
import org.openhab.binding.energidataservice.internal.config.EnergiDataServiceConfiguration;
import org.openhab.binding.energidataservice.internal.exception.DataServiceException;
import org.openhab.binding.energidataservice.internal.retry.RetryPolicyFactory;
import org.openhab.binding.energidataservice.internal.retry.RetryStrategy;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EnergiDataServiceHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class EnergiDataServiceHandler extends BaseThingHandler {

    private static final Duration emissionPrognosisJobInterval = Duration.ofMinutes(15);
    private static final Duration emissionRealtimeJobInterval = Duration.ofMinutes(5);

    private final Logger logger = LoggerFactory.getLogger(EnergiDataServiceHandler.class);
    private final TimeZoneProvider timeZoneProvider;
    private final ApiController apiController;
    private final CacheManager cacheManager;

    private EnergiDataServiceConfiguration config;
    private RetryStrategy retryPolicy = RetryPolicyFactory.initial();
    private boolean realtimeEmissionsFetchedFirstTime = false;
    private @Nullable ScheduledFuture<?> refreshPriceFuture;
    private @Nullable ScheduledFuture<?> refreshEmissionPrognosisFuture;
    private @Nullable ScheduledFuture<?> refreshEmissionRealtimeFuture;
    private @Nullable ScheduledFuture<?> priceUpdateFuture;

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

        String channelId = channelUID.getId();
        if (ELECTRICITY_CHANNELS.contains(channelId)) {
            refreshElectricityPrices();
        } else if (CHANNEL_CO2_EMISSION_PROGNOSIS.equals(channelId)) {
            rescheduleEmissionPrognosisJob();
        } else if (CHANNEL_CO2_EMISSION_REALTIME.equals(channelId)) {
            realtimeEmissionsFetchedFirstTime = false;
            rescheduleEmissionRealtimeJob();
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

        refreshPriceFuture = scheduler.schedule(this::refreshElectricityPrices, 0, TimeUnit.SECONDS);

        if (isLinked(CHANNEL_CO2_EMISSION_PROGNOSIS)) {
            rescheduleEmissionPrognosisJob();
        }
        if (isLinked(CHANNEL_CO2_EMISSION_REALTIME)) {
            rescheduleEmissionRealtimeJob();
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> refreshPriceFuture = this.refreshPriceFuture;
        if (refreshPriceFuture != null) {
            refreshPriceFuture.cancel(true);
            this.refreshPriceFuture = null;
        }
        ScheduledFuture<?> refreshEmissionPrognosisFuture = this.refreshEmissionPrognosisFuture;
        if (refreshEmissionPrognosisFuture != null) {
            refreshEmissionPrognosisFuture.cancel(true);
            this.refreshEmissionPrognosisFuture = null;
        }
        ScheduledFuture<?> refreshEmissionRealtimeFuture = this.refreshEmissionRealtimeFuture;
        if (refreshEmissionRealtimeFuture != null) {
            refreshEmissionRealtimeFuture.cancel(true);
            this.refreshEmissionRealtimeFuture = null;
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

    @Override
    public void channelLinked(ChannelUID channelUID) {
        super.channelLinked(channelUID);

        if (!"DK1".equals(config.priceArea) && !"DK2".equals(config.priceArea)
                && (CHANNEL_CO2_EMISSION_PROGNOSIS.equals(channelUID.getId())
                        || CHANNEL_CO2_EMISSION_REALTIME.contains(channelUID.getId()))) {
            logger.warn("Item linked to channel '{}', but price area {} is not supported for this channel",
                    channelUID.getId(), config.priceArea);
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        super.channelUnlinked(channelUID);

        if (CHANNEL_CO2_EMISSION_PROGNOSIS.equals(channelUID.getId()) && !isLinked(CHANNEL_CO2_EMISSION_PROGNOSIS)) {
            logger.debug("No more items linked to channel '{}', stopping emission prognosis refresh job",
                    channelUID.getId());
            ScheduledFuture<?> refreshEmissionPrognosisFuture = this.refreshEmissionPrognosisFuture;
            if (refreshEmissionPrognosisFuture != null) {
                refreshEmissionPrognosisFuture.cancel(true);
                this.refreshEmissionPrognosisFuture = null;
            }
        } else if (CHANNEL_CO2_EMISSION_REALTIME.contains(channelUID.getId())
                && !isLinked(CHANNEL_CO2_EMISSION_REALTIME)) {
            logger.debug("No more items linked to channel '{}', stopping realtime emission refresh job",
                    channelUID.getId());
            ScheduledFuture<?> refreshEmissionRealtimeFuture = this.refreshEmissionRealtimeFuture;
            if (refreshEmissionRealtimeFuture != null) {
                refreshEmissionRealtimeFuture.cancel(true);
                this.refreshEmissionRealtimeFuture = null;
            }
        }
    }

    private void refreshElectricityPrices() {
        RetryStrategy retryPolicy;
        try {
            boolean spotPricesDownloaded = false;
            if (isLinked(CHANNEL_SPOT_PRICE)) {
                spotPricesDownloaded = downloadSpotPrices();
            }

            for (DatahubTariff datahubTariff : DatahubTariff.values()) {
                if (isLinked(datahubTariff.getChannelId())) {
                    downloadTariffs(datahubTariff);
                }
            }

            updateStatus(ThingStatus.ONLINE);
            updatePrices();
            updateElectricityTimeSeriesFromCache();

            if (isLinked(CHANNEL_SPOT_PRICE)) {
                long numberOfFutureSpotPrices = cacheManager.getNumberOfFutureSpotPrices();
                LocalTime now = LocalTime.now(NORD_POOL_TIMEZONE);

                if (numberOfFutureSpotPrices >= 13 || (numberOfFutureSpotPrices == 12
                        && now.isAfter(DAILY_REFRESH_TIME_CET.minusHours(1)) && now.isBefore(DAILY_REFRESH_TIME_CET))) {
                    if (spotPricesDownloaded) {
                        triggerChannel(CHANNEL_EVENT, EVENT_DAY_AHEAD_AVAILABLE);
                    }
                    retryPolicy = RetryPolicyFactory.atFixedTime(DAILY_REFRESH_TIME_CET, NORD_POOL_TIMEZONE);
                } else {
                    logger.warn("Spot prices are not available, retry scheduled (see details in Thing properties)");
                    retryPolicy = RetryPolicyFactory.whenExpectedSpotPriceDataMissing();
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

        reschedulePriceRefreshJob(retryPolicy);
    }

    private boolean downloadSpotPrices() throws InterruptedException, DataServiceException {
        if (cacheManager.areSpotPricesFullyCached()) {
            logger.debug("Cached spot prices still valid, skipping download.");
            return false;
        }
        DateQueryParameter start;
        if (cacheManager.areHistoricSpotPricesCached()) {
            start = DateQueryParameter.of(DateQueryParameterType.UTC_NOW);
        } else {
            start = DateQueryParameter.of(DateQueryParameterType.UTC_NOW,
                    Duration.ofHours(-CacheManager.NUMBER_OF_HISTORIC_HOURS));
        }
        Map<String, String> properties = editProperties();
        try {
            ElspotpriceRecord[] spotPriceRecords = apiController.getSpotPrices(config.priceArea, config.getCurrency(),
                    start, DateQueryParameter.EMPTY, properties);
            cacheManager.putSpotPrices(spotPriceRecords, config.getCurrency());
        } finally {
            updateProperties(properties);
        }
        return true;
    }

    private void downloadTariffs(DatahubTariff datahubTariff) throws InterruptedException, DataServiceException {
        GlobalLocationNumber globalLocationNumber = getGlobalLocationNumber(datahubTariff);
        if (globalLocationNumber.isEmpty()) {
            return;
        }
        if (cacheManager.areTariffsValidTomorrow(datahubTariff)) {
            logger.debug("Cached tariffs of type {} still valid, skipping download.", datahubTariff);
            cacheManager.updateTariffs(datahubTariff);
        } else {
            DatahubTariffFilter filter = getDatahubTariffFilter(datahubTariff);
            cacheManager.putTariffs(datahubTariff, downloadPriceLists(globalLocationNumber, filter));
        }
    }

    private DatahubTariffFilter getDatahubTariffFilter(DatahubTariff datahubTariff) {
        return switch (datahubTariff) {
            case GRID_TARIFF -> getGridTariffFilter();
            case SYSTEM_TARIFF -> DatahubTariffFilterFactory.getSystemTariff();
            case TRANSMISSION_GRID_TARIFF -> DatahubTariffFilterFactory.getTransmissionGridTariff();
            case ELECTRICITY_TAX -> DatahubTariffFilterFactory.getElectricityTax();
            case REDUCED_ELECTRICITY_TAX -> DatahubTariffFilterFactory.getReducedElectricityTax();
        };
    }

    private GlobalLocationNumber getGlobalLocationNumber(DatahubTariff datahubTariff) {
        return switch (datahubTariff) {
            case GRID_TARIFF -> config.getGridCompanyGLN();
            default -> config.getEnerginetGLN();
        };
    }

    private Collection<DatahubPricelistRecord> downloadPriceLists(GlobalLocationNumber globalLocationNumber,
            DatahubTariffFilter filter) throws InterruptedException, DataServiceException {
        Map<String, String> properties = editProperties();
        try {
            return apiController.getDatahubPriceLists(globalLocationNumber, ChargeType.Tariff, filter, properties);
        } finally {
            updateProperties(properties);
        }
    }

    private DatahubTariffFilter getGridTariffFilter() {
        Channel channel = getThing().getChannel(CHANNEL_GRID_TARIFF);
        if (channel == null) {
            return DatahubTariffFilterFactory.getGridTariffByGLN(config.gridCompanyGLN);
        }

        DatahubPriceConfiguration datahubPriceConfiguration = channel.getConfiguration()
                .as(DatahubPriceConfiguration.class);

        if (!datahubPriceConfiguration.hasAnyFilterOverrides()) {
            return DatahubTariffFilterFactory.getGridTariffByGLN(config.gridCompanyGLN);
        }

        DateQueryParameter start = datahubPriceConfiguration.getStart();
        if (start == null) {
            logger.warn("Invalid channel configuration parameter 'start' or 'offset': {} (offset: {})",
                    datahubPriceConfiguration.start, datahubPriceConfiguration.offset);
            return DatahubTariffFilterFactory.getGridTariffByGLN(config.gridCompanyGLN);
        }

        Set<ChargeTypeCode> chargeTypeCodes = datahubPriceConfiguration.getChargeTypeCodes();
        Set<String> notes = datahubPriceConfiguration.getNotes();
        DatahubTariffFilter filter;
        if (!chargeTypeCodes.isEmpty() || !notes.isEmpty()) {
            // Completely override filter.
            filter = new DatahubTariffFilter(chargeTypeCodes, notes, start);
        } else {
            // Only override start date in pre-configured filter.
            filter = new DatahubTariffFilter(DatahubTariffFilterFactory.getGridTariffByGLN(config.gridCompanyGLN),
                    start);
        }

        return new DatahubTariffFilter(filter,
                DateQueryParameter.of(filter.getStart(), Duration.ofHours(-CacheManager.NUMBER_OF_HISTORIC_HOURS)));
    }

    private void refreshCo2EmissionPrognosis() {
        try {
            updateCo2Emissions(Dataset.CO2EmissionPrognosis, CHANNEL_CO2_EMISSION_PROGNOSIS,
                    DateQueryParameter.of(DateQueryParameterType.UTC_NOW, Duration.ofMinutes(-5)));
            updateStatus(ThingStatus.ONLINE);
        } catch (DataServiceException e) {
            if (e.getHttpStatus() != 0) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        HttpStatus.getCode(e.getHttpStatus()).getMessage());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
            if (e.getCause() != null) {
                logger.debug("Error retrieving CO2 emission prognosis", e);
            }
        } catch (InterruptedException e) {
            logger.debug("Emission prognosis refresh job interrupted");
            Thread.currentThread().interrupt();
            return;
        }
    }

    private void refreshCo2EmissionRealtime() {
        try {
            updateCo2Emissions(Dataset.CO2Emission, CHANNEL_CO2_EMISSION_REALTIME,
                    DateQueryParameter.of(DateQueryParameterType.UTC_NOW,
                            realtimeEmissionsFetchedFirstTime ? Duration.ofMinutes(-5) : Duration.ofHours(-24)));
            realtimeEmissionsFetchedFirstTime = true;
            updateStatus(ThingStatus.ONLINE);
        } catch (DataServiceException e) {
            if (e.getHttpStatus() != 0) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR,
                        HttpStatus.getCode(e.getHttpStatus()).getMessage());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
            }
            if (e.getCause() != null) {
                logger.debug("Error retrieving CO2 realtime emissions", e);
            }
        } catch (InterruptedException e) {
            logger.debug("Emission realtime refresh job interrupted");
            Thread.currentThread().interrupt();
            return;
        }
    }

    private void updateCo2Emissions(Dataset dataset, String channelId, DateQueryParameter dateQueryParameter)
            throws InterruptedException, DataServiceException {
        if (!"DK1".equals(config.priceArea) && !"DK2".equals(config.priceArea)) {
            // Dataset is only for Denmark.
            return;
        }
        Map<String, String> properties = editProperties();
        CO2EmissionRecord[] emissionRecords = apiController.getCo2Emissions(dataset, config.priceArea,
                dateQueryParameter, properties);
        updateProperties(properties);

        TimeSeries timeSeries = new TimeSeries(REPLACE);
        Instant now = Instant.now();

        if (dataset == Dataset.CO2Emission && emissionRecords.length > 0) {
            // Records are sorted descending, first record is current.
            updateState(channelId, new QuantityType<>(emissionRecords[0].emission(), Units.GRAM_PER_KILOWATT_HOUR));
        }

        for (CO2EmissionRecord emissionRecord : emissionRecords) {
            State state = new QuantityType<>(emissionRecord.emission(), Units.GRAM_PER_KILOWATT_HOUR);
            timeSeries.add(emissionRecord.start(), state);

            if (dataset == Dataset.CO2EmissionPrognosis && now.compareTo(emissionRecord.start()) >= 0
                    && now.compareTo(emissionRecord.end()) < 0) {
                updateState(channelId, state);
            }
        }
        sendTimeSeries(channelId, timeSeries);
    }

    private void updatePrices() {
        cacheManager.cleanup();

        updateCurrentSpotPrice();
        Arrays.stream(DatahubTariff.values())
                .forEach(tariff -> updateCurrentTariff(tariff.getChannelId(), cacheManager.getTariff(tariff)));

        reschedulePriceUpdateJob();
    }

    private void updateCurrentSpotPrice() {
        if (!isLinked(CHANNEL_SPOT_PRICE)) {
            return;
        }
        BigDecimal spotPrice = cacheManager.getSpotPrice();
        updatePriceState(CHANNEL_SPOT_PRICE, spotPrice, config.getCurrency());
    }

    private void updateCurrentTariff(String channelId, @Nullable BigDecimal tariff) {
        if (!isLinked(channelId)) {
            return;
        }
        updatePriceState(channelId, tariff, CURRENCY_DKK);
    }

    private void updatePriceState(String channelID, @Nullable BigDecimal price, Currency currency) {
        updateState(channelID, price != null ? getEnergyPrice(price, currency) : UnDefType.UNDEF);
    }

    private State getEnergyPrice(BigDecimal price, Currency currency) {
        String currencyCode = currency.getCurrencyCode();
        Unit<?> unit = CurrencyUnits.getInstance().getUnit(currencyCode);
        if (unit == null) {
            logger.trace("Currency {} is unknown, falling back to DecimalType", currency.getCurrencyCode());
            return new DecimalType(price);
        }
        try {
            return new QuantityType<>(price + " " + currencyCode + "/kWh");
        } catch (IllegalArgumentException e) {
            logger.debug("Unable to create QuantityType, falling back to DecimalType", e);
            return new DecimalType(price);
        }
    }

    /**
     * Download spot prices in requested period and update corresponding channel with time series.
     * 
     * @param startDate Start date of period
     * @param endDate End date of period
     * @return number of published states
     */
    public int updateSpotPriceTimeSeries(LocalDate startDate, LocalDate endDate)
            throws InterruptedException, DataServiceException {
        if (!isLinked(CHANNEL_SPOT_PRICE)) {
            return 0;
        }
        Map<String, String> properties = editProperties();
        try {
            Currency currency = config.getCurrency();
            ElspotpriceRecord[] spotPriceRecords = apiController.getSpotPrices(config.priceArea, currency,
                    DateQueryParameter.of(startDate), DateQueryParameter.of(endDate.plusDays(1)), properties);
            boolean isDKK = EnergiDataServiceBindingConstants.CURRENCY_DKK.equals(currency);
            TimeSeries spotPriceTimeSeries = new TimeSeries(REPLACE);
            if (spotPriceRecords.length == 0) {
                return 0;
            }
            for (ElspotpriceRecord record : Arrays.stream(spotPriceRecords)
                    .sorted(Comparator.comparing(ElspotpriceRecord::hour)).toList()) {
                spotPriceTimeSeries.add(record.hour(), getEnergyPrice(
                        (isDKK ? record.spotPriceDKK() : record.spotPriceEUR()).divide(BigDecimal.valueOf(1000)),
                        currency));
            }
            sendTimeSeries(CHANNEL_SPOT_PRICE, spotPriceTimeSeries);
            return spotPriceRecords.length;
        } finally {
            updateProperties(properties);
        }
    }

    /**
     * Download tariffs in requested period and update corresponding channel with time series.
     * 
     * @param datahubTariff Tariff to update
     * @param startDate Start date of period
     * @param endDate End date of period
     * @return number of published states
     */
    public int updateTariffTimeSeries(DatahubTariff datahubTariff, LocalDate startDate, LocalDate endDate)
            throws InterruptedException, DataServiceException {
        if (!isLinked(datahubTariff.getChannelId())) {
            return 0;
        }
        GlobalLocationNumber globalLocationNumber = getGlobalLocationNumber(datahubTariff);
        if (globalLocationNumber.isEmpty()) {
            return 0;
        }
        DatahubTariffFilter filter = getDatahubTariffFilter(datahubTariff);
        DateQueryParameter start = filter.getStart();
        DateQueryParameterType filterStartDateType = start.getDateType();
        LocalDate filterStartDate = start.getDate();
        if (filterStartDateType != null) {
            // For filters with date relative to current date, override with provided parameters.
            filter = new DatahubTariffFilter(filter, DateQueryParameter.of(startDate), DateQueryParameter.of(endDate));
        } else if (filterStartDate != null && startDate.isBefore(filterStartDate)) {
            throw new IllegalArgumentException("Start date before " + start.getDate() + " is not supported");
        }
        Collection<DatahubPricelistRecord> datahubRecords = downloadPriceLists(globalLocationNumber, filter);
        ZoneId zoneId = timeZoneProvider.getTimeZone();
        Instant firstHourStart = startDate.atStartOfDay(zoneId).toInstant();
        Instant lastHourStart = endDate.plusDays(1).atStartOfDay(zoneId).toInstant();
        Map<Instant, BigDecimal> tariffMap = new PriceListParser().toHourly(datahubRecords, firstHourStart,
                lastHourStart);

        return updatePriceTimeSeries(datahubTariff.getChannelId(), tariffMap, CURRENCY_DKK, true);
    }

    private void updateElectricityTimeSeriesFromCache() {
        updatePriceTimeSeries(CHANNEL_SPOT_PRICE, cacheManager.getSpotPrices(), config.getCurrency(), false);

        for (DatahubTariff datahubTariff : DatahubTariff.values()) {
            String channelId = datahubTariff.getChannelId();
            updatePriceTimeSeries(channelId, cacheManager.getTariffs(datahubTariff), CURRENCY_DKK, true);
        }
    }

    private int updatePriceTimeSeries(String channelId, Map<Instant, BigDecimal> priceMap, Currency currency,
            boolean deduplicate) {
        if (!isLinked(channelId)) {
            return 0;
        }
        List<Entry<Instant, BigDecimal>> prices = priceMap.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .toList();
        TimeSeries timeSeries = new TimeSeries(REPLACE);
        BigDecimal previousTariff = null;
        for (Entry<Instant, BigDecimal> price : prices) {
            Instant hourStart = price.getKey();
            BigDecimal priceValue = price.getValue();
            if (deduplicate && priceValue.equals(previousTariff)) {
                // Skip redundant states.
                continue;
            }
            timeSeries.add(hourStart, getEnergyPrice(priceValue, currency));
            previousTariff = priceValue;
        }
        if (timeSeries.size() > 0) {
            sendTimeSeries(channelId, timeSeries);
        }
        return timeSeries.size();
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

    private void reschedulePriceRefreshJob(RetryStrategy retryPolicy) {
        // Preserve state of previous retry policy when configuration is the same.
        if (!retryPolicy.equals(this.retryPolicy)) {
            this.retryPolicy = retryPolicy;
        }

        ScheduledFuture<?> refreshJob = this.refreshPriceFuture;

        long secondsUntilNextRefresh = this.retryPolicy.getDuration().getSeconds();
        Instant timeOfNextRefresh = Instant.now().plusSeconds(secondsUntilNextRefresh);
        this.refreshPriceFuture = scheduler.schedule(this::refreshElectricityPrices, secondsUntilNextRefresh,
                TimeUnit.SECONDS);
        logger.debug("Price refresh job rescheduled in {} seconds: {}", secondsUntilNextRefresh, timeOfNextRefresh);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(PROPERTY_DATETIME_FORMAT);
        updateProperty(PROPERTY_NEXT_CALL, LocalDateTime.ofInstant(timeOfNextRefresh, timeZoneProvider.getTimeZone())
                .truncatedTo(ChronoUnit.SECONDS).format(formatter));

        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
    }

    private void rescheduleEmissionPrognosisJob() {
        logger.debug("Scheduling emission prognosis refresh job now and every {}", emissionPrognosisJobInterval);

        ScheduledFuture<?> refreshEmissionPrognosisFuture = this.refreshEmissionPrognosisFuture;
        if (refreshEmissionPrognosisFuture != null) {
            refreshEmissionPrognosisFuture.cancel(true);
        }

        this.refreshEmissionPrognosisFuture = scheduler.scheduleWithFixedDelay(this::refreshCo2EmissionPrognosis, 0,
                emissionPrognosisJobInterval.toSeconds(), TimeUnit.SECONDS);
    }

    private void rescheduleEmissionRealtimeJob() {
        logger.debug("Scheduling emission realtime refresh job now and every {}", emissionRealtimeJobInterval);

        ScheduledFuture<?> refreshEmissionFuture = this.refreshEmissionRealtimeFuture;
        if (refreshEmissionFuture != null) {
            refreshEmissionFuture.cancel(true);
        }

        this.refreshEmissionRealtimeFuture = scheduler.scheduleWithFixedDelay(this::refreshCo2EmissionRealtime, 0,
                emissionRealtimeJobInterval.toSeconds(), TimeUnit.SECONDS);
    }
}
