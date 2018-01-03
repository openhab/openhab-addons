/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.swagger.client.model.NAWebhookCameraEvent;

/**
 * Main OSGi service and HTTP servlet for Netatmo Welcome Webhook.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(service = HttpServlet.class, configurationPolicy = ConfigurationPolicy.OPTIONAL, immediate = true)
public class WelcomeWebHookServlet extends HttpServlet {
    private static final long serialVersionUID = 1288539782077957954L;
    public static final String PATH = "/netatmo/camera";
    private static final String APPLICATION_JSON = "application/json";
    private static final String CHARSET = "utf-8";
    private final Gson gson = new Gson();

    private final Logger logger = LoggerFactory.getLogger(WelcomeWebHookServlet.class);

    private HttpService httpService;
    private NetatmoHandlerFactory netatmoHandlerFactory;

    /**
     * OSGi activation callback.
     *
     * @param config Service config.
     */
    @Activate
    protected void activate(Map<String, Object> config) {
        try {
            httpService.registerServlet(PATH, this, null, httpService.createDefaultHttpContext());
            logger.info("Started Netatmo Webhook servlet at {}", PATH);
        } catch (ServletException | NamespaceException e) {
            logger.error("Could not start Netatmo Webhook servlet: {}", e.getMessage(), e);
        }
    }

    /**
     * OSGi deactivation callback.
     */
    @Deactivate
    protected void deactivate() {
        httpService.unregister(PATH);
        logger.info("Netatmo webhook servlet stopped");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String data = inputStreamToString(req);
        if (data != null && netatmoHandlerFactory != null) {
            NAWebhookCameraEvent event = gson.fromJson(data, NAWebhookCameraEvent.class);
            netatmoHandlerFactory.webHookEvent(event);
        }

        setHeaders(resp);
        resp.getWriter().write("");
    }

    private String inputStreamToString(HttpServletRequest req) throws IOException {
        Scanner scanner = new Scanner(req.getInputStream()).useDelimiter("\\A");
        String result = scanner.hasNext() ? scanner.next() : "";
        return result;
    }

    private void setHeaders(HttpServletResponse response) {
        response.setCharacterEncoding(CHARSET);
        response.setContentType(APPLICATION_JSON);
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    public void setNetatmoHandlerFactory(NetatmoHandlerFactory netatmoHandlerFactory) {
        this.netatmoHandlerFactory = netatmoHandlerFactory;
    }

    public void unsetNetatmoHandlerFactory(NetatmoHandlerFactory netatmoHandlerFactory) {
        this.netatmoHandlerFactory = null;
    }

    @Reference
    public void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    public void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

}
