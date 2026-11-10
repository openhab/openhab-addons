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
package org.openhab.binding.tailwind.internal.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.Inet4Address;
import java.net.InetAddress;

import javax.jmdns.ServiceInfo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingUID;

/**
 * Tests for {@link TailwindDiscoveryParticipant}.
 *
 * @author Christopher Smith - Initial contribution
 */
@NonNullByDefault
public class TailwindDiscoveryParticipantTest {
    private final TailwindDiscoveryParticipant participant = new TailwindDiscoveryParticipant();
    private final ServiceInfo service = mock(ServiceInfo.class);

    @BeforeEach
    public void setUp() throws Exception {
        when(service.getPropertyString("vendor")).thenReturn("tailwind");
        when(service.getPropertyString("product")).thenReturn("iQ3");
        when(service.getPropertyString("device_id")).thenReturn("_24_6f_28_86_7d_80_");
        when(service.getPropertyString("SW ver")).thenReturn("10.80");
        when(service.getPropertyString("HW ver")).thenReturn("V1.3");
        when(service.getServer()).thenReturn("tailwind-246f28867d80.local.");
        when(service.getInet4Addresses()).thenReturn(new Inet4Address[] {
                (Inet4Address) InetAddress.getByAddress(new byte[] { (byte) 192, (byte) 168, 1, 50 }) });
    }

    @Test
    public void controllerIsDiscoveredWithItsAddressAndProperties() {
        DiscoveryResult result = participant.createResult(service);

        assertNotNull(result);
        assertEquals(new ThingUID("tailwind", "iq3", "246f28867d80"), result.getThingUID());
        assertEquals("192.168.1.50", result.getProperties().get("hostname"));
        assertEquals("246f28867d80", result.getProperties().get("macAddress"));
        assertEquals("10.80", result.getProperties().get("firmwareVersion"));
        assertEquals("V1.3", result.getProperties().get("hardwareVersion"));
        assertEquals("macAddress", result.getRepresentationProperty());
        assertEquals("TailWind iQ3", result.getLabel());
    }

    @Test
    public void hostNameIsUsedWhenTheServiceHasNoAddress() {
        when(service.getInet4Addresses()).thenReturn(new Inet4Address[0]);

        DiscoveryResult result = participant.createResult(service);

        assertNotNull(result);
        assertEquals("tailwind-246f28867d80.local", result.getProperties().get("hostname"));
    }

    @Test
    public void otherDevicesAreIgnored() {
        when(service.getPropertyString("vendor")).thenReturn("acme");
        assertNull(participant.getThingUID(service));
        assertNull(participant.createResult(service));

        when(service.getPropertyString("vendor")).thenReturn("tailwind");
        when(service.getPropertyString("product")).thenReturn("light");
        assertNull(participant.createResult(service));

        when(service.getPropertyString("product")).thenReturn("iQ3");
        when(service.getPropertyString("device_id")).thenReturn(null);
        assertNull(participant.createResult(service));
    }
}
