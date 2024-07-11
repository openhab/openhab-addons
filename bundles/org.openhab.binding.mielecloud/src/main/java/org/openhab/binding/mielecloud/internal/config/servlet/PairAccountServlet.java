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
package org.openhab.binding.mielecloud.internal.config.servlet;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Servlet showing the pair account page.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public final class PairAccountServlet extends AbstractShowPageServlet {
    private static final long serialVersionUID = 6565378471951635420L;

    public static final String CLIENT_ID_PARAMETER_NAME = "clientId";
    public static final String CLIENT_SECRET_PARAMETER_NAME = "clientSecret";

    public static final String MISSING_CLIENT_ID_PARAMETER_NAME = "missingClientId";
    public static final String MISSING_CLIENT_SECRET_PARAMETER_NAME = "missingClientSecret";
    public static final String MISSING_BRIDGE_ID_PARAMETER_NAME = "missingBridgeId";
    public static final String MISSING_EMAIL_PARAMETER_NAME = "missingEmail";
    public static final String MALFORMED_BRIDGE_ID_PARAMETER_NAME = "malformedBridgeId";
    public static final String FAILED_TO_DERIVE_REDIRECT_URL_PARAMETER_NAME = "failedToDeriveRedirectUrl";
    public static final String ONGOING_AUTHORIZATION_IN_STEP1_EXPIRES_IN_MINUTES_PARAMETER_NAME = "ongoingAuthorizationInStep1ExpiresInMinutes";
    public static final String ONGOING_AUTHORIZATION_UNKNOWN_EXPIRY_TIME = "unknown";
    public static final String NO_ONGOING_AUTHORIZATION_IN_STEP2_PARAMETER_NAME = "noOngoingAuthorizationInStep2";
    public static final String MISSING_REQUEST_URL_PARAMETER_NAME = "missingRequestUrl";

    private static final String PAIR_ACCOUNT_SKELETON = "pairing.html";

    private static final String CLIENT_ID_PLACEHOLDER = "<!-- CLIENT ID -->";
    private static final String CLIENT_SECRET_PLACEHOLDER = "<!-- CLIENT SECRET -->";
    private static final String ERROR_MESSAGE_PLACEHOLDER = "<!-- ERROR MESSAGE -->";

    /**
     * Creates a new {@link PairAccountServlet}.
     *
     * @param resourceLoader Loader for resources.
     */
    public PairAccountServlet(ResourceLoader resourceLoader) {
        super(resourceLoader);
    }

    @Override
    protected String handleGetRequest(HttpServletRequest request, HttpServletResponse response)
            throws MieleHttpException, IOException {
        String skeleton = getResourceLoader().loadResourceAsString(PAIR_ACCOUNT_SKELETON);
        skeleton = renderClientIdAndClientSecret(request, skeleton);
        skeleton = renderErrorMessage(request, skeleton);
        return skeleton;
    }

    private String renderClientIdAndClientSecret(HttpServletRequest request, String skeleton) {
        String prefilledClientId = Objects.requireNonNullElse(request.getParameter(CLIENT_ID_PARAMETER_NAME), "");
        String prefilledClientSecret = Objects.requireNonNullElse(request.getParameter(CLIENT_SECRET_PARAMETER_NAME),
                "");
        return skeleton.replace(CLIENT_ID_PLACEHOLDER, prefilledClientId).replace(CLIENT_SECRET_PLACEHOLDER,
                prefilledClientSecret);
    }

    private String renderErrorMessage(HttpServletRequest request, String skeleton) {
        if (ServletUtil.isParameterEnabled(request, MISSING_CLIENT_ID_PARAMETER_NAME)) {
            return skeleton.replace(ERROR_MESSAGE_PLACEHOLDER,
                    "<div class=\"alert alert-danger\" role=\"alert\">Missing client ID.</div>");
        } else if (ServletUtil.isParameterEnabled(request, MISSING_CLIENT_SECRET_PARAMETER_NAME)) {
            return skeleton.replace(ERROR_MESSAGE_PLACEHOLDER,

                    "<div class=\"alert alert-danger\" role=\"alert\">Missing client secret.</div>");
        } else if (ServletUtil.isParameterEnabled(request, MISSING_BRIDGE_ID_PARAMETER_NAME)) {
            return skeleton.replace(ERROR_MESSAGE_PLACEHOLDER,
                    "<div class=\"alert alert-danger\" role=\"alert\">Missing bridge ID.</div>");
        } else if (ServletUtil.isParameterEnabled(request, MISSING_EMAIL_PARAMETER_NAME)) {
            return skeleton.replace(ERROR_MESSAGE_PLACEHOLDER,
                    "<div class=\"alert alert-danger\" role=\"alert\">Missing e-mail address.</div>");
        } else if (ServletUtil.isParameterEnabled(request, MALFORMED_BRIDGE_ID_PARAMETER_NAME)) {
            return skeleton.replace(ERROR_MESSAGE_PLACEHOLDER,
                    "<div class=\"alert alert-danger\" role=\"alert\">Malformed bridge ID. A bridge ID may only contain letters, numbers, '-' and '_'!</div>");
        } else if (ServletUtil.isParameterEnabled(request, FAILED_TO_DERIVE_REDIRECT_URL_PARAMETER_NAME)) {
            return skeleton.replace(ERROR_MESSAGE_PLACEHOLDER,
                    "<div class=\"alert alert-danger\" role=\"alert\">Failed to derive redirect URL.</div>");
        } else if (ServletUtil.isParameterPresent(request,
                ONGOING_AUTHORIZATION_IN_STEP1_EXPIRES_IN_MINUTES_PARAMETER_NAME)) {
            String minutesUntilExpiry = request
                    .getParameter(ONGOING_AUTHORIZATION_IN_STEP1_EXPIRES_IN_MINUTES_PARAMETER_NAME);
            if (ONGOING_AUTHORIZATION_UNKNOWN_EXPIRY_TIME.equals(minutesUntilExpiry)) {
                return skeleton.replace(ERROR_MESSAGE_PLACEHOLDER,
                        "<div class=\"alert alert-danger\" role=\"alert\">There is an authorization ongoing at the moment. Please complete that authorization prior to starting a new one or try again later.</div>");
            } else {
                return skeleton.replace(ERROR_MESSAGE_PLACEHOLDER,
                        "<div class=\"alert alert-danger\" role=\"alert\">There is an authorization ongoing at the moment. Please complete that authorization prior to starting a new one or try again in "
                                + minutesUntilExpiry + " minutes.</div>");
            }
        } else if (ServletUtil.isParameterEnabled(request, NO_ONGOING_AUTHORIZATION_IN_STEP2_PARAMETER_NAME)) {
            return skeleton.replace(ERROR_MESSAGE_PLACEHOLDER,
                    "<div class=\"alert alert-danger\" role=\"alert\">Failed to start auhtorization process. Are you trying to perform multiple authorizations at the same time?</div>");
        } else if (ServletUtil.isParameterEnabled(request, MISSING_REQUEST_URL_PARAMETER_NAME)) {
            return skeleton.replace(ERROR_MESSAGE_PLACEHOLDER,
                    "<div class=\"alert alert-danger\" role=\"alert\">Missing request URL. Please try again.</div>");
        } else {
            return skeleton.replace(ERROR_MESSAGE_PLACEHOLDER, "");
        }
    }
}
