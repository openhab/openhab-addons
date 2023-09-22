/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import org.openhab.binding.smartthings.internal.SmartthingsBindingConstants;
import org.openhab.binding.smartthings.internal.SmartthingsHandlerFactory;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.status.ConfigStatusMessage;
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
 * The {@link SmartthingsHubBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartthingsHubBridgeHandler extends SmartthingsBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(SmartthingsHubBridgeHandler.class);

    public SmartthingsHubBridgeHandler(Bridge bridge, SmartthingsHandlerFactory smartthingsHandlerFactory,
            BundleContext bundleContext, HttpService httpService, OAuthFactory oAuthFactory) {
        super(bridge, smartthingsHandlerFactory, bundleContext, httpService, oAuthFactory);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Commands are handled by the "Things"
    }

    @Override
    public void initialize() {
        super.initialize();

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

        if (config.smartthingsIp.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Smartthings IP address is not specified");
            return false;
        }

        if (config.smartthingsPort <= 0) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Smartthings Port is not specified");
            return false;
        }

        return true;
    }

    @Override
    public SmartthingsHandlerFactory getSmartthingsHandlerFactory() {
        return smartthingsHandlerFactory;
    }

    public String getSmartthingsIp() {
        return config.smartthingsIp;
    }

    public int getSmartthingsPort() {
        return config.smartthingsPort;
    }

    @Override
    public Collection<ConfigStatusMessage> getConfigStatus() {
        Collection<ConfigStatusMessage> configStatusMessages = new LinkedList<ConfigStatusMessage>();

        // The IP must be provided
        String ip = config.smartthingsIp;
        if (ip.isEmpty()) {
            configStatusMessages.add(ConfigStatusMessage.Builder.error(SmartthingsBindingConstants.IP_ADDRESS)
                    .withMessageKeySuffix(SmartthingsBridgeConfigStatusMessage.IP_MISSING)
                    .withArguments(SmartthingsBindingConstants.IP_ADDRESS).build());
        }

        // The PORT must be provided
        int port = config.smartthingsPort;
        if (port <= 0) {
            configStatusMessages.add(ConfigStatusMessage.Builder.error(SmartthingsBindingConstants.PORT)
                    .withMessageKeySuffix(SmartthingsBridgeConfigStatusMessage.PORT_MISSING)
                    .withArguments(SmartthingsBindingConstants.PORT).build());
        }

        return configStatusMessages;
    }
}
