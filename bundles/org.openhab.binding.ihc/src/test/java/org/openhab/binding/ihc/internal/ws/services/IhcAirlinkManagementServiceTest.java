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
package org.openhab.binding.ihc.internal.ws.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ihc.internal.ws.ResourceFileUtils;
import org.openhab.binding.ihc.internal.ws.datatypes.WSRFDevice;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.internal.ws.http.IhcConnectionPool;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcAirlinkManagementServiceTest {

    private IhcAirlinkManagementService ihcAirlinkManagementService;
    private final String host = "1.1.1.1";
    private final String url = "https://1.1.1.1/ws/AirlinkManagementService";
    private Map<String, String> requestProps = new HashMap<>();
    private String query;
    private final int timeout = 100;

    @BeforeEach
    public void setUp() throws IhcExecption, SocketTimeoutException {
        ihcAirlinkManagementService = spy(new IhcAirlinkManagementService(host, timeout, new IhcConnectionPool()));

        query = ResourceFileUtils.getFileContent("EmptyQuery.xml");

        requestProps.clear();
        requestProps.put("SOAPAction", "getDetectedDeviceList");
    }

    @Test
    public void test() throws IhcExecption {
        final String response = ResourceFileUtils.getFileContent("GetDetectedDeviceListResponse.xml");
        doReturn(response).when(ihcAirlinkManagementService).sendQuery(eq(url), eq(requestProps), eq(query),
                eq(timeout));

        final List<WSRFDevice> result = ihcAirlinkManagementService.getDetectedDeviceList();

        assertEquals(1, result.size());

        assertEquals(1, result.get(0).getBatteryLevel());
        assertEquals(true, result.get(0).getDetected());
        assertEquals(2049, result.get(0).getDeviceType());
        assertEquals(123456789, result.get(0).getSerialNumber());
        assertEquals(10, result.get(0).getSignalStrength());
        assertEquals(1, result.get(0).getVersion());
    }

    @Test
    public void testEmptyList() throws IhcExecption {
        final String response = ResourceFileUtils.getFileContent("GetEmptyDetectedDeviceListResponse.xml");
        doReturn(response).when(ihcAirlinkManagementService).sendQuery(eq(url), eq(requestProps), eq(query),
                eq(timeout));

        final List<WSRFDevice> result = ihcAirlinkManagementService.getDetectedDeviceList();

        assertEquals(0, result.size());
    }
}
