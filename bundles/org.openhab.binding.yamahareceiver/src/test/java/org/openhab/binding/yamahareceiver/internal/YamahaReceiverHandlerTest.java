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
package org.openhab.binding.yamahareceiver.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.binding.yamahareceiver.internal.config.YamahaBridgeConfig;
import org.openhab.binding.yamahareceiver.internal.discovery.ZoneDiscoveryService;
import org.openhab.binding.yamahareceiver.internal.handler.YamahaBridgeHandler;
import org.openhab.binding.yamahareceiver.internal.protocol.ConnectionStateListener;
import org.openhab.binding.yamahareceiver.internal.protocol.DeviceInformation;
import org.openhab.binding.yamahareceiver.internal.protocol.ProtocolFactory;
import org.openhab.binding.yamahareceiver.internal.protocol.SystemControl;
import org.openhab.binding.yamahareceiver.internal.protocol.xml.AbstractXMLProtocolTest;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Test cases for {@link YamahaBridgeHandler}. The tests provide mocks for supporting entities using Mockito.
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public class YamahaReceiverHandlerTest extends AbstractXMLProtocolTest {

    private YamahaBridgeHandler subject;

    private @Mock YamahaBridgeConfig bridgeConfig;
    private @Mock Configuration configuration;
    private @Mock ProtocolFactory protocolFactory;
    private @Mock DeviceInformation deviceInformation;
    private @Mock SystemControl systemControl;
    private @Mock ThingHandlerCallback callback;
    private @Mock Bridge bridge;

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        ctx.prepareForModel(TestModels.RX_S601D);
        ctx.respondWith("<Main_Zone><Input><Input_Sel_Item>GetParam</Input_Sel_Item></Input></Main_Zone>",
                "Main_Zone_Input_Input_Sel.xml");

        when(bridgeConfig.getHostWithPort()).thenReturn(Optional.of("localhost:80"));
        when(bridgeConfig.getInputMapping()).thenReturn("");
        when(bridgeConfig.getRefreshInterval()).thenReturn(10);

        when(configuration.as(YamahaBridgeConfig.class)).thenReturn(bridgeConfig);
        when(bridge.getConfiguration()).thenReturn(configuration);

        when(protocolFactory.DeviceInformation(any(), any())).thenReturn(deviceInformation);
        when(protocolFactory.SystemControl(any(), any(), any())).thenReturn(systemControl);

        subject = new YamahaBridgeHandler(bridge);
        subject.setZoneDiscoveryService(mock(ZoneDiscoveryService.class));
        subject.setProtocolFactory(protocolFactory);
        subject.setCallback(callback);

        doAnswer(a -> {
            ((ConnectionStateListener) a.getArgument(1)).onConnectionCreated(ctx.getConnection());
            return null;
        }).when(protocolFactory).createConnection(anyString(), same(subject));
    }

    @Test
    public void afterInitializeBridgeShouldBeOnline() throws InterruptedException {
        // when
        subject.initialize();
        // internally there is a timer, let's allow it to execute
        Thread.sleep(200);

        // then
        ArgumentCaptor<ThingStatusInfo> statusInfoCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(same(bridge), statusInfoCaptor.capture());

        List<ThingStatusInfo> thingStatusInfo = statusInfoCaptor.getAllValues();
        // the first one will be OFFLINE
        assertThat(thingStatusInfo.get(0).getStatus(), is(equalTo(ThingStatus.OFFLINE)));
        // depending on the internal timer, several status updates and especially the last one will be ONLINE
        assertThat(thingStatusInfo.get(thingStatusInfo.size() - 1).getStatus(), is(equalTo(ThingStatus.ONLINE)));
    }
}
