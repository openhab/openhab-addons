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
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.transform.geocoding.internal.config.GeoProfileConfig;
import org.openhab.transform.geocoding.internal.provider.nominatim.OSMGeocodingResolver;
import org.openhab.transform.geocoding.internal.provider.nominatim.OSMReverseGeocodingResolver;

/**
 * The {@link GeocodingProviderFactory} is responsible to create resolver objects for geocoding and reverse geocoding.
 * {@link GeoProfileConfig} holds the information about the configured provider. The factory provides the necessary
 * structure to integrate new geocoding providers.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class GeocodingProviderFactory {

    // HttpClient to execute service API calls
    protected HttpClient httpClient;
    // Configuration parameters to be respected
    protected GeoProfileConfig configuration;

    public GeocodingProviderFactory(GeoProfileConfig config, HttpClient httpClient) {
        this.configuration = config;
        this.httpClient = httpClient;
    }

    /**
     * Factory function to create GeocodingResolver objects. Extend this function if new providers are introduced,
     *
     * @param toBeResolved state for transformation
     * @return GeocodingResolver
     */
    public GeocodingResolver createResolver(State toBeResolved) {
        switch (configuration.provider) {
            case PROVIDER_NOMINATIM_OPENSTREETMAP: {
                return getNominatimOSMResolver(toBeResolved);
            }
            default:
                // default is Nominatiom / OpenStreetMap
                return getNominatimOSMResolver(toBeResolved);
        }
    }

    /**
     * Deliver Nominatim / OpenStreetMap resolver objects based on the given state type.
     * StringType will be resolved to PointType (geocoding) and other way rounf (reverse geocding).
     *
     * @param toBeResolved state for transformation
     * @return {@link GeocodingResolver} which is used in the profile
     */
    private GeocodingResolver getNominatimOSMResolver(State toBeResolved) {
        if (toBeResolved instanceof PointType pointType) {
            return new OSMReverseGeocodingResolver(pointType, configuration, httpClient);
        } else if (toBeResolved instanceof StringType stringType) {
            return new OSMGeocodingResolver(stringType, configuration, httpClient);
        } else {
            throw new IllegalArgumentException(
                    "Unsupported state type for geocoding resolver: " + toBeResolved.getClass().getSimpleName());
        }
    }
}
