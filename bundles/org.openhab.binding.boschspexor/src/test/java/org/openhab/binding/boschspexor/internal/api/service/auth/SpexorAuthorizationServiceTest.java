package org.openhab.binding.boschspexor.internal.api.service.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.boschspexor.internal.BoschSpexorBindingConstants.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.FakeHttpRequest;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpConversation;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.openhab.binding.boschspexor.internal.api.service.BoschSpexorBridgeConfig;
import org.openhab.binding.boschspexor.internal.api.service.auth.SpexorAuthorizationService.SpexorAuthGrantState;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.oauth2client.internal.OAuthStoreHandlerImpl;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.test.storage.VolatileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class SpexorAuthorizationServiceTest {
    private final Logger logger = LoggerFactory.getLogger(SpexorAuthorizationServiceTest.class);
    private SpexorAuthorizationService authorizationService;
    private OAuthStoreHandlerImpl oAuthStoreHandler;
    private @Mock HttpClient httpClient;
    private @Mock HttpConversation conversation;
    private StorageService storageService = new VolatileStorageService();
    private BoschSpexorBridgeConfig bridgeConfig;

    @BeforeEach
    public void setUp() {
        oAuthStoreHandler = new OAuthStoreHandlerImpl(storageService);
        bridgeConfig = new BoschSpexorBridgeConfig();
        bridgeConfig.setTesting(true);
    }

    public @NonNull Storage<String> getStorage() {
        return storageService.getStorage(BINDING_ID, String.class.getClassLoader());
    }

    @AfterEach
    public void tearDown() {
        for (String key : getStorage().getKeys()) {
            storageService.getStorage(BINDING_ID, String.class.getClassLoader()).remove(key);
        }
    }

    @Test
    public void testInitializeFirstTime() {
        authorizationService = new SpexorAuthorizationService(oAuthStoreHandler, httpClient, storageService);
        assertNotNull(authorizationService.getStatus());
        assertEquals(SpexorAuthGrantState.UNINITIALIZED, authorizationService.getStatus().getState());
        verify(httpClient, never()).newRequest(any(URI.class));
    }

    @Test
    public void testAuthorizationAccepted() {
        authorizationService = new SpexorAuthorizationService(oAuthStoreHandler, httpClient, storageService);
        authorizationService.setConfig(bridgeConfig);

        ContentResponse responseAuthorization = mock(ContentResponse.class);
        ContentResponse responseTokenNotCompleted = mock(ContentResponse.class);
        ContentResponse responseTokenCompleted = mock(ContentResponse.class);

        when(httpClient.newRequest(URI.create(bridgeConfig.buildAuthorizationUrl())))
                .thenAnswer(defineResponse(responseAuthorization));
        when(httpClient.newRequest(URI.create(bridgeConfig.buildTokenUrl())))
                .thenAnswer(defineResponse(responseTokenNotCompleted))
                .thenAnswer(defineResponse(responseTokenCompleted));

        assertNotNull(authorizationService.getStatus());
        assertEquals(SpexorAuthGrantState.UNINITIALIZED, authorizationService.getStatus().getState());
        try {
            when(responseAuthorization.getContent()).thenReturn(
                    " {\"device_code\": \"YUcTDyJOT3XmWS-AoRkE6gv4fbmpV9N5mIRBe8s5AXY\",\"user_code\": \"QAMP-ENOU\",\"expires_in\": 3600,\"interval\": 5, \"verification_uri\":\"\"}"
                            .getBytes());
            when(responseTokenNotCompleted.getContent()).thenReturn("{\"error\":\"authorization_pending\"}".getBytes());
            when(responseTokenCompleted.getContent()).thenReturn(
                    "{\"access_token\": \"access-token\",\"expires_in\": 3600,\"refresh_expires_in\": 315359795,\"refresh_token\": \"refresh-token\",\"token_type\": \"Bearer\"}"
                            .getBytes());
            authorizationService.authorize();
            assertEquals(SpexorAuthGrantState.AWAITING_USER_ACCEPTANCE, authorizationService.getStatus().getState());
            int loops = 0;
            LocalDateTime startTime = LocalDateTime.now();
            while (authorizationService.getThreadPoolExecutor().getTaskCount() > 0) {
                Thread.sleep(500);
                long timePassedInMillis = ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
                logger.debug("Checking state after {} ms ({} ms)", 500 * ++loops, timePassedInMillis);
                if (SpexorAuthGrantState.AUTHORIZED.equals(authorizationService.getStatus().getState())) {
                    assertEquals(2, authorizationService.getThreadPoolExecutor().getCompletedTaskCount());
                    // already responsed with wished result
                    break;
                }
                assertTrue(timePassedInMillis < 21000, "waiting loop took longer than expected (21 seconds max");
            }
            Thread.sleep(500);
            assertEquals(SpexorAuthGrantState.AUTHORIZED, authorizationService.getStatus().getState());
        } catch (Throwable t) {
            fail(t);
        }
    }

    @Test
    public void testAuthorizationOutdated() {
        authorizationService = new SpexorAuthorizationService(oAuthStoreHandler, httpClient, storageService);
        authorizationService.setConfig(bridgeConfig);

        ContentResponse responseAuthorization = mock(ContentResponse.class);
        ContentResponse responseTokenNotCompleted = mock(ContentResponse.class);
        ContentResponse responseTokenOutdated = mock(ContentResponse.class);

        when(httpClient.newRequest(URI.create(bridgeConfig.buildAuthorizationUrl())))
                .thenAnswer(defineResponse(responseAuthorization));
        when(httpClient.newRequest(URI.create(bridgeConfig.buildTokenUrl())))
                .thenAnswer(defineResponse(responseTokenNotCompleted))
                .thenAnswer(defineResponse(responseTokenNotCompleted))
                .thenAnswer(defineResponse(responseTokenNotCompleted))
                .thenAnswer(defineResponse(responseTokenNotCompleted))
                .thenAnswer(defineResponse(responseTokenOutdated));

        assertNotNull(authorizationService.getStatus());
        assertEquals(SpexorAuthGrantState.UNINITIALIZED, authorizationService.getStatus().getState());
        try {
            when(responseAuthorization.getContent()).thenReturn(
                    " {\"device_code\": \"YUcTDyJOT3XmWS-AoRkE6gv4fbmpV9N5mIRBe8s5AXY\",\"user_code\": \"QAMP-ENOU\",\"expires_in\": 10,\"interval\": 2, \"verification_uri\":\"\"}"
                            .getBytes());
            when(responseTokenNotCompleted.getContent()).thenReturn("{\"error\":\"authorization_pending\"}".getBytes());
            when(responseTokenOutdated.getContent()).thenReturn("{\"error\":\"expired_token\"}".getBytes());
            authorizationService.authorize();
            assertEquals(SpexorAuthGrantState.AWAITING_USER_ACCEPTANCE, authorizationService.getStatus().getState());
            int loops = 0;
            LocalDateTime startTime = LocalDateTime.now();
            while (authorizationService.getThreadPoolExecutor().getTaskCount() > 0) {
                Thread.sleep(500);
                long timePassedInMillis = ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
                logger.debug("Checking state after {} ms ({} ms)", 500 * ++loops, timePassedInMillis);
                if (SpexorAuthGrantState.CODE_REQUEST_FAILED.equals(authorizationService.getStatus().getState())) {
                    // already responsed with success
                    break;
                }
                assertTrue(timePassedInMillis < 15000, "waiting loop took longer than expected (15 seconds max");
            }
            Thread.sleep(500);
            assertEquals(SpexorAuthGrantState.CODE_REQUEST_FAILED, authorizationService.getStatus().getState());
        } catch (Throwable t) {
            fail(t);
        }
    }

    @Test
    public void testAuthorizationAcceptedAndContainsValidToken() {
        authorizationService = new SpexorAuthorizationService(oAuthStoreHandler, httpClient, storageService);
        authorizationService.setConfig(bridgeConfig);

        ContentResponse responseAuthorization = mock(ContentResponse.class);
        ContentResponse responseTokenNotCompleted = mock(ContentResponse.class);
        ContentResponse responseTokenCompleted = mock(ContentResponse.class);

        when(httpClient.newRequest(URI.create(bridgeConfig.buildAuthorizationUrl())))
                .thenAnswer(defineResponse(responseAuthorization));
        when(httpClient.newRequest(URI.create(bridgeConfig.buildTokenUrl())))
                .thenAnswer(defineResponse(responseTokenNotCompleted))
                .thenAnswer(defineResponse(responseTokenCompleted));

        assertNotNull(authorizationService.getStatus());
        assertEquals(SpexorAuthGrantState.UNINITIALIZED, authorizationService.getStatus().getState());
        try {
            when(responseAuthorization.getContent()).thenReturn(
                    " {\"device_code\": \"YUcTDyJOT3XmWS-AoRkE6gv4fbmpV9N5mIRBe8s5AXY\",\"user_code\": \"QAMP-ENOU\",\"expires_in\": 3,\"interval\": 1, \"verification_uri\":\"\"}"
                            .getBytes());
            when(responseTokenNotCompleted.getContent()).thenReturn("{\"error\":\"authorization_pending\"}".getBytes());
            when(responseTokenCompleted.getContent()).thenReturn(
                    "{\"access_token\": \"access-token\",\"expires_in\": 3600,\"refresh_expires_in\": 315359795,\"refresh_token\": \"refresh-token\",\"token_type\": \"Bearer\"}"
                            .getBytes());
            authorizationService.authorize();
            assertEquals(SpexorAuthGrantState.AWAITING_USER_ACCEPTANCE, authorizationService.getStatus().getState());
            int loops = 0;
            LocalDateTime startTime = LocalDateTime.now();
            while (authorizationService.getThreadPoolExecutor().getTaskCount() > 0) {
                Thread.sleep(500);
                long timePassedInMillis = ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
                logger.debug("Checking state after {} ms ({} ms)", 500 * ++loops, timePassedInMillis);
                if (SpexorAuthGrantState.AUTHORIZED.equals(authorizationService.getStatus().getState())) {
                    assertEquals(2, authorizationService.getThreadPoolExecutor().getCompletedTaskCount());
                    // already responsed with wished result
                    break;
                }
                assertTrue(timePassedInMillis < 5000, "waiting loop took longer than expected (5 seconds max");
            }
            Thread.sleep(500);
            assertEquals(SpexorAuthGrantState.AUTHORIZED, authorizationService.getStatus().getState());
            AccessTokenResponse accessToken1 = oAuthStoreHandler
                    .loadAccessTokenResponse(getConstantBinding(bridgeConfig.getScope()));
            assertNotNull(accessToken1);
            authorizationService.authorize();
            assertEquals(SpexorAuthGrantState.AUTHORIZED, authorizationService.getStatus().getState());
            AccessTokenResponse accessToken2 = oAuthStoreHandler
                    .loadAccessTokenResponse(getConstantBinding(bridgeConfig.getScope()));
            assertEquals(accessToken1, accessToken2);
            assertEquals("access-token", accessToken2.getAccessToken());
            assertEquals(3600, accessToken2.getExpiresIn());
            assertEquals("refresh-token", accessToken2.getRefreshToken());
            assertTrue(
                    accessToken2.getCreatedOn().plusSeconds(accessToken2.getExpiresIn()).isAfter(LocalDateTime.now()));
        } catch (Throwable t) {
            fail(t);
        }
    }

    @Test
    public void testAuthorizationAcceptedAndRefreshTokenWasRequestedAgain() {
        authorizationService = new SpexorAuthorizationService(oAuthStoreHandler, httpClient, storageService);
        authorizationService.setConfig(bridgeConfig);

        ContentResponse responseAuthorization = mock(ContentResponse.class);
        ContentResponse responseTokenNotCompleted = mock(ContentResponse.class);
        ContentResponse responseTokenCompleted = mock(ContentResponse.class);
        ContentResponse responseRefreshTokenCompleted = mock(ContentResponse.class);

        when(httpClient.newRequest(URI.create(bridgeConfig.buildAuthorizationUrl())))
                .thenAnswer(defineResponse(responseAuthorization));
        when(httpClient.newRequest(URI.create(bridgeConfig.buildTokenUrl())))
                .thenAnswer(defineResponse(responseTokenNotCompleted))
                .thenAnswer(defineResponse(responseTokenCompleted));
        when(httpClient.newRequest(URI.create(bridgeConfig.buildRefreshUrl())))
                .thenAnswer(defineResponse(responseRefreshTokenCompleted));

        assertNotNull(authorizationService.getStatus());
        assertEquals(SpexorAuthGrantState.UNINITIALIZED, authorizationService.getStatus().getState());
        try {
            when(responseAuthorization.getContent()).thenReturn(
                    " {\"device_code\": \"YUcTDyJOT3XmWS-AoRkE6gv4fbmpV9N5mIRBe8s5AXY\",\"user_code\": \"QAMP-ENOU\",\"expires_in\": 3,\"interval\": 1, \"verification_uri\":\"\"}"
                            .getBytes());
            when(responseTokenNotCompleted.getContent()).thenReturn("{\"error\":\"authorization_pending\"}".getBytes());
            when(responseTokenCompleted.getContent()).thenReturn(
                    "{\"access_token\": \"access-token1\",\"expires_in\": 1,\"refresh_expires_in\": 315359795,\"refresh_token\": \"refresh-token\",\"token_type\": \"Bearer\"}"
                            .getBytes());
            when(responseRefreshTokenCompleted.getContent()).thenReturn(
                    "{\"access_token\": \"access-token2\",\"expires_in\": 3600,\"refresh_expires_in\": 315359795,\"refresh_token\": \"refresh-token\",\"token_type\": \"Bearer\"}"
                            .getBytes());
            authorizationService.authorize();
            assertEquals(SpexorAuthGrantState.AWAITING_USER_ACCEPTANCE, authorizationService.getStatus().getState());
            int loops = 0;
            LocalDateTime startTime = LocalDateTime.now();
            while (authorizationService.getThreadPoolExecutor().getTaskCount() > 0) {
                Thread.sleep(500);
                long timePassedInMillis = ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
                logger.debug("Checking state after {} ms ({} ms)", 500 * ++loops, timePassedInMillis);
                if (SpexorAuthGrantState.AUTHORIZED.equals(authorizationService.getStatus().getState())) {
                    assertEquals(2, authorizationService.getThreadPoolExecutor().getCompletedTaskCount());
                    // already responsed with wished result
                    break;
                }
                assertTrue(timePassedInMillis < 5000, "waiting loop took longer than expected (5 seconds max");
            }
            Thread.sleep(500);
            assertEquals(SpexorAuthGrantState.AUTHORIZED, authorizationService.getStatus().getState());
            AccessTokenResponse accessToken1 = oAuthStoreHandler
                    .loadAccessTokenResponse(getConstantBinding(bridgeConfig.getScope()));
            assertNotNull(accessToken1);
            assertEquals("access-token1", accessToken1.getAccessToken());
            assertEquals("refresh-token", accessToken1.getRefreshToken());
            Thread.sleep(1000);
            assertEquals(1, accessToken1.getExpiresIn());
            assertTrue(
                    accessToken1.getCreatedOn().plusSeconds(accessToken1.getExpiresIn()).isBefore(LocalDateTime.now()));
            authorizationService.authorize();
            assertEquals(SpexorAuthGrantState.AUTHORIZED, authorizationService.getStatus().getState());
            AccessTokenResponse accessToken2 = oAuthStoreHandler
                    .loadAccessTokenResponse(getConstantBinding(bridgeConfig.getScope()));
            assertNotEquals(accessToken1, accessToken2);
            assertNotNull(accessToken2);
            assertEquals(3600, accessToken2.getExpiresIn());
            assertEquals("access-token2", accessToken2.getAccessToken());
            assertEquals("refresh-token", accessToken2.getRefreshToken());
            assertTrue(
                    accessToken2.getCreatedOn().plusSeconds(accessToken2.getExpiresIn()).isAfter(LocalDateTime.now()));
        } catch (Throwable t) {
            fail(t);
        }
    }

    private Answer<Request> defineResponse(ContentResponse response) {
        return new Answer<Request>() {
            @Override
            public @Nullable Request answer(@NonNull InvocationOnMock invocation) throws Throwable {
                Request request = spy(new FakeHttpRequest(httpClient, conversation, invocation.getArgument(0)));
                doReturn(response).when(request).send();
                return request;
            }
        };
    }
}
