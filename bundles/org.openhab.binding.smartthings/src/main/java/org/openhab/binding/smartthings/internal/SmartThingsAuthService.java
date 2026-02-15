/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartThingsAuthService} class to manage the servlets and bind authorization servlet to bridges.
 *
 * @author Laurent Arnal - Initial contribution
 */
@Component(service = SmartThingsAuthService.class, configurationPid = "binding.internal.authService")
@NonNullByDefault
public class SmartThingsAuthService {

    private static final String ERROR_UKNOWN_BRIDGE = "Returned 'state' by doesn't match any Bridges. Has the bridge been removed?";

    private final Logger logger = LoggerFactory.getLogger(SmartThingsAuthService.class);

    private @Nullable SmartThingsAccountHandler accountHandler;

    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
    }

    protected void initialize() {
    }

    /**
     * Call with SmartThings redirect uri returned State and Code values to get the refresh and access tokens and
     * persist
     * these values
     *
     * @param redirectUri the redirectUri use in oauth call
     * @param state The state use in oauth call
     * @param code The smartthings return authorization code
     *
     * @return returns""
     */
    public String authorize(String redirectUri, String state, String code) throws SmartThingsException {
        SmartThingsAccountHandler accountHandler = getSmartThingsAccountHandler();
        if (accountHandler == null) {
            logger.debug(
                    "SmartThings redirected with state '{}' but no matching bridge was found. Possible bridge has been removed.",
                    state);
            throw new SmartThingsException(ERROR_UKNOWN_BRIDGE);
        } else {
            return accountHandler.authorize(redirectUri, code);
        }
    }

    /**
     * @param listener Adds the given handler
     */
    public void setSmartThingsAccountHandler(@Nullable SmartThingsAccountHandler accountHandler) {
        this.accountHandler = accountHandler;
    }

    /**
     * @param handler Removes the given handler
     */
    public void unsetSmartThingsAccountHandler(@NotNull SmartThingsAccountHandler accountHandler) {
        this.accountHandler = null;
    }

    /**
     * @param listener Adds the given handler
     */
    public @Nullable SmartThingsAccountHandler getSmartThingsAccountHandler() {
        return this.accountHandler;
    }
}
