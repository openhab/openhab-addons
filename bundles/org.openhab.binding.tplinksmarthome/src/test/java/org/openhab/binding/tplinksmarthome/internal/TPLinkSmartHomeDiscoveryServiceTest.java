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
package org.openhab.binding.tplinksmarthome.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;

/**
 * Test class for {@link TPLinkSmartHomeDiscoveryService} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
public class TPLinkSmartHomeDiscoveryServiceTest {

    private static final List<Object[]> TESTS = Arrays.asList(
            new Object[][] { { "bulb_get_sysinfo_response_on", 11 }, { "rangeextender_get_sysinfo_response", 11 } });

    private @Mock DatagramSocket discoverSocket;
    private @Mock DiscoveryListener discoveryListener;

    private TPLinkSmartHomeDiscoveryService discoveryService;

    public static List<Object[]> data() {
        return TESTS;
    }

    public void setUp(String filename) throws IOException {
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
     *
     * @throws IOException
     */
    @ParameterizedTest
    @MethodSource("data")
    public void testScan(String filename, int propertiesSize) throws IOException {
        setUp(filename);
        discoveryService.startScan();
        ArgumentCaptor<DiscoveryResult> discoveryResultCaptor = ArgumentCaptor.forClass(DiscoveryResult.class);
        verify(discoveryListener).thingDiscovered(any(), discoveryResultCaptor.capture());
        DiscoveryResult discoveryResult = discoveryResultCaptor.getValue();
        assertEquals(TPLinkSmartHomeBindingConstants.BINDING_ID, discoveryResult.getBindingId(),
                "Check if correct binding id found");
        assertEquals(propertiesSize, discoveryResult.getProperties().size(),
                "Check if expected number of properties found");
    }
}
