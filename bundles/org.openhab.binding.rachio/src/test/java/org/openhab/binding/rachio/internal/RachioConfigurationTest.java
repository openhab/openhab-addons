/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.rachio.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.DEFAULT_EVENT_HISTORY_LOOKBACK_HOURS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.DEFAULT_FORECAST_UNITS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.DEFAULT_HOSE_SUMMARY_LOOKAHEAD_DAYS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.DEFAULT_HOSE_SUMMARY_LOOKBACK_DAYS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.MAX_EVENT_HISTORY_LOOKBACK_HOURS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.MAX_HOSE_SUMMARY_WINDOW_DAYS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_APIKEY;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_CALLBACK_URL;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_EVENT_HISTORY_LOOKBACK_HOURS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_FORECAST_UNITS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_HOSE_SUMMARY_LOOKAHEAD_DAYS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_HOSE_SUMMARY_LOOKBACK_DAYS;
import static org.openhab.binding.rachio.internal.RachioConfiguration.ConfigurationSource.CLOUD_THING;
import static org.openhab.binding.rachio.internal.RachioConfiguration.ConfigurationSource.DEFAULT;
import static org.openhab.binding.rachio.internal.RachioConfiguration.ConfigurationSource.DEPRECATED_BINDING_FALLBACK;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Tests robust Rachio binding configuration parsing.
 *
 * @author openHAB Contributors - Initial contribution
 */
class RachioConfigurationTest {
    @Test
    void invalidEventHistoryLookbackUsesDefault() {
        RachioConfiguration configuration = new RachioConfiguration();

        configuration.updateConfig(Map.of(PARAM_EVENT_HISTORY_LOOKBACK_HOURS, "not-a-number"));

        assertThat(configuration.eventHistoryLookbackHours, is(DEFAULT_EVENT_HISTORY_LOOKBACK_HOURS));
    }

    @Test
    void negativeEventHistoryLookbackDisablesPolling() {
        RachioConfiguration configuration = new RachioConfiguration();

        configuration.updateConfig(Map.of(PARAM_EVENT_HISTORY_LOOKBACK_HOURS, "-1"));

        assertThat(configuration.eventHistoryLookbackHours, is(0));
    }

    @Test
    void excessivelyLargeEventHistoryLookbackIsClamped() {
        RachioConfiguration configuration = new RachioConfiguration();

        configuration.updateConfig(Map.of(PARAM_EVENT_HISTORY_LOOKBACK_HOURS, "9999"));

        assertThat(configuration.eventHistoryLookbackHours, is(MAX_EVENT_HISTORY_LOOKBACK_HOURS));
    }

    @Test
    void invalidForecastUnitsUsesDefault() {
        RachioConfiguration configuration = new RachioConfiguration();

        configuration.updateConfig(Map.of(PARAM_FORECAST_UNITS, "SI"));

        assertThat(configuration.forecastUnits, is(DEFAULT_FORECAST_UNITS));
    }

    @Test
    void forecastUnitsAreNormalized() {
        RachioConfiguration configuration = new RachioConfiguration();

        configuration.updateConfig(Map.of(PARAM_FORECAST_UNITS, "us"));

        assertThat(configuration.forecastUnits, is("US"));
    }

    @Test
    void invalidHoseSummaryWindowsAreDefaultedOrClamped() {
        RachioConfiguration invalidConfiguration = new RachioConfiguration();
        invalidConfiguration.updateConfig(Map.of(PARAM_HOSE_SUMMARY_LOOKBACK_DAYS, "not-a-number"));

        RachioConfiguration negativeConfiguration = new RachioConfiguration();
        negativeConfiguration.updateConfig(Map.of(PARAM_HOSE_SUMMARY_LOOKAHEAD_DAYS, "-1"));

        RachioConfiguration largeConfiguration = new RachioConfiguration();
        largeConfiguration.updateConfig(Map.of(PARAM_HOSE_SUMMARY_LOOKAHEAD_DAYS, "999"));

        assertThat(invalidConfiguration.hoseSummaryLookbackDays, is(DEFAULT_HOSE_SUMMARY_LOOKBACK_DAYS));
        assertThat(negativeConfiguration.hoseSummaryLookaheadDays, is(0));
        assertThat(largeConfiguration.hoseSummaryLookaheadDays, is(MAX_HOSE_SUMMARY_WINDOW_DAYS));
    }

    @Test
    void hoseSummaryWindowsUseConfiguredValues() {
        RachioConfiguration configuration = new RachioConfiguration();

        configuration
                .updateConfig(Map.of(PARAM_HOSE_SUMMARY_LOOKBACK_DAYS, "3", PARAM_HOSE_SUMMARY_LOOKAHEAD_DAYS, "10"));

        assertThat(configuration.hoseSummaryLookbackDays, is(3));
        assertThat(configuration.hoseSummaryLookaheadDays, is(10));
        assertThat(DEFAULT_HOSE_SUMMARY_LOOKAHEAD_DAYS, is(7));
    }

