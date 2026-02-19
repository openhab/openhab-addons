/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.servlet;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.BINDING_ID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NetatmoServlet} is the ancestor class for Netatmo servlets
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public abstract class NetatmoServlet extends HttpServlet {
    private static final long serialVersionUID = 5671438863935117735L;

    private final Logger logger = LoggerFactory.getLogger(NetatmoServlet.class);
    private final HttpService httpService;
    private final String path;

    protected final ApiBridgeHandler handler;

    public NetatmoServlet(ApiBridgeHandler handler, HttpService httpService, String localPath) {
        this.path = "/" + BINDING_ID + "/" + localPath + "/" + handler.getId();
        this.httpService = httpService;
        this.handler = handler;
    }

    public void startListening() {
        try {
            httpService.registerServlet(path, this, null, httpService.createDefaultHttpContext());
            logger.info("Registered Netatmo servlet at '{}'", path);
        } catch (NamespaceException | ServletException e) {
            logger.warn("Registering servlet failed:{}", e.getMessage());
        }
    }

    public void dispose() {
        logger.debug("Stopping Netatmo Servlet {}", path);
        httpService.unregister(path);
        this.destroy();
    }

    public String getPath() {
        return path;
    }
}
