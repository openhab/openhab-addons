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
package org.openhab.binding.rachio.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_CLOUD;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_DEVICE;

import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.types.Command;

/**
 * Tests scheduled polling and listener ownership across bridge handler lifecycle changes.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class RachioPollingLifecycleTest {
    @Test
    void pollingRestartsAfterBridgeReinitializationWithoutDuplicateListeners() {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        TestBridgeHandler handler = new TestBridgeHandler(bridge);
        RachioStatusListener listener = new TestStatusListener();

        handler.registerStatusListener(listener);
        handler.registerStatusListener(listener);

        assertThat(handler.getStatusListenerCount(), is(1));
        assertThat(handler.isPollingJobActive(), is(true));

        handler.dispose();
        assertThat(handler.isPollingJobActive(), is(false));

        handler.restartPollingAfterInitialization();
        assertThat(handler.getStatusListenerCount(), is(1));
        assertThat(handler.isPollingJobActive(), is(true));

        handler.dispose();
    }

    @Test
    void registeredChildMovesFromDisposedBridgeHandlerToReplacement() {
        Thing thing = Mockito.mock(Thing.class);
        Mockito.when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE_DEVICE, "bridge", "controller"));
        RachioBridgeHandler oldBridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        RachioBridgeHandler replacementBridgeHandler = Mockito.mock(RachioBridgeHandler.class);
        TestThingHandler childHandler = new TestThingHandler(thing);

        childHandler.useBridgeHandler(oldBridgeHandler);
        childHandler.listenForStatus();
        childHandler.listenForStatus();
        childHandler.useBridgeHandler(replacementBridgeHandler);

        verify(oldBridgeHandler, times(1)).registerStatusListener(childHandler);
        verify(oldBridgeHandler, times(1)).unregisterStatusListener(childHandler);
        verify(replacementBridgeHandler, times(1)).registerStatusListener(childHandler);
    }

    private static class TestBridgeHandler extends AbstractRachioBridgeHandler {
        TestBridgeHandler(Bridge bridge) {
            super(bridge);
        }

        void restartPollingAfterInitialization() {
            updateListenerManagement();
        }

        @Override
        public void initialize() {
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
        }

        @Override
        public Collection<ConfigStatusMessage> getConfigStatus() {
            return List.of();
        }

        @Override
        protected int getPollingIntervalSeconds() {
            return 3600;
        }

        @Override
        protected void runScheduledRefresh() {
        }
    }

    private static class TestThingHandler extends AbstractRachioThingHandler {
        TestThingHandler(Thing thing) {
            super(thing);
        }

        void useBridgeHandler(RachioBridgeHandler bridgeHandler) {
            bindCloudHandler(bridgeHandler);
        }

        void listenForStatus() {
            registerStatusListener();
        }

        @Override
        public void initialize() {
        }

        @Override
        public void handleCommand(ChannelUID channelUID, Command command) {
        }

        @Override
        public boolean onThingStateChanged(@Nullable RachioDevice updatedDev, @Nullable RachioZone updatedZone) {
            return false;
        }

        @Override
        protected void goOnline() {
        }

        @Override
        protected void postChannelData() {
        }
    }

    private static class TestStatusListener implements RachioStatusListener {
        @Override
        public boolean onThingStateChanged(@Nullable RachioDevice updatedDev, @Nullable RachioZone updatedZone) {
            return false;
        }

        @Override
        public void onConfigurationUpdated() {
        }
    }
}
