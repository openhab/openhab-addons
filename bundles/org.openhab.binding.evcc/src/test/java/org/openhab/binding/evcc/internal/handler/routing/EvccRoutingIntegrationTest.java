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
package org.openhab.binding.evcc.internal.handler.routing;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.evcc.internal.handler.EvccThingLifecycleAware;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Integration tests for EVCC routing system.
 * 
 * Tests the complete message routing flow from bridge dispatch through extraction to handler update.
 *
 * @author Marcel Goerentz - Initial contribution
 */
class EvccRoutingIntegrationTest {

    private MessageRouter router;
    private List<EvccThingLifecycleAware> capturedHandlers;

    @BeforeEach
    void setUp() {
        router = new MessageRouter();
        capturedHandlers = new ArrayList<>();
    }

    @Nested
    class GridUpdateRoutingTests {

        private EvccThingLifecycleAware siteHandler;

        @BeforeEach
        void setUp() {
            siteHandler = mock(EvccThingLifecycleAware.class);
            capturedHandlers.add(siteHandler);
        }

        @Test
        void gridUpdateWithJsonPathExtractionExtractsRootElement() {
            router.registerRoute(new HandlerRoute("grid", new JsonPathExtraction("$"), siteHandler, "grid"));

            JsonObject gridUpdate = new JsonObject();
            gridUpdate.addProperty("power", 2000);
            gridUpdate.addProperty("energy", 10000);

            assertTrue(router.route("grid", gridUpdate));

            verify(siteHandler).handleUpdate("grid", gridUpdate);
        }

        @Test
        void gridUpdateWithFixedExtractionExtractsRootElement() {
            router.registerRoute(new HandlerRoute("grid", new FixedValueExtraction(), siteHandler, "grid"));

            JsonObject gridUpdate = new JsonObject();
            gridUpdate.addProperty("power", 2000);
            gridUpdate.addProperty("energy", 10000);

            assertTrue(router.route("grid", gridUpdate));

            verify(siteHandler).handleUpdate("grid", gridUpdate);
        }

        @Test
        void gridUpdateEmbeddedInSiteMessageExtractsGridField() {
            router.registerRoute(new HandlerRoute("site", new ObjectFieldExtraction("grid"), siteHandler, "grid"));

            JsonObject siteMessage = new JsonObject();
            JsonObject gridData = new JsonObject();
            gridData.addProperty("power", 2000);
            gridData.addProperty("energy", 10000);
            siteMessage.add("grid", gridData);

            assertTrue(router.route("site", siteMessage));

            verify(siteHandler).handleUpdate("grid", gridData);
        }

        @Test
        void gridUpdateWithCurrentsArrayIsExtractedCorrectly() {
            router.registerRoute(new HandlerRoute("grid", new JsonPathExtraction("$"), siteHandler, "grid"));

            JsonObject gridUpdate = new JsonObject();
            JsonArray currents = new JsonArray();
            currents.add(6.0);
            currents.add(7.0);
            currents.add(8.0);
            gridUpdate.add("currents", currents);

            assertTrue(router.route("grid", gridUpdate));

            ArgumentCaptor<JsonElement> captor = ArgumentCaptor.forClass(JsonElement.class);
            verify(siteHandler).handleUpdate(eq("grid"), captor.capture());

            JsonObject captured = captor.getValue().getAsJsonObject();
            assertEquals(currents, captured.get("currents"));
        }
    }

    @Nested
    class TopLevelKeyRoutingTests {

        private EvccThingLifecycleAware siteHandler;

        @BeforeEach
        void setUp() {
            siteHandler = mock(EvccThingLifecycleAware.class);
            capturedHandlers.add(siteHandler);
        }

        @Test
        void pvPowerTopLevelKeyIsRouted() {
            router.registerRoute(new HandlerRoute("pvPower", new FixedValueExtraction(), siteHandler, "pvPower"));

            JsonPrimitive pvPower = new JsonPrimitive(8476.122);

            assertTrue(router.route("pvPower", pvPower));

            verify(siteHandler).handleUpdate("pvPower", pvPower);
        }

        @Test
        void pvEnergyTopLevelKeyIsRouted() {
            router.registerRoute(new HandlerRoute("pvEnergy", new FixedValueExtraction(), siteHandler, "pvEnergy"));

            JsonPrimitive pvEnergy = new JsonPrimitive(50000.5);

            assertTrue(router.route("pvEnergy", pvEnergy));

            verify(siteHandler).handleUpdate("pvEnergy", pvEnergy);
        }

        @Test
        void tariffGridTopLevelKeyIsRouted() {
            router.registerRoute(new HandlerRoute("tariffGrid", new FixedValueExtraction(), siteHandler, "tariffGrid"));

            JsonPrimitive tariff = new JsonPrimitive(0.236);

            assertTrue(router.route("tariffGrid", tariff));

            verify(siteHandler).handleUpdate("tariffGrid", tariff);
        }

