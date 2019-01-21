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
package org.openhab.ui.paper.internal;

import org.openhab.ui.dashboard.DashboardTile;
import org.osgi.service.component.annotations.Component;

/**
 * The dashboard tile for the Paper UI
 *
 * @author Kai Kreuzer
 *
 */
@Component
public class PaperUIDashboardTile implements DashboardTile {

    @Override
    public String getName() {
        return "Paper UI";
    }

    @Override
    public String getUrl() {
        return "../paperui/index.html";
    }

    @Override
    public String getOverlay() {
        return null;
    }

    @Override
    public String getImageUrl() {
        return "../paperui/img/dashboardtile.png";
    }
}
