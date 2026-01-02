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
package org.openhab.transform.geocoding.internal.provider;

import static org.openhab.transform.geocoding.internal.GeoProfileConstants.PROVIDER_NOMINATIM_OPENSTREETMAP;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.types.State;
import org.openhab.transform.geocoding.internal.config.GeoProfileConfig;
import org.openhab.transform.geocoding.internal.provider.nominatim.OSMGeoResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GeoResolverFactory} is responsible to create resolver objects for geocoding and reverse geocoding.
 * {@link GeoProfileConfig} holds the information about the configured provider. The factory provides the necessary
 * structure to integrate new geocoding providers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class GeoResolverFactory {
    private final Logger logger = LoggerFactory.getLogger(GeoResolverFactory.class);

    // HttpClient to execute service API calls
    protected HttpClient httpClient;
    // Configuration parameters to be respected
    protected GeoProfileConfig configuration;

    public GeoResolverFactory(GeoProfileConfig config, HttpClient httpClient) {
        this.configuration = config;
        this.httpClient = httpClient;
    }

    /**
     * Factory function to create GeocodingResolver objects. Extend this function if new providers are introduced,
     *
     * @param toBeResolved state for transformation
     * @return GeocodingResolver according to configured provider
     */
    public BaseGeoResolver createResolver(State toBeResolved) {
        switch (configuration.provider) {
            case PROVIDER_NOMINATIM_OPENSTREETMAP: {
                return new OSMGeoResolver(toBeResolved, configuration, httpClient);
            }
            default:
                logger.warn("Configured geocoding provider '{}' is not supported, falling back to default provider.",
                        configuration.provider);
                // default is Nominatiom / OpenStreetMap
                return new OSMGeoResolver(toBeResolved, configuration, httpClient);
        }
    }
}