        @Test
        void unmatchedTopLevelKeyDoesNotRouteWhenNoFallback() {
            router.registerRoute(new HandlerRoute("pvPower", new FixedValueExtraction(), siteHandler, "pvPower"));

            JsonPrimitive someValue = new JsonPrimitive(42);

            assertFalse(router.route("unmatchedKey", someValue));

            verify(siteHandler, never()).handleUpdate(anyString(), any());
        }
    }

    @Nested
    class MultipleHandlerRoutingTests {

        private EvccThingLifecycleAware siteHandler;
        private EvccThingLifecycleAware batteryHandler;
        private EvccThingLifecycleAware pvHandler;

        @BeforeEach
        void setUp() {
            siteHandler = mock(EvccThingLifecycleAware.class);
            batteryHandler = mock(EvccThingLifecycleAware.class);
            pvHandler = mock(EvccThingLifecycleAware.class);

            capturedHandlers.add(siteHandler);
            capturedHandlers.add(batteryHandler);
            capturedHandlers.add(pvHandler);
        }

        @Test
        void multipleHandlersReceiveCorrectMessages() {
            router.registerRoute(new HandlerRoute("grid", new JsonPathExtraction("$"), siteHandler, "grid"));
            router.registerRoute(new HandlerRoute("battery", new FixedValueExtraction(), batteryHandler, "battery"));
            router.registerRoute(new HandlerRoute("pv", new FixedValueExtraction(), pvHandler, "pv"));

            JsonObject gridMsg = new JsonObject();
            gridMsg.addProperty("power", 2000);

            JsonObject batteryMsg = new JsonObject();
            batteryMsg.addProperty("soc", 65);

            JsonObject pvMsg = new JsonObject();
            pvMsg.addProperty("power", 5000);

            assertTrue(router.route("grid", gridMsg));
            assertTrue(router.route("battery", batteryMsg));
            assertTrue(router.route("pv", pvMsg));

            verify(siteHandler).handleUpdate(eq("grid"), eq(gridMsg));
            verify(batteryHandler).handleUpdate(eq("battery"), eq(batteryMsg));
            verify(pvHandler).handleUpdate(eq("pv"), eq(pvMsg));

            verify(siteHandler, never()).handleUpdate(eq("battery"), any());
            verify(batteryHandler, never()).handleUpdate(eq("grid"), any());
            verify(pvHandler, never()).handleUpdate(eq("battery"), any());
        }
    }

    @Nested
    class ExtractionStrategyTests {

        private EvccThingLifecycleAware handler;

        @BeforeEach
        void setUp() {
            handler = mock(EvccThingLifecycleAware.class);
            capturedHandlers.add(handler);
        }

        @Test
        void jsonPathExtractionRootExtractsFullObject() {
            router.registerRoute(new HandlerRoute("data", new JsonPathExtraction("$"), handler, "result"));

            JsonObject input = new JsonObject();
            input.addProperty("nested", "value");

            assertTrue(router.route("data", input));

            ArgumentCaptor<JsonElement> captor = ArgumentCaptor.forClass(JsonElement.class);
            verify(handler).handleUpdate(eq("result"), captor.capture());

            assertEquals(input, captor.getValue());
        }

        @Test
        void jsonPathExtractionNestedPropertyExtractsCorrectly() {
            router.registerRoute(new HandlerRoute("data", new JsonPathExtraction("$.battery"), handler, "result"));

            JsonObject battery = new JsonObject();
            battery.addProperty("soc", 75);

            JsonObject input = new JsonObject();
            input.add("battery", battery);

            assertTrue(router.route("data", input));

            ArgumentCaptor<JsonElement> captor = ArgumentCaptor.forClass(JsonElement.class);
            verify(handler).handleUpdate(eq("result"), captor.capture());

            assertEquals(battery, captor.getValue());
        }

        @Test
        void fixedValueExtractionReturnsInputAsIs() {
            router.registerRoute(new HandlerRoute("power", new FixedValueExtraction(), handler, "pvPower"));

            JsonPrimitive value = new JsonPrimitive(5000);

            assertTrue(router.route("power", value));

            verify(handler).handleUpdate("pvPower", value);
        }

        @Test
        void objectFieldExtractionExtractsSpecificField() {
            router.registerRoute(new HandlerRoute("site", new ObjectFieldExtraction("grid"), handler, "gridUpdate"));

            JsonObject gridData = new JsonObject();
            gridData.addProperty("power", 2000);

            JsonObject siteData = new JsonObject();
            siteData.add("grid", gridData);

            assertTrue(router.route("site", siteData));

            ArgumentCaptor<JsonElement> captor = ArgumentCaptor.forClass(JsonElement.class);
            verify(handler).handleUpdate(eq("gridUpdate"), captor.capture());

            assertEquals(gridData, captor.getValue());
        }

