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
package org.openhab.transform.geocoding.internal.provider;

import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.core.OpenHAB;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.openhab.transform.geocoding.internal.config.GeoProfileConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link BaseGeoResolver} abstract class is a helper to extend this transformation service with new providers
 * without affecting the Profile itself.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public abstract class BaseGeoResolver {
    private final Logger logger = LoggerFactory.getLogger(BaseGeoResolver.class);

    // HTTP timeout
    protected static final int HTTP_TIMEOUT_SECONDS = 10;
    // HttpClient to execute service API calls
    protected HttpClient httpClient;
    // Configuration parameters to be respected
    protected GeoProfileConfig config;
    // State to be resolved
    protected State toBeResolved;
    // Geo search string to be resolved
    protected @Nullable String geoSearchString;
    // Geo location to be resolved
    protected @Nullable PointType geoLocation;
    // resulting address after resolve call
    protected @Nullable String resolvedString;
    // provide user agent for all providers
    protected Supplier<String> userAgentSupplier;

    /**
     * Creates a resolver object with all necessary data and configuration parameters.
     *
     * @param toBeResolved as State
     * @param config with all configured parameters
     * @param httpClient to perform service API calls
     */
    public BaseGeoResolver(State toBeResolved, GeoProfileConfig config, HttpClient httpClient) {
        this.config = config;
        this.httpClient = httpClient;
        userAgentSupplier = this::getUserAgent;
        this.toBeResolved = toBeResolved;
        // evaluate which state type is given to decide if it's a geocoding or reverse geocoding requests
        if (toBeResolved instanceof StringType stringType) {
            geoSearchString = stringType.toFullString();
        } else if (toBeResolved instanceof PointType pointType) {
            geoLocation = pointType;
        } else {
            logger.debug("State {} isn't supported for geocoding", toBeResolved);
        }
    }

    /**
     * Query to check State toBeResolved from constructor is successfully resolved
     *
     * @return true if address is resolved, false otherwise
     */
    public boolean isResolved() {
        return resolvedString != null;
    }

    /**
     * Gets the resolved string, either with human readable readable address string or geo coordinates for given search
     * address
     *
     * @return resolved String
     */
    public String getResolved() {
        String localResolved = resolvedString;
        if (localResolved != null) {
            return localResolved;
        }
        return "";
    }

    public String getProvider() {
        return config.provider;
    }

    /**
     * Starts resolved execution with the objects given in the constructor. Check afterwards with isResolved for
     * successful execution and getAddress to get resolved String.
     */
    public void resolve() {
        if (isResolved()) {
            logger.trace("State {} is already resolved {}", toBeResolved.toFullString(), getResolved());
            return;
        }

        PointType localGeoLocation = geoLocation;
        String localGeoSearchString = geoSearchString;
        if (localGeoLocation != null) {
            resolvedString = geoReverseSearch(localGeoLocation);
        } else if (localGeoSearchString != null) {
            resolvedString = geoSearch(localGeoSearchString);
        }
    }

    /**
     * Search coordinates for given address
     *
     * @param address as String
     * @return resolved coordinates as String "lat,lon" or null if not resolved
     */
    public abstract @Nullable String geoSearch(String address);

    /**
     * Search address for given coordinates
     *
     * @param coordinates as PointType
     * @return address as String or null if not resolved
     */
    public abstract @Nullable String geoReverseSearch(PointType coordinates);

    /**
     * Override the supplier for unit and release tests.
     *
     * @param userAgentSupplier the supplier providing the User-Agent header value
     */
    public void setUserAgentSupplier(Supplier<String> userAgentSupplier) {
        this.userAgentSupplier = userAgentSupplier;
    }

    /**
     * Get user agent for productive code
     *
     * @return user agent String for http queries
     */
    protected String getUserAgent() {
        return "openHAB/" + OpenHAB.getVersion() + " (Geo Transformation Service)";
    }
}
