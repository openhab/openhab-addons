/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.ihc.internal.ws.services;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.net.SocketTimeoutException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.openhab.binding.ihc.internal.ws.IhcClient;
import org.openhab.binding.ihc.internal.ws.ResourceFileUtils;
import org.openhab.binding.ihc.internal.ws.datatypes.WSControllerState;
import org.openhab.binding.ihc.internal.ws.datatypes.WSFile;
import org.openhab.binding.ihc.internal.ws.datatypes.WSProjectInfo;
import org.openhab.binding.ihc.internal.ws.exeptions.IhcExecption;
import org.openhab.binding.ihc.internal.ws.http.IhcConnectionPool;

/**
 * Test for IHC / ELKO binding
 *
 * @author Pauli Anttila - Initial contribution
 */
public class IhcControllerServiceTest {

    private IhcControllerService ihcControllerService;
    private final String url = "https://1.1.1.1/ws/ControllerService";

    private String query;

    @Before
    public void setUp() throws IhcExecption, SocketTimeoutException {
        ihcControllerService = spy(new IhcControllerService(url, 0, new IhcConnectionPool()));
        doNothing().when(ihcControllerService).openConnection(eq(url));

        query = ResourceFileUtils.getFileContent("src/test/resources/EmptyQuery.xml");
    }

    @Test
    public void projectInfoTest() throws IhcExecption {
        doNothing().when(ihcControllerService).setRequestProperty(eq("SOAPAction"), eq("getProjectInfo"));

        final String projectInfoResponse = ResourceFileUtils
                .getFileContent("src/test/resources/GetProjectInfoResponse.xml");

        doReturn(projectInfoResponse).when(ihcControllerService).sendQuery(eq(query), ArgumentMatchers.anyInt());

        final WSProjectInfo result = ihcControllerService.getProjectInfo();

        Mockito.verify(ihcControllerService).setRequestProperty(eq("SOAPAction"), eq("getProjectInfo"));
        assertEquals("Pertti 'Speedy' Keinonen", result.getCustomerName());
    }

    @Test
    public void projectNumberOfSegmentsTest() throws IhcExecption {
        doNothing().when(ihcControllerService).setRequestProperty(eq("SOAPAction"),
                eq("getIHCProjectNumberOfSegments"));

        final String projectNumberOfSegmentsResponse = ResourceFileUtils
                .getFileContent("src/test/resources/GetProjectNumberOfSegmentsResponse.xml");

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

        final String projectSegmentationSizeResponse = ResourceFileUtils
                .getFileContent("src/test/resources/GetProjectSegmentationSizeResponse.xml");

        doReturn(projectSegmentationSizeResponse).when(ihcControllerService).sendQuery(eq(query),
                ArgumentMatchers.anyInt());

        final int result = ihcControllerService.getProjectSegmentationSize();

        Mockito.verify(ihcControllerService).setRequestProperty(eq("SOAPAction"), eq("getIHCProjectSegmentationSize"));
        assertEquals(7500, result);
    }

    @Test
    public void controllerStateTest() throws IhcExecption {
        doNothing().when(ihcControllerService).setRequestProperty(eq("SOAPAction"), eq("getState"));

        final String controllerStateResponse = ResourceFileUtils
                .getFileContent("src/test/resources/ControllerStateResponse.xml");

        doReturn(controllerStateResponse).when(ihcControllerService).sendQuery(eq(query), ArgumentMatchers.anyInt());

        final WSControllerState result = ihcControllerService.getControllerState();

        Mockito.verify(ihcControllerService).setRequestProperty(eq("SOAPAction"), eq("getState"));
        assertEquals(IhcClient.CONTROLLER_STATE_READY, result.getState());
    }

    @Test
    public void waitForControllerStateChangeQueryTest() throws IhcExecption {
        doNothing().when(ihcControllerService).setRequestProperty(eq("SOAPAction"), eq("waitForControllerStateChange"));

        final String waitForControllerStateChangeQuery = ResourceFileUtils
                .getFileContent("src/test/resources/WaitForControllerStateChangeQuery.xml");
        final String waitForControllerStateChangeResponse = ResourceFileUtils
                .getFileContent("src/test/resources/WaitForControllerStateChangeResponse.xml");

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

        final String projectSegmentQuery = ResourceFileUtils
                .getFileContent("src/test/resources/GetProjectSegmentQuery.xml");
        final String projectSegmentResponse = ResourceFileUtils
                .getFileContent("src/test/resources/GetProjectSegmentResponse.xml");

        doReturn(projectSegmentResponse).when(ihcControllerService).sendQuery(eq(projectSegmentQuery),
                ArgumentMatchers.anyInt());

        final byte[] expectedResult = "LvVF4VWSi0WqRKps7lGH6U....OBCl1gwKGbvYM1SDh".getBytes();
        final WSFile result = ihcControllerService.getProjectSegment(1, 1001, 2002);

        Mockito.verify(ihcControllerService).setRequestProperty(eq("SOAPAction"), eq("getIHCProjectSegment"));
        assertTrue("Result bytes doesn't match to expected bytes", Arrays.equals(expectedResult, result.getData()));
    }

}
