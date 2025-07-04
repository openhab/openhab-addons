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
package org.openhab.binding.jellyfin.internal.client.discovery;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.openhab.binding.jellyfin.internal.client.Jellyfin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for discovering Jellyfin servers and normalizing server addresses
 *
 * @author Patrik Gfeller, based on Android SDK by Peter Feller - Initial contribution (AI generated code by "Claude
 *         Sonnet 3.7")
 */
public class DiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(DiscoveryService.class);
    private final Jellyfin jellyfin;

    /**
     * Create a new discovery service
     *
     * @param jellyfin The Jellyfin client instance
     */
    public DiscoveryService(Jellyfin jellyfin) {
        this.jellyfin = jellyfin;
    }

    /**
     * Normalize a server URL to ensure it has the correct format
     *
     * @param url The server URL to normalize
     * @return The normalized URL or null if invalid
     */
    public String normalizeUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }

        try {
            // Add http:// if no protocol is specified
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://" + url;
            }

            URI uri = new URI(url);

            // Ensure we have a valid hostname or IP
            if (uri.getHost() == null || uri.getHost().isEmpty()) {
                return null;
            }

            // Remove trailing slashes from path
            String path = uri.getPath();
            while (path != null && !path.isEmpty() && path.endsWith("/")) {
                path = path.substring(0, path.length() - 1);
            }

            // Build the normalized URL
            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path, uri.getQuery(),
                    uri.getFragment()).toString();
        } catch (URISyntaxException e) {
            logger.debug("Failed to normalize URL: {}", url, e);
            return null;
        }
    }

    /**
     * Search for Jellyfin servers on the local network
     *
     * @return CompletableFuture with list of discovered servers
     */
    public CompletableFuture<List<ServerDiscoveryInfo>> discoverLocalServers() {
        // In a real implementation, this would perform server discovery
        // For this example, we'll return an empty list
        return CompletableFuture.completedFuture(new ArrayList<>());
    }
}
