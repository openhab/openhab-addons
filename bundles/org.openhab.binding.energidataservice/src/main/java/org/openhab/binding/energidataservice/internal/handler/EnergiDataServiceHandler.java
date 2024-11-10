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
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.energidataservice.internal.ApiController;
import org.openhab.binding.energidataservice.internal.DatahubTariff;
import org.openhab.binding.energidataservice.internal.PriceListParser;
import org.openhab.binding.energidataservice.internal.action.EnergiDataServiceActions;
import org.openhab.binding.energidataservice.internal.api.ChargeType;
import org.openhab.binding.energidataservice.internal.api.ChargeTypeCode;
import org.openhab.binding.energidataservice.internal.api.DatahubTariffFilter;
import org.openhab.binding.energidataservice.internal.api.DateQueryParameter;
import org.openhab.binding.energidataservice.internal.api.DateQueryParameterType;
import org.openhab.binding.energidataservice.internal.api.GlobalLocationNumber;
import org.openhab.binding.energidataservice.internal.api.dto.DatahubPricelistRecord;
import org.openhab.binding.energidataservice.internal.api.dto.ElspotpriceRecord;
import org.openhab.binding.energidataservice.internal.api.filter.DatahubTariffFilterFactory;
import org.openhab.binding.energidataservice.internal.config.DatahubPriceConfiguration;
import org.openhab.binding.energidataservice.internal.config.EnergiDataServiceConfiguration;
import org.openhab.binding.energidataservice.internal.exception.DataServiceException;
import org.openhab.binding.energidataservice.internal.provider.Co2EmissionProvider;
import org.openhab.binding.energidataservice.internal.provider.ElectricityPriceProvider;
import org.openhab.binding.energidataservice.internal.provider.cache.ElectricityPriceSubscriptionCache;
import org.openhab.binding.energidataservice.internal.provider.listener.Co2EmissionListener;
import org.openhab.binding.energidataservice.internal.provider.listener.ElectricityPriceListener;
import org.openhab.binding.energidataservice.internal.provider.subscription.Co2EmissionSubscription;
import org.openhab.binding.energidataservice.internal.provider.subscription.DatahubPriceSubscription;
import org.openhab.binding.energidataservice.internal.provider.subscription.ElectricityPriceSubscription;
import org.openhab.binding.energidataservice.internal.provider.subscription.SpotPriceSubscription;
import org.openhab.binding.energidataservice.internal.provider.subscription.Subscription;
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
public class EnergiDataServiceHandler extends BaseThingHandler
        implements ElectricityPriceListener, Co2EmissionListener {

    private static final Map<String, DatahubTariff> CHANNEL_ID_TO_DATAHUB_TARIFF = Arrays.stream(DatahubTariff.values())
            .collect(Collectors.toMap(DatahubTariff::getChannelId, Function.identity()));

    private final Logger logger = LoggerFactory.getLogger(EnergiDataServiceHandler.class);
    private final TimeZoneProvider timeZoneProvider;
    private final ApiController apiController;
    private final ElectricityPriceProvider electricityPriceProvider;
    private final Co2EmissionProvider co2EmissionProvider;
    private final DatahubTariffFilterFactory datahubTariffFilterFactory;
    private final Set<Subscription> activeSubscriptions = new HashSet<>();

    private EnergiDataServiceConfiguration config;

    public EnergiDataServiceHandler(final Thing thing, final HttpClient httpClient,
            final TimeZoneProvider timeZoneProvider, final ElectricityPriceProvider electricityPriceProvider,
            final Co2EmissionProvider co2EmissionProvider,
            final DatahubTariffFilterFactory datahubTariffFilterFactory) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
        this.apiController = new ApiController(httpClient, timeZoneProvider);
        this.electricityPriceProvider = electricityPriceProvider;
        this.co2EmissionProvider = co2EmissionProvider;
        this.datahubTariffFilterFactory = datahubTariffFilterFactory;

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
            if (!electricityPriceProvider.forceRefreshPrices(getChannelSubscription(channelId))) {
                // All subscriptions are automatically notified upon actual changes after download.
                // If cached values are the same, we will update the requested channel directly.
                updateChannelFromCache(getChannelSubscription(channelId), channelId);
            }
        } else if (CO2_EMISSION_CHANNELS.contains(channelId)) {
            Subscription subscription = getChannelSubscription(channelId);
            unsubscribe(subscription);
            subscribe(subscription);
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

        if (SUBSCRIPTION_CHANNELS.stream().anyMatch(this::isLinked)) {
            updateStatus(ThingStatus.UNKNOWN);
            subscribeLinkedChannels();
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void dispose() {
        electricityPriceProvider.unsubscribe(this);
        co2EmissionProvider.unsubscribe(this);
        activeSubscriptions.clear();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(EnergiDataServiceActions.class);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        String channelId = channelUID.getId();
        if (!SUBSCRIPTION_CHANNELS.contains(channelId)) {
            // Do not trigger REFRESH command for subscription-based channels, we will trigger
            // a state update ourselves through relevant provider.
            super.channelLinked(channelUID);
        }

        if (ELECTRICITY_CHANNELS.contains(channelId)) {
            Subscription subscription = getChannelSubscription(channelId);
            if (subscribe(subscription)) {
                logger.debug("First item linked to channel '{}', starting {}", channelId, subscription);
            } else {
                updateChannelFromCache(subscription, channelId);
            }
        } else if (CO2_EMISSION_CHANNELS.contains(channelId)) {
            if ("DK1".equals(config.priceArea) || "DK2".equals(config.priceArea)) {
                Subscription subscription = getChannelSubscription(channelId);
                if (subscribe(subscription)) {
                    logger.debug("First item linked to channel '{}', starting {}", channelId, subscription);
                }
            } else {
                logger.warn("Item linked to channel '{}', but price area {} is not supported for this channel",
                        channelId, config.priceArea);
            }
        }
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        super.channelUnlinked(channelUID);

        String channelId = channelUID.getId();
        if (SUBSCRIPTION_CHANNELS.contains(channelId) && !isLinked(channelId)) {
            Subscription subscription = getChannelSubscription(channelId);
            logger.debug("No more items linked to channel '{}', stopping {}", channelId, subscription);
            unsubscribe(getChannelSubscription(channelId));
        }
    }

    @Override
    public void onDayAheadAvailable() {
        triggerChannel(CHANNEL_EVENT, EVENT_DAY_AHEAD_AVAILABLE);
    }

    @Override
    public void onCurrentSpotPrice(@Nullable BigDecimal price, Currency currency) {
        updateStatus(ThingStatus.ONLINE);
        updatePriceState(CHANNEL_SPOT_PRICE, price, currency);
    }

    @Override
    public void onSpotPrices(Map<Instant, BigDecimal> spotPrices, Currency currency) {
        updateStatus(ThingStatus.ONLINE);
        updatePriceTimeSeries(CHANNEL_SPOT_PRICE, spotPrices, currency, false);
    }

    @Override
    public void onCurrentTariff(DatahubTariff datahubTariff, @Nullable BigDecimal tariff) {
        updateStatus(ThingStatus.ONLINE);
        updatePriceState(datahubTariff.getChannelId(), tariff, CURRENCY_DKK);
    }

    @Override
    public void onTariffs(DatahubTariff datahubTariff, Map<Instant, BigDecimal> tariffs) {
        updateStatus(ThingStatus.ONLINE);
        updatePriceTimeSeries(datahubTariff.getChannelId(), tariffs, CURRENCY_DKK, true);
    }

    @Override
    public void onCurrentEmission(Co2EmissionSubscription.Type type, BigDecimal emission) {
        updateStatus(ThingStatus.ONLINE);
        updateState(type == Co2EmissionSubscription.Type.Prognosis ? CHANNEL_CO2_EMISSION_PROGNOSIS
                : CHANNEL_CO2_EMISSION_REALTIME, new QuantityType<>(emission, Units.GRAM_PER_KILOWATT_HOUR));
    }

    @Override
    public void onEmissions(Co2EmissionSubscription.Type type, Map<Instant, BigDecimal> emissions) {
        updateStatus(ThingStatus.ONLINE);
        TimeSeries timeSeries = new TimeSeries(REPLACE);
        for (Entry<Instant, BigDecimal> emission : emissions.entrySet()) {
            timeSeries.add(emission.getKey(), new QuantityType<>(emission.getValue(), Units.GRAM_PER_KILOWATT_HOUR));
        }
        sendTimeSeries(type == Co2EmissionSubscription.Type.Prognosis ? CHANNEL_CO2_EMISSION_PROGNOSIS
                : CHANNEL_CO2_EMISSION_REALTIME, timeSeries);
    }

    @Override
    public void onPropertiesUpdated(Map<String, String> properties) {
        updateProperties(properties);
    }

    @Override
    public void onCommunicationError(@Nullable String description) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, description);
    }

    @Override
    public String toString() {
        return this.thing.getUID().getAsString();
    }

    private void subscribeLinkedChannels() {
        if (isLinked(CHANNEL_SPOT_PRICE)) {
            subscribe(getChannelSubscription(CHANNEL_SPOT_PRICE));
        }

        Arrays.stream(DatahubTariff.values()).filter(tariff -> isLinked(tariff.getChannelId()))
                .map(tariff -> DatahubPriceSubscription.of(tariff, getGlobalLocationNumber(tariff),
                        getDatahubTariffFilter(tariff)))
                .forEach(this::subscribe);

        if ("DK1".equals(config.priceArea) || "DK2".equals(config.priceArea)) {
            CO2_EMISSION_CHANNELS.stream().filter(this::isLinked)
                    .forEach(channelId -> subscribe(getChannelSubscription(channelId)));
        }
    }

    private boolean subscribe(Subscription subscription) {
        if (activeSubscriptions.add(subscription)) {
            if (subscription instanceof ElectricityPriceSubscription) {
                electricityPriceProvider.subscribe(this, subscription);
            } else if (subscription instanceof Co2EmissionSubscription) {
                co2EmissionProvider.subscribe(this, subscription);
            } else {
                throw new IllegalArgumentException(subscription.getClass().getName() + " is not supported");
            }
            return true;
        } else {
            return false;
        }
    }

    private void unsubscribe(Subscription subscription) {
        if (activeSubscriptions.remove(subscription)) {
            if (subscription instanceof ElectricityPriceSubscription) {
                electricityPriceProvider.unsubscribe(this, subscription);
            } else if (subscription instanceof Co2EmissionSubscription) {
                co2EmissionProvider.unsubscribe(this, subscription);
            } else {
                throw new IllegalArgumentException(subscription.getClass().getName() + " is not supported");
            }
        }
    }

    private Subscription getChannelSubscription(String channelId) {
        if (CHANNEL_SPOT_PRICE.equals(channelId)) {
            return SpotPriceSubscription.of(config.priceArea, config.getCurrency());
        } else if (CHANNEL_CO2_EMISSION_PROGNOSIS.equals(channelId)) {
            return Co2EmissionSubscription.of(config.priceArea, Co2EmissionSubscription.Type.Prognosis);
        } else if (CHANNEL_CO2_EMISSION_REALTIME.equals(channelId)) {
            return Co2EmissionSubscription.of(config.priceArea, Co2EmissionSubscription.Type.Realtime);
        } else {
            DatahubTariff tariff = CHANNEL_ID_TO_DATAHUB_TARIFF.get(channelId);

            if (tariff != null) {
                return DatahubPriceSubscription.of(tariff, getGlobalLocationNumber(tariff),
                        getDatahubTariffFilter(tariff));
            }
        }
        throw new IllegalArgumentException("Could not create subscription for channel id " + channelId);
    }

    private void updateChannelFromCache(Subscription subscription, String channelId) {
        BigDecimal currentPrice = electricityPriceProvider.getCurrentPriceIfCached(subscription);
        Map<Instant, BigDecimal> prices = electricityPriceProvider.getPricesIfCached(subscription);
        if (subscription instanceof SpotPriceSubscription) {
            updatePriceState(channelId, currentPrice, config.getCurrency());
            updatePriceTimeSeries(channelId, prices, config.getCurrency(), false);
        } else if (subscription instanceof DatahubPriceSubscription) {
            updatePriceState(channelId, currentPrice, CURRENCY_DKK);
            updatePriceTimeSeries(channelId, prices, CURRENCY_DKK, true);
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

    private DatahubTariffFilter getGridTariffFilter() {
        Channel channel = getThing().getChannel(CHANNEL_GRID_TARIFF);
        if (channel == null) {
            return datahubTariffFilterFactory.getGridTariffByGLN(config.gridCompanyGLN);
        }

        DatahubPriceConfiguration datahubPriceConfiguration = channel.getConfiguration()
                .as(DatahubPriceConfiguration.class);

        if (!datahubPriceConfiguration.hasAnyFilterOverrides()) {
            return datahubTariffFilterFactory.getGridTariffByGLN(config.gridCompanyGLN);
        }

        DateQueryParameter start = datahubPriceConfiguration.getStart();
        if (start == null) {
            logger.warn("Invalid channel configuration parameter 'start' or 'offset': {} (offset: {})",
                    datahubPriceConfiguration.start, datahubPriceConfiguration.offset);
            return datahubTariffFilterFactory.getGridTariffByGLN(config.gridCompanyGLN);
        }

        Set<ChargeTypeCode> chargeTypeCodes = datahubPriceConfiguration.getChargeTypeCodes();
        Set<String> notes = datahubPriceConfiguration.getNotes();
        DatahubTariffFilter filter;
        if (!chargeTypeCodes.isEmpty() || !notes.isEmpty()) {
            // Completely override filter.
            filter = new DatahubTariffFilter(chargeTypeCodes, notes, start);
        } else {
            // Only override start date in pre-configured filter.
            filter = new DatahubTariffFilter(datahubTariffFilterFactory.getGridTariffByGLN(config.gridCompanyGLN),
                    start);
        }

        return new DatahubTariffFilter(filter, DateQueryParameter.of(filter.getStart(),
                Duration.ofHours(-ElectricityPriceSubscriptionCache.NUMBER_OF_HISTORIC_HOURS)));
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
            boolean isDKK = CURRENCY_DKK.equals(currency);
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

    private Collection<DatahubPricelistRecord> downloadPriceLists(GlobalLocationNumber globalLocationNumber,
            DatahubTariffFilter filter) throws InterruptedException, DataServiceException {
        Map<String, String> properties = editProperties();
        try {
            return apiController.getDatahubPriceLists(globalLocationNumber, ChargeType.Tariff, filter, properties);
        } finally {
            updateProperties(properties);
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
        return this.getPrices(getChannelSubscription(CHANNEL_SPOT_PRICE));
    }

    /**
     * Return cached tariffs or try once to download them if not cached
     * (usually if no items are linked).
     *
     * @return Map of future tariffs
     */
    public Map<Instant, BigDecimal> getTariffs(DatahubTariff datahubTariff) {
        return this.getPrices(DatahubPriceSubscription.of(datahubTariff, getGlobalLocationNumber(datahubTariff),
                getDatahubTariffFilter(datahubTariff)));
    }

    private Map<Instant, BigDecimal> getPrices(Subscription subscription) {
        try {
            return electricityPriceProvider.getPrices(subscription);
        } catch (DataServiceException e) {
            logger.warn("Error retrieving prices for subscription {}: {}", subscription, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return Map.of();
    }

    /**
     * Return whether reduced electricity tax is set in configuration.
     *
     * @return true if reduced electricity tax applies
     */
    public boolean isReducedElectricityTax() {
        return config.reducedElectricityTax;
    }
}
