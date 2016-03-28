/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.classicui.internal;

import org.openhab.ui.dashboard.DashboardTile;

/**
 * The dashboard tile for the Classic UI
 * 
 * @author Kai Kreuzer
 *
 */
public class ClassicUIDashboardTile implements DashboardTile {

    @Override
    public String getName() {
        return "Classic UI";
    }

    @Override
    public String getUrl() {
        return "../classicui/app";
    }

    @Override
    public String getOverlay() {
        return "html5";
    }

    @Override
    public String getImageUrl() {
        return "img/classicui.png";
    }
}
