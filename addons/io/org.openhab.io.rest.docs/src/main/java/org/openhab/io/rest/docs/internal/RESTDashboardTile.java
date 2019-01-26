/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.io.rest.docs.internal;

import org.openhab.ui.dashboard.DashboardTile;
import org.osgi.service.component.annotations.Component;

/**
 * The dashboard tile for the REST API,
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
@Component
public class RESTDashboardTile implements DashboardTile {

    protected void activate() {
    }

    protected void deactivate() {
    }

    @Override
    public String getName() {
        return "REST API";
    }

    @Override
    public String getUrl() {
        return "../doc/index.html";
    }

    @Override
    public String getOverlay() {
        return null;
    }

    @Override
    public String getImageUrl() {
        return "../doc/images/dashboardtile.png";
    }

}
