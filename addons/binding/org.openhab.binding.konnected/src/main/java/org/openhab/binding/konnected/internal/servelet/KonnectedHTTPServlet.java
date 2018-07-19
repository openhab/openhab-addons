/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.konnected.internal.servelet;

import java.io.IOException;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openhab.binding.konnected.internal.handler.KonnectedHandler;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Main OSGi service and HTTP servlet for Konnected Webhook.
 *
 * @author Zachary Christiansen - Initial contribution
 */
public class KonnectedHTTPServlet extends HttpServlet {
    private static final long serialVersionUID = 1288539782077957954L;
    private static final String PATH = "/Konnected";
    private static final String APPLICATION_JSON = "application/json";
    private static final String CHARSET = "utf-8";
    private final Gson gson = new Gson();

    private final Logger logger = LoggerFactory.getLogger(KonnectedHTTPServlet.class);

    private HttpService httpService;
    private KonnectedHandler bridgeHandler;
    private String path;

    public KonnectedHTTPServlet(HttpService httpService, String id) {
        this.httpService = httpService;
        this.path = PATH + "/" + id;
    }

    /**
     * OSGi activation callback.
     *
     * @param config Service config.
     **/
    public void activate(KonnectedHandler bridgeHandler) {
        this.bridgeHandler = bridgeHandler;

        try {
            logger.debug("Trying to Start Webhook.");
            httpService.registerServlet(path, this, null, httpService.createDefaultHttpContext());
            logger.debug("Started Konnected Webhook servlet at {}", path);
        } catch (ServletException | NamespaceException e) {
            logger.error("Could not start Netatmo Webhook servlet: {}", e.getMessage(), e);
        }
    }

    /**
     * OSGi deactivation callback.
     */
    public void deactivate() {
        httpService.unregister(path);
        logger.debug("Konnected webhook servlet stopped");
        this.bridgeHandler = null;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String data = inputStreamToString(req);
        if (data != null && bridgeHandler != null) {
            KonnectedModuleEvent event = gson.fromJson(data, KonnectedModuleEvent.class);
            String auth = req.getHeader("Authorization");
            this.bridgeHandler.handleWebHookEvent(event.getPin(), event.getState(), auth);
        }

        setHeaders(resp);
        resp.getWriter().write("");
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
