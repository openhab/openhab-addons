/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants.*;
import static org.openhab.binding.mielecloud.internal.util.ReflectionUtil.*;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants;
import org.openhab.binding.mielecloud.internal.MieleCloudBindingConstants.I18NKeys;
import org.openhab.binding.mielecloud.internal.auth.OAuthTokenRefresher;
import org.openhab.binding.mielecloud.internal.auth.OpenHabOAuthTokenRefresher;
import org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants;
import org.openhab.binding.mielecloud.internal.util.OpenHabOsgiTest;
import org.openhab.binding.mielecloud.internal.webservice.ConnectionError;
import org.openhab.binding.mielecloud.internal.webservice.MieleWebservice;
import org.openhab.binding.mielecloud.internal.webservice.MieleWebserviceFactory;
import org.openhab.binding.mielecloud.internal.webservice.language.CombiningLanguageProvider;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class MieleBridgeHandlerTest extends OpenHabOsgiTest {
    private static final String SERVICE_HANDLE = MieleCloudBindingIntegrationTestConstants.EMAIL;
    private static final String CONFIG_PARAM_LOCALE = "locale";

    @Nullable
    private MieleWebservice webserviceMock;
    @Nullable
    private String webserviceAccessToken;
    @Nullable
    private OAuthFactory oauthFactoryMock;
    @Nullable
    private OAuthClientService oauthClientServiceMock;

    @Nullable
    private Bridge bridge;
    @Nullable
    private MieleBridgeHandler handler;

    private MieleWebservice getWebserviceMock() {
        assertNotNull(webserviceMock);
        return Objects.requireNonNull(webserviceMock);
    }

    private OAuthFactory getOAuthFactoryMock() {
        assertNotNull(oauthFactoryMock);
        return Objects.requireNonNull(oauthFactoryMock);
    }

    private OAuthClientService getOAuthClientServiceMock() {
        OAuthClientService oauthClientServiceMock = this.oauthClientServiceMock;
        assertNotNull(oauthClientServiceMock);
        return Objects.requireNonNull(oauthClientServiceMock);
    }

    private Bridge getBridge() {
        assertNotNull(bridge);
        return Objects.requireNonNull(bridge);
    }

    private MieleBridgeHandler getHandler() {
        assertNotNull(handler);
        return Objects.requireNonNull(handler);
    }

    @BeforeEach
    public void setUp() throws Exception {
        setUpWebservice();
        setUpBridgeThingAndHandler();
        setUpOAuthFactory();
    }

    private void setUpWebservice() throws NoSuchFieldException, IllegalAccessException {
        webserviceMock = mock(MieleWebservice.class);
        doAnswer(invocation -> {
            if (invocation != null) {
                webserviceAccessToken = invocation.getArgument(0);
            }
            return null;
        }).when(getWebserviceMock()).setAccessToken(anyString());
        when(getWebserviceMock().hasAccessToken()).then(invocation -> webserviceAccessToken != null);

        MieleWebserviceFactory webserviceFactory = mock(MieleWebserviceFactory.class);
        when(webserviceFactory.create(any())).thenReturn(getWebserviceMock());

        MieleHandlerFactory handlerFactory = getService(ThingHandlerFactory.class, MieleHandlerFactory.class);
        assertNotNull(handlerFactory);
        setPrivate(Objects.requireNonNull(handlerFactory), "webserviceFactory", webserviceFactory);
    }

    private void setUpBridgeThingAndHandler() {
        when(getWebserviceMock().hasAccessToken()).thenReturn(false);

        bridge = BridgeBuilder
                .create(MieleCloudBindingConstants.THING_TYPE_BRIDGE,
                        MieleCloudBindingIntegrationTestConstants.BRIDGE_THING_UID)
                .withConfiguration(new Configuration(Map.of(MieleCloudBindingConstants.CONFIG_PARAM_EMAIL,
                        MieleCloudBindingIntegrationTestConstants.EMAIL)))
                .withLabel(MIELE_CLOUD_ACCOUNT_LABEL).build();
        assertNotNull(bridge);

        getThingRegistry().add(getBridge());

        waitForAssert(() -> {
            assertNotNull(getBridge().getHandler());
            assertTrue(getBridge().getHandler() instanceof MieleBridgeHandler, "Handler type is wrong");
        });
        handler = (MieleBridgeHandler) getBridge().getHandler();
    }

    private void setUpOAuthFactory() throws Exception {
        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setAccessToken(ACCESS_TOKEN);

        oauthClientServiceMock = mock(OAuthClientService.class);
        when(oauthClientServiceMock.getAccessTokenResponse()).thenReturn(accessTokenResponse);

        OAuthFactory oAuthFactory = mock(OAuthFactory.class);
        Mockito.when(oAuthFactory.getOAuthClientService(SERVICE_HANDLE)).thenReturn(getOAuthClientServiceMock());
        oauthFactoryMock = oAuthFactory;

        OpenHabOAuthTokenRefresher tokenRefresher = getService(OAuthTokenRefresher.class,
                OpenHabOAuthTokenRefresher.class);
        assertNotNull(tokenRefresher);
        setPrivate(Objects.requireNonNull(tokenRefresher), "oauthFactory", oAuthFactory);
    }

    private void initializeBridgeWithTokens() {
        getHandler().initialize();
        assertThingStatusIs(ThingStatus.UNKNOWN);
    }

    private void assertThingStatusIs(ThingStatus expectedStatus) {
        assertThingStatusIs(expectedStatus, ThingStatusDetail.NONE);
    }

    private void assertThingStatusIs(ThingStatus expectedStatus, ThingStatusDetail expectedStatusDetail) {
        assertThingStatusIs(expectedStatus, expectedStatusDetail, null);
    }

    private void assertThingStatusIs(ThingStatus expectedStatus, ThingStatusDetail expectedStatusDetail,
            @Nullable String expectedDescription) {
        assertEquals(expectedStatus, getBridge().getStatus());
        assertEquals(expectedStatusDetail, getBridge().getStatusInfo().getStatusDetail());
        if (expectedDescription == null) {
            assertNull(getBridge().getStatusInfo().getDescription());
        } else {
            assertEquals(expectedDescription, getBridge().getStatusInfo().getDescription());
        }
    }

    @Test
    public void testThingStatusIsSetToOfflineWithDetailConfigurationPendingAndDescriptionWhenTokensAreNotPassedViaInitialConfiguration()
            throws Exception {
        when(getOAuthClientServiceMock().getAccessTokenResponse()).thenReturn(null);

        // when:
        getHandler().initialize();

        // then:
        assertThingStatusIs(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                MieleCloudBindingConstants.I18NKeys.BRIDGE_STATUS_DESCRIPTION_ACCESS_TOKEN_NOT_CONFIGURED);
    }

    @Test
    public void testThingStatusIsSetToOfflineWithDetailConfigurationErrorAndDescriptionWhenTheMieleAccountHasNotBeenAuthorized()
            throws Exception {
        // given:
        OAuthFactory oAuthFactory = mock(OAuthFactory.class);
        Mockito.when(oAuthFactory.getOAuthClientService(SERVICE_HANDLE)).thenReturn(null);

        OpenHabOAuthTokenRefresher tokenRefresher = getService(OAuthTokenRefresher.class,
                OpenHabOAuthTokenRefresher.class);
        assertNotNull(tokenRefresher);
        // Clear the setup configuration and use the failing one for this test.
        setPrivate(Objects.requireNonNull(tokenRefresher), "oauthFactory", oAuthFactory);

        // when:
        getHandler().initialize();

        // then:
        assertThingStatusIs(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                MieleCloudBindingConstants.I18NKeys.BRIDGE_STATUS_DESCRIPTION_ACCOUNT_NOT_AUTHORIZED);
    }

    @Test
    public void testThingStatusIsSetToUnknownAndThingWaitsForCloudConnectionWhenTheMieleAccountBecomesAuthorizedAfterTheBridgeWasInitialized()
            throws Exception {
        // given:
        OAuthFactory oAuthFactory = mock(OAuthFactory.class);
        Mockito.when(oAuthFactory.getOAuthClientService(SERVICE_HANDLE)).thenReturn(null);

        OpenHabOAuthTokenRefresher tokenRefresher = getService(OAuthTokenRefresher.class,
                OpenHabOAuthTokenRefresher.class);
        assertNotNull(tokenRefresher);
        // Clear the setup configuration and use the failing one for this test.
        setPrivate(Objects.requireNonNull(tokenRefresher), "oauthFactory", oAuthFactory);

        getHandler().initialize();

        assertThingStatusIs(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                I18NKeys.BRIDGE_STATUS_DESCRIPTION_ACCOUNT_NOT_AUTHORIZED);

        setUpOAuthFactory();

        // when:
        getHandler().dispose();
        getHandler().initialize();

        // then:
        assertThingStatusIs(ThingStatus.UNKNOWN);
    }

    @Test
    public void whenTheSseConnectionIsEstablishedThenTheThingStatusIsSetToOnline() throws Exception {
        // given:
        initializeBridgeWithTokens();

        // when:
        getHandler().onConnectionAlive();

        // then:
        assertThingStatusIs(ThingStatus.ONLINE);
    }

    @Test
    public void whenAnAuthorizationFailedErrorIsReportedThenTheAccessTokenIsRefreshedAndTheSseConnectionRestored()
            throws Exception {
        // given:
        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setAccessToken(ACCESS_TOKEN);
        when(getOAuthClientServiceMock().refreshToken()).thenReturn(accessTokenResponse);

        initializeBridgeWithTokens();
        getHandler().onConnectionAlive();

        // when:
        getHandler().onConnectionError(ConnectionError.AUTHORIZATION_FAILED, 0);

        // then:
        verify(getOAuthClientServiceMock()).refreshToken();
        verify(getWebserviceMock()).connectSse();
        assertThingStatusIs(ThingStatus.ONLINE);
    }

    @Test
    public void whenAnAuthorizationFailedErrorIsReportedAndTokenRefreshFailsThenSseConnectionIsTerminatedAndTheStatusSetToOfflineWithDetailConfigurationError()
            throws Exception {
        // given:
        when(getOAuthClientServiceMock().refreshToken()).thenReturn(new AccessTokenResponse());
        initializeBridgeWithTokens();
        getHandler().onConnectionAlive();

        // when:
        getHandler().onConnectionError(ConnectionError.AUTHORIZATION_FAILED, 0);

        // then:
        verify(getOAuthClientServiceMock()).refreshToken();
        verify(getWebserviceMock()).disconnectSse();
        assertThingStatusIs(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                I18NKeys.BRIDGE_STATUS_DESCRIPTION_ACCESS_TOKEN_REFRESH_FAILED);
    }

    @Test
    public void whenARequestExecutionFailedErrorIsReportedAndNoRetriesHaveBeenMadeThenItHasNoEffectOnTheThingStatus()
            throws Exception {
        // given:
        initializeBridgeWithTokens();
        getHandler().onConnectionAlive();

        // when:
        getHandler().onConnectionError(ConnectionError.REQUEST_EXECUTION_FAILED, 0);

        // then:
        assertThingStatusIs(ThingStatus.ONLINE);
    }

    @Test
    public void whenARequestExecutionFailedErrorIsReportedWithSufficientRetriesThenTheThingStatusIsOfflineWithDetailCommunicationError()
            throws Exception {
        // given:
        initializeBridgeWithTokens();
        getHandler().onConnectionAlive();

        // when:
        getHandler().onConnectionError(ConnectionError.REQUEST_EXECUTION_FAILED, 10);

        // then:
        assertThingStatusIs(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    @Test
    public void whenARequestExecutionFailedErrorIsReportedAndThingIsInStatusUnknownThenTheThingStatusIsOfflineWithDetailCommunicationError()
            throws Exception {
        // given:
        initializeBridgeWithTokens();

        // when:
        getHandler().onConnectionError(ConnectionError.REQUEST_EXECUTION_FAILED, 0);

        // then:
        assertThingStatusIs(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    @Test
    public void whenAServiceUnavailableErrorIsReportedWithSufficientRetriesThenTheThingStatusIsOfflineWithDetailCommunicationError()
            throws Exception {
        // given:
        initializeBridgeWithTokens();
        getHandler().onConnectionAlive();

        // when:
        getHandler().onConnectionError(ConnectionError.SERVICE_UNAVAILABLE, 10);

        // then:
        assertThingStatusIs(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    @Test
    public void whenAResponseMalformedErrorIsReportedWithSufficientRetriesThenTheThingStatusIsOfflineWithDetailCommunicationError()
            throws Exception {
        // given:
        initializeBridgeWithTokens();

        // when:
        getHandler().onConnectionError(ConnectionError.RESPONSE_MALFORMED, 10);

        // then:
        assertThingStatusIs(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    @Test
    public void whenATimeoutErrorIsReportedWithSufficientRetriesThenTheThingStatusIsOfflineWithDetailCommunicationError()
            throws Exception {
        // given:
        initializeBridgeWithTokens();

        // when:
        getHandler().onConnectionError(ConnectionError.TIMEOUT, 10);

        // then:
        assertThingStatusIs(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    @Test
    public void whenATooManyRequestsErrorIsReportedWithSufficientRetriesThenTheThingStatusIsOfflineWithDetailCommunicationError()
            throws Exception {
        // given:
        initializeBridgeWithTokens();

        // when:
        getHandler().onConnectionError(ConnectionError.TOO_MANY_RERQUESTS, 10);

        // then:
        assertThingStatusIs(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    @Test
    public void whenAServerErrorIsReportedWithSufficientRetriesThenTheThingStatusIsOfflineWithDetailCommunicationError()
            throws Exception {
        // given:
        initializeBridgeWithTokens();

        // when:
        getHandler().onConnectionError(ConnectionError.SERVER_ERROR, 10);

        // then:
        assertThingStatusIs(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                I18NKeys.BRIDGE_STATUS_DESCRIPTION_TRANSIENT_HTTP_ERROR);
    }

    @Test
    public void whenARequestInterruptedErrorIsReportedWithSufficientRetriesThenTheThingStatusIsOfflineWithDetailCommunicationError()
            throws Exception {
        // given:
        initializeBridgeWithTokens();

        // when:
        getHandler().onConnectionError(ConnectionError.REQUEST_INTERRUPTED, 10);

        // then:
        assertThingStatusIs(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                I18NKeys.BRIDGE_STATUS_DESCRIPTION_TRANSIENT_HTTP_ERROR);
    }

    @Test
    public void whenSomeOtherHttpErrorIsReportedWithSufficientRetriesThenTheThingStatusIsOfflineWithDetailCommunicationError()
            throws Exception {
        // given:
        initializeBridgeWithTokens();

        // when:
        getHandler().onConnectionError(ConnectionError.OTHER_HTTP_ERROR, 10);

        // then:
        assertThingStatusIs(ThingStatus.OFFLINE, ThingStatusDetail.NONE,
                I18NKeys.BRIDGE_STATUS_DESCRIPTION_TRANSIENT_HTTP_ERROR);
    }

    @Test
    public void whenARequestIsInterruptedDuringInitializationThenTheThingStatusIsNotModified() throws Exception {
        // given:
        initializeBridgeWithTokens();

        // when:
        getHandler().onConnectionError(ConnectionError.REQUEST_INTERRUPTED, 0);

        // then:
        assertThingStatusIs(ThingStatus.UNKNOWN);
    }

    @Test
    public void whenTheAccessTokenWasRefreshedThenTheWebserviceIsSetIntoAnOperationalState()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        // given:
        getHandler().initialize();

        // when:
        getHandler().onNewAccessToken(ACCESS_TOKEN);

        // then:
        verify(getWebserviceMock(), atLeast(1)).setAccessToken(ACCESS_TOKEN);
        verify(getWebserviceMock(), atLeast(1)).connectSse();
    }

    @Test
    public void whenTheHandlerIsDisposedThenTheSseConnectionIsDisconnectedAndTheLanguageProviderIsUnset()
            throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
        // given:
        getHandler().initialize();

        // when:
        getHandler().dispose();

        // then:
        verify(getWebserviceMock()).disconnectSse();

        CombiningLanguageProvider languageProvider = getPrivate(getHandler(), "languageProvider");
        assertNull(getPrivate(languageProvider, "prioritizedLanguageProvider"));
    }

    @Test
    public void testNoLanguageIsReturnedWhenTheConfigurationParameterIsNotSet() {
        // when:
        Optional<String> language = getHandler().getLanguage();

        // then:
        assertFalse(language.isPresent());
    }

    @Test
    public void testNoLanguageIsReturnedWhenTheConfigurationParameterIsEmpty() {
        // given:
        getHandler().handleConfigurationUpdate(
                Map.of(MieleCloudBindingConstants.CONFIG_PARAM_EMAIL, SERVICE_HANDLE, CONFIG_PARAM_LOCALE, ""));

        // when:
        Optional<String> language = getHandler().getLanguage();

        // then:
        assertFalse(language.isPresent());
    }

    @Test
    public void testNoLanguageIsReturnedWhenTheConfigurationParameterIsNotAValidTwoLetterLanguageCode() {
        // given:
        getHandler().handleConfigurationUpdate(
                Map.of(MieleCloudBindingConstants.CONFIG_PARAM_EMAIL, SERVICE_HANDLE, CONFIG_PARAM_LOCALE, "Deutsch"));

        // when:
        Optional<String> language = getHandler().getLanguage();

        // then:
        assertFalse(language.isPresent());
    }

    @Test
    public void testAValidTwoLetterLanguageCodeIsReturnedWhenTheConfigurationParameterIsSetToTheTwoLetterLanguageCode() {
        // given:
        getHandler().handleConfigurationUpdate(
                Map.of(MieleCloudBindingConstants.CONFIG_PARAM_EMAIL, SERVICE_HANDLE, CONFIG_PARAM_LOCALE, "DE"));

        // when:
        String language = getHandler().getLanguage().get();

        // then:
        assertEquals("DE", language);
    }

    @Test
    public void testWhenTheThingIsRemovedThenTheWebserviceIsLoggedOut() throws Exception {
        // given:
        initializeBridgeWithTokens();

        // when:
        getThingRegistry().remove(getHandler().getThing().getUID());

        // then:
        waitForAssert(() -> {
            verify(getWebserviceMock()).logout();
        });
    }

    @Test
    public void testWhenTheThingIsRemovedThenTheTokensAreRemovedFromTheStorage() throws Exception {
        // given:
        initializeBridgeWithTokens();

        // when:
        getThingRegistry().remove(getHandler().getThing().getUID());

        // then:
        waitForAssert(() -> {
            verify(getOAuthFactoryMock()).deleteServiceAndAccessToken(SERVICE_HANDLE);
        });
    }
}
