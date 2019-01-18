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
package org.openhab.io.neeo;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.neeo.internal.NeeoConstants;
import org.openhab.ui.dashboard.DashboardTile;
import org.osgi.service.component.annotations.Component;

/**
 * Implementation of the {@link DashboardTile} for the NEEO Integration
 *
 * @author Tim Roberts - Initial Contribution
 */
@NonNullByDefault
@Component(service = org.openhab.ui.dashboard.DashboardTile.class, immediate = true)
public class NeeoDashboardTile implements DashboardTile {

    @Override
    public String getName() {
        return "Neeo Integration";
    }

    @Override
    public String getUrl() {
        return ".." + NeeoConstants.WEBAPP_PREFIX + "/index.html";
    }

    @Override
    public String getImageUrl() {
        return ".." + NeeoConstants.WEBAPP_PREFIX + "/img/neeo.jpg";
    }

    @Nullable
    @Override
    public String getOverlay() {
        return null;
    }
}
