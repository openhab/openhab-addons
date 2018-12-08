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
import org.openhab.binding.ihc.internal.ws.datatypes.WSTimeManagerSettings;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.internal.ws.http.IhcConnectionPool;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcTimeServiceTest {

    private IhcTimeService ihcTimeService;
    private final String url = "https://1.1.1.1/ws/TimeManagerService";

    @Before
    public void setUp() throws IhcExecption, SocketTimeoutException {
        ihcTimeService = spy(new IhcTimeService(url, 0, new IhcConnectionPool()));
        doNothing().when(ihcTimeService).openConnection(eq(url));

        final String query = ResourceFileUtils.getFileContent("src/test/resources/EmptyQuery.xml");
        final String response = ResourceFileUtils.getFileContent("src/test/resources/GetSettingsResponse.xml");

        doReturn(response).when(ihcTimeService).sendQuery(eq(query), ArgumentMatchers.anyInt());
    }

    @Test
    public void test() throws IhcExecption {
        doNothing().when(ihcTimeService).setRequestProperty(eq("SOAPAction"), eq("getSettings"));

        final WSTimeManagerSettings result = ihcTimeService.getTimeSettings();

        Mockito.verify(ihcTimeService).setRequestProperty(eq("SOAPAction"), eq("getSettings"));
        assertEquals(true, result.getSynchroniseTimeAgainstServer());
        assertEquals(true, result.getUseDST());
        assertEquals(2, result.getGmtOffsetInHours());
        assertEquals("192.168.1.10", result.getServerName());
        assertEquals(24, result.getSyncIntervalInHours());
        assertEquals(LocalDateTime.parse("2018-12-07T08:20:10", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                result.getTimeAndDateInUTC().getAsLocalDateTime());

    }
}
