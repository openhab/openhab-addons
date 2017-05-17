/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.ui.cometvisu.internal;

import org.openhab.ui.dashboard.DashboardTile;

public class CometVisuDashboardTile implements DashboardTile {
    @Override
    public String getName() {
        return "CometVisu";
    }

    @Override
    public String getUrl() {
        return Config.COMETVISU_WEBAPP_ALIAS + "/?config=demo";
    }

    @Override
    public String getOverlay() {
        return "html5";
    }

    @Override
    public String getImageUrl() {
        return "img/cometvisu.png";
    }
}
