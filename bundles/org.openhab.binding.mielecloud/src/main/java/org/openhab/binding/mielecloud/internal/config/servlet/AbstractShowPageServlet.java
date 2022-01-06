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
 * Base class for servlets that show a visible frontend in the browser.
 *
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public abstract class AbstractShowPageServlet extends HttpServlet {
    private static final long serialVersionUID = 3820684716753275768L;

    private static final String CONTENT_TYPE = "text/html;charset=UTF-8";

    private final Logger logger = LoggerFactory.getLogger(AbstractShowPageServlet.class);

    private final ResourceLoader resourceLoader;

    protected ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    /**
     * Creates a new {@link AbstractShowPageServlet}.
     *
     * @param resourceLoader Loader for resource files.
     */
    public AbstractShowPageServlet(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

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

        try {
            String html = handleGetRequest(request, response);
            response.setContentType(CONTENT_TYPE);
            response.getWriter().write(html);
            response.getWriter().close();
        } catch (MieleHttpException e) {
            response.sendError(e.getHttpErrorCode());
        } catch (IOException e) {
            logger.warn("Failed to load resources.", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handles a GET request.
     *
     * @param request The request.
     * @param response The response.
     * @return A rendered HTML body to be displayed in the browser. The body will be framed by the binding's frontend
     *         layout.
     * @throws MieleHttpException if an error occurs that should be handled by sending a default error response.
     * @throws IOException if an error occurs while loading resources.
     */
    protected abstract String handleGetRequest(HttpServletRequest request, HttpServletResponse response)
            throws MieleHttpException, IOException;
}
