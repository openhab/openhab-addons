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

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Servlet showing a failure page.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public class FailureServlet extends AbstractShowPageServlet {
    private static final long serialVersionUID = -5195984256535664942L;

    public static final String OAUTH2_ERROR_PARAMETER_NAME = "oauth2Error";
    public static final String ILLEGAL_RESPONSE_PARAMETER_NAME = "illegalResponse";
    public static final String NO_ONGOING_AUTHORIZATION_PARAMETER_NAME = "noOngoingAuthorization";
    public static final String FAILED_TO_COMPLETE_AUTHORIZATION_PARAMETER_NAME = "failedToCompleteAuthorization";
    public static final String MISSING_BRIDGE_UID_PARAMETER_NAME = "missingBridgeUid";
    public static final String MISSING_EMAIL_PARAMETER_NAME = "missingEmail";
    public static final String MALFORMED_BRIDGE_UID_PARAMETER_NAME = "malformedBridgeUid";
    public static final String MISSING_REQUEST_URL_PARAMETER_NAME = "missingRequestUrl";

    public static final String OAUTH2_ERROR_ACCESS_DENIED = "access_denied";
    public static final String OAUTH2_ERROR_INVALID_REQUEST = "invalid_request";
    public static final String OAUTH2_ERROR_UNAUTHORIZED_CLIENT = "unauthorized_client";
    public static final String OAUTH2_ERROR_UNSUPPORTED_RESPONSE_TYPE = "unsupported_response_type";
    public static final String OAUTH2_ERROR_INVALID_SCOPE = "invalid_scope";
    public static final String OAUTH2_ERROR_SERVER_ERROR = "server_error";
    public static final String OAUTH2_ERROR_TEMPORARY_UNAVAILABLE = "temporarily_unavailable";

    private static final String ERROR_MESSAGE_TEXT_PLACEHOLDER = "<!-- ERROR MESSAGE TEXT -->";

    /**
     * Creates a new {@link FailureServlet}.
     *
     * @param resourceLoader Loader to use for resources.
     */
    public FailureServlet(ResourceLoader resourceLoader) {
        super(resourceLoader);
    }

    @Override
    protected String handleGetRequest(HttpServletRequest request, HttpServletResponse response)
            throws MieleHttpException, IOException {
        return getResourceLoader().loadResourceAsString("failure.html").replace(ERROR_MESSAGE_TEXT_PLACEHOLDER,
                getErrorMessage(request));
    }

    private String getErrorMessage(HttpServletRequest request) {
        String oauth2Error = request.getParameter(OAUTH2_ERROR_PARAMETER_NAME);
        if (oauth2Error != null) {
            return getOAuth2ErrorMessage(oauth2Error);
        } else if (ServletUtil.isParameterEnabled(request, ILLEGAL_RESPONSE_PARAMETER_NAME)) {
            return "Miele cloud service returned an illegal response.";
        } else if (ServletUtil.isParameterEnabled(request, NO_ONGOING_AUTHORIZATION_PARAMETER_NAME)) {
            return "There is no ongoing authorization. Please start an authorization first.";
        } else if (ServletUtil.isParameterEnabled(request, FAILED_TO_COMPLETE_AUTHORIZATION_PARAMETER_NAME)) {
            return "Completing the final authorization request failed. Please try the config flow again.";
        } else if (ServletUtil.isParameterEnabled(request, MISSING_BRIDGE_UID_PARAMETER_NAME)) {
            return "Missing bridge UID.";
        } else if (ServletUtil.isParameterEnabled(request, MISSING_EMAIL_PARAMETER_NAME)) {
            return "Missing e-mail address.";
        } else if (ServletUtil.isParameterEnabled(request, MALFORMED_BRIDGE_UID_PARAMETER_NAME)) {
            return "Malformed bridge UID.";
        } else if (ServletUtil.isParameterEnabled(request, MISSING_REQUEST_URL_PARAMETER_NAME)) {
            return "Missing request URL. Please try the config flow again.";
        } else {
            return "Unknown error.";
        }
    }

    private String getOAuth2ErrorMessage(String oauth2Error) {
        return "OAuth2 authentication with Miele cloud service failed: " + getOAuth2ErrorDetailMessage(oauth2Error);
    }

    private String getOAuth2ErrorDetailMessage(String oauth2Error) {
        switch (oauth2Error) {
            case OAUTH2_ERROR_ACCESS_DENIED:
                return "Access denied.";
            case OAUTH2_ERROR_INVALID_REQUEST:
                return "Malformed request.";
            case OAUTH2_ERROR_UNAUTHORIZED_CLIENT:
                return "Account not authorized to request authorization code.";
            case OAUTH2_ERROR_UNSUPPORTED_RESPONSE_TYPE:
                return "Obtaining an authorization code is not supported.";
            case OAUTH2_ERROR_INVALID_SCOPE:
                return "Invalid scope.";
            case OAUTH2_ERROR_SERVER_ERROR:
                return "Unexpected server error.";
            case OAUTH2_ERROR_TEMPORARY_UNAVAILABLE:
                return "Authorization server temporarily unavailable.";
            default:
                return "Unknown error code \"" + oauth2Error + "\".";
        }
    }
}
