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
package org.openhab.binding.smartthings.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.smartthings.internal.SmartThingsAuthService;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.SmartThingsHandlerFactory;
import org.openhab.binding.smartthings.internal.discovery.SmartThingsDiscoveryService;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;

/**
 * Tests for {@link SmartThingsAccountHandler}.
 */
@NonNullByDefault
class SmartThingsAccountHandlerTest {

    @Test
    void initLocationMarksBridgeOnlineAfterSuccessfulAuthorization() throws SmartThingsException {
        SmartThingsDiscoveryService discoveryService = mock(SmartThingsDiscoveryService.class);
        TestSmartThingsAccountHandler handler = new TestSmartThingsAccountHandler(accountBridge(), discoveryService);

        handler.initLocation("location-1");

        verify(discoveryService).doScan(true);
        assertEquals(ThingStatus.ONLINE, handler.lastStatus);
    }

    private Bridge accountBridge() {
        return BridgeBuilder.create(SmartThingsBindingConstants.THING_TYPE_ACCOUNT, "account").build();
    }

    private static class TestSmartThingsAccountHandler extends SmartThingsAccountHandler {
        private @Nullable ThingStatus lastStatus;

        TestSmartThingsAccountHandler(Bridge bridge, SmartThingsDiscoveryService discoveryService) {
            super(bridge, mock(SmartThingsHandlerFactory.class), mock(SmartThingsAuthService.class),
                    mock(TranslationProvider.class), mock(BundleContext.class), mock(HttpService.class),
                    mock(OAuthFactory.class), mock(HttpClientFactory.class), mock(SmartThingsTypeRegistry.class),
                    mock(ClientBuilder.class), mock(SseEventSourceFactory.class), null);
            this.discoService = discoveryService;
        }

        @Override
        public void registerSubscriptions() {
        }

        @Override
        protected void updateLocationProperties(@Nullable String location) {
        }

        @Override
        protected void updateStatus(ThingStatus status) {
            lastStatus = status;
        }

        @Override
        protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
            lastStatus = status;
        }
    }
}
