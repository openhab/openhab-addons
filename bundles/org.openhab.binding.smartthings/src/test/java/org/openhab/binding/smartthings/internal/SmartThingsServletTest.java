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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.smartthings.internal.handler.SmartThingsBridgeHandler;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.core.i18n.TranslationProvider;
import org.osgi.framework.Bundle;
import org.osgi.service.http.HttpService;

/**
 * Tests for {@link SmartThingsServlet}.
 */
@NonNullByDefault
class SmartThingsServletTest {

    @ParameterizedTest
    @ValueSource(strings = { "/finish", "/smartthings/account", "/smartthings/account/" })
    void oauthCallbackPathsAcceptLocalListenerAndAccountServletRedirects(String path) {
        assertTrue(SmartThingsServlet.isOAuthCallbackPath(path, "/smartthings/account"));
    }

    @Test
    void oauthCallbackPathsRejectOtherAccountAndWebhookCallback() {
        assertFalse(SmartThingsServlet.isOAuthCallbackPath("/smartthings/other", "/smartthings/account"));
        assertFalse(SmartThingsServlet.isOAuthCallbackPath("/smartthings/account/cb", "/smartthings/account"));
    }

    @Test
    void accountServletPathUsesBridgeId() {
        assertEquals("/smartthings/account", SmartThingsServlet.getServletPath("account"));
    }

    @Test
    void accountServletRegistrationUsesUniqueServletName() {
        Dictionary<String, String> firstParams = SmartThingsServlet.createServletParams("/smartthings/first");
        Dictionary<String, String> secondParams = SmartThingsServlet.createServletParams("/smartthings/second");

        assertEquals("org.openhab.binding.smartthings.internal.SmartThingsServlet.smartthings.first",
                firstParams.get("servlet-name"));
        assertEquals("org.openhab.binding.smartthings.internal.SmartThingsServlet.smartthings.second",
                secondParams.get("servlet-name"));
        assertNotEquals(firstParams.get("servlet-name"), secondParams.get("servlet-name"));
    }

    @Test
    void smartThingsRequestErrorsUseGenericSmartThingsTitle() throws SmartThingsException {
        TranslationProvider translationProvider = mock(TranslationProvider.class);
        when(translationProvider.getText(nullable(Bundle.class), eq("smartthing-error"), isNull(), any(Locale.class)))
                .thenReturn("Call to SmartThings failed with error:");

        SmartThingsServlet servlet = new TestSmartThingsServlet(translationProvider);

        assertEquals("<p class='block error'>Call to SmartThings failed with error:<pre>network failure</pre></p>",
                servlet.formatSmartThingsError("network failure"));
    }

    @Test
    void callbackInfoIsHiddenWhenEventCallbackCannotBeRegistered() {
        assertEquals("", SmartThingsServlet.formatCallbackInfo("http://localhost:8080/smartthings/account/cb"));
    }

    @Test
    void callbackInfoIsShownForHttpsEventCallbacks() {
        String callbackInfo = SmartThingsServlet
                .formatCallbackInfo("https://openhab.example.org/smartthings/account/cb");

        assertTrue(callbackInfo.contains("Callback URL"));
        assertTrue(callbackInfo.contains("href=\"https://openhab.example.org/smartthings/account/cb\""));
        assertTrue(callbackInfo.contains("SmartThings uses this URL for event callbacks."));
    }

    @Test
    void callbackInfoWarnsAboutCallbackRequirements() {
        String callbackInfo = SmartThingsServlet.formatCallbackInfo("https://cloud.example.org/smartthings/account/cb",
                true);

        assertTrue(callbackInfo.contains("Callback URL"));
        assertTrue(callbackInfo.contains("SmartThings requires an HTTPS callback URL"));
        assertTrue(callbackInfo.contains("reachable from the internet"));
    }

    @Test
    void callbackInfoWarnsWhenCallbackUrlIsNotHttps() {
        String callbackInfo = SmartThingsServlet.formatCallbackInfo("http://cloud.example.org/smartthings/account/cb",
                true);

        assertTrue(callbackInfo.contains("Callback URL"));
        assertTrue(callbackInfo.contains("href=\"http://cloud.example.org/smartthings/account/cb\""));
        assertTrue(callbackInfo.contains("does not use HTTPS"));
        assertTrue(callbackInfo.contains("reachable from the internet"));
    }

    @Test
    void callbackInfoIsHiddenWhenCallbackUrlIsMissing() {
        String callbackInfo = SmartThingsServlet.formatCallbackInfo("", true);

        assertEquals("", callbackInfo);
    }

    @Test
    void localhostRedirectInfoExplainsRemoteOpenhabHostReplacement() {
        String localhostRedirectInfo = SmartThingsServlet.formatLocalhostRedirectInfo();

        assertTrue(localhostRedirectInfo.contains("localhost"));
        assertTrue(localhostRedirectInfo.contains("openHAB host name or IP address"));
        assertTrue(localhostRedirectInfo.contains("Keep the port, path, and query string unchanged"));
        assertTrue(localhostRedirectInfo.contains("class=\"block warn localhost-redirect-info\""));
    }

    private static class TestSmartThingsServlet extends SmartThingsServlet {
        private static final long serialVersionUID = 1L;

        TestSmartThingsServlet(TranslationProvider translationProvider) throws SmartThingsException {
            super(mock(SmartThingsBridgeHandler.class), "/smartthings/account", mock(SmartThingsAuthService.class),
                    translationProvider, mock(HttpService.class));
        }

        @Override
        protected String readTemplate(String templateName) throws IOException {
            return "";
        }
    }
}
