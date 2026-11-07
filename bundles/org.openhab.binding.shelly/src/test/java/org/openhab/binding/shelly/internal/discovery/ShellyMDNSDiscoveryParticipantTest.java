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
package org.openhab.binding.shelly.internal.discovery;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.Hashtable;
import java.util.stream.Stream;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.NetworkAddressService;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;

/**
 * Tests for {@link ShellyMDNSDiscoveryParticipantTest}.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
public class ShellyMDNSDiscoveryParticipantTest {

    private static final String SERVICE_NAME = "shellyplussmoke-666edfe225ed";
    private static final String IP_ADDRESS = "192.168.1.17";

    @Test
    void createResultNotifiesThingOfAnnouncedDevice() throws Exception {
        ShellyThingTable thingTable = mock(ShellyThingTable.class);
        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        when(thingTable.findAnnouncedThing(SERVICE_NAME, IP_ADDRESS)).thenReturn(thing);

        createParticipant(thingTable, mock(DiscoveryResult.class)).createResult(mockService());

        verify(thing).checkRepresentation(IP_ADDRESS);
        verify(thing).onDeviceWakeup();
    }

    @Test
    void createResultIgnoresAnnouncementWhenDeviceDoesNotAnswer() throws Exception {
        ShellyThingTable thingTable = mock(ShellyThingTable.class);
        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        when(thingTable.findAnnouncedThing(SERVICE_NAME, IP_ADDRESS)).thenReturn(thing);

        assertThat(createParticipant(thingTable, null).createResult(mockService()), is(nullValue()));

        verify(thingTable, never()).findAnnouncedThing(anyString(), anyString());
        verify(thing, never()).onDeviceWakeup();
    }

    @Test
    void createResultIgnoresThingOfOtherDeviceAtSameIpAddress() throws Exception {
        ShellyThingTable thingTable = mock(ShellyThingTable.class);
        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        when(thingTable.findThing(IP_ADDRESS)).thenReturn(thing);

        createParticipant(thingTable, mock(DiscoveryResult.class)).createResult(mockService());

        verify(thing, never()).onDeviceWakeup();
    }

    @Test
    void createResultForUnknownDeviceReturnsResult() throws Exception {
        ShellyThingTable thingTable = mock(ShellyThingTable.class);
        DiscoveryResult result = mock(DiscoveryResult.class);

        assertThat(createParticipant(thingTable, result).createResult(mockService()), is(result));

        verify(thingTable).findAnnouncedThing(SERVICE_NAME, IP_ADDRESS);
    }

    private static ServiceInfo mockService() throws IOException {
        ServiceInfo service = mock(ServiceInfo.class);
        when(service.getName()).thenReturn(SERVICE_NAME);
        when(service.getInet4Addresses())
                .thenReturn(new Inet4Address[] { (Inet4Address) InetAddress.getByName(IP_ADDRESS) });
        when(service.getPropertyString("gen")).thenReturn("2");
        return service;
    }

    private static ShellyMDNSDiscoveryParticipant createParticipant(ShellyThingTable thingTable,
            @Nullable DiscoveryResult discoveryResult) throws Exception {
        HttpClient httpClient = mock(HttpClient.class);
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        when(httpClientFactory.getCommonHttpClient()).thenReturn(httpClient);
        ConfigurationAdmin configurationAdmin = mock(ConfigurationAdmin.class);
        when(configurationAdmin.getConfiguration(anyString())).thenReturn(mock(Configuration.class));
        ComponentContext componentContext = mock(ComponentContext.class);
        when(componentContext.getProperties()).thenReturn(new Hashtable<String, Object>());
        return new ShellyMDNSDiscoveryParticipant(configurationAdmin, httpClientFactory, mock(LocaleProvider.class),
                mock(ShellyTranslationProvider.class), thingTable, mock(NetworkAddressService.class),
                componentContext) {
            @Override
            protected @Nullable DiscoveryResult discoverDevice(boolean gen2, String serviceName, String address,
                    ShellyBindingRuntimeConfig cfg) {
                return discoveryResult;
            }
        };
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForIsValidShellyServiceName")
    void isValidShellyServiceName(String serviceName, boolean expected) {
        assertThat("serviceName: " + serviceName, ShellyMDNSDiscoveryParticipant.isValidShellyServiceName(serviceName),
                is(expected));
    }

    private static Stream<Arguments> provideTestCasesForIsValidShellyServiceName() {
        return Stream.of( //
                Arguments.of("shellypmmini-123456789012", true), //
                Arguments.of("ShellyPlusPMMini-Test", true), //
                Arguments.of("shelly1-ABC", true), //
                Arguments.of("ShellyOne-001", true), //
                Arguments.of("MyShelly-001", true), //
                Arguments.of("my-shelly", false), //
                Arguments.of("shelly_one-001", false), //
                Arguments.of("shelly-", false), //
                Arguments.of("shelly 1-001", false), //
                Arguments.of("shelly1-001!", false), //
                Arguments.of("shell-001", false), //
                Arguments.of("ShellyPlusPMMini", false), //
                Arguments.of("ShellyPlusPMMini - Test", false));
    }
}
