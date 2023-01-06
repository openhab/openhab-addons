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
package org.openhab.binding.konnected.internal.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openhab.binding.konnected.internal.gson.KonnectedModuleGson;
import org.openhab.binding.konnected.internal.handler.KonnectedHandler;
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

    private HashMap<String, KonnectedHandler> konnectedThingHandlers = new HashMap<>();

    public KonnectedHTTPServlet() {
    }

    public void add(KonnectedHandler thingHandler) {
        logger.trace("Adding KonnectedHandler[{}] to KonnectedHTTPServlet.", thingHandler.getThing().getUID());
        konnectedThingHandlers.put(thingHandler.getThing().getUID().getAsString(), thingHandler);
    }

    public void remove(KonnectedHandler thingHandler) {
        logger.trace("Removing KonnectedHandler [{}] from KonnectedHTTPServlet. ", thingHandler.getThing().getUID());

        konnectedThingHandlers.remove(thingHandler.getThing().getUID().getAsString());
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        logger.debug("Unhandled get request: {}?{}", req.getRequestURI(), req.getQueryString());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        logger.debug("Got POST: {}", req.getRequestURI());
        handleDeviceCallback(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        logger.debug("Got PUT: {}", req.getRequestURI());
        handleDeviceCallback(req, resp);
    }

    private void handleDeviceCallback(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String data = inputStreamToString(req);

            logger.debug("The raw json data is: {}", data);
            if (data != null && !konnectedThingHandlers.isEmpty()) {
                KonnectedModuleGson event = gson.fromJson(data, KonnectedModuleGson.class);
                String authorizationHeader = req.getHeader("Authorization");
                String thingHandlerKey = authorizationHeader.substring("Bearer".length()).trim();
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
        return scanner.hasNext() ? scanner.next() : null;
    }

    private void setHeaders(HttpServletResponse response) {
        response.setCharacterEncoding(CHARSET);
        response.setContentType(APPLICATION_JSON);
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "PUT");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    }
}
