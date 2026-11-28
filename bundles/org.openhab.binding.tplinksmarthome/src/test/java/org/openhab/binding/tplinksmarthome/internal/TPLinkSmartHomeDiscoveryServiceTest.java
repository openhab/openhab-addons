/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.util.SameThreadExecutorService;

/**
 * Test class for {@link TPLinkSmartHomeDiscoveryService} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 * @author Christopher Smith - Discovery of devices on secured firmware
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TPLinkSmartHomeDiscoveryServiceTest {

    private static final List<Object[]> TESTS = Arrays.asList(
            new Object[][] { { "bulb_get_sysinfo_response_on", 11 }, { "rangeextender_get_sysinfo_response", 11 } });

    private @Mock DatagramSocket discoverSocket;
    private @Mock DiscoveryListener discoveryListener;
    private @Mock ThingRegistry thingRegistry;

    private TPLinkSmartHomeDiscoveryService discoveryService;

    public static List<Object[]> data() {
        return TESTS;
    }

    public void setUp(String filename) throws IOException {
        setUpReply(CryptUtil.encrypt(ModelTestUtil.readJson(filename)), -1);
    }

    private void setUpReply(byte[] data, int port) throws IOException {
        when(thingRegistry.stream()).thenAnswer(invocation -> Stream.empty());
        discoveryService = new TPLinkSmartHomeDiscoveryService(new SameThreadExecutorService(), thingRegistry) {
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
                packet.setAddress(InetAddress.getByName("192.168.1.20"));
                if (port > 0) {
                    packet.setPort(port);
                }
                packet.setData(data);
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

    private void setUpKlapReply(String deviceType) throws IOException {
        setUpReply(KlapDiscoveryTest.replyPacket(KlapDiscoveryTest.reply(deviceType, "KLAP")), KlapDiscovery.PORT);
    }

    private void withThings(Thing... things) {
        when(thingRegistry.stream()).thenAnswer(invocation -> Stream.of(things));
    }

    private static Thing thing(String thingUid, Map<String, Object> configuration) {
        String[] segments = thingUid.split(":");
        return ThingBuilder.create(new ThingTypeUID(segments[0], segments[1]), new ThingUID(thingUid))
                .withConfiguration(new Configuration(configuration)).build();
    }

    @Test
    public void testDiscoversDeviceOnSecuredFirmware() throws IOException {
        setUpKlapReply("IOT.SMARTPLUGSWITCH");
        withThings(thing("tplinkbridge:account:home", Map.of()));

        discoveryService.startScan();

        ArgumentCaptor<DiscoveryResult> captor = ArgumentCaptor.forClass(DiscoveryResult.class);
        verify(discoveryListener).thingDiscovered(any(), captor.capture());
        DiscoveryResult result = captor.getValue();
        assertEquals(new ThingUID("tplinksmarthome:ep10:ACE081"), result.getThingUID());
        assertEquals("192.168.1.20", result.getProperties().get(TPLinkSmartHomeBindingConstants.CONFIG_IP));
        assertEquals("tplinkbridge:account:home",
                result.getProperties().get(TPLinkSmartHomeBindingConstants.CONFIG_ACCOUNT));
        assertNull(result.getProperties().get(TPLinkSmartHomeBindingConstants.CONFIG_DEVICE_ID));
        assertEquals("192.168.1.20", discoveryService.getLastKnownIpAddress(KlapDiscoveryTest.DEVICE_ID));
    }

    @Test
    public void testLeavesAccountOpenWhenThereAreSeveral() throws IOException {
        setUpKlapReply("IOT.SMARTPLUGSWITCH");
        withThings(thing("tplinkbridge:account:home", Map.of()), thing("tplinkbridge:account:cabin", Map.of()));

        discoveryService.startScan();

        ArgumentCaptor<DiscoveryResult> captor = ArgumentCaptor.forClass(DiscoveryResult.class);
        verify(discoveryListener).thingDiscovered(any(), captor.capture());
        assertFalse(captor.getValue().getProperties().containsKey(TPLinkSmartHomeBindingConstants.CONFIG_ACCOUNT));
    }

    @Test
    public void testSkipsDeviceThatHasAThing() throws IOException {
        setUpKlapReply("IOT.SMARTPLUGSWITCH");
        withThings(thing("tplinksmarthome:ep10:lamp", Map.of(TPLinkSmartHomeBindingConstants.CONFIG_DEVICE_ID,
                KlapDiscoveryTest.DEVICE_ID, TPLinkSmartHomeBindingConstants.CONFIG_IP, "192.168.1.99")));

        discoveryService.startScan();

        verify(discoveryListener, never()).thingDiscovered(any(), any());
        // The address is still tracked, so the Thing follows the device to a new address
        assertEquals("192.168.1.20", discoveryService.getLastKnownIpAddress(KlapDiscoveryTest.DEVICE_ID));
    }

    @Test
    public void testSkipsDeviceAtTheAddressOfAThing() throws IOException {
        setUpKlapReply("IOT.SMARTPLUGSWITCH");
        withThings(
                thing("tplinksmarthome:ep10:lamp", Map.of(TPLinkSmartHomeBindingConstants.CONFIG_IP, "192.168.1.20")));

        discoveryService.startScan();

        verify(discoveryListener, never()).thingDiscovered(any(), any());
    }

    @Test
    public void testIgnoresDeviceOnSmartProtocol() throws IOException {
        setUpKlapReply("SMART.KASAPLUG");

        discoveryService.startScan();

        verify(discoveryListener, never()).thingDiscovered(any(), any());
    }
}
