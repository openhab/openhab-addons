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
package org.openhab.binding.mielecloud.internal.config.servlet;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mielecloud.internal.util.ReflectionUtil.setPrivate;

import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.auth.OAuthException;
import org.openhab.binding.mielecloud.internal.config.OAuthAuthorizationHandler;
import org.openhab.binding.mielecloud.internal.config.exception.NoOngoingAuthorizationException;
import org.openhab.binding.mielecloud.internal.config.exception.OngoingAuthorizationException;
import org.openhab.binding.mielecloud.internal.util.AbstractConfigFlowTest;
import org.openhab.binding.mielecloud.internal.util.MieleCloudBindingIntegrationTestConstants;
import org.openhab.binding.mielecloud.internal.util.Website;

/**
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public class ForwardToLoginServletTest extends AbstractConfigFlowTest {
    @Test
    public void whenAuthorizationCannotBeBegunThenTheBrowserIsRedirectedToThePairSiteAndAWarningIsDisplayed()
            throws Exception {
        // given:
        OAuthAuthorizationHandler authorizationHandler = mock(OAuthAuthorizationHandler.class);
        doThrow(new OngoingAuthorizationException("", LocalDateTime.now().plusMinutes(3))).when(authorizationHandler)
                .beginAuthorization(anyString(), anyString(), any(), anyString());
        setPrivate(getForwardToLoginServlet(), "authorizationHandler", authorizationHandler);

        // when:
        Website maybePairAccountSite = getCrawler().doGetRelative("/mielecloud/forwardToLogin?"
                + ForwardToLoginServlet.CLIENT_ID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_ID + "&"
                + ForwardToLoginServlet.CLIENT_SECRET_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_SECRET + "&"
                + ForwardToLoginServlet.BRIDGE_ID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.BRIDGE_ID + "&" + ForwardToLoginServlet.EMAIL_PARAMETER_NAME
                + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(maybePairAccountSite.contains(
                "Go to <a href=\"https://www.miele.com/f/com/en/register_api.aspx\">the Miele developer portal</a> to obtain your"));
        assertTrue(maybePairAccountSite.contains(
                "There is an authorization ongoing at the moment. Please complete that authorization prior to starting a new one or try again"));
    }

    @Test
    public void whenNoAuthorizationIsOngoingWhenTheAuthorizationUrlIsRequestedThenTheBrowserIsRedirectedToThePairSiteAndAWarningIsDisplayed()
            throws Exception {
        // given:
        OAuthAuthorizationHandler authorizationHandler = mock(OAuthAuthorizationHandler.class);
        doThrow(new NoOngoingAuthorizationException("")).when(authorizationHandler).getAuthorizationUrl(anyString());
        setPrivate(getForwardToLoginServlet(), "authorizationHandler", authorizationHandler);

        // when:
        Website maybePairAccountSite = getCrawler().doGetRelative("/mielecloud/forwardToLogin?"
                + ForwardToLoginServlet.CLIENT_ID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_ID + "&"
                + ForwardToLoginServlet.CLIENT_SECRET_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_SECRET + "&"
                + ForwardToLoginServlet.BRIDGE_ID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.BRIDGE_ID + "&" + ForwardToLoginServlet.EMAIL_PARAMETER_NAME
                + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(maybePairAccountSite.contains(
                "Go to <a href=\"https://www.miele.com/f/com/en/register_api.aspx\">the Miele developer portal</a> to obtain your"));
        assertTrue(maybePairAccountSite.contains(
                "Failed to start auhtorization process. Are you trying to perform multiple authorizations at the same time?"));
    }

    @Test
    public void whenNoClientIdIsPassedThenTheBrowserIsRedirectedToThePairSiteAndAWarningIsDisplayed() throws Exception {
        // when:
        Website maybePairAccountSite = getCrawler().doGetRelative("/mielecloud/forwardToLogin?"
                + ForwardToLoginServlet.CLIENT_SECRET_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_SECRET + "&"
                + ForwardToLoginServlet.BRIDGE_ID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.BRIDGE_ID + "&" + ForwardToLoginServlet.EMAIL_PARAMETER_NAME
                + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(maybePairAccountSite.contains(
                "Go to <a href=\"https://www.miele.com/f/com/en/register_api.aspx\">the Miele developer portal</a> to obtain your"));
        assertTrue(maybePairAccountSite.contains("Missing client ID."));
    }

    @Test
    public void whenAnEmptyClientIdIsPassedThenTheBrowserIsRedirectedToThePairSiteAndAWarningIsDisplayed()
            throws Exception {
        // when:
        Website maybePairAccountSite = getCrawler().doGetRelative("/mielecloud/forwardToLogin?"
                + ForwardToLoginServlet.CLIENT_ID_PARAMETER_NAME + "=&"
                + ForwardToLoginServlet.CLIENT_SECRET_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_SECRET + "&"
                + ForwardToLoginServlet.BRIDGE_ID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.BRIDGE_ID + "&" + ForwardToLoginServlet.EMAIL_PARAMETER_NAME
                + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(maybePairAccountSite.contains(
                "Go to <a href=\"https://www.miele.com/f/com/en/register_api.aspx\">the Miele developer portal</a> to obtain your"));
        assertTrue(maybePairAccountSite.contains("Missing client ID."));
    }

    @Test
    public void whenNoClientSecretIsPassedThenTheBrowserIsRedirectedToThePairSiteAndAWarningIsDisplayed()
            throws Exception {
        // when:
        Website maybePairAccountSite = getCrawler().doGetRelative("/mielecloud/forwardToLogin?"
                + ForwardToLoginServlet.CLIENT_ID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_ID + "&"
                + ForwardToLoginServlet.BRIDGE_ID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.BRIDGE_ID + "&" + ForwardToLoginServlet.EMAIL_PARAMETER_NAME
                + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(maybePairAccountSite.contains(
                "Go to <a href=\"https://www.miele.com/f/com/en/register_api.aspx\">the Miele developer portal</a> to obtain your"));
        assertTrue(maybePairAccountSite.contains("Missing client secret."));
    }

    @Test
    public void whenAnEmptyClientSecretIsPassedThenTheBrowserIsRedirectedToThePairSiteAndAWarningIsDisplayed()
            throws Exception {
        // when:
        Website maybePairAccountSite = getCrawler().doGetRelative("/mielecloud/forwardToLogin?"
                + ForwardToLoginServlet.CLIENT_ID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_ID + "&"
                + ForwardToLoginServlet.CLIENT_SECRET_PARAMETER_NAME + "=" + "&"
                + ForwardToLoginServlet.BRIDGE_ID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.BRIDGE_ID + "&" + ForwardToLoginServlet.EMAIL_PARAMETER_NAME
                + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(maybePairAccountSite.contains(
                "Go to <a href=\"https://www.miele.com/f/com/en/register_api.aspx\">the Miele developer portal</a> to obtain your"));
        assertTrue(maybePairAccountSite.contains("Missing client secret."));
    }

    @Test
    public void whenOAuthClientDoesNotProvideAnAuthorizationUrlThenTheBrowserIsRedirectedToThePairSiteAndAWarningIsDisplayed()
            throws Exception {
        // given:
        OAuthAuthorizationHandler authorizationHandler = mock(OAuthAuthorizationHandler.class);
        doThrow(new OAuthException("")).when(authorizationHandler).getAuthorizationUrl(anyString());
        setPrivate(getForwardToLoginServlet(), "authorizationHandler", authorizationHandler);

        // when:
        Website maybePairAccountSite = getCrawler().doGetRelative("/mielecloud/forwardToLogin?"
                + ForwardToLoginServlet.CLIENT_ID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_ID + "&"
                + ForwardToLoginServlet.CLIENT_SECRET_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_SECRET + "&"
                + ForwardToLoginServlet.BRIDGE_ID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.BRIDGE_ID + "&" + ForwardToLoginServlet.EMAIL_PARAMETER_NAME
                + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(maybePairAccountSite.contains(
                "Go to <a href=\"https://www.miele.com/f/com/en/register_api.aspx\">the Miele developer portal</a> to obtain your"));
        assertTrue(maybePairAccountSite.contains("Failed to derive redirect URL."));
    }

    @Test
    public void whenNoBridgeUidIsPassedThenTheBrowserIsRedirectedToThePairSiteAndAWarningIsDisplayed()
            throws Exception {
        // when:
        Website maybePairAccountSite = getCrawler().doGetRelative("/mielecloud/forwardToLogin?"
                + ForwardToLoginServlet.CLIENT_ID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_ID + "&"
                + ForwardToLoginServlet.CLIENT_SECRET_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_SECRET + "&"
                + ForwardToLoginServlet.EMAIL_PARAMETER_NAME + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(maybePairAccountSite.contains(
                "Go to <a href=\"https://www.miele.com/f/com/en/register_api.aspx\">the Miele developer portal</a> to obtain your"));
        assertTrue(maybePairAccountSite.contains("Missing bridge ID."));
    }

    @Test
    public void whenAnEmptyBridgeUidIsPassedThenTheBrowserIsRedirectedToThePairSiteAndAWarningIsDisplayed()
            throws Exception {
        // when:
        Website maybePairAccountSite = getCrawler().doGetRelative("/mielecloud/forwardToLogin?"
                + ForwardToLoginServlet.CLIENT_ID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_ID + "&"
                + ForwardToLoginServlet.CLIENT_SECRET_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_SECRET + "&"
                + ForwardToLoginServlet.BRIDGE_ID_PARAMETER_NAME + "=" + "&"
                + ForwardToLoginServlet.EMAIL_PARAMETER_NAME + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(maybePairAccountSite.contains(
                "Go to <a href=\"https://www.miele.com/f/com/en/register_api.aspx\">the Miele developer portal</a> to obtain your"));
        assertTrue(maybePairAccountSite.contains("Missing bridge ID."));
    }

    @Test
    public void whenAMalformedBridgeUidIsPassedThenTheBrowserIsRedirectedToThePairSiteAndAWarningIsDisplayed()
            throws Exception {
        // when:
        Website maybePairAccountSite = getCrawler().doGetRelative("/mielecloud/forwardToLogin?"
                + ForwardToLoginServlet.CLIENT_ID_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_ID + "&"
                + ForwardToLoginServlet.CLIENT_SECRET_PARAMETER_NAME + "="
                + MieleCloudBindingIntegrationTestConstants.CLIENT_SECRET + "&"
                + ForwardToLoginServlet.BRIDGE_ID_PARAMETER_NAME + "=genesis!" + "&"
                + ForwardToLoginServlet.EMAIL_PARAMETER_NAME + "=" + MieleCloudBindingIntegrationTestConstants.EMAIL);

        // then:
        assertTrue(maybePairAccountSite.contains(
                "Go to <a href=\"https://www.miele.com/f/com/en/register_api.aspx\">the Miele developer portal</a> to obtain your"));
        assertTrue(maybePairAccountSite
                .contains("Malformed bridge ID. A bridge ID may only contain letters, numbers, '-' and '_'!"));
    }

    @Test
    public void whenNoEmailIsPassedThenTheBrowserIsRedirectedToThePairSiteAndAWarningIsDisplayed() throws Exception {
        // when:
        Website maybePairAccountSite = getCrawler()
                .doGetRelative("/mielecloud/forwardToLogin?" + ForwardToLoginServlet.CLIENT_ID_PARAMETER_NAME + "="
                        + MieleCloudBindingIntegrationTestConstants.CLIENT_ID + "&"
                        + ForwardToLoginServlet.CLIENT_SECRET_PARAMETER_NAME + "="
                        + MieleCloudBindingIntegrationTestConstants.CLIENT_SECRET + "&"
                        + ForwardToLoginServlet.BRIDGE_ID_PARAMETER_NAME + "="
                        + MieleCloudBindingIntegrationTestConstants.BRIDGE_ID);

        // then:
        assertTrue(maybePairAccountSite.contains(
                "Go to <a href=\"https://www.miele.com/f/com/en/register_api.aspx\">the Miele developer portal</a> to obtain your"));
        assertTrue(maybePairAccountSite.contains("Missing e-mail address."));
    }

    @Test
    public void whenAnEmptyEmailIsPassedThenTheBrowserIsRedirectedToThePairSiteAndAWarningIsDisplayed()
            throws Exception {
        // when:
        Website maybePairAccountSite = getCrawler()
                .doGetRelative("/mielecloud/forwardToLogin?" + ForwardToLoginServlet.CLIENT_ID_PARAMETER_NAME + "="
                        + MieleCloudBindingIntegrationTestConstants.CLIENT_ID + "&"
                        + ForwardToLoginServlet.CLIENT_SECRET_PARAMETER_NAME + "="
                        + MieleCloudBindingIntegrationTestConstants.CLIENT_SECRET + "&"
                        + ForwardToLoginServlet.BRIDGE_ID_PARAMETER_NAME + "="
                        + MieleCloudBindingIntegrationTestConstants.BRIDGE_ID + "&"
                        + ForwardToLoginServlet.EMAIL_PARAMETER_NAME + "=");

        // then:
        assertTrue(maybePairAccountSite.contains(
                "Go to <a href=\"https://www.miele.com/f/com/en/register_api.aspx\">the Miele developer portal</a> to obtain your"));
        assertTrue(maybePairAccountSite.contains("Missing e-mail address."));
    }
}
