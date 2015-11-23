/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.basicui.internal;

import org.openhab.ui.dashboard.DashboardTile;

/**
 * The dashboard tile for the Basic UI
 * 
 * @author Kai Kreuzer
 *
 */
public class BasicUIDashboardTile implements DashboardTile {

    @Override
    public String getName() {
        return "Basic UI";
    }

    @Override
    public String getUrl() {
        return "../basicui/app";
    }

    @Override
    public String getOverlay() {
        return "html5";
    }

    @Override
    public String getImageUrl() {
        return "img/basicui.png";
    }
}
