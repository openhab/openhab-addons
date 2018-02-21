/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.yamahareceiver.handler;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants;
import org.openhab.binding.yamahareceiver.YamahaReceiverBindingConstants.Zone;
import org.openhab.binding.yamahareceiver.discovery.YamahaDiscoveryParticipant;
import org.openhab.binding.yamahareceiver.discovery.ZoneDiscoveryService;
import org.openhab.binding.yamahareceiver.internal.protocol.ReceivedMessageParseException;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLConnection;

/**
 * Tests cases for {@link YamahaZoneThingHandler}.
 *
 * @author David Graeff - Initial contribution
 */
public class ZoneHandlerTest {
    private final static int LOCAL_PORT = 12312;
    private YamahaZoneThingHandlerExt handler;

    @Mock
    private ThingHandlerCallback callback;

    @Mock
    private Thing thing;

    @Mock
    private Bridge bridge;

    @Mock
    Channel channel;

    @Mock
    YamahaBridgeHandler bridgeHandler;

    EmulatedYamahaReceiver emulatedReceiver;

    @Before
    public void setUp() throws InterruptedException, IOException, ReceivedMessageParseException {
        initMocks(this);

        emulatedReceiver = new EmulatedYamahaReceiver(LOCAL_PORT);
        assertTrue(emulatedReceiver.waitForStarted(1000));

        ThingUID bridgeUID = YamahaDiscoveryParticipant.getThingUID(YamahaReceiverBindingConstants.UPNP_MANUFACTURER,
                YamahaReceiverBindingConstants.UPNP_TYPE, "test");
        when(thing.getUID()).thenReturn(ZoneDiscoveryService.zoneThing(bridgeUID, Zone.Main_Zone.name()));
        when(thing.getBridgeUID()).thenReturn(bridgeUID);

        // For isLinked, we need thing.getChannel()
        when(thing.getChannel(anyObject())).thenReturn(channel);

        when(bridge.getUID()).thenReturn(bridgeUID);
        when(bridge.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, ""));
        when(bridgeHandler.getThing()).thenReturn(bridge);

        XMLConnection xmlConnection = new XMLConnection("127.0.0.1:" + String.valueOf(LOCAL_PORT));
        when(bridgeHandler.getCommunication()).thenReturn(xmlConnection);

        Configuration configuration = new Configuration();
        configuration.put(YamahaReceiverBindingConstants.CONFIG_ZONE, Zone.Main_Zone.name());
        when(thing.getConfiguration()).thenReturn(configuration);

        handler = spy(new YamahaZoneThingHandlerExt(thing, bridgeHandler));
        handler.setCallback(callback);
    }

    @After
    public void tearDown() {
        emulatedReceiver.destroy();
    }

    @Test
    public void loadAllStatesTest() throws InterruptedException {
        // This will update states synchronously, because we overwrote updateAsyncMakeOfflineIfFail
        handler.initialize();

        assertNull(handler.updateException);

        assertThat(handler.zoneState.inputID, is("NET_RADIO"));
        assertThat(handler.zoneState.inputName, is(""));
        assertThat(handler.zoneState.mute, is(false));
        assertThat(handler.zoneState.power, is(true));
        assertThat(handler.zoneState.surroundProgram, is("7ch Stereo"));
        assertThat(handler.zoneState.volume, is(97.82609f));

        assertThat(handler.playInfoState.album, is("TestAlbum"));
        assertThat(handler.playInfoState.artist, is("TestArtist"));
        assertThat(handler.playInfoState.playbackMode, is("Play"));
        assertThat(handler.playInfoState.song, is("TestSong"));
        assertThat(handler.playInfoState.station, is("TestStation"));

        assertThat(handler.navigationInfoState.currentLine, is(1));
        assertThat(handler.navigationInfoState.items[0], is("Eintrag1"));
        assertThat(handler.navigationInfoState.maxLine, is(1));
        assertThat(handler.navigationInfoState.menuLayer, is(1));
        assertThat(handler.navigationInfoState.menuName, is("Testname"));

        assertThat(handler.presetInfoState.presetChannel, is(1));
        assertThat(handler.presetInfoState.presetChannelNames.length, is(40));
        assertThat(handler.presetInfoState.presetChannelNamesChanged, is(false));

        assertThat(handler.channelsTypeProviderAvailableInputs.getChannelTypeUID().getId(),
                is(YamahaReceiverBindingConstants.CHANNEL_INPUT_TYPE_AVAILABLE + thing.getUID().getId()));
        assertThat(handler.channelsTypeProviderPreset.getChannelTypeUID().getId(),
                is(YamahaReceiverBindingConstants.CHANNEL_PLAYBACK_PRESET_TYPE_NAMED + thing.getUID().getId()));

        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, times(2)).statusUpdated(eq(thing), statusInfoCaptor.capture());
        // assert that the ThingStatusInfo given to the callback was build with the ONLINE status:
        ThingStatusInfo thingStatusInfo = statusInfoCaptor.getValue();
        Assert.assertThat(thingStatusInfo.getStatus(), is(equalTo(ThingStatus.ONLINE)));

        handler.dispose();
    }
}
