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
package org.openhab.binding.caldav.internal.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.caldav.internal.client.CalDavXml;
import org.openhab.binding.caldav.internal.logic.CalendarWindow;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.config.core.ConfigDescriptionRegistry;
import org.openhab.core.config.core.ConfigUtil;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.core.internal.validation.ConfigDescriptionValidatorImpl;
import org.openhab.core.config.core.validation.ConfigDescriptionValidator;
import org.openhab.core.config.core.validation.ConfigValidationException;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.w3c.dom.Element;

/**
 * Tests optional account credentials and configuration validation.
 *
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Configuration boundaries and synchronization horizon
 */
@NonNullByDefault
class CalDavConfigurationTest {
    private record IntegerParameter(String name, int minimum, int maximum) {
    }

    private static final List<IntegerParameter> INTEGER_PARAMETERS = List.of(
            new IntegerParameter("refreshInterval", 30, Integer.MAX_VALUE),
            new IntegerParameter("requestTimeout", 1, 300), new IntegerParameter("maxPastDays", 0, 36500),
            new IntegerParameter("maxFutureDays", 1, 36500),
            new IntegerParameter("rangeStartOffset", Integer.MIN_VALUE, Integer.MAX_VALUE),
            new IntegerParameter("rangeEndOffset", Integer.MIN_VALUE, Integer.MAX_VALUE),
            new IntegerParameter("maxEvents", 1, 50000));

    @Test
    void acceptsPathOnlyCalendarConfiguration() {
        AccountConfiguration account = new AccountConfiguration();
        account.url = "https://caldav.example.test/caldav/";
        for (String path : List.of("family/", "https://caldav.example.test/caldav/family/")) {
            CalendarConfiguration calendar = new CalendarConfiguration();
            calendar.path = path;
            assertEquals(URI.create("https://caldav.example.test/caldav/family/"),
                    CalDavConfiguration.validate(calendar, account));
        }
    }

    @Test
    void rejectsBlankCalendarPath() {
        AccountConfiguration account = new AccountConfiguration();
        account.url = "https://caldav.example.test/caldav/";
        for (String path : List.of("", " ")) {
            CalendarConfiguration calendar = new CalendarConfiguration();
            calendar.path = path;
            assertThrows(IllegalArgumentException.class, () -> CalDavConfiguration.validate(calendar, account));
        }
    }

    @Test
    void acceptsAnonymousAndCompleteCredentialsForEveryAuthType() {
        for (String authType : List.of("AUTO", "BASIC", "DIGEST")) {
            AccountConfiguration config = new AccountConfiguration();
            config.url = "https://caldav.example.test/";
            config.authType = authType;
            assertDoesNotThrow(() -> CalDavConfiguration.validate(config));
            config.username = " ";
            config.password = " ";
            assertDoesNotThrow(() -> CalDavConfiguration.validate(config));
            config.username = "user";
            config.password = "secret";
            assertDoesNotThrow(() -> CalDavConfiguration.validate(config));
        }
    }

    @Test
    void rejectsIncompleteCredentials() {
        for (String blank : List.of("", " ")) {
            AccountConfiguration config = new AccountConfiguration();
            config.url = "https://caldav.example.test/";
            config.username = "user";
            config.password = blank;
            assertThrows(IllegalArgumentException.class, () -> CalDavConfiguration.validate(config));
            config.username = blank;
            config.password = "secret";
            assertThrows(IllegalArgumentException.class, () -> CalDavConfiguration.validate(config));
        }
    }

    @Test
    void anonymousAccessStillRequiresValidAccountSettings() {
        AccountConfiguration config = new AccountConfiguration();
        config.url = "https://caldav.example.test/";
        config.authType = "NONE";
        assertThrows(IllegalArgumentException.class, () -> CalDavConfiguration.validate(config));
        config.authType = "AUTO";
        config.requestTimeout = 0;
        assertThrows(IllegalArgumentException.class, () -> CalDavConfiguration.validate(config));
    }

