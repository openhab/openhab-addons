/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.discovery.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.binding.bluetooth.BluetoothAddress;
import org.openhab.binding.bluetooth.BluetoothBindingConstants;
import org.openhab.binding.bluetooth.BluetoothCharacteristic.GattCharacteristic;
import org.openhab.binding.bluetooth.BluetoothDevice;
import org.openhab.binding.bluetooth.MockBluetoothAdapter;
import org.openhab.binding.bluetooth.MockBluetoothDevice;
import org.openhab.binding.bluetooth.TestUtils;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryDevice;
import org.openhab.binding.bluetooth.discovery.BluetoothDiscoveryParticipant;
import org.openhab.binding.bluetooth.notification.BluetoothConnectionStatusNotification;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.util.StringUtils;

/**
 * Tests {@link BluetoothDiscoveryService}.
 *
 * @author Connor Petty - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class BluetoothDiscoveryServiceTest {

    private static final int TIMEOUT = 2000;

    private @NonNullByDefault({}) BluetoothDiscoveryService discoveryService;

    private @Spy @NonNullByDefault({}) MockDiscoveryParticipant participant1 = new MockDiscoveryParticipant();
    private @Mock @NonNullByDefault({}) DiscoveryListener mockDiscoveryListener;

    @BeforeEach
    public void setup() {
        discoveryService = new BluetoothDiscoveryService();
        discoveryService.addDiscoveryListener(mockDiscoveryListener);
        discoveryService.addBluetoothDiscoveryParticipant(participant1);
    }

    @Test
    public void ignoreDuplicateTest() {
        BluetoothAdapter mockAdapter1 = new MockBluetoothAdapter();
        BluetoothDevice device = mockAdapter1.getDevice(TestUtils.randomAddress());
        discoveryService.deviceDiscovered(device);
        // this second call should not produce another result
        discoveryService.deviceDiscovered(device);

        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(1)).thingDiscovered(
                ArgumentMatchers.same(discoveryService),
                ArgumentMatchers.argThat(arg -> arg.getThingTypeUID().equals(participant1.typeUID)));
    }

    @Test
    public void ignoreOtherDuplicateTest() {
        BluetoothAdapter mockAdapter1 = new MockBluetoothAdapter();
        BluetoothAdapter mockAdapter2 = new MockBluetoothAdapter();
        BluetoothAddress address = TestUtils.randomAddress();
        BluetoothDevice device1 = mockAdapter1.getDevice(address);
        BluetoothDevice device2 = mockAdapter2.getDevice(address);
        discoveryService.deviceDiscovered(device1);
        discoveryService.deviceDiscovered(device2);
        // this should not produce another result
        discoveryService.deviceDiscovered(device1);

        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(2)).thingDiscovered(
                ArgumentMatchers.same(discoveryService),
                ArgumentMatchers.argThat(arg -> arg.getThingTypeUID().equals(participant1.typeUID)));
    }

    @Test
    public void ignoreRssiDuplicateTest() {
        MockBluetoothAdapter mockAdapter1 = new MockBluetoothAdapter();
        MockBluetoothDevice device = mockAdapter1.getDevice(TestUtils.randomAddress());
        discoveryService.deviceDiscovered(device);
        // changing the rssi should not result in a new discovery
        device.setRssi(100);
        discoveryService.deviceDiscovered(device);

        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(1)).thingDiscovered(
                ArgumentMatchers.same(discoveryService),
                ArgumentMatchers.argThat(arg -> arg.getThingTypeUID().equals(participant1.typeUID)));
    }

    @Test
    public void nonDuplicateNameTest() throws InterruptedException {
        MockBluetoothAdapter mockAdapter1 = new MockBluetoothAdapter();
        MockBluetoothDevice device = mockAdapter1.getDevice(TestUtils.randomAddress());
        discoveryService.deviceDiscovered(device);
        // this second call should produce another result
        device.setName("sdfad");
        discoveryService.deviceDiscovered(device);

        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(2)).thingDiscovered(
                ArgumentMatchers.same(discoveryService),
                ArgumentMatchers.argThat(arg -> arg.getThingTypeUID().equals(participant1.typeUID)));
    }

    @Test
    public void nonDuplicateTxPowerTest() {
        MockBluetoothAdapter mockAdapter1 = new MockBluetoothAdapter();
        MockBluetoothDevice device = mockAdapter1.getDevice(TestUtils.randomAddress());
        discoveryService.deviceDiscovered(device);
        // this second call should produce another result
        device.setTxPower(10);
        discoveryService.deviceDiscovered(device);

        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(2)).thingDiscovered(
                ArgumentMatchers.same(discoveryService),
                ArgumentMatchers.argThat(arg -> arg.getThingTypeUID().equals(participant1.typeUID)));
    }

    @Test
    public void nonDuplicateManufacturerIdTest() {
        MockBluetoothAdapter mockAdapter1 = new MockBluetoothAdapter();
        MockBluetoothDevice device = mockAdapter1.getDevice(TestUtils.randomAddress());
        discoveryService.deviceDiscovered(device);
        // this second call should produce another result
        device.setManufacturerId(100);
        discoveryService.deviceDiscovered(device);

        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(2)).thingDiscovered(
                ArgumentMatchers.same(discoveryService),
                ArgumentMatchers.argThat(arg -> arg.getThingTypeUID().equals(participant1.typeUID)));
    }

    @Test
    public void useResultFromAnotherAdapterTest() {
        BluetoothAdapter mockAdapter1 = new MockBluetoothAdapter();
        BluetoothAdapter mockAdapter2 = new MockBluetoothAdapter();
        BluetoothAddress address = TestUtils.randomAddress();

        discoveryService.deviceDiscovered(mockAdapter1.getDevice(address));
        discoveryService.deviceDiscovered(mockAdapter2.getDevice(address));

        ArgumentCaptor<DiscoveryResult> resultCaptor = ArgumentCaptor.forClass(DiscoveryResult.class);
        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(2))
                .thingDiscovered(ArgumentMatchers.same(discoveryService), resultCaptor.capture());

        List<DiscoveryResult> results = resultCaptor.getAllValues();
        DiscoveryResult result1 = results.get(0);
        DiscoveryResult result2 = results.get(1);

        assertNotEquals(result1.getBridgeUID(), result2.getBridgeUID());
        assertThat(result1.getBridgeUID(), anyOf(is(mockAdapter1.getUID()), is(mockAdapter2.getUID())));
        assertThat(result2.getBridgeUID(), anyOf(is(mockAdapter1.getUID()), is(mockAdapter2.getUID())));
        assertEquals(result1.getThingUID().getId(), result2.getThingUID().getId());
        assertEquals(result1.getLabel(), result2.getLabel());
        assertEquals(result1.getRepresentationProperty(), result2.getRepresentationProperty());
    }

    @Test
    public void connectionParticipantTest() {
        Mockito.doReturn(true).when(participant1).requiresConnection(ArgumentMatchers.any());
        BluetoothAddress address = TestUtils.randomAddress();

        MockBluetoothAdapter mockAdapter1 = new MockBluetoothAdapter();
        MockBluetoothDevice mockDevice = mockAdapter1.getDevice(address);
        String deviceName = StringUtils.getRandomAlphanumeric(10);
        mockDevice.setDeviceName(deviceName);

        BluetoothDevice device = Mockito.spy(mockDevice);

        discoveryService.deviceDiscovered(device);

        Mockito.verify(device, Mockito.timeout(TIMEOUT).times(1)).connect();
        Mockito.verify(device, Mockito.timeout(TIMEOUT).times(1)).readCharacteristic(
                ArgumentMatchers.argThat(ch -> ch.getGattCharacteristic() == GattCharacteristic.DEVICE_NAME));
        Mockito.verify(device, Mockito.timeout(TIMEOUT).times(1)).disconnect();

        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(1)).thingDiscovered(
                ArgumentMatchers.same(discoveryService),
                ArgumentMatchers.argThat(arg -> arg.getThingTypeUID().equals(participant1.typeUID)
                        && arg.getThingUID().getId().equals(deviceName)));
    }

    @Test
    public void multiDiscoverySingleConnectionTest() {
        Mockito.doReturn(true).when(participant1).requiresConnection(ArgumentMatchers.any());
        BluetoothAddress address = TestUtils.randomAddress();

        MockBluetoothAdapter mockAdapter1 = new MockBluetoothAdapter();
        MockBluetoothAdapter mockAdapter2 = new MockBluetoothAdapter();
        MockBluetoothDevice mockDevice1 = mockAdapter1.getDevice(address);
        MockBluetoothDevice mockDevice2 = mockAdapter2.getDevice(address);
        String deviceName = StringUtils.getRandomAlphanumeric(10);
        mockDevice1.setDeviceName(deviceName);
        mockDevice2.setDeviceName(deviceName);

        BluetoothDevice device1 = Mockito.spy(mockDevice1);
        BluetoothDevice device2 = Mockito.spy(mockDevice2);

        discoveryService.deviceDiscovered(device1);

        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(1)).thingDiscovered(
                ArgumentMatchers.same(discoveryService),
                ArgumentMatchers.argThat(arg -> arg.getThingTypeUID().equals(participant1.typeUID)
                        && mockAdapter1.getUID().equals(arg.getBridgeUID())
                        && arg.getThingUID().getId().equals(deviceName)));

        Mockito.verify(device1, Mockito.times(1)).connect();
        Mockito.verify(device1, Mockito.times(1)).readCharacteristic(
                ArgumentMatchers.argThat(ch -> ch.getGattCharacteristic() == GattCharacteristic.DEVICE_NAME));
        Mockito.verify(device1, Mockito.times(1)).disconnect();

        discoveryService.deviceDiscovered(device2);

        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(1)).thingDiscovered(
                ArgumentMatchers.same(discoveryService),
                ArgumentMatchers.argThat(arg -> arg.getThingTypeUID().equals(participant1.typeUID)
                        && mockAdapter2.getUID().equals(arg.getBridgeUID())
                        && arg.getThingUID().getId().equals(deviceName)));

        Mockito.verify(device2, Mockito.never()).connect();
        Mockito.verify(device2, Mockito.never()).readCharacteristic(
                ArgumentMatchers.argThat(ch -> ch.getGattCharacteristic() == GattCharacteristic.DEVICE_NAME));
        Mockito.verify(device2, Mockito.never()).disconnect();
    }

    @Test
    public void nonConnectionParticipantTest() {
        MockBluetoothAdapter mockAdapter1 = new MockBluetoothAdapter();
        MockBluetoothDevice mockDevice = mockAdapter1.getDevice(TestUtils.randomAddress());
        String deviceName = StringUtils.getRandomAlphanumeric(10);
        mockDevice.setDeviceName(deviceName);

        BluetoothDevice device = Mockito.spy(mockDevice);

        discoveryService.deviceDiscovered(device);

        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(1)).thingDiscovered(
                ArgumentMatchers.same(discoveryService),
                ArgumentMatchers.argThat(arg -> arg.getThingTypeUID().equals(participant1.typeUID)
                        && !arg.getThingUID().getId().equals(deviceName)));
        Mockito.verify(device, Mockito.never()).connect();
        Mockito.verify(device, Mockito.never()).readCharacteristic(
                ArgumentMatchers.argThat(ch -> ch.getGattCharacteristic() == GattCharacteristic.DEVICE_NAME));
        Mockito.verify(device, Mockito.never()).disconnect();
    }

    @Test
    public void defaultResultTest() {
        Mockito.doReturn(null).when(participant1).createResult(ArgumentMatchers.any());
        BluetoothAdapter mockAdapter1 = new MockBluetoothAdapter();
        BluetoothDevice device = mockAdapter1.getDevice(TestUtils.randomAddress());
        discoveryService.deviceDiscovered(device);

        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(1))
                .thingDiscovered(ArgumentMatchers.same(discoveryService), ArgumentMatchers
                        .argThat(arg -> arg.getThingTypeUID().equals(BluetoothBindingConstants.THING_TYPE_BEACON)));
    }

    @Test
    public void removeDefaultDeviceTest() {
        Mockito.doReturn(null).when(participant1).createResult(ArgumentMatchers.any());
        BluetoothAdapter mockAdapter1 = new MockBluetoothAdapter();
        BluetoothDevice device = mockAdapter1.getDevice(TestUtils.randomAddress());
        discoveryService.deviceDiscovered(device);
        discoveryService.deviceRemoved(device);

        ArgumentCaptor<DiscoveryResult> resultCaptor = ArgumentCaptor.forClass(DiscoveryResult.class);
        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(1))
                .thingDiscovered(ArgumentMatchers.same(discoveryService), resultCaptor.capture());

        DiscoveryResult result = resultCaptor.getValue();

        assertEquals(BluetoothBindingConstants.THING_TYPE_BEACON, result.getThingTypeUID());

        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(1)).thingRemoved(
                ArgumentMatchers.same(discoveryService),
                ArgumentMatchers.argThat(arg -> arg.equals(result.getThingUID())));
    }

    @Test
    public void removeUpdatedDefaultDeviceTest() {
        Mockito.doReturn(null).when(participant1).createResult(ArgumentMatchers.any());
        MockBluetoothAdapter mockAdapter1 = new MockBluetoothAdapter();
        MockBluetoothDevice device = mockAdapter1.getDevice(TestUtils.randomAddress());
        discoveryService.deviceDiscovered(device);
        device.setName("somename");
        discoveryService.deviceDiscovered(device);

        ArgumentCaptor<DiscoveryResult> resultCaptor = ArgumentCaptor.forClass(DiscoveryResult.class);
        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(2))
                .thingDiscovered(ArgumentMatchers.same(discoveryService), resultCaptor.capture());

        DiscoveryResult result = resultCaptor.getValue();

        assertEquals(BluetoothBindingConstants.THING_TYPE_BEACON, result.getThingTypeUID());

        discoveryService.deviceRemoved(device);

        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(1)).thingRemoved(
                ArgumentMatchers.same(discoveryService),
                ArgumentMatchers.argThat(arg -> arg.equals(result.getThingUID())));
    }

    @Test
    public void bluezConnectionTimeoutTest() {
        Mockito.doReturn(true).when(participant1).requiresConnection(ArgumentMatchers.any());

        BluetoothAdapter mockAdapter1 = new MockBluetoothAdapter();
        BadConnectionDevice device = new BadConnectionDevice(mockAdapter1, TestUtils.randomAddress(), 100);
        discoveryService.deviceDiscovered(device);

        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(1))
                .thingDiscovered(ArgumentMatchers.same(discoveryService), ArgumentMatchers
                        .argThat(arg -> arg.getThingTypeUID().equals(BluetoothBindingConstants.THING_TYPE_BEACON)));
    }

    @Test
    public void replaceOlderDiscoveryTest() {
        Mockito.doReturn(null).when(participant1).createResult(ArgumentMatchers.any());

        MockBluetoothAdapter mockAdapter1 = new MockBluetoothAdapter();
        MockBluetoothDevice device = mockAdapter1.getDevice(TestUtils.randomAddress());

        MockDiscoveryParticipant participant2 = new MockDiscoveryParticipant() {
            @Override
            public @Nullable DiscoveryResult createResult(BluetoothDiscoveryDevice device) {
                Integer manufacturer = device.getManufacturerId();
                if (manufacturer != null && manufacturer.equals(10)) {
                    // without a device name it should produce a random ThingUID
                    return super.createResult(device);
                }
                return null;
            }
        };

        discoveryService.addBluetoothDiscoveryParticipant(participant2);

        // lets start with producing a default result
        discoveryService.deviceDiscovered(device);

        ArgumentCaptor<DiscoveryResult> resultCaptor = ArgumentCaptor.forClass(DiscoveryResult.class);
        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(1))
                .thingDiscovered(ArgumentMatchers.same(discoveryService), resultCaptor.capture());

        DiscoveryResult result = resultCaptor.getValue();

        assertEquals(BluetoothBindingConstants.THING_TYPE_BEACON, result.getThingTypeUID());

        device.setManufacturerId(10);

        // lets start with producing a default result
        discoveryService.deviceDiscovered(device);

        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(1)).thingRemoved(
                ArgumentMatchers.same(discoveryService),
                ArgumentMatchers.argThat(arg -> arg.equals(result.getThingUID())));

        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(1)).thingDiscovered(
                ArgumentMatchers.same(discoveryService),
                ArgumentMatchers.argThat(arg -> arg.getThingTypeUID().equals(participant2.typeUID)));
    }

    @Test
    public void recursiveFutureTest() throws InterruptedException {
        /*
         * 1. deviceDiscovered(device1)
         * 2. cause discovery to pause at participant1
         * participant1 should make a field non-null for device1 upon unpause
         * 3. make the same field non-null for device2
         * 4. deviceDiscovered(device2)
         * this discovery should be waiting for first discovery to finish
         * 5. unpause participant
         * End result:
         * - participant should only have been called once
         * - thingDiscovered should have been called twice
         */
        Mockito.doReturn(null).when(participant1).createResult(ArgumentMatchers.any());

        AtomicInteger callCount = new AtomicInteger(0);
        final CountDownLatch pauseLatch = new CountDownLatch(1);

        BluetoothAddress address = TestUtils.randomAddress();
        MockBluetoothAdapter mockAdapter1 = new MockBluetoothAdapter();
        MockBluetoothAdapter mockAdapter2 = new MockBluetoothAdapter();
        MockBluetoothDevice mockDevice1 = mockAdapter1.getDevice(address);
        MockBluetoothDevice mockDevice2 = mockAdapter2.getDevice(address);
        String deviceName = StringUtils.getRandomAlphanumeric(10);

        MockDiscoveryParticipant participant2 = new MockDiscoveryParticipant() {
            @Override
            public @Nullable DiscoveryResult createResult(BluetoothDiscoveryDevice device) {
                try {
                    pauseLatch.await();
                } catch (InterruptedException e) {
                    // do nothing
                }
                ((BluetoothDeviceSnapshot) device).setName(deviceName);
                callCount.incrementAndGet();
                return super.createResult(device);
            }
        };

        discoveryService.addBluetoothDiscoveryParticipant(participant2);

        discoveryService.deviceDiscovered(mockDevice1);
        mockDevice2.setName(deviceName);
        discoveryService.deviceDiscovered(mockDevice2);

        pauseLatch.countDown();

        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(2)).thingDiscovered(
                ArgumentMatchers.same(discoveryService),
                ArgumentMatchers.argThat(arg -> arg.getThingTypeUID().equals(participant2.typeUID)));

        assertEquals(1, callCount.get());
    }

    @Test
    public void roamingDiscoveryTest() {
        RoamingDiscoveryParticipant roamingParticipant = new RoamingDiscoveryParticipant();
        MockBluetoothAdapter roamingAdapter = roamingParticipant.roamingAdapter;
        discoveryService.addBluetoothDiscoveryParticipant(roamingParticipant);

        BluetoothAdapter mockAdapter1 = new MockBluetoothAdapter();
        BluetoothDevice device = mockAdapter1.getDevice(TestUtils.randomAddress());
        discoveryService.deviceDiscovered(device);

        ArgumentCaptor<DiscoveryResult> resultCaptor = ArgumentCaptor.forClass(DiscoveryResult.class);
        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(2))
                .thingDiscovered(ArgumentMatchers.same(discoveryService), resultCaptor.capture());

        List<DiscoveryResult> results = resultCaptor.getAllValues();
        DiscoveryResult result1 = results.get(0);
        DiscoveryResult result2 = results.get(1);

        assertNotEquals(result1.getBridgeUID(), result2.getBridgeUID());
        assertThat(result1.getBridgeUID(), anyOf(is(mockAdapter1.getUID()), is(roamingAdapter.getUID())));
        assertThat(result2.getBridgeUID(), anyOf(is(mockAdapter1.getUID()), is(roamingAdapter.getUID())));
        assertEquals(result1.getThingUID().getId(), result2.getThingUID().getId());
        assertEquals(result1.getLabel(), result2.getLabel());
        assertEquals(result1.getRepresentationProperty(), result2.getRepresentationProperty());
    }

    @Test
    public void roamingDiscoveryRetractionTest() {
        RoamingDiscoveryParticipant roamingParticipant = new RoamingDiscoveryParticipant();
        MockBluetoothAdapter roamingAdapter = roamingParticipant.roamingAdapter;
        discoveryService.addBluetoothDiscoveryParticipant(roamingParticipant);

        MockBluetoothAdapter mockAdapter1 = new MockBluetoothAdapter();
        MockBluetoothDevice device = mockAdapter1.getDevice(TestUtils.randomAddress());
        discoveryService.deviceDiscovered(device);
        device.setName("dasf");
        discoveryService.deviceDiscovered(device);

        ArgumentCaptor<ThingUID> resultCaptor = ArgumentCaptor.forClass(ThingUID.class);
        Mockito.verify(mockDiscoveryListener, Mockito.timeout(TIMEOUT).times(2))
                .thingRemoved(ArgumentMatchers.same(discoveryService), resultCaptor.capture());

        List<ThingUID> results = resultCaptor.getAllValues();
        ThingUID result1 = results.get(0);
        ThingUID result2 = results.get(1);

        assertNotEquals(result1.getBridgeIds(), result2.getBridgeIds());
        assertThat(result1.getBridgeIds().get(0),
                anyOf(is(mockAdapter1.getUID().getId()), is(roamingAdapter.getUID().getId())));
        assertThat(result2.getBridgeIds().get(0),
                anyOf(is(mockAdapter1.getUID().getId()), is(roamingAdapter.getUID().getId())));
        assertEquals(result1.getId(), result2.getId());
    }

    private class RoamingDiscoveryParticipant implements BluetoothDiscoveryParticipant {

        private MockBluetoothAdapter roamingAdapter = new MockBluetoothAdapter();

        @Override
        public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
            return Collections.emptySet();
        }

        @Override
        public @Nullable DiscoveryResult createResult(BluetoothDiscoveryDevice device) {
            return null;
        }

        @Override
        public @Nullable ThingUID getThingUID(BluetoothDiscoveryDevice device) {
            return null;
        }

        @Override
        public void publishAdditionalResults(DiscoveryResult result,
                BiConsumer<BluetoothAdapter, DiscoveryResult> publisher) {
            publisher.accept(roamingAdapter, result);
        }
    }

    private class MockDiscoveryParticipant implements BluetoothDiscoveryParticipant {

        private ThingTypeUID typeUID;

        public MockDiscoveryParticipant() {
            this.typeUID = new ThingTypeUID(BluetoothBindingConstants.BINDING_ID, StringUtils.getRandomAlphabetic(6));
        }

        @Override
        public Set<ThingTypeUID> getSupportedThingTypeUIDs() {
            return Set.of(typeUID);
        }

        @Override
        public @Nullable DiscoveryResult createResult(BluetoothDiscoveryDevice device) {
            String repProp = StringUtils.getRandomAlphabetic(6);
            ThingUID thingUID = getThingUID(device);
            if (thingUID == null) {
                return null;
            }
            return DiscoveryResultBuilder.create(thingUID).withLabel(StringUtils.getRandomAlphabetic(6))
                    .withProperty(repProp, StringUtils.getRandomAlphabetic(6)).withRepresentationProperty(repProp)
                    .withBridge(device.getAdapter().getUID()).build();
        }

        @Override
        public @Nullable ThingUID getThingUID(BluetoothDiscoveryDevice device) {
            String deviceName = device.getName();
            String id = deviceName != null ? deviceName : StringUtils.getRandomAlphabetic(6);
            return new ThingUID(typeUID, device.getAdapter().getUID(), id);
        }
    }

    private class BadConnectionDevice extends MockBluetoothDevice {

        private int sleepTime;

        public BadConnectionDevice(BluetoothAdapter adapter, BluetoothAddress address, int sleepTime) {
            super(adapter, address);
            this.sleepTime = sleepTime;
        }

        @Override
        public boolean connect() {
            notifyListeners(BluetoothEventType.CONNECTION_STATE,
                    new BluetoothConnectionStatusNotification(ConnectionState.CONNECTED));
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                // do nothing
            }
            notifyListeners(BluetoothEventType.CONNECTION_STATE,
                    new BluetoothConnectionStatusNotification(ConnectionState.DISCONNECTED));
            return false;
        }
    }
}
