/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.onebusaway.internal.config;

import static org.openhab.binding.onebusaway.OneBusAwayBindingConstants.ROUTE_CONFIG_ROUTE_ID;

import org.apache.commons.lang.builder.ToStringBuilder;

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
        return new ToStringBuilder(this).append(ROUTE_CONFIG_ROUTE_ID, this.getRouteId()).toString();
    }
}
