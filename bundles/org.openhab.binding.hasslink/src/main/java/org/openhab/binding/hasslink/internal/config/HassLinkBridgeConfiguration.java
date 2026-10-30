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
package org.openhab.binding.hasslink.internal.config;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HassLinkBridgeConfiguration} class contains fields mapping the Home Assistant
 * server bridge configuration parameters.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class HassLinkBridgeConfiguration {

    /** Home Assistant hostname or IP address, without a protocol prefix. */
    public String host = "";

    /** Home Assistant HTTP and WebSocket port. */
    public int port = 8123;

    /**
     * Home Assistant Long-Lived Access Token used to authenticate the WebSocket connection.
     */
    public String token = "";

    /** Whether the HTTP and WebSocket connections use TLS. */
    public boolean secure;

    // Filtering Parameters

    // Global / Behavior Flags
    public boolean ignoreIndependentEntities = true;

    // Spatial / Organizational Filters (Where is it?)
    public List<String> includedAreas = List.of();
    public List<String> excludedAreas = List.of();

    // Domain / Type Filters (What type of entity is it?)
    public List<String> includedDomains = List.of();
    public List<String> excludedDomains = List.of("automation", "conversation", "counter", "media_source",
            "persistent_notification", "stt", "sun", "timer", "tts", "zone");

    // Label / Tag Filters (How is it tagged?)
    public List<String> includedLabels = List.of();
    public List<String> excludedLabels = List.of();

    public String toString() {
        String redactedToken = token.length() > 5 ? "..." + token.substring(token.length() - 5) : token;
        StringBuilder sb = new StringBuilder();
        sb.append("HassLinkBridgeConfiguration [host=").append(host) //
                .append(", port=").append(port) //
                .append(", token=").append(redactedToken) //
                .append(", secure=").append(secure) //
                .append(", ignoreIndependentEntities=").append(ignoreIndependentEntities) //
                .append(", includedAreas=").append(includedAreas) //
                .append(", excludedAreas=").append(excludedAreas) //
                .append(", includedDomains=").append(includedDomains) //
                .append(", excludedDomains=").append(excludedDomains) //
                .append(", includedLabels=").append(includedLabels) //
                .append(", excludedLabels=").append(excludedLabels) //
                .append("]");
        return sb.toString();
    }
}
