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

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RachioConfiguration} contains the binding configuration and default values. The field names represent the
 * configuration names, do not rename them if you don't intend to break the configuration interface.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class RachioConfiguration {
    private static final String REDACTED = "[redacted]";
    private static final String REDACTED_INVALID_URL = "[redacted invalid URL]";
    private static final List<String> CONFIGURATION_PARAMETERS = List.of(PARAM_APIKEY, PARAM_POLLING_INTERVAL,
            PARAM_DEF_RUNTIME, PARAM_CALLBACK_URL, PARAM_CALLBACK_USERNAME, PARAM_CALLBACK_PASSWORD,
            PARAM_CLEAR_CALLBACK, PARAM_EVENT_HISTORY_LOOKBACK_HOURS, PARAM_FORECAST_UNITS,
            PARAM_HOSE_SUMMARY_LOOKBACK_DAYS, PARAM_HOSE_SUMMARY_LOOKAHEAD_DAYS);

    private final Logger logger = LoggerFactory.getLogger(RachioConfiguration.class);
    private final Set<String> configuredParameters = new HashSet<>();

    public String apikey = "";
    public int pollingInterval = DEFAULT_POLLING_INTERVAL_SEC;
    public int defaultRuntime = DEFAULT_ZONE_RUNTIME_SEC;
    public String callbackUrl = "";
    public String callbackUsername = "";
    public String callbackPassword = "";
    public Boolean clearAllCallbacks = false;
    public int eventHistoryLookbackHours = DEFAULT_EVENT_HISTORY_LOOKBACK_HOURS;
    public String forecastUnits = DEFAULT_FORECAST_UNITS;
    public int hoseSummaryLookbackDays = DEFAULT_HOSE_SUMMARY_LOOKBACK_DAYS;
    public int hoseSummaryLookaheadDays = DEFAULT_HOSE_SUMMARY_LOOKAHEAD_DAYS;

    public RachioConfiguration() {
    }

    public RachioConfiguration(RachioConfiguration source) {
        copyValuesFrom(source);
        configuredParameters.addAll(source.configuredParameters);
    }

    public enum ConfigurationSource {
        CLOUD_THING("Cloud Thing"),
        DEPRECATED_BINDING_FALLBACK("deprecated binding fallback"),
        DEFAULT("built-in default");

        private final String label;

        ConfigurationSource(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    public static class ResolvedConfiguration {
        private final RachioConfiguration configuration;
        private final Map<String, ConfigurationSource> sources;

        private ResolvedConfiguration(RachioConfiguration configuration, Map<String, ConfigurationSource> sources) {
            this.configuration = configuration;
            this.sources = Map.copyOf(sources);
        }

        public RachioConfiguration configuration() {
            return configuration;
        }

        public ConfigurationSource source(String parameterName) {
            String canonicalParameterName = canonicalParameterName(parameterName);
            if (canonicalParameterName == null) {
                return ConfigurationSource.DEFAULT;
            }
            return sources.getOrDefault(canonicalParameterName, ConfigurationSource.DEFAULT);
        }
    }

    public static ResolvedConfiguration resolveEffectiveConfig(RachioConfiguration bindingFallback,
            @Nullable Map<String, @Nullable Object> cloudThingConfig) {
        RachioConfiguration effectiveConfig = new RachioConfiguration();
        Map<String, ConfigurationSource> sources = new HashMap<>();
        for (String parameterName : CONFIGURATION_PARAMETERS) {
            sources.put(parameterName, ConfigurationSource.DEFAULT);
        }

        effectiveConfig.applyConfiguredValuesFrom(bindingFallback);
        for (String parameterName : CONFIGURATION_PARAMETERS) {
            if (bindingFallback.hasConfiguredValue(parameterName)) {
                sources.put(parameterName, ConfigurationSource.DEPRECATED_BINDING_FALLBACK);
            }
        }

        effectiveConfig.updateConfig(cloudThingConfig);
        if (cloudThingConfig != null) {
            for (Map.Entry<String, @Nullable Object> entry : cloudThingConfig.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                String parameterName = canonicalParameterName(entry.getKey());
                if (parameterName != null) {
                    sources.put(parameterName, ConfigurationSource.CLOUD_THING);
                }
            }
        }

        return new ResolvedConfiguration(effectiveConfig, sources);
    }

    public void updateConfig(@Nullable Map<String, @Nullable Object> config) {
        if (config == null) {
            return;
        }
        for (Map.Entry<String, @Nullable Object> ce : config.entrySet()) {
            String key = ce.getKey();
            if (key.equalsIgnoreCase("component.name") || key.equalsIgnoreCase("component.id")) {
                continue;
            }
            if (ce.getValue() == null) {
                // no value set
                continue;
            }
            Object configValue = ce.getValue();
            String value = configValue != null ? configValue.toString() : "";

            if (key.equalsIgnoreCase("service.pid")) {
                logger.debug("Rachio: Binding configuration:");
            }
            logger.debug("  {}={}", key, sanitizeValueForLogging(key, value));

            String parameterName = canonicalParameterName(key);
            if (parameterName == null) {
                continue;
            }

            configuredParameters.add(parameterName);
            if (parameterName.equals(PARAM_APIKEY)) {
                apikey = value;
            } else if (parameterName.equals(PARAM_POLLING_INTERVAL)) {
                this.pollingInterval = Integer.parseInt(value);
            } else if (parameterName.equals(PARAM_DEF_RUNTIME)) {
                this.defaultRuntime = Integer.parseInt(value);
            } else if (parameterName.equals(PARAM_CALLBACK_URL)) {
                this.callbackUrl = value;
            } else if (parameterName.equals(PARAM_CALLBACK_USERNAME)) {
                this.callbackUsername = value;
            } else if (parameterName.equals(PARAM_CALLBACK_PASSWORD)) {
                this.callbackPassword = value;
            } else if (parameterName.equals(PARAM_CLEAR_CALLBACK)) {
                String str = value;
                this.clearAllCallbacks = str.toLowerCase().equals("true");
            } else if (parameterName.equals(PARAM_EVENT_HISTORY_LOOKBACK_HOURS)) {
                this.eventHistoryLookbackHours = parseEventHistoryLookbackHours(value);
            } else if (parameterName.equals(PARAM_FORECAST_UNITS)) {
                this.forecastUnits = parseForecastUnits(value);
            } else if (parameterName.equals(PARAM_HOSE_SUMMARY_LOOKBACK_DAYS)) {
                this.hoseSummaryLookbackDays = parseSummaryWindowDays(value, DEFAULT_HOSE_SUMMARY_LOOKBACK_DAYS,
                        PARAM_HOSE_SUMMARY_LOOKBACK_DAYS);
            } else if (parameterName.equals(PARAM_HOSE_SUMMARY_LOOKAHEAD_DAYS)) {
                this.hoseSummaryLookaheadDays = parseSummaryWindowDays(value, DEFAULT_HOSE_SUMMARY_LOOKAHEAD_DAYS,
                        PARAM_HOSE_SUMMARY_LOOKAHEAD_DAYS);
            }
        }
    }

    public boolean hasConfiguredValue(String parameterName) {
        String canonicalParameterName = canonicalParameterName(parameterName);
        return canonicalParameterName != null && configuredParameters.contains(canonicalParameterName);
    }

    private void applyConfiguredValuesFrom(RachioConfiguration source) {
        for (String parameterName : CONFIGURATION_PARAMETERS) {
            if (source.hasConfiguredValue(parameterName)) {
                copyValueFrom(source, parameterName);
                configuredParameters.add(parameterName);
            }
        }
    }

    private void copyValuesFrom(RachioConfiguration source) {
        for (String parameterName : CONFIGURATION_PARAMETERS) {
            copyValueFrom(source, parameterName);
        }
    }

    private void copyValueFrom(RachioConfiguration source, String parameterName) {
        switch (parameterName) {
            case PARAM_APIKEY:
                apikey = source.apikey;
                break;
            case PARAM_POLLING_INTERVAL:
                pollingInterval = source.pollingInterval;
                break;
            case PARAM_DEF_RUNTIME:
                defaultRuntime = source.defaultRuntime;
                break;
            case PARAM_CALLBACK_URL:
                callbackUrl = source.callbackUrl;
                break;
            case PARAM_CALLBACK_USERNAME:
                callbackUsername = source.callbackUsername;
                break;
            case PARAM_CALLBACK_PASSWORD:
                callbackPassword = source.callbackPassword;
                break;
            case PARAM_CLEAR_CALLBACK:
                clearAllCallbacks = source.clearAllCallbacks;
                break;
            case PARAM_EVENT_HISTORY_LOOKBACK_HOURS:
                eventHistoryLookbackHours = source.eventHistoryLookbackHours;
                break;
            case PARAM_FORECAST_UNITS:
                forecastUnits = source.forecastUnits;
                break;
            case PARAM_HOSE_SUMMARY_LOOKBACK_DAYS:
                hoseSummaryLookbackDays = source.hoseSummaryLookbackDays;
                break;
            case PARAM_HOSE_SUMMARY_LOOKAHEAD_DAYS:
                hoseSummaryLookaheadDays = source.hoseSummaryLookaheadDays;
                break;
            default:
                break;
        }
    }

    private static @Nullable String canonicalParameterName(String key) {
        for (String parameterName : CONFIGURATION_PARAMETERS) {
            if (parameterName.equalsIgnoreCase(key)) {
                return parameterName;
            }
        }
        return null;
    }

    private int parseEventHistoryLookbackHours(String value) {
        try {
            int lookbackHours = Integer.parseInt(value.trim());
            if (lookbackHours < 0) {
                logger.warn("Invalid Rachio eventHistoryLookbackHours '{}'; using 0 to disable event history polling.",
                        value);
                return 0;
            }
            if (lookbackHours > MAX_EVENT_HISTORY_LOOKBACK_HOURS) {
                logger.warn("Rachio eventHistoryLookbackHours '{}' is too large; using maximum {}.", value,
                        MAX_EVENT_HISTORY_LOOKBACK_HOURS);
                return MAX_EVENT_HISTORY_LOOKBACK_HOURS;
            }
            return lookbackHours;
        } catch (NumberFormatException e) {
            logger.warn("Invalid Rachio eventHistoryLookbackHours '{}'; using default {}.", value,
                    DEFAULT_EVENT_HISTORY_LOOKBACK_HOURS);
            return DEFAULT_EVENT_HISTORY_LOOKBACK_HOURS;
        }
    }

    private String parseForecastUnits(String value) {
        String normalizedValue = value.trim().toUpperCase();
        if ("METRIC".equals(normalizedValue) || "US".equals(normalizedValue)) {
            return normalizedValue;
        }
        logger.warn("Invalid Rachio forecastUnits '{}'; using default {}.", value, DEFAULT_FORECAST_UNITS);
        return DEFAULT_FORECAST_UNITS;
    }

    private int parseSummaryWindowDays(String value, int defaultValue, String parameterName) {
        try {
            int days = Integer.parseInt(value.trim());
            if (days < 0) {
                logger.warn("Invalid Rachio {} '{}'; using 0.", parameterName, value);
                return 0;
            }
            if (days > MAX_HOSE_SUMMARY_WINDOW_DAYS) {
                logger.warn("Rachio {} '{}' is too large; using maximum {}.", parameterName, value,
                        MAX_HOSE_SUMMARY_WINDOW_DAYS);
                return MAX_HOSE_SUMMARY_WINDOW_DAYS;
            }
            return days;
        } catch (NumberFormatException e) {
            logger.warn("Invalid Rachio {} '{}'; using default {}.", parameterName, value, defaultValue);
            return defaultValue;
        }
    }

    private String sanitizeValueForLogging(String key, String value) {
        if (key.equalsIgnoreCase(PARAM_APIKEY)) {
            return REDACTED;
        }
        if (key.equalsIgnoreCase(PARAM_CALLBACK_USERNAME)) {
            return REDACTED;
        }
        if (key.equalsIgnoreCase(PARAM_CALLBACK_PASSWORD)) {
            return REDACTED;
        }
        if (key.equalsIgnoreCase(PARAM_CALLBACK_URL)) {
            return sanitizeCallbackUrlForLogging(value);
        }
        return value;
    }

    private String sanitizeCallbackUrlForLogging(String value) {
        if (value.isBlank()) {
            return value;
        }

        try {
            URI uri = new URI(value);
            String rawUserInfo = uri.getRawUserInfo();
            String rawAuthority = uri.getRawAuthority();
            if (rawUserInfo == null) {
                if (rawAuthority != null && rawAuthority.contains("@")) {
                    return REDACTED_INVALID_URL;
                }
                return value;
            }

            if (rawAuthority == null) {
                return REDACTED_INVALID_URL;
            }

            String userInfoPrefix = rawUserInfo + "@";
            if (!rawAuthority.startsWith(userInfoPrefix)
                    || rawAuthority.indexOf('@') != rawAuthority.lastIndexOf('@')) {
                return REDACTED_INVALID_URL;
            }

            return buildCallbackUrlForLogging(uri, "***:***@" + rawAuthority.substring(userInfoPrefix.length()));
        } catch (URISyntaxException e) {
            return REDACTED_INVALID_URL;
        }
    }

    private String buildCallbackUrlForLogging(URI uri, String sanitizedAuthority) {
        StringBuilder url = new StringBuilder();
        String scheme = uri.getScheme();
        if (scheme != null) {
            url.append(scheme).append(":");
        }
        url.append("//").append(sanitizedAuthority);

        String path = uri.getRawPath();
        if (path != null) {
            url.append(path);
        }
        String query = uri.getRawQuery();
        if (query != null) {
            url.append("?").append(query);
        }
        String fragment = uri.getRawFragment();
        if (fragment != null) {
            url.append("#").append(fragment);
        }
        return url.toString();
    }
}
