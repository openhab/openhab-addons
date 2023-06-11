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
package org.openhab.binding.smartthings.internal;

import static org.openhab.binding.smartthings.internal.SmartthingsBindingConstants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Receives all Http data from the Smartthings Hub
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("serial")
@Component(service = HttpServlet.class)
public class SmartthingsServlet extends HttpServlet {
    private static final String PATH = "/smartthings";
    private final Logger logger = LoggerFactory.getLogger(SmartthingsServlet.class);
    private @NonNullByDefault({}) HttpService httpService;
    private @Nullable EventAdmin eventAdmin;
    private Gson gson = new Gson();

    @Activate
    protected void activate(Map<String, Object> config) {
        if (httpService == null) {
            logger.warn("SmartthingsServlet.activate: httpService is unexpectedly null");
            return;
        }
        try {
            Dictionary<String, String> servletParams = new Hashtable<String, String>();
            httpService.registerServlet(PATH, this, servletParams, httpService.createDefaultHttpContext());
        } catch (ServletException | NamespaceException e) {
            logger.warn("Could not start Smartthings servlet service: {}", e.getMessage());
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        if (httpService != null) {
            try {
                httpService.unregister(PATH);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Reference
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    @Reference
    protected void setEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = eventAdmin;
    }

    protected void unsetEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = null;
    }

    @Override
    protected void service(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        if (req == null) {
            logger.debug("SmartthingsServlet.service unexpectedly received a null request. Request not processed");
            return;
        }
        String path = req.getRequestURI();

        // See what is in the path
        String[] pathParts = path.replace(PATH + "/", "").split("/");
        if (pathParts.length != 1) {
            logger.warn(
                    "Smartthing servlet received a path with zero or more than one parts. Only one part is allowed. path {}",
                    path);
            return;
        }

        BufferedReader rdr = new BufferedReader(req.getReader());
        String s = rdr.lines().collect(Collectors.joining());
        switch (pathParts[0]) {
            case "state":
                // This is device state info returned from Smartthings
                logger.debug("Smartthing servlet processing \"state\" request. data: {}", s);
                publishEvent(STATE_EVENT_TOPIC, "data", s);
                break;
            case "discovery":
                // This is discovery data returned from Smartthings
                logger.trace("Smartthing servlet processing \"discovery\" request. data: {}", s);
                publishEvent(DISCOVERY_EVENT_TOPIC, "data", s);
                break;
            case "error":
                // This is an error message from smartthings
                Map<String, String> map = new HashMap<String, String>();
                map = gson.fromJson(s, map.getClass());
                logger.warn("Error message from Smartthings: {}", map.get("message"));
                break;
            default:
                logger.warn("Smartthings servlet received a path that is not supported {}", pathParts[0]);
        }

        // A user @fx submitted a pull request stating:
        // It appears that the HubAction queue will choke for a timeout of 6-8s~ if a http action doesn't return a body
        // (or possibly on the 204 http code, I didn't test them separately.)
        // I tested the following scenarios:
        // 1. Return status 204 with a response of OK
        // 2. Return status 202 with no response
        // 3. No response.
        // In all cases the time was about the same - 3.5 sec/request
        // Both the 202 and 204 responses resulted in the hub logging an error: received a request with an unknown path:
        // HTTP/1.1 200 OK, content-Length: 0
        // Therefore I am opting to return nothing since no error message occurs.
        // resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        // resp.setStatus(HttpServletResponse.SC_OK);
        // resp.getWriter().write("OK");
        // resp.getWriter().flush();
        // resp.getWriter().close();
        logger.trace("Smartthings servlet returning.");
        return;
    }

    private void publishEvent(String topic, String name, String data) {
        Dictionary<String, String> props = new Hashtable<String, String>();
        props.put(name, data);
        Event event = new Event(topic, props);
        if (eventAdmin != null) {
            eventAdmin.postEvent(event);
        } else {
            logger.debug("SmartthingsServlet:publishEvent eventAdmin is unexpectedly null");
        }
    }
}
