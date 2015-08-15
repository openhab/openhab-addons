/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.internal.icon;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This servlet answers requests to /images (which was the openHAB 1 location for icons)
 * with HTTP 301 (permanently moved) with the new location /icon
 *
 * @author Kai Kreuzer
 *
 */
public class IconForwarder extends HttpServlet {

    private static final long serialVersionUID = 5220836868829415723L;

    final static private Logger logger = LoggerFactory.getLogger(IconForwarder.class);

    private static final String IMAGES_ALIAS = "/images";

    protected void setHttpService(HttpService httpService) {
        try {
            httpService.registerServlet(IMAGES_ALIAS, this, null, httpService.createDefaultHttpContext());
        } catch (Exception e) {
            logger.error("Could not register icon forwarder servlet: {}", e.getMessage());
        }
    }

    protected void unsetHttpService(HttpService httpService) {
        httpService.unregister(IMAGES_ALIAS);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        resp.setStatus(301);
        resp.setHeader("Location", "/icon" + req.getPathInfo());
        resp.setHeader("Connection", "close");
    }
}
