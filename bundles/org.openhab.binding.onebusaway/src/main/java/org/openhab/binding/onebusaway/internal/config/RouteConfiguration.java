/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.onebusaway.internal.config;

import static org.openhab.binding.onebusaway.internal.OneBusAwayBindingConstants.ROUTE_CONFIG_ROUTE_ID;

/**
 * The {@link RouteConfiguration} defines the model for a route stop configuration.
 *
 * @author Shawn Wilsher - Initial contribution
 */
public class RouteConfiguration {

    private String routeId;

    /**
     * @return the route ID.
     */
    public String getRouteId() {
        return routeId;
    }

    /**
     * Sets the route ID.
     */
    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{ " + ROUTE_CONFIG_ROUTE_ID + "=" + this.getRouteId() + "}";
    }
}
