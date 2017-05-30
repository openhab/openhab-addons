/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.neeo;

import org.openhab.ui.dashboard.DashboardTile;
import org.osgi.service.component.annotations.Component;

/**
 * Implementation of the {@link DashboardTile} for the NEEO Transport
 *
 * @author Tim Roberts - Initial contribution
 */
@Component(service = org.openhab.ui.dashboard.DashboardTile.class, immediate = true)
public class NeeoDashboardTile implements DashboardTile {

    @Override
    public String getName() {
        return "Neeo Transport";
    }

    @Override
    public String getUrl() {
        return ".." + NeeoConstants.WEBAPP_PREFIX + "/index.html";
    }

    @Override
    public String getImageUrl() {
        return ".." + NeeoConstants.WEBAPP_PREFIX + "/img/neeo.jpg";
    }

    @Override
    public String getOverlay() {
        return null;
    }

}
