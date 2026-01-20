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
package org.openhab.binding.tidal.internal;

import static org.openhab.binding.tidal.internal.TidalBindingConstants.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.tidal.internal.api.exception.TidalException;
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
 * The {@link TidalAuthService} class to manage the servlets and bind authorization servlet to bridges.
 *
 * @author Laurent Arnal - Initial contribution
 */
@Component(service = TidalAuthService.class, configurationPid = "binding.tidal.authService")
@NonNullByDefault
public class TidalAuthService {

    private static final String TEMPLATE_PATH = "templates/";
    private static final String TEMPLATE_PLAYER = TEMPLATE_PATH + "player.html";
    private static final String TEMPLATE_INDEX = TEMPLATE_PATH + "index.html";
    private static final String ERROR_UKNOWN_BRIDGE = "Returned 'state' by doesn't match any Bridges. Has the bridge been removed?";

    private final Logger logger = LoggerFactory.getLogger(TidalAuthService.class);

    private final List<TidalAccountHandler> handlers = new ArrayList<>();

    private @NonNullByDefault({}) HttpService httpService;
    private @NonNullByDefault({}) BundleContext bundleContext;

    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
        try {
            bundleContext = componentContext.getBundleContext();
            httpService.registerServlet(TIDAL_ALIAS, createServlet(), new Hashtable<>(),
                    httpService.createDefaultHttpContext());
            httpService.registerResources(TIDAL_ALIAS + TIDAL_IMG_ALIAS, "web", null);
        } catch (NamespaceException | ServletException | IOException e) {
            logger.warn("Error during tidal servlet startup", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        httpService.unregister(TIDAL_ALIAS);
        httpService.unregister(TIDAL_ALIAS + TIDAL_IMG_ALIAS);
    }

    /**
     * Creates a new {@link TidalAuthServlet}.
     *
     * @return the newly created servlet
     * @throws IOException thrown when an HTML template could not be read
     */
    private HttpServlet createServlet() throws IOException {
        return new TidalAuthServlet(this, readTemplate(TEMPLATE_INDEX));
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
                    String.format("Cannot find '{}' - failed to initialize Tidal servlet", templateName));
        } else {
            try (InputStream inputStream = index.openStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }

    /**
     * Call with Tidal redirect uri returned State and Code values to get the refresh and access tokens and persist
     * these values
     *
     * @param servletBaseURL the servlet base, which will be the Tidal redirect url
     * @param state The Tidal returned state value
     * @param code The Tidal returned code value
     * @return returns the name of the Tidal user that is authorized
     */
    public String authorize(String redirectUri, String state, String code) {
        final TidalAccountHandler listener = getTidalAuthListener(state);

        if (listener == null) {
            logger.debug(
                    "Tidal redirected with state '{}' but no matching bridge was found. Possible bridge has been removed.",
                    state);
            throw new TidalException(ERROR_UKNOWN_BRIDGE);
        } else {
            return listener.authorize(redirectUri, code);
        }
    }

    /**
     * @param listener Adds the given handler
     */
    public void addTidalAccountHandler(TidalAccountHandler listener) {
        if (!handlers.contains(listener)) {
            handlers.add(listener);
        }
    }

    /**
     * @param handler Removes the given handler
     */
    public void removeTidalAccountHandler(TidalAccountHandler handler) {
        handlers.remove(handler);
    }

    /**
     * @return Returns all {@link TidalAccountHandler}s.
     */
    public List<TidalAccountHandler> getTidalAccountHandlers() {
        return handlers;
    }

    /**
     * Get the {@link TidalAccountHandler} that matches the given thing UID.
     *
     * @param thingUID UID of the thing to match the handler with
     * @return the {@link TidalAccountHandler} matching the thing UID or null
     */
    private @Nullable TidalAccountHandler getTidalAuthListener(String thingUID) {
        final Optional<TidalAccountHandler> maybeListener = handlers.stream().filter(l -> l.equalsThingUID(thingUID))
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
