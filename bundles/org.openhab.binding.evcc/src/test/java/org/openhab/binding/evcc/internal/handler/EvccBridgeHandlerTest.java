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
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.evcc.internal.EvccBindingConstants;
import org.openhab.binding.evcc.internal.handler.routing.FixedValueExtraction;
import org.openhab.binding.evcc.internal.handler.routing.HandlerRoute;
import org.openhab.binding.evcc.internal.handler.routing.JsonPathExtraction;
import org.openhab.binding.evcc.internal.handler.routing.LoadpointStateTransformer;
import org.openhab.binding.evcc.internal.handler.routing.StateTransformer;
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
 * @author Michael Weger - Add tests for indexed delta dispatching
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
        private int dispatchCount;

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
            dispatchCount++;
        }

        @Override
        public void applyNormalizedUpdate(JsonObject normalized) {
            lastKey = null;
            lastValue = normalized;
            dispatchCount++;
        }

        JsonObject lastObject() {
            JsonElement value = lastValue;
            Assertions.assertNotNull(value);
            return value.getAsJsonObject();
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

    private void partialUpdate(String key, JsonElement value) {
        JsonObject delta = new JsonObject();
        delta.add(key, value);
        bridgeHandler.onPartialUpdate(delta);
    }

    private RecordingHandler registerLoadpointRoute(int index, @Nullable StateTransformer transformer) {
        RecordingHandler handler = new RecordingHandler(EvccBindingConstants.JSON_KEY_LOADPOINTS, index);
        bridgeHandler.getMessageRouter()
                .registerRoute(new HandlerRoute(EvccBindingConstants.JSON_KEY_LOADPOINTS + "." + index,
                        new FixedValueExtraction(), handler, EvccBindingConstants.JSON_KEY_LOADPOINTS, transformer));
        return handler;
    }

    @Test
    void indexedDeltaIsOnlyDispatchedToTheRouteOfThatIndex() {
        RecordingHandler loadpoint0 = registerLoadpointRoute(0, null);
        RecordingHandler loadpoint1 = registerLoadpointRoute(1, null);

        partialUpdate("loadpoints.1.chargePower", new JsonPrimitive(1500));

        assertEquals(0, loadpoint0.dispatchCount);
        assertEquals(1, loadpoint1.dispatchCount);
    }

    @Test
    void indexedDeltaOnlyCarriesTheChangedFields() {
        RecordingHandler loadpoint0 = registerLoadpointRoute(0, null);
        partialUpdate("loadpoints.0.mode", new JsonPrimitive("pv"));
        partialUpdate("loadpoints.0.charging", new JsonPrimitive(false));

        partialUpdate("loadpoints.0.chargePower", new JsonPrimitive(1500));

        JsonObject dispatched = loadpoint0.lastObject();
        assertEquals(1, dispatched.size());
        assertEquals(1500, dispatched.get("chargePower").getAsInt());
    }

    @Test
    void indexedKeysOfOneMessageAreDispatchedOncePerElement() {
        RecordingHandler loadpoint0 = registerLoadpointRoute(0, null);
        RecordingHandler loadpoint1 = registerLoadpointRoute(1, null);
        JsonObject message = new JsonObject();
        message.addProperty("loadpoints.0.chargePower", 1500);
        message.addProperty("loadpoints.0.charging", true);
        message.addProperty("loadpoints.1.connected", false);

        bridgeHandler.onPartialUpdate(message);

        assertEquals(1, loadpoint0.dispatchCount);
        JsonObject dispatched0 = loadpoint0.lastObject();
        assertEquals(2, dispatched0.size());
        assertEquals(1500, dispatched0.get("chargePower").getAsInt());
        assertEquals(true, dispatched0.get("charging").getAsBoolean());
        assertEquals(1, loadpoint1.dispatchCount);
        JsonObject dispatched1 = loadpoint1.lastObject();
        assertEquals(1, dispatched1.size());
        assertEquals(false, dispatched1.get("connected").getAsBoolean());
    }

    @Test
    void indexedDeltaIsNormalizedByTheRouteTransformer() {
        RecordingHandler loadpoint0 = registerLoadpointRoute(0, new LoadpointStateTransformer());

        partialUpdate("loadpoints.0." + EvccBindingConstants.JSON_KEY_CHARGE_CURRENT, new JsonPrimitive(16));

        JsonObject dispatched = loadpoint0.lastObject();
        assertEquals(1, dispatched.size());
        assertEquals(16, dispatched.get(EvccBindingConstants.JSON_KEY_OFFERED_CURRENT).getAsInt());
    }

    @Test
    void indexedDotNotationKeyIsRoutedUnderTheElementKey() {
        // evcc reports individual loadpoint field changes using dot notation
        // ("loadpoints.<index>.<field>"), never as a full "loadpoints" array snapshot.
        // The loadpoint handler registers its route under the element key ("loadpoints.<index>"),
        // so the dotted key must be routed under that key with the changed field.
        RecordingHandler loadpointHandler = registerLoadpointRoute(1, null);

        partialUpdate("loadpoints.1.chargePower", new JsonPrimitive(1500));

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
        partialUpdate("forecast.solar", solar);

        assertEquals(EvccBindingConstants.JSON_KEY_FORECAST, forecastHandler.lastKey);
        JsonElement dispatched = forecastHandler.lastValue;
        Assertions.assertNotNull(dispatched);
        assertEquals(1.0, dispatched.getAsJsonObject().get("scale").getAsDouble());
    }

    @Test
    void nestedDotNotationKeyDoesNotRedispatchSiblingRoutesUnderSameKey() {
        // The forecast handler registers separate routes for co2/feedin/grid/solar, all under
        // the shared route key "forecast". A "forecast.solar" delta must only dispatch to the
        // solar route; the sibling routes must not be redundantly redispatched with their own,
        // unchanged, merged-cache data.
        RecordingHandler co2Handler = new RecordingHandler(EvccBindingConstants.JSON_KEY_FORECAST, "co2");
        RecordingHandler solarHandler = new RecordingHandler(EvccBindingConstants.JSON_KEY_FORECAST, "solar");
        bridgeHandler.getMessageRouter().registerRoute(new HandlerRoute(EvccBindingConstants.JSON_KEY_FORECAST,
                new JsonPathExtraction("$.co2"), co2Handler, EvccBindingConstants.JSON_KEY_FORECAST));
        bridgeHandler.getMessageRouter().registerRoute(new HandlerRoute(EvccBindingConstants.JSON_KEY_FORECAST,
                new JsonPathExtraction("$.solar"), solarHandler, EvccBindingConstants.JSON_KEY_FORECAST));

        // Populate the co2 sub-object in the cache first, as if it arrived earlier.
        JsonObject co2 = new JsonObject();
        co2.addProperty("today", 100);
        partialUpdate("forecast.co2", co2);
        assertEquals(EvccBindingConstants.JSON_KEY_FORECAST, co2Handler.lastKey);
        co2Handler.lastKey = null;
        co2Handler.lastValue = null;

        // A later, unrelated "forecast.solar" delta must not re-trigger the co2 route, even
        // though the merged cache value for "forecast" still contains the earlier co2 data.
        JsonObject solar = new JsonObject();
        solar.addProperty("scale", 1.0);
        partialUpdate("forecast.solar", solar);

        assertEquals(EvccBindingConstants.JSON_KEY_FORECAST, solarHandler.lastKey);
        assertNull(co2Handler.lastKey);
        assertNull(co2Handler.lastValue);
    }

    @Test
    void nonDottedKeyStillFallsBackToSiteHandlerWhenUnrouted() {
        RecordingHandler siteHandler = new RecordingHandler(EvccBindingConstants.PROPERTY_TYPE_SITE, "");
        bridgeHandler.register(siteHandler);

        partialUpdate("homePower", new JsonPrimitive(1234));

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

        partialUpdate("loadpoints.0.chargePower", new JsonPrimitive(0));

        assertNull(siteHandler.lastKey);
    }
}
