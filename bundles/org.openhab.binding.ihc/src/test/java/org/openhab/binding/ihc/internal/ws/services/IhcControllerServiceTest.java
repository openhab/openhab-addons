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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
    private final String host = "1.1.1.1";
    private final String url = "https://1.1.1.1/ws/ControllerService";
    private Map<String, String> requestProps = new HashMap<>();
    private String query;
    private final int timeout = 100;

    @BeforeEach
    public void setUp() throws IhcExecption, SocketTimeoutException {
        ihcControllerService = spy(new IhcControllerService(host, timeout, new IhcConnectionPool()));

        query = ResourceFileUtils.getFileContent("EmptyQuery.xml");
        requestProps.clear();
    }

    @Test
    public void projectInfoTest() throws IhcExecption {
        requestProps.put("SOAPAction", "getProjectInfo");
        final String projectInfoResponse = ResourceFileUtils.getFileContent("GetProjectInfoResponse.xml");

        doReturn(projectInfoResponse).when(ihcControllerService).sendQuery(eq(url), eq(requestProps), eq(query),
                eq(timeout));

        final WSProjectInfo result = ihcControllerService.getProjectInfo();

        assertEquals("Pertti 'Speedy' Keinonen", result.getCustomerName());
    }

    @Test
    public void projectNumberOfSegmentsTest() throws IhcExecption {
        final String projectNumberOfSegmentsResponse = ResourceFileUtils
                .getFileContent("GetProjectNumberOfSegmentsResponse.xml");

        requestProps.put("SOAPAction", "getIHCProjectNumberOfSegments");
        doReturn(projectNumberOfSegmentsResponse).when(ihcControllerService).sendQuery(eq(url), eq(requestProps),
                eq(query), eq(timeout));

        final int result = ihcControllerService.getProjectNumberOfSegments();

        assertEquals(28, result);
    }

    @Test
    public void projectSegmentationSizeTest() throws IhcExecption {
        final String projectSegmentationSizeResponse = ResourceFileUtils
                .getFileContent("GetProjectSegmentationSizeResponse.xml");

        requestProps.put("SOAPAction", "getIHCProjectSegmentationSize");

        doReturn(projectSegmentationSizeResponse).when(ihcControllerService).sendQuery(eq(url), eq(requestProps),
                eq(query), anyInt());

        final int result = ihcControllerService.getProjectSegmentationSize();

        assertEquals(7500, result);
    }

    @Test
    public void controllerStateTest() throws IhcExecption {
        final String controllerStateResponse = ResourceFileUtils.getFileContent("ControllerStateResponse.xml");

        requestProps.put("SOAPAction", "getState");
        doReturn(controllerStateResponse).when(ihcControllerService).sendQuery(eq(url), eq(requestProps), eq(query),
                anyInt());

        final WSControllerState result = ihcControllerService.getControllerState();

        assertEquals(IhcClient.CONTROLLER_STATE_READY, result.getState());
    }

    @Test
    public void waitForControllerStateChangeQueryTest() throws IhcExecption {
        final String waitForControllerStateChangeQuery = ResourceFileUtils
                .getFileContent("WaitForControllerStateChangeQuery.xml");
        final String waitForControllerStateChangeResponse = ResourceFileUtils
                .getFileContent("WaitForControllerStateChangeResponse.xml");

        requestProps.put("SOAPAction", "waitForControllerStateChange");
        doReturn(waitForControllerStateChangeResponse).when(ihcControllerService).sendQuery(eq(url), eq(requestProps),
                eq(waitForControllerStateChangeQuery), anyInt());

        WSControllerState previousState = new WSControllerState();
        previousState.setState(IhcClient.CONTROLLER_STATE_INITIALIZE);
        final WSControllerState result = ihcControllerService.waitStateChangeNotifications(previousState, 5);

        assertEquals(IhcClient.CONTROLLER_STATE_READY, result.getState());
    }

    @Test
    public void projectSegmentQueryTest() throws IhcExecption {
        final String projectSegmentQuery = ResourceFileUtils.getFileContent("GetProjectSegmentQuery.xml");
        final String projectSegmentResponse = ResourceFileUtils.getFileContent("GetProjectSegmentResponse.xml");

        requestProps.put("SOAPAction", "getIHCProjectSegment");
        doReturn(projectSegmentResponse).when(ihcControllerService).sendQuery(eq(url), eq(requestProps),
                eq(projectSegmentQuery), anyInt());

        final byte[] expectedResult = "LvVF4VWSi0WqRKps7lGH6U....OBCl1gwKGbvYM1SDh".getBytes();
        final WSFile result = ihcControllerService.getProjectSegment(1, 1001, 2002);

        assertTrue(Arrays.equals(expectedResult, result.getData()), "Result bytes doesn't match to expected bytes");
    }
}
