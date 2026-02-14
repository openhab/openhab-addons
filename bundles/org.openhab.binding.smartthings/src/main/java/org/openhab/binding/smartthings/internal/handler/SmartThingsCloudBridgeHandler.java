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
package org.openhab.binding.smartthings.internal.handler;

import java.net.URI;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smartthings.internal.SmartThingsAuthService;
import org.openhab.binding.smartthings.internal.SmartThingsHandlerFactory;
import org.openhab.binding.smartthings.internal.api.SmartThingsApi;
import org.openhab.binding.smartthings.internal.api.SmartThingsNetworkCallback;
import org.openhab.binding.smartthings.internal.dto.SmartThingsCapability;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
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
 * The {@link SmartThingsBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bob Raker - Initial contribution
 */
@NonNullByDefault
public class SmartThingsCloudBridgeHandler extends SmartThingsBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(SmartThingsCloudBridgeHandler.class);

    public SmartThingsCloudBridgeHandler(Bridge bridge, SmartThingsHandlerFactory smartthingsHandlerFactory,
            SmartThingsAuthService authService, BundleContext bundleContext, HttpService httpService,
            OAuthFactory oAuthFactory, HttpClientFactory httpClientFactory, SmartThingsTypeRegistry typeRegistry,
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
        updateStatus(ThingStatus.UNKNOWN);
        super.initialize();
    }

    @Override
    protected void setupClient(@Nullable String callBackUri) {
        super.setupClient(callBackUri);

        scheduler.submit(() -> {
            try {
                initRegistry();
                updateStatus(ThingStatus.ONLINE);
            } catch (SmartThingsException e) {
                logger.error("Unable to initialize", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });
    }

    public void initRegistry() throws SmartThingsException {
        initCapabilites();
        registerSubcriptions();
        discoService.doScan(false);
    }

    public void registerSubcriptions() {
        logger.info("registerSubcriptions()");
        SmartThingsApi api = this.getSmartThingsApi();

        boolean sucess = false;

        sucess = api.registerSSESubscription();
        if (!sucess) {
            api.registerCallbackSubscription();
        }
    }

    public void initCapabilites() throws SmartThingsException {
        logger.info("initCapabilites()");
        SmartThingsApi api = this.getSmartThingsApi();
        typeRegistry.setCloudBridgeHandler(this);

        SmartThingsCapability[] capabilitiesList = api.getAllCapabilities();

        for (SmartThingsCapability cap : capabilitiesList) {
            String capId = cap.id;
            String capVersion = cap.version;

            api.getCapability(capId, capVersion, new SmartThingsNetworkCallback<SmartThingsCapability>() {

                @Override
                public void execute(URI uri, int status, @Nullable SmartThingsCapability capa) {
                    if (capa != null) {
                        if (capa.status.equals("proposed")) {
                            return;
                        }
                        logger.trace("Cap: {} / {}", capa.id, capa.name);
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
    protected boolean validateConfig(SmartThingsBridgeConfig config) {
        if (!super.validateConfig(config)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Unknow");
            return false;
        }

        return true;
    }

    @Override
    public SmartThingsHandlerFactory getSmartThingsHandlerFactory() {
        return smartthingsHandlerFactory;
    }

    public String getAppName() {
        return config.appName;
    }
}
