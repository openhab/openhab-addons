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
package org.openhab.binding.mielecloud.internal.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mielecloud.internal.util.ReflectionUtil.getPrivate;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingTestConstants;
import org.openhab.binding.mielecloud.internal.auth.OAuthException;
import org.openhab.binding.mielecloud.internal.config.exception.NoOngoingAuthorizationException;
import org.openhab.binding.mielecloud.internal.config.exception.OngoingAuthorizationException;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.thing.ThingUID;

/**
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class OAuthAuthorizationHandlerImplTest {
    private static final String CLIENT_ID = "01234567-890a-bcde-f012-34567890abcd";
    private static final String CLIENT_SECRET = "0123456789abcdefghijklmnopqrstiu";
    private static final String REDIRECT_URL = "http://127.0.0.1:8080/mielecloud/result";
    private static final String AUTH_CODE = "abcdef";
    private static final ThingUID BRIDGE_UID = new ThingUID(MieleCloudBindingConstants.THING_TYPE_BRIDGE,
            MieleCloudBindingTestConstants.BRIDGE_ID);
    private static final String EMAIL = "openhab@openhab.org";

    @Nullable
    private OAuthClientService clientService;
    @Nullable
    private ScheduledFuture<?> timer;
    @Nullable
    private Runnable scheduledRunnable;
    @Nullable
    private OAuthAuthorizationHandler authorizationHandler;

    private OAuthClientService getClientService() {
        final OAuthClientService clientService = this.clientService;
        assertNotNull(clientService);
        return Objects.requireNonNull(clientService);
    }

    private ScheduledFuture<?> getTimer() {
        final ScheduledFuture<?> timer = this.timer;
        assertNotNull(timer);
        return Objects.requireNonNull(timer);
    }

    private Runnable getScheduledRunnable() {
        final Runnable scheduledRunnable = this.scheduledRunnable;
        assertNotNull(scheduledRunnable);
        return Objects.requireNonNull(scheduledRunnable);
    }

    private OAuthAuthorizationHandler getAuthorizationHandler() {
        final OAuthAuthorizationHandler authorizationHandler = this.authorizationHandler;
        assertNotNull(authorizationHandler);
        return Objects.requireNonNull(authorizationHandler);
    }

    @BeforeEach
    public void setUp() {
        OAuthClientService clientService = mock(OAuthClientService.class);

        OAuthFactory oauthFactory = mock(OAuthFactory.class);
        when(oauthFactory.createOAuthClientService(anyString(), anyString(), anyString(), anyString(), anyString(),
                isNull(), any())).thenReturn(clientService);

        ScheduledFuture<?> timer = mock(ScheduledFuture.class);
        when(timer.isDone()).thenReturn(false);

        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        when(scheduler.schedule(ArgumentMatchers.<Runnable> any(), anyLong(), any())).thenAnswer(invocation -> {
            scheduledRunnable = invocation.getArgument(0);
            return timer;
        });

        OAuthAuthorizationHandler authorizationHandler = new OAuthAuthorizationHandlerImpl(oauthFactory, scheduler);

        this.clientService = clientService;
        this.timer = timer;
        this.scheduledRunnable = null;
        this.authorizationHandler = authorizationHandler;
    }

    @Test
    public void whenTheAuthorizationIsCompletedInTimeThenTheTimerIsCancelledAndAllResourcesAreCleanedUp()
            throws Exception {
        // given:
        getAuthorizationHandler().beginAuthorization(CLIENT_ID, CLIENT_SECRET, BRIDGE_UID, EMAIL);
        getAuthorizationHandler().getAuthorizationUrl(REDIRECT_URL);

        // when:
        getAuthorizationHandler().completeAuthorization("http://127.0.0.1:8080/mielecloud/result?code=abc&state=def");

        // then:
        assertNull(getPrivate(getAuthorizationHandler(), "timer"));
        verify(getTimer()).cancel(false);

        assertNull(getPrivate(getAuthorizationHandler(), "oauthClientService"));
        assertNull(getPrivate(getAuthorizationHandler(), "bridgeUid"));
        assertNull(getPrivate(getAuthorizationHandler(), "redirectUri"));
        assertNull(getPrivate(getAuthorizationHandler(), "email"));

        verify(getClientService()).extractAuthCodeFromAuthResponse(anyString());
        verify(getClientService()).getAccessTokenResponseByAuthorizationCode(isNull(), anyString());
    }

    @Test
    public void whenTheAuthorizationTimesOutThenTheOngoingAuthorizationIsCancelled() throws Exception {
        // given:
        getAuthorizationHandler().beginAuthorization(CLIENT_ID, CLIENT_SECRET, BRIDGE_UID, EMAIL);
        getAuthorizationHandler().getAuthorizationUrl(REDIRECT_URL);

        // when:
        getScheduledRunnable().run();

        // then:
        assertNull(getPrivate(getAuthorizationHandler(), "oauthClientService"));
        assertNull(getPrivate(getAuthorizationHandler(), "bridgeUid"));
        assertNull(getPrivate(getAuthorizationHandler(), "redirectUri"));
        assertNull(getPrivate(getAuthorizationHandler(), "email"));
        assertNull(getPrivate(getAuthorizationHandler(), "timer"));
        verify(getTimer()).cancel(false);
    }

    @Test
    public void whenTheAuthorizationCompletesAfterItTimedOutThenAnNoOngoingAuthorizationExceptionIsThrown()
            throws Exception {
        // given:
        getAuthorizationHandler().beginAuthorization(CLIENT_ID, CLIENT_SECRET, BRIDGE_UID, EMAIL);
        getAuthorizationHandler().getAuthorizationUrl(REDIRECT_URL);

        getScheduledRunnable().run();

        // when:
        assertThrows(NoOngoingAuthorizationException.class, () -> {
            getAuthorizationHandler()
                    .completeAuthorization("http://127.0.0.1:8080/mielecloud/result?code=abc&state=def");
        });
    }

    @Test
    public void whenASecondAuthorizationIsBegunWhileAnotherIsStillOngoingThenAnOngoingAuthorizationExceptionIsThrown() {
        // given:
        getAuthorizationHandler().beginAuthorization(CLIENT_ID, CLIENT_SECRET, BRIDGE_UID, EMAIL);

        // when:
        assertThrows(OngoingAuthorizationException.class, () -> {
            getAuthorizationHandler().beginAuthorization(CLIENT_ID, CLIENT_SECRET, BRIDGE_UID, EMAIL);
        });
    }

    @Test
    public void whenNoAuthorizationIsOngoingAndTheAuthorizationUrlIsRequestedThenAnNoOngoingAuthorizationExceptionIsThrown() {
        // when:
        assertThrows(NoOngoingAuthorizationException.class, () -> {
            getAuthorizationHandler().getAuthorizationUrl(REDIRECT_URL);
        });
    }

    @Test
    public void whenGetAuthorizationUrlFromTheFrameworkFailsThenTheOngoingAuthorizationIsAborted()
            throws org.openhab.core.auth.client.oauth2.OAuthException, IllegalArgumentException, IllegalAccessException,
            NoSuchFieldException, SecurityException {
        // given:
        when(getClientService().getAuthorizationUrl(anyString(), isNull(), isNull()))
                .thenThrow(new org.openhab.core.auth.client.oauth2.OAuthException());

        getAuthorizationHandler().beginAuthorization(CLIENT_ID, CLIENT_SECRET, BRIDGE_UID, EMAIL);

        // when:
        assertThrows(OAuthException.class, () -> {
            try {
                getAuthorizationHandler().getAuthorizationUrl(REDIRECT_URL);
            } catch (OAuthException e) {
                assertNull(getPrivate(getAuthorizationHandler(), "timer"));
                assertNull(getPrivate(getAuthorizationHandler(), "oauthClientService"));
                assertNull(getPrivate(getAuthorizationHandler(), "bridgeUid"));
                assertNull(getPrivate(getAuthorizationHandler(), "redirectUri"));
                assertNull(getPrivate(getAuthorizationHandler(), "email"));
                throw e;
            }
        });
    }

    @Test
    public void whenExtractingTheAuthCodeFromTheResponseFailsThenAnOAuthExceptionIsThrownAndAllResourcesAreCleanedUp()
            throws Exception {
        // given:
        when(getClientService().extractAuthCodeFromAuthResponse(anyString()))
                .thenThrow(new org.openhab.core.auth.client.oauth2.OAuthException());

        getAuthorizationHandler().beginAuthorization(CLIENT_ID, CLIENT_SECRET, BRIDGE_UID, EMAIL);
        getAuthorizationHandler().getAuthorizationUrl(REDIRECT_URL);

        // when:
        assertThrows(OAuthException.class, () -> {
            try {
                getAuthorizationHandler()
                        .completeAuthorization("http://127.0.0.1:8080/mielecloud/result?code=abc&state=def");
            } catch (OAuthException e) {
                assertNull(getPrivate(getAuthorizationHandler(), "timer"));
                assertNull(getPrivate(getAuthorizationHandler(), "oauthClientService"));
                assertNull(getPrivate(getAuthorizationHandler(), "bridgeUid"));
                assertNull(getPrivate(getAuthorizationHandler(), "email"));
                assertNull(getPrivate(getAuthorizationHandler(), "redirectUri"));
                throw e;
            }
        });
    }

    @Test
    public void whenRetrievingTheAccessTokenFailsDueToANetworkErrorThenAnOAuthExceptionIsThrownAndAllResourcesAreCleanedUp()
            throws Exception {
        // given:
        when(getClientService().extractAuthCodeFromAuthResponse(anyString())).thenReturn(AUTH_CODE);
        when(getClientService().getAccessTokenResponseByAuthorizationCode(anyString(), anyString()))
                .thenThrow(new IOException());

        getAuthorizationHandler().beginAuthorization(CLIENT_ID, CLIENT_SECRET, BRIDGE_UID, EMAIL);
        getAuthorizationHandler().getAuthorizationUrl(REDIRECT_URL);

        // when:
        assertThrows(OAuthException.class, () -> {
            try {
                getAuthorizationHandler()
                        .completeAuthorization("http://127.0.0.1:8080/mielecloud/result?code=abc&state=def");
            } catch (OAuthException e) {
                assertNull(getPrivate(getAuthorizationHandler(), "timer"));
                assertNull(getPrivate(getAuthorizationHandler(), "oauthClientService"));
                assertNull(getPrivate(getAuthorizationHandler(), "bridgeUid"));
                assertNull(getPrivate(getAuthorizationHandler(), "email"));
                assertNull(getPrivate(getAuthorizationHandler(), "redirectUri"));
                throw e;
            }
        });
    }

    @Test
    public void whenRetrievingTheAccessTokenFailsDueToAnIllegalAnswerFromTheMieleServiceThenAnOAuthExceptionIsThrownAndAllResourcesAreCleanedUp()
            throws Exception {
        // given:
        when(getClientService().extractAuthCodeFromAuthResponse(anyString())).thenReturn(AUTH_CODE);
        when(getClientService().getAccessTokenResponseByAuthorizationCode(anyString(), anyString()))
                .thenThrow(new OAuthResponseException());

        getAuthorizationHandler().beginAuthorization(CLIENT_ID, CLIENT_SECRET, BRIDGE_UID, EMAIL);
        getAuthorizationHandler().getAuthorizationUrl(REDIRECT_URL);

        // when:
        assertThrows(OAuthException.class, () -> {
            try {
                getAuthorizationHandler()
                        .completeAuthorization("http://127.0.0.1:8080/mielecloud/result?code=abc&state=def");
            } catch (OAuthException e) {
                assertNull(getPrivate(getAuthorizationHandler(), "timer"));
                assertNull(getPrivate(getAuthorizationHandler(), "oauthClientService"));
                assertNull(getPrivate(getAuthorizationHandler(), "bridgeUid"));
                assertNull(getPrivate(getAuthorizationHandler(), "email"));
                assertNull(getPrivate(getAuthorizationHandler(), "redirectUri"));
                throw e;
            }
        });
    }

    @Test
    public void whenRetrievingTheAccessTokenFailsWhileProcessingTheResponseThenAnOAuthExceptionIsThrownAndAllResourcesAreCleanedUp()
            throws Exception {
        // given:
        when(getClientService().extractAuthCodeFromAuthResponse(anyString())).thenReturn(AUTH_CODE);
        when(getClientService().getAccessTokenResponseByAuthorizationCode(anyString(), anyString()))
                .thenThrow(new org.openhab.core.auth.client.oauth2.OAuthException());

        getAuthorizationHandler().beginAuthorization(CLIENT_ID, CLIENT_SECRET, BRIDGE_UID, EMAIL);
        getAuthorizationHandler().getAuthorizationUrl(REDIRECT_URL);

        // when:
        assertThrows(OAuthException.class, () -> {
            try {
                getAuthorizationHandler()
                        .completeAuthorization("http://127.0.0.1:8080/mielecloud/result?code=abc&state=def");
            } catch (OAuthException e) {
                assertNull(getPrivate(getAuthorizationHandler(), "timer"));
                assertNull(getPrivate(getAuthorizationHandler(), "oauthClientService"));
                assertNull(getPrivate(getAuthorizationHandler(), "bridgeUid"));
                assertNull(getPrivate(getAuthorizationHandler(), "email"));
                assertNull(getPrivate(getAuthorizationHandler(), "redirectUri"));
                throw e;
            }
        });
    }

    @Test
    public void whenNoAuthorizationIsOngoingThenGetBridgeUidThrowsNoOngoingAuthorizationException() {
        // when:
        assertThrows(NoOngoingAuthorizationException.class, () -> {
            getAuthorizationHandler().getBridgeUid();
        });
    }

    @Test
    public void whenNoAuthorizationIsOngoingThenGetEmailThrowsNoOngoingAuthorizationException() {
        // when:
        assertThrows(NoOngoingAuthorizationException.class, () -> {
            getAuthorizationHandler().getEmail();
        });
    }

    @Test
    public void whenAnAuthorizationIsOngoingThenGetBridgeUidReturnsTheUidOfTheBridgeBeingAuthorized() {
        // given:
        getAuthorizationHandler().beginAuthorization(CLIENT_ID, CLIENT_SECRET, BRIDGE_UID, EMAIL);

        // when:
        ThingUID bridgeUid = getAuthorizationHandler().getBridgeUid();

        // then:
        assertEquals(BRIDGE_UID, bridgeUid);
    }

    @Test
    public void whenAnAuthorizationIsOngoingThenGetEmailReturnsTheEmailBeingAuthorized() {
        // given:
        getAuthorizationHandler().beginAuthorization(CLIENT_ID, CLIENT_SECRET, BRIDGE_UID, EMAIL);

        // when:
        String email = getAuthorizationHandler().getEmail();

        // then:
        assertEquals(EMAIL, email);
    }
}
