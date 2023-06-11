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
package org.openhab.binding.easee.internal.handler;

import static org.openhab.binding.easee.internal.EaseeBindingConstants.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.easee.internal.Utils;
import org.openhab.binding.easee.internal.command.EaseeCommand;
import org.openhab.binding.easee.internal.command.site.GetSite;
import org.openhab.binding.easee.internal.config.EaseeConfiguration;
import org.openhab.binding.easee.internal.connector.CommunicationStatus;
import org.openhab.binding.easee.internal.connector.WebInterface;
import org.openhab.binding.easee.internal.discovery.EaseeSiteDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link EaseeSiteHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class EaseeSiteHandler extends BaseBridgeHandler implements EaseeBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(EaseeSiteHandler.class);

    private @Nullable DiscoveryService discoveryService;

    /**
     * Interface object for querying the Easee web interface
     */
    private WebInterface webInterface;

    public EaseeSiteHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.webInterface = new WebInterface(scheduler, this, httpClient, super::updateStatus);
    }

    @Override
    public void initialize() {
        logger.debug("About to initialize Easee Site");
        EaseeConfiguration config = getBridgeConfiguration();
        logger.debug("Easee Site initialized with configuration: {}", config.toString());

        updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, STATUS_WAITING_FOR_LOGIN);
        webInterface.start();

        enqueueCommand(new GetSite(this, this::updateProperties));
    }

    private void updateProperties(CommunicationStatus status, JsonObject jsonObject) {
        Map<String, String> properties = editProperties();
        String name = Utils.getAsString(jsonObject, JSON_KEY_GENERIC_NAME);
        if (name != null) {
            properties.put(JSON_KEY_GENERIC_NAME, name);
        }
        String siteKey = Utils.getAsString(jsonObject, JSON_KEY_SITE_KEY);
        if (siteKey != null) {
            properties.put(JSON_KEY_SITE_KEY, siteKey);
        }
        updateProperties(properties);
    }

    /**
     * Disposes the bridge.
     */
    @Override
    public void dispose() {
        logger.debug("Handler disposed.");
        webInterface.dispose();
    }

    @Override
    public EaseeConfiguration getBridgeConfiguration() {
        return this.getConfigAs(EaseeConfiguration.class);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(EaseeSiteDiscoveryService.class);
    }

    public void setDiscoveryService(EaseeSiteDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Override
    public void startDiscovery() {
        DiscoveryService discoveryService = this.discoveryService;
        if (discoveryService != null) {
            discoveryService.startScan(null);
        }
    }

    @Override
    public void enqueueCommand(EaseeCommand command) {
        webInterface.enqueueCommand(command);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }
}
