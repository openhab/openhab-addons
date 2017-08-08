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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.math.BigDecimal;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
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
import org.openhab.binding.yamahareceiver.internal.protocol.xml.XMLConnection;

/**
 * Tests cases for {@link YamahaBridgeHandler}.
 *
 * @author David Graeff - Initial contribution
 */
public class BridgeHandlerTest {
    private final static int LOCAL_PORT = 12312;
    private YamahaBridgeHandler handler;

    @Mock
    private ThingHandlerCallback callback;

    @Mock
    private Bridge thing;

    EmulatedYamahaReceiver emulatedReceiver;

    @Before
    public void setUp() throws InterruptedException {
        initMocks(this);

        emulatedReceiver = new EmulatedYamahaReceiver(LOCAL_PORT);
        assertTrue(emulatedReceiver.waitForStarted(1000));

        ThingUID thingUID = YamahaDiscoveryParticipant.getThingUID(YamahaReceiverBindingConstants.UPNP_MANUFACTURER,
                YamahaReceiverBindingConstants.UPNP_TYPE, "test");
        when(thing.getUID()).thenReturn(thingUID);

        Configuration configuration = new Configuration();
        configuration.put(YamahaReceiverBindingConstants.CONFIG_HOST_NAME, "127.0.0.1");
        configuration.put(YamahaReceiverBindingConstants.CONFIG_HOST_PORT, new BigDecimal(LOCAL_PORT));
        when(thing.getConfiguration()).thenReturn(configuration);

        handler = new YamahaBridgeHandler(thing);
        handler.setCallback(callback);
    }

    @After
    public void tearDown() {
        emulatedReceiver.destroy();
    }

    @Test
    public void loadAllStatesTest() throws InterruptedException {
        handler.initialize();

        assertTrue(handler.getCommunication() instanceof XMLConnection);
        assertTrue(handler.waitForLoadingDone(1200000));

        assertThat(handler.deviceInformationState.zones.size(), is(2));
        assertThat(handler.deviceInformationState.zones.get(0), is(Zone.Main_Zone));
        assertThat(handler.deviceInformationState.zones.get(1), is(Zone.Zone_2));
        assertThat(handler.deviceInformationState.name, is("Test AVR"));
        assertThat(handler.deviceInformationState.version, is("1.0"));
        assertThat(handler.deviceInformationState.id, is("1234"));
        assertThat(handler.deviceInformationState.host, is("127.0.0.1:" + String.valueOf(LOCAL_PORT)));

        assertThat(handler.systemControlState.power, is(true));

        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, times(2)).statusUpdated(eq(thing), statusInfoCaptor.capture());
        // assert that the ThingStatusInfo given to the callback was build with the ONLINE status:
        ThingStatusInfo thingStatusInfo = statusInfoCaptor.getValue();
        Assert.assertThat(thingStatusInfo.getStatus(), is(equalTo(ThingStatus.ONLINE)));
    }

}
