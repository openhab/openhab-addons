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

    // @formatter:off
    private final String query =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            " <soapenv:Body>\n" +
            " </soapenv:Body>\n" +
            "</soapenv:Envelope>\n";

    private final String response =
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:getSystemInfo1 xmlns:ns1=\"utcs\" xsi:type=\"ns1:WSSystemInfo\">\n" +
            "<ns1:uptime xsi:type=\"xsd:long\">3980146956</ns1:uptime>\n" +
            "\n" +
            "<ns1:realtimeclock xsi:type=\"xsd:dateTime\">2018-06-28T17:02:29Z</ns1:realtimeclock>\n" +
            "\n" +
            "<ns1:serialNumber xsi:type=\"xsd:string\">1234567890</ns1:serialNumber>\n" +
            "\n" +
            "<ns1:productionDate xsi:type=\"xsd:dateTime\">2011-04-28T09:00:00Z</ns1:productionDate>\n" +
            "\n" +
            "<ns1:brand xsi:type=\"xsd:string\">ELKOFI</ns1:brand>\n" +
            "\n" +
            "<ns1:version xsi:type=\"xsd:string\">2.7.144</ns1:version>\n" +
            "\n" +
            "<ns1:hwRevision xsi:type=\"xsd:string\">6.2</ns1:hwRevision>\n" +
            "\n" +
            "<ns1:swDate xsi:type=\"xsd:dateTime\">2011-03-04T09:00:00Z</ns1:swDate>\n" +
            "\n" +
            "<ns1:applicationIsWithoutViewer xsi:type=\"xsd:boolean\">false</ns1:applicationIsWithoutViewer>\n" +
            "</ns1:getSystemInfo1>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";
    // @formatter:on

    @Before
    public void setUp() throws IhcExecption, SocketTimeoutException {
        ihcConfigurationService = spy(new IhcConfigurationService(url, 0, new IhcConnectionPool()));
        doNothing().when(ihcConfigurationService).openConnection(eq(url));

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