        @Test
        void extractionFailureStillMatchesButDoesNotDispatch() {
            router.registerRoute(new HandlerRoute("data", new JsonPathExtraction("$.missing"), handler, "result"));

            JsonObject input = new JsonObject();
            input.addProperty("other", "value");

            // Route matches the key, so returns true, but extraction fails so handler is never called
            assertTrue(router.route("data", input));

            verify(handler, never()).handleUpdate(anyString(), any());
        }
    }

    @Nested
    class RealWorldScenarios {

        private EvccThingLifecycleAware siteHandler;
        private EvccThingLifecycleAware batteryHandler;

        @BeforeEach
        void setUp() {
            siteHandler = mock(EvccThingLifecycleAware.class);
            batteryHandler = mock(EvccThingLifecycleAware.class);

            capturedHandlers.add(siteHandler);
            capturedHandlers.add(batteryHandler);
        }

        @Test
        void gridUpdateWithPhaseDataIsRoutedCorrectly() {
            router.registerRoute(new HandlerRoute("grid", new JsonPathExtraction("$"), siteHandler, "grid"));

            JsonArray currents = new JsonArray();
            currents.add(6.0);
            currents.add(7.5);
            currents.add(8.2);

            JsonArray voltages = new JsonArray();
            voltages.add(230.0);
            voltages.add(231.0);
            voltages.add(229.5);

            JsonArray powers = new JsonArray();
            powers.add(1380.0);
            powers.add(1732.5);
            powers.add(1890.2);

            JsonObject gridUpdate = new JsonObject();
            gridUpdate.add("currents", currents);
            gridUpdate.add("voltages", voltages);
            gridUpdate.add("powers", powers);

            assertTrue(router.route("grid", gridUpdate));

            ArgumentCaptor<JsonElement> captor = ArgumentCaptor.forClass(JsonElement.class);
            verify(siteHandler).handleUpdate(eq("grid"), captor.capture());

            JsonObject captured = captor.getValue().getAsJsonObject();
            assertEquals(currents, captured.get("currents"));
            assertEquals(voltages, captured.get("voltages"));
            assertEquals(powers, captured.get("powers"));
        }

        @Test
        void siteMessageWithEmbeddedGridIsExtractedCorrectly() {
            router.registerRoute(
                    new HandlerRoute("site", new ObjectFieldExtraction("grid"), siteHandler, "gridUpdate"));
            router.registerRoute(
                    new HandlerRoute("site", new ObjectFieldExtraction("battery"), batteryHandler, "batteryUpdate"));

            JsonObject gridData = new JsonObject();
            gridData.addProperty("power", 2000);

            JsonObject batteryData = new JsonObject();
            batteryData.addProperty("soc", 75);

            JsonObject siteMessage = new JsonObject();
            siteMessage.add("grid", gridData);
            siteMessage.add("battery", batteryData);
            siteMessage.addProperty("smartCostType", "price");

            assertTrue(router.route("site", siteMessage));

            ArgumentCaptor<JsonElement> gridCaptor = ArgumentCaptor.forClass(JsonElement.class);
            verify(siteHandler).handleUpdate(eq("gridUpdate"), gridCaptor.capture());
            assertEquals(gridData, gridCaptor.getValue());

            ArgumentCaptor<JsonElement> batteryCaptor = ArgumentCaptor.forClass(JsonElement.class);
            verify(batteryHandler).handleUpdate(eq("batteryUpdate"), batteryCaptor.capture());
            assertEquals(batteryData, batteryCaptor.getValue());
        }

        @Test
        void topLevelKeysAreRoutedWithFixedExtraction() {
            router.registerRoute(new HandlerRoute("pvPower", new FixedValueExtraction(), siteHandler, "pvPower"));
            router.registerRoute(new HandlerRoute("pvEnergy", new FixedValueExtraction(), siteHandler, "pvEnergy"));
            router.registerRoute(new HandlerRoute("tariffGrid", new FixedValueExtraction(), siteHandler, "tariffGrid"));

            JsonPrimitive pvPower = new JsonPrimitive(8476.122);
            JsonPrimitive pvEnergy = new JsonPrimitive(50000.5);
            JsonPrimitive tariff = new JsonPrimitive(0.236);

            assertTrue(router.route("pvPower", pvPower));
            assertTrue(router.route("pvEnergy", pvEnergy));
            assertTrue(router.route("tariffGrid", tariff));

            verify(siteHandler, times(3)).handleUpdate(anyString(), any());
        }
    }
}
