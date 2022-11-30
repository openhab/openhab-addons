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
package org.openhab.binding.webexteams.internal;

import static org.openhab.binding.webexteams.internal.WebexTeamsBindingConstants.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * The {@link WebexAuthService} class to manage the servlets and bind authorization servlet to bridges.
 * 
 * @author Tom Deckers - Initial contribution
 */
@Component(service = WebexAuthService.class, configurationPid = "binding.webexteams.authService")
@NonNullByDefault
public class WebexAuthService {

    private static final String TEMPLATE_PATH = "templates/";
    private static final String TEMPLATE_ACCOUNT = TEMPLATE_PATH + "account.html";
    private static final String TEMPLATE_INDEX = TEMPLATE_PATH + "index.html";

    private final Logger logger = LoggerFactory.getLogger(WebexAuthService.class);

    private final List<WebexTeamsHandler> handlers = Collections.synchronizedList(new ArrayList<>());

    private static final String ERROR_UKNOWN_BRIDGE = "Returned 'state' by oauth redirect doesn't match any accounts. Has the account been removed?";

    private @NonNullByDefault({}) HttpService httpService;
    private @NonNullByDefault({}) BundleContext bundleContext;

    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
        logger.debug("Activating WebexAuthService");
        try {
            bundleContext = componentContext.getBundleContext();
            httpService.registerServlet(WEBEX_ALIAS, createServlet(), new Hashtable<>(),
                    httpService.createDefaultHttpContext());
            httpService.registerResources(WEBEX_ALIAS + WEBEX_RES_ALIAS, "web", null);
        } catch (NamespaceException | ServletException | IOException e) {
            logger.warn("Error during webex auth servlet startup", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        logger.debug("Deactivating WebexAuthService");
        httpService.unregister(WEBEX_ALIAS);
        httpService.unregister(WEBEX_ALIAS + WEBEX_RES_ALIAS);
    }

    /**
     * Creates a new {@link WebexAuthServlet}.
     *
     * @return the newly created servlet
     * @throws IOException thrown when an HTML template could not be read
     */
    private HttpServlet createServlet() throws IOException {
        return new WebexAuthServlet(this, readTemplate(TEMPLATE_INDEX), readTemplate(TEMPLATE_ACCOUNT));
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
                    String.format("Cannot find '{}' - failed to initialize Webex servlet", templateName));
        } else {
            try (InputStream inputStream = index.openStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }

    /**
     * Call with Webex redirect uri returned State and Code values to get the refresh and access tokens and persist
     * these values
     *
     * @param servletBaseURL the servlet base, which will be the Webex redirect url
     * @param state The Webex returned state value
     * @param code The Webex returned code value
     * @return returns the name of the Webex user that is authorized
     * @throws WebexTeamsException if no handler was found for the state
     */
    public String authorize(String servletBaseURL, String state, String code) throws WebexTeamsException {
        logger.debug("Authorizing for state: {}, code: {}", state, code);

        final WebexTeamsHandler listener = getWebexTeamsHandler(state);

        if (listener == null) {
            logger.debug(
                    "Webex redirected with state '{}' but no matching account was found. Possible account has been removed.",
                    state);
            throw new WebexTeamsException(ERROR_UKNOWN_BRIDGE);
        } else {
            return listener.authorize(servletBaseURL, code);
        }
    }

    /**
     * @param listener Adds the given handler
     */
    public void addWebexTeamsHandler(WebexTeamsHandler listener) {
        if (!handlers.contains(listener)) {
            handlers.add(listener);
        }
    }

    /**
     * @param handler Removes the given handler
     */
    public void removeWebexTeamsHandler(WebexTeamsHandler handler) {
        handlers.remove(handler);
    }

    /**
     * @return Returns all {@link WebexTeamsHandler}s.
     */
    public List<WebexTeamsHandler> getWebexTeamsHandlers() {
        return handlers;
    }

    /**
     * Get the {@link WebexTeamsHandler} that matches the given thing UID.
     *
     * @param thingUID UID of the thing to match the handler with
     * @return the {@link WebexTeamsHandler} matching the thing UID or null
     */
    private @Nullable WebexTeamsHandler getWebexTeamsHandler(String thingUID) {
        final Optional<WebexTeamsHandler> maybeListener = handlers.stream().filter(l -> l.equalsThingUID(thingUID))
                .findFirst();
        return maybeListener.isPresent() ? maybeListener.get() : null;
    }

    @Reference
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }
}
