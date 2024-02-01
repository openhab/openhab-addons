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
package org.openhab.binding.linky.internal;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.Map;

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
 *
 * @author Gaël L'hopital - Initial contribution *
 * @author Laurent Arnal - Rewrite addon to use official dataconect API
 */
@Component(service = LinkyAuthService.class, configurationPid = "binding.internal.authService")
@NonNullByDefault
public class LinkyAuthService {

    private static final String TEMPLATE_PATH = "templates/";
    private static final String TEMPLATE_INDEX = TEMPLATE_PATH + "index.html";
    private static final String ERROR_UKNOWN_BRIDGE = "Returned 'state' by doesn't match any Bridges. Has the bridge been removed?";

    private final Logger logger = LoggerFactory.getLogger(LinkyAuthService.class);

    private @NonNullByDefault({}) HttpService httpService;
    private @NonNullByDefault({}) BundleContext bundleContext;
    private @Nullable LinkyAccountHandler accountHandler;

    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
        try {
            bundleContext = componentContext.getBundleContext();
            httpService.registerServlet(LinkyBindingConstants.LINKY_ALIAS, createServlet(), new Hashtable<>(),
                    httpService.createDefaultHttpContext());
            httpService.registerResources(LinkyBindingConstants.LINKY_ALIAS + LinkyBindingConstants.LINKY_IMG_ALIAS,
                    "web", null);
        } catch (NamespaceException | ServletException | IOException e) {
            logger.warn("Error during linky servlet startup", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        httpService.unregister(LinkyBindingConstants.LINKY_ALIAS);
        httpService.unregister(LinkyBindingConstants.LINKY_ALIAS + LinkyBindingConstants.LINKY_IMG_ALIAS);
    }

    /**
     * Creates a new {@link LinkyAuthServlet}.
     *
     * @return the newly created servlet
     * @throws IOException thrown when an HTML template could not be read
     */
    private HttpServlet createServlet() throws IOException {
        return new LinkyAuthServlet(this, readTemplate(TEMPLATE_INDEX));
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
                    String.format("Cannot find '{}' - failed to initialize Linky servlet", templateName));
        } else {
            try (InputStream inputStream = index.openStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }

    /**
     * Call with Linky redirect uri returned State and Code values to get the refresh and access tokens and persist
     * these values
     *
     * @param servletBaseURL the servlet base, which will be the Linky redirect url
     * @param state The Linky returned state value
     * @param code The Linky returned code value
     * @return returns the name of the Linky user that is authorized
     */
    public String authorize(String servletBaseURL, String state, String code) {
        LinkyAccountHandler accountHandler = getLinkyAccountHandler();
        if (accountHandler == null) {
            logger.debug(
                    "Linky redirected with state '{}' but no matching bridge was found. Possible bridge has been removed.",
                    state);
            throw new RuntimeException(ERROR_UKNOWN_BRIDGE);
        } else {
            return accountHandler.authorize(servletBaseURL, code);
        }
    }

    /**
     * @param listener Adds the given handler
     */
    public void setLinkyAccountHandler(LinkyAccountHandler accountHandler) {
        this.accountHandler = accountHandler;
    }

    /**
     * @param handler Removes the given handler
     */
    public void unsetLinkyAccountHandler(LinkyAccountHandler accountHandler) {
        this.accountHandler = null;
    }

    /**
     * @param listener Adds the given handler
     */
    public @Nullable LinkyAccountHandler getLinkyAccountHandler() {
        return this.accountHandler;
    }

    @Reference
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }
}
