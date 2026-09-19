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
package org.openhab.binding.lghorizon.internal.api;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Known LG Horizon providers and the connection details behind each one. LG Horizon is white-labelled by several cable
 * operators; each one runs its own "spark" cloud backend under the same platform.
 * <p>
 * This is the single source of truth for known providers: both {@link #get(String)} (used to resolve a selected
 * provider to its connection details) and {@code LGHorizonConfigOptionProvider} (used to populate the "provider"
 * dropdown in the UI) read from the same table.
 * <p>
 * A provider that isn't listed here at all (an unreleased backend, a provider's preprod/test environment, ...) doesn't
 * need an entry: the account thing's advanced {@code country}/{@code apiUrl}/ {@code useRefreshToken} parameters can be
 * filled in manually instead.
 *
 * @author Mark - Initial contribution
 */
@NonNullByDefault
public class ProviderPresets {

    /**
     * Connection details for a single known provider.
     *
     * @param apiUrl base URL of the provider's "spark" REST API
     * @param countryCode two-letter locale code used in the service-discovery
     *            URL path (e.g. {@code be}, {@code nl}, {@code ch}) - not
     *            necessarily identical to the country implied by the provider
     *            name (matches {@code country_code[0:2]} in
     *            lghorizon-python)
     * @param useRefreshToken whether this provider requires refresh-token auth
     *            instead of plain username/password
     * @param displayName human readable provider name
     */
    public record Preset(String apiUrl, String countryCode, boolean useRefreshToken, String displayName) {
    }

    private static final Map<String, Preset> PRESETS = new HashMap<>();

    static {
        PRESETS.put("telenet",
                new Preset("https://spark-prod-be.gnp.cloud.telenet.tv", "be", true, "Telenet (Belgium)"));
        PRESETS.put("basetv", new Preset("https://spark-prod-be.gnp.cloud.base.tv", "be", true, "BASE TV (Belgium)"));
        PRESETS.put("ziggo",
                new Preset("https://spark-prod-nl.gnp.cloud.ziggogo.tv", "nl", false, "Ziggo (Netherlands)"));
        PRESETS.put("upc-sunrise",
                new Preset("https://spark-prod-ch.gnp.cloud.sunrisetv.ch", "ch", true, "UPC / Sunrise (Switzerland)"));
        PRESETS.put("virginmedia-gb", new Preset("https://spark-prod-gb.gnp.cloud.virgintvgo.virginmedia.com", "gb",
                true, "Virgin Media (United Kingdom)"));
        PRESETS.put("virginmedia-ie",
                new Preset("https://spark-prod-ie.gnp.cloud.virginmediatv.ie", "ie", false, "Virgin Media (Ireland)"));
        PRESETS.put("upc-poland", new Preset("https://spark-prod-pl.gnp.cloud.upctv.pl", "pl", false, "UPC (Poland)"));
    }

    private ProviderPresets() {
        // static lookup table
    }

    public static Preset get(String providerId) {
        Preset preset = PRESETS.get(providerId);
        if (preset == null) {
            throw new IllegalArgumentException("Unknown/unsupported LG Horizon provider: " + providerId);
        }
        return preset;
    }

    public static Map<String, Preset> all() {
        return PRESETS;
    }
}
