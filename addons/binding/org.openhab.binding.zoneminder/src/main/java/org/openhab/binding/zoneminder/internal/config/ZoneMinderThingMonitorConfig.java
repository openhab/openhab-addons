/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder.internal.config;

import org.openhab.binding.zoneminder.ZoneMinderConstants;

public class ZoneMinderThingMonitorConfig extends ZoneMinderThingConfig {

    // Parameters
    private Integer monitorId;

    @Override
    public String getConfigId() {
        return ZoneMinderConstants.THING_ZONEMINDER_MONITOR;
    }

    public String getId() {
        return monitorId.toString();
    }

    /*
     * public void setId(String id) {
     * this.monitorId = id;
     * 
     * }
     */
    @Override
    public String getZoneMinderId() {
        return monitorId.toString();
    }
}
