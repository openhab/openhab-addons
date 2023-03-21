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
package org.openhab.voice.actiontemplatehli.internal.rest;

import java.io.IOException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.http.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link HttpContext} which will handle the gzip-compressed assets.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class ActionTemplateHttpContext implements HttpContext {
    private final Logger logger = LoggerFactory.getLogger(ActionTemplateHttpContext.class);

    private final HttpContext defaultHttpContext;
    private final String resourcesBase;

    /**
     * Constructs an {@link ActionTemplateHttpContext} with will another {@link HttpContext} as a base.
     *
     * @param defaultHttpContext the base {@link HttpContext}
     */
    public ActionTemplateHttpContext(HttpContext defaultHttpContext, String resourcesBase) {
        this.defaultHttpContext = defaultHttpContext;
        this.resourcesBase = resourcesBase;
    }

    @Override
    public boolean handleSecurity(@Nullable HttpServletRequest request, @Nullable HttpServletResponse response)
            throws IOException {
        return defaultHttpContext.handleSecurity(request, response);
    }

    @Override
    public URL getResource(@Nullable String name) {
        logger.debug("Requesting resource: {}", name);
        // Get the gzipped version for selected resources, built as static resources
        URL defaultResource = defaultHttpContext.getResource(name);
        if (name != null) {
            if (name.equals(resourcesBase) || (resourcesBase + "/").equals(name) || !name.contains(".")) {
                return defaultHttpContext.getResource(resourcesBase + "/index.html");
            }
        }
        return defaultResource;
    }

    @Override
    public String getMimeType(@Nullable String name) {
        return defaultHttpContext.getMimeType(name);
    }
}
