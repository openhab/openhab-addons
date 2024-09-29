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
package org.openhab.binding.ipobserver.internal;

import static org.openhab.binding.ipobserver.internal.IpObserverBindingConstants.SERVER_UPDATE_URL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link IpObserverUpdateReceiver} captures any updates sent to the openHAB Jetty server if the weather station is
 * setup to direct the weather updates to the HTTP server of openHAB which is normally port 8080.
 *
 * @author Matthew Skinner - Initial contribution
 */
@NonNullByDefault
public class IpObserverUpdateReceiver extends HttpServlet {
    private static final long serialVersionUID = -234658674L;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private List<IpObserverHandler> listOfHandlers = new ArrayList<>(1);

    public IpObserverUpdateReceiver(HttpService httpService) {
        try {
            httpService.registerServlet(SERVER_UPDATE_URL, this, null, httpService.createDefaultHttpContext());
        } catch (NamespaceException | ServletException e) {
            logger.warn("Registering servlet failed:{}", e.getMessage());
        }
    }

    @Override
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp) throws IOException {
        if (req == null) {
            return;
        }
        String stationUpdate = req.getQueryString();
        if (stationUpdate == null) {
            return;
        }
        logger.debug("Weather station packet received from {}", req.getRemoteHost());
        for (IpObserverHandler ipObserverHandler : listOfHandlers) {
            ipObserverHandler.processServerQuery(stationUpdate);
        }
    }

    public void addStation(IpObserverHandler ipObserverHandler) {
        listOfHandlers.add(ipObserverHandler);
    }

    public void removeStation(IpObserverHandler ipObserverHandler) {
        listOfHandlers.remove(ipObserverHandler);
    }
}
