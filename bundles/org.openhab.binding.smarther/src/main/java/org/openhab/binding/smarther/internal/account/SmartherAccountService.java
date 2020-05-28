/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.smarther.internal.account;

import static org.openhab.binding.smarther.internal.SmartherBindingConstants.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smarther.internal.api.dto.Notification;
import org.openhab.binding.smarther.internal.api.exception.SmartherGatewayException;
import org.openhab.binding.smarther.internal.util.StringUtil;
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
 * The {@link SmartherAccountService} class to manage the servlets and bind authorization servlet to bridges.
 *
 * @author Fabio Possieri - Initial contribution
 */
@Component(service = SmartherAccountService.class, immediate = true, configurationPid = "binding.smarther.accountService")
@NonNullByDefault
public class SmartherAccountService {

    private static final String TEMPLATE_PATH = "templates/";
    private static final String IMAGE_PATH = "web";
    private static final String TEMPLATE_APPLICATION = TEMPLATE_PATH + "application.html";
    private static final String TEMPLATE_INDEX = TEMPLATE_PATH + "index.html";
    private static final String ERROR_UKNOWN_BRIDGE = "Returned 'state' by doesn't match any Bridges. Has the bridge been removed?";

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final List<SmartherAccountHandler> handlers = new ArrayList<>();

    private @NonNullByDefault({}) HttpService httpService;
    private @NonNullByDefault({}) BundleContext bundleContext;

    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
        try {
            bundleContext = componentContext.getBundleContext();

            // Register the authorization servlet
            httpService.registerServlet(AUTH_SERVLET_ALIAS, createAuthorizationServlet(), new Hashtable<>(),
                    httpService.createDefaultHttpContext());
            httpService.registerResources(AUTH_SERVLET_ALIAS + IMG_SERVLET_ALIAS, IMAGE_PATH, null);

            // Register the notification servlet
            httpService.registerServlet(NOTIFY_SERVLET_ALIAS, createNotificationServlet(), new Hashtable<>(),
                    httpService.createDefaultHttpContext());
        } catch (NamespaceException | ServletException | IOException e) {
            logger.warn("Error during Smarther servlet startup", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        // Unregister the authorization servlet
        httpService.unregister(AUTH_SERVLET_ALIAS);
        httpService.unregister(AUTH_SERVLET_ALIAS + IMG_SERVLET_ALIAS);

        // Unregister the notification servlet
        httpService.unregister(NOTIFY_SERVLET_ALIAS);
    }

    /**
     * Creates a new {@link SmartherAuthorizationServlet}.
     *
     * @return the newly created servlet
     * @throws IOException thrown when an HTML template could not be read
     */
    private HttpServlet createAuthorizationServlet() throws IOException {
        return new SmartherAuthorizationServlet(this, readTemplate(TEMPLATE_INDEX), readTemplate(TEMPLATE_APPLICATION));
    }

    /**
     * Creates a new {@link SmartherNotificationServlet}.
     *
     * @return the newly created servlet
     */
    private HttpServlet createNotificationServlet() {
        return new SmartherNotificationServlet(this);
    }

    /**
     * Reads a template from file and returns the content as String.
     *
     * @param templateName name of the template file to read
     * @return The content of the template file
     * @throws IOException thrown when an HTML template could not be read
     */
    private String readTemplate(String templateName) throws IOException {
        final URL index = bundleContext.getBundle().getEntry(templateName);

        if (index == null) {
            throw new FileNotFoundException(
                    String.format("Cannot find template '%s' - failed to initialize Smarther servlet", templateName));
        } else {
            try (InputStream input = index.openStream()) {
                return StringUtil.streamToString(input);
            }
        }
    }

    /**
     * Call with BTicino/Legrand API redirect uri returned State and Code values to get the refresh and access tokens
     * and persist these values
     *
     * @param servletBaseURL the servlet base, which will be the BTicino/Legrand API redirect url
     * @param state The BTicino/Legrand API returned state value
     * @param code The BTicino/Legrand API returned code value
     * @return returns the name of the BTicino/Legrand portal user that is authorized
     */
    public String authorize(String servletBaseURL, String state, String code) {
        // Searches the SmartherAccountHandler instance that matches the given state
        final SmartherAccountHandler listener = getAccountListenerByUID(state);

        if (listener == null) {
            logger.debug(
                    "BTicino/Legrand API redirected with state '{}' but no matching bridge was found. Possible bridge has been removed.",
                    state);
            throw new SmartherGatewayException(ERROR_UKNOWN_BRIDGE);
        }

        // Generates the notification URL from servletBaseURL
        final String notificationUrl = servletBaseURL.replace(AUTH_SERVLET_ALIAS, NOTIFY_SERVLET_ALIAS);

        logger.debug("Calling authorize on {}", listener.getUID());

        // Passes the authorization to the handler
        return listener.authorize(servletBaseURL, code, notificationUrl);
    }

    /**
     * Listener to BTicino/Legrand C2C notification service received a new notification to be handled
     *
     * @param notification The received notification
     */
    public void handleNotification(Notification notification) {
        // Searches the SmartherAccountHandler instance that matches the given location
        final SmartherAccountHandler listener = getAccountListenerByLocation(
                notification.getData().toChronothermostat().getSender().getPlant().getId());

        if (listener == null) {
            logger.warn("C2C notification [{}]: no matching bridge was found. Possible bridge has been removed.",
                    notification.getId());
            throw new SmartherGatewayException(ERROR_UKNOWN_BRIDGE);
        } else if (listener.isOnline()) {
            final SmartherNotificationHandler handler = (SmartherNotificationHandler) listener;

            if (handler.useNotifications()) {
                // Passes the notification to the handler
                handler.handleNotification(notification);
            } else {
                logger.debug("C2C notification [{}]: notification discarded as bridge does not handle notifications.",
                        notification.getId());
            }
        } else {
            logger.debug("C2C notification [{}]: notification discarded as bridge is offline.", notification.getId());
        }
    }

    /**
     * @param listener Adds the given handler
     */
    public void addSmartherAccountHandler(SmartherAccountHandler listener) {
        if (!handlers.contains(listener)) {
            handlers.add(listener);
        }
    }

    /**
     * @param handler Removes the given handler
     */
    public void removeSmartherAccountHandler(SmartherAccountHandler handler) {
        handlers.remove(handler);
    }

    /**
     * @return Returns all {@link SmartherAccountHandler}s.
     */
    public List<SmartherAccountHandler> getSmartherAccountHandlers() {
        return handlers;
    }

    /**
     * Searches the {@link SmartherAccountHandler} that matches the given thing UID
     *
     * @param thingUID UID of the thing to match the handler with
     * @return the {@link SmartherAccountHandler} matching the given thing UID or null
     */
    private @Nullable SmartherAccountHandler getAccountListenerByUID(String thingUID) {
        final Optional<SmartherAccountHandler> maybeListener = handlers.stream().filter(l -> l.equalsThingUID(thingUID))
                .findFirst();
        return (maybeListener.isPresent()) ? maybeListener.get() : null;
    }

    /**
     * Searches the {@link SmartherAccountHandler} that matches the given location ID
     *
     * @param plantId Id of the location to match the handler with
     * @return the {@link SmartherAccountHandler} matching the given location or null
     */
    private @Nullable SmartherAccountHandler getAccountListenerByLocation(String plantId) {
        final Optional<SmartherAccountHandler> maybeListener = handlers.stream().filter(l -> l.hasLocation(plantId))
                .findFirst();
        return (maybeListener.isPresent()) ? maybeListener.get() : null;
    }

    @Reference
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }

}
