/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.net.URI;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartthingsAuthService;
import org.openhab.binding.smartthings.internal.SmartthingsHandlerFactory;
import org.openhab.binding.smartthings.internal.api.SmartthingsApi;
import org.openhab.binding.smartthings.internal.api.SmartthingsNetworkCallback;
import org.openhab.binding.smartthings.internal.dto.SmartthingsCapability;
import org.openhab.binding.smartthings.internal.type.SmartthingsException;
import org.openhab.binding.smartthings.internal.type.SmartthingsTypeRegistry;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;
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
            SmartthingsAuthService authService, BundleContext bundleContext, HttpService httpService,
            OAuthFactory oAuthFactory, HttpClientFactory httpClientFactory, SmartthingsTypeRegistry typeRegistry,
            ClientBuilder clientBuilder, SseEventSourceFactory eventSourceFactory) {
        super(bridge, smartthingsHandlerFactory, authService, bundleContext, httpService, oAuthFactory,
                httpClientFactory, typeRegistry, clientBuilder, eventSourceFactory);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Commands are handled by the "Things"
    }

    @Override
    public void initialize() {
        super.initialize();

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.submit(() -> {
            try {
                initRegistry();
                updateStatus(ThingStatus.ONLINE);
            } catch (SmartthingsException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });
    }

    public void initRegistry() throws SmartthingsException {
        initCapabilites();
        registerSubcriptions();
        discoService.doScan(false);
    }

    public void registerSubcriptions() {
        logger.info("registerSubcriptions()");
        SmartthingsApi api = this.getSmartthingsApi();
        api.registerSubscription();
    }

    public void initCapabilites() throws SmartthingsException {
        logger.info("initCapabilites()");
        SmartthingsApi api = this.getSmartthingsApi();
        typeRegistry.setCloudBridgeHandler(this);

        SmartthingsCapability[] capabilitiesList = api.getAllCapabilities();

        for (SmartthingsCapability cap : capabilitiesList) {
            String capId = cap.id;
            String capVersion = cap.version;

            api.getCapability(capId, capVersion, new SmartthingsNetworkCallback<SmartthingsCapability>() {

                @Override
                public void execute(URI uri, int status, @Nullable SmartthingsCapability capa) {
                    if (capa != null) {
                        logger.info("Cap: {} / {}", capa.id, capa.name);
                        typeRegistry.registerCapability(capa);
                    }
                }
            });
        }

        api.getNetworkConnector().waitAllPendingRequest();
        logger.info("End init capa");
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

        return true;
    }

    @Override
    public SmartthingsHandlerFactory getSmartthingsHandlerFactory() {
        return smartthingsHandlerFactory;
    }

    public String getClientId() {
        return config.clientId;
    }

    public String getClientSecret() {
        return config.clientSecret;
    }
}
