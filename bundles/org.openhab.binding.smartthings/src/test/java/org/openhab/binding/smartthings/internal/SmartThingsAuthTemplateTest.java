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
package org.openhab.binding.smartthings.internal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests for bundled SmartThings authorization templates.
 */
@NonNullByDefault
class SmartThingsAuthTemplateTest {

    static Stream<String> authTemplates() {
        return Stream.of("index-oauth.html", "step1.html", "confirmation.html", "error.html");
    }

    static Stream<String> authAssets() {
        return Stream.of("/img/auth.css", "/img/openhab-logo.svg", "/img/smartthings-logo.svg");
    }

    @ParameterizedTest
    @MethodSource("authTemplates")
    void authTemplatesUseModernOpenHABShellWithOfficialLogo(String templateName) throws IOException {
        String template = readTemplate(templateName);

        assertTrue(template.contains("class=\"auth-page\""));
        assertTrue(template.contains("class=\"auth-card\""));
        assertTrue(template.contains("class=\"openhab-logo\""));
        assertTrue(template.contains("<base href=\"${assetBaseUri}/\">"));
        assertTrue(template.contains("href=\"img/auth.css\""));
        assertTrue(template.contains("src=\"img/openhab-logo.svg\""));
        assertTrue(template.contains("src=\"img/smartthings-logo.svg\""));
        assertFalse(template.contains(">" + "o" + "h" + "<"));
    }

    @ParameterizedTest
    @MethodSource("authAssets")
    void authTemplateAssetsExist(String assetPath) throws IOException {
        try (InputStream stream = getClass().getResourceAsStream(assetPath)) {
            assertNotNull(stream);
            assertTrue(stream.readAllBytes().length > 0);
        }
    }

    @Test
    void authIndexTemplateUsesConditionalCallbackInfo() throws IOException {
        String template = readTemplate("index-oauth.html");

        assertTrue(template.contains("${callbackInfo}"));
        assertFalse(template.contains("${callBackUri}"));
    }

    @Test
    void authIndexTemplateShowsLocalhostRedirectInfo() throws IOException {
        String template = readTemplate("index-oauth.html");

        assertTrue(template.contains("${localhostRedirectInfo}"));
    }

    private String readTemplate(String templateName) throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("/templates/" + templateName)) {
            assertNotNull(stream);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
