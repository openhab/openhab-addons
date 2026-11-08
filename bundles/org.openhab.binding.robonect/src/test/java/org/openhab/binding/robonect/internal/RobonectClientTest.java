/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.robonect.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.robonect.internal.model.AjaxInfo;
import org.openhab.binding.robonect.internal.model.ErrorList;
import org.openhab.binding.robonect.internal.model.MowerInfo;
import org.openhab.binding.robonect.internal.model.Name;
import org.openhab.binding.robonect.internal.model.VersionInfo;
import org.openhab.binding.robonect.internal.model.cmd.DirectCommand;
import org.openhab.binding.robonect.internal.model.cmd.DoorCommand.Direction;
import org.openhab.binding.robonect.internal.model.cmd.ModeCommand;
import org.openhab.binding.robonect.internal.model.cmd.ServiceCommand.Operation;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * The goal of this class is to test the functionality of the RobonectClient,
 * by mocking the module responses.
 *
 * @author Marco Meyer - Initial contribution
 * @author Nico Brüttner - API and web interface coverage
 */
@ExtendWith(MockitoExtension.class)
public class RobonectClientTest {

    private RobonectClient subject;

    private @Mock HttpClient httpClientMock;
    private @Mock ContentResponse responseMock;
    private @Mock Request requestMock;

    /** Creates the client under test with a shared mocked HTTP client. */
    @BeforeEach
    public void init() {
        RobonectEndpoint dummyEndPoint = new RobonectEndpoint("123.456.789.123", null, null);
        subject = new RobonectClient(httpClientMock, dummyEndPoint);
    }

    /** Uses different endpoint APIs to check reuse of the same transport and authentication setup. */
    @Test
    public void shouldShareAuthenticatedTransportAcrossEndpointApis()
            throws InterruptedException, ExecutionException, TimeoutException {
        var authenticationStore = mock(org.eclipse.jetty.client.api.AuthenticationStore.class);
        when(httpClientMock.getAuthenticationStore()).thenReturn(authenticationStore);
        RobonectClient client = new RobonectClient(httpClientMock,
                new RobonectEndpoint("123.456.789.123", "user", "password"));
        String doorUrl = "http://123.456.789.123/json?cmd=door";
        String weatherUrl = "http://123.456.789.123/json?cmd=weather";
        when(httpClientMock.newRequest(doorUrl)).thenReturn(requestMock);
        when(httpClientMock.newRequest(weatherUrl)).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true}");

