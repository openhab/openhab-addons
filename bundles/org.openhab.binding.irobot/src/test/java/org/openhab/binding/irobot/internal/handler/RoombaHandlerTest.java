/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.irobot.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.lang.reflect.Field;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.irobot.internal.config.IRobotConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.internal.ThingImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

/**
 * Test the MQTT protocol with local iRobot (without openhab running).
 * This class is used to test the binding against a local iRobot instance.
 *
 * @author Florian Binder - Initial contribution
 */

@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RoombaHandlerTest {

    private static final String IP_ADDRESS = "<iRobotIP>";
    private static final String PASSWORD = "<PasswordForIRobot>";

    @Nullable
    private RoombaHandler handler;
    // We have to initialize it to avoid compile errors
    private @Mock Thing thing = new ThingImpl(new ThingTypeUID("AA:BB"), "");
    @Nullable
    private ThingHandlerCallback callback;

    @BeforeEach
    void setUp() throws Exception {
        Logger logger = LoggerFactory.getLogger(RoombaHandler.class);
        Field logLevelField = logger.getClass().getDeclaredField("currentLogLevel");
        logLevelField.setAccessible(true);
        logLevelField.set(logger, LocationAwareLogger.TRACE_INT);

        Configuration config = new Configuration();
        config.put("ipaddress", RoombaHandlerTest.IP_ADDRESS);
        config.put("password", RoombaHandlerTest.PASSWORD);
        Mockito.when(thing.getConfiguration()).thenReturn(config);
        Mockito.lenient().when(thing.getStatusInfo())
                .thenReturn(new ThingStatusInfo(ThingStatus.UNINITIALIZED, ThingStatusDetail.NONE, "mocked"));
        Mockito.lenient().when(thing.getUID()).thenReturn(new ThingUID("mocked", "irobot", "uid"));

        callback = Mockito.mock(ThingHandlerCallback.class);

        handler = new RoombaHandler(thing);
        handler.setCallback(callback);
    }

    @Test
    void testConfiguration() throws InterruptedException, IOException {
        handler.initialize();

        IRobotConfiguration config = thing.getConfiguration().as(IRobotConfiguration.class);
        assertEquals(config.getIpAddress(), IP_ADDRESS);
        assertEquals(config.getPassword(), PASSWORD);

        handler.dispose();
    }

    @Test
    void testCleanRegion() throws InterruptedException, IOException {
        handler.initialize();

        ChannelUID cmd = new ChannelUID(thing.getUID(), "command");
        handler.handleCommand(cmd, new StringType("cleanRegions:AABBCCDDEEFFGGHH;2,3"));

        handler.dispose();
    }

    @Test
    void testDock() throws InterruptedException, IOException {
        handler.initialize();

        ChannelUID cmd = new ChannelUID(thing.getUID(), "command");
        handler.handleCommand(cmd, new StringType("dock"));

        handler.dispose();
    }

    @Test
    void testStop() throws InterruptedException, IOException {
        handler.initialize();

        ChannelUID cmd = new ChannelUID(thing.getUID(), "command");
        handler.handleCommand(cmd, new StringType("stop"));

        handler.dispose();
    }
}
