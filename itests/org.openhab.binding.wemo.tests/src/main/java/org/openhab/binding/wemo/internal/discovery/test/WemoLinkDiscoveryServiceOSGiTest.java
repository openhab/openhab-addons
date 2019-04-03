/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.wemo.internal.discovery.test;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.junit.Before;
import org.junit.Test;
import org.jupnp.model.ValidationException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.binding.wemo.internal.discovery.WemoLinkDiscoveryService;
import org.openhab.binding.wemo.internal.handler.WemoBridgeHandler;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.openhab.binding.wemo.internal.test.GenericWemoLightOSGiTestParent;
import org.openhab.binding.wemo.internal.test.GenericWemoOSGiTest;

/**
 * Tests for {@link WemoLinkDiscoveryService}.
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Stefan Triller - Ported Tests from Groovy to Java
 */
public class WemoLinkDiscoveryServiceOSGiTest extends GenericWemoLightOSGiTestParent {

    @Before
    public void setUp() throws IOException {
        setUpServices();
    }

    @Test
    public void assertSupportedThingIsDiscovered()
            throws MalformedURLException, URISyntaxException, ValidationException {
        String model = WemoBindingConstants.THING_TYPE_MZ100.getId();
        addUpnpDevice(SERVICE_ID, SERVICE_NUMBER, model);

        Configuration config = new Configuration();
        config.put(WemoBindingConstants.UDN, GenericWemoOSGiTest.DEVICE_UDN);

        Bridge bridge = mock(Bridge.class);
        when(bridge.getThingTypeUID()).thenReturn(WemoBindingConstants.THING_TYPE_BRIDGE);
        when(bridge.getConfiguration()).thenReturn(config);

        WemoBridgeHandler handler = mock(WemoBridgeHandler.class);
        when(handler.getThing()).thenReturn(bridge);

        WemoHttpCall mockCaller = Mockito.spy(new WemoHttpCall());

        WemoLinkDiscoveryService discoveryService = new WemoLinkDiscoveryService(handler, upnpIOService, mockCaller);
        discoveryService.startScan();

        ArgumentCaptor<String> captur = ArgumentCaptor.forClass(String.class);
        verify(mockCaller, atLeastOnce()).executeCall(any(), any(), captur.capture());

        List<String> results = captur.getAllValues();
        boolean found = false;
        for (String result : results) {
            if (result.contains(
                    "<u:GetEndDevices xmlns:u=\"urn:Belkin:service:bridge:1\"><DevUDN>uuid:Test-1_0-22124</DevUDN><ReqListType>PAIRED_LIST</ReqListType></u:GetEndDevices>")) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }
}
