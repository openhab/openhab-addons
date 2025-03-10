/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.dirigera.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.openhab.binding.dirigera.internal.Constants.CHANNEL_LOCATION;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.binding.dirigera.internal.mock.DirigeraAPISimu;
import org.openhab.binding.dirigera.internal.mock.DirigeraHandlerManipulator;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * {@link TestGateway} for checking gateway use cases
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestGateway {
    private static String deviceId = "594197c3-23c9-4dc7-a6ca-1fe6a8455d29_1";

    private static DirigeraHandler handler = mock(DirigeraHandler.class);
    private static CallbackMock callback = mock(CallbackMock.class);
    private static Thing thing = mock(Thing.class);
    private static String mockFile = "src/test/resources/gateway/home-with-coordinates.json";

    @Test
    void testBridgeCreation() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge(mockFile, false, List.of());
        ThingHandler bridgeHandler = hubBridge.getHandler();
        assertTrue(bridgeHandler instanceof DirigeraHandlerManipulator);
        handler = (DirigeraHandlerManipulator) bridgeHandler;
        thing = handler.getThing();
        ThingHandlerCallback proxyCallback = ((DirigeraHandlerManipulator) handler).getCallback();
        assertNotNull(proxyCallback);
        assertTrue(proxyCallback instanceof CallbackMock);
        callback = (CallbackMock) proxyCallback;
        handler.initialize();
        callback.waitForOnline();
    }

    @Test
    void testWithCoordinates() {
        mockFile = "src/test/resources/gateway/home-with-coordinates.json";
        testBridgeCreation();
        assertNotNull(handler);
        assertNotNull(thing);
        assertNotNull(callback);

        State locationPoint = callback.getState("dirigera:gateway:9876:location");
        assertNotNull(locationPoint);
        assertTrue(locationPoint instanceof PointType);
        assertEquals("9.876,1.234", ((PointType) locationPoint).toFullString(), "Location Point");
    }

    @Test
    void testWithoutCoordinates() {
        mockFile = "src/test/resources/gateway/home-without-coordinates.json";
        testBridgeCreation();
        assertNotNull(handler);
        assertNotNull(thing);
        assertNotNull(callback);

        State locationPoint = callback.getState("dirigera:gateway:9876:location");
        assertNotNull(locationPoint);
        assertTrue(locationPoint instanceof UnDefType);
    }

    @Test
    void testCommands() {
        testWithCoordinates();

        // remove location from gateway with empty string
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LOCATION), StringType.EMPTY);
        String patch = DirigeraAPISimu.patchMap.get(deviceId);
        assertNotNull(patch);
        assertEquals("{\"attributes\":{\"coordinates\":{}}}", patch, "Empty Coordinates");
        DirigeraAPISimu.patchMap.clear();

        // set new location with valid coordinates
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LOCATION), StringType.valueOf("9.123,1.987"));
        patch = DirigeraAPISimu.patchMap.get(deviceId);
        assertNotNull(patch);
        assertEquals("{\"attributes\":{\"coordinates\":{\"latitude\":9.123,\"longitude\":1.987}}}", patch,
                "Valid Coordinates");
        DirigeraAPISimu.patchMap.clear();

        // nothing send if value is invalid
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LOCATION), StringType.valueOf("wrong coding"));
        patch = DirigeraAPISimu.patchMap.get(deviceId);
        assertNull(patch, "Wrong coordinates");
        DirigeraAPISimu.patchMap.clear();
    }
}
