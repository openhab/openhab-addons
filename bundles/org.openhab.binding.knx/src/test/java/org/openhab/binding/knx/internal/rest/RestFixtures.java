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
package org.openhab.binding.knx.internal.rest;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.knx.internal.factory.KNXHandlerFactory;
import org.openhab.binding.knx.internal.handler.IPBridgeThingHandler;
import org.openhab.binding.knx.internal.handler.KNXBridgeBaseThingHandler;
import org.openhab.binding.knx.internal.monitor.TelegramMonitor;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Made-up bridges for the REST tests: a handler stand-in with its own monitor, and a factory that knows them.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
final class RestFixtures {
    /** Writes the answers as core's JSON writer does: a plain Gson, null fields left out. */
    static final Gson GSON = new GsonBuilder().create();

    private RestFixtures() {
    }

    static ThingUID uid(String id) {
        return new ThingUID("knx", "ip", id);
    }

    static Bridge bridgeThing(String id, Map<String, Object> configuration) {
        return BridgeBuilder.create(new ThingTypeUID("knx", "ip"), uid(id)).withLabel("Made-up bridge " + id)
                .withConfiguration(new Configuration(configuration)).build();
    }

    static IPBridgeThingHandler handler(String id, TelegramMonitor monitor) {
        return handler(bridgeThing(id, Map.of()), monitor);
    }

    static IPBridgeThingHandler handler(Bridge bridge, TelegramMonitor monitor) {
        IPBridgeThingHandler handler = mock(IPBridgeThingHandler.class);
        when(handler.getThing()).thenReturn(bridge);
        when(handler.getTelegramMonitor()).thenReturn(monitor);
        when(handler.getKeyringFileName()).thenReturn("");
        return handler;
    }

    static KNXHandlerFactory factory(KNXBridgeBaseThingHandler... handlers) {
        KNXHandlerFactory factory = mock(KNXHandlerFactory.class);
        when(factory.getBridges()).thenReturn(List.of(handlers));
        return factory;
    }
}
