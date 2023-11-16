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
package org.openhab.binding.energidataservice.internal.action;

import static org.openhab.binding.energidataservice.internal.EnergiDataServiceBindingConstants.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.energidataservice.internal.DatahubTariff;
import org.openhab.binding.energidataservice.internal.PriceCalculator;
import org.openhab.binding.energidataservice.internal.exception.MissingPriceException;
import org.openhab.binding.energidataservice.internal.handler.EnergiDataServiceHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link EnergiDataServiceActions} provides actions for getting energy data into a rule context.
 *
 * @author Jacob Laursen - Initial contribution
 */
@ThingActionsScope(name = "energidataservice")
@NonNullByDefault
public class EnergiDataServiceActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(EnergiDataServiceActions.class);

    private @Nullable EnergiDataServiceHandler handler;

    private enum PriceElement {
        SPOT_PRICE("spotprice", null),
        NET_TARIFF("nettariff", DatahubTariff.NET_TARIFF),
        SYSTEM_TARIFF("systemtariff", DatahubTariff.SYSTEM_TARIFF),
        ELECTRICITY_TAX("electricitytax", DatahubTariff.ELECTRICITY_TAX),
        REDUCED_ELECTRICITY_TAX("reducedelectricitytax", DatahubTariff.REDUCED_ELECTRICITY_TAX),
        TRANSMISSION_NET_TARIFF("transmissionnettariff", DatahubTariff.TRANSMISSION_NET_TARIFF);

        private static final Map<String, PriceElement> NAME_MAP = Stream.of(values())
                .collect(Collectors.toMap(PriceElement::toString, Function.identity()));

        private String name;
        private @Nullable DatahubTariff datahubTariff;

        private PriceElement(String name, @Nullable DatahubTariff datahubTariff) {
            this.name = name;
            this.datahubTariff = datahubTariff;
        }

        @Override
        public String toString() {
            return name;
        }

        public static PriceElement fromString(final String name) {
            PriceElement myEnum = NAME_MAP.get(name.toLowerCase());
            if (null == myEnum) {
                throw new IllegalArgumentException(String.format("'%s' has no corresponding value. Accepted values: %s",
                        name, Arrays.asList(values())));
            }
            return myEnum;
        }

        public @Nullable DatahubTariff getDatahubTariff() {
            return datahubTariff;
        }
    }

    @RuleAction(label = "@text/action.get-prices.label", description = "@text/action.get-prices.description")
    public @ActionOutput(name = "prices", type = "java.util.Map<java.time.Instant, java.math.BigDecimal>") Map<Instant, BigDecimal> getPrices() {
        EnergiDataServiceHandler handler = this.handler;
        if (handler == null) {
            logger.warn("EnergiDataServiceActions ThingHandler is null.");
            return Map.of();
        }

        boolean isReducedElectricityTax = handler.isReducedElectricityTax();

        return getPrices(Arrays.stream(PriceElement.values())
                .filter(element -> element != (isReducedElectricityTax ? PriceElement.ELECTRICITY_TAX
                        : PriceElement.REDUCED_ELECTRICITY_TAX))
                .collect(Collectors.toSet()));
    }

    @RuleAction(label = "@text/action.get-prices.label", description = "@text/action.get-prices.description")
    public @ActionOutput(name = "prices", type = "java.util.Map<java.time.Instant, java.math.BigDecimal>") Map<Instant, BigDecimal> getPrices(
            @ActionInput(name = "priceElements", label = "@text/action.get-prices.priceElements.label", description = "@text/action.get-prices.priceElements.description") @Nullable String priceElements) {
        if (priceElements == null) {
            logger.warn("Argument 'priceElements' is null");
            return Map.of();
        }

        Set<PriceElement> priceElementsSet;
        try {
            priceElementsSet = new HashSet<PriceElement>(
                    Arrays.stream(priceElements.split(",")).map(PriceElement::fromString).toList());
        } catch (IllegalArgumentException e) {
            logger.warn("{}", e.getMessage());
            return Map.of();
        }

        return getPrices(priceElementsSet);
    }

    @RuleAction(label = "@text/action.calculate-price.label", description = "@text/action.calculate-price.description")
    public @ActionOutput(name = "price", type = "java.math.BigDecimal") BigDecimal calculatePrice(
            @ActionInput(name = "start", type = "java.time.Instant") Instant start,
            @ActionInput(name = "end", type = "java.time.Instant") Instant end,
            @ActionInput(name = "power", type = "QuantityType<Power>") QuantityType<Power> power) {
        PriceCalculator priceCalculator = new PriceCalculator(getPrices());

        try {
            return priceCalculator.calculatePrice(start, end, power);
        } catch (MissingPriceException e) {
            logger.warn("{}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }

    @RuleAction(label = "@text/action.calculate-cheapest-period.label", description = "@text/action.calculate-cheapest-period.description")
    public @ActionOutput(name = "result", type = "java.util.Map<String, Object>") Map<String, Object> calculateCheapestPeriod(
            @ActionInput(name = "earliestStart", type = "java.time.Instant") Instant earliestStart,
            @ActionInput(name = "latestEnd", type = "java.time.Instant") Instant latestEnd,
            @ActionInput(name = "duration", type = "java.time.Duration") Duration duration) {
        PriceCalculator priceCalculator = new PriceCalculator(getPrices());

        try {
            Map<String, Object> intermediateResult = priceCalculator.calculateCheapestPeriod(earliestStart, latestEnd,
                    duration, QuantityType.valueOf(1000, Units.WATT));

            // Create new result with stripped price information.
            Map<String, Object> result = new HashMap<>();
            Object value = intermediateResult.get("CheapestStart");
            if (value != null) {
                result.put("CheapestStart", value);
            }
            value = intermediateResult.get("MostExpensiveStart");
            if (value != null) {
                result.put("MostExpensiveStart", value);
            }
            return result;
        } catch (MissingPriceException | IllegalArgumentException e) {
            logger.warn("{}", e.getMessage());
            return Map.of();
        }
    }

    @RuleAction(label = "@text/action.calculate-cheapest-period.label", description = "@text/action.calculate-cheapest-period.description")
    public @ActionOutput(name = "result", type = "java.util.Map<String, Object>") Map<String, Object> calculateCheapestPeriod(
            @ActionInput(name = "earliestStart", type = "java.time.Instant") Instant earliestStart,
            @ActionInput(name = "latestEnd", type = "java.time.Instant") Instant latestEnd,
            @ActionInput(name = "duration", type = "java.time.Duration") Duration duration,
            @ActionInput(name = "power", type = "QuantityType<Power>") QuantityType<Power> power) {
        PriceCalculator priceCalculator = new PriceCalculator(getPrices());

        try {
            return priceCalculator.calculateCheapestPeriod(earliestStart, latestEnd, duration, power);
        } catch (MissingPriceException | IllegalArgumentException e) {
            logger.warn("{}", e.getMessage());
            return Map.of();
        }
    }

    @RuleAction(label = "@text/action.calculate-cheapest-period.label", description = "@text/action.calculate-cheapest-period.description")
    public @ActionOutput(name = "result", type = "java.util.Map<String, Object>") Map<String, Object> calculateCheapestPeriod(
            @ActionInput(name = "earliestStart", type = "java.time.Instant") Instant earliestStart,
            @ActionInput(name = "latestEnd", type = "java.time.Instant") Instant latestEnd,
            @ActionInput(name = "totalDuration", type = "java.time.Duration") Duration totalDuration,
            @ActionInput(name = "durationPhases", type = "java.util.List<java.time.Duration>") List<Duration> durationPhases,
            @ActionInput(name = "energyUsedPerPhase", type = "QuantityType<Energy>") QuantityType<Energy> energyUsedPerPhase) {
        PriceCalculator priceCalculator = new PriceCalculator(getPrices());

        try {
            return priceCalculator.calculateCheapestPeriod(earliestStart, latestEnd, totalDuration, durationPhases,
                    energyUsedPerPhase);
        } catch (MissingPriceException | IllegalArgumentException e) {
            logger.warn("{}", e.getMessage());
            return Map.of();
        }
    }

    @RuleAction(label = "@text/action.calculate-cheapest-period.label", description = "@text/action.calculate-cheapest-period.description")
    public @ActionOutput(name = "result", type = "java.util.Map<String, Object>") Map<String, Object> calculateCheapestPeriod(
            @ActionInput(name = "earliestStart", type = "java.time.Instant") Instant earliestStart,
            @ActionInput(name = "latestEnd", type = "java.time.Instant") Instant latestEnd,
            @ActionInput(name = "durationPhases", type = "java.util.List<java.time.Duration>") List<Duration> durationPhases,
            @ActionInput(name = "powerPhases", type = "java.util.List<QuantityType<Power>>") List<QuantityType<Power>> powerPhases) {
        if (durationPhases.size() != powerPhases.size()) {
            logger.warn("Number of duration phases ({}) is different from number of consumption phases ({})",
                    durationPhases.size(), powerPhases.size());
            return Map.of();
        }
        PriceCalculator priceCalculator = new PriceCalculator(getPrices());

        try {
            return priceCalculator.calculateCheapestPeriod(earliestStart, latestEnd, durationPhases, powerPhases);
        } catch (MissingPriceException | IllegalArgumentException e) {
            logger.warn("{}", e.getMessage());
            return Map.of();
        }
    }

    private Map<Instant, BigDecimal> getPrices(Set<PriceElement> priceElements) {
        EnergiDataServiceHandler handler = this.handler;
        if (handler == null) {
            logger.warn("EnergiDataServiceActions ThingHandler is null.");
            return Map.of();
        }

        Map<Instant, BigDecimal> prices;
        boolean spotPricesRequired;
        if (priceElements.contains(PriceElement.SPOT_PRICE)) {
            if (priceElements.size() > 1 && !handler.getCurrency().equals(CURRENCY_DKK)) {
                logger.warn("Cannot calculate sum when spot price currency is {}", handler.getCurrency());
                return Map.of();
            }
            prices = handler.getSpotPrices();
            spotPricesRequired = true;
        } else {
            spotPricesRequired = false;
            prices = new HashMap<>();
        }

        for (PriceElement priceElement : PriceElement.values()) {
            DatahubTariff datahubTariff = priceElement.getDatahubTariff();
            if (datahubTariff == null) {
                continue;
            }

            if (priceElements.contains(priceElement)) {
                Map<Instant, BigDecimal> tariffMap = handler.getTariffs(datahubTariff);
                mergeMaps(prices, tariffMap, !spotPricesRequired);
            }
        }

        return prices;
    }

    private void mergeMaps(Map<Instant, BigDecimal> destinationMap, Map<Instant, BigDecimal> sourceMap,
            boolean createNew) {
        for (Entry<Instant, BigDecimal> source : sourceMap.entrySet()) {
            Instant key = source.getKey();
            BigDecimal sourceValue = source.getValue();
            BigDecimal destinationValue = destinationMap.get(key);
            if (destinationValue != null) {
                destinationMap.put(key, sourceValue.add(destinationValue));
            } else if (createNew) {
                destinationMap.put(key, sourceValue);
            }
        }
    }

    /**
     * Static get prices method for DSL rule compatibility.
     *
     * @param actions
     * @param priceElements Comma-separated list of price elements to include in prices.
     * @return Map of prices
     */
    public static Map<Instant, BigDecimal> getPrices(@Nullable ThingActions actions, @Nullable String priceElements) {
        if (actions instanceof EnergiDataServiceActions serviceActions) {
            if (priceElements != null && !priceElements.isBlank()) {
                return serviceActions.getPrices(priceElements);
            } else {
                return serviceActions.getPrices();
            }
        } else {
            throw new IllegalArgumentException("Instance is not an EnergiDataServiceActions class.");
        }
    }

    /**
     * Static get prices method for DSL rule compatibility.
     *
     * @param actions
     * @param start Start time
     * @param end End time
     * @param power Constant power consumption
     * @return Map of prices
     */
    public static BigDecimal calculatePrice(@Nullable ThingActions actions, @Nullable Instant start,
            @Nullable Instant end, @Nullable QuantityType<Power> power) {
        if (start == null || end == null || power == null) {
            return BigDecimal.ZERO;
        }
        if (actions instanceof EnergiDataServiceActions serviceActions) {
            return serviceActions.calculatePrice(start, end, power);
        } else {
            throw new IllegalArgumentException("Instance is not an EnergiDataServiceActions class.");
        }
    }

    public static Map<String, Object> calculateCheapestPeriod(@Nullable ThingActions actions,
            @Nullable Instant earliestStart, @Nullable Instant latestEnd, @Nullable Duration duration) {
        if (actions instanceof EnergiDataServiceActions serviceActions) {
            if (earliestStart == null || latestEnd == null || duration == null) {
                return Map.of();
            }
            return serviceActions.calculateCheapestPeriod(earliestStart, latestEnd, duration);
        } else {
            throw new IllegalArgumentException("Instance is not an EnergiDataServiceActions class.");
        }
    }

    public static Map<String, Object> calculateCheapestPeriod(@Nullable ThingActions actions,
            @Nullable Instant earliestStart, @Nullable Instant latestEnd, @Nullable Duration duration,
            @Nullable QuantityType<Power> power) {
        if (actions instanceof EnergiDataServiceActions serviceActions) {
            if (earliestStart == null || latestEnd == null || duration == null || power == null) {
                return Map.of();
            }
            return serviceActions.calculateCheapestPeriod(earliestStart, latestEnd, duration, power);
        } else {
            throw new IllegalArgumentException("Instance is not an EnergiDataServiceActions class.");
        }
    }

    public static Map<String, Object> calculateCheapestPeriod(@Nullable ThingActions actions,
            @Nullable Instant earliestStart, @Nullable Instant latestEnd, @Nullable Duration totalDuration,
            @Nullable List<Duration> durationPhases, @Nullable QuantityType<Energy> energyUsedPerPhase) {
        if (actions instanceof EnergiDataServiceActions serviceActions) {
            if (earliestStart == null || latestEnd == null || totalDuration == null || durationPhases == null
                    || energyUsedPerPhase == null) {
                return Map.of();
            }
            return serviceActions.calculateCheapestPeriod(earliestStart, latestEnd, totalDuration, durationPhases,
                    energyUsedPerPhase);
        } else {
            throw new IllegalArgumentException("Instance is not an EnergiDataServiceActions class.");
        }
    }

    public static Map<String, Object> calculateCheapestPeriod(@Nullable ThingActions actions,
            @Nullable Instant earliestStart, @Nullable Instant latestEnd, @Nullable List<Duration> durationPhases,
            @Nullable List<QuantityType<Power>> powerPhases) {
        if (actions instanceof EnergiDataServiceActions serviceActions) {
            if (earliestStart == null || latestEnd == null || durationPhases == null || powerPhases == null) {
                return Map.of();
            }
            return serviceActions.calculateCheapestPeriod(earliestStart, latestEnd, durationPhases, powerPhases);
        } else {
            throw new IllegalArgumentException("Instance is not an EnergiDataServiceActions class.");
        }
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof EnergiDataServiceHandler serviceHandler) {
            this.handler = serviceHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return handler;
    }
}
