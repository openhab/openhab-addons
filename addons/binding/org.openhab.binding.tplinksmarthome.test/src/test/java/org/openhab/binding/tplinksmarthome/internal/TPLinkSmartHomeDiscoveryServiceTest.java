/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.config.discovery.DiscoveryListener;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openhab.binding.tplinksmarthome.TPLinkSmartHomeBindingConstants;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;

/**
 * Test class for {@link TPLinkSmartHomeDiscoveryService} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@RunWith(value = Parameterized.class)
public class TPLinkSmartHomeDiscoveryServiceTest {

    private static final List<Object[]> TESTS = Arrays.asList(
            new Object[][] { { "bulb_get_sysinfo_response_on", 11 }, { "rangeextender_get_sysinfo_response", 11 } });

    @Mock
    private DatagramSocket discoverSocket;

    @Mock
    private DiscoveryListener discoveryListener;

    private TPLinkSmartHomeDiscoveryService discoveryService;

    private final String filename;
    private final int propertiesSize;

    public TPLinkSmartHomeDiscoveryServiceTest(String filename, int propertiesSize) {
        this.filename = filename;
        this.propertiesSize = propertiesSize;
    }

    @Parameters(name = "{0}")
    public static List<Object[]> data() {
        return TESTS;
    }

    @Before
    public void setUp() throws IOException {
        initMocks(this);
        discoveryService = new TPLinkSmartHomeDiscoveryService() {
            @Override
            protected DatagramSocket sendDiscoveryPacket() throws IOException {
                return discoverSocket;
            }
        };
        doAnswer(new Answer<Void>() {
            private int cnt;

            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                if (cnt++ > 0) {
                    throw new SocketTimeoutException("Only test 1 thing discovery");
                }
                DatagramPacket packet = (DatagramPacket) invocation.getArguments()[0];
                packet.setAddress(InetAddress.getLocalHost());
                packet.setData(CryptUtil.encrypt(ModelTestUtil.readJson(filename)));
                return null;
            }

        }).when(discoverSocket).receive(any());
        discoveryService.addDiscoveryListener(discoveryListener);
    }

    /**
     * Test if startScan method finds a device with expected properties.
     */
    @Test
    public void testScan() {
        discoveryService.startScan();
        ArgumentCaptor<DiscoveryResult> discoveryResultCaptor = ArgumentCaptor.forClass(DiscoveryResult.class);
        verify(discoveryListener).thingDiscovered(any(), discoveryResultCaptor.capture());
        DiscoveryResult discoveryResult = discoveryResultCaptor.getValue();
        assertEquals("Check if correct binding id found", TPLinkSmartHomeBindingConstants.BINDING_ID,
                discoveryResult.getBindingId());
        assertEquals("Check if expected number of properties found", propertiesSize,
                discoveryResult.getProperties().size());
    }

}
