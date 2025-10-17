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
package org.openhab.binding.jellyfin.internal.handler.util;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.Configuration;
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for managing configuration updates from URIs and SystemInfo.
 * Encapsulates configuration comparison and update logic.
 *
 * @author Patrik Gfeller - Extracted from ServerHandler for better maintainability
 */
@NonNullByDefault
public class ConfigurationManager {

    private final Logger logger = LoggerFactory.getLogger(ConfigurationManager.class);

    /**
     * Configuration update result containing the updated configuration values
     * and whether any changes were detected.
     */
    public record ConfigurationUpdate(Map<String, Object> configMap, boolean hasChanges) {
    }

    /**
     * Analyzes a URI and determines what configuration changes are needed
     * compared to the current configuration.
     *
     * @param uri The URI containing new configuration information
     * @param currentConfig The current configuration to compare against
     * @return ConfigurationUpdate containing the new configuration map and change status
     */
    public ConfigurationUpdate analyzeUriConfiguration(URI uri, Configuration currentConfig) {
        var configMap = new HashMap<String, Object>();
        boolean hasChanges = false;

        // Check hostname changes
        if (uri.getHost() != null && !uri.getHost().equals(currentConfig.hostname)) {
            configMap.put("hostname", uri.getHost());
            hasChanges = true;
            logger.debug("Configuration change detected: hostname {} -> {}", currentConfig.hostname, uri.getHost());
        } else {
            configMap.put("hostname", currentConfig.hostname);
        }

        // Check port changes
        if (uri.getPort() > 0 && uri.getPort() != currentConfig.port) {
            configMap.put("port", uri.getPort());
            hasChanges = true;
            logger.debug("Configuration change detected: port {} -> {}", currentConfig.port, uri.getPort());
        } else {
            configMap.put("port", currentConfig.port);
        }

        // Check SSL/scheme changes
        if (uri.getScheme() != null) {
            boolean newSslValue = "https".equalsIgnoreCase(uri.getScheme());
            if (newSslValue != currentConfig.ssl) {
                configMap.put("ssl", newSslValue);
                hasChanges = true;
                logger.debug("Configuration change detected: ssl {} -> {}", currentConfig.ssl, newSslValue);
            } else {
                configMap.put("ssl", currentConfig.ssl);
            }
        } else {
            configMap.put("ssl", currentConfig.ssl);
        }

        // Check path changes
        if (uri.getPath() != null && !uri.getPath().isEmpty() && !uri.getPath().equals(currentConfig.path)) {
            configMap.put("path", uri.getPath());
            hasChanges = true;
            logger.debug("Configuration change detected: path {} -> {}", currentConfig.path, uri.getPath());
        } else {
            configMap.put("path", currentConfig.path);
        }

        if (hasChanges) {
            logger.info("Configuration changes detected from URI analysis");
        } else {
            logger.debug("No configuration changes needed from URI analysis");
        }

        return new ConfigurationUpdate(configMap, hasChanges);
    }

    /**
     * Extracts local address from SystemInfo and creates a URI for configuration analysis.
     *
     * @param systemInfo The system information from the Jellyfin server
     * @param currentConfig The current configuration
     * @return ConfigurationUpdate if local address is available, empty result otherwise
     */
    public ConfigurationUpdate analyzeSystemInfoConfiguration(SystemInfo systemInfo, Configuration currentConfig) {
        var localAddress = systemInfo.getLocalAddress();

        if (localAddress != null && !localAddress.isEmpty()) {
            try {
                URI localUri = new URI(localAddress);
                logger.debug("Analyzing configuration from SystemInfo local address: {}", localAddress);
                return analyzeUriConfiguration(localUri, currentConfig);
            } catch (Exception e) {
                logger.debug("Failed to parse local address URI from SystemInfo: {}", e.getMessage());
                return new ConfigurationUpdate(Map.of(), false);
            }
        }

        logger.debug("No local address available in SystemInfo for configuration analysis");
        return new ConfigurationUpdate(Map.of(), false);
    }

    /**
     * Updates the current configuration object with new values.
     * This method modifies the configuration object in place.
     *
     * @param currentConfig The configuration object to update
     * @param configMap The new configuration values
     */
    public void applyConfigurationChanges(Configuration currentConfig, Map<String, Object> configMap) {
        configMap.forEach((key, value) -> {
            switch (key) {
                case "hostname":
                    currentConfig.hostname = (String) value;
                    break;
                case "port":
                    currentConfig.port = (Integer) value;
                    break;
                case "ssl":
                    currentConfig.ssl = (Boolean) value;
                    break;
                case "path":
                    currentConfig.path = (String) value;
                    break;
                default:
                    logger.warn("Unknown configuration key: {}", key);
                    break;
            }
        });
    }
}
