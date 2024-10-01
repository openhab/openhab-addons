/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.pentair.internal.handler;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.pentair.internal.PentairBindingConstants.*;
import static org.openhab.binding.pentair.internal.TestUtilities.parsehex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.pentair.internal.parser.PentairStandardPacket;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PentailIntelliFlowHandlerTest }
 *
 * @author Jeff James - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class PentairIntelliFloHandlerTest {
    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.getLogger(PentairIntelliFloHandlerTest.class);

    //@formatter:off
    public static byte[][] packets = {
            parsehex("A5 00 22 60 07 0F 0A 02 02 00 E7 06 D6 00 00 00 00 00 01 02 03"),
            parsehex("A5 00 22 60 07 0F 0A 00 00 01 F9 07 D5 00 00 00 00 09 21 0A 3A"),          // SVRS alarm
            parsehex("a5 00 10 60 07 0f 0a 02 02 00 5a 02 ee 00 00 00 00 00 01 15 1f"),
            parsehex("A5 00 10 60 07 0F 04 00 00 00 00 00 00 00 00 00 00 00 00 14 1E")
    };
    //@formatter:on

    private @NonNullByDefault({}) PentairIntelliFloHandler handler;
    private String uid = "1:2:3";
    private ThingUID thingUID = new ThingUID(uid);

    private List<Channel> channels = new ArrayList<Channel>();

    @Mock
    private @NonNullByDefault({}) Bridge bridge;

    @Mock
    private @NonNullByDefault({}) ThingHandlerCallback callback;

    @Mock
    private @NonNullByDefault({}) Thing thing;

    @Mock
    private @NonNullByDefault({}) PentairIPBridgeHandler pibh;

    @BeforeAll
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterAll
    public static void tearDownAfterClass() throws Exception {
    }

    @BeforeEach
    public void setUp() throws Exception {
        pibh = new PentairIPBridgeHandler(bridge);

        handler = new PentairIntelliFloHandler(thing) {
            @Override
            public @NonNull PentairBaseBridgeHandler getBridgeHandler() {
                return pibh;
            }
        };

        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLIFLO_RUN)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLIFLO_POWER)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLIFLO_RPM)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, INTELLIFLO_GPM)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, INTELLIFLO_ERROR)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, INTELLIFLO_STATUS1)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, INTELLIFLO_STATUS2)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, INTELLIFLO_TIMER)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, INTELLIFLO_RUNPROGRAM)).build());

        when(thing.getConfiguration()).thenReturn(new Configuration(Collections.singletonMap("id", 0x10)));
        when(thing.getHandler()).thenReturn(handler);
        when(thing.getChannels()).thenReturn(channels);
        handler.setCallback(callback);
    }

    @AfterEach
    public void tearDown() throws Exception {
        handler.dispose();
    }

    @Test
    public void testPacketProcessing() {
        ChannelUID cuid;
        PentairStandardPacket p;

        handler.initialize();

        verify(callback, times(1)).statusUpdated(eq(thing),
                argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));

        p = new PentairStandardPacket(packets[0], packets[0].length);
        handler.processPacketFrom(p);
        verify(callback, times(1)).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
        cuid = new ChannelUID(thingUID, CHANNEL_INTELLIFLO_RUN);
        verify(callback, times(1)).stateUpdated(cuid, OnOffType.ON);
        cuid = new ChannelUID(thingUID, CHANNEL_INTELLIFLO_POWER);
        verify(callback, times(1)).stateUpdated(cuid, new QuantityType<>(231, Units.WATT));
        cuid = new ChannelUID(thingUID, CHANNEL_INTELLIFLO_RPM);
        verify(callback, times(1)).stateUpdated(cuid, new DecimalType(1750));

        Mockito.reset(callback);

        p = new PentairStandardPacket(packets[1], packets[1].length);
        handler.processPacketFrom(p);
        cuid = new ChannelUID(thingUID, CHANNEL_INTELLIFLO_RUN);
        verify(callback, times(1)).stateUpdated(cuid, OnOffType.ON);
        cuid = new ChannelUID(thingUID, CHANNEL_INTELLIFLO_POWER);
        verify(callback, times(1)).stateUpdated(cuid, new QuantityType<>(505, Units.WATT));
        cuid = new ChannelUID(thingUID, CHANNEL_INTELLIFLO_RPM);
        verify(callback, times(1)).stateUpdated(cuid, new DecimalType(2005));

        Mockito.reset(callback);

        p = new PentairStandardPacket(packets[2], packets[2].length);
        handler.processPacketFrom(p);

        Mockito.reset(callback);

        p = new PentairStandardPacket(packets[3], packets[3].length);
        handler.processPacketFrom(p);
        cuid = new ChannelUID(thingUID, CHANNEL_INTELLIFLO_RUN);
        verify(callback, times(1)).stateUpdated(cuid, OnOffType.OFF);
        cuid = new ChannelUID(thingUID, CHANNEL_INTELLIFLO_POWER);
        verify(callback, times(1)).stateUpdated(cuid, new QuantityType<>(0, Units.WATT));
        cuid = new ChannelUID(thingUID, CHANNEL_INTELLIFLO_RPM);
        verify(callback, times(1)).stateUpdated(cuid, new DecimalType(0));
    }
}
