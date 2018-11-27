/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.konnected.internal.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openhab.binding.konnected.internal.gson.KonnectedModuleGson;
import org.openhab.binding.konnected.internal.handler.KonnectedHandler;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Main OSGi service and HTTP servlet for Konnected Webhook.
 *
 * @author Zachary Christiansen - Initial contribution
 */
public class KonnectedHTTPServlet extends HttpServlet {
    private final Logger logger = LoggerFactory.getLogger(KonnectedHTTPServlet.class);

    private static final long serialVersionUID = 1288539782077957954L;
    private static final String APPLICATION_JSON = "application/json";
    private static final String CHARSET = "utf-8";
    private final Gson gson = new Gson();

    private final HttpService httpService;

    private final String path;
    private HashMap<String, KonnectedHandler> konnectedThingHandlers = new HashMap<>();

    public KonnectedHTTPServlet(HttpService httpService, String id) {
        this.httpService = httpService;
        this.path = id;
    }

    public void add(KonnectedHandler thingHandler) throws KonnectedWebHookFail {
        logger.trace("Adding KonnectedHandler[{}] to KonnectedHTTPServlet.", thingHandler.getThing().getUID());

        if (konnectedThingHandlers.size() == 0) {
            this.activate();
        }

        konnectedThingHandlers.put(thingHandler.getThing().getUID().getAsString(), thingHandler);
    }

    public void remove(KonnectedHandler thingHandler) {
        logger.trace("Removing KonnectedHandler [{}] from KonnectedHTTP Servlet. ", thingHandler.getThing().getUID());

        konnectedThingHandlers.remove(thingHandler.getThing().getUID().getAsString());

        if (konnectedThingHandlers.size() == 0) {
            this.deactivate();
        }
    }

    /**
     * Activation callback.
     *
     * @param config Service config.
     **/
    public void activate() throws KonnectedWebHookFail {
        try {
            logger.debug("Trying to Start Webhook at {}.", path);
            httpService.registerServlet(path, this, null, httpService.createDefaultHttpContext());
            logger.debug("Started Konnected Webhook servlet at {}", path);
        } catch (ServletException | NamespaceException e) {
            throw new KonnectedWebHookFail("Could not start Konnected Webhook servlet: " + e.getMessage(), e);
        }
    }

    /**
     * Webhook Deactivation callback.
     */
    public void deactivate() {
        httpService.unregister(path);
        logger.debug("Konnected webhook servlet stopped");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String data = inputStreamToString(req);
            logger.debug("The raw json data is: {}", data);
            if (data != null && !(konnectedThingHandlers.size() == 0)) {
                KonnectedModuleGson event = gson.fromJson(data, KonnectedModuleGson.class);
                String authorizationHeader = req.getHeader("Authorization");
                String thingHandlerKey = authorizationHeader.substring("Bearer".length()).trim();
                logger.debug("The path of the response was: {}", req.getContextPath());
                logger.debug("The json received was: {}", event.toString());
                logger.debug("The thing handler to send the command to is the handler for thing: {}", thingHandlerKey);
                try {
                    KonnectedHandler thingHandler = konnectedThingHandlers.get(thingHandlerKey);
                    thingHandler.handleWebHookEvent(event);
                } catch (NullPointerException e) {
                    logger.debug("There was not a handler registered on the servlet to handler commands for thing: {}",
                            thingHandlerKey);
                }
            }

            setHeaders(resp);
            resp.getWriter().write("");
        } catch (IOException | JsonSyntaxException e) {
            logger.debug("The response received from the module was not valid. {}", e.getMessage());
        }
    }

    private String inputStreamToString(HttpServletRequest req) throws IOException {
        Scanner scanner = new Scanner(req.getInputStream()).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }

    private void setHeaders(HttpServletResponse response) {
        response.setCharacterEncoding(CHARSET);
        response.setContentType(APPLICATION_JSON);
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "PUT");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    }

    public String getPath() {
        return path;
    }
}
