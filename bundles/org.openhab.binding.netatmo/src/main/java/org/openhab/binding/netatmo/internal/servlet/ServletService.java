/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.servlet;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.BINDING_ID;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.handler.ApiBridgeHandler;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ServletService} class to manage the servlets and bind authorization servlet to bridges.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@Component(service = ServletService.class)
@NonNullByDefault
public class ServletService {
    private static final String BASE_PATH = "/" + BINDING_ID + "/";

    private final Logger logger = LoggerFactory.getLogger(ServletService.class);
    private final Map<String, ApiBridgeHandler> accountHandlers = new HashMap<>();
    private final HttpService httpService;
    private final BundleContext bundleContext;
    private final Map<String, HttpServlet> servlets = new HashMap<>();

    @Activate
    public ServletService(@Reference HttpService httpService, ComponentContext componentContext) {
        this.httpService = httpService;
        this.bundleContext = componentContext.getBundleContext();
        createAuthenticationServlet();
    }

    private void createAuthenticationServlet() {
        AuthenticationServlet authServlet = new AuthenticationServlet(this, bundleContext);
        registerServlet(authServlet, authServlet.getPath());
    }

    public WebhookServlet createWebhookServlet(ApiBridgeHandler apiBridgeHandler, String clientId, String webHookUrl) {
        WebhookServlet webhookServlet = new WebhookServlet(apiBridgeHandler, webHookUrl, clientId);
        registerServlet(webhookServlet, webhookServlet.getPath());
        return webhookServlet;
    }

    private void registerServlet(HttpServlet servlet, String servletPath) {
        String path = BASE_PATH + servletPath;
        try {
            httpService.registerServlet(path, servlet, new Hashtable<>(), httpService.createDefaultHttpContext());
            servlets.put(servletPath, servlet);
            logger.info("Registered Netatmo {} servlet at '{}'", servlet.getClass().getName(), servletPath);
        } catch (NamespaceException | ServletException e) {
            logger.warn("Error during Netatmo authentication servlet startup", e.getMessage());
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        servlets.keySet().forEach(alias -> {
            httpService.unregister(alias);
        });
        servlets.clear();
    }

    /**
     * @param listener Adds the given handler
     */
    public void addAccountHandler(ApiBridgeHandler listener) {
        accountHandlers.put(listener.getUIDString(), listener);
    }

    /**
     * @param handler Removes the given handler
     */
    public void removeAccountHandler(ApiBridgeHandler apiBridge) {
        // TODO Ca ne marche pas mais l'idée est là...
        servlets.forEach((alias, handler) -> {
            if (handler.equals(apiBridge)) {
                WebhookServlet webhook = (WebhookServlet) servlets.remove(alias);
                webhook.dispose();
            }
        });
        accountHandlers.remove(apiBridge.getUIDString());
    }

    /**
     * @return Returns all {@link ApiBridgeHandler}s.
     */
    public Map<String, ApiBridgeHandler> getAccountHandlers() {
        return accountHandlers;
    }
}
