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
package org.openhab.binding.boschshc.internal.devices;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.boschshc.internal.devices.bridge.BridgeHandler;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Device;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Abstract unit test implementation for all types of handlers.
 *
 * @author David Pace - Initial contribution
 *
 * @param <T> type of the handler to be tested
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public abstract class AbstractBoschSHCHandlerTest<T extends BoschSHCHandler> {

    private T fixture;

    private @Mock @NonNullByDefault({}) Thing thing;

    private @Mock @NonNullByDefault({}) Bridge bridge;

    private @Mock @NonNullByDefault({}) BridgeHandler bridgeHandler;

    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;

    private @NonNullByDefault({}) Device device;

    protected AbstractBoschSHCHandlerTest() {
        this.fixture = createFixture();
    }

    /**
     * Initializes the fixture and all required mocks around the handler.
     * 
     * @param testInfo used in subclasses where initializing the handler differently in individual tests is required.
     * 
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     * @throws BoschSHCException
     */
    @BeforeEach
    void beforeEach(TestInfo testInfo)
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        fixture = createFixture();
        lenient().when(thing.getUID()).thenReturn(getThingUID());
        when(thing.getBridgeUID()).thenReturn(new ThingUID("boschshc", "shc", "myBridgeUID"));
        when(callback.getBridge(any())).thenReturn(bridge);
        fixture.setCallback(callback);
        when(bridge.getHandler()).thenReturn(bridgeHandler);
        lenient().when(thing.getConfiguration()).thenReturn(getConfiguration());

        device = new Device();
        configureDevice(device);
        lenient().when(bridgeHandler.getDeviceInfo(anyString())).thenReturn(device);

        beforeHandlerInitialization(testInfo);

        fixture.initialize();

        afterHandlerInitialization(testInfo);
    }

    /**
     * Hook to allow tests to add custom setup code before the handler initialization.
     * 
     * @param testInfo provides metadata related to the current test being executed
     */
    protected void beforeHandlerInitialization(TestInfo testInfo) {
        // default implementation is empty, subclasses may override
    }

    /**
     * Hook to allow tests to add custom setup code after the handler initialization.
     * 
     * @param testInfo provides metadata related to the current test being executed
     */
    protected void afterHandlerInitialization(TestInfo testInfo) {
        // default implementation is empty, subclasses may override
    }

    protected abstract T createFixture();

    protected T getFixture() {
        return fixture;
    }

    protected ThingUID getThingUID() {
        return new ThingUID(getThingTypeUID(), "abcdef");
    }

    protected abstract ThingTypeUID getThingTypeUID();

    protected ChannelUID getChannelUID(String channelID) {
        return new ChannelUID(getThingUID(), channelID);
    }

    protected Configuration getConfiguration() {
        return new Configuration();
    }

    protected Thing getThing() {
        return thing;
    }

    protected BridgeHandler getBridgeHandler() {
        return bridgeHandler;
    }

    protected ThingHandlerCallback getCallback() {
        return callback;
    }

    protected Device getDevice() {
        return device;
    }

    protected void configureDevice(Device device) {
        // abstract implementation is empty, subclasses may override
    }

    @Test
    void testInitialize() {
        ThingStatusInfo expectedStatusInfo = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
        verify(callback).statusUpdated(any(Thing.class), eq(expectedStatusInfo));
    }

    @Test
    void testGetBridgeHandler() throws BoschSHCException {
        assertThat(fixture.getBridgeHandler(), sameInstance(bridgeHandler));
    }

    @Test
    void testGetBridgeHandlerThrowExceptionIfBridgeIsNull() throws BoschSHCException {
        when(callback.getBridge(any())).thenReturn(null);
        assertThrows(BoschSHCException.class, () -> fixture.getBridgeHandler());
    }

    @Test
    void testGetBridgeHandlerThrowExceptionIfBridgeHandlerIsNull() throws BoschSHCException {
        when(bridge.getHandler()).thenReturn(null);
        assertThrows(BoschSHCException.class, () -> fixture.getBridgeHandler());
    }
}
