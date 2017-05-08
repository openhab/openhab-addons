package org.openhab.binding.robonect.model.org.openhab.binding.robonect;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.robonect.RobonectClient;
import org.openhab.binding.robonect.RobonectEndpoint;
import org.openhab.binding.robonect.model.ErrorList;
import org.openhab.binding.robonect.model.MowerInfo;
import org.openhab.binding.robonect.model.Name;
import org.openhab.binding.robonect.model.VersionInfo;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RobonectClientTest {

    private RobonectClient client;

    @Mock
    private HttpClient httpClientMock;

    @Mock
    private ContentResponse responseMock;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        RobonectEndpoint dummyEndPoint = new RobonectEndpoint("123.456.789.123", "user", "password");
        client = new RobonectClient(httpClientMock, dummyEndPoint);
    }

    @Test
    public void shouldCallStatusCommand() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.GET("http://123.456.789.123/json?cmd=status")).thenReturn(responseMock);
        when(responseMock.getContentAsString()).thenReturn(
                "{\"successful\": true, \"name\": \"Mein Automower\", \"status\": {\"status\": 17, \"stopped\": false, \"duration\": 4359, \"mode\": 0, \"battery\": 100, \"hours\": 29}, \"timer\": {\"status\": 2, \"next\": {\"date\": \"01.05.2017\", \"time\": \"19:00:00\", \"unix\": 1493665200}}, \"wlan\": {\"signal\": -76}}");
        client.getMowerInfo();
        verify(httpClientMock, times(1)).GET("http://123.456.789.123/json?cmd=status");
    }

    @Test
    public void shouldCallStartCommand() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.GET("http://123.456.789.123/json?cmd=start")).thenReturn(responseMock);
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true}");
        client.start();
        verify(httpClientMock, times(1)).GET("http://123.456.789.123/json?cmd=start");
    }

    @Test
    public void shouldCallStopCommand() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.GET("http://123.456.789.123/json?cmd=stop")).thenReturn(responseMock);
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true}");
        client.stop();
        verify(httpClientMock, times(1)).GET("http://123.456.789.123/json?cmd=stop");
    }

    @Test
    public void shouldResetErrors() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.GET("http://123.456.789.123/json?cmd=error&reset")).thenReturn(responseMock);
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true}");
        client.resetErrors();
        verify(httpClientMock, times(1)).GET("http://123.456.789.123/json?cmd=error&reset");         
    }

    @Test
    public void shouldRetrieveName() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.GET("http://123.456.789.123/json?cmd=name")).thenReturn(responseMock);
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true, \"name\": \"hugo\"}");
        Name name = client.getName();
        assertEquals("hugo", name.getName());
        verify(httpClientMock, times(1)).GET("http://123.456.789.123/json?cmd=name");
    }

    @Test
    public void shouldSetNewName() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.GET("http://123.456.789.123/json?cmd=name&name=MyRobo")).thenReturn(responseMock);
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true, \"name\": \"MyRobo\"}");
        Name name = client.setName("MyRobo");
        assertEquals("MyRobo", name.getName());
        verify(httpClientMock, times(1)).GET("http://123.456.789.123/json?cmd=name&name=MyRobo");
    }

    @Test
    public void shouldListErrors() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.GET("http://123.456.789.123/json?cmd=error")).thenReturn(responseMock);
        when(responseMock.getContentAsString()).thenReturn("{\"errors\": [{\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"04.05.2017\", \"time\": \"22:22:17\", \"unix\": 1493936537}, {\"error_code\": 15, \"error_message\": \"Grasi ist angehoben\", \"date\": \"02.05.2017\", \"time\": \"20:36:43\", \"unix\": 1493757403}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"26.04.2017\", \"time\": \"21:31:18\", \"unix\": 1493242278}, {\"error_code\": 13, \"error_message\": \"Kein Antrieb\", \"date\": \"21.04.2017\", \"time\": \"20:17:22\", \"unix\": 1492805842}, {\"error_code\": 10, \"error_message\": \"Grasi ist umgedreht\", \"date\": \"20.04.2017\", \"time\": \"20:14:37\", \"unix\": 1492719277}, {\"error_code\": 1, \"error_message\": \"Grasi hat Arbeitsbereich überschritten\", \"date\": \"12.04.2017\", \"time\": \"19:10:09\", \"unix\": 1492024209}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"10.04.2017\", \"time\": \"22:59:35\", \"unix\": 1491865175}, {\"error_code\": 1, \"error_message\": \"Grasi hat Arbeitsbereich überschritten\", \"date\": \"10.04.2017\", \"time\": \"21:21:55\", \"unix\": 1491859315}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"10.04.2017\", \"time\": \"20:26:13\", \"unix\": 1491855973}, {\"error_code\": 1, \"error_message\": \"Grasi hat Arbeitsbereich überschritten\", \"date\": \"09.04.2017\", \"time\": \"14:50:36\", \"unix\": 1491749436}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"09.04.2017\", \"time\": \"14:23:27\", \"unix\": 1491747807}], \"successful\": true}");
        ErrorList list = client.errorList();
        assertEquals(11, list.getErrors().size());
        verify(httpClientMock, times(1)).GET("http://123.456.789.123/json?cmd=error");
    }

    @Test
    public void shouldRetrieveVersionInfo() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.GET("http://123.456.789.123/json?cmd=version")).thenReturn(responseMock);
        when(responseMock.getContentAsString()).thenReturn(
                "{\"robonect\": {\"serial\": \"05D92D32-38355048-43203030\", \"version\": \"V0.9\", \"compiled\": \"2017-03-25 20:10:00\", \"comment\": \"V0.9c\"}, \"successful\": true}");
        VersionInfo info = client.getVersionInfo();
        assertEquals("05D92D32-38355048-43203030", info.getRobonect().getSerial());
        verify(httpClientMock, times(1)).GET("http://123.456.789.123/json?cmd=version");
    }

    @Test
    public void shouldReceiveErrorAnswerOnInterruptedException()
            throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.GET("http://123.456.789.123/json?cmd=status"))
                .thenThrow(new InterruptedException("Mock Interrupted Exception"));
        MowerInfo answer = client.getMowerInfo();
        assertEquals("Mock Interrupted Exception", answer.getErrorMessage());
        assertEquals(new Integer(999), answer.getErrorCode());
    }

    @Test
    public void shouldReceiveErrorAnswerOnExecutionException()
            throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.GET("http://123.456.789.123/json?cmd=status"))
                .thenThrow(new ExecutionException(new Exception("Mock Exception")));
        MowerInfo answer = client.getMowerInfo();
        assertEquals("java.lang.Exception: Mock Exception", answer.getErrorMessage());
        assertEquals(new Integer(888), answer.getErrorCode());
    }

    @Test
    public void shouldReceiveErrorAnswerOnTimeoutException()
            throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.GET("http://123.456.789.123/json?cmd=status"))
                .thenThrow(new TimeoutException("Mock Timeout Exception"));
        MowerInfo answer = client.getMowerInfo();
        assertEquals("Mock Timeout Exception", answer.getErrorMessage());
        assertEquals(new Integer(777), answer.getErrorCode());
    }

}