    @Test
    void acceptsAccountNumericBoundaries() {
        List<Consumer<AccountConfiguration>> settings = List.of(c -> c.requestTimeout = 1, c -> c.requestTimeout = 300,
                c -> c.refreshInterval = 30, c -> c.refreshInterval = Integer.MAX_VALUE, c -> c.maxPastDays = 0,
                c -> c.maxPastDays = 36500, c -> c.maxFutureDays = 1, c -> c.maxFutureDays = 36500);
        for (var setting : settings) {
            AccountConfiguration config = account();
            setting.accept(config);
            assertDoesNotThrow(() -> CalDavConfiguration.validate(config));
        }
    }

    @Test
    void rejectsAccountValuesOutsideNumericBoundaries() {
        List<Consumer<AccountConfiguration>> settings = List.of(c -> c.requestTimeout = 0, c -> c.requestTimeout = 301,
                c -> c.refreshInterval = 29, c -> c.maxPastDays = -1, c -> c.maxPastDays = 36501,
                c -> c.maxFutureDays = 0, c -> c.maxFutureDays = 36501);
        for (var setting : settings) {
            AccountConfiguration config = account();
            setting.accept(config);
            assertThrows(IllegalArgumentException.class, () -> CalDavConfiguration.validate(config));
        }
    }

    @Test
    void coreValidatorAcceptsExactIntegerMetadataBounds() throws Exception {
        for (IntegerParameter setting : INTEGER_PARAMETERS) {
            String parameter = setting.name();
            var validator = metadataValidator(parameter);
            for (int value : new int[] { setting.minimum(), setting.maximum() }) {
                BigDecimal raw = BigDecimal.valueOf(value);
                assertDoesNotThrow(() -> validator.validate(parameter, raw), parameter + "=" + raw);
                Configuration configuration = new Configuration(Map.of(parameter, raw));
                assertEquals(value, configuredInteger(configuration, parameter));
            }
        }
    }

    @Test
    void coreValidatorRejectsRawNumbersOutsideMetadataBounds() throws Exception {
        for (IntegerParameter setting : INTEGER_PARAMETERS) {
            String parameter = setting.name();
            var validator = metadataValidator(parameter);
            for (BigDecimal raw : List.of(BigDecimal.valueOf((long) setting.minimum() - 1),
                    BigDecimal.valueOf((long) setting.maximum() + 1))) {
                ConfigValidationException failure = assertThrows(ConfigValidationException.class,
                        () -> validator.validate(parameter, raw), parameter + "=" + raw);
                assertTrue(failure.getValidationMessages().containsKey(parameter));
            }
        }
    }

    @Test
    void integerGuardRejectsFractionsAfterCoreNormalization() throws Exception {
        for (IntegerParameter setting : INTEGER_PARAMETERS) {
            BigDecimal fraction = BigDecimal.valueOf(setting.minimum()).add(new BigDecimal("0.5"));
            var metadata = metadataValidator(setting.name());
            Configuration raw = new Configuration(Map.of(setting.name(), fraction));
            Configuration normalized = new Configuration(
                    ConfigUtil.normalizeTypes(raw.getProperties(), List.of(metadata.description())));
            assertThrows(IllegalArgumentException.class,
                    () -> CalDavConfiguration.validateIntegerValues(normalized, setting.name()), setting.name());
        }
    }

    @Test
    void integerGuardRejectsOverflowFractionalStringsAndNonNumericTypes() {
        for (IntegerParameter setting : INTEGER_PARAMETERS) {
            for (Object value : List.of(BigDecimal.valueOf((long) Integer.MIN_VALUE - 1),
                    BigDecimal.valueOf((long) Integer.MAX_VALUE + 1), "1.5", "3E2", true)) {
                Configuration configuration = new Configuration(Map.of(setting.name(), value));
                assertThrows(IllegalArgumentException.class,
                        () -> CalDavConfiguration.validateIntegerValues(configuration, setting.name()),
                        setting.name() + "=" + value);
            }
        }
    }