    @Test
    void effectiveConfigurationPrefersCloudThingValuesOverDeprecatedBindingFallback() {
        RachioConfiguration bindingFallback = new RachioConfiguration();
        bindingFallback.updateConfig(Map.of(PARAM_FORECAST_UNITS, "METRIC", PARAM_HOSE_SUMMARY_LOOKAHEAD_DAYS, "4",
                PARAM_EVENT_HISTORY_LOOKBACK_HOURS, "12", PARAM_CALLBACK_URL,
                "https://legacy.example.org/rachio/webhook"));

        RachioConfiguration.ResolvedConfiguration resolvedConfiguration = RachioConfiguration.resolveEffectiveConfig(
                bindingFallback,
                Map.of(PARAM_FORECAST_UNITS, "US", PARAM_HOSE_SUMMARY_LOOKAHEAD_DAYS, "9",
                        PARAM_EVENT_HISTORY_LOOKBACK_HOURS, "6", PARAM_CALLBACK_URL,
                        "https://cloud.example.org/rachio/webhook"));
        RachioConfiguration configuration = resolvedConfiguration.configuration();

        assertThat(configuration.forecastUnits, is("US"));
        assertThat(configuration.hoseSummaryLookaheadDays, is(9));
        assertThat(configuration.eventHistoryLookbackHours, is(6));
        assertThat(configuration.callbackUrl, is("https://cloud.example.org/rachio/webhook"));
        assertThat(resolvedConfiguration.source(PARAM_FORECAST_UNITS), is(CLOUD_THING));
        assertThat(resolvedConfiguration.source(PARAM_HOSE_SUMMARY_LOOKAHEAD_DAYS), is(CLOUD_THING));
        assertThat(bindingFallback.forecastUnits, is("METRIC"));
    }

    @Test
    void effectiveConfigurationUsesDeprecatedBindingFallbackOnlyWhenCloudValueIsAbsent() {
        RachioConfiguration bindingFallback = new RachioConfiguration();
        bindingFallback.updateConfig(Map.of(PARAM_FORECAST_UNITS, "US", PARAM_HOSE_SUMMARY_LOOKBACK_DAYS, "5",
                PARAM_EVENT_HISTORY_LOOKBACK_HOURS, "8", PARAM_CALLBACK_URL,
                "https://legacy.example.org/rachio/webhook"));

        RachioConfiguration.ResolvedConfiguration resolvedConfiguration = RachioConfiguration
                .resolveEffectiveConfig(bindingFallback, Map.of(PARAM_APIKEY, "thing-api-key"));
        RachioConfiguration configuration = resolvedConfiguration.configuration();

        assertThat(configuration.apikey, is("thing-api-key"));
        assertThat(configuration.forecastUnits, is("US"));
        assertThat(configuration.hoseSummaryLookbackDays, is(5));
        assertThat(configuration.eventHistoryLookbackHours, is(8));
        assertThat(configuration.callbackUrl, is("https://legacy.example.org/rachio/webhook"));
        assertThat(resolvedConfiguration.source(PARAM_APIKEY), is(CLOUD_THING));
        assertThat(resolvedConfiguration.source(PARAM_FORECAST_UNITS), is(DEPRECATED_BINDING_FALLBACK));
        assertThat(resolvedConfiguration.source(PARAM_CALLBACK_URL), is(DEPRECATED_BINDING_FALLBACK));
    }

    @Test
    void effectiveConfigurationUsesBuiltInDefaultsWhenNoCloudOrBindingValueExists() {
        RachioConfiguration.ResolvedConfiguration resolvedConfiguration = RachioConfiguration
                .resolveEffectiveConfig(new RachioConfiguration(), Map.of());
        RachioConfiguration configuration = resolvedConfiguration.configuration();

        assertThat(configuration.forecastUnits, is(DEFAULT_FORECAST_UNITS));
        assertThat(configuration.hoseSummaryLookbackDays, is(DEFAULT_HOSE_SUMMARY_LOOKBACK_DAYS));
        assertThat(configuration.hoseSummaryLookaheadDays, is(DEFAULT_HOSE_SUMMARY_LOOKAHEAD_DAYS));
        assertThat(configuration.eventHistoryLookbackHours, is(DEFAULT_EVENT_HISTORY_LOOKBACK_HOURS));
        assertThat(configuration.callbackUrl, is(""));
        assertThat(resolvedConfiguration.source(PARAM_FORECAST_UNITS), is(DEFAULT));
        assertThat(resolvedConfiguration.source(PARAM_HOSE_SUMMARY_LOOKAHEAD_DAYS), is(DEFAULT));
    }
}
