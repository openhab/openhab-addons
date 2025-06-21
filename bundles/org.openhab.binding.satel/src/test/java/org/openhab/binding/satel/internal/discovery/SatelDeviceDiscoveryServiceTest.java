/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.satel.internal.discovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.openhab.binding.satel.internal.SatelBindingConstants.*;
import static org.openhab.binding.satel.internal.command.SatelCommand.State.FAILED;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.satel.internal.command.ReadDeviceInfoCommand;
import org.openhab.binding.satel.internal.event.EventDispatcher;
import org.openhab.binding.satel.internal.handler.SatelBridgeHandler;
import org.openhab.binding.satel.internal.protocol.SatelMessage;
import org.openhab.binding.satel.internal.types.IntegraType;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.internal.BridgeImpl;
import org.openhab.core.thing.type.ThingType;

/**
 * @author Krzysztof Goworek - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class SatelDeviceDiscoveryServiceTest {

    private static final Charset bridgeEncoding = StandardCharsets.US_ASCII;

    @Mock
    private SatelBridgeHandler bridgeHandler;

    @Mock
    private Function<ThingTypeUID, ThingType> thingTypeProvider;

    @Mock
    private EventDispatcher eventDispatcher;

    @Mock
    private DiscoveryListener listener;

    @InjectMocks
    private SatelDeviceDiscoveryService testSubject;

    @BeforeEach
    void setUp() {
        when(bridgeHandler.getIntegraType()).thenReturn(IntegraType.I24);
        when(bridgeHandler.getEncoding()).thenReturn(bridgeEncoding);
        testSubject.addDiscoveryListener(listener);
    }

    @Test
    void startScanShouldNotAddAnyThingWhenBridgeIsNotInitialized() {
        when(bridgeHandler.getThing()).thenReturn(new BridgeImpl(THING_TYPE_ETHM1, "bridgeId"));

        testSubject.startScan();

        verifyNoInteractions(listener);
    }

    @Test
    void startScanShouldAddVirtualThingsWhenBridgeIsInitialized() {
        ThingType thingType = mock(ThingType.class);
        when(thingTypeProvider.apply(any())).thenReturn(thingType);
        when(bridgeHandler.isInitialized()).thenReturn(true);
        when(bridgeHandler.getThing()).thenReturn(new BridgeImpl(THING_TYPE_ETHM1, "bridgeId"));

        testSubject.startScan();

        ArgumentCaptor<DiscoveryResult> resultCaptor = ArgumentCaptor.forClass(DiscoveryResult.class);
        verify(listener, atLeastOnce()).thingDiscovered(any(), resultCaptor.capture());
        List<DiscoveryResult> results = resultCaptor.getAllValues();

        assertEquals(2, results.size());
        assertEquals(THING_TYPE_SYSTEM, results.get(0).getThingTypeUID());
        assertEquals(THING_TYPE_EVENTLOG, results.get(1).getThingTypeUID());
    }

    @Test
    void startScanShouldContinueWhenFailureOccurred() {
        setUpCommandFailure();

        testSubject.startScan();

        verifyNoInteractions(listener);
        verify(bridgeHandler, times(52)).sendCommand(any(), eq(false));
    }

    @Test
    void startScanShouldAddAllDevices() {
        BridgeImpl bridge = new BridgeImpl(THING_TYPE_ETHM1, "bridgeId");
        when(bridgeHandler.getThing()).thenReturn(bridge);
        setUpCommandResponse(1);

        testSubject.startScan();

        ArgumentCaptor<DiscoveryResult> resultCaptor = ArgumentCaptor.forClass(DiscoveryResult.class);
        verify(listener, atLeastOnce()).thingDiscovered(any(), resultCaptor.capture());
        List<DiscoveryResult> results = resultCaptor.getAllValues();
        assertEquals(4,
                results.stream().filter(result -> THING_TYPE_PARTITION.equals(result.getThingTypeUID())).count());
        assertEquals(24, results.stream().filter(result -> THING_TYPE_ZONE.equals(result.getThingTypeUID())).count());
        assertEquals(24, results.stream().filter(result -> THING_TYPE_OUTPUT.equals(result.getThingTypeUID())).count());
        assertTrue(results.stream().allMatch(r -> "Device".equals(r.getLabel())));
        for (DiscoveryResult result : results) {
            assertEquals("Device", result.getLabel());
            assertEquals(bridge.getUID(), result.getBridgeUID());
            assertEquals(1, result.getProperties().size());
        }
    }

    @Test
    void startScanShouldAddShutters() {
        BridgeImpl bridge = new BridgeImpl(THING_TYPE_ETHM1, "bridgeId");
        when(bridgeHandler.getThing()).thenReturn(bridge);
        setUpCommandResponse(105);

        testSubject.startScan();

        ArgumentCaptor<DiscoveryResult> resultCaptor = ArgumentCaptor.forClass(DiscoveryResult.class);
        verify(listener, atLeastOnce()).thingDiscovered(any(), resultCaptor.capture());
        List<DiscoveryResult> results = resultCaptor.getAllValues().stream()
                .filter(result -> THING_TYPE_SHUTTER.equals(result.getThingTypeUID())).toList();
        assertEquals(24, results.size());
        for (DiscoveryResult result : results) {
            assertEquals("Device", result.getLabel());
            assertEquals(bridge.getUID(), result.getBridgeUID());
            assertEquals(2, result.getProperties().size());
        }
    }

    @Test
    void startScanShouldSkipUnusedOutput() {
        when(bridgeHandler.getThing()).thenReturn(new BridgeImpl(THING_TYPE_ETHM1, "bridgeId"));
        setUpCommandResponse(0);

        testSubject.startScan();

        ArgumentCaptor<DiscoveryResult> resultCaptor = ArgumentCaptor.forClass(DiscoveryResult.class);
        verify(listener, atLeastOnce()).thingDiscovered(any(), resultCaptor.capture());
        List<DiscoveryResult> results = resultCaptor.getAllValues();
        assertEquals(0, results.stream().filter(result -> THING_TYPE_OUTPUT.equals(result.getThingTypeUID())).count());
        assertEquals(0, results.stream().filter(result -> THING_TYPE_SHUTTER.equals(result.getThingTypeUID())).count());
    }

    @Test
    void startScanShouldSkipSecondShutterOutput() {
        when(bridgeHandler.getThing()).thenReturn(new BridgeImpl(THING_TYPE_ETHM1, "bridgeId"));
        setUpCommandResponse(106);

        testSubject.startScan();

        ArgumentCaptor<DiscoveryResult> resultCaptor = ArgumentCaptor.forClass(DiscoveryResult.class);
        verify(listener, atLeastOnce()).thingDiscovered(any(), resultCaptor.capture());
        List<DiscoveryResult> results = resultCaptor.getAllValues();
        assertEquals(0, results.stream().filter(result -> THING_TYPE_OUTPUT.equals(result.getThingTypeUID())).count());
        assertEquals(0, results.stream().filter(result -> THING_TYPE_SHUTTER.equals(result.getThingTypeUID())).count());
    }

    @Test
    void stopScanShouldSkipDiscovery() throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch stopLatch = new CountDownLatch(1);
        Thread thread = new Thread(() -> {
            reset(bridgeHandler);
            when(bridgeHandler.isInitialized()).thenAnswer(invocationOnMock -> {
                startLatch.countDown();
                stopLatch.await();
                return false;
            });
            testSubject.startScan();
        });
        thread.start();
        startLatch.await();

        testSubject.stopScan();
        stopLatch.countDown();
        thread.join();

        verifyNoMoreInteractions(bridgeHandler);
        verifyNoInteractions(listener);
    }

    private void setUpCommandResponse(int deviceKind) {
        when(bridgeHandler.sendCommand(isA(ReadDeviceInfoCommand.class), eq(false))).thenAnswer(invocationOnMock -> {
            ReadDeviceInfoCommand cmd = invocationOnMock.getArgument(0);
            byte[] payload = new byte[19];
            byte[] nameBytes = "Device".getBytes(bridgeEncoding);
            System.arraycopy(nameBytes, 0, payload, 3, nameBytes.length);
            payload[2] = (byte) deviceKind;
            cmd.handleResponse(eventDispatcher, new SatelMessage(ReadDeviceInfoCommand.COMMAND_CODE, payload));
            return true;
        });
    }

    private void setUpCommandFailure() {
        when(bridgeHandler.sendCommand(isA(ReadDeviceInfoCommand.class), eq(false))).thenAnswer(invocationOnMock -> {
            ReadDeviceInfoCommand cmd = invocationOnMock.getArgument(0);
            cmd.setState(FAILED);
            return false;
        });
    }
}
