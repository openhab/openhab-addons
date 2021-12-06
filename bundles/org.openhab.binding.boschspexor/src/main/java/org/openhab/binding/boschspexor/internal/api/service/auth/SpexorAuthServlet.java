/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.boschspexor.internal.api.service.auth;

import java.io.IOException;
import java.text.MessageFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SpexorAuthServlet} manages the information for the user to get the
 * status of the authorization with the spexor API. The servlet implements the
 * OAuth2.0 Device Code flow and saves the resulting refreshToken with the
 * bridge.
 *
 * @author Marc Fischer - Initial contribution
 */
@NonNullByDefault
public class SpexorAuthServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final Logger logger = LoggerFactory.getLogger(SpexorAuthServlet.class);
    private final SpexorUserGrantService grantService;
    private final String statusPage;

    public SpexorAuthServlet(SpexorUserGrantService grantService, String statusPage) {
        super();
        this.grantService = grantService;
        this.statusPage = statusPage;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (MimeTypes.Type.APPLICATION_JSON.asString().equalsIgnoreCase(req.getContentType())) {
            resp.setContentType(MimeTypes.Type.APPLICATION_JSON.asString());
            AuthProcessingStatus currentStatus = determineCurrentStatus();
            if ("authorize".equalsIgnoreCase(req.getParameter("action"))) {
                grantService.getAuthService().authorize();
                // start authorization
            }
            if (currentStatus.isError()) {
                logger.error("requested state of spexor grant service returned with an error: {}",
                        currentStatus.getErrorMessage());
                resp.sendError(HttpStatus.INTERNAL_SERVER_ERROR_500,
                        MessageFormat.format("{'status' : '{0}', 'message': '{1}'}", currentStatus.getState().name(),
                                currentStatus.getErrorMessage()));
            } else {
                resp.setStatus(HttpStatus.OK_200);
                resp.getWriter().append(
                        MessageFormat.format("{'status' : '{0}', 'message': null}", currentStatus.getState().name()));
            }
        } else {
            resp.setContentType(MimeTypes.Type.TEXT_HTML_UTF_8.asString());
            resp.getWriter().append(statusPage);
        }
    }

    private AuthProcessingStatus determineCurrentStatus() {
        AuthProcessingStatus result = null;
        SpexorAuthorizationService authService = grantService.getAuthService();
        if (authService == null || authService.getStatus() == null) {
            result = new AuthProcessingStatus();
            result.error("openHAB spexor grant service is not available");
            logger.error("openHAB spexorGrantService grant process is null");
        } else {
            result = authService.getStatus();
        }
        return result;
    }
}
