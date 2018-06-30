/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.ws.services;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.net.SocketTimeoutException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.openhab.binding.ihc.ws.IhcClient;
import org.openhab.binding.ihc.ws.datatypes.WSControllerState;
import org.openhab.binding.ihc.ws.datatypes.WSFile;
import org.openhab.binding.ihc.ws.datatypes.WSProjectInfo;
import org.openhab.binding.ihc.ws.exeptions.IhcExecption;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcControllerServiceTest {

    private IhcControllerService ihcControllerService;
    private final String url = "https://1.1.1.1/ws/ControllerService";

    // @formatter:off
    private final String query =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            " <soapenv:Body>\n" +
            " </soapenv:Body>\n" +
            "</soapenv:Envelope>\n";

    private final String waitForControllerStateChangeQuery =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
          + "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\">\n"
          + " <soapenv:Body>\n"
          + "  <ns1:waitForControllerStateChange1 xmlns:ns1=\"utcs\" xsi:type=\"ns1:WSControllerState\">\n"
          + "   <ns1:state xsi:type=\"xsd:string\">text.ctrl.state.initialize</ns1:state>\n"
          + "  </ns1:waitForControllerStateChange1>\n"
          + "  <ns2:waitForControllerStateChange2 xmlns:ns2=\"utcs\" xsi:type=\"xsd:int\">5</ns2:waitForControllerStateChange2>\n"
          + " </soapenv:Body>\n"
          + "</soapenv:Envelope>";

    private final String projectSegmentQuery =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
            " <soap:Body>\n" +
            "  <ns1:getIHCProjectSegment1 xmlns:ns1=\"utcs\" xsi:type=\"xsd:int\">1</ns1:getIHCProjectSegment1>\n" +
            "  <ns2:getIHCProjectSegment2 xmlns:ns2=\"utcs\" xsi:type=\"xsd:int\">1001</ns2:getIHCProjectSegment2>\n" +
            "  <ns3:getIHCProjectSegment3 xmlns:ns3=\"utcs\" xsi:type=\"xsd:int\">2002</ns3:getIHCProjectSegment3>\n" +
            " </soap:Body>\n" +
            "</soap:Envelope>";

    private final String projectInfoResponse =
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:getProjectInfo1 xmlns:ns1=\"utcs\" xsi:type=\"ns1:WSProjectInfo\">\n" +
            "<ns1:visualMinorVersion xsi:type=\"xsd:int\">3</ns1:visualMinorVersion>\n" +
            "\n" +
            "<ns1:visualMajorVersion xsi:type=\"xsd:int\">3</ns1:visualMajorVersion>\n" +
            "\n" +
            "<ns1:projectMajorRevision xsi:type=\"xsd:int\">218958359</ns1:projectMajorRevision>\n" +
            "\n" +
            "<ns1:projectMinorRevision xsi:type=\"xsd:int\">219293703</ns1:projectMinorRevision>\n" +
            "\n" +
            "<ns1:lastmodified xsi:type=\"ns1:WSDate\">\n" +
            "<ns1:day xsi:type=\"xsd:int\">13</ns1:day>\n" +
            "\n" +
            "<ns1:monthWithJanuaryAsOne xsi:type=\"xsd:int\">5</ns1:monthWithJanuaryAsOne>\n" +
            "\n" +
            "<ns1:hours xsi:type=\"xsd:int\">18</ns1:hours>\n" +
            "\n" +
            "<ns1:minutes xsi:type=\"xsd:int\">40</ns1:minutes>\n" +
            "\n" +
            "<ns1:seconds xsi:type=\"xsd:int\">0</ns1:seconds>\n" +
            "\n" +
            "<ns1:year xsi:type=\"xsd:int\">2018</ns1:year>\n" +
            "</ns1:lastmodified>\n" +
            "\n" +
            "<ns1:projectNumber xsi:type=\"xsd:string\">1</ns1:projectNumber>\n" +
            "\n" +
            "<ns1:customerName xsi:type=\"xsd:string\">Pertti 'Speedy' Keinonen</ns1:customerName>\n" +
            "\n" +
            "<ns1:installerName xsi:type=\"xsd:string\"></ns1:installerName>\n" +
            "</ns1:getProjectInfo1>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";

    private final String projectNumberOfSegmentsResponse =
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:getIHCProjectNumberOfSegments1 xmlns:ns1=\"utcs\" xsi:type=\"xsd:int\">28</ns1:getIHCProjectNumberOfSegments1>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";

    private final String projectSegmentationSizeResponse =
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:getIHCProjectSegmentationSize1 xmlns:ns1=\"utcs\" xsi:type=\"xsd:int\">7500</ns1:getIHCProjectSegmentationSize1>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";

    private final String controllerStateResponse =
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:getState1 xmlns:ns1=\"utcs\" xsi:type=\"ns1:WSControllerState\">\n" +
            "<ns1:state xsi:type=\"xsd:string\">text.ctrl.state.ready</ns1:state>\n" +
            "</ns1:getState1>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";

    private final String waitForControllerStateChangeResponse =
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:waitForControllerStateChange3 xmlns:ns1=\"utcs\" xsi:type=\"ns1:WSControllerState\">\n" +
            "<ns1:state xsi:type=\"xsd:string\">text.ctrl.state.ready</ns1:state>\n" +
            "</ns1:waitForControllerStateChange3>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";

    private final String projectSegmentResponse =
            "<?xml version='1.0' encoding='UTF-8'?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "<SOAP-ENV:Body>\n" +
            "<ns1:getIHCProjectSegment4 xmlns:ns1=\"utcs\" xsi:type=\"ns1:WSFile\">\n" +
            "<ns1:filename xsi:type=\"xsd:string\">unknown.bin</ns1:filename>\n" +
            "\n" +
            "<ns1:data xsi:type=\"xsd:base64Binary\">LvVF4VWSi0WqRKps7lGH6U....OBCl1gwKGbvYM1SDh</ns1:data>\n" +
            "</ns1:getIHCProjectSegment4>\n" +
            "</SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>\n";

    // @formatter:on

    @Before
    public void setUp() throws IhcExecption, SocketTimeoutException {
        ihcControllerService = spy(new IhcControllerService(url, 0));
        doNothing().when(ihcControllerService).openConnection(eq(url));
    }

    @Test
    public void projectInfoTest() throws IhcExecption {
        doNothing().when(ihcControllerService).setRequestProperty(eq("SOAPAction"), eq("getProjectInfo"));
        doReturn(projectInfoResponse).when(ihcControllerService).sendQuery(eq(query), ArgumentMatchers.anyInt());

        final WSProjectInfo result = ihcControllerService.getProjectInfo();

        Mockito.verify(ihcControllerService).setRequestProperty(eq("SOAPAction"), eq("getProjectInfo"));

        assertEquals("Pertti 'Speedy' Keinonen", result.getCustomerName());
    }

    @Test
    public void projectNumberOfSegmentsTest() throws IhcExecption {
        doNothing().when(ihcControllerService).setRequestProperty(eq("SOAPAction"),
                eq("getIHCProjectNumberOfSegments"));
        doReturn(projectNumberOfSegmentsResponse).when(ihcControllerService).sendQuery(eq(query),
                ArgumentMatchers.anyInt());

        final int result = ihcControllerService.getProjectNumberOfSegments();

        Mockito.verify(ihcControllerService).setRequestProperty(eq("SOAPAction"), eq("getIHCProjectNumberOfSegments"));

        assertEquals(28, result);
    }

    @Test
    public void projectSegmentationSizeTest() throws IhcExecption {
        doNothing().when(ihcControllerService).setRequestProperty(eq("SOAPAction"),
                eq("getIHCProjectSegmentationSize"));
        doReturn(projectSegmentationSizeResponse).when(ihcControllerService).sendQuery(eq(query),
                ArgumentMatchers.anyInt());

        final int result = ihcControllerService.getProjectSegmentationSize();

        Mockito.verify(ihcControllerService).setRequestProperty(eq("SOAPAction"), eq("getIHCProjectSegmentationSize"));

        assertEquals(7500, result);
    }

    @Test
    public void controllerStateTest() throws IhcExecption {
        doNothing().when(ihcControllerService).setRequestProperty(eq("SOAPAction"), eq("getState"));
        doReturn(controllerStateResponse).when(ihcControllerService).sendQuery(eq(query), ArgumentMatchers.anyInt());

        final WSControllerState result = ihcControllerService.getControllerState();

        Mockito.verify(ihcControllerService).setRequestProperty(eq("SOAPAction"), eq("getState"));

        assertEquals(IhcClient.CONTROLLER_STATE_READY, result.getState());
    }

    @Test
    public void waitForControllerStateChangeQueryTest() throws IhcExecption {
        doNothing().when(ihcControllerService).setRequestProperty(eq("SOAPAction"), eq("waitForControllerStateChange"));
        doReturn(waitForControllerStateChangeResponse).when(ihcControllerService)
                .sendQuery(eq(waitForControllerStateChangeQuery), ArgumentMatchers.anyInt());

        WSControllerState previousState = new WSControllerState();
        previousState.setState(IhcClient.CONTROLLER_STATE_INITIALIZE);
        final WSControllerState result = ihcControllerService.waitStateChangeNotifications(previousState, 5);

        Mockito.verify(ihcControllerService).setRequestProperty(eq("SOAPAction"), eq("waitForControllerStateChange"));

        assertEquals(IhcClient.CONTROLLER_STATE_READY, result.getState());
    }

    @Test
    public void projectSegmentQueryTest() throws IhcExecption {
        doNothing().when(ihcControllerService).setRequestProperty(eq("SOAPAction"), eq("getIHCProjectSegment"));
        doReturn(projectSegmentResponse).when(ihcControllerService).sendQuery(eq(projectSegmentQuery),
                ArgumentMatchers.anyInt());

        final byte[] expectedResult = "LvVF4VWSi0WqRKps7lGH6U....OBCl1gwKGbvYM1SDh".getBytes();
        final WSFile result = ihcControllerService.getProjectSegment(1, 1001, 2002);

        Mockito.verify(ihcControllerService).setRequestProperty(eq("SOAPAction"), eq("getIHCProjectSegment"));

        assertTrue("Result bytes doesn't match to expected bytes", Arrays.equals(expectedResult, result.getData()));
    }

}
