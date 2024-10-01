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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.pentair.internal.PentairBindingConstants.*;
import static org.openhab.binding.pentair.internal.TestUtilities.parsehex;

import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Dimensionless;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.pentair.internal.parser.PentairIntelliChlorPacket;
import org.openhab.core.config.core.Configuration;
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

/**
 * PentairIntelliChloreHandlerTest
 *
 * @author Jeff James - Initial contribution
 *
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class PentairIntelliChlorHandlerTest {

    //@formatter:off
    public static byte[][] packets = {
            parsehex("10 02 50 11 50"),
            parsehex("10 02 00 12 67 80"),
            parsehex("10 02 50 14 00"),
            parsehex("10 02 50 11 00"),
            parsehex("10 02 00 12 4C 81"),
            parsehex("10 02 00 03 00 49 6E 74 65 6C 6C 69 63 68 6C 6F 72 2D 2D 34 30"),
            parsehex("10 02 00 12 4C 81")
    };
    //@formatter:on

    private @NonNullByDefault({}) PentairIntelliChlorHandler handler;
    private String uid = "1:2:3";
    private ThingUID thingUID = new ThingUID(uid);

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

        handler = new PentairIntelliChlorHandler(thing) {
            @Override
            public @NonNull PentairBaseBridgeHandler getBridgeHandler() {
                return pibh;
            }
        };

        List<Channel> channels = new ArrayList<Channel>();

        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHLOR_PROPERTYVERSION)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHLOR_PROPERTYMODEL)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHLOR_SALTOUTPUT)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHLOR_SALINITY)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHLOR_OK)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHLOR_LOWFLOW)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHLOR_LOWSALT)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHLOR_VERYLOWSALT)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHLOR_HIGHCURRENT)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHLOR_CLEANCELL)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHLOR_LOWVOLTAGE)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHLOR_LOWWATERTEMP)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHLOR_COMMERROR)).build());

        when(thing.getConfiguration()).thenReturn(new Configuration());
        when(thing.getUID()).thenReturn(thingUID);
        when(thing.getHandler()).thenReturn(handler);
        when(thing.getChannels()).thenReturn(channels);

        handler.setCallback(callback);
    }

    @AfterEach
    public void tearDown() throws Exception {
        handler.dispose();
    }

    @Test
    public void test() {
        handler.initialize();

        verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));
        assertThat(handler.getPentairID(), equalTo(0));

        PentairIntelliChlorPacket p = new PentairIntelliChlorPacket(packets[0], packets[0].length);
        handler.processPacketFrom(p);
        ChannelUID cuid = new ChannelUID(new ThingUID("1:2:3"), CHANNEL_INTELLICHLOR_SALTOUTPUT);
        verify(callback, times(1)).stateUpdated(cuid, new QuantityType<Dimensionless>(80, Units.PERCENT));

        p = new PentairIntelliChlorPacket(packets[1], packets[1].length);
        handler.processPacketFrom(p);

        // Won't actually go ONLINE until a packet FROM the intellichlor
        verify(callback, times(1)).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));
        cuid = new ChannelUID(new ThingUID("1:2:3"), CHANNEL_INTELLICHLOR_SALINITY);
        verify(callback, times(1)).stateUpdated(cuid, new QuantityType<Dimensionless>(5150, Units.PARTS_PER_MILLION));
        cuid = new ChannelUID(new ThingUID("1:2:3"), CHANNEL_INTELLICHLOR_OK);
        verify(callback, times(1)).stateUpdated(cuid, OnOffType.ON);

        p = new PentairIntelliChlorPacket(packets[2], packets[2].length);
        handler.processPacketFrom(p);

        p = new PentairIntelliChlorPacket(packets[3], packets[3].length);
        handler.processPacketFrom(p);
        cuid = new ChannelUID(new ThingUID("1:2:3"), CHANNEL_INTELLICHLOR_SALTOUTPUT);
        verify(callback, times(1)).stateUpdated(cuid, new QuantityType<Dimensionless>(0, Units.PERCENT));

        p = new PentairIntelliChlorPacket(packets[4], packets[4].length);
        handler.processPacketFrom(p);
        cuid = new ChannelUID(new ThingUID("1:2:3"), CHANNEL_INTELLICHLOR_SALINITY);
        verify(callback, times(1)).stateUpdated(cuid, new QuantityType<Dimensionless>(3800, Units.PARTS_PER_MILLION));
        cuid = new ChannelUID(new ThingUID("1:2:3"), CHANNEL_INTELLICHLOR_OK);
        verify(callback, times(1)).stateUpdated(cuid, OnOffType.OFF);

        cuid = new ChannelUID(new ThingUID("1:2:3"), CHANNEL_INTELLICHLOR_LOWFLOW);
        verify(callback, times(1)).stateUpdated(cuid, OnOffType.ON);

        p = new PentairIntelliChlorPacket(packets[5], packets[5].length);
        handler.processPacketFrom(p);
        assertThat(handler.version, equalTo(0));
        assertThat(handler.name, equalTo("Intellichlor--40"));

        p = new PentairIntelliChlorPacket(packets[6], packets[6].length);
        handler.processPacketFrom(p);
        assertThat(handler.version, equalTo(0));
        assertThat(handler.name, equalTo("Intellichlor--40"));
    }
}
