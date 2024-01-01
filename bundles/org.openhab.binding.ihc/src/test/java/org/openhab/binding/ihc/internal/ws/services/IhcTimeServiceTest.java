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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    private final String host = "1.1.1.1";
    private final String url = "https://1.1.1.1/ws/TimeManagerService";
    private Map<String, String> requestProps = new HashMap<>();
    private final int timeout = 100;

    @BeforeEach
    public void setUp() throws IhcExecption, SocketTimeoutException {
        ihcTimeService = spy(new IhcTimeService(host, timeout, new IhcConnectionPool()));

        final String query = ResourceFileUtils.getFileContent("EmptyQuery.xml");
        final String response = ResourceFileUtils.getFileContent("GetSettingsResponse.xml");

        requestProps.clear();
        requestProps.put("SOAPAction", "getSettings");

        doReturn(response).when(ihcTimeService).sendQuery(eq(url), eq(requestProps), eq(query), eq(timeout));
    }

    @Test
    public void test() throws IhcExecption {
        final WSTimeManagerSettings result = ihcTimeService.getTimeSettings();

        assertEquals(true, result.getSynchroniseTimeAgainstServer());
        assertEquals(true, result.getUseDST());
        assertEquals(2, result.getGmtOffsetInHours());
        assertEquals("192.168.1.10", result.getServerName());
        assertEquals(24, result.getSyncIntervalInHours());
        assertEquals(LocalDateTime.parse("2018-12-07T08:20:10", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                result.getTimeAndDateInUTC().getAsLocalDateTime());
    }
}