        assertTrue(client.mower().getDoorInfo().isSuccessful());
        assertTrue(client.weather().getWeatherInfo().isSuccessful());
        verify(authenticationStore, times(1)).addAuthenticationResult(any());
        verify(httpClientMock).newRequest(doorUrl);
        verify(httpClientMock).newRequest(weatherUrl);
    }

    /** Checks the URL used for a normal mower status request. */
    @Test
    public void shouldCallStatusCommand() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=status")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn(
                "{\"successful\": true, \"name\": \"Mein Automower\", \"status\": {\"status\": 17, \"stopped\": false, \"duration\": 4359, \"mode\": 0, \"battery\": 100, \"hours\": 29}, \"timer\": {\"status\": 2, \"next\": {\"date\": \"01.05.2017\", \"time\": \"19:00:00\", \"unix\": 1493665200}}, \"wlan\": {\"signal\": -76}}");
        subject.mower().getMowerInfo(false);
        verify(httpClientMock, times(1)).newRequest("http://123.456.789.123/json?cmd=status");
    }

    /** Checks decoding and parsing of the web interface's encoded JSON response. */
    @Test
    public void shouldDecodeAjaxUiResponse() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/ajax?type=1&group=2")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString())
                .thenReturn("%7B%22tmnucontrol%22%3A%2210%3A00+-+12%3A00%22%2C%22tmnutimer%22%3A%22Job%22%7D");

        AjaxInfo info = subject.mower().getAjaxInfo();

        assertEquals("10:00 - 12:00", info.getControlMenuText());
        assertEquals("Job", info.getTimerMenuText());
        verify(httpClientMock).newRequest("http://123.456.789.123/ajax?type=1&group=2");
    }

    /** Checks the request sent to start the mower. */
    @Test
    public void shouldCallStartCommand() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=start")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true}");
        subject.mower().start();
        verify(httpClientMock, times(1)).newRequest("http://123.456.789.123/json?cmd=start");
    }

    /** Checks the request sent to stop the mower. */
    @Test
    public void shouldCallStopCommand() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=stop")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true}");
        subject.mower().stop();
        verify(httpClientMock, times(1)).newRequest("http://123.456.789.123/json?cmd=stop");
    }

    /** Checks that a direct wheel request includes both signed values and the millisecond timeout. */
    @Test
    public void shouldSendDirectWheelCommand() throws InterruptedException, ExecutionException, TimeoutException {
        String url = "http://123.456.789.123/json?cmd=direct&left=-1000&right=1000&timeout=5000";
        when(httpClientMock.newRequest(url)).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn("{\"successful\":true}");

        assertTrue(subject.mower().drive(new DirectCommand(-1000, 1000, 5000)).isSuccessful());
        verify(httpClientMock).newRequest(url);
    }

    /** Checks that the WLAN request parses the returned network data. */
    @Test
    public void shouldRetrieveWlanInfo() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=wlan")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString())
                .thenReturn("{\"successful\": true, \"station\": {\"ssid\": \"mower\"}}");

        assertEquals("mower", subject.diagnostics().getWlanInfo().getStation().getSsid());
        verify(httpClientMock).newRequest("http://123.456.789.123/json?cmd=wlan");
    }

    /** Checks the request sent to clear mower errors. */
    @Test
    public void shouldResetErrors() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=error&reset")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true}");
        subject.mower().resetErrors();
        verify(httpClientMock, times(1)).newRequest("http://123.456.789.123/json?cmd=error&reset");
    }

    /** Checks the command parameters used for a service operation. */
    @Test
    public void shouldCallServiceOperation() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=service&sleep")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true}");

        subject.mower().performServiceOperation(Operation.SLEEP);

        verify(httpClientMock).newRequest("http://123.456.789.123/json?cmd=service&sleep");
    }

    /** Checks the direction sent when commanding the optional door. */
    @Test
    public void shouldOpenDoor() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=door&open=1")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true}");

        subject.mower().setDoorDirection(Direction.OPEN);

        verify(httpClientMock).newRequest("http://123.456.789.123/json?cmd=door&open=1");
    }

    /** Checks that explicit job times are serialized into the mower request. */
    @Test
    public void shouldStartJobWithStartAndEndTimes() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest(
                "http://123.456.789.123/json?cmd=mode&mode=job&remotestart=0&after=4&start=00:00&end=23:59&duration=60"))
                .thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true}");

        subject.mower().startJob(new RobonectClient.JobSettings().withRemoteStart(ModeCommand.RemoteStart.STANDARD)
                .withAfterMode(ModeCommand.Mode.AUTO).withStart("00:00").withEnd("23:59").withDuration(60));

        verify(httpClientMock).newRequest(
                "http://123.456.789.123/json?cmd=mode&mode=job&remotestart=0&after=4&start=00:00&end=23:59&duration=60");
    }

    /** Checks the protocol value sent for the fifth remote start point. */
    @Test
    public void shouldStartJobFromRemoteFive() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=mode&mode=job&remotestart=6"))
                .thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true}");

        subject.mower().startJob(new RobonectClient.JobSettings().withRemoteStart(ModeCommand.RemoteStart.REMOTE_5));

        verify(httpClientMock).newRequest("http://123.456.789.123/json?cmd=mode&mode=job&remotestart=6");
    }

    /** Checks the default remote start and absence of unspecified settings. */
    @Test
    public void shouldRetainOptionalJobSettings() {
        RobonectClient.JobSettings settings = new RobonectClient.JobSettings().withRemoteStart(null).withAfterMode(null)
                .withStart(null).withEnd(null);
        assertEquals(ModeCommand.RemoteStart.STANDARD, settings.getRemoteStart());
        assertNull(settings.getAfterMode());
    }

    /** Checks valid local times and rejects malformed or out-of-range values. */
    @Test
    public void shouldStoreJobTimesInHoursAndMinutes() {
        RobonectClient.JobSettings settings = new RobonectClient.JobSettings().withStart("09:05").withEnd("10:30");
        assertEquals("09:05", settings.getStart());
        assertEquals("10:30", settings.getEnd());

        assertThrows(IllegalArgumentException.class, () -> settings.withStart("24:00"));
        assertThrows(IllegalArgumentException.class, () -> settings.withEnd("1440"));
        settings.withStart(null).withEnd(null);
        assertNull(settings.getStart());
        assertNull(settings.getEnd());
    }

    /** Checks that job tracking is isolated per client and that stop can restore the scraped pre-job mode. */
    @Test
    public void shouldTrackJobsPerClientAndRestorePreJobEndOfDayMode()
            throws InterruptedException, ExecutionException, TimeoutException {
        String startUrl = "http://123.456.789.123/json?cmd=mode&mode=job&remotestart=0&after=4";
        String stopUrl = "http://123.456.789.123/json?cmd=mode&mode=eod";
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=status")).thenReturn(requestMock);
        when(httpClientMock.newRequest("http://123.456.789.123/ajax?type=1&group=2")).thenReturn(requestMock);
        when(httpClientMock.newRequest(startUrl)).thenReturn(requestMock);
        when(httpClientMock.newRequest(stopUrl)).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true, \"status\": {\"mode\": 2}}",
                "%7B%22tmnucontrol%22%3A%22End+of+day%22%7D", "{\"successful\": true, \"status\": {\"mode\": 0}}");

        RobonectClient.JobSettings settings = new RobonectClient.JobSettings().withAfterMode(ModeCommand.Mode.AUTO);
        assertEquals(Boolean.FALSE, Objects.requireNonNull(subject.mower().getMowerInfo(true).getJob()).isJobActiv());
        assertTrue(subject.mower().startJob(settings).isSuccessful());
        assertEquals(Boolean.TRUE, Objects.requireNonNull(subject.mower().getMowerInfo(false).getJob()).isJobActiv());
        RobonectClient replacement = new RobonectClient(httpClientMock,
                new RobonectEndpoint("123.456.789.123", null, null));
        assertEquals(Boolean.FALSE,
                Objects.requireNonNull(replacement.mower().getMowerInfo(false).getJob()).isJobActiv());
        assertTrue(subject.mower().stopJob(null).isSuccessful());
        assertEquals(Boolean.FALSE, Objects.requireNonNull(subject.mower().getMowerInfo(false).getJob()).isJobActiv());
        verify(httpClientMock).newRequest(startUrl);
        verify(httpClientMock).newRequest(stopUrl);
    }

    /** Checks that related remote start settings are sent in the same request. */
    @Test
    public void shouldUpdateRemoteStartValuesTogether()
            throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock
                .newRequest("http://123.456.789.123/json?cmd=remote&name2=Test+name&distance2=10&proportion2=46"))
                .thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true}");

        subject.remoteStart().updateRemoteStart(2, "Test name", 10, 46);

        verify(httpClientMock)
                .newRequest("http://123.456.789.123/json?cmd=remote&name2=Test+name&distance2=10&proportion2=46");
    }

    /** Checks the name read request and the response it produces. */
    @Test
    public void shouldRetrieveName() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=name")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true, \"name\": \"hugo\"}");
        Name name = subject.mower().getName();
        assertEquals("hugo", name.getName());
        verify(httpClientMock, times(1)).newRequest("http://123.456.789.123/json?cmd=name");
    }

    /** Checks that the requested name is sent to the mower. */
    @Test
    public void shouldSetNewName() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=name&name=MyRobo")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn("{\"successful\": true, \"name\": \"MyRobo\"}");
        Name name = subject.mower().setName("MyRobo");
        assertEquals("MyRobo", name.getName());
        verify(httpClientMock, times(1)).newRequest("http://123.456.789.123/json?cmd=name&name=MyRobo");
    }

    /** Checks the endpoint used to read error history. */
    @Test
    public void shouldListErrors() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=error")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn(
                "{\"errors\": [{\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"04.05.2017\", \"time\": \"22:22:17\", \"unix\": 1493936537}, {\"error_code\": 15, \"error_message\": \"Grasi ist angehoben\", \"date\": \"02.05.2017\", \"time\": \"20:36:43\", \"unix\": 1493757403}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"26.04.2017\", \"time\": \"21:31:18\", \"unix\": 1493242278}, {\"error_code\": 13, \"error_message\": \"Kein Antrieb\", \"date\": \"21.04.2017\", \"time\": \"20:17:22\", \"unix\": 1492805842}, {\"error_code\": 10, \"error_message\": \"Grasi ist umgedreht\", \"date\": \"20.04.2017\", \"time\": \"20:14:37\", \"unix\": 1492719277}, {\"error_code\": 1, \"error_message\": \"Grasi hat Arbeitsbereich überschritten\", \"date\": \"12.04.2017\", \"time\": \"19:10:09\", \"unix\": 1492024209}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"10.04.2017\", \"time\": \"22:59:35\", \"unix\": 1491865175}, {\"error_code\": 1, \"error_message\": \"Grasi hat Arbeitsbereich überschritten\", \"date\": \"10.04.2017\", \"time\": \"21:21:55\", \"unix\": 1491859315}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"10.04.2017\", \"time\": \"20:26:13\", \"unix\": 1491855973}, {\"error_code\": 1, \"error_message\": \"Grasi hat Arbeitsbereich überschritten\", \"date\": \"09.04.2017\", \"time\": \"14:50:36\", \"unix\": 1491749436}, {\"error_code\": 33, \"error_message\": \"Grasi ist gekippt\", \"date\": \"09.04.2017\", \"time\": \"14:23:27\", \"unix\": 1491747807}], \"successful\": true}");
        ErrorList list = subject.diagnostics().errorList();
        assertEquals(11, list.getErrors().size());
        verify(httpClientMock, times(1)).newRequest("http://123.456.789.123/json?cmd=error");
    }

    /** Checks the firmware version request and its response. */
    @Test
    public void shouldRetrieveVersionInfo() throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=version")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn(
                "{\"robonect\": {\"serial\": \"05D92D32-38355048-43203030\", \"version\": \"V0.9\", \"compiled\": \"2017-03-25 20:10:00\", \"comment\": \"V0.9c\"}, \"successful\": true}");
        VersionInfo info = subject.diagnostics().getVersionInfo();
        assertEquals("05D92D32-38355048-43203030", info.getRobonect().getSerial());
        verify(httpClientMock, times(1)).newRequest("http://123.456.789.123/json?cmd=version");
    }

    /** Checks parsing of the version fields supplied by the older firmware. */
    @Test
    public void shouldRetrieveVersionOneInformation()
            throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=version")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn(
                """
                        {"successful":true,"serial":"1234","mower":{"hardware":{"serial":456,"production":"2020"},"msw":{"title":"MSW","version":"1.2","compiled":"2020-01-01"},"sub":{"version":"1"}},"bootloader":{"version":"1"},"wlan":{"at-version":"AT","sdk-version":"SDK"},"application":{"version":"1.6","compiled":"2020-01-01","comment":"release"},"robonect":{"version":"HW1"}}
                        """);

        VersionInfo info = subject.diagnostics().getVersionInfo();

        assertEquals("1234", info.getSerial());
        assertEquals(Integer.valueOf(456), info.getMower().getHardware().getSerial());
        assertEquals("1.2", info.getMower().getMsw().getVersion());
        assertEquals("AT", info.getWlan().getAtVersion());
        assertEquals("1.6", info.getApplication().getVersion());
    }

    /** Checks that the HTTP response encoding is used when parsing non-ASCII status text. */
    @Test
    public void shouldHandleProperEncoding() throws InterruptedException, ExecutionException, TimeoutException {
        byte[] responseBytesISO88591 = "{\"successful\": true, \"name\": \"Mein Automower\", \"status\": {\"status\": 7, \"stopped\": true, \"duration\": 192, \"mode\": 1, \"battery\": 95, \"hours\": 41}, \"timer\": {\"status\": 2}, \"error\" : {\"error_code\": 15, \"error_message\": \"Utanför arbetsområdet\", \"date\": \"02.05.2017\", \"time\": \"20:36:43\", \"unix\": 1493757403}, \"wlan\": {\"signal\": -75}}"
                .getBytes(StandardCharsets.ISO_8859_1);
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=status")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn(null);
        when(responseMock.getContent()).thenReturn(responseBytesISO88591);
        MowerInfo info = subject.mower().getMowerInfo(false);
        assertEquals("Utanför arbetsområdet", info.getError().getErrorMessage());
        verify(httpClientMock, times(1)).newRequest("http://123.456.789.123/json?cmd=status");
    }

    /**
     * Checks that the parser reads the selected push mode from the web interface.
     *
     * @param mode the selected mode in the HTML fixture
     */
    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    public void shouldReadSelectedPushModeFromHtml(int mode)
            throws InterruptedException, ExecutionException, TimeoutException {
        mockWebResponse("http://123.456.789.123/push", """
                <html><body><table><tr><td>Modus:</td><td>
                <input type="radio" name="mode" value="0">
                <input type="radio" name="mode" value="1">
                <input type="radio" name="mode" value="2">
                <input value='%d' checked='checked' id='selected' name='mode' type='radio'>
                </td></tr></table></body></html>
                """.formatted(mode));
        assertEquals(mode, subject.push().getPushModeViaWebInterface());
        verify(httpClientMock).newRequest("http://123.456.789.123/push");
    }

    /**
     * Checks that absent or unsupported push mode values yield no selection.
     *
     * @param html the web-interface response containing a missing or invalid selection
     */
    @ParameterizedTest
    @ValueSource(strings = { "<html>Login required</html>", "<input type='radio' name='mode' value='1'>",
            "<input type='checkbox' name='mode' checked='checked' value='1'>",
            "<input type='radio' name='other' checked='checked' value='1'>",
            "<input type='radio' name='mode' checked='false' value='1'>",
            "<input type='radio' name='mode' checked='checked'>",
            "<input type='radio' name='mode' checked='checked' value='3'>",
            "<input type='radio' name='mode' checked='checked' value='bad'>" })
    public void shouldIgnoreMissingOrInvalidPushModeSelections(String html)
            throws InterruptedException, ExecutionException, TimeoutException {
        mockWebResponse("http://123.456.789.123/push", html);
        assertNull(subject.push().getPushModeViaWebInterface());
    }

    /**
     * Checks the web request used to write a push mode and parse the returned selection.
     *
     * @param mode the push mode to send and expect in the response
     */
    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2 })
    public void shouldSetPushModeAndParseReturnedSelection(int mode)
            throws InterruptedException, ExecutionException, TimeoutException {
        String url = "http://123.456.789.123/push&mode=" + mode;
        mockWebResponse(url, "<input type='radio' name='mode' checked='checked' value='" + mode + "'>");
        assertEquals(mode, subject.push().updatePushModeViaWebInterface(mode));
        verify(httpClientMock).newRequest(url);
    }

    /** Checks that a mode outside the supported range produces no HTTP request. */
    @Test
    public void shouldRejectInvalidPushModeBeforeSendingRequest() {
        assertThrows(IllegalArgumentException.class, () -> subject.push().updatePushModeViaWebInterface(-1));
        assertThrows(IllegalArgumentException.class, () -> subject.push().updatePushModeViaWebInterface(3));
        verifyNoInteractions(httpClientMock);
    }

    /**
     * Checks that the web-interface fallback parses the configured weather interval.
     *
     * @param interval the polling interval in minutes in the HTML fixture
     */
    @ParameterizedTest
    @ValueSource(ints = { 1, 10, 240 })
    public void shouldReadWeatherIntervalFromWebInterface(int interval)
            throws InterruptedException, ExecutionException, TimeoutException {
        mockWebResponse("http://123.456.789.123/weather",
                "<input value='" + interval + "' type='range' name='interval' id='interval'>");
        assertEquals(interval, subject.weather().getWeatherIntervalViaWebInterface());
        verify(httpClientMock).newRequest("http://123.456.789.123/weather");
    }

    /**
     * Checks that a malformed weather interval is treated as absent.
     *
     * @param html the web-interface response containing a missing or invalid interval
     */
    @ParameterizedTest
    @ValueSource(strings = { "", "<input id='other' value='10'>", "<output id='interval' value='10'>",
            "<input id='interval'>", "<input id='interval' value='bad'>", "<input id='interval' value='1.5'>",
            "<input id='interval' value='0'>", "<input id='interval' value='-1'>",
            "<input id='interval' value='2147483648'>" })
    public void shouldIgnoreMissingOrInvalidWeatherInterval(String html)
            throws InterruptedException, ExecutionException, TimeoutException {
        mockWebResponse("http://123.456.789.123/weather", html);
        assertNull(subject.weather().getWeatherIntervalViaWebInterface());
    }

    /**
     * Stubs a successful HTTP response for a localized web-interface request.
     *
     * @param url the requested endpoint URL
     * @param html the response body to return
     */
    private void mockWebResponse(String url, String html)
            throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest(url)).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenReturn(responseMock);
        when(responseMock.getEncoding()).thenReturn("utf8");
        when(responseMock.getContentAsString()).thenReturn(html);
    }

    /** Checks how an interrupted HTTP request propagates through the transport. */
    @Test
    public void shouldReceiveErrorAnswerOnInterruptedException()
            throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=status")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenThrow(new InterruptedException("Mock Interrupted Exception"));
        try {
            assertThrows(RobonectCommunicationException.class, () -> subject.mower().getMowerInfo(false));
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
    }

    /** Checks how a failed HTTP request propagates through the transport. */
    @Test
    public void shouldReceiveErrorAnswerOnExecutionException()
            throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=status")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenThrow(new ExecutionException(new Exception("Mock Exception")));
        assertThrows(RobonectCommunicationException.class, () -> subject.mower().getMowerInfo(false));
    }

    /** Checks URL encoding of the password write and parsing of the returned push settings. */
    @Test
    public void shouldEncodePushPasswordAndParseOtherSettings()
            throws InterruptedException, ExecutionException, TimeoutException {
        String url = "http://123.456.789.123/json?cmd=push&pass=a+b%26%3F%23%3D%C3%A4";
        mockWebResponse(url, "{\"successful\":true,\"push\":{\"trigger\":{\"interval\":30}}}");
        var result = subject.push().updatePush("pass", "a b&?#=ä");
        assertTrue(result.isSuccessful());
        assertEquals(30, Objects.requireNonNull(Objects.requireNonNull(result.getPush()).getTrigger()).getInterval());
        verify(httpClientMock).newRequest(url);
    }

    /** Checks that an HTTP transport failure omits the password from the exception. */
    @Test
    public void shouldNotExposePushPasswordInTransportException()
            throws InterruptedException, ExecutionException, TimeoutException {
        String password = "test-secret";
        String url = "http://123.456.789.123/json?cmd=push&pass=" + password;
        when(httpClientMock.newRequest(url)).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenThrow(new ExecutionException(new IllegalStateException(url)));
        var error = assertThrows(RobonectCommunicationException.class,
                () -> subject.push().updatePush("pass", password));
        assertFalse(error.toString().contains(password));
        assertFalse(error.toString().contains("pass="));
        assertNull(error.getCause());
    }

    /** Captures debug logs to check that neither the password-bearing URL nor response body is exposed. */
    @Test
    public void shouldNotLogPushPasswordUrlOrResponseAtDebugLevel()
            throws InterruptedException, ExecutionException, TimeoutException {
        Logger logger = (Logger) LoggerFactory.getLogger(RobonectClient.class);
        Level previousLevel = logger.getLevel();
        ListAppender<ILoggingEvent> events = new ListAppender<>();
        events.start();
        logger.addAppender(events);
        logger.setLevel(Level.DEBUG);
        try {
            mockWebResponse("http://123.456.789.123/json?cmd=push&pass=test-secret",
                    "{\"successful\":true,\"unexpected\":\"test-secret\"}");
            assertTrue(subject.push().updatePush("pass", "test-secret").isSuccessful());
            assertTrue(events.list.stream().noneMatch(event -> event.getFormattedMessage().contains("test-secret")
                    || event.getFormattedMessage().contains("pass=")));
        } finally {
            logger.detachAppender(events);
            logger.setLevel(previousLevel);
            events.stop();
        }
    }

    /** Checks redaction when constructing a password write request fails. */
    @Test
    public void shouldNotExposePushPasswordInRequestConstructionException() {
        String url = "http://123.456.789.123/json?cmd=push&pass=test-secret";
        when(httpClientMock.newRequest(url)).thenThrow(new IllegalArgumentException(url));
        var error = assertThrows(RobonectCommunicationException.class,
                () -> subject.push().updatePush("pass", "test-secret"));
        assertFalse(error.toString().contains("test-secret"));
        assertNull(error.getCause());
    }

    /** Checks redaction when the response to a password write cannot be parsed. */
    @Test
    public void shouldNotExposePasswordInMalformedResponseException()
            throws InterruptedException, ExecutionException, TimeoutException {
        mockWebResponse("http://123.456.789.123/json?cmd=push&pass=test-secret", "not-json test-secret");
        var error = assertThrows(RobonectCommunicationException.class,
                () -> subject.push().updatePush("pass", "test-secret"));
        assertFalse(error.toString().contains("test-secret"));
        assertNull(error.getCause());
    }

    /** Checks how an HTTP request timeout propagates through the transport. */
    @Test
    public void shouldReceiveErrorAnswerOnTimeoutException()
            throws InterruptedException, ExecutionException, TimeoutException {
        when(httpClientMock.newRequest("http://123.456.789.123/json?cmd=status")).thenReturn(requestMock);
        when(requestMock.method(HttpMethod.GET)).thenReturn(requestMock);
        when(requestMock.timeout(30000L, TimeUnit.MILLISECONDS)).thenReturn(requestMock);
        when(requestMock.send()).thenThrow(new TimeoutException("Mock Timeout Exception"));
        assertThrows(RobonectCommunicationException.class, () -> subject.mower().getMowerInfo(false));
    }
}
