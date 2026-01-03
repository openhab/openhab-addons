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
package org.openhab.binding.jellyfin.internal.util.config;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.Configuration;
import org.openhab.binding.jellyfin.internal.thirdparty.api.current.model.SystemInfo;

/**
 * Extracts configuration properties from Jellyfin SystemInfo by parsing relevant
 * fields and comparing them against current configuration.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class SystemInfoConfigurationExtractor implements ConfigurationExtractor<SystemInfo> {

    @Override
    public ConfigurationUpdate extract(SystemInfo source, Configuration current) {
        String serverName = extractServerName(source, current);
        String hostname = extractHostname(source, current);

        // SystemInfo doesn't typically contain port, ssl, or path information,
        // so we create a new Configuration with serverName and hostname updated
        Configuration updated = new Configuration();
        updated.serverName = serverName;
        updated.hostname = hostname;
        updated.port = current.port;
        updated.ssl = current.ssl;
        updated.path = current.path;
        updated.token = current.token;
        updated.refreshSeconds = current.refreshSeconds;
        updated.clientActiveWithInSeconds = current.clientActiveWithInSeconds;

        boolean hasChanges = !Objects.equals(serverName, current.serverName)
                || !Objects.equals(hostname, current.hostname);

        return new ConfigurationUpdate(updated, hasChanges);
    }

    private String extractServerName(SystemInfo source, Configuration current) {
        // Only update serverName if it's currently empty (user hasn't set a custom name)
        if (current.serverName.isEmpty()) {
            String name = source.getServerName();
            return name != null ? name : "";
        }
        // Preserve user-configured server name
        return current.serverName;
    }

    private String extractHostname(SystemInfo source, Configuration current) {
        String localAddress = source.getLocalAddress();
        return localAddress != null ? localAddress : current.hostname;
    }
}
