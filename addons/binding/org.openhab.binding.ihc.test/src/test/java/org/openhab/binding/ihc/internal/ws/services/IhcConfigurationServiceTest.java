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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.openhab.binding.ihc.internal.ws.ResourceFileUtils;
import org.openhab.binding.ihc.internal.ws.datatypes.WSSystemInfo;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.internal.ws.http.IhcConnectionPool;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcConfigurationServiceTest {

    private IhcConfigurationService ihcConfigurationService;
    private final String url = "https://1.1.1.1/ws/ConfigurationService";

    @Before
    public void setUp() throws IhcExecption, SocketTimeoutException {
        ihcConfigurationService = spy(new IhcConfigurationService(url, 0, new IhcConnectionPool()));
        doNothing().when(ihcConfigurationService).openConnection(eq(url));

        final String query = ResourceFileUtils.getFileContent("src/test/resources/EmptyQuery.xml");
        final String response = ResourceFileUtils.getFileContent("src/test/resources/GetSystemInfoResponse.xml");

        doReturn(response).when(ihcConfigurationService).sendQuery(eq(query), ArgumentMatchers.anyInt());
    }

    @Test
    public void test() throws IhcExecption {
        doNothing().when(ihcConfigurationService).setRequestProperty(eq("SOAPAction"), eq("getSystemInfo"));

        final WSSystemInfo result = ihcConfigurationService.getSystemInfo();

        Mockito.verify(ihcConfigurationService).setRequestProperty(eq("SOAPAction"), eq("getSystemInfo"));
        assertEquals(false, result.getApplicationIsWithoutViewer());
        assertEquals("ELKOFI", result.getBrand());
        assertEquals("6.2", result.getHwRevision());
        assertEquals(LocalDateTime.parse("2018-06-28 17:02:29", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                result.getRealTimeClock());
        assertEquals("1234567890", result.getSerialNumber());
        assertEquals(LocalDateTime.parse("2011-03-04 09:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                result.getSwDate());
        assertEquals(3980146956L, result.getUptime());
        assertEquals("2.7.144", result.getVersion());
    }
}
