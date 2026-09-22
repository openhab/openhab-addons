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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.evcc.internal.handler.EvccThingLifecycleAware;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

/**
 * Tests for MessageRouter.
 *
 * @author Marcel Goerentz - Initial contribution
 */
class MessageRouterTest {

    private MessageRouter router;
    private final ExtractionStrategy passThroughExtraction = new ExtractionStrategy() {
        @Override
        public JsonObject extract(com.google.gson.JsonElement source) {
            return source.getAsJsonObject();
        }

        @Override
        public String describe() {
            return "pass-through";
        }
    };

    @BeforeEach
    void setUp() {
        router = new MessageRouter();
    }

    @Test
    void testRegisterRoute() {
        EvccThingLifecycleAware handler = mock(EvccThingLifecycleAware.class);
        HandlerRoute route = new HandlerRoute("battery", passThroughExtraction, handler, "battery");

        router.registerRoute(route);
    }

    @Test
    void testRouteMessageToHandler() {
        EvccThingLifecycleAware handler = mock(EvccThingLifecycleAware.class);
        HandlerRoute route = new HandlerRoute("battery", passThroughExtraction, handler, "battery");

        router.registerRoute(route);

        JsonObject message = new JsonObject();
        message.add("power", new JsonPrimitive(-222));

        assertTrue(router.route("battery", message));

        verify(handler).handleUpdate("battery", message);
    }

    @Test
    void testUnmatchedRouteNotDispatched() {
        EvccThingLifecycleAware handler = mock(EvccThingLifecycleAware.class);
        HandlerRoute route = new HandlerRoute("battery", passThroughExtraction, handler, "battery");

        router.registerRoute(route);

        JsonObject message = new JsonObject();
        message.add("power", new JsonPrimitive(-222));

        assertFalse(router.route("pv", message)); // Different key

        verify(handler, never()).handleUpdate("battery", message);
    }

    @Test
    void testMultipleRoutes() {
        EvccThingLifecycleAware handler1 = mock(EvccThingLifecycleAware.class);
        EvccThingLifecycleAware handler2 = mock(EvccThingLifecycleAware.class);

        HandlerRoute route1 = new HandlerRoute("battery", passThroughExtraction, handler1, "battery");
        HandlerRoute route2 = new HandlerRoute("pv", passThroughExtraction, handler2, "pv");

        router.registerRoute(route1);
        router.registerRoute(route2);

        JsonObject batteryMsg = new JsonObject();
        batteryMsg.add("power", new JsonPrimitive(-222));

        JsonObject pvMsg = new JsonObject();
        pvMsg.add("power", new JsonPrimitive(1196));

        assertTrue(router.route("battery", batteryMsg));
        assertTrue(router.route("pv", pvMsg));

        verify(handler1).handleUpdate("battery", batteryMsg);
        verify(handler2).handleUpdate("pv", pvMsg);
    }

    @Test
    void testRouteWithoutMatchingKeyReturnsFalse() {
        JsonObject message = new JsonObject();
        message.add("power", new JsonPrimitive(1));

        assertFalse(router.route("battery", message));
    }

    @Test
    void testUnregisterRoutesStopsDispatchingToDisposedHandler() {
        EvccThingLifecycleAware handler = mock(EvccThingLifecycleAware.class);
        HandlerRoute route = new HandlerRoute("grid", passThroughExtraction, handler, "grid");
        router.registerRoute(route);

        router.unregisterRoutes(handler);

        JsonObject message = new JsonObject();
        message.add("power", new JsonPrimitive(-283.3));

        assertFalse(router.route("grid", message));
        verify(handler, never()).handleUpdate("grid", message);
    }

    @Test
    void testUnregisterRoutesOnlyRemovesRoutesForGivenHandler() {
        EvccThingLifecycleAware handler1 = mock(EvccThingLifecycleAware.class);
        EvccThingLifecycleAware handler2 = mock(EvccThingLifecycleAware.class);

        router.registerRoute(new HandlerRoute("battery", passThroughExtraction, handler1, "battery"));
        router.registerRoute(new HandlerRoute("pv", passThroughExtraction, handler2, "pv"));

        router.unregisterRoutes(handler1);

        JsonObject batteryMsg = new JsonObject();
        batteryMsg.add("power", new JsonPrimitive(-222));
        JsonObject pvMsg = new JsonObject();
        pvMsg.add("power", new JsonPrimitive(1196));

        assertFalse(router.route("battery", batteryMsg));
        assertTrue(router.route("pv", pvMsg));

        verify(handler1, never()).handleUpdate("battery", batteryMsg);
        verify(handler2).handleUpdate("pv", pvMsg);
    }

    @Test
    void testRouteSkipsDispatchWhenHandlerReportsDisposed() {
        // Simulates a handler whose dispose() ran (and set its own disposed flag) between the
        // time this route was registered and the time route() is invoked - e.g. a route that
        // was not yet unregistered because unregisterRoutes() itself is racing on another
        // thread. route() must check isDisposed() before calling handleUpdate() so no
        // already-disposed handler is ever invoked, even if its route is still present.
        EvccThingLifecycleAware handler = mock(EvccThingLifecycleAware.class);
        when(handler.isDisposed()).thenReturn(true);
        HandlerRoute route = new HandlerRoute("grid", passThroughExtraction, handler, "grid");
        router.registerRoute(route);

        JsonObject message = new JsonObject();
        message.add("power", new JsonPrimitive(-283.3));

        assertTrue(router.route("grid", message));
        verify(handler, never()).handleUpdate("grid", message);
    }
}
