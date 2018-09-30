/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.ws.services;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.net.SocketTimeoutException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
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
    private final String url = "https://1.1.1.1/ws/AirlinkManagementService";

    @Before
    public void setUp() throws IhcExecption, SocketTimeoutException {
        ihcAirlinkManagementService = spy(new IhcAirlinkManagementService(url, 0, new IhcConnectionPool()));
        doNothing().when(ihcAirlinkManagementService).openConnection(eq(url));

        final String query = ResourceFileUtils.getFileContent("src/test/resources/EmptyQuery.xml");
        final String response = ResourceFileUtils
                .getFileContent("src/test/resources/GetDetectedDeviceListResponse.xml");

        doReturn(response).when(ihcAirlinkManagementService).sendQuery(eq(query), ArgumentMatchers.anyInt());
    }

    @Test
    public void test() throws IhcExecption {
        doNothing().when(ihcAirlinkManagementService).setRequestProperty(eq("SOAPAction"), eq("getDetectedDeviceList"));

        final List<WSRFDevice> result = ihcAirlinkManagementService.getDetectedDeviceList();

        Mockito.verify(ihcAirlinkManagementService).setRequestProperty(eq("SOAPAction"), eq("getDetectedDeviceList"));

        assertEquals(1, result.size());

        assertEquals(1, result.get(0).getBatteryLevel());
        assertEquals(true, result.get(0).getDetected());
        assertEquals(2049, result.get(0).getDeviceType());
        assertEquals(123456789, result.get(0).getSerialNumber());
        assertEquals(10, result.get(0).getSignalStrength());
        assertEquals(1, result.get(0).getVersion());
    }
}
