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
package org.openhab.binding.jellyfin.internal.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.BindingConfiguration;
import org.openhab.binding.jellyfin.internal.Constants;
import org.openhab.binding.jellyfin.internal.api.ApiClientWrapper;
import org.openhab.binding.jellyfin.internal.gen.ApiException;
import org.openhab.binding.jellyfin.internal.gen.current.SystemApi;
import org.openhab.binding.jellyfin.internal.gen.current.model.ServerDiscoveryInfo;
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
    private static final String MIN_SUPPORTED_VERSION = "10.10.7"; // Minimum supported server version

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
        ServerDiscovery discoverer = new ServerDiscovery(configuration.discoveryPort, configuration.discoveryTimeout,
                configuration.discoveryMessage);

        List<ServerDiscoveryInfo> servers = discoverer.discoverServers();

        if (!servers.isEmpty()) {
            for (ServerDiscoveryInfo server : servers) {
                String serverName = server.getName();
                String serverAddress = server.getAddress();
                String serverId = server.getId();

                if (serverId == null || serverAddress == null) {
                    logger.debug("Skipping discovered server with missing id or address");
                    continue;
                }

                logger.debug("Server {} @ {}", serverName, serverAddress);

                var uid = new ThingUID(Constants.THING_TYPE_SERVER, serverId);
                try {
                    var properties = this.getProperties(server);
                    var version = properties.get(Thing.PROPERTY_FIRMWARE_VERSION).toString();
                    if (this.isVersionSupported(version)) {
                        var resultBuilder = DiscoveryResultBuilder.create(uid).withProperties(properties)
                                .withRepresentationProperty(Thing.PROPERTY_SERIAL_NUMBER)
                                .withLabel(serverName != null ? serverName : serverId)
                                .withTTL(Constants.DISCOVERY_RESULT_TTL_SEC);

                        var result = resultBuilder.build();

                        this.thingDiscovered(result);
                    } else {
                        logger.info("Discovered server {} @ {} will be ignored. Version {} is not supported.",
                                serverName, serverAddress, version);
                    }
                } catch (Exception e) {
                    logger.warn(
                            "Failed to retrieve system info from Jellyfin server at {}: {}, server will be ignored.",
                            serverAddress, e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Get properties of the Jellyfin server.
     *
     * @param server The discovered server
     * @return Map of server properties
     * @throws ApiException If the API call to get additional server information fails
     */
    private Map<String, Object> getProperties(ServerDiscoveryInfo server) throws ApiException {
        Map<String, Object> properties = new HashMap<>();

        var uri = server.getAddress();
        var client = new ApiClientWrapper();
        client.updateBaseUri(uri);

        var systemApi = new SystemApi(client);
        var systemInformation = systemApi.getPublicSystemInfo();

        String id = systemInformation.getId();
        if (id != null) {
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, id);
        }
        String version = systemInformation.getVersion();
        if (version != null) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, version);
        }
        properties.put(Thing.PROPERTY_VENDOR, "https://jellyfin.org");

        if (uri != null) {
            properties.put(Constants.ServerProperties.SERVER_URI, uri);
        }

        return properties;
    }

    /**
     * Check if the provided version is equal to or newer than the minimum supported version (10.10.7).
     * <p>
     * If the version string is empty or cannot be parsed as a semantic version, this method will assume the version is
     * supported.
     * This non-standard behavior is intended to avoid false negatives due to unexpected version formats.
     *
     * @param version The version string to check
     * @return true if the version is equal to or newer than 10.10.7, or if parsing fails; false otherwise
     */
    private boolean isVersionSupported(String version) {
        if (version == null || version.isEmpty()) {
            logger.warn("Empty version string provided, assuming supported version");
            return true;
        }

        try {
            // Split version strings into components
            String[] currentParts = version.split("\\.");
            String[] minParts = MIN_SUPPORTED_VERSION.split("\\.");

            // Parse major version
            int currentMajor = currentParts.length > 0 ? parseVersionComponent(currentParts[0]) : 0;
            int minMajor = minParts.length > 0 ? Integer.parseInt(minParts[0]) : 0;

            if (currentMajor > minMajor) {
                return true;
            } else if (currentMajor < minMajor) {
                return false;
            }

            // Parse minor version
            int currentMinor = currentParts.length > 1 ? parseVersionComponent(currentParts[1]) : 0;
            int minMinor = minParts.length > 1 ? Integer.parseInt(minParts[1]) : 0;

            if (currentMinor > minMinor) {
                return true;
            } else if (currentMinor < minMinor) {
                return false;
            }

            // Parse patch version
            int currentPatch = currentParts.length > 2 ? parseVersionComponent(currentParts[2]) : 0;
            int minPatch = minParts.length > 2 ? Integer.parseInt(minParts[2]) : 0;

            return currentPatch >= minPatch;
        } catch (NumberFormatException e) {
            logger.warn("Failed to parse version '{}' as semantic version: {}. Assuming supported version.", version,
                    e.getMessage(), e);
            return true;
        }
    }

    /**
     * Parse a version component, handling non-numeric suffixes.
     * For example, "8-beta" would return 8.
     *
     * @param component The version component to parse
     * @return The numeric part as an integer
     * @throws NumberFormatException If parsing fails
     */
    private int parseVersionComponent(String component) {
        // Extract numeric part before any non-numeric character
        int endIndex = 0;
        while (endIndex < component.length() && Character.isDigit(component.charAt(endIndex))) {
            endIndex++;
        }

        if (endIndex == 0) {
            throw new NumberFormatException("No numeric part in version component: " + component);
        }

        return Integer.parseInt(component.substring(0, endIndex));
    }
}
