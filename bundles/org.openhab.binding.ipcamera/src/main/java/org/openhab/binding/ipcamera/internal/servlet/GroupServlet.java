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
package org.openhab.binding.ipcamera.internal.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.ipcamera.internal.handler.IpCameraGroupHandler;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GroupServlet} is responsible for serving files for a rotating feed of multiple cameras back to the Jetty
 * server normally found on port 8080
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class GroupServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final long serialVersionUID = -234658667574L;
    private final IpCameraGroupHandler handler;

    public GroupServlet(IpCameraGroupHandler ipCameraGroupHandler, HttpService httpService) {
        handler = ipCameraGroupHandler;
        try {
            httpService.registerServlet("/ipcamera/" + handler.getThing().getUID().getId(), this, null,
                    httpService.createDefaultHttpContext());
        } catch (NamespaceException | ServletException e) {
            logger.warn("Registering servlet failed:{}", e.getMessage());
        }
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        if (req == null) {
            return;
        }
        // TODO need to refactor all functions over from StreamServerGroupHandler
    }
}
