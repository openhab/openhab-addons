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
package org.openhab.binding.gme.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

class GmeHandlerFactoryTest {

    @Test
    void usesBindingClassLoaderForBridgeStorage() {
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        HttpClient httpClient = mock(HttpClient.class);
        StorageService storageService = mock(StorageService.class);
        @SuppressWarnings("unchecked")
        Storage<String> storage = mock(Storage.class);
        Bridge bridge = mock(Bridge.class);
        ThingUID thingUID = new ThingUID(GmeBindingConstants.THING_TYPE_API, "account");

        when(httpClientFactory.getCommonHttpClient()).thenReturn(httpClient);
        when(bridge.getThingTypeUID()).thenReturn(GmeBindingConstants.THING_TYPE_API);
        when(bridge.getUID()).thenReturn(thingUID);
        doReturn(storage).when(storageService).getStorage(thingUID.toString(),
                GmeHandlerFactory.class.getClassLoader());

        GmeHandlerFactory factory = new GmeHandlerFactory(httpClientFactory, storageService);

        assertNotNull(factory.createHandler(bridge));
        verify(storageService).getStorage(thingUID.toString(), GmeHandlerFactory.class.getClassLoader());
    }
}
