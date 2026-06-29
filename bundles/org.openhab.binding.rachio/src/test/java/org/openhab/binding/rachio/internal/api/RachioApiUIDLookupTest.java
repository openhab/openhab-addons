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
package org.openhab.binding.rachio.internal.api;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_DEV_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PROPERTY_ZONE_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_CLOUD;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_DEVICE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_ZONE;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudDevice;
import org.openhab.binding.rachio.internal.api.json.RachioZoneGsonDTO.RachioCloudZone;
import org.openhab.core.thing.ThingUID;

/**
 * Tests for Rachio Thing UID mapping across API model rebuilds.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
class RachioApiUIDLookupTest {
    private static final ThingUID BRIDGE_UID = new ThingUID(THING_TYPE_CLOUD, "bridge");

    @Test
    void getDevByUIDMatchesCanonicalThingUID() {
        RachioDevice device = device("device-id", "ABCDEF123456", "serial-number");
        RachioApi api = apiWithDevice(device);
        ThingUID thingUID = new ThingUID(THING_TYPE_DEVICE, BRIDGE_UID, "ABCDEF123456");

        RachioDevice foundDevice = api.getDevByUID(BRIDGE_UID, thingUID);

        assertThat(foundDevice, is(sameInstance(device)));
        assertThat(device.getUID(), is(thingUID));
    }

    @Test
    void bindDeviceByRachioIdMatchesRandomThingUID() {
        RachioDevice device = device("811aea42-2bf5-4761-9f97-900108d6f04e", "009D6BC04DAC", "serial-number");
        RachioApi api = apiWithDevice(device);
        ThingUID customThingUID = new ThingUID(THING_TYPE_DEVICE, BRIDGE_UID, "809c1a9736");

        RachioDevice foundDevice = api.bindDeviceByRachioId(BRIDGE_UID, customThingUID, device.id);

        assertThat(foundDevice, is(sameInstance(device)));
        assertThat(device.getUID(), is(customThingUID));
    }

    @Test
    void bindDeviceByRachioIdReturnsNullWhenConfiguredDeviceIdIsInvalid() {
        RachioDevice device = device("device-id", "ABCDEF123456", "serial-number");
        RachioApi api = apiWithDevice(device);
        ThingUID customThingUID = new ThingUID(THING_TYPE_DEVICE, BRIDGE_UID, "809c1a9736");

        RachioDevice foundDevice = api.bindDeviceByRachioId(BRIDGE_UID, customThingUID, "wrong-device-id");

        assertThat(foundDevice, is(nullValue()));
    }

    @Test
    void getDevByUIDMatchesConfiguredDeviceIdWithCanonicalDiscoveryThingUID() {
        RachioDevice device = device("811aea42-2bf5-4761-9f97-900108d6f04e", "009D6BC04DAC", "serial-number");
        RachioApi api = apiWithDevice(device);
        ThingUID thingUID = new ThingUID(THING_TYPE_DEVICE, BRIDGE_UID, "009D6BC04DAC");

        RachioDevice foundDevice = api.getDevByUID(BRIDGE_UID, thingUID,
                Map.<String, @Nullable Object> of(PROPERTY_DEV_ID, device.id), Collections.emptyMap());

        assertThat(foundDevice, is(sameInstance(device)));
        assertThat(device.getUID(), is(thingUID));
    }

    @Test
    void getDevByUIDMatchesConfiguredDeviceIdWithArbitraryThingUID() {
        RachioDevice device = device("811aea42-2bf5-4761-9f97-900108d6f04e", "009D6BC04DAC", "serial-number");
        RachioApi api = apiWithDevice(device);
        ThingUID customThingUID = new ThingUID(THING_TYPE_DEVICE, BRIDGE_UID, "a283238ad8");

        RachioDevice foundDevice = api.getDevByUID(BRIDGE_UID, customThingUID,
                Map.<String, @Nullable Object> of(PROPERTY_DEV_ID, device.id), Collections.emptyMap());

        assertThat(foundDevice, is(sameInstance(device)));
        assertThat(device.getUID(), is(customThingUID));
    }

    @Test
    void getDevByUIDFallsBackToPersistedDeviceIdProperty() {
        RachioDevice device = device("device-id", "ABCDEF123456", "serial-number");
        RachioApi api = apiWithDevice(device);
        ThingUID legacyThingUID = new ThingUID(THING_TYPE_DEVICE, BRIDGE_UID, "legacy-device-id");

        RachioDevice foundDevice = api.getDevByUID(BRIDGE_UID, legacyThingUID, Map.of(PROPERTY_DEV_ID, "device-id"));

        assertThat(foundDevice, is(sameInstance(device)));
        assertThat(device.getUID(), is(legacyThingUID));
    }

    @Test
    void getDevByUIDDoesNotUseLegacyFallbackWhenConfiguredDeviceIdIsInvalid() {
        RachioDevice device = device("device-id", "ABCDEF123456", "serial-number");
        RachioApi api = apiWithDevice(device);
        ThingUID thingUID = new ThingUID(THING_TYPE_DEVICE, BRIDGE_UID, "ABCDEF123456");

        RachioDevice foundDevice = api.getDevByUID(BRIDGE_UID, thingUID,
                Map.<String, @Nullable Object> of(PROPERTY_DEV_ID, "wrong-device-id"), Collections.emptyMap());

        assertThat(foundDevice, is(nullValue()));
    }

    @Test
    void getZoneByUIDMatchesCanonicalThingUID() {
        RachioCloudZone cloudZone = zone("zone-id", 3);
        RachioDevice device = device("device-id", "ABCDEF123456", "serial-number", cloudZone);
        RachioZone zone = Objects.requireNonNull(device.getZones().get("zone-id"));
        RachioApi api = apiWithDevice(device);
        ThingUID zoneUID = new ThingUID(THING_TYPE_ZONE, BRIDGE_UID, zone.getThingID());

        RachioZone foundZone = api.getZoneByUID(BRIDGE_UID, zoneUID);

        assertThat(foundZone, is(sameInstance(zone)));
        assertThat(zone.getUID(), is(zoneUID));
    }

    @Test
    void getZoneByUIDMatchesConfiguredZoneIdWithArbitraryThingUID() {
        RachioCloudZone cloudZone = zone("zone-id", 3);
        RachioDevice device = device("device-id", "ABCDEF123456", "serial-number", cloudZone);
        RachioZone zone = Objects.requireNonNull(device.getZones().get("zone-id"));
        RachioApi api = apiWithDevice(device);
        ThingUID customZoneUID = new ThingUID(THING_TYPE_ZONE, BRIDGE_UID, "custom-zone-id");

        RachioZone foundZone = api.getZoneByUID(BRIDGE_UID, customZoneUID,
                Map.<String, @Nullable Object> of(PROPERTY_ZONE_ID, "zone-id"), Collections.emptyMap());

        assertThat(foundZone, is(sameInstance(zone)));
        assertThat(zone.getUID(), is(customZoneUID));
    }

    @Test
    void getZoneByUIDPreservesManuallyBoundControllerUID() {
        RachioCloudZone cloudZone = zone("zone-id", 3);
        RachioDevice device = device("device-id", "ABCDEF123456", "serial-number", cloudZone);
        RachioZone zone = Objects.requireNonNull(device.getZones().get("zone-id"));
        RachioApi api = apiWithDevice(device);
        ThingUID manualDeviceUID = new ThingUID(THING_TYPE_DEVICE, BRIDGE_UID, "manual-controller");
        ThingUID customZoneUID = new ThingUID(THING_TYPE_ZONE, BRIDGE_UID, "custom-zone-id");
        device.setUID(BRIDGE_UID, manualDeviceUID);

        RachioZone foundZone = api.getZoneByUID(BRIDGE_UID, customZoneUID,
                Map.<String, @Nullable Object> of(PROPERTY_ZONE_ID, "zone-id"), Collections.emptyMap());

        assertThat(foundZone, is(sameInstance(zone)));
        assertThat(device.getUID(), is(manualDeviceUID));
        assertThat(zone.getDevUID(), is(manualDeviceUID));
        assertThat(zone.getUID(), is(customZoneUID));
    }

    @Test
    void getZoneByUIDDoesNotUseLegacyFallbackWhenConfiguredZoneIdIsInvalid() {
        RachioCloudZone cloudZone = zone("zone-id", 3);
        RachioDevice device = device("device-id", "ABCDEF123456", "serial-number", cloudZone);
        RachioZone zone = Objects.requireNonNull(device.getZones().get("zone-id"));
        RachioApi api = apiWithDevice(device);
        ThingUID zoneUID = new ThingUID(THING_TYPE_ZONE, BRIDGE_UID, zone.getThingID());

        RachioZone foundZone = api.getZoneByUID(BRIDGE_UID, zoneUID,
                Map.<String, @Nullable Object> of(PROPERTY_ZONE_ID, "wrong-zone-id"), Collections.emptyMap());

        assertThat(foundZone, is(nullValue()));
    }

    @Test
    void getZoneByUIDFallsBackToPersistedZoneIdProperty() {
        RachioCloudZone cloudZone = zone("zone-id", 3);
        RachioDevice device = device("device-id", "ABCDEF123456", "serial-number", cloudZone);
        RachioZone zone = Objects.requireNonNull(device.getZones().get("zone-id"));
        RachioApi api = apiWithDevice(device);
        ThingUID legacyZoneUID = new ThingUID(THING_TYPE_ZONE, BRIDGE_UID, "legacy-zone-id");

        RachioZone foundZone = api.getZoneByUID(BRIDGE_UID, legacyZoneUID, Map.of(PROPERTY_ZONE_ID, "zone-id"));

        assertThat(foundZone, is(sameInstance(zone)));
        assertThat(zone.getUID(), is(legacyZoneUID));
    }

    @Test
    void getDevByUIDReturnsNullWithoutMatchingUIDOrProperties() {
        RachioDevice device = device("device-id", "ABCDEF123456", "serial-number");
        RachioApi api = apiWithDevice(device);
        ThingUID legacyThingUID = new ThingUID(THING_TYPE_DEVICE, BRIDGE_UID, "legacy-device-id");

        RachioDevice foundDevice = api.getDevByUID(BRIDGE_UID, legacyThingUID, Collections.emptyMap());

        assertThat(foundDevice, is(nullValue()));
    }

    private RachioApi apiWithDevice(RachioDevice device) {
        RachioApi api = new RachioApi("");
        api.getDevices().put(device.id, device);
        return api;
    }

    private RachioDevice device(String id, String macAddress, String serialNumber, RachioCloudZone... zones) {
        RachioCloudDevice cloudDevice = new RachioCloudDevice();
        cloudDevice.id = id;
        cloudDevice.name = "Test Controller";
        cloudDevice.macAddress = macAddress;
        cloudDevice.serialNumber = serialNumber;
        cloudDevice.zones.addAll(java.util.List.of(zones));
        return new RachioDevice(cloudDevice);
    }

    private RachioCloudZone zone(String id, int zoneNumber) {
        RachioCloudZone zone = new RachioCloudZone();
        zone.id = id;
        zone.name = "Test Zone";
        zone.zoneNumber = zoneNumber;
        return zone;
    }
}
