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
package org.openhab.binding.mielecloud.internal.webservice;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mielecloud.internal.util.MockUtil.mockDevice;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.webservice.api.ActionsState;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Actions;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Device;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceCollection;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class DeviceStateDispatcherTest {
    private static final String FIRST_DEVICE_IDENTIFIER = "000124430016";
    private static final String SECOND_DEVICE_IDENTIFIER = "000124430017";
    private static final String UNKNOWN_DEVICE_IDENTIFIER = "100124430016";

    @Nullable
    private Device firstDevice;
    @Nullable
    private Device secondDevice;
    @Nullable
    private DeviceCollection devices;

    private Device getFirstDevice() {
        assertNotNull(firstDevice);
        return Objects.requireNonNull(firstDevice);
    }

    private Device getSecondDevice() {
        assertNotNull(secondDevice);
        return Objects.requireNonNull(secondDevice);
    }

    private DeviceCollection getDevices() {
        assertNotNull(devices);
        return Objects.requireNonNull(devices);
    }

    @BeforeEach
    public void setUp() {
        firstDevice = mockDevice(FIRST_DEVICE_IDENTIFIER);
        secondDevice = mockDevice(SECOND_DEVICE_IDENTIFIER);

        devices = mock(DeviceCollection.class);
        when(getDevices().getDeviceIdentifiers())
                .thenReturn(new HashSet<String>(Arrays.asList(FIRST_DEVICE_IDENTIFIER, SECOND_DEVICE_IDENTIFIER)));
        when(getDevices().getDevice(FIRST_DEVICE_IDENTIFIER)).thenReturn(getFirstDevice());
        when(getDevices().getDevice(SECOND_DEVICE_IDENTIFIER)).thenReturn(getSecondDevice());
    }

    @Test
    public void testAddListenerDispatchesStateUpdatesToPassedListenerForCachedDevices()
            throws InterruptedException, TimeoutException, ExecutionException {
        // given:
        DeviceStateListener listener = mock(DeviceStateListener.class);

        DeviceStateDispatcher dispatcher = new DeviceStateDispatcher();
        dispatcher.dispatchDeviceStateUpdates(getDevices());

        // when:
        dispatcher.addListener(listener);

        // then:
        verify(listener).onDeviceStateUpdated(new DeviceState(FIRST_DEVICE_IDENTIFIER, firstDevice));
        verify(listener).onDeviceStateUpdated(new DeviceState(SECOND_DEVICE_IDENTIFIER, secondDevice));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testDeviceStateUpdatesAreNotDispatchedToRemovedListeners() {
        // given:
        DeviceStateListener listener = mock(DeviceStateListener.class);

        DeviceStateDispatcher dispatcher = new DeviceStateDispatcher();
        dispatcher.addListener(listener);

        // when:
        dispatcher.removeListener(listener);
        dispatcher.dispatchDeviceStateUpdates(getDevices());

        // then:
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testClearCachePreventsDeviceStateUpdateDispatchingOnListenerRegistration() {
        // given:
        DeviceStateListener listener = mock(DeviceStateListener.class);

        DeviceStateDispatcher dispatcher = new DeviceStateDispatcher();
        dispatcher.dispatchDeviceStateUpdates(getDevices());

        // when:
        dispatcher.clearCache();
        dispatcher.addListener(listener);

        // then:
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testDeviceStateUpdatesAreDispatchedToSubscribedListeners() {
        // given:
        DeviceStateListener listener = mock(DeviceStateListener.class);

        DeviceStateDispatcher dispatcher = new DeviceStateDispatcher();
        dispatcher.addListener(listener);

        // when:
        dispatcher.dispatchDeviceStateUpdates(getDevices());

        // then:
        verify(listener).onDeviceStateUpdated(new DeviceState(FIRST_DEVICE_IDENTIFIER, firstDevice));
        verify(listener).onDeviceStateUpdated(new DeviceState(SECOND_DEVICE_IDENTIFIER, secondDevice));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testRemovalEventsAreDispatchedToSubscribedListeners()
            throws InterruptedException, TimeoutException, ExecutionException {
        // given:
        DeviceStateListener listener = mock(DeviceStateListener.class);

        Device deviceWithUnknownIdentifier = mockDevice(UNKNOWN_DEVICE_IDENTIFIER);
        DeviceCollection devicesWithUnknownDevice = mock(DeviceCollection.class);
        when(devicesWithUnknownDevice.getDeviceIdentifiers())
                .thenReturn(new HashSet<String>(Arrays.asList(UNKNOWN_DEVICE_IDENTIFIER)));
        when(devicesWithUnknownDevice.getDevice(UNKNOWN_DEVICE_IDENTIFIER)).thenReturn(deviceWithUnknownIdentifier);

        DeviceStateDispatcher dispatcher = new DeviceStateDispatcher();
        dispatcher.dispatchDeviceStateUpdates(devicesWithUnknownDevice);
        dispatcher.clearCache();
        dispatcher.addListener(listener);

        // when:
        dispatcher.dispatchDeviceStateUpdates(getDevices());

        // then:
        verify(listener).onDeviceRemoved(UNKNOWN_DEVICE_IDENTIFIER);
        verify(listener, times(2)).onDeviceStateUpdated(any());
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testRemovalEventsAreDispatchedToSubscribedListenersMatchingAllDeviceIds()
            throws InterruptedException, TimeoutException, ExecutionException {
        // given:
        DeviceStateListener listener = mock(DeviceStateListener.class);

        Device deviceWithUnknownIdentifier = mockDevice(UNKNOWN_DEVICE_IDENTIFIER);
        DeviceCollection devicesWithUnknownDevice = mock(DeviceCollection.class);
        when(devicesWithUnknownDevice.getDeviceIdentifiers())
                .thenReturn(new HashSet<String>(Arrays.asList(UNKNOWN_DEVICE_IDENTIFIER)));
        when(devicesWithUnknownDevice.getDevice(UNKNOWN_DEVICE_IDENTIFIER)).thenReturn(deviceWithUnknownIdentifier);

        DeviceCollection emptyDevices = mock(DeviceCollection.class);
        when(emptyDevices.getDeviceIdentifiers()).thenReturn(new HashSet<String>());

        DeviceStateDispatcher dispatcher = new DeviceStateDispatcher();
        dispatcher.dispatchDeviceStateUpdates(devicesWithUnknownDevice);
        dispatcher.clearCache();
        dispatcher.addListener(listener);

        // when:
        dispatcher.dispatchDeviceStateUpdates(emptyDevices);

        // then:
        verify(listener).onDeviceRemoved(UNKNOWN_DEVICE_IDENTIFIER);
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testDeviceEventDispatchingForSubscribedListenersWithAnyDeviceIdFilter()
            throws InterruptedException, TimeoutException, ExecutionException {
        // given:
        DeviceStateListener listener = mock(DeviceStateListener.class);

        DeviceStateDispatcher dispatcher = new DeviceStateDispatcher();
        dispatcher.addListener(listener);

        // when:
        dispatcher.dispatchDeviceStateUpdates(getDevices());

        // then:
        verify(listener).onDeviceStateUpdated(new DeviceState(FIRST_DEVICE_IDENTIFIER, firstDevice));
        verify(listener).onDeviceStateUpdated(new DeviceState(SECOND_DEVICE_IDENTIFIER, secondDevice));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testActionsEventDispatchingForSubscribedListeners()
            throws InterruptedException, TimeoutException, ExecutionException {
        // given:
        DeviceStateListener listener = mock(DeviceStateListener.class);
        Actions actions = mock(Actions.class);

        DeviceStateDispatcher dispatcher = new DeviceStateDispatcher();
        dispatcher.addListener(listener);

        // when:
        dispatcher.dispatchActionStateUpdates(FIRST_DEVICE_IDENTIFIER, actions);

        // then:
        verify(listener).onProcessActionUpdated(new ActionsState(FIRST_DEVICE_IDENTIFIER, actions));
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void testDeviceStateDispatcherDispatchesDeviceStatesAndActions() {
        // given:
        DeviceStateListener listener = mock(DeviceStateListener.class);
        Actions actions = mock(Actions.class);

        DeviceStateDispatcher dispatcher = new DeviceStateDispatcher();
        dispatcher.addListener(listener);

        dispatcher.dispatchDeviceStateUpdates(getDevices());
        dispatcher.dispatchActionStateUpdates(FIRST_DEVICE_IDENTIFIER, actions);

        // when:
        dispatcher.dispatchDeviceState(FIRST_DEVICE_IDENTIFIER);

        // then:
        verify(listener, times(2)).onDeviceStateUpdated(new DeviceState(FIRST_DEVICE_IDENTIFIER, firstDevice));
        verify(listener).onDeviceStateUpdated(new DeviceState(SECOND_DEVICE_IDENTIFIER, secondDevice));
        verify(listener).onProcessActionUpdated(new ActionsState(FIRST_DEVICE_IDENTIFIER, actions));
        verifyNoMoreInteractions(listener);
    }
}
