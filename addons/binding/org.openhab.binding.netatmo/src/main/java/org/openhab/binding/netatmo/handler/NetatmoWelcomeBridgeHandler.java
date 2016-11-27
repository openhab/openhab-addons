/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.handler;

import org.eclipse.smarthome.core.thing.Bridge;
import org.openhab.binding.netatmo.config.NetatmoWelcomeBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.client.api.WelcomeApi;
import io.swagger.client.model.NAWelcomeHomeData;

/**
 * {@link NetatmoWelcomeBridgeHandler} is the handler for a Netatmo Welcome camera and connects it
 * to the framework. The devices and modules uses the
 * {@link NetatmoWelcomeBridgeHandler} to request informations about their status
 *
 * @author Ing. Peter Weiss - Welcome camera implementation
 *
 */
public class NetatmoWelcomeBridgeHandler<X extends NetatmoWelcomeBridgeConfiguration> extends NetatmoBridgeHandler<X> {
    private static Logger logger = LoggerFactory.getLogger(NetatmoWelcomeBridgeHandler.class);
    private WelcomeApi welcomeApi = null;

    public NetatmoWelcomeBridgeHandler(Bridge bridge, Class<X> configurationClass) {
        super(bridge, configurationClass);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Netatmo Welcome bridge handler.");
        super.initialize();
    }

    private WelcomeApi getWelcomeApi() {
        // if (configuration.readWelcome && welcomeApi == null) {
        if (welcomeApi == null) {
            welcomeApi = apiClient.createService(WelcomeApi.class);
        }
        return welcomeApi;
    }

    public NAWelcomeHomeData getWelcomeDataBody(String homeId) {
        if (getWelcomeApi() != null) {
            try {
                return getWelcomeApi().gethomedata(homeId, null).getBody();
            } catch (Exception e) {
                logger.warn("An error occured while calling welcome API : {}", e.getMessage());
            }
        }
        return null;
    }

    public Integer getWelcomeEventThings() {
        return configuration.welcomeEventThings;
    }

    public Integer getWelcomeUnknownPersonThings() {
        return configuration.welcomeUnknownPersonThings;
    }
}
