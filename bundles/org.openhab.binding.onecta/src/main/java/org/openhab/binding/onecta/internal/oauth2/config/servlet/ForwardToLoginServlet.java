/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.onecta.internal.oauth2.config.servlet;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.onecta.internal.constants.OnectaBridgeConstants;
import org.openhab.binding.onecta.internal.oauth2.auth.OAuthException;
import org.openhab.binding.onecta.internal.oauth2.config.OAuthAuthorizationHandler;
import org.openhab.binding.onecta.internal.oauth2.config.exception.NoOngoingAuthorizationException;
import org.openhab.binding.onecta.internal.oauth2.config.exception.OngoingAuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet gathers and processes required information to perform an authorization with the Onecta service
 * and create a bridge afterwards. Required parameters are the client ID, client secret, an ID for the bridge and an
 * e-mail address. If the given parameters are valid, the browser is redirected to the Onecta service login. Otherwise,
 * the browser is redirected to the previous page with an according error message.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class ForwardToLoginServlet extends AbstractRedirectionServlet {
    private static final long serialVersionUID = -9094642228439994183L;

    public static final String CLIENT_ID_PARAMETER_NAME = "clientId";
    public static final String CLIENT_SECRET_PARAMETER_NAME = "clientSecret";
    public static final String RETURN_URL_PARAMETER_NAME = "returnUrl";
    public static final String BRIDGE_ID_PARAMETER_NAME = "bridgeId";
    public static final String EMAIL_PARAMETER_NAME = "email";

    private final Logger logger = LoggerFactory.getLogger(ForwardToLoginServlet.class);

    private final OAuthAuthorizationHandler authorizationHandler;

    /**
     * Creates a new {@link ForwardToLoginServlet}.
     *
     * @param authorizationHandler Handler implementing the OAuth authorization process.
     */
    public ForwardToLoginServlet(OAuthAuthorizationHandler authorizationHandler) {
        this.authorizationHandler = authorizationHandler;
    }

    @Override
    protected String getRedirectionDestination(HttpServletRequest request) {
        String clientId = request.getParameter(CLIENT_ID_PARAMETER_NAME);
        String clientSecret = request.getParameter(CLIENT_SECRET_PARAMETER_NAME);
        String requestUrl = request.getParameter(RETURN_URL_PARAMETER_NAME);

        if (clientId == null || clientId.isEmpty()) {
            logger.warn("Request is missing client ID.");
            return getErrorRedirectionUrl(PairAccountServlet.MISSING_CLIENT_ID_PARAMETER_NAME);
        }
        clientId = clientId.strip();

        if (clientSecret == null || clientSecret.isEmpty()) {
            logger.warn("Request is missing client secret.");
            return getErrorRedirectionUrl(PairAccountServlet.MISSING_CLIENT_SECRET_PARAMETER_NAME);
        }
        clientSecret = clientSecret.strip();

        try {
            authorizationHandler.beginAuthorization(clientId, clientSecret,
                    OnectaBridgeConstants.OAUTH2_SERVICE_HANDLE);
        } catch (OngoingAuthorizationException e) {
            logger.warn("Cannot begin new authorization process while another one is still running.");
            return getErrorRedirectUrlWithExpiryTime(e.getOngoingAuthorizationExpiryTimestamp());
        }

        if (requestUrl == null) {
            return getErrorRedirectionUrl(PairAccountServlet.MISSING_REQUEST_URL_PARAMETER_NAME);
        }

        try {
            return authorizationHandler.getAuthorizationUrl(deriveRedirectUri(requestUrl.toString()));
        } catch (NoOngoingAuthorizationException e) {
            logger.warn(
                    "Failed to create authorization URL: There was no ongoing authorization although we just started one.");
            return getErrorRedirectionUrl(PairAccountServlet.NO_ONGOING_AUTHORIZATION_IN_STEP2_PARAMETER_NAME);
        } catch (OAuthException e) {
            logger.warn("Failed to create authorization URL.", e);
            return getErrorRedirectionUrl(PairAccountServlet.FAILED_TO_DERIVE_REDIRECT_URL_PARAMETER_NAME);
        }
    }

    private String getErrorRedirectUrlWithExpiryTime(@Nullable LocalDateTime ongoingAuthorizationExpiryTimestamp) {
        if (ongoingAuthorizationExpiryTimestamp == null) {
            return getErrorRedirectionUrl(
                    PairAccountServlet.ONGOING_AUTHORIZATION_IN_STEP1_EXPIRES_IN_MINUTES_PARAMETER_NAME,
                    PairAccountServlet.ONGOING_AUTHORIZATION_UNKNOWN_EXPIRY_TIME);
        }

        long minutesUntilExpiry = ChronoUnit.MINUTES.between(LocalDateTime.now(), ongoingAuthorizationExpiryTimestamp)
                + 1;
        return getErrorRedirectionUrl(
                PairAccountServlet.ONGOING_AUTHORIZATION_IN_STEP1_EXPIRES_IN_MINUTES_PARAMETER_NAME,
                Long.toString(minutesUntilExpiry));
    }

    private String getErrorRedirectionUrl(String errorCode) {
        return getErrorRedirectionUrl(errorCode, "true");
    }

    private String getErrorRedirectionUrl(String errorCode, String parameterValue) {
        return "/onecta/pair?" + errorCode + "=" + parameterValue;
    }

    private String deriveRedirectUri(String requestUrl) {
        return requestUrl.replace("forwardToLogin", "result");
    }
}