    @Test
    void integerGuardAcceptsExactValuesAndPreservesUnrelatedSettingsAndDefaults() {
        for (IntegerParameter setting : INTEGER_PARAMETERS) {
            for (int value : new int[] { setting.minimum(), setting.maximum() }) {
                for (Object raw : List.of(BigDecimal.valueOf(value).setScale(1), Integer.toString(value))) {
                    Configuration configuration = new Configuration(Map.of(setting.name(), raw));
                    assertDoesNotThrow(() -> CalDavConfiguration.validateIntegerValues(configuration, setting.name()));
                    assertEquals(value, configuredInteger(configuration, setting.name()));
                }
            }
        }
        for (String raw : List.of("300", "+300", "00300")) {
            Configuration configuration = new Configuration(Map.of("refreshInterval", raw));
            assertDoesNotThrow(() -> CalDavConfiguration.validateIntegerValues(configuration, "refreshInterval"));
            assertEquals(300, configuredInteger(configuration, "refreshInterval"));
        }
        Configuration defaults = new Configuration(Map.of("rangeAnchor", "1.5", "includeCancelled", false));
        assertDoesNotThrow(() -> CalDavConfiguration.validateIntegerValues(defaults, "rangeStartOffset",
                "rangeEndOffset", "maxEvents"));
        assertEquals("1.5", defaults.get("rangeAnchor"));
        assertEquals(500, configuredInteger(defaults, "maxEvents"));
    }

    @Test
    void metadataRejectsOverflowBeforeCoreDtoConversionLosesUpperBits() throws Exception {
        BigDecimal refresh = new BigDecimal("4294967596");
        Configuration rawAccount = new Configuration(
                Map.of("url", "https://caldav.example.test/", "refreshInterval", refresh));
        AccountConfiguration account = Objects.requireNonNull(rawAccount.as(AccountConfiguration.class));
        assertEquals(300, account.refreshInterval);
        assertDoesNotThrow(() -> CalDavConfiguration.validate(account));
        assertThrows(ConfigValidationException.class,
                () -> metadataValidator("refreshInterval").validate("refreshInterval", refresh));

        Map<String, BigDecimal> offsets = Map.of("rangeStartOffset", new BigDecimal("4294967296"), "rangeEndOffset",
                new BigDecimal("4294967302"));
        Configuration rawCalendar = new Configuration();
        rawCalendar.put("path", "family/");
        offsets.forEach(rawCalendar::put);
        CalendarConfiguration calendar = Objects.requireNonNull(rawCalendar.as(CalendarConfiguration.class));
        assertEquals(0, calendar.rangeStartOffset);
        assertEquals(6, calendar.rangeEndOffset);
        assertDoesNotThrow(() -> CalDavConfiguration.validate(calendar, account));
        for (var offset : offsets.entrySet()) {
            assertThrows(ConfigValidationException.class,
                    () -> metadataValidator(offset.getKey()).validate(offset.getKey(), offset.getValue()));
        }
    }

    private record MetadataValidator(ConfigDescriptionValidator validator, URI uri, ConfigDescription description) {
        void validate(String parameter, BigDecimal value) {
            validator.validate(Map.of(parameter, value), uri);
        }
    }

    private static int configuredInteger(Configuration configuration, String parameter) {
        return switch (parameter) {
            case "refreshInterval" ->
                Objects.requireNonNull(configuration.as(AccountConfiguration.class)).refreshInterval;
            case "requestTimeout" ->
                Objects.requireNonNull(configuration.as(AccountConfiguration.class)).requestTimeout;
            case "maxPastDays" -> Objects.requireNonNull(configuration.as(AccountConfiguration.class)).maxPastDays;
            case "maxFutureDays" -> Objects.requireNonNull(configuration.as(AccountConfiguration.class)).maxFutureDays;
            case "rangeStartOffset" ->
                Objects.requireNonNull(configuration.as(CalendarConfiguration.class)).rangeStartOffset;
            case "rangeEndOffset" ->
                Objects.requireNonNull(configuration.as(CalendarConfiguration.class)).rangeEndOffset;
            case "maxEvents" -> Objects.requireNonNull(configuration.as(CalendarConfiguration.class)).maxEvents;
            default -> throw new IllegalArgumentException("Unexpected numeric parameter: " + parameter);
        };
    }

