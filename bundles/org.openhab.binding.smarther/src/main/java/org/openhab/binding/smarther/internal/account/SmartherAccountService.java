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
 * The {@code SmartherAccountService} class manages the servlets and bind authorization servlet to Bridges.
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
    private static final String ERROR_UKNOWN_BRIDGE = "Returned 'state' doesn't match any Bridges. Has the bridge been removed?";

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final List<SmartherAccountHandler> handlers = new ArrayList<SmartherAccountHandler>();

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
     * Constructs a {@code SmartherAuthorizationServlet}.
     *
     * @return the newly created servlet
     *
     * @throws {@link IOException}
     *             in case of issues reading one of the internal html templates
     */
    private HttpServlet createAuthorizationServlet() throws IOException {
        return new SmartherAuthorizationServlet(this, readTemplate(TEMPLATE_INDEX), readTemplate(TEMPLATE_APPLICATION));
    }

    /**
     * Constructs a {@code SmartherNotificationServlet}.
     *
     * @return the newly created servlet
     */
    private HttpServlet createNotificationServlet() {
        return new SmartherNotificationServlet(this);
    }

    /**
     * Reads a template from file and returns its content as string.
     *
     * @param templateName
     *            the name of the template file to read
     *
     * @return a string representing the content of the template file
     *
     * @throws {@link IOException}
     *             in case of issues reading the template from file
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
     * Dispatches the received Smarther API authorization response to the proper Smarther account handler.
     * Part of the Legrand/Bticino OAuth2 authorization process.
     *
     * @param servletBaseURL
     *            the authorization servlet url needed to derive the notification endpoint url
     * @param state
     *            the authorization state needed to match the correct Smarther account handler to authorize
     * @param code The BTicino/Legrand API returned code value
     *            the authorization code to authorize with the account handler
     *
     * @return a string containing the name of the authorized BTicino/Legrand portal user
     *
     * @throws {@link SmartherGatewayException}
     *             in case of communication issues with the Smarther API or no account handler found
     */
    public String dispatchAuthorization(String servletBaseURL, String state, String code)
            throws SmartherGatewayException {
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
     * Dispatches the received C2C Webhook notification to the proper Smarther notification handler.
     *
     * @param notification
     *            the received notification to handle
     *
     * @throws {@link SmartherGatewayException}
     *             in case of communication issues with the Smarther API or no notification handler found
     */
    public void dispatchNotification(Notification notification) throws SmartherGatewayException {
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
     * Adds a {@link SmartherAccountHandler} handler to the account service handlers list.
     *
     * @param handler
     *            the handler to add to the handlers list
     */
    public void addSmartherAccountHandler(SmartherAccountHandler handler) {
        if (!handlers.contains(handler)) {
            handlers.add(handler);
        }
    }

    /**
     * Removes a {@link SmartherAccountHandler} handler from the account service handlers list.
     *
     * @param handler
     *            the handler to remove from the handlers list
     */
    public void removeSmartherAccountHandler(SmartherAccountHandler handler) {
        handlers.remove(handler);
    }

    /**
     * Returns all the account service {@link SmartherAccountHandler} handlers list.
     *
     * @return the account service handlers list
     */
    public List<SmartherAccountHandler> getSmartherAccountHandlers() {
        return handlers;
    }

    /**
     * Searches the {@link SmartherAccountHandler} handler that matches the given Thing UID.
     *
     * @param thingUID
     *            the UID of the Thing to match the handler with
     *
     * @return the handler matching the given Thing UID, or {@code null} if none matches
     */
    private @Nullable SmartherAccountHandler getAccountListenerByUID(String thingUID) {
        final Optional<SmartherAccountHandler> maybeListener = handlers.stream().filter(l -> l.equalsThingUID(thingUID))
                .findFirst();
        return (maybeListener.isPresent()) ? maybeListener.get() : null;
    }

    /**
     * Searches the {@link SmartherAccountHandler} handler that matches the given location plant.
     *
     * @param plantId
     *            the identifier of the plant to match the handler with
     *
     * @return the handler matching the given location plant, or {@code null} if none matches
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
