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
package org.openhab.binding.freecurrency.internal;

import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.freecurrency.internal.config.FreecurrencyServiceConfig;
import org.openhab.binding.freecurrency.internal.dto.CurrenciesDTO;
import org.openhab.binding.freecurrency.internal.dto.ExchangeRatesDTO;
import org.openhab.core.config.core.ConfigOptionProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.core.ParameterOption;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.dimension.Currency;
import org.openhab.core.library.unit.CurrencyProvider;
import org.openhab.core.library.unit.CurrencyUnit;
import org.openhab.core.scheduler.Scheduler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link FreecurrencyProvider} class implements a {@link CurrencyProvider} based on currencies and dynamic exchange
 * rates from <a href="https://freecurrencyapi.com">Freecurrency API</a>. It also allows to register
 * {@link ExchangeRateListener}s for classes that want to be notified about changed exchange rates.
 * 
 * @author Jan N. Klug - Initial contribution
 */
@Component(immediate = true, configurationPid = "binding.freecurrency", configurationPolicy = ConfigurationPolicy.REQUIRE, service = {
        CurrencyProvider.class, ConfigOptionProvider.class, FreecurrencyProvider.class })
@NonNullByDefault
public class FreecurrencyProvider implements CurrencyProvider, ConfigOptionProvider {
    private static final CurrencyInformation DEFAULT_CURRENCY_USD = new CurrencyInformation("USD",
            new CurrencyUnit("USD", null), "US Dollar");
    private static final Duration REFRESH_OFFSET = Duration.parse("P1DT1M");
    private final Logger logger = LoggerFactory.getLogger(FreecurrencyProvider.class);
    private final HttpClient httpClient;
    private final Scheduler scheduler;
    private final Gson gson = new Gson();
    private @NonNullByDefault({}) FreecurrencyServiceConfig config;
    private @Nullable ScheduledFuture<?> refreshJob;
    private Map<String, CurrencyInformation> currencies = Map.of();
    private Map<Unit<Currency>, BigDecimal> exchangeRates = Map.of();
    private @Nullable ZonedDateTime lastUpdated = null;
    private Set<ExchangeRateListener> exchangeRateListeners = new HashSet<>();

    @Activate
    public FreecurrencyProvider(@Reference HttpClientFactory httpClientFactory, @Reference Scheduler scheduler,
            Map<String, Object> config) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.scheduler = scheduler;
        modified(config);
    }

    @Modified
    public void modified(Map<String, Object> config) {
        stopRefresh();
        this.config = new Configuration(config).as(FreecurrencyServiceConfig.class);

        if (this.config.apiKey.isBlank()) {
            logger.warn("Configuration error: API key must not be blank.");
            return;
        }

        getCurrencies();
        getExchangeRates();
    }

    @Deactivate
    public void deactivate() {
        stopRefresh();
    }

    public void addListener(ExchangeRateListener listener) {
        exchangeRateListeners.add(listener);
        listener.onExchangeRatesChanged();
    }

    public void removeListener(ExchangeRateListener listener) {
        exchangeRateListeners.remove(listener);
    }

    public @Nullable BigDecimal getExchangeRate(String currency1, String currency2) {
        CurrencyInformation info1 = currencies.get(currency1);
        CurrencyInformation info2 = currencies.get(currency2.isBlank() ? config.baseCurrency : currency2);
        if (info1 == null || info2 == null) {
            return null;
        }
        BigDecimal rate1 = exchangeRates.get(info1.unit());
        BigDecimal rate2 = exchangeRates.get(info2.unit());
        if (rate1 == null || rate2 == null) {
            return null;
        }
        return rate2.divide(rate1, MathContext.DECIMAL128);
    }

    public @Nullable ZonedDateTime getLastUpdated() {
        return lastUpdated;
    }

    private void stopRefresh() {
        ScheduledFuture<?> localJob = this.refreshJob;
        if (localJob != null) {
            localJob.cancel(false);
            refreshJob = null;
        }
    }

    private void getCurrencies() {
        String uri = "https://api.freecurrencyapi.com/v1/currencies?apikey=" + config.apiKey;
        try {
            String currenciesJson = httpClient.GET(uri).getContentAsString();
            CurrenciesDTO currenciesDTO = gson.fromJson(currenciesJson, CurrenciesDTO.class);
            currencies = currenciesDTO.data.values().stream()
                    .map(c -> new CurrencyInformation(c.code, new CurrencyUnit(c.code, null), c.name))
                    .collect(Collectors.toMap(CurrencyInformation::code, u -> u));
            logger.debug("Retrieved {} currencies", currencies.size());
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Failed to request currencies", e);
        }
    }

    private void getExchangeRates() {
        if (!currencies.containsKey(config.baseCurrency)) {
            logger.warn("Configuration error: Base currency '{}' is not in list of available currencies {}.",
                    config.baseCurrency, currencies.keySet());
            return;
        }
        String uri = "https://api.freecurrencyapi.com/v1/latest?apikey=" + config.apiKey + "&base_currency="
                + config.baseCurrency;
        try {
            String currenciesJson = httpClient.GET(uri).getContentAsString();
            ExchangeRatesDTO exchangeRatesDTO = gson.fromJson(currenciesJson, ExchangeRatesDTO.class);
            Map<Unit<Currency>, BigDecimal> newExchangeRates = new HashMap<>();
            exchangeRatesDTO.data.forEach((k, v) -> {
                CurrencyInformation currencyInfo = currencies.get(k);
                if (currencyInfo == null) {
                    logger.debug("Not considering exchange rate for '{}' because it is not supported.", k);
                } else {
                    newExchangeRates.put(currencyInfo.unit, v);
                }
            });
            exchangeRates = newExchangeRates;
            logger.debug("Retrieved exchange rates for {} currencies", newExchangeRates.size());
            lastUpdated = ZonedDateTime.now();

            // exchange rates are refreshed every day at midnight UTC
            // we refresh one minute later to be sure we are not too early.
            Instant nextRefresh = Instant.now().truncatedTo(ChronoUnit.DAYS).plus(REFRESH_OFFSET);
            refreshJob = scheduler.at(this::getExchangeRates, nextRefresh);

            // notify listeners about changed exchange rates
            exchangeRateListeners.forEach(ExchangeRateListener::onExchangeRatesChanged);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            logger.debug("Failed to request currencies", e);
        }
    }

    @Override
    public String getName() {
        return "Freecurrency API";
    }

    @Override
    public Unit<Currency> getBaseCurrency() {
        return currencies.getOrDefault(config.baseCurrency, DEFAULT_CURRENCY_USD).unit();
    }

    @Override
    public Collection<Unit<Currency>> getAdditionalCurrencies() {
        return exchangeRates.keySet().stream().filter(c -> !config.baseCurrency.equals(c.getName())).toList();
    }

    @Override
    public Function<Unit<Currency>, @Nullable BigDecimal> getExchangeRateFunction() {
        return c -> exchangeRates.get(c);
    }

    @Override
    public @Nullable Collection<ParameterOption> getParameterOptions(URI uri, String param, @Nullable String context,
            @Nullable Locale locale) {
        if (("binding:freecurrency".equals(uri.toString()) && "baseCurrency".equals(param))
                || ("channel-type:freecurrency:exchange-rate".equals(uri.toString()) && param.startsWith("currency"))) {
            return currencies.values().stream().map(c -> new ParameterOption(c.code(), c.name() + " (" + c.code + ")"))
                    .toList();
        }
        return null;
    }

    private record CurrencyInformation(String code, Unit<Currency> unit, String name) {
    }
}
