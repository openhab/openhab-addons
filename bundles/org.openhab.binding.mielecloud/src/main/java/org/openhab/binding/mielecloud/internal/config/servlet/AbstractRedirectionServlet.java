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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for servlets that have no visible frontend and just serve the purpose of redirecting the user to another
 * website.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public abstract class AbstractRedirectionServlet extends HttpServlet {
    private static final long serialVersionUID = 4280026301732437523L;

    private final Logger logger = LoggerFactory.getLogger(AbstractRedirectionServlet.class);

    @Override
    protected void doGet(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws ServletException, IOException {
        if (response == null) {
            logger.warn("Ignoring received request without response.");
            return;
        }
        if (request == null) {
            logger.warn("Ignoring illegal request.");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        response.sendRedirect(getRedirectionDestination(request));
    }

    /**
     * Gets the redirection destination. This can be a relative or absolute path or a link to another website.
     *
     * @param request The original request sent by the browser.
     * @return The redirection destination.
     */
    protected abstract String getRedirectionDestination(HttpServletRequest request);
}
