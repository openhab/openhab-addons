/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.nuki.internal.discovery;

import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpStatus;
import org.openhab.binding.nuki.internal.constants.NukiBindingConstants;
import org.openhab.binding.nuki.internal.constants.NukiLinkBuilder;
import org.openhab.binding.nuki.internal.dto.BridgeApiAuthDto;
import org.openhab.binding.nuki.internal.dto.WebApiBridgeDiscoveryDto;
import org.openhab.binding.nuki.internal.dto.WebApiBridgeDto;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.id.InstanceUUID;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Discovery service which uses Nuki Web API to discover all bridges on same network
 * and uses authentication API to obtain access token.
 *
 * @author Jan Vybíral - Initial contribution
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, configurationPid = "discovery." + NukiBindingConstants.BINDING_ID)
public class NukiBridgeDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(NukiBridgeDiscoveryService.class);

    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private final ScheduledExecutorService threadPool = ThreadPoolManager.getScheduledPool("discovery");

    @Activate
    public NukiBridgeDiscoveryService(@Reference final HttpClientFactory httpClientFactory) {
        super(Collections.singleton(NukiBindingConstants.THING_TYPE_BRIDGE), 30, false);
        this.httpClient = httpClientFactory.getCommonHttpClient();
    }

    @Override
    protected void startScan() {
        logger.info("Bridge discovery started");

        try {
            ContentResponse response = this.httpClient.GET(NukiLinkBuilder.URI_BRIDGE_DISCOVERY);
            if (response.getStatus() == HttpStatus.OK_200) {
                String responseString = response.getContentAsString();
                WebApiBridgeDiscoveryDto discoveryResult = gson.fromJson(responseString,
                        WebApiBridgeDiscoveryDto.class);
                if (discoveryResult == null) {
                    logger.error("Bridge discovery failed - API returned invalid body {}", responseString);
                } else if (discoveryResult.getErrorCode() == 0) {
                    discoverBridges(discoveryResult);
                } else {
                    logger.error("Bridge discovery failed - API returned error code '{}': {}",
                            discoveryResult.getErrorCode(), responseString);
                }
            } else {
                logger.error("Bridge discovery failed - invalid status {}: '{}'", response.getStatus(),
                        response.getContentAsString());
            }
        } catch (Exception e) {
            logger.error("Bridge discovery failed - '{}': {}", e.getClass(), e.getMessage());
            logger.debug("Bridge discovery failed", e);
        }
    }

    private static String generateBridgeId(String bridgeNukiId) {
        String hash = NukiLinkBuilder.sha256(InstanceUUID.get() + ":" + bridgeNukiId);
        return hash.substring(0, 10);
    }

    private void discoverBridges(WebApiBridgeDiscoveryDto discoveryResult) {
        logger.debug("Discovery finished, found {} bridges", discoveryResult);

        discoveryResult.getBridges().forEach(bridge -> {
            threadPool.execute(new BridgeInitializer(bridge));
        });
    }

    private void discoverBridge(WebApiBridgeDto bridgeData, String token) {
        String name;
        if (token.isBlank()) {
            logger.debug("Nuki bridge {}({}) discovered without api token", bridgeData.getIp(),
                    bridgeData.getBridgeId());
            name = "Nuki Bridge (no API token)";
        } else {
            logger.info("Nuki bridge {}({}) discovered and initialized", bridgeData.getIp(), bridgeData.getBridgeId());
            name = "Nuki Bridge";
        }

        DiscoveryResult result = DiscoveryResultBuilder
                .create(new ThingUID(NukiBindingConstants.THING_TYPE_BRIDGE,
                        generateBridgeId(bridgeData.getBridgeId())))
                .withLabel(name).withProperty(NukiBindingConstants.PROPERTY_BRIDGE_ID, bridgeData.getBridgeId())
                .withProperty(NukiBindingConstants.PROPERTY_BRIDGE_IP, bridgeData.getIp())
                .withProperty(NukiBindingConstants.PROPERTY_BRIDGE_PORT, bridgeData.getPort())
                .withProperty(NukiBindingConstants.PROPERTY_BRIDGE_TOKEN, token)
                .withRepresentationProperty(NukiBindingConstants.PROPERTY_BRIDGE_ID).build();
        thingDiscovered(result);
    }

    private class BridgeInitializer implements Runnable {
        private final WebApiBridgeDto bridge;

        private BridgeInitializer(WebApiBridgeDto bridge) {
            this.bridge = bridge;
        }

        @Override
        public void run() {
            logger.info("Discovered Nuki bridge {}({}) - obtaining API token, press button on bridge to complete",
                    bridge.getIp(), bridge.getBridgeId());
            try {
                ContentResponse response = httpClient.GET(NukiLinkBuilder.getAuthUri(bridge.getIp(), bridge.getPort()));
                String responseData = response.getContentAsString();
                if (response.getStatus() == HttpStatus.OK_200) {
                    BridgeApiAuthDto authResult = gson.fromJson(responseData, BridgeApiAuthDto.class);
                    if (authResult != null && authResult.isSuccess()) {
                        discoverBridge(bridge, authResult.getToken());
                        return;
                    } else {
                        logger.warn(
                                "Failed to get API token for bridge {}({}) - bridge did not return success response, make sure button on bridge was pressed during discovery",
                                bridge.getIp(), bridge.getBridgeId());
                    }
                } else if (response.getStatus() == HttpStatus.FORBIDDEN_403) {
                    logger.error(
                            "Failed to get API token for bridge {}({}) - bridge authentication is disabled, check settings",
                            bridge.getIp(), bridge.getBridgeId());
                } else {
                    logger.error("Failed to get API token for bridge {}({}) - invalid status {}: {}", bridge.getIp(),
                            bridge.getBridgeId(), response.getStatus(), responseData);
                }
            } catch (Exception e) {
                logger.error("Failed to get API token for bridge {}({}) - {}", bridge.getIp(), bridge.getBridgeId(),
                        e.getMessage());
                logger.debug("Failed to get API token for bridge {}({})", bridge.getIp(), bridge.getBridgeId(), e);
            }
            discoverBridge(bridge, "");
        }
    }
}
