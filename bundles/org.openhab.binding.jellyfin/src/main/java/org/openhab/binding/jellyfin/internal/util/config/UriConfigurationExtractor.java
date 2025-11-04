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

import java.net.URI;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.jellyfin.internal.Configuration;

/**
 * Extracts configuration properties from a URI by parsing its components
 * (scheme, host, port, path) and comparing them against current configuration.
 *
 * @author Patrik Gfeller - Initial contribution
 */
@NonNullByDefault
public class UriConfigurationExtractor implements ConfigurationExtractor<URI> {

    @Override
    public ConfigurationUpdate extract(URI source, Configuration current) {
        String hostname = extractHostname(source, current);
        int port = extractPort(source, current);
        boolean ssl = extractSsl(source, current);
        String path = extractPath(source, current);

        // Create updated configuration, preserving serverName
        Configuration updated = new Configuration();
        updated.serverName = current.serverName;
        updated.hostname = hostname;
        updated.port = port;
        updated.ssl = ssl;
        updated.path = path;
        updated.token = current.token;
        updated.refreshSeconds = current.refreshSeconds;
        updated.clientActiveWithInSeconds = current.clientActiveWithInSeconds;

        boolean hasChanges = !Objects.equals(hostname, current.hostname) || port != current.port || ssl != current.ssl
                || !Objects.equals(path, current.path);

        return new ConfigurationUpdate(updated, hasChanges);
    }

    private String extractHostname(URI source, Configuration current) {
        String host = source.getHost();
        return host != null ? host : current.hostname;
    }

    private int extractPort(URI source, Configuration current) {
        int uriPort = source.getPort();
        return uriPort > 0 ? uriPort : current.port;
    }

    private boolean extractSsl(URI source, Configuration current) {
        String scheme = source.getScheme();
        return scheme != null ? "https".equalsIgnoreCase(scheme) : current.ssl;
    }

    private String extractPath(URI source, Configuration current) {
        String uriPath = source.getPath();
        return (uriPath != null && !uriPath.isEmpty()) ? uriPath : current.path;
    }
}
