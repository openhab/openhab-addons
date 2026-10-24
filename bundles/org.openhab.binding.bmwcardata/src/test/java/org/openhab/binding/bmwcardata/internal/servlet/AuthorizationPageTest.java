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
package org.openhab.binding.bmwcardata.internal.servlet;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bmwcardata.internal.api.BMWCarDataAuthClient.DeviceAuthorization;

/**
 * Tests for {@link AuthorizationPage} and {@link QrSvg}.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
class AuthorizationPageTest {

    private static final String UID = "bmwcardata:account:a";
    private static final DeviceAuthorization AUTHORIZATION = new DeviceAuthorization("device-code", "mJimW4Gm",
            "https://customer.bmwgroup.com/oneid/link", Instant.parse("2026-10-23T12:55:00Z"), 5, "verifier");

    // the binding's default translations, relative to the bundle, the working directory of the tests
    private static final Properties TRANSLATIONS = new Properties();

    @BeforeAll
    static void loadTranslations() throws IOException {
        try (Reader reader = Files.newBufferedReader(Path.of("src/main/resources/OH-INF/i18n/bmwcardata.properties"),
                StandardCharsets.UTF_8)) {
            TRANSLATIONS.load(reader);
        }
    }

    /**
     * Translates like openHAB's translation provider: the default translation, formatted when there are arguments.
     */
    private static String translate(String key, Object... arguments) {
        String text = TRANSLATIONS.getProperty(key);
        assertNotNull(text, "missing translation " + key);
        return arguments.length == 0 ? text : MessageFormat.format(text, arguments);
    }

    private static String render(List<AuthorizationPage.Account> accounts, Locale locale) {
        return AuthorizationPage.render(accounts, ZoneOffset.ofHours(2), locale, AuthorizationPageTest::translate);
    }

    @Test
    void everyTextIsTranslated() {
        for (String key : AuthorizationPage.KEYS) {
            assertNotNull(TRANSLATIONS.getProperty(key), "missing translation " + key);
        }
    }

    @Test
    void showsLinkQrCodeAndCodeOfAPendingAccount() {
        String page = render(List.of(new AuthorizationPage.Account("My BMW", UID, false, AUTHORIZATION,
                "OFFLINE (CONFIGURATION_PENDING)")), Locale.ENGLISH);
        assertTrue(page.contains("href=\"https://customer.bmwgroup.com/oneid/link?user_code=mJimW4Gm\""));
        // opens in the same window, which the user closes after confirming
        assertFalse(page.contains("target="));
        assertTrue(page.contains("rel=\"noreferrer\""));
        assertTrue(page.contains("<p class=\"code\">mJimW4Gm</p>"));
        assertTrue(page.contains("<svg"));
        assertTrue(page.contains("valid until 2:55\u202FPM") || page.contains("valid until 2:55 PM"), page);
        assertTrue(page.contains("<html lang=\"en\">"));
        assertTrue(page.contains("http-equiv=\"refresh\""));
        // the device code and verifier are secrets
        assertFalse(page.contains("device-code"));
        assertFalse(page.contains("verifier"));
    }

    @Test
    void authorizedAccountsNeedNoRefresh() {
        String page = render(List.of(new AuthorizationPage.Account("My BMW", UID, true, null, "ONLINE")),
                Locale.ENGLISH);
        assertTrue(page.contains("Authorized"));
        assertFalse(page.contains("http-equiv=\"refresh\""));
        assertFalse(page.contains("<svg"));
    }

    @Test
    void showsStatusWhileWaitingForACode() {
        String page = render(List.of(new AuthorizationPage.Account("My BMW", UID, false, null,
                "OFFLINE (CONFIGURATION_ERROR)")), Locale.ENGLISH);
        assertTrue(page.contains("OFFLINE (CONFIGURATION_ERROR)"));
        assertTrue(page.contains("http-equiv=\"refresh\""));
    }

    @Test
    void escapesLabels() {
        String page = render(List.of(new AuthorizationPage.Account("<b>BMW</b>", UID, true, null, "ONLINE")),
                Locale.ENGLISH);
        assertFalse(page.contains("<b>"));
        assertTrue(page.contains("&lt;b&gt;BMW&lt;/b&gt;"));
        assertEquals("&amp;&quot;&#39;", AuthorizationPage.escape("&\"'"));
    }

    @Test
    void qrCodeHasFinderPatternsAndQuietZone() {
        String svg = QrSvg.toSvg("https://customer.bmwgroup.com/oneid/link?user_code=mJimW4Gm", "QR code");
        Matcher viewBox = Pattern.compile("viewBox=\"0 0 (\\d+) (\\d+)\"").matcher(svg);
        assertTrue(viewBox.find());
        int size = Integer.parseInt(viewBox.group(1));
        // versions grow in steps of 4 modules from 21, plus the quiet zone on both sides
        assertEquals(0, (size - 2 * QrSvg.BORDER - 21) % 4);
        // the top left finder pattern starts right after the quiet zone; nothing is drawn inside the quiet zone
        assertTrue(svg.contains("M" + QrSvg.BORDER + "," + QrSvg.BORDER + "h1v1h-1z"));
        assertFalse(svg.contains("M" + (QrSvg.BORDER - 1) + ","));
        assertTrue(svg.contains("fill=\"#fff\""));
    }

    @Test
    void usesTheLanguageAndTimeFormatOfTheLocale() {
        String page = render(List.of(new AuthorizationPage.Account("My BMW", UID, false, AUTHORIZATION,
                "OFFLINE (CONFIGURATION_PENDING)")), Locale.forLanguageTag("nl-BE"));
        assertTrue(page.contains("<html lang=\"nl-BE\">"));
        // the time in the 24 hour format of the locale; the text falls back to English without a Dutch translation
        assertTrue(page.contains("valid until 14:55"), page);
    }

    @Test
    void escapesTranslations() {
        String page = AuthorizationPage.render(List.of(), ZoneOffset.UTC, Locale.ENGLISH,
                (key, arguments) -> "<b>" + key + "</b>");
        assertFalse(page.contains("<b>"));
        assertTrue(page.contains("&lt;b&gt;authorization-page.title&lt;/b&gt;"));
    }
}
