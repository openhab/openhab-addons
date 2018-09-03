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
            "<ns1:getDetectedDeviceList1 xmlns:ns1=\"utcs\">\n" +
            "<ns1:arrayItem xsi:type=\"ns1:WSRFDevice\">\n" +
            "<ns1:batteryLevel xsi:type=\"xsd:int\">1</ns1:batteryLevel>\n" +
            "\n" +
            "<ns1:deviceType xsi:type=\"xsd:int\">2049</ns1:deviceType>\n" +
            "\n" +
            "<ns1:serialNumber xsi:type=\"xsd:long\">123456789</ns1:serialNumber>\n" +
            "\n" +
            "<ns1:signalStrength xsi:type=\"xsd:int\">10</ns1:signalStrength>\n" +
            "\n" +
            "<ns1:version xsi:type=\"xsd:int\">1</ns1:version>\n" +
            "\n" +
            "<ns1:detected xsi:type=\"xsd:boolean\">true</ns1:detected>\n" +
            "</ns1:arrayItem>\n" +
            "</ns1:getDetectedDeviceList1>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";
    // @formatter:on

    @Before
    public void setUp() throws IhcExecption, SocketTimeoutException {
        ihcAirlinkManagementService = spy(new IhcAirlinkManagementService(url, 0, new IhcConnectionPool()));
        doNothing().when(ihcAirlinkManagementService).openConnection(eq(url));

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
