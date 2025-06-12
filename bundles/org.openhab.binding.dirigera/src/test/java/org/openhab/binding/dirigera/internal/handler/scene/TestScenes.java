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
package org.openhab.binding.dirigera.internal.handler.scene;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.dirigera.internal.Constants.*;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dirigera.internal.handler.DirigeraBridgeProvider;
import org.openhab.binding.dirigera.internal.mock.CallbackMock;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.internal.ThingImpl;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;

/**
 * {@link TestSCene} Tests device handler creation, initializing and refresh of channels
 *
 * @author Bernd Weymann - Initial Contribution
 */
@NonNullByDefault
class TestScene {

    @Test
    void testSceneHandler() {
        Bridge hubBridge = DirigeraBridgeProvider.prepareSimuBridge();
        ThingImpl thing = new ThingImpl(THING_TYPE_SCENE, "test-device");
        thing.setBridgeUID(hubBridge.getBridgeUID());
        SceneHandler handler = new SceneHandler(thing, SCENE_MAP);
        CallbackMock callback = new CallbackMock();
        callback.setBridge(hubBridge);
        handler.setCallback(callback);

        // set the right id
        Map<String, Object> config = new HashMap<>();
        config.put("id", "086f4a37-ebe8-4fd4-9a25-a0220a1e5f58");
        handler.handleConfigurationUpdate(config);

        handler.initialize();
        callback.waitForOnline();
        checkSceneStates(callback);

        callback.clear();
        handler.handleCommand(new ChannelUID(thing.getUID(), CHANNEL_LAST_TRIGGER), RefreshType.REFRESH);
        checkSceneStates(callback);
    }

    void checkSceneStates(CallbackMock callback) {
        State dateTimeState = callback.getState("dirigera:scene:test-device:last-trigger");
        assertNotNull(dateTimeState);
        assertTrue(dateTimeState instanceof DateTimeType);
        assertEquals("2024-10-16T02:21:15.977+0200",
                ((DateTimeType) dateTimeState).toFullString(ZoneId.of("Europe/Berlin")), "Last trigger");
    }
}
