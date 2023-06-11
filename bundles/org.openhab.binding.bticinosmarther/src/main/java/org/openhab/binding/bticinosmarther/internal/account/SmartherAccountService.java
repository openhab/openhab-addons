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
package org.openhab.binding.bticinosmarther.internal.account;

import static org.openhab.binding.bticinosmarther.internal.SmartherBindingConstants.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bticinosmarther.internal.api.dto.Notification;
import org.openhab.binding.bticinosmarther.internal.api.dto.Sender;
import org.openhab.binding.bticinosmarther.internal.api.exception.SmartherGatewayException;
import org.openhab.binding.bticinosmarther.internal.util.StringUtil;
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
@Component(service = SmartherAccountService.class, configurationPid = "binding.bticinosmarther.accountService")
@NonNullByDefault
public class SmartherAccountService {

    private static final String TEMPLATE_PATH = "templates/";
    private static final String IMAGE_PATH = "web";
    private static final String TEMPLATE_APPLICATION = TEMPLATE_PATH + "application.html";
    private static final String TEMPLATE_INDEX = TEMPLATE_PATH + "index.html";
    private static final String ERROR_UKNOWN_BRIDGE = "Returned 'state' doesn't match any Bridges. Has the bridge been removed?";

    private final Logger logger = LoggerFactory.getLogger(SmartherAccountService.class);

    private final Set<SmartherAccountHandler> handlers = ConcurrentHashMap.newKeySet();

    private @Nullable HttpService httpService;
    private @Nullable BundleContext bundleContext;

    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
        try {
            this.bundleContext = componentContext.getBundleContext();

            final HttpService localHttpService = this.httpService;
            if (localHttpService != null) {
                // Register the authorization servlet
                localHttpService.registerServlet(AUTH_SERVLET_ALIAS, createAuthorizationServlet(), new Hashtable<>(),
                        localHttpService.createDefaultHttpContext());
                localHttpService.registerResources(AUTH_SERVLET_ALIAS + IMG_SERVLET_ALIAS, IMAGE_PATH, null);

                // Register the notification servlet
                localHttpService.registerServlet(NOTIFY_SERVLET_ALIAS, createNotificationServlet(), new Hashtable<>(),
                        localHttpService.createDefaultHttpContext());
            }
        } catch (NamespaceException | ServletException | IOException e) {
            logger.warn("Error during Smarther servlet startup", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        final HttpService localHttpService = this.httpService;
        if (localHttpService != null) {
            // Unregister the authorization servlet
            localHttpService.unregister(AUTH_SERVLET_ALIAS);
            localHttpService.unregister(AUTH_SERVLET_ALIAS + IMG_SERVLET_ALIAS);

            // Unregister the notification servlet
            localHttpService.unregister(NOTIFY_SERVLET_ALIAS);
        }
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
        final BundleContext localBundleContext = this.bundleContext;
        if (localBundleContext != null) {
            final URL index = localBundleContext.getBundle().getEntry(templateName);

            if (index == null) {
                throw new FileNotFoundException(String
                        .format("Cannot find template '%s' - failed to initialize Smarther servlet", templateName));
            } else {
                try (InputStream input = index.openStream()) {
                    return StringUtil.streamToString(input);
                }
            }
        } else {
            throw new IOException("Cannot get template, bundle context is null");
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
        final SmartherAccountHandler accountHandler = getAccountHandlerByUID(state);
        if (accountHandler != null) {
            // Generates the notification URL from servletBaseURL
            final String notificationUrl = servletBaseURL.replace(AUTH_SERVLET_ALIAS, NOTIFY_SERVLET_ALIAS);

            logger.debug("API authorization: calling authorize on {}", accountHandler.getUID());

            // Passes the authorization to the handler
            return accountHandler.authorize(servletBaseURL, code, notificationUrl);
        } else {
            logger.trace("API authorization: request redirected with state '{}'", state);
            logger.warn("API authorization: no matching bridge was found. Possible bridge has been removed.");
            throw new SmartherGatewayException(ERROR_UKNOWN_BRIDGE);
        }
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
        final Sender sender = notification.getSender();
        if (sender != null) {
            // Searches the SmartherAccountHandler instance that matches the given location
            final SmartherAccountHandler accountHandler = getAccountHandlerByLocation(sender.getPlant().getId());
            if (accountHandler == null) {
                logger.warn("C2C notification [{}]: no matching bridge was found. Possible bridge has been removed.",
                        notification.getId());
                throw new SmartherGatewayException(ERROR_UKNOWN_BRIDGE);
            } else if (accountHandler.isOnline()) {
                final SmartherNotificationHandler notificationHandler = (SmartherNotificationHandler) accountHandler;

                if (notificationHandler.useNotifications()) {
                    // Passes the notification to the handler
                    notificationHandler.handleNotification(notification);
                } else {
                    logger.debug(
                            "C2C notification [{}]: notification discarded as bridge does not handle notifications.",
                            notification.getId());
                }
            } else {
                logger.debug("C2C notification [{}]: notification discarded as bridge is offline.",
                        notification.getId());
            }
        } else {
            logger.debug("C2C notification [{}]: notification discarded as payload is invalid.", notification.getId());
        }
    }

    /**
     * Adds a {@link SmartherAccountHandler} handler to the set of account service handlers.
     *
     * @param handler
     *            the handler to add to the handlers set
     */
    public void addSmartherAccountHandler(SmartherAccountHandler handler) {
        handlers.add(handler);
    }

    /**
     * Removes a {@link SmartherAccountHandler} handler from the set of account service handlers.
     *
     * @param handler
     *            the handler to remove from the handlers set
     */
    public void removeSmartherAccountHandler(SmartherAccountHandler handler) {
        handlers.remove(handler);
    }

    /**
     * Returns all the {@link SmartherAccountHandler} account service handlers.
     *
     * @return a set containing all the account service handlers
     */
    public Set<SmartherAccountHandler> getSmartherAccountHandlers() {
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
    private @Nullable SmartherAccountHandler getAccountHandlerByUID(String thingUID) {
        final Optional<SmartherAccountHandler> maybeHandler = handlers.stream().filter(l -> l.equalsThingUID(thingUID))
                .findFirst();
        return (maybeHandler.isPresent()) ? maybeHandler.get() : null;
    }

    /**
     * Searches the {@link SmartherAccountHandler} handler that matches the given location plant.
     *
     * @param plantId
     *            the identifier of the plant to match the handler with
     *
     * @return the handler matching the given location plant, or {@code null} if none matches
     */
    private @Nullable SmartherAccountHandler getAccountHandlerByLocation(String plantId) {
        final Optional<SmartherAccountHandler> maybeHandler = handlers.stream().filter(l -> l.hasLocation(plantId))
                .findFirst();
        return (maybeHandler.isPresent()) ? maybeHandler.get() : null;
    }

    @Reference
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }
}
