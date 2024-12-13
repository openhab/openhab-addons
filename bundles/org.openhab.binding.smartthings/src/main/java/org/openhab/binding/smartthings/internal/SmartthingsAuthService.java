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
package org.openhab.binding.smartthings.internal;

import java.util.Hashtable;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.api.SmartthingsNetworkConnector;
import org.openhab.binding.smartthings.internal.handler.SmartthingsBridgeHandler;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SpotifyAuthService} class to manage the servlets and bind authorization servlet to bridges.
 *
 * @author Laurent Arnal - Initial contribution
 */
@Component(service = SmartthingsAuthService.class, configurationPid = "binding.internal.authService")
@NonNullByDefault
public class SmartthingsAuthService {

    private static final String ERROR_UKNOWN_BRIDGE = "Returned 'state' by doesn't match any Bridges. Has the bridge been removed?";

    private final Logger logger = LoggerFactory.getLogger(SmartthingsAuthService.class);

    // private final List<SpotifyAccountHandler> handlers = new ArrayList<>();

    private @NonNullByDefault({}) HttpService httpService;
    private @Nullable SmartthingsAccountHandler accountHandler;

    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
    }

    protected void initialize() {
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        httpService.unregister(SmartthingsBindingConstants.SMARTTHINGS_ALIAS);
        httpService.unregister(
                SmartthingsBindingConstants.SMARTTHINGS_ALIAS + SmartthingsBindingConstants.SMARTTHINGS_IMG_ALIAS);
    }

    /**
     * Creates a new {@link SpotifyAuthServlet}.
     *
     * @return the newly created servlet
     */

    public void registerServlet() {
        try {
            httpService.registerServlet(SmartthingsBindingConstants.SMARTTHINGS_ALIAS, createServlet(),
                    new Hashtable<>(), httpService.createDefaultHttpContext());
            httpService.registerResources(
                    SmartthingsBindingConstants.SMARTTHINGS_ALIAS + SmartthingsBindingConstants.SMARTTHINGS_IMG_ALIAS,
                    "web", null);
        } catch (Exception e) {
            logger.warn("Error during spotify servlet startup", e);
        }
    }

    private HttpServlet createServlet() throws Exception {
        SmartthingsBridgeHandler bridgeHandler = (SmartthingsBridgeHandler) accountHandler;
        if (bridgeHandler == null) {
            throw new Exception("BridgeHandler is null");
        }

        SmartthingsNetworkConnector networkConnector = bridgeHandler.getNetworkConnector();
        return new SmartthingsAuthServlet(bridgeHandler, this, httpService, networkConnector, "");
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
        SmartthingsAccountHandler accountHandler = getSmartthingsAccountHandler();
        if (accountHandler == null) {
            logger.debug(
                    "Smartthings redirected with state '{}' but no matching bridge was found. Possible bridge has been removed.",
                    state);
            throw new RuntimeException(ERROR_UKNOWN_BRIDGE);
        } else {
            return accountHandler.authorize(servletBaseURL, code);
        }
    }

    /**
     * @param listener Adds the given handler
     */
    public void setSmartthingsAccountHandler(@Nullable SmartthingsAccountHandler accountHandler) {
        this.accountHandler = accountHandler;
    }

    /**
     * @param handler Removes the given handler
     */
    public void unsetSmartthingsAccountHandler(@NotNull SmartthingsAccountHandler accountHandler) {
        this.accountHandler = null;
    }

    /**
     * @param listener Adds the given handler
     */
    public @Nullable SmartthingsAccountHandler getSmartthingsAccountHandler() {
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
