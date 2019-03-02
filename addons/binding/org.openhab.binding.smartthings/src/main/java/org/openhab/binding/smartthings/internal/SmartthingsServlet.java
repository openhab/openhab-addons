/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.openhab.binding.smartthings.SmartthingsBindingConstants.*;

import java.io.IOException;
import java.io.Reader;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openhab.binding.smartthings.handler.SmartthingsBridgeHandler;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Receives all Http data from the Smartthings Hub
 *
 * @author Bob Raker - Initial contribution
 *
 */
@SuppressWarnings("serial")
public class SmartthingsServlet extends HttpServlet {
    private static final String PATH = "/smartthings";
    private Logger logger = LoggerFactory.getLogger(SmartthingsServlet.class);
    private HttpService httpService;
    SmartthingsBridgeHandler bridgeHandler;
    private EventAdmin eventAdmin;
    private Gson gson;

    protected void activate(Map<String, Object> config) {
        // Get a Gson instance
        gson = new Gson();
        try {
            Dictionary<String, String> servletParams = new Hashtable<String, String>();
            httpService.registerServlet(PATH, this, servletParams, httpService.createDefaultHttpContext());
            logger.info("Started Smartthings servlet at {}", PATH);
        } catch (Exception e) {
            logger.warn("Could not start Smartthings servlet service: {}", e.getMessage());
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        try {
            httpService.unregister(PATH);
        } catch (IllegalArgumentException ignored) {
        }
    }

    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

    protected void setEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = eventAdmin;
    }

    protected void unsetEventAdmin(EventAdmin eventAdmin) {
        this.eventAdmin = null;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getRequestURI();
        logger.debug("Smartthings servlet service() called with: {}: {} {}", req.getRemoteAddr(), req.getMethod(),
                path);

        // See what is in the path
        String[] pathParts = path.replace(PATH + "/", "").split("/");
        logger.debug("Smartthing servlet function requested: {} with Method: {}", pathParts[0], req.getMethod());

        if (pathParts.length != 1) {
            logger.warn(
                    "Smartthing servlet recieved a path with zero or more than one parts. Only one part is allowed. path {}",
                    path);
            return;
        }

        if (pathParts[0].equals("state")) {
            // This is device state info returned from Smartthings
            Reader rdr = req.getReader();
            StringBuffer sb = new StringBuffer();
            int c;
            while ((c = rdr.read()) != -1) {
                sb.append((char) c);
            }
            rdr.close();
            logger.trace("Smartthing servlet processing \"state\" request. data: {}", sb);
            publishEvent(STATE_EVENT_TOPIC, "data", sb.toString());
        } else if (pathParts[0].equals("discovery")) {
            // This is discovery data returned from Smartthings
            Reader rdr = req.getReader();
            StringBuffer sb = new StringBuffer();
            int c;
            while ((c = rdr.read()) != -1) {
                sb.append((char) c);
            }
            rdr.close();
            logger.trace("Smartthing servlet processing \"discovery\" request. data: {}", sb);
            publishEvent(DISCOVERY_EVENT_TOPIC, "data", sb.toString());
        } else if (pathParts[0].equals("error")) {
            // This is an error message from smartthings
            Reader rdr = req.getReader();
            StringBuffer sb = new StringBuffer();
            int c;
            while ((c = rdr.read()) != -1) {
                sb.append((char) c);
            }
            rdr.close();
            logger.trace("Smartthing servlet processing \"error\" request. data: {}", sb);
            Map<String, Object> map = new HashMap<String, Object>();
            map = gson.fromJson(sb.toString(), map.getClass());
            StringBuffer msg = new StringBuffer("Error message from Smartthings: ");
            msg.append(map.get("message"));
            logger.warn("{}", msg);
        } else {
            logger.warn("Smartthing servlet recieved a path that is not supported {}", pathParts[0]);
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
        eventAdmin.postEvent(event);
    }

}
