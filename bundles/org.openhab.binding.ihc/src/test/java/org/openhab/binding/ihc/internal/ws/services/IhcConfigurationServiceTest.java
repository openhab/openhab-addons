/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
    private final String host = "1.1.1.1";
    private final String url = "https://1.1.1.1/ws/ConfigurationService";
    final String query = ResourceFileUtils.getFileContent("EmptyQuery.xml");
    private Map<String, String> requestProps = new HashMap<>();
    private final int timeout = 100;

    @BeforeEach
    public void setUp() throws IhcExecption, SocketTimeoutException {
        ihcConfigurationService = spy(new IhcConfigurationService(host, timeout, new IhcConnectionPool()));
        requestProps.clear();
        requestProps.put("SOAPAction", "getSystemInfo");
    }

    @Test
    public void testv2() throws IhcExecption {
        final String response = ResourceFileUtils.getFileContent("GetSystemInfoResponse.xml");

        doReturn(response).when(ihcConfigurationService).sendQuery(eq(url), eq(requestProps), eq(query), eq(timeout));

        final WSSystemInfo result = ihcConfigurationService.getSystemInfo();

        assertEquals(false, result.getApplicationIsWithoutViewer());
        assertEquals("ELKOFI", result.getBrand());
        assertEquals("6.2", result.getHwRevision());
        assertEquals(LocalDateTime.parse("2018-06-28T17:02:29", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                result.getRealTimeClock().toLocalDateTime());
        assertEquals("1234567890", result.getSerialNumber());
        assertEquals(LocalDateTime.parse("2011-03-04T09:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")),
                result.getSwDate().toLocalDateTime());
        assertEquals(3980146956L, result.getUptime());
        assertEquals("2.7.144", result.getVersion());
        assertEquals("2011-04-28T09:00:00Z", result.getProductionDate());
        assertEquals("", result.getDatalineVersion());
        assertEquals("", result.getRfModuleSoftwareVersion());
        assertEquals("", result.getRfModuleSerialNumber());
    }

    @Test
    public void testv3() throws IhcExecption {
        final String response = ResourceFileUtils.getFileContent("GetSystemInfoResponse2.xml");
        doReturn(response).when(ihcConfigurationService).sendQuery(eq(url), eq(requestProps), eq(query), eq(timeout));

        final WSSystemInfo result = ihcConfigurationService.getSystemInfo();

        assertEquals(false, result.getApplicationIsWithoutViewer());
        assertEquals("LK", result.getBrand());
        assertEquals("7.1", result.getHwRevision());
        assertEquals(
                LocalDateTime.parse("2018-12-15T13:15:00Z", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")),
                result.getRealTimeClock().toLocalDateTime());
        assertEquals("CN1734000000", result.getSerialNumber());
        assertEquals(
                LocalDateTime.parse("2018-07-23T10:00:00Z", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")),
                result.getSwDate().toLocalDateTime());
        assertEquals(164057634L, result.getUptime());
        assertEquals("3.3.9", result.getVersion());
        assertEquals("2017/34", result.getProductionDate());
        assertEquals("IOB.B.03.02.01", result.getDatalineVersion());
        assertEquals("3.06.c", result.getRfModuleSoftwareVersion());
        assertEquals("640C10140000", result.getRfModuleSerialNumber());
    }
}
