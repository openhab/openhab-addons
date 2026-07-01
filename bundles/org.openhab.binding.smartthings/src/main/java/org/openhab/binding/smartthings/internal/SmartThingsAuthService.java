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
import java.util.concurrent.ConcurrentHashMap;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.core.thing.ThingUID;
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
    private final Logger logger = LoggerFactory.getLogger(SmartThingsAuthService.class);

    private final Map<ThingUID, SmartThingsOAuthHandler> oAuthHandlers = new ConcurrentHashMap<>();

    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
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
     * @return returns the name of the SmartThings user that is authorized
     */
    public String authorize(ThingUID bridgeUID, String redirectUri, String state, String code)
            throws SmartThingsException {
        SmartThingsOAuthHandler oAuthHandler = getSmartThingsOAuthHandler(bridgeUID);
        if (oAuthHandler == null) {
            logger.debug(
                    "SmartThings redirected for bridge '{}' with state '{}' but no matching bridge was found. Possible bridge has been removed.",
                    bridgeUID, state);
            throw new SmartThingsException("@text/error-unknow-bridge");
        } else {
            return oAuthHandler.authorize(redirectUri, code);
        }
    }

    /**
     * @param listener Adds the given handler
     */
    public void setSmartThingsOAuthHandler(ThingUID bridgeUID, SmartThingsOAuthHandler oAuthHandler) {
        oAuthHandlers.put(bridgeUID, oAuthHandler);
    }

    /**
     * @param handler Removes the given handler
     */
    public void unsetSmartThingsOAuthHandler(ThingUID bridgeUID, @NotNull SmartThingsOAuthHandler oAuthHandler) {
        oAuthHandlers.remove(bridgeUID, oAuthHandler);
    }

    /**
     * @param listener Adds the given handler
     */
    public @Nullable SmartThingsOAuthHandler getSmartThingsOAuthHandler(ThingUID bridgeUID) {
        return oAuthHandlers.get(bridgeUID);
    }
}
