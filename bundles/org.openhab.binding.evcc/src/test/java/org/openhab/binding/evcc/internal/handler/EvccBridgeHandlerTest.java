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
package org.openhab.binding.evcc.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.evcc.internal.EvccBindingConstants;
import org.openhab.binding.evcc.internal.handler.routing.HandlerRoute;
import org.openhab.binding.evcc.internal.handler.routing.JsonPathExtraction;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Tests for {@link EvccBridgeHandler}, focused on dispatching websocket updates.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
class EvccBridgeHandlerTest {

    private EvccBridgeHandler bridgeHandler = newBridgeHandler();

    private static EvccBridgeHandler newBridgeHandler() {
        Bridge bridge = mock(Bridge.class);
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        when(httpClientFactory.getCommonHttpClient()).thenReturn(mock(HttpClient.class));
        TranslationProvider i18nProvider = mock(TranslationProvider.class);
        LocaleProvider localeProvider = mock(LocaleProvider.class);
        return new EvccBridgeHandler(bridge, httpClientFactory, i18nProvider, localeProvider);
    }

    /** Minimal {@link EvccThingLifecycleAware} stub that records the last dispatched update. */
    private static class RecordingHandler implements EvccThingLifecycleAware {
        private final String type;
        private final Object identifier;
        private @org.eclipse.jdt.annotation.Nullable String lastKey;
        private @org.eclipse.jdt.annotation.Nullable JsonElement lastValue;

        RecordingHandler(String type, Object identifier) {
            this.type = type;
            this.identifier = identifier;
        }

        @Override
        public void initializeThingFromLatestState(JsonObject state) {
        }

        @Override
        public JsonObject getStateFromCachedState(JsonObject state) {
            return new JsonObject();
        }

        @Override
        public void handleUpdate(String key, JsonElement value) {
            lastKey = key;
            lastValue = value;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public Object getIdentifier() {
            return identifier;
        }

        @Override
        public boolean isDisposed() {
            return false;
        }
    }

    @BeforeEach
    void setUp() {
        bridgeHandler = newBridgeHandler();
    }

    @Test
    void indexedDotNotationKeyIsRoutedUsingMergedCachedValue() {
        // evcc reports individual loadpoint field changes using dot notation
        // ("loadpoints.<index>.<field>"), never as a full "loadpoints" array snapshot.
        // The route registered by a loadpoint handler only matches the literal key
        // "loadpoints", so the dotted key must be resolved to its root ("loadpoints")
        // and dispatched using the up-to-date merged value for that root.
        RecordingHandler loadpointHandler = new RecordingHandler(EvccBindingConstants.JSON_KEY_LOADPOINTS, 1);
        bridgeHandler.getMessageRouter().registerRoute(new HandlerRoute(EvccBindingConstants.JSON_KEY_LOADPOINTS,
                new JsonPathExtraction("$[1]"), loadpointHandler, EvccBindingConstants.JSON_KEY_LOADPOINTS));

        bridgeHandler.onPartialUpdate("loadpoints.1.chargePower", new JsonPrimitive(1500));

        assertEquals(EvccBindingConstants.JSON_KEY_LOADPOINTS, loadpointHandler.lastKey);
        JsonElement dispatched = loadpointHandler.lastValue;
        Assertions.assertNotNull(dispatched);
        assertEquals(1500, dispatched.getAsJsonObject().get("chargePower").getAsInt());
    }

    @Test
    void nestedDotNotationKeyIsRoutedUsingMergedCachedValue() {
        // evcc reports "forecast.solar" as its own dotted key carrying the full "solar"
        // sub-object; the forecast handler's route matches the literal key "forecast" and
        // extracts "$.solar" from it.
        RecordingHandler forecastHandler = new RecordingHandler(EvccBindingConstants.JSON_KEY_FORECAST, "solar");
        bridgeHandler.getMessageRouter().registerRoute(new HandlerRoute(EvccBindingConstants.JSON_KEY_FORECAST,
                new JsonPathExtraction("$.solar"), forecastHandler, EvccBindingConstants.JSON_KEY_FORECAST));

        JsonObject solar = new JsonObject();
        solar.addProperty("scale", 1.0);
        bridgeHandler.onPartialUpdate("forecast.solar", solar);

        assertEquals(EvccBindingConstants.JSON_KEY_FORECAST, forecastHandler.lastKey);
        JsonElement dispatched = forecastHandler.lastValue;
        Assertions.assertNotNull(dispatched);
        assertEquals(1.0, dispatched.getAsJsonObject().get("scale").getAsDouble());
    }

    @Test
    void nonDottedKeyStillFallsBackToSiteHandlerWhenUnrouted() {
        RecordingHandler siteHandler = new RecordingHandler(EvccBindingConstants.PROPERTY_TYPE_SITE, "");
        bridgeHandler.register(siteHandler);

        bridgeHandler.onPartialUpdate("homePower", new JsonPrimitive(1234));

        assertEquals("homePower", siteHandler.lastKey);
        JsonElement dispatched = siteHandler.lastValue;
        Assertions.assertNotNull(dispatched);
        assertEquals(1234, dispatched.getAsInt());
    }

    @Test
    void unroutableDottedKeyIsSilentlyIgnoredWithoutDispatchingToSiteHandler() {
        // No loadpoint route is registered here, so the resolved root key ("loadpoints")
        // must not be forwarded to the site-handler fallback either (that fallback is
        // reserved for genuinely top-level, non-nested keys).
        RecordingHandler siteHandler = new RecordingHandler(EvccBindingConstants.PROPERTY_TYPE_SITE, "");
        bridgeHandler.register(siteHandler);

        bridgeHandler.onPartialUpdate("loadpoints.0.chargePower", new JsonPrimitive(0));

        assertNull(siteHandler.lastKey);
    }
}
