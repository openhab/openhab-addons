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
package org.openhab.binding.philipsair.internal.discovery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.*;

import java.net.URI;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.jupnp.model.meta.DeviceDetails;
import org.jupnp.model.meta.ManufacturerDetails;
import org.jupnp.model.meta.ModelDetails;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteDeviceIdentity;
import org.jupnp.model.types.UDN;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.philipsair.internal.PhilipsAirConfiguration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * Test cases for {@link PhilipsAirUpnpDiscoveryParticipant}. Covers recognition of things based on received UPnP info.
 *
 * @author michalboronski - Initial contribution
 * @author Marcel Verpaalen - Re-enable tests and cover model numbers with region suffix
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PhilipsAirUpnpDiscoveryParticipantTest {

    private static final String UDN_ID = "12345678-1234-1234-1234-e8c1d7007123";

    private @Mock @NonNullByDefault({}) RemoteDevice device;
    private @Mock @NonNullByDefault({}) DeviceDetails deviceDetails;
    private @Mock @NonNullByDefault({}) RemoteDeviceIdentity remoteDeviceIdentity;
    private @Mock @NonNullByDefault({}) ModelDetails modelDetails;

    private final PhilipsAirUpnpDiscoveryParticipant participant = new PhilipsAirUpnpDiscoveryParticipant();

    @BeforeEach
    public void setUp() {
        when(device.getDetails()).thenReturn(deviceDetails);
        when(device.getIdentity()).thenReturn(remoteDeviceIdentity);
        when(deviceDetails.getModelDetails()).thenReturn(modelDetails);
        when(modelDetails.getModelName()).thenReturn("AirPurifier");
        when(remoteDeviceIdentity.getUdn()).thenReturn(new UDN(UDN_ID));
    }

    @ParameterizedTest
    @CsvSource({ "AC2889, ac2889-10", "AC2889/10, ac2889-10", "AC3829, ac3829-10", "AC3829/10, ac3829-10",
            "AC1214, ac1214-10", "AC2729, ac2729", "AC2729/50, ac2729", "AC3829/50, universal", "AC3333, universal" })
    public void thingTypeIsDerivedFromModelNumber(String modelNumber, String expectedThingTypeId) {
        when(modelDetails.getModelNumber()).thenReturn(modelNumber);

        ThingUID thingUID = participant.getThingUID(device);

        assertEquals(new ThingUID(new ThingTypeUID(BINDING_ID, expectedThingTypeId), UDN_ID), thingUID);
    }

    @Test
    public void missingModelNumberIsUniversal() {
        ThingUID expected = new ThingUID(THING_TYPE_UNIVERSAL, UDN_ID);

        when(modelDetails.getModelNumber()).thenReturn(null);
        assertEquals(expected, participant.getThingUID(device));

        when(modelDetails.getModelNumber()).thenReturn("");
        assertEquals(expected, participant.getThingUID(device));
    }

    @Test
    public void otherDevicesAreIgnored() {
        when(modelDetails.getModelName()).thenReturn("MediaRenderer");
        assertNull(participant.getThingUID(device));

        when(modelDetails.getModelName()).thenReturn(null);
        assertNull(participant.getThingUID(device));
    }

    private void stubResultDetails() throws Exception {
        ManufacturerDetails manufacturerDetails = mock(ManufacturerDetails.class);
        when(manufacturerDetails.getManufacturer()).thenReturn("Philips");
        when(deviceDetails.getManufacturerDetails()).thenReturn(manufacturerDetails);
        when(remoteDeviceIdentity.getDescriptorURL()).thenReturn(new URI("http://192.168.1.50:80/desc.xml").toURL());
    }

    @Test
    public void deviceWithoutIdentityOrIdentifierHasNoThingUID() {
        when(modelDetails.getModelNumber()).thenReturn("AC2889/10");

        when(remoteDeviceIdentity.getUdn()).thenReturn(new UDN((String) null));
        assertNull(participant.getThingUID(device));

        when(remoteDeviceIdentity.getUdn()).thenReturn(null);
        assertNull(participant.getThingUID(device));

        when(device.getIdentity()).thenReturn(null);
        assertNull(participant.getThingUID(device));
    }

    @Test
    public void resultIsCreatedForPurifier() throws Exception {
        stubResultDetails();
        when(modelDetails.getModelNumber()).thenReturn("AC2889/10");

        DiscoveryResult result = participant.createResult(device);

        assertNotNull(result);
        assertEquals(new ThingUID(THING_TYPE_AC2889_10, UDN_ID), result.getThingUID());
        assertEquals(PhilipsAirConfiguration.CONFIG_DEF_DEVICE_UUID, result.getRepresentationProperty());
        assertEquals("Philips AirPurifier AC2889/10", result.getLabel());
        Map<String, Object> properties = result.getProperties();
        assertEquals("192.168.1.50", properties.get(PhilipsAirConfiguration.CONFIG_HOST));
        assertEquals(UDN_ID, properties.get(PhilipsAirConfiguration.CONFIG_DEF_DEVICE_UUID));
        assertEquals("e8c1d7007123", properties.get(Thing.PROPERTY_MAC_ADDRESS));
        assertEquals("Philips", properties.get(PROPERTY_MANUFACTURER));
        assertEquals(VENDOR, properties.get(Thing.PROPERTY_VENDOR));
        assertEquals("AC2889/10", properties.get(Thing.PROPERTY_MODEL_ID));
        assertEquals("AirPurifier", properties.get(PROPERTY_DEV_TYPE));
    }

    @Test
    public void noResultForOtherDevices() {
        when(modelDetails.getModelName()).thenReturn("MediaRenderer");

        assertNull(participant.createResult(device));
    }

    @Test
    public void noResultWhenBackgroundDiscoveryIsDisabled() throws Exception {
        stubResultDetails();
        when(modelDetails.getModelNumber()).thenReturn("AC2889/10");
        participant.activate(Map.of(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, false));
        assertNull(participant.createResult(device));

        participant.modified(Map.of(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, true));
        assertNotNull(participant.createResult(device));
    }
}
