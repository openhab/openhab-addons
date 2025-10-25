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
package org.openhab.binding.jellyfin.internal.util;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for managing configuration updates from URIs and SystemInfo.
 * Encapsulates configuration comparison and update logic.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class ConfigurationManager {
    /**
     * Analyzes SystemInfo and determines what configuration changes are needed compared to the current configuration.
     *
     * @param systemInfo The SystemInfo object from Jellyfin
     * @param currentConfig The current configuration to compare against
     * @return ConfigurationUpdate containing the new configuration map and change status
     */
    public ConfigurationUpdate analyzeSystemInfoConfiguration(
            org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo systemInfo,
            Configuration currentConfig) {
        var configMap = new HashMap<String, Object>();
        boolean hasChanges = false;

        // Example: check for server name or version changes (customize as needed)
        if (systemInfo != null && systemInfo.getServerName() != null
                && !systemInfo.getServerName().equals(currentConfig.hostname)) {
            configMap.put("hostname", systemInfo.getServerName());
            hasChanges = true;
            logger.debug("Configuration change detected: hostname {} -> {}", currentConfig.hostname,
                    systemInfo.getServerName());
        } else {
            configMap.put("hostname", currentConfig.hostname);
        }
        // Add more fields as needed for your configuration
        // For now, just return the current config if nothing changes
        return new ConfigurationUpdate(configMap, hasChanges);
    }

    /**
     * Applies configuration changes to the given configuration object.
     *
     * @param configuration The configuration to update
     * @param configMap The map of configuration values to apply
     */
    public void applyConfigurationChanges(Configuration configuration, Map<String, Object> configMap) {
        if (configMap.containsKey("hostname")) {
            configuration.hostname = (String) configMap.get("hostname");
        }
        if (configMap.containsKey("port")) {
            configuration.port = (Integer) configMap.get("port");
        }
        if (configMap.containsKey("ssl")) {
            configuration.ssl = (Boolean) configMap.get("ssl");
        }
        if (configMap.containsKey("path")) {
            configuration.path = (String) configMap.get("path");
        }
        logger.info("Applied configuration changes: {}", configMap);
    }

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
}
