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
package org.openhab.binding.jellyfin.internal.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.BindingConfiguration;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;
import org.openhab.binding.jellyfin.internal.api.generated.ApiException;
import org.openhab.binding.jellyfin.internal.api.generated.current.SystemApi;
import org.openhab.core.config.discovery.AbstractDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ServerDiscoveryService} discover Jellyfin servers in the network.
 * 
 * @author Miguel Álvarez - Initial contribution
 * @author Patrik Gfeller - Adjustments to work independently of the Android SDK
 *         and respective runtime
 */
@NonNullByDefault
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.jellyfin")
public class ServerDiscoveryService extends AbstractDiscoveryService {
    private static final int DEFAULT_SCAN_TIMEOUT_SECONDS = 60; // Timeout for discovery scan in seconds
    private static final boolean DEFAULT_BACKGROUND_SCAN = false; // Disable background scan

    private final Logger logger = LoggerFactory.getLogger(ServerDiscoveryService.class);
    private final ConfigurationAdmin configurationService;

    @Activate
    public ServerDiscoveryService(final @Reference ConfigurationAdmin configurationService)
            throws IllegalArgumentException {
        // Use named constants for scan timeout and background scan
        super(Set.of(Constants.THING_TYPE_SERVER), DEFAULT_SCAN_TIMEOUT_SECONDS, DEFAULT_BACKGROUND_SCAN);
        this.configurationService = configurationService;
    }

    @Override
    protected synchronized void startScan() {
        var configuration = BindingConfiguration.getConfiguration(configurationService);
        ServerDiscovery discoverer = new ServerDiscovery(configuration.discoveryPort, configuration.discoveryTimeout);

        List<ServerDiscoveryResult> servers = discoverer.discoverServers();

        if (!servers.isEmpty()) {
            servers.parallelStream().forEach(server -> {
                logger.debug("Server {} @ {}", server.getName(), server.getAddress());

                var uid = new ThingUID(Constants.THING_TYPE_SERVER, server.getId());
                try {
                    var properties = this.getProperties(server);
                    var resultBuilder = DiscoveryResultBuilder.create(uid).withProperties(properties)
                            .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER).withLabel(server.getName())
                            .withTTL(Constants.DISCOVERY_RESULT_TTL_SEC);

                    var result = resultBuilder.build();

                    this.thingDiscovered(result);
                } catch (Exception e) {
                    logger.warn("Failed to retrieve system info from Jellyfin server at {}: {}", server.getAddress(),
                            e.getMessage());
                    logger.info("Discovered server {} @ {} will be ignored.", server.getName(), server.getAddress());
                }
            });
        }
    }

    /**
     * Get properties of the Jellyfin server.
     * 
     * @param server The discovered server
     * @return Map of server properties
     * @throws ApiException If the API call to get additional server information fails
     */
    private Map<String, Object> getProperties(ServerDiscoveryResult server) throws ApiException {
        Map<String, Object> properties = new HashMap<>();

        var uri = server.getAddress();
        var client = new ApiClient();
        client.updateBaseUri(uri);

        var systemApi = new SystemApi(client);
        var systemInformation = systemApi.getPublicSystemInfo();

        properties.put(Thing.PROPERTY_SERIAL_NUMBER, systemInformation.getId());
        properties.put(Thing.PROPERTY_FIRMWARE_VERSION, systemInformation.getVersion());
        properties.put(Thing.PROPERTY_VENDOR, "https://jellyfin.org");

        properties.put(Constants.ServerProperties.SERVER_URI, uri);

        return properties;
    }
}
