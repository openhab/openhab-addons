/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.onebusaway.internal.handler;

import java.util.List;

/**
 * The {@link RouteDataListener} is the interface used for the stop (a bridge) to communicate information about route
 * arrivals.
 *
 * @author Shawn Wilsher - Initial contribution
 */
interface RouteDataListener {
    /**
     * @return The routeId for this listener. {@link #onNewRouteData(List)} should only receive updates for
     *         this route.
     */
    String getRouteId();

    /**
     * Called when new arrival and departure data is available for this listeners route (as specified by
     * {@link #getRouteId()}).
     *
     * @param lastUpdateTime a {@link long} representing the time the data was last updated.
     * @param data a {@link List} of data from the OneBusAway API.
     */
    void onNewRouteData(long lastUpdateTime, List<ObaStopArrivalResponse.ArrivalAndDeparture> data);
}