    private static MetadataValidator metadataValidator(String parameter) throws Exception {
        try (var input = Objects.requireNonNull(
                CalDavConfigurationTest.class.getResourceAsStream("/OH-INF/thing/caldav-thing-types.xml"))) {
            var document = CalDavXml.parse(new String(input.readAllBytes(), StandardCharsets.UTF_8));
            var parameters = document.getElementsByTagName("parameter");
            for (int index = 0; index < parameters.getLength(); index++) {
                Element element = (Element) parameters.item(index);
                if (!parameter.equals(element.getAttribute("name"))) {
                    continue;
                }
                var builder = ConfigDescriptionParameterBuilder.create(parameter, ConfigDescriptionParameter.Type
                        .valueOf(element.getAttribute("type").toUpperCase(java.util.Locale.ROOT)));
                if (element.hasAttribute("min")) {
                    builder.withMinimum(new BigDecimal(element.getAttribute("min")));
                }
                if (element.hasAttribute("max")) {
                    builder.withMaximum(new BigDecimal(element.getAttribute("max")));
                }
                URI uri = URI.create("thing-type:caldav:"
                        + (parameter.startsWith("range") || "maxEvents".equals(parameter) ? "calendar" : "account"));
                var description = ConfigDescriptionBuilder.create(uri).withParameters(List.of(builder.build())).build();
                ConfigDescriptionRegistry registry = Objects.requireNonNull(mock(ConfigDescriptionRegistry.class));
                when(registry.getConfigDescription(uri)).thenReturn(description);
                BundleContext context = Objects.requireNonNull(mock(BundleContext.class));
                Bundle bundle = Objects.requireNonNull(mock(Bundle.class));
                when(context.getBundle()).thenReturn(bundle);
                TranslationProvider translations = Objects.requireNonNull(mock(TranslationProvider.class));
                return new MetadataValidator(new ConfigDescriptionValidatorImpl(context, registry, translations), uri,
                        description);
            }
        }
        throw new IllegalArgumentException("Missing metadata parameter: " + parameter);
    }

    @Test
    void acceptsAllSupportedDiscoveryAndSynchronizationModes() {
        for (String discovery : List.of("AUTO", "DIRECT")) {
            for (String sync : List.of("AUTO", "FULL", "ETAG", "SYNC_TOKEN")) {
                AccountConfiguration config = account();
                config.discoveryMode = discovery;
                config.syncMode = sync;
                assertDoesNotThrow(() -> CalDavConfiguration.validate(config));
            }
        }
    }

    @Test
    void rejectsUnsupportedModesAndCalendarWrites() {
        List<Consumer<AccountConfiguration>> settings = List.of(c -> c.authType = "auto", c -> c.authType = "NONE",
                c -> c.discoveryMode = "PRINCIPAL", c -> c.discoveryMode = "", c -> c.syncMode = "UNKNOWN",
                c -> c.syncMode = "", c -> c.readOnly = false);
        for (var setting : settings) {
            AccountConfiguration config = account();
            setting.accept(config);
            assertThrows(IllegalArgumentException.class, () -> CalDavConfiguration.validate(config));
        }
    }

    @Test
    void rejectsInvalidBasicCredentialSyntax() {
        for (String auth : List.of("BASIC", "AUTO")) {
            for (String username : List.of("user:name", "user\nname", "user\0name")) {
                AccountConfiguration config = account();
                config.authType = auth;
                config.username = username;
                config.password = "secret";
                assertThrows(IllegalArgumentException.class, () -> CalDavConfiguration.validate(config));
            }
            for (String password : List.of("secret\nvalue", "secret\tvalue", "secret" + (char) 127)) {
                AccountConfiguration config = account();
                config.authType = auth;
                config.username = "user";
                config.password = password;
                assertThrows(IllegalArgumentException.class, () -> CalDavConfiguration.validate(config));
            }
        }
    }

