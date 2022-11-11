/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mielecloud.internal.util.ReflectionUtil.getPrivate;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpFields;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingTestConstants;
import org.openhab.binding.mielecloud.internal.auth.OAuthTokenRefresher;
import org.openhab.binding.mielecloud.internal.util.MockUtil;
import org.openhab.binding.mielecloud.internal.webservice.api.json.ProcessAction;
import org.openhab.binding.mielecloud.internal.webservice.exception.AuthorizationFailedException;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceException;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceTransientException;
import org.openhab.binding.mielecloud.internal.webservice.exception.TooManyRequestsException;
import org.openhab.binding.mielecloud.internal.webservice.language.LanguageProvider;
import org.openhab.binding.mielecloud.internal.webservice.request.RequestFactory;
import org.openhab.binding.mielecloud.internal.webservice.retry.AuthorizationFailedRetryStrategy;
import org.openhab.binding.mielecloud.internal.webservice.retry.NTimesRetryStrategy;
import org.openhab.binding.mielecloud.internal.webservice.retry.RetryStrategy;
import org.openhab.binding.mielecloud.internal.webservice.retry.RetryStrategyCombiner;
import org.openhab.binding.mielecloud.internal.webservice.sse.ServerSentEvent;
import org.openhab.core.io.net.http.HttpClientFactory;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class DefaultMieleWebserviceTest {
    private static final String MESSAGE_INTERNAL_SERVER_ERROR = "{\"message\": \"Internal Server Error\"}";
    private static final String MESSAGE_SERVICE_UNAVAILABLE = "{\"message\": \"unavailable\"}";
    private static final String MESSAGE_INVALID_JSON = "{\"abc123: \"äfgh\"}";

    private static final String DEVICE_IDENTIFIER = "000124430016";

    private static final String SERVER_ADDRESS = "https://api.mcs3.miele.com";
    private static final String ENDPOINT_DEVICES = SERVER_ADDRESS + "/v1/devices/";
    private static final String ENDPOINT_EXTENSION_ACTIONS = "/actions";
    private static final String ENDPOINT_ACTIONS = ENDPOINT_DEVICES + DEVICE_IDENTIFIER + ENDPOINT_EXTENSION_ACTIONS;
    private static final String ENDPOINT_LOGOUT = SERVER_ADDRESS + "/thirdparty/logout";

    private static final String ACCESS_TOKEN = "DE_0123456789abcdef0123456789abcdef";

    private final RetryStrategy retryStrategy = new UncatchedRetryStrategy();
    private final Request request = mock(Request.class);

    @Test
    public void testDefaultRetryStrategyIsCombinationOfOneTimeRetryStrategyAndAuthorizationFailedStrategy()
            throws Exception {
        // given:
        HttpClientFactory httpClientFactory = mock(HttpClientFactory.class);
        when(httpClientFactory.createHttpClient(anyString())).thenReturn(MockUtil.mockHttpClient());
        LanguageProvider languageProvider = mock(LanguageProvider.class);
        OAuthTokenRefresher tokenRefresher = mock(OAuthTokenRefresher.class);
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        // when:
        DefaultMieleWebservice webservice = new DefaultMieleWebservice(MieleWebserviceConfiguration.builder()
                .withHttpClientFactory(httpClientFactory).withLanguageProvider(languageProvider)
                .withTokenRefresher(tokenRefresher).withServiceHandle(MieleCloudBindingTestConstants.SERVICE_HANDLE)
                .withScheduler(scheduler).build());

        // then:
        RetryStrategy retryStrategy = getPrivate(webservice, "retryStrategy");
        assertTrue(retryStrategy instanceof RetryStrategyCombiner);

        RetryStrategy first = getPrivate(retryStrategy, "first");
        assertTrue(first instanceof NTimesRetryStrategy);
        int numberOfRetries = getPrivate(first, "numberOfRetries");
        assertEquals(1, numberOfRetries);

        RetryStrategy second = getPrivate(retryStrategy, "second");
        assertTrue(second instanceof AuthorizationFailedRetryStrategy);
        OAuthTokenRefresher internalTokenRefresher = getPrivate(second, "tokenRefresher");
        assertEquals(tokenRefresher, internalTokenRefresher);
    }

    private ContentResponse createContentResponseMock(int errorCode, String content) {
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(errorCode);
        when(response.getContentAsString()).thenReturn(content);
        return response;
    }

    private void performFetchActions() throws Exception {
        RequestFactory requestFactory = mock(RequestFactory.class);
        when(requestFactory.createGetRequest(ENDPOINT_ACTIONS, ACCESS_TOKEN)).thenReturn(request);

        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        try (DefaultMieleWebservice webservice = new DefaultMieleWebservice(requestFactory, retryStrategy,
                new DeviceStateDispatcher(), scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            webservice.fetchActions(DEVICE_IDENTIFIER);
        }
    }

    private void performFetchActionsExpectingFailure(ConnectionError expectedError) throws Exception {
        try {
            performFetchActions();
        } catch (MieleWebserviceException e) {
            assertEquals(expectedError, e.getConnectionError());
            throw e;
        } catch (MieleWebserviceTransientException e) {
            assertEquals(expectedError, e.getConnectionError());
            throw e;
        }
    }

    @Test
    public void testTimeoutExceptionWhilePerformingFetchActionsRequest() throws Exception {
        // given:
        when(request.send()).thenThrow(TimeoutException.class);

        // when:
        assertThrows(MieleWebserviceTransientException.class, () -> {
            performFetchActionsExpectingFailure(ConnectionError.TIMEOUT);
        });
    }

    @Test
    public void test500InternalServerErrorWhilePerformingFetchActionsRequest() throws Exception {
        // given:
        ContentResponse contentResponse = createContentResponseMock(500, MESSAGE_INTERNAL_SERVER_ERROR);
        when(request.send()).thenReturn(contentResponse);

        // when:
        assertThrows(MieleWebserviceTransientException.class, () -> {
            performFetchActionsExpectingFailure(ConnectionError.SERVER_ERROR);
        });
    }

    @Test
    public void test503ServiceUnavailableWhilePerformingFetchActionsRequest() throws Exception {
        // given:
        ContentResponse contentResponse = createContentResponseMock(503, MESSAGE_SERVICE_UNAVAILABLE);
        when(request.send()).thenReturn(contentResponse);

        // when:
        assertThrows(MieleWebserviceTransientException.class, () -> {
            performFetchActionsExpectingFailure(ConnectionError.SERVICE_UNAVAILABLE);
        });
    }

    @Test
    public void testInvalidJsonWhilePerformingFetchActionsRequest() throws Exception {
        // given:
        ContentResponse contentResponse = createContentResponseMock(200, MESSAGE_INVALID_JSON);
        when(request.send()).thenReturn(contentResponse);

        // when:
        assertThrows(MieleWebserviceTransientException.class, () -> {
            performFetchActionsExpectingFailure(ConnectionError.RESPONSE_MALFORMED);
        });
    }

    @Test
    public void testInterruptedExceptionWhilePerformingFetchActionsRequest() throws Exception {
        // given:
        when(request.send()).thenThrow(InterruptedException.class);

        // when:
        assertThrows(MieleWebserviceException.class, () -> {
            performFetchActionsExpectingFailure(ConnectionError.REQUEST_INTERRUPTED);
        });
    }

    @Test
    public void testExecutionExceptionWhilePerformingFetchActionsRequest() throws Exception {
        // given:
        when(request.send()).thenThrow(ExecutionException.class);

        // when:
        assertThrows(MieleWebserviceException.class, () -> {
            performFetchActionsExpectingFailure(ConnectionError.REQUEST_EXECUTION_FAILED);
        });
    }

    @Test
    public void test400BadRequestWhilePerformingFetchActionsRequest() throws Exception {
        // given:
        ContentResponse response = createContentResponseMock(400, "{\"message\": \"grant_type is invalid\"}");
        when(request.send()).thenReturn(response);

        // when:
        assertThrows(MieleWebserviceException.class, () -> {
            performFetchActionsExpectingFailure(ConnectionError.OTHER_HTTP_ERROR);
        });
    }

    @Test
    public void test401UnauthorizedWhilePerformingFetchActionsRequest() throws Exception {
        // given:
        ContentResponse response = createContentResponseMock(401, "{\"message\": \"Unauthorized\"}");
        when(request.send()).thenReturn(response);

        // when:
        assertThrows(AuthorizationFailedException.class, () -> {
            performFetchActions();
        });
    }

    @Test
    public void test404NotFoundWhilePerformingFetchActionsRequest() throws Exception {
        // given:
        ContentResponse response = createContentResponseMock(404, "{\"message\": \"Not found\"}");
        when(request.send()).thenReturn(response);

        // when:
        assertThrows(MieleWebserviceException.class, () -> {
            performFetchActionsExpectingFailure(ConnectionError.OTHER_HTTP_ERROR);
        });
    }

    @Test
    public void test405MethodNotAllowedWhilePerformingFetchActionsRequest() throws Exception {
        // given:
        ContentResponse response = createContentResponseMock(405, "{\"message\": \"HTTP 405 Method Not Allowed\"}");
        when(request.send()).thenReturn(response);

        // when:
        assertThrows(MieleWebserviceException.class, () -> {
            performFetchActionsExpectingFailure(ConnectionError.OTHER_HTTP_ERROR);
        });
    }

    @Test
    public void test429TooManyRequestsWhilePerformingFetchActionsRequest() throws Exception {
        // given:
        HttpFields headerFields = mock(HttpFields.class);
        when(headerFields.containsKey(anyString())).thenReturn(false);

        ContentResponse response = createContentResponseMock(429, "{\"message\": \"Too Many Requests\"}");
        when(response.getHeaders()).thenReturn(headerFields);

        when(request.send()).thenReturn(response);

        // when:
        assertThrows(TooManyRequestsException.class, () -> {
            performFetchActions();
        });
    }

    @Test
    public void test502BadGatewayWhilePerforminggFetchActionsRequest() throws Exception {
        // given:
        ContentResponse response = createContentResponseMock(502, "{\"message\": \"Bad Gateway\"}");
        when(request.send()).thenReturn(response);

        // when:
        assertThrows(MieleWebserviceException.class, () -> {
            performFetchActionsExpectingFailure(ConnectionError.OTHER_HTTP_ERROR);
        });
    }

    @Test
    public void testMalformatedBodyWhilePerforminggFetchActionsRequest() throws Exception {
        // given:
        ContentResponse response = createContentResponseMock(502, "{\"message \"Bad Gateway\"}");
        when(request.send()).thenReturn(response);

        // when:
        assertThrows(MieleWebserviceException.class, () -> {
            performFetchActionsExpectingFailure(ConnectionError.OTHER_HTTP_ERROR);
        });
    }

    private void fillRequestMockWithDefaultContent() throws InterruptedException, TimeoutException, ExecutionException {
        ContentResponse response = createContentResponseMock(200,
                "{\"000124430016\":{\"ident\": {\"deviceName\": \"MyFancyHood\", \"deviceIdentLabel\": {\"fabNumber\": \"000124430016\"}}}}");
        when(request.send()).thenReturn(response);
    }

    @Test
    public void testAddDeviceStateListenerIsDelegatedToDeviceStateDispatcher() throws Exception {
        // given:
        RequestFactory requestFactory = mock(RequestFactory.class);
        DeviceStateDispatcher dispatcher = mock(DeviceStateDispatcher.class);
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        try (DefaultMieleWebservice webservice = new DefaultMieleWebservice(requestFactory, new NTimesRetryStrategy(0),
                dispatcher, scheduler)) {
            DeviceStateListener listener = mock(DeviceStateListener.class);

            // when:
            webservice.addDeviceStateListener(listener);

            // then:
            verify(dispatcher).addListener(listener);
            verifyNoMoreInteractions(dispatcher);
        }
    }

    @Test
    public void testFetchActionsDelegatesDeviceStateListenerDispatchingToDeviceStateDispatcher() throws Exception {
        // given:
        fillRequestMockWithDefaultContent();

        RequestFactory requestFactory = mock(RequestFactory.class);
        when(requestFactory.createGetRequest(ENDPOINT_ACTIONS, ACCESS_TOKEN)).thenReturn(request);

        DeviceStateDispatcher dispatcher = mock(DeviceStateDispatcher.class);

        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        try (DefaultMieleWebservice webservice = new DefaultMieleWebservice(requestFactory, retryStrategy, dispatcher,
                scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            // when:
            webservice.fetchActions(DEVICE_IDENTIFIER);

            // then:
            verify(dispatcher).dispatchActionStateUpdates(any(), any());
            verifyNoMoreInteractions(dispatcher);
        }
    }

    @Test
    public void testFetchActionsThrowsMieleWebserviceTransientExceptionWhenRequestContentIsMalformatted()
            throws Exception {
        // given:
        ContentResponse response = createContentResponseMock(200, "{\"}");
        when(request.send()).thenReturn(response);

        RequestFactory requestFactory = mock(RequestFactory.class);
        when(requestFactory.createGetRequest(ENDPOINT_ACTIONS, ACCESS_TOKEN)).thenReturn(request);

        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        try (DefaultMieleWebservice webservice = new DefaultMieleWebservice(requestFactory, retryStrategy,
                new DeviceStateDispatcher(), scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            // when:
            assertThrows(MieleWebserviceTransientException.class, () -> {
                webservice.fetchActions(DEVICE_IDENTIFIER);
            });
        }
    }

    @Test
    public void testPutProcessActionSendsRequestWithCorrectJsonContent() throws Exception {
        // given:
        ContentResponse response = createContentResponseMock(204, "");
        when(request.send()).thenReturn(response);

        RequestFactory requestFactory = mock(RequestFactory.class);
        when(requestFactory.createPutRequest(ENDPOINT_ACTIONS, ACCESS_TOKEN, "{\"processAction\":1}"))
                .thenReturn(request);

        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        try (DefaultMieleWebservice webservice = new DefaultMieleWebservice(requestFactory, new NTimesRetryStrategy(0),
                new DeviceStateDispatcher(), scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            // when:
            webservice.putProcessAction(DEVICE_IDENTIFIER, ProcessAction.START);

            // then:
            verify(request).send();
            verifyNoMoreInteractions(request);
        }
    }

    @Test
    public void testPutProcessActionThrowsIllegalArgumentExceptionWhenProcessActionIsUnknown() throws Exception {
        // given:
        RequestFactory requestFactory = mock(RequestFactory.class);

        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        try (DefaultMieleWebservice webservice = new DefaultMieleWebservice(requestFactory, retryStrategy,
                new DeviceStateDispatcher(), scheduler)) {

            // when:
            assertThrows(IllegalArgumentException.class, () -> {
                webservice.putProcessAction(DEVICE_IDENTIFIER, ProcessAction.UNKNOWN);
            });
        }
    }

    @Test
    public void testPutProcessActionThrowsTooManyRequestsExceptionWhenHttpResponseCodeIs429() throws Exception {
        // given:
        HttpFields responseHeaders = mock(HttpFields.class);
        when(responseHeaders.containsKey(anyString())).thenReturn(false);

        ContentResponse response = createContentResponseMock(429, "{\"message\":\"Too many requests\"}");
        when(response.getHeaders()).thenReturn(responseHeaders);

        when(request.send()).thenReturn(response);

        RequestFactory requestFactory = mock(RequestFactory.class);
        when(requestFactory.createPutRequest(ENDPOINT_ACTIONS, ACCESS_TOKEN, "{\"processAction\":1}"))
                .thenReturn(request);

        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        try (DefaultMieleWebservice webservice = new DefaultMieleWebservice(requestFactory, new NTimesRetryStrategy(0),
                new DeviceStateDispatcher(), scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            // when:
            assertThrows(TooManyRequestsException.class, () -> {
                webservice.putProcessAction(DEVICE_IDENTIFIER, ProcessAction.START);
            });
        }
    }

    @Test
    public void testPutLightSendsRequestWithCorrectJsonContentWhenTurningTheLightOn() throws Exception {
        // given:
        ContentResponse response = createContentResponseMock(204, "");
        when(request.send()).thenReturn(response);

        RequestFactory requestFactory = mock(RequestFactory.class);
        when(requestFactory.createPutRequest(ENDPOINT_ACTIONS, ACCESS_TOKEN, "{\"light\":1}")).thenReturn(request);

        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        try (DefaultMieleWebservice webservice = new DefaultMieleWebservice(requestFactory, new NTimesRetryStrategy(0),
                new DeviceStateDispatcher(), scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            // when:
            webservice.putLight(DEVICE_IDENTIFIER, true);

            // then:
            verify(request).send();
            verifyNoMoreInteractions(request);
        }
    }

    @Test
    public void testPutLightSendsRequestWithCorrectJsonContentWhenTurningTheLightOff() throws Exception {
        // given:
        ContentResponse response = createContentResponseMock(204, "");
        when(request.send()).thenReturn(response);

        RequestFactory requestFactory = mock(RequestFactory.class);
        when(requestFactory.createPutRequest(ENDPOINT_ACTIONS, ACCESS_TOKEN, "{\"light\":2}")).thenReturn(request);

        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        try (DefaultMieleWebservice webservice = new DefaultMieleWebservice(requestFactory, new NTimesRetryStrategy(0),
                new DeviceStateDispatcher(), scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            // when:
            webservice.putLight(DEVICE_IDENTIFIER, false);

            // then:
            verify(request).send();
            verifyNoMoreInteractions(request);
        }
    }

    @Test
    public void testPutLightThrowsTooManyRequestsExceptionWhenHttpResponseCodeIs429() throws Exception {
        // given:
        HttpFields responseHeaders = mock(HttpFields.class);
        when(responseHeaders.containsKey(anyString())).thenReturn(false);

        ContentResponse response = createContentResponseMock(429, "{\"message\":\"Too many requests\"}");
        when(response.getHeaders()).thenReturn(responseHeaders);

        when(request.send()).thenReturn(response);

        RequestFactory requestFactory = mock(RequestFactory.class);
        when(requestFactory.createPutRequest(ENDPOINT_ACTIONS, ACCESS_TOKEN, "{\"light\":2}")).thenReturn(request);

        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        try (DefaultMieleWebservice webservice = new DefaultMieleWebservice(requestFactory, new NTimesRetryStrategy(0),
                new DeviceStateDispatcher(), scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            // when:
            assertThrows(TooManyRequestsException.class, () -> {
                webservice.putLight(DEVICE_IDENTIFIER, false);
            });
        }
    }

    @Test
    public void testLogoutInvalidatesAccessTokenOnSuccess() throws Exception {
        // given:
        ContentResponse response = createContentResponseMock(204, "");
        when(request.send()).thenReturn(response);

        RequestFactory requestFactory = mock(RequestFactory.class);
        when(requestFactory.createPostRequest(ENDPOINT_LOGOUT, ACCESS_TOKEN)).thenReturn(request);

        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        try (DefaultMieleWebservice webservice = new DefaultMieleWebservice(requestFactory, retryStrategy,
                new DeviceStateDispatcher(), scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            // when:
            webservice.logout();

            // then:
            assertFalse(webservice.hasAccessToken());
            verify(request).send();
            verifyNoMoreInteractions(request);
        }
    }

    @Test
    public void testLogoutThrowsMieleWebserviceExceptionWhenMieleWebserviceTransientExceptionIsThrownInternally()
            throws Exception {
        // given:
        when(request.send()).thenThrow(TimeoutException.class);

        RequestFactory requestFactory = mock(RequestFactory.class);
        when(requestFactory.createPostRequest(ENDPOINT_LOGOUT, ACCESS_TOKEN)).thenReturn(request);

        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        try (DefaultMieleWebservice webservice = new DefaultMieleWebservice(requestFactory, retryStrategy,
                new DeviceStateDispatcher(), scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            // when:
            assertThrows(MieleWebserviceException.class, () -> {
                webservice.logout();
            });
        }
    }

    @Test
    public void testLogoutInvalidatesAccessTokenWhenOperationFails() throws Exception {
        // given:
        when(request.send()).thenThrow(TimeoutException.class);

        RequestFactory requestFactory = mock(RequestFactory.class);
        when(requestFactory.createPostRequest(ENDPOINT_LOGOUT, ACCESS_TOKEN)).thenReturn(request);

        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        try (DefaultMieleWebservice webservice = new DefaultMieleWebservice(requestFactory, retryStrategy,
                new DeviceStateDispatcher(), scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            // when:
            try {
                webservice.logout();
            } catch (MieleWebserviceException e) {
            }

            // then:
            assertFalse(webservice.hasAccessToken());
        }
    }

    @Test
    public void testRemoveDeviceStateListenerIsDelegatedToDeviceStateDispatcher() throws Exception {
        // given:
        RequestFactory requestFactory = mock(RequestFactory.class);

        DeviceStateDispatcher dispatcher = mock(DeviceStateDispatcher.class);

        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        try (DefaultMieleWebservice webservice = new DefaultMieleWebservice(requestFactory, new NTimesRetryStrategy(0),
                dispatcher, scheduler)) {
            DeviceStateListener listener = mock(DeviceStateListener.class);
            webservice.addDeviceStateListener(listener);

            // when:
            webservice.removeDeviceStateListener(listener);

            // then:
            verify(dispatcher).addListener(listener);
            verify(dispatcher).removeListener(listener);
            verifyNoMoreInteractions(dispatcher);
        }
    }

    @Test
    public void testPutPowerStateSendsRequestWithCorrectJsonContentWhenSwitchingTheDeviceOn() throws Exception {
        // given:
        ContentResponse response = createContentResponseMock(204, "");
        when(request.send()).thenReturn(response);

        RequestFactory requestFactory = mock(RequestFactory.class);
        when(requestFactory.createPutRequest(ENDPOINT_ACTIONS, ACCESS_TOKEN, "{\"powerOn\":true}")).thenReturn(request);

        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        try (DefaultMieleWebservice webservice = new DefaultMieleWebservice(requestFactory, new NTimesRetryStrategy(0),
                new DeviceStateDispatcher(), scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            // when:
            webservice.putPowerState(DEVICE_IDENTIFIER, true);

            // then:
            verify(request).send();
            verifyNoMoreInteractions(request);
        }
    }

    @Test
    public void testPutPowerStateSendsRequestWithCorrectJsonContentWhenDeviceIsSwitchedOff() throws Exception {
        // given:
        ContentResponse response = createContentResponseMock(204, "");
        when(request.send()).thenReturn(response);

        RequestFactory requestFactory = mock(RequestFactory.class);
        when(requestFactory.createPutRequest(ENDPOINT_ACTIONS, ACCESS_TOKEN, "{\"powerOff\":true}"))
                .thenReturn(request);

        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        try (DefaultMieleWebservice webservice = new DefaultMieleWebservice(requestFactory, new NTimesRetryStrategy(0),
                new DeviceStateDispatcher(), scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            // when:
            webservice.putPowerState(DEVICE_IDENTIFIER, false);

            // then:
            verify(request).send();
            verifyNoMoreInteractions(request);
        }
    }

    @Test
    public void testPutPowerStateThrowsTooManyRequestsExceptionWhenHttpResponseCodeIs429() throws Exception {
        // given:
        HttpFields responseHeaders = mock(HttpFields.class);
        when(responseHeaders.containsKey(anyString())).thenReturn(false);

        ContentResponse response = createContentResponseMock(429, "{\"message\":\"Too many requests\"}");
        when(response.getHeaders()).thenReturn(responseHeaders);

        when(request.send()).thenReturn(response);

        RequestFactory requestFactory = mock(RequestFactory.class);
        when(requestFactory.createPutRequest(ENDPOINT_ACTIONS, ACCESS_TOKEN, "{\"powerOff\":true}"))
                .thenReturn(request);

        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        try (DefaultMieleWebservice webservice = new DefaultMieleWebservice(requestFactory, new NTimesRetryStrategy(0),
                new DeviceStateDispatcher(), scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            // when:
            assertThrows(TooManyRequestsException.class, () -> {
                webservice.putPowerState(DEVICE_IDENTIFIER, false);
            });
        }
    }

    @Test
    public void testPutProgramResultsInARequestWithCorrectJson() throws Exception {
        // given:
        ContentResponse response = createContentResponseMock(204, "");
        when(request.send()).thenReturn(response);
        RequestFactory requestFactory = mock(RequestFactory.class);
        when(requestFactory.createPutRequest(ENDPOINT_ACTIONS, ACCESS_TOKEN, "{\"programId\":1}")).thenReturn(request);

        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        try (DefaultMieleWebservice webservice = new DefaultMieleWebservice(requestFactory, new NTimesRetryStrategy(0),
                new DeviceStateDispatcher(), scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            // when:
            webservice.putProgram(DEVICE_IDENTIFIER, 1);

            // then:
            verify(request).send();
            verifyNoMoreInteractions(request);
        }
    }

    @Test
    public void testDispatchDeviceStateIsDelegatedToDeviceStateDispatcher() throws Exception {
        // given:
        RequestFactory requestFactory = mock(RequestFactory.class);
        DeviceStateDispatcher dispatcher = mock(DeviceStateDispatcher.class);
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);

        try (DefaultMieleWebservice webservice = new DefaultMieleWebservice(requestFactory, new NTimesRetryStrategy(0),
                dispatcher, scheduler)) {
            // when:
            webservice.dispatchDeviceState(DEVICE_IDENTIFIER);

            // then:
            verify(dispatcher).dispatchDeviceState(DEVICE_IDENTIFIER);
            verifyNoMoreInteractions(dispatcher);
        }
    }

    @Test
    public void receivingSseActionsEventNotifiesConnectionAlive() throws Exception {
        // given:
        var requestFactory = mock(RequestFactory.class);
        var dispatcher = mock(DeviceStateDispatcher.class);
        var scheduler = mock(ScheduledExecutorService.class);

        var connectionStatusListener = mock(ConnectionStatusListener.class);

        try (var webservice = new DefaultMieleWebservice(requestFactory, new NTimesRetryStrategy(0), dispatcher,
                scheduler)) {
            webservice.addConnectionStatusListener(connectionStatusListener);

            var actionsEvent = new ServerSentEvent(DefaultMieleWebservice.SSE_EVENT_TYPE_ACTIONS, "{}");

            // when:
            webservice.onServerSentEvent(actionsEvent);

            // then:
            verify(connectionStatusListener).onConnectionAlive();
        }
    }

    @Test
    public void receivingSseActionsEventWithNonJsonPayloadDoesNothing() throws Exception {
        // given:
        var requestFactory = mock(RequestFactory.class);
        var dispatcher = mock(DeviceStateDispatcher.class);
        var scheduler = mock(ScheduledExecutorService.class);

        try (var webservice = new DefaultMieleWebservice(requestFactory, new NTimesRetryStrategy(0), dispatcher,
                scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            var actionsEvent = new ServerSentEvent(DefaultMieleWebservice.SSE_EVENT_TYPE_ACTIONS,
                    "{\"" + DEVICE_IDENTIFIER + "\": {}");

            // when:
            webservice.onServerSentEvent(actionsEvent);

            // then:
            verifyNoMoreInteractions(dispatcher);
        }
    }

    @Test
    public void receivingSseActionsEventFetchesActionsForADevice() throws Exception {
        // given:
        var requestFactory = mock(RequestFactory.class);
        when(requestFactory.createGetRequest(ENDPOINT_ACTIONS, ACCESS_TOKEN)).thenReturn(request);

        var response = createContentResponseMock(200, "{}");
        when(request.send()).thenReturn(response);

        var dispatcher = mock(DeviceStateDispatcher.class);
        var scheduler = mock(ScheduledExecutorService.class);

        try (var webservice = new DefaultMieleWebservice(requestFactory, new NTimesRetryStrategy(0), dispatcher,
                scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            var actionsEvent = new ServerSentEvent(DefaultMieleWebservice.SSE_EVENT_TYPE_ACTIONS,
                    "{\"" + DEVICE_IDENTIFIER + "\": {}}");

            // when:
            webservice.onServerSentEvent(actionsEvent);

            // then:
            verify(dispatcher).dispatchActionStateUpdates(eq(DEVICE_IDENTIFIER), any());
            verifyNoMoreInteractions(dispatcher);
        }
    }

    @Test
    public void receivingSseActionsEventFetchesActionsForMultipleDevices() throws Exception {
        // given:
        var otherRequest = mock(Request.class);
        var otherDeviceIdentifier = "000124430017";

        var requestFactory = mock(RequestFactory.class);
        when(requestFactory.createGetRequest(ENDPOINT_ACTIONS, ACCESS_TOKEN)).thenReturn(request);
        when(requestFactory.createGetRequest(ENDPOINT_DEVICES + otherDeviceIdentifier + ENDPOINT_EXTENSION_ACTIONS,
                ACCESS_TOKEN)).thenReturn(otherRequest);

        var response = createContentResponseMock(200, "{}");
        when(request.send()).thenReturn(response);
        when(otherRequest.send()).thenReturn(response);

        var dispatcher = mock(DeviceStateDispatcher.class);
        var scheduler = mock(ScheduledExecutorService.class);

        try (var webservice = new DefaultMieleWebservice(requestFactory, new NTimesRetryStrategy(0), dispatcher,
                scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            var actionsEvent = new ServerSentEvent(DefaultMieleWebservice.SSE_EVENT_TYPE_ACTIONS,
                    "{\"" + DEVICE_IDENTIFIER + "\": {}, \"" + otherDeviceIdentifier + "\": {}}");

            // when:
            webservice.onServerSentEvent(actionsEvent);

            // then:
            verify(dispatcher).dispatchActionStateUpdates(eq(DEVICE_IDENTIFIER), any());
            verify(dispatcher).dispatchActionStateUpdates(eq(otherDeviceIdentifier), any());
            verifyNoMoreInteractions(dispatcher);
        }
    }

    @Test
    public void whenFetchingActionsAfterReceivingSseActionsEventFailsForADeviceThenNothingHappensForThisDevice()
            throws Exception {
        // given:
        var otherRequest = mock(Request.class);
        var otherDeviceIdentifier = "000124430017";

        var requestFactory = mock(RequestFactory.class);
        when(requestFactory.createGetRequest(ENDPOINT_ACTIONS, ACCESS_TOKEN)).thenReturn(request);
        when(requestFactory.createGetRequest(ENDPOINT_DEVICES + otherDeviceIdentifier + ENDPOINT_EXTENSION_ACTIONS,
                ACCESS_TOKEN)).thenReturn(otherRequest);

        var response = createContentResponseMock(200, "{}");
        when(request.send()).thenReturn(response);
        var otherResponse = createContentResponseMock(405, "{\"message\": \"HTTP 405 Method Not Allowed\"}");
        when(otherRequest.send()).thenReturn(otherResponse);

        var dispatcher = mock(DeviceStateDispatcher.class);
        var scheduler = mock(ScheduledExecutorService.class);

        try (var webservice = new DefaultMieleWebservice(requestFactory, new NTimesRetryStrategy(0), dispatcher,
                scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            var actionsEvent = new ServerSentEvent(DefaultMieleWebservice.SSE_EVENT_TYPE_ACTIONS,
                    "{\"" + DEVICE_IDENTIFIER + "\": {}, \"" + otherDeviceIdentifier + "\": {}}");

            // when:
            webservice.onServerSentEvent(actionsEvent);

            // then:
            verify(dispatcher).dispatchActionStateUpdates(eq(DEVICE_IDENTIFIER), any());
            verifyNoMoreInteractions(dispatcher);
        }
    }

    @Test
    public void whenFetchingActionsAfterReceivingSseActionsEventFailsBecauseOfTooManyRequestsThenNothingHappens()
            throws Exception {
        // given:
        var requestFactory = mock(RequestFactory.class);
        when(requestFactory.createGetRequest(ENDPOINT_ACTIONS, ACCESS_TOKEN)).thenReturn(request);

        var response = createContentResponseMock(429, "{\"message\": \"Too Many Requests\"}");
        when(request.send()).thenReturn(response);

        var headerFields = mock(HttpFields.class);
        when(headerFields.containsKey(anyString())).thenReturn(false);
        when(response.getHeaders()).thenReturn(headerFields);

        var dispatcher = mock(DeviceStateDispatcher.class);
        var scheduler = mock(ScheduledExecutorService.class);

        try (var webservice = new DefaultMieleWebservice(requestFactory, new NTimesRetryStrategy(0), dispatcher,
                scheduler)) {
            webservice.setAccessToken(ACCESS_TOKEN);

            var actionsEvent = new ServerSentEvent(DefaultMieleWebservice.SSE_EVENT_TYPE_ACTIONS,
                    "{\"" + DEVICE_IDENTIFIER + "\": {}}");

            // when:
            webservice.onServerSentEvent(actionsEvent);

            // then:
            verifyNoMoreInteractions(dispatcher);
        }
    }

    @Test
    public void whenFetchingActionsAfterReceivingSseActionsEventFailsBecauseOfAuthorizationFailedThenThisIsNotifiedToListeners()
            throws Exception {
        // given:
        var requestFactory = mock(RequestFactory.class);
        when(requestFactory.createGetRequest(ENDPOINT_ACTIONS, ACCESS_TOKEN)).thenReturn(request);

        var response = createContentResponseMock(401, "{\"message\": \"Unauthorized\"}");
        when(request.send()).thenReturn(response);

        var dispatcher = mock(DeviceStateDispatcher.class);
        var scheduler = mock(ScheduledExecutorService.class);

        var connectionStatusListener = mock(ConnectionStatusListener.class);

        try (var webservice = new DefaultMieleWebservice(requestFactory, new NTimesRetryStrategy(0), dispatcher,
                scheduler)) {
            webservice.addConnectionStatusListener(connectionStatusListener);
            webservice.setAccessToken(ACCESS_TOKEN);

            var actionsEvent = new ServerSentEvent(DefaultMieleWebservice.SSE_EVENT_TYPE_ACTIONS,
                    "{\"" + DEVICE_IDENTIFIER + "\": {}}");

            // when:
            webservice.onServerSentEvent(actionsEvent);

            // then:
            verifyNoMoreInteractions(dispatcher);
            verify(connectionStatusListener).onConnectionError(ConnectionError.AUTHORIZATION_FAILED, 0);
        }
    }

    /**
     * {@link RetryStrategy} for testing purposes. No exceptions will be catched.
     *
     * @author Roland Edelhoff - Initial contribution.
     */
    private static class UncatchedRetryStrategy implements RetryStrategy {

        @Override
        public <@Nullable T> T performRetryableOperation(Supplier<T> operation,
                Consumer<Exception> onTransientException) {
            return operation.get();
        }

        @Override
        public void performRetryableOperation(Runnable operation, Consumer<Exception> onTransientException) {
            operation.run();
        }
    }
}
