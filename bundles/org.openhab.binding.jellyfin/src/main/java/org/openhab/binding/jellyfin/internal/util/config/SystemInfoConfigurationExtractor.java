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
import org.openhab.binding.jellyfin.internal.api.generated.current.model.SystemInfo;

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
        String hostname = extractHostname(source, current);

        // SystemInfo doesn't typically contain port, ssl, or path information,
        // so we preserve the current values
        boolean hasChanges = !Objects.equals(hostname, current.hostname);

        return new ConfigurationUpdate(hostname, current.port, current.ssl, current.path, hasChanges);
    }

    private String extractHostname(SystemInfo source, Configuration current) {
        String serverName = source.getServerName();
        return serverName != null ? serverName : current.hostname;
    }
}
