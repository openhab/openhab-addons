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
package org.openhab.binding.smartthings.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.stream.Collectors;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Receives all Http data from the Smartthings Hub
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("serial")
public class SmartthingsServlet extends HttpServlet {
    private static final String PATH = "/smartthings";
    private final Logger logger = LoggerFactory.getLogger(SmartthingsServlet.class);
    private @NonNullByDefault({}) HttpService httpService;
    private @Nullable EventAdmin eventAdmin;
    private Gson gson = new Gson();

    public SmartthingsServlet(HttpService httpService) {
        this.httpService = httpService;
    }

    @Activate
    public void activate() {
        if (httpService == null) {
            logger.info("SmartthingsServlet.activate: httpService is unexpectedly null");
            return;
        }
        try {
            Dictionary<String, String> servletParams = new Hashtable<String, String>();
            logger.info("registerServlet:" + PATH);
            httpService.registerServlet(PATH, this, servletParams, httpService.createDefaultHttpContext());
            httpService.registerResources(PATH + "/img", "web", null);

            //
        } catch (ServletException | NamespaceException e) {
            logger.warn("Could not start Smartthings servlet service: {}", e.getMessage());
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        if (httpService != null) {
            try {
                httpService.unregister(PATH);
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    @Override
    public void init(@Nullable ServletConfig servletConfig) throws ServletException {

        logger.info("SmartthingsServlet:init");
        ServletContext context = servletConfig.getServletContext();
        BundleContext bundleContext = (BundleContext) context.getAttribute("osgi-bundlecontext");
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
    protected void doGet(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {
        logger.info("SmartthingsServlet:doGet");
    }

    @Override
    protected void service(@Nullable HttpServletRequest req, @Nullable HttpServletResponse resp)
            throws ServletException, IOException {

        logger.info("SmartthingsServlet:service");

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

        JsonObject resultObj = gson.fromJson(s, JsonObject.class);
        String lifeCycle = resultObj.get("lifecycle").getAsString();

        if (lifeCycle.equals("EVENT")) {
            JsonObject eventData = resultObj.get("eventData").getAsJsonObject();
            JsonArray events = eventData.get("events").getAsJsonArray();
            JsonObject event = events.get(0).getAsJsonObject();
            String eventType = event.get("eventType").getAsString();
            JsonObject deviceEvent = event.get("deviceEvent").getAsJsonObject();
            String value = deviceEvent.get("value").getAsString();
            String locationId = deviceEvent.get("locationId").getAsString();
            String deviceId = deviceEvent.get("deviceId").getAsString();
        } else if (lifeCycle.equals("CONFIGURATION")) {
            resp.getWriter().print("");
            resp.getWriter().print("{");
            resp.getWriter().print("\"configurationData\": {");
            resp.getWriter().print("\"initialize\": {");
            resp.getWriter().print("\"name\": \"Openhab2\",");
            resp.getWriter().print("\"description\": \"Openhab2 Desc\",");
            resp.getWriter().print("    \"id\": \"6756ca2f-ba54-470b-bcd9-dbe3c564c9d0\",");
            resp.getWriter().print("    \"permissions\": [\"r:devices\"],");
            resp.getWriter().print("    \"firstPageId\": \"1\"");
            resp.getWriter().print("  }");
            resp.getWriter().print(" }");
            resp.getWriter().print("}");
        } else if (lifeCycle.equals("PAGE")) {
            resp.getWriter().print("{");
            resp.getWriter().print("\"configurationData\": {");
            resp.getWriter().print("\"page\": {");
            resp.getWriter().print("      \"pageId\": \"1\",");
            resp.getWriter().print("      \"name\": \"aaa\",");
            resp.getWriter().print("      \"nextPageId\": null,");
            resp.getWriter().print("      \"previousPageId\": null,");
            resp.getWriter().print("      \"complete\": true,");
            resp.getWriter().print("      \"sections\": [");
            resp.getWriter().print("          {");
            resp.getWriter().print("              \"name\": \"When this opens closes...\",");
            resp.getWriter().print("              \"settings\": [");
            resp.getWriter().print("              {");
            resp.getWriter().print("                  \"id\": \"contactSensor2\",");
            resp.getWriter().print("                  \"name\": \"Which contact sensor\",");
            resp.getWriter().print("                  \"description\": \"Tap to set\",");
            resp.getWriter().print("                  \"type\": \"TEXT\",");
            resp.getWriter().print("                  \"required\": true,");
            resp.getWriter().print("                  \"multiple\": false,");
            resp.getWriter().print("                  \"defaultValue\": \"Some default value\"");
            resp.getWriter().print("              }");
            resp.getWriter().print("              ]");
            resp.getWriter().print("          },");
            resp.getWriter().print("          {");
            resp.getWriter().print("            \"name\": \"Turn on off this light...\",");
            resp.getWriter().print("            \"settings\": [");
            resp.getWriter().print("              {");
            resp.getWriter().print("                \"id\": \"lightSwitch\",");
            resp.getWriter().print("                \"name\": \"Which switch?\",");
            resp.getWriter().print("                \"description\": \"Tap to set\",");
            resp.getWriter().print("                \"type\": \"DEVICE\",");
            resp.getWriter().print("                \"required\": true,");
            resp.getWriter().print("                \"multiple\": false,");
            resp.getWriter().print("                \"capabilities\": [");
            resp.getWriter().print("                  \"switch\"");
            resp.getWriter().print("                ],");
            resp.getWriter().print("                \"permissions\": [");
            resp.getWriter().print("                  \"r\",");
            resp.getWriter().print("                  \"x\"");
            resp.getWriter().print("                ]");
            resp.getWriter().print("              }");
            resp.getWriter().print("            ]");
            resp.getWriter().print("          }");
            resp.getWriter().print("      ]");
            resp.getWriter().print("  }");
            resp.getWriter().print("}");
            resp.getWriter().print("}");
        }

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