    @Test
    void acceptsBasicPasswordColonAndDigestUsernameColon() {
        for (String auth : List.of("BASIC", "AUTO", "DIGEST")) {
            AccountConfiguration config = account();
            config.authType = auth;
            config.username = "user";
            config.password = "secret:value";
            assertDoesNotThrow(() -> CalDavConfiguration.validate(config));
        }
        AccountConfiguration digest = account();
        digest.authType = "DIGEST";
        digest.username = "user:name";
        digest.password = "secret";
        assertDoesNotThrow(() -> CalDavConfiguration.validate(digest));
    }

    @Test
    void acceptsCalendarLimitsAndRejectsInvalidSettings() {
        AccountConfiguration account = account();
        for (String anchor : List.of("TODAY", "NOW")) {
            for (int maximum : new int[] { 1, 50000 }) {
                CalendarConfiguration config = calendar();
                config.rangeAnchor = anchor;
                config.maxEvents = maximum;
                assertEquals(URI.create("https://caldav.example.test/family/"),
                        CalDavConfiguration.validate(config, account));
            }
        }
        List<Consumer<CalendarConfiguration>> invalid = List.of(c -> c.maxEvents = 0, c -> c.maxEvents = 50001,
                c -> c.rangeAnchor = "today", c -> c.rangeAnchor = "", c -> c.rangeStartOffset = 7);
        for (var setting : invalid) {
            CalendarConfiguration config = calendar();
            setting.accept(config);
            assertThrows(IllegalArgumentException.class, () -> CalDavConfiguration.validate(config, account));
        }
    }

    @Test
    void horizonUsesCalendarDaysAndAcceptsExactlyCoveredRange() {
        AccountConfiguration account = account();
        account.maxPastDays = 0;
        account.maxFutureDays = 1;
        ZoneId zone = ZoneId.of("Europe/Berlin");
        ZonedDateTime now = ZonedDateTime.parse("2026-03-29T12:00:00+02:00[Europe/Berlin]");
        CalendarWindow horizon = CalDavConfiguration.horizon(account, zone, now);
        assertEquals(ZonedDateTime.parse("2026-03-29T00:00:00+01:00[Europe/Berlin]"), horizon.start());
        assertEquals(ZonedDateTime.parse("2026-03-31T00:00:00+02:00[Europe/Berlin]"), horizon.end());
        assertEquals(Duration.ofHours(47), Duration.between(horizon.start(), horizon.end()));
        assertDoesNotThrow(() -> CalDavConfiguration.validate(horizon, horizon));
        assertThrows(IllegalArgumentException.class, () -> CalDavConfiguration
                .validate(new CalendarWindow(horizon.start().minusNanos(1), horizon.end()), horizon));
        assertThrows(IllegalArgumentException.class, () -> CalDavConfiguration
                .validate(new CalendarWindow(horizon.start(), horizon.end().plusNanos(1)), horizon));
        assertThrows(IllegalArgumentException.class,
                () -> CalDavConfiguration.validate(new CalendarWindow(horizon.start(), horizon.start()), horizon));
        assertThrows(IllegalArgumentException.class,
                () -> CalDavConfiguration.validate(new CalendarWindow(horizon.end(), horizon.start()), horizon));

        CalendarConfiguration calendar = calendar();
        calendar.rangeEndOffset = 1;
        assertDoesNotThrow(() -> CalDavConfiguration.validate(CalendarWindow.from(calendar, zone, now), horizon));
        calendar.rangeStartOffset = -1;
        assertThrows(IllegalArgumentException.class,
                () -> CalDavConfiguration.validate(CalendarWindow.from(calendar, zone, now), horizon));
        calendar.rangeStartOffset = 0;
        calendar.rangeEndOffset = 2;
        assertThrows(IllegalArgumentException.class,
                () -> CalDavConfiguration.validate(CalendarWindow.from(calendar, zone, now), horizon));
    }

    private static AccountConfiguration account() {
        AccountConfiguration config = new AccountConfiguration();
        config.url = "https://caldav.example.test/";
        return config;
    }

    private static CalendarConfiguration calendar() {
        CalendarConfiguration config = new CalendarConfiguration();
        config.path = "family/";
        return config;
    }
}
