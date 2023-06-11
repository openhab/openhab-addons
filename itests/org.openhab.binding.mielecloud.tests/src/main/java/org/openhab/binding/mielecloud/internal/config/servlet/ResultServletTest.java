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
package org.openhab.binding.mielecloud.internal.config.servlet;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mielecloud.internal.util.ReflectionUtil.setPrivate;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.auth.OAuthException;
import org.openhab.binding.mielecloud.internal.config.OAuthAuthorizationHandler;
import org.openhab.binding.mielecloud.internal.config.exception.NoOngoingAuthorizationException;
import org.openhab.binding.mielecloud.internal.util.AbstractConfigFlowTest;
import org.openhab.binding.mielecloud.internal.util.Website;

/**
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public class ResultServletTest extends AbstractConfigFlowTest {
    @Test
    public void whenOAuthErrorAccessDeniedIsReturnedByMieleServiceThenTheFailurePageWithAccordingErrorMessageIsDisplayed()
            throws Exception {
        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/result?" + ResultServlet.ERROR_PARAMETER_NAME + "="
                + FailureServlet.OAUTH2_ERROR_ACCESS_DENIED);

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains("OAuth2 authentication with Miele cloud service failed: Access denied."));
    }

    @Test
    public void whenOAuthErrorInvalidRequestIsReturnedByMieleServiceThenTheFailurePageWithAccordingErrorMessageIsDisplayed()
            throws Exception {
        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/result?" + ResultServlet.ERROR_PARAMETER_NAME + "="
                + FailureServlet.OAUTH2_ERROR_INVALID_REQUEST);

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains("OAuth2 authentication with Miele cloud service failed: Malformed request."));
    }

    @Test
    public void whenOAuthErrorUnauthorizedClientIsReturnedByMieleServiceThenTheFailurePageWithAccordingErrorMessageIsDisplayed()
            throws Exception {
        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/result?" + ResultServlet.ERROR_PARAMETER_NAME + "="
                + FailureServlet.OAUTH2_ERROR_UNAUTHORIZED_CLIENT);

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains(
                "OAuth2 authentication with Miele cloud service failed: Account not authorized to request authorization code."));
    }

    @Test
    public void whenOAuthErrorUnsupportedResponseTypeIsReturnedByMieleServiceThenTheFailurePageWithAccordingErrorMessageIsDisplayed()
            throws Exception {
        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/result?" + ResultServlet.ERROR_PARAMETER_NAME + "="
                + FailureServlet.OAUTH2_ERROR_UNSUPPORTED_RESPONSE_TYPE);

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains(
                "OAuth2 authentication with Miele cloud service failed: Obtaining an authorization code is not supported."));
    }

    @Test
    public void whenOAuthErrorInvalidScopeIsReturnedByMieleServiceThenTheFailurePageWithAccordingErrorMessageIsDisplayed()
            throws Exception {
        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/result?" + ResultServlet.ERROR_PARAMETER_NAME + "="
                + FailureServlet.OAUTH2_ERROR_INVALID_SCOPE);

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains("OAuth2 authentication with Miele cloud service failed: Invalid scope."));
    }

    @Test
    public void whenOAuthErrorServerErrorIsReturnedByMieleServiceThenTheFailurePageWithAccordingErrorMessageIsDisplayed()
            throws Exception {
        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/result?" + ResultServlet.ERROR_PARAMETER_NAME + "="
                + FailureServlet.OAUTH2_ERROR_SERVER_ERROR);

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains("OAuth2 authentication with Miele cloud service failed: Unexpected server error."));
    }

    @Test
    public void whenOAuthErrorTemporarilyUnavailableIsReturnedByMieleServiceThenTheFailurePageWithAccordingErrorMessageIsDisplayed()
            throws Exception {
        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/result?" + ResultServlet.ERROR_PARAMETER_NAME + "="
                + FailureServlet.OAUTH2_ERROR_TEMPORARY_UNAVAILABLE);

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains(
                "OAuth2 authentication with Miele cloud service failed: Authorization server temporarily unavailable."));
    }

    @Test
    public void whenUnknwonOAuthErrorIsReturnedByMieleServiceThenTheFailurePageWithAccordingErrorMessageIsDisplayed()
            throws Exception {
        // when:
        Website website = getCrawler()
                .doGetRelative("/mielecloud/result?" + ResultServlet.ERROR_PARAMETER_NAME + "=unknown_oauth_2_error");

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains(
                "OAuth2 authentication with Miele cloud service failed: Unknown error code \"unknown_oauth_2_error\"."));
    }

    @Test
    public void whenCodeParameterIsNotPassedByMieleServiceThenTheFailurePageWithAccordingErrorMessageIsDisplayed()
            throws Exception {
        // when:
        Website website = getCrawler()
                .doGetRelative("/mielecloud/result?" + ResultServlet.STATE_PARAMETER_NAME + "=state");

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains("Miele cloud service returned an illegal response."));
    }

    @Test
    public void whenStateParameterIsNotPassedByMieleServiceThenTheFailurePageWithAccordingErrorMessageIsDisplayed()
            throws Exception {
        // when:
        Website website = getCrawler()
                .doGetRelative("/mielecloud/result?" + ResultServlet.CODE_PARAMETER_NAME + "=code");

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains("Miele cloud service returned an illegal response."));
    }

    @Test
    public void whenNoAuthorizationIsOngoingThenTheFailurePageWithAccordingErrorMessageIsDisplayed() throws Exception {
        // given:
        OAuthAuthorizationHandler authorizationHandler = mock(OAuthAuthorizationHandler.class);
        doThrow(new NoOngoingAuthorizationException("")).when(authorizationHandler).completeAuthorization(anyString());
        setPrivate(getResultServlet(), "authorizationHandler", authorizationHandler);

        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/result?" + ResultServlet.CODE_PARAMETER_NAME
                + "=code&" + ResultServlet.STATE_PARAMETER_NAME + "=state");

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website.contains("There is no ongoing authorization. Please start an authorization first."));
    }

    @Test
    public void whenLastStepOfAuthorizationFailsThenTheFailurePageWithAccordingErrorMessageIsDisplayed()
            throws Exception {
        // given:
        OAuthAuthorizationHandler authorizationHandler = mock(OAuthAuthorizationHandler.class);
        doThrow(new OAuthException("")).when(authorizationHandler).completeAuthorization(anyString());
        setPrivate(getResultServlet(), "authorizationHandler", authorizationHandler);

        // when:
        Website website = getCrawler().doGetRelative("/mielecloud/result?" + ResultServlet.CODE_PARAMETER_NAME
                + "=code&" + ResultServlet.STATE_PARAMETER_NAME + "=state");

        // then:
        assertTrue(website.contains("Pairing failed!"));
        assertTrue(website
                .contains("Completing the final authorization request failed. Please try the config flow again."));
    }
}
