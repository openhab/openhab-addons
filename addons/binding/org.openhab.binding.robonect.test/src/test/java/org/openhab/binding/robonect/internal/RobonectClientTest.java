/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.internal;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.robonect.internal.model.ErrorList;
import org.openhab.binding.robonect.internal.model.MowerInfo;
import org.openhab.binding.robonect.internal.model.Name;
import org.openhab.binding.robonect.internal.model.VersionInfo;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The goal of this class is to test the functionality of the RobonectClient,
 * by mocking the module responses.
 * 
 * @author Marco Meyer - Initial contribution
 */
public class RobonectClientTest {

    private RobonectClient subject;

    @Mock
    private HttpClient httpClientMock;

    @Mock
    private ContentResponse responseMock;

    @Mock
    private Request requestMock;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        RobonectEndpoint dummyEndPoint = new RobonectEndpoint("123.456.789.123", null, null);
        subject = new RobonectClient(httpClientMock, dummyEndPoint);
    }

    @Test
    public void shouldCallStatusCommand() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=status")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn(
                "{\"successful\": true, \"name\": \"Mein Automower\", \"status\": {\"status\": 17, \"stopped\": false, \"duration\": 4359, \"mode\": 0, \"battery\": 100, \"hours\": 29}, \"timer\": {\"status\": 2, \"next\": {\"date\": \"01.05.2017\", \"time\": \"19:00:00\", \"unix\": 1493665200}}, \"wlan\": {\"signal\": -76}}");
        subject.getMowerInfo();
        verify(httpClientMock, times(1)).newRequest("http://123.456.789.123/json?cmd=status");
    }

    @Test
    public void shouldCallStartCommand() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=start")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true}");
        subject.start();
        verify(httpClientMock, times(1)).newRequest("http://123.456.789.123/json?cmd=start");
    }

    @Test
    public void shouldCallStopCommand() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=stop")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true}");
        subject.stop();
        verify(httpClientMock, times(1)).newRequest("http://123.456.789.123/json?cmd=stop");
    }

    @Test
    public void shouldResetErrors() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=error&reset")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true}");
        subject.resetErrors();
        verify(httpClientMock, times(1)).newRequest("http://123.456.789.123/json?cmd=error&reset");
    }

    @Test
    public void shouldRetrieveName() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=name")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true, \"name\": \"hugo\"}");
        Name name = subject.getName();
        assertEquals("hugo", name.getName());
        verify(httpClientMock, times(1)).newRequest("http://123.456.789.123/json?cmd=name");
    }

    @Test
    public void shouldSetNewName() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=name&name=MyRobo")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true, \"name\": \"MyRobo\"}");
        Name name = subject.setName("MyRobo");
        assertEquals("MyRobo", name.getName());
        verify(httpClientMock, times(1)).newRequest("http://123.456.789.123/json?cmd=name&name=MyRobo");
    }

    @Test
    public void shouldListErrors() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=error")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn(
                "{\"errors\": [{\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"04.05.2017\", \"time\": \"22:22:17\", \"unix\": 1493936537}, {\"error_code\": 15, \"error_message\": \"Grasi ist angehoben\", \"date\": \"02.05.2017\", \"time\": \"20:36:43\", \"unix\": 1493757403}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"26.04.2017\", \"time\": \"21:31:18\", \"unix\": 1493242278}, {\"error_code\": 13, \"error_message\": \"Kein Antrieb\", \"date\": \"21.04.2017\", \"time\": \"20:17:22\", \"unix\": 1492805842}, {\"error_code\": 10, \"error_message\": \"Grasi ist umgedreht\", \"date\": \"20.04.2017\", \"time\": \"20:14:37\", \"unix\": 1492719277}, {\"error_code\": 1, \"error_message\": \"Grasi hat Arbeitsbereich überschritten\", \"date\": \"12.04.2017\", \"time\": \"19:10:09\", \"unix\": 1492024209}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"10.04.2017\", \"time\": \"22:59:35\", \"unix\": 1491865175}, {\"error_code\": 1, \"error_message\": \"Grasi hat Arbeitsbereich überschritten\", \"date\": \"10.04.2017\", \"time\": \"21:21:55\", \"unix\": 1491859315}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"10.04.2017\", \"time\": \"20:26:13\", \"unix\": 1491855973}, {\"error_code\": 1, \"error_message\": \"Grasi hat Arbeitsbereich überschritten\", \"date\": \"09.04.2017\", \"time\": \"14:50:36\", \"unix\": 1491749436}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"09.04.2017\", \"time\": \"14:23:27\", \"unix\": 1491747807}], \"successful\": true}");
        ErrorList list = subject.errorList();
        assertEquals(11, list.getErrors().size());
        verify(httpClientMock, times(1)).newRequest("http://123.456.789.123/json?cmd=error");
    }

    @Test
    public void shouldRetrieveVersionInfo() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=version")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn(
                "{\"robonect\": {\"serial\": \"05D92D32-38355048-43203030\", \"version\": \"V0.9\", \"compiled\": \"2017-03-25 20:10:00\", \"comment\": \"V0.9c\"}, \"successful\": true}");
        VersionInfo info = subject.getVersionInfo();
        assertEquals("05D92D32-38355048-43203030", info.getRobonect().getSerial());
        verify(httpClientMock, times(1)).newRequest("http://123.456.789.123/json?cmd=version");
    }
    
    @Test
    public void shouldHandleProperEncoding() throws InterruptedException, ExecutionException, TimeoutException {
        byte[] responseBytesISO88591 = "{\"successful\": true, \"name\": \"Mein Automower\", \"status\": {\"status\": 7, \"stopped\": true, \"duration\": 192, \"mode\": 1, \"battery\": 95, \"hours\": 41}, \"timer\": {\"status\": 2}, \"error\" : {\"error_code\": 15, \"error_message\": \"Utanför arbetsområdet\", \"date\": \"02.05.2017\", \"time\": \"20:36:43\", \"unix\": 1493757403}, \"wlan\": {\"signal\": -75}}".getBytes(
                StandardCharsets.ISO_8859_1);
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=status")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn(null);
        when(responseMock.getContent()).thenReturn(responseBytesISO88591);
        MowerInfo info = subject.getMowerInfo();
        assertEquals("Utanför arbetsområdet", info.getError().getErrorMessage());
        verify(httpClientMock, times(1)).newRequest("http://123.456.789.123/json?cmd=status");
    }

    @Test(expected = RobonectCommunicationException.class)
    public void shouldReceiveErrorAnswerOnInterruptedException()
            throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=status")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenThrow(new InterruptedException("Mock Interrupted Exception"));
        MowerInfo answer = subject.getMowerInfo();
    }

    @Test(expected = RobonectCommunicationException.class)
    public void shouldReceiveErrorAnswerOnExecutionException()
            throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=status")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenThrow(new ExecutionException(new Exception("Mock Exception")));
        MowerInfo answer = subject.getMowerInfo();
    }

    @Test(expected = RobonectCommunicationException.class)
    public void shouldReceiveErrorAnswerOnTimeoutException()
            throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=status")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenThrow(new TimeoutException("Mock Timeout Exception"));
        MowerInfo answer = subject.getMowerInfo();
    }

}
