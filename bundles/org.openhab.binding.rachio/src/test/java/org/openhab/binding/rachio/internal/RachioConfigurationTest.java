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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.DEFAULT_EVENT_HISTORY_LOOKBACK_HOURS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.DEFAULT_FORECAST_UNITS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.DEFAULT_HOSE_SUMMARY_LOOKAHEAD_DAYS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.DEFAULT_HOSE_SUMMARY_LOOKBACK_DAYS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.MAX_EVENT_HISTORY_LOOKBACK_HOURS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.MAX_HOSE_SUMMARY_WINDOW_DAYS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_APIKEY;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_CALLBACK_PASSWORD;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_CALLBACK_URL;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_CALLBACK_USERNAME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_CLEAR_CALLBACK;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_DEFAULT_RUNTIME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_EVENT_HISTORY_LOOKBACK_HOURS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_FORECAST_UNITS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_HOSE_SUMMARY_LOOKAHEAD_DAYS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_HOSE_SUMMARY_LOOKBACK_DAYS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_POLLING_INTERVAL;
import static org.openhab.binding.rachio.internal.RachioConfiguration.ConfigurationSource.CLOUD_THING;
import static org.openhab.binding.rachio.internal.RachioConfiguration.ConfigurationSource.DEFAULT;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;

/**
 * Tests robust Rachio binding configuration parsing.
 *
 * @author openHAB Contributors - Initial contribution
 */
class RachioConfigurationTest {
    private static final List<String> CLOUD_THING_PARAMETERS = List.of(PARAM_APIKEY, PARAM_POLLING_INTERVAL,
            PARAM_DEFAULT_RUNTIME, PARAM_EVENT_HISTORY_LOOKBACK_HOURS, PARAM_FORECAST_UNITS,
            PARAM_HOSE_SUMMARY_LOOKBACK_DAYS, PARAM_HOSE_SUMMARY_LOOKAHEAD_DAYS, PARAM_CALLBACK_URL,
            PARAM_CALLBACK_USERNAME, PARAM_CALLBACK_PASSWORD, PARAM_CLEAR_CALLBACK);

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
    void effectiveConfigurationUsesCloudThingValues() {
        RachioConfiguration.ResolvedConfiguration resolvedConfiguration = RachioConfiguration
                .resolveEffectiveConfig(Map.of(PARAM_FORECAST_UNITS, "US", PARAM_HOSE_SUMMARY_LOOKAHEAD_DAYS, "9",
                        PARAM_EVENT_HISTORY_LOOKBACK_HOURS, "6", PARAM_CALLBACK_URL,
                        "https://cloud.example.org/rachio/webhook"));
        RachioConfiguration configuration = resolvedConfiguration.configuration();

        assertThat(configuration.forecastUnits, is("US"));
        assertThat(configuration.hoseSummaryLookaheadDays, is(9));
        assertThat(configuration.eventHistoryLookbackHours, is(6));
        assertThat(configuration.callbackUrl, is("https://cloud.example.org/rachio/webhook"));
        assertThat(resolvedConfiguration.source(PARAM_FORECAST_UNITS), is(CLOUD_THING));
        assertThat(resolvedConfiguration.source(PARAM_HOSE_SUMMARY_LOOKAHEAD_DAYS), is(CLOUD_THING));
    }

    @Test
    void effectiveConfigurationUsesBuiltInDefaultsWhenNoCloudValueExists() {
        RachioConfiguration.ResolvedConfiguration resolvedConfiguration = RachioConfiguration
                .resolveEffectiveConfig(Map.of());
        RachioConfiguration configuration = resolvedConfiguration.configuration();

        assertThat(configuration.forecastUnits, is(DEFAULT_FORECAST_UNITS));
        assertThat(configuration.hoseSummaryLookbackDays, is(DEFAULT_HOSE_SUMMARY_LOOKBACK_DAYS));
        assertThat(configuration.hoseSummaryLookaheadDays, is(DEFAULT_HOSE_SUMMARY_LOOKAHEAD_DAYS));
        assertThat(configuration.eventHistoryLookbackHours, is(DEFAULT_EVENT_HISTORY_LOOKBACK_HOURS));
        assertThat(configuration.callbackUrl, is(""));
        assertThat(resolvedConfiguration.source(PARAM_FORECAST_UNITS), is(DEFAULT));
        assertThat(resolvedConfiguration.source(PARAM_HOSE_SUMMARY_LOOKAHEAD_DAYS), is(DEFAULT));
    }

    @Test
    void addonXmlDoesNotExposeBindingLevelConfigParameters() throws IOException, URISyntaxException {
        String xml = readResource("/OH-INF/addon/addon.xml");

        assertThat(xml, not(containsString("<config-description>")));
        for (String parameter : CLOUD_THING_PARAMETERS) {
            assertThat(xml, not(containsString("name=\"" + parameter + "\"")));
        }
    }

    @Test
    void cloudThingXmlExposesRequiredConfigParameters() throws IOException, URISyntaxException {
        String xml = readResource("/OH-INF/thing/cloud.xml");

        for (String parameter : CLOUD_THING_PARAMETERS) {
            assertThat(xml, containsString("<parameter name=\"" + parameter + "\""));
        }
    }

    private String readResource(String resourcePath) throws IOException, URISyntaxException {
        return Files.readString(Path.of(Objects.requireNonNull(getClass().getResource(resourcePath)).toURI()),
                StandardCharsets.UTF_8);
    }
}
