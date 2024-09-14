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
package org.openhab.binding.entsoe.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.entsoe.internal.client.Client;
import org.openhab.binding.entsoe.internal.client.Request;
import org.openhab.binding.entsoe.internal.exception.entsoeConfigurationException;
import org.openhab.binding.entsoe.internal.exception.entsoeResponseException;
import org.openhab.binding.entsoe.internal.exception.entsoeResponseMapException;
import org.openhab.binding.entsoe.internal.exception.entsoeUnexpectedException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.dimension.Currency;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.CurrencyUnit;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link entsoeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jørgen Melhus - Initial contribution
 */
@NonNullByDefault
public class entsoeHandler extends BaseThingHandler {

    private final Logger _logger = LoggerFactory.getLogger(entsoeHandler.class);

    private entsoeConfiguration _config;

    private @Nullable ScheduledFuture<?> _refreshJob;

    private Unit<Currency> _baseUnit = CurrencyUnits.BASE_CURRENCY;
    private Unit<Currency> _fromUnit = new CurrencyUnit(entsoeBindingConstants.ENTSOE_CURRENCY, null);

    private @Nullable Map<ZonedDateTime, Double> _responseMap;

    private ZonedDateTime lastDayAheadReceived = ZonedDateTime.of(LocalDateTime.MIN, ZoneId.of("UTC"));

    private int historicDaysInitially = 0;

    public entsoeHandler(Thing thing) {
        super(thing);
        _logger.trace("entsoeHandler(thing:{})", thing.getLabel());
        _logger.trace("Reading entsoeConfiguration");
        _config = getConfigAs(entsoeConfiguration.class);
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        _logger.trace("channelLinked(channelUID:{})", channelUID.getAsString());
        String channelID = channelUID.getId();
        _logger.info("Channel linked {}", channelID);

        if (channelID.equals(entsoeBindingConstants.CHANNEL_SPOT_PRICES))
            updateCurrentHourState(entsoeBindingConstants.CHANNEL_SPOT_PRICES);

        if (channelID.equals(entsoeBindingConstants.CHANNEL_LAST_DAY_AHEAD_RECEIVED) && lastDayAheadReceived
                .toEpochSecond() > ZonedDateTime.of(LocalDateTime.MIN, ZoneId.of("UTC")).toEpochSecond())
            updateState(entsoeBindingConstants.CHANNEL_LAST_DAY_AHEAD_RECEIVED, new DateTimeType(lastDayAheadReceived));
    }

    @Override
    public void dispose() {
        _logger.trace("dispose()");
        if (_refreshJob != null) {
            _refreshJob.cancel(true);
            _refreshJob = null;
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        _logger.trace("handleCommand(channelUID:{}, command:{})", channelUID.getAsString(), command.toFullString());
        _logger.debug("ChannelUID: {}, Command: {}", channelUID, command);

        if (command instanceof RefreshType) {
            _logger.debug("Command Instance Of Refresh");
            refreshPrices();
        } else {
            _logger.debug("Command Instance Not Implemented");
        }
    }

    @Override
    public void initialize() {
        _logger.trace("initialize()");
        updateStatus(ThingStatus.UNKNOWN);

        _logger.trace("Reading entsoeConfiguration");
        _config = getConfigAs(entsoeConfiguration.class);

        if (historicDaysInitially == 0) {
            historicDaysInitially = _config.historicDays;
            _logger.info("Setting initial value of historicDays: {}", historicDaysInitially);
        }

        BigDecimal rate = getExchangeRate();
        if (rate.compareTo(new BigDecimal(0)) == 1) {
            updateStatus(ThingStatus.ONLINE);
            _logger.debug("Initialized {}", isInitialized());
            if (isInitialized())
                _refreshJob = scheduler.schedule(this::refreshPrices, 5, TimeUnit.SECONDS);
        } else {
            String msg = "Could not get exchange rate from openHAB. Have you configured a currency binding to fetch currency rates (e.g. Freecurrency) and set your default currency provider together with a default base currency?";
            _logger.error(msg);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, msg);
        }
    }

    @Override
    public void thingUpdated(Thing thing) {
        _logger.trace("thingUpdated(thing:{})", thing.getLabel());
        super.thingUpdated(thing);
    }

    private ZonedDateTime currentUtcTime() {
        _logger.trace("currentUtcTime()");
        return ZonedDateTime.now(ZoneId.of("UTC"));
    }

