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
package org.openhab.binding.smartthings.internal.handler;

import java.util.Collection;
import java.util.LinkedList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smartthings.internal.SmartthingsHandlerFactory;
import org.openhab.binding.smartthings.internal.api.SmartthingsApi;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.status.ConfigStatusMessage;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartthingsBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsCloudBridgeHandler extends SmartthingsBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(SmartthingsCloudBridgeHandler.class);

    public SmartthingsCloudBridgeHandler(Bridge bridge, SmartthingsHandlerFactory smartthingsHandlerFactory,
            BundleContext bundleContext, HttpService httpService, OAuthFactory oAuthFactory,
            HttpClientFactory httpClientFactory) {
        super(bridge, smartthingsHandlerFactory, bundleContext, httpService, oAuthFactory, httpClientFactory);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Commands are handled by the "Things"
    }

    @Override
    public void initialize() {
        super.initialize();

        SmartthingsApi api = this.getSmartthingsApi();

        // AppResponse appResponse = api.CreateApp();
        // config.clientId = appResponse.oauthClientId;
        // config.clientSecret = appResponse.oauthClientSecret;

        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    protected boolean validateConfig(SmartthingsBridgeConfig config) {
        if (!super.validateConfig(config)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unknow");
            return false;
        }

        if (config.clientId.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Smartthings Client Id is not specified");
            return false;
        }

        if (config.clientSecret.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Smartthings Client Secret is not specified");
            return false;
        }

        return true;
    }

    @Override
    public SmartthingsHandlerFactory getSmartthingsHandlerFactory() {
        return smartthingsHandlerFactory;
    }

    public String getToken() {
        return config.token;
    }

    public String getClientId() {
        return config.clientId;
    }

    public String getClientSecret() {
        return config.clientSecret;
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        Collection<ConfigStatusMessage> configStatusMessages = new LinkedList<ConfigStatusMessage>();

        return configStatusMessages;
    }
}
