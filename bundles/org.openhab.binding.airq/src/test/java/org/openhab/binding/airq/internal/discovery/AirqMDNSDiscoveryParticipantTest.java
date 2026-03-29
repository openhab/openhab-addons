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
package org.openhab.binding.airq.internal.discovery;

import static org.eclipse.jdt.annotation.Checks.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.airq.internal.AirqBindingConstants.CONFIG_IP_ADDRESS;
import static org.openhab.binding.airq.internal.AirqBindingConstants.THING_TYPE_AIRQ;

import java.net.Inet4Address;
import java.net.InetAddress;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingUID;

/**
 * Tests for {@link AirqMDNSDiscoveryParticipant}.
 *
 * @author Renat Sibgatulin - Initial contribution
 */
@NonNullByDefault
public class AirqMDNSDiscoveryParticipantTest {

    @SuppressWarnings("unused")
    private @NonNullByDefault({}) AirqMDNSDiscoveryParticipant participant;

    @SuppressWarnings("unused")
    private @NonNullByDefault({}) ServiceInfo serviceInfo;

    @BeforeEach
    public void setUp() {
        participant = new AirqMDNSDiscoveryParticipant();
        serviceInfo = mock(ServiceInfo.class);
    }

    @Test
    public void testGetServiceType() {
        assertThat(participant.getServiceType(), is("_http._tcp.local."));
    }

    @Test
    public void testGetSupportedThingTypeUIDs() {
        assertThat(participant.getSupportedThingTypeUIDs(), contains(THING_TYPE_AIRQ));
    }

    @Test
    public void testGetThingUIDForAirQDevice() {
        when(serviceInfo.getPropertyString("device")).thenReturn("air-q");
        when(serviceInfo.getPropertyString("id")).thenReturn("abc123def456");

        ThingUID uid = requireNonNull(participant.getThingUID(serviceInfo));

        assertThat(uid, is(new ThingUID(THING_TYPE_AIRQ, "abc123def456")));
    }

    @Test
    public void testGetThingUIDForAirQDeviceCaseInsensitive() {
        when(serviceInfo.getPropertyString("device")).thenReturn("Air-Q");
        when(serviceInfo.getPropertyString("id")).thenReturn("abc123def456");

        ThingUID uid = requireNonNull(participant.getThingUID(serviceInfo));

        assertThat(uid, is(new ThingUID(THING_TYPE_AIRQ, "abc123def456")));
    }

    @Test
    public void testGetThingUIDReturnsNullForNonAirQDevice() {
        when(serviceInfo.getPropertyString("device")).thenReturn("other-device");

        assertThat(participant.getThingUID(serviceInfo), is(nullValue()));
    }

    @Test
    public void testGetThingUIDReturnsNullWhenDevicePropertyMissing() {
        when(serviceInfo.getPropertyString("device")).thenReturn(null);

        assertThat(participant.getThingUID(serviceInfo), is(nullValue()));
    }

    @Test
    public void testGetThingUIDReturnsNullWhenIdMissing() {
        when(serviceInfo.getPropertyString("device")).thenReturn("air-q");
        when(serviceInfo.getPropertyString("id")).thenReturn(null);

        assertThat(participant.getThingUID(serviceInfo), is(nullValue()));
    }

    @Test
    public void testGetThingUIDReturnsNullWhenIdBlank() {
        when(serviceInfo.getPropertyString("device")).thenReturn("air-q");
        when(serviceInfo.getPropertyString("id")).thenReturn("  ");

        assertThat(participant.getThingUID(serviceInfo), is(nullValue()));
    }

    @Test
    public void testCreateResultForAirQDevice() throws Exception {
        when(serviceInfo.getPropertyString("device")).thenReturn("air-q");
        when(serviceInfo.getPropertyString("id")).thenReturn("abc123def456");
        when(serviceInfo.getPropertyString("devicename")).thenReturn("Living Room");
        when(serviceInfo.getQualifiedName()).thenReturn("airq._http._tcp.local.");
        when(serviceInfo.getInet4Addresses())
                .thenReturn(new Inet4Address[] { (Inet4Address) InetAddress.getByName("192.168.1.42") });

        DiscoveryResult result = requireNonNull(participant.createResult(serviceInfo));

        assertThat(result.getThingUID(), is(new ThingUID(THING_TYPE_AIRQ, "abc123def456")));
        assertThat(result.getLabel(), is("air-Q (Living Room)"));
        assertThat(result.getProperties(), hasEntry(CONFIG_IP_ADDRESS, "192.168.1.42"));
        assertThat(result.getRepresentationProperty(), is(CONFIG_IP_ADDRESS));
    }

    @Test
    public void testCreateResultWithoutDeviceName() throws Exception {
        when(serviceInfo.getPropertyString("device")).thenReturn("air-q");
        when(serviceInfo.getPropertyString("id")).thenReturn("abc123def456");
        when(serviceInfo.getPropertyString("devicename")).thenReturn(null);
        when(serviceInfo.getQualifiedName()).thenReturn("airq._http._tcp.local.");
        when(serviceInfo.getInet4Addresses())
                .thenReturn(new Inet4Address[] { (Inet4Address) InetAddress.getByName("192.168.1.42") });

        DiscoveryResult result = requireNonNull(participant.createResult(serviceInfo));

        assertThat(result.getLabel(), is("air-Q"));
    }

    @Test
    public void testCreateResultReturnsNullForNonAirQDevice() {
        when(serviceInfo.getPropertyString("device")).thenReturn("other-device");

        assertThat(participant.createResult(serviceInfo), is(nullValue()));
    }

    @Test
    public void testCreateResultReturnsNullWhenNoIPv4Address() {
        when(serviceInfo.getPropertyString("device")).thenReturn("air-q");
        when(serviceInfo.getPropertyString("id")).thenReturn("abc123def456");
        when(serviceInfo.getQualifiedName()).thenReturn("airq._http._tcp.local.");
        when(serviceInfo.getInet4Addresses()).thenReturn(new Inet4Address[] {});

        assertThat(participant.createResult(serviceInfo), is(nullValue()));
    }

    @Test
    public void testCreateResultReturnsNullWhenFirstIPv4AddressIsNull() {
        when(serviceInfo.getPropertyString("device")).thenReturn("air-q");
        when(serviceInfo.getPropertyString("id")).thenReturn("abc123def456");
        when(serviceInfo.getQualifiedName()).thenReturn("airq._http._tcp.local.");
        when(serviceInfo.getInet4Addresses()).thenReturn(new Inet4Address[] { null });

        assertThat(participant.createResult(serviceInfo), is(nullValue()));
    }
}