    private ZonedDateTime currentUtcTimeWholeHours() {
        _logger.trace("currentUtcTimeWholeHours()");
        return ZonedDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.HOURS);
    }

    @SuppressWarnings("null")
    private BigDecimal getCurrentHourExchangedKwhPrice() throws entsoeResponseMapException {
        _logger.trace("getCurrentHourExchangedKwhPrice()");
        if (_responseMap == null) {
            throw new entsoeResponseMapException("responseMap is empty");
        }
        Integer currentHour = currentUtcTime().getHour();
        Integer currentDayInYear = currentUtcTime().getDayOfYear();
        var result = _responseMap.entrySet().stream()
                .filter(x -> x.getKey().getHour() == currentHour && x.getKey().getDayOfYear() == currentDayInYear)
                .findFirst();
        if (result.isEmpty()) {
            throw new entsoeResponseMapException(
                    String.format("Could not find a value for hour {} and day in year {} in responseMap", currentHour,
                            currentDayInYear));
        }

        double eurMwhPrice = result.get().getValue();
        BigDecimal exchangedKwhPrice = getExchangedKwhPrice(eurMwhPrice);
        return exchangedKwhPrice;
    }

    private BigDecimal getExchangedKwhPrice(double eurMwhPrice) {
        _logger.trace("getExchangedKwhPrice()");
        BigDecimal kwhPrice = BigDecimal.valueOf(eurMwhPrice).divide(new BigDecimal(1000), 20, RoundingMode.HALF_UP);
        BigDecimal exchangeRate = getExchangeRate();
        BigDecimal exchangedKwhPrice = kwhPrice.divide(exchangeRate, 10, RoundingMode.HALF_UP);
        BigDecimal totalExchangedKwhPrice = exchangedKwhPrice.add(BigDecimal.valueOf(_config.additionalCost));
        BigDecimal vat = BigDecimal.valueOf((_config.vat + 100.0) / 100.0);
        BigDecimal finalKwhPrice = vat.multiply(totalExchangedKwhPrice);
        _logger.debug("EUR price {} Exchange rate {} Exchanged price {} Local price {} VAT {} Result {}", eurMwhPrice,
                exchangeRate, exchangedKwhPrice, totalExchangedKwhPrice, vat, finalKwhPrice);
        return finalKwhPrice;
    }

    private BigDecimal getExchangeRate() {
        _logger.trace("getExchangeRate()");
        BigDecimal rate = CurrencyUnits.getExchangeRate(_fromUnit);

        if (rate == null) {
            rate = new BigDecimal(0);
        } else {
            _logger.debug("Exchange rate {}{}: {}", _fromUnit.getName(), _baseUnit.getName(), rate.floatValue());
        }
        return rate;
    }

    private long getSecondsToNextHour() {
        _logger.trace("getSecondsToNextHour()");
        ZonedDateTime now = currentUtcTime().truncatedTo(ChronoUnit.SECONDS);
        ZonedDateTime then = now.plusHours(1).truncatedTo(ChronoUnit.HOURS);
        long seconds = now.until(then, ChronoUnit.SECONDS);
        _logger.debug("Seconds to next hour {}", seconds);
        return seconds;
    }

    private boolean needToFetchHistoricDays() {
        return needToFetchHistoricDays(false);
    }

    private boolean needToFetchHistoricDays(boolean updateHistoricDaysInitially) {
        _logger.trace("needToFetchHistoricDays(updateHistoricDaysInitially:{})", updateHistoricDaysInitially);
        boolean needToFetch = false;
        if (historicDaysInitially < _config.historicDays) {
            _logger.info("Need to fetch historic data. Historicdays was changed to a greater number: {}",
                    _config.historicDays);
            needToFetch = true;
        }

        if (updateHistoricDaysInitially && historicDaysInitially != _config.historicDays)
            historicDaysInitially = _config.historicDays;

        return needToFetch;
    }

    @SuppressWarnings("null")
    private void refreshPrices() {
        _logger.trace("refreshPrices()");
        if (!isLinked(entsoeBindingConstants.CHANNEL_SPOT_PRICES)) {
            _logger.debug("Channel {} is not linked, cant update channel", entsoeBindingConstants.CHANNEL_SPOT_PRICES);
            return;
        }

        _config = getConfigAs(entsoeConfiguration.class);

        ZonedDateTime startUtc = currentUtcTimeWholeHours()
                .minusDays(needToFetchHistoricDays() ? _config.historicDays : 1).withHour(22);
        ZonedDateTime endUtc = currentUtcTimeWholeHours().plusDays(1).withHour(22);

        boolean needsUpdate = lastDayAheadReceived.equals(ZonedDateTime.of(LocalDateTime.MIN, ZoneId.of("UTC")))
                || _responseMap == null || needToFetchHistoricDays(true);

        boolean hasNextDayValue = needsUpdate ? false
                : _responseMap.entrySet().stream()
                        .anyMatch(x -> x.getKey().getDayOfYear() == currentUtcTime().plusDays(1).getDayOfYear());
        boolean readyForNextDayValue = needsUpdate ? true
                : currentUtcTime().toEpochSecond() > currentUtcTime().withHour(_config.spotPricesAvailableUtcHour)
                        .toEpochSecond();

        // Update whole time series
        if (needsUpdate || (!hasNextDayValue && readyForNextDayValue)) {
            _logger.debug("Updating timeseries");
            Request request = new Request(_config.securityToken, _config.area, startUtc, endUtc);
            Client client = new Client();
            boolean success = false;

            try {
                _responseMap = client.doGetRequest(request, 30000);
                TimeSeries baseTimeSeries = new TimeSeries(entsoeBindingConstants.TIMESERIES_POLICY);
                for (Map.Entry<ZonedDateTime, Double> entry : _responseMap.entrySet()) {
                    BigDecimal exchangedKwhPrice = getExchangedKwhPrice(entry.getValue());
                    State baseState = new QuantityType<>(exchangedKwhPrice + " " + _baseUnit.getName() + "/kWh");
                    baseTimeSeries.add(entry.getKey().toInstant(), baseState);
                }
                lastDayAheadReceived = currentUtcTime();
                sendTimeSeries(entsoeBindingConstants.CHANNEL_SPOT_PRICES, baseTimeSeries);
                updateState(entsoeBindingConstants.CHANNEL_LAST_DAY_AHEAD_RECEIVED,
                        new DateTimeType(lastDayAheadReceived));
                updateCurrentHourState(entsoeBindingConstants.CHANNEL_SPOT_PRICES);
                scheduler.schedule(this::triggerChannelSpotPricesReceived, 30, TimeUnit.SECONDS);
                success = true;
            } catch (entsoeResponseException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        String.format("%s", e.getMessage()));
                _logger.error("{}", e.getMessage());
            } catch (entsoeUnexpectedException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        String.format("%s", e.getMessage()));
                _logger.error("{}", e.getMessage());
            } catch (entsoeConfigurationException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        String.format("%s", e.getMessage()));
                _logger.error("{}", e.getMessage());
            } finally {
                schedule(success);
            }
        }
        // Update current hour with refreshed exchange rate
        else {
            _logger.debug("Updating current hour");
            updateCurrentHourState(entsoeBindingConstants.CHANNEL_SPOT_PRICES);
            schedule(true);
        }
    }

    private void schedule(boolean success) {
        _logger.trace("schedule(success:{})", success);
        if (!success) {
            // not successful, run again in 5 minutes
            _refreshJob = scheduler.schedule(this::refreshPrices, 300, TimeUnit.SECONDS);
        } else {
            // run each whole hour
            _refreshJob = scheduler.schedule(this::refreshPrices, getSecondsToNextHour(), TimeUnit.SECONDS);
        }
    }

    private void triggerChannelSpotPricesReceived() {
        triggerChannel(entsoeBindingConstants.CHANNEL_TRIGGER_PRICES_RECEIVED);
    }

    private void updateCurrentHourState(String channelID) {
        _logger.trace("updateCurrentHourState(channelID:{})", channelID);
        try {
            BigDecimal exchangedKwhPrice = getCurrentHourExchangedKwhPrice();
            _logger.trace("updateCurrentHourState price is: {}", exchangedKwhPrice);
            updateState(channelID, new DecimalType(exchangedKwhPrice));
        } catch (entsoeResponseMapException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    String.format("%s", e.getMessage()));
            _logger.error("{}", e.getMessage());
        }
    }

    @SuppressWarnings("unused")
    private void updateEntsoeConfig(String key, Object value) {
        _logger.trace("updateEntsoeConfig(key:{},value:{})", key, value);
        Configuration config = editConfiguration();
        config.put(key, value);
        updateConfiguration(config);
        _config = getConfigAs(entsoeConfiguration.class);
    }
}
