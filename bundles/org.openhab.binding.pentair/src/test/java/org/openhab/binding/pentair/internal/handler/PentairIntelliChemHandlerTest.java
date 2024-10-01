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
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.pentair.internal.parser.PentairStandardPacket;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;

/**
 * PentairIntelliChemHandlerTest
 *
 * @author Jeff James - Initial contribution
 *
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class PentairIntelliChemHandlerTest {

    //@formatter:off
    public static byte[][] packets = {
            parsehex("A50010901229030202A302D002C60000000000000000000000000006070000C8003F005A3C00580006A5201E01000000"),
            parsehex("A5100F10122902E302AF02EE02BC000000020000002A0004005C060518019000000096140051000065203C0100000000")
    };
    //@formatter:on

    private @NonNullByDefault({}) PentairIntelliChemHandler pich;
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

        pich = new PentairIntelliChemHandler(thing) {
            @Override
            public @NonNull PentairBaseBridgeHandler getBridgeHandler() {
                return pibh;
            }
        };

        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_PHREADING)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_ORPREADING)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_PHSETPOINT)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_ORPSETPOINT)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_TANK1LEVEL)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_TANK2LEVEL)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_CALCIUMHARDNESS)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_CYAREADING)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_ALKALINITY)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_PHDOSERTYPE)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_ORPDOSERTYPE)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_PHDOSERSTATUS)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_ORPDOSERSTATUS)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_PHDOSETIME)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_ORPDOSETIME)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_LSI)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_SALTLEVEL)).build());

        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_ALARMWATERFLOW)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_ALARMPH)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_ALARMORP)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_ALARMPHTANK)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_ALARMORPTANK)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_ALARMPROBEFAULT)).build());

        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_WARNINGPHLOCKOUT)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_WARNINGPHDAILYLIMITREACHED))
                .build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_WARNINGORPDAILYLIMITREACHED))
                .build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_WARNINGINVALIDSETUP)).build());
        channels.add(ChannelBuilder.create(new ChannelUID(thingUID, CHANNEL_INTELLICHEM_WARNINGCHLORINATORCOMMERROR))
                .build());

        when(thing.getConfiguration()).thenReturn(new Configuration(Collections.singletonMap("id", 144)));
        when(thing.getHandler()).thenReturn(pich);
        when(thing.getChannels()).thenReturn(channels);
        pich.setCallback(callback);
    }

    @AfterEach
    public void tearDown() throws Exception {
        pich.dispose();
    }

    @Test
    public void test() {
        pich.initialize();

        PentairStandardPacket p = new PentairStandardPacket(packets[0], packets[0].length);

        verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.UNKNOWN)));

        pich.processPacketFrom(p);

        verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

        ChannelUID cuid = new ChannelUID(thingUID, CHANNEL_INTELLICHEM_PHREADING);
        verify(callback, times(1)).stateUpdated(cuid, new DecimalType(7.7));

        cuid = new ChannelUID(thingUID, CHANNEL_INTELLICHEM_ORPREADING);
        verify(callback, times(1)).stateUpdated(cuid, new DecimalType(675));
    }
}
