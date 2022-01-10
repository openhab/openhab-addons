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
package org.openhab.binding.spotify.internal;

import static org.openhab.binding.spotify.internal.SpotifyBindingConstants.*;

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
import org.openhab.binding.spotify.internal.api.exception.SpotifyException;
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
 * The {@link SpotifyAuthService} class to manage the servlets and bind authorization servlet to bridges.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Hilbrand Bouwkamp - Made this the service class instead of only interface. Added templates
 */
@Component(service = SpotifyAuthService.class, configurationPid = "binding.spotify.authService")
@NonNullByDefault
public class SpotifyAuthService {

    private static final String TEMPLATE_PATH = "templates/";
    private static final String TEMPLATE_PLAYER = TEMPLATE_PATH + "player.html";
    private static final String TEMPLATE_INDEX = TEMPLATE_PATH + "index.html";
    private static final String ERROR_UKNOWN_BRIDGE = "Returned 'state' by doesn't match any Bridges. Has the bridge been removed?";

    private final Logger logger = LoggerFactory.getLogger(SpotifyAuthService.class);

    private final List<SpotifyAccountHandler> handlers = new ArrayList<>();

    private @NonNullByDefault({}) HttpService httpService;
    private @NonNullByDefault({}) BundleContext bundleContext;

    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
        try {
            bundleContext = componentContext.getBundleContext();
            httpService.registerServlet(SPOTIFY_ALIAS, createServlet(), new Hashtable<>(),
                    httpService.createDefaultHttpContext());
            httpService.registerResources(SPOTIFY_ALIAS + SPOTIFY_IMG_ALIAS, "web", null);
        } catch (NamespaceException | ServletException | IOException e) {
            logger.warn("Error during spotify servlet startup", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        httpService.unregister(SPOTIFY_ALIAS);
        httpService.unregister(SPOTIFY_ALIAS + SPOTIFY_IMG_ALIAS);
    }

    /**
     * Creates a new {@link SpotifyAuthServlet}.
     *
     * @return the newly created servlet
     * @throws IOException thrown when an HTML template could not be read
     */
    private HttpServlet createServlet() throws IOException {
        return new SpotifyAuthServlet(this, readTemplate(TEMPLATE_INDEX), readTemplate(TEMPLATE_PLAYER));
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
                    String.format("Cannot find '{}' - failed to initialize Spotify servlet", templateName));
        } else {
            try (InputStream inputStream = index.openStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }

    /**
     * Call with Spotify redirect uri returned State and Code values to get the refresh and access tokens and persist
     * these values
     *
     * @param servletBaseURL the servlet base, which will be the Spotify redirect url
     * @param state The Spotify returned state value
     * @param code The Spotify returned code value
     * @return returns the name of the Spotify user that is authorized
     */
    public String authorize(String servletBaseURL, String state, String code) {
        final SpotifyAccountHandler listener = getSpotifyAuthListener(state);

        if (listener == null) {
            logger.debug(
                    "Spotify redirected with state '{}' but no matching bridge was found. Possible bridge has been removed.",
                    state);
            throw new SpotifyException(ERROR_UKNOWN_BRIDGE);
        } else {
            return listener.authorize(servletBaseURL, code);
        }
    }

    /**
     * @param listener Adds the given handler
     */
    public void addSpotifyAccountHandler(SpotifyAccountHandler listener) {
        if (!handlers.contains(listener)) {
            handlers.add(listener);
        }
    }

    /**
     * @param handler Removes the given handler
     */
    public void removeSpotifyAccountHandler(SpotifyAccountHandler handler) {
        handlers.remove(handler);
    }

    /**
     * @return Returns all {@link SpotifyAccountHandler}s.
     */
    public List<SpotifyAccountHandler> getSpotifyAccountHandlers() {
        return handlers;
    }

    /**
     * Get the {@link SpotifyAccountHandler} that matches the given thing UID.
     *
     * @param thingUID UID of the thing to match the handler with
     * @return the {@link SpotifyAccountHandler} matching the thing UID or null
     */
    private @Nullable SpotifyAccountHandler getSpotifyAuthListener(String thingUID) {
        final Optional<SpotifyAccountHandler> maybeListener = handlers.stream().filter(l -> l.equalsThingUID(thingUID))
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
