/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal;

import io.swagger.client.model.NADashboardData;
import io.swagger.client.model.NAHealthyHomeCoach;
import io.swagger.client.model.NAHealthyHomeCoachDataBody;
import io.swagger.client.model.NAPlace;

/**
 * {@link NAHealthyHomeCoachAdapter} handles specifics of the NAHealthyHomeCoach device
 *
 * @author Michael Svinth - Initial contribution OH2 version
 *
 */
public class NAHealthyHomeCoachAdapter extends NADeviceAdapter<NAHealthyHomeCoach> {

    public NAHealthyHomeCoachAdapter(NAHealthyHomeCoach device) {
        super(device);
    }

    public NAHealthyHomeCoachAdapter(NAHealthyHomeCoachDataBody homecoachDataBody) {
        super(homecoachDataBody);
    }

    @Override
    public Integer getLastStatusStore() {
        return device.getLastStatusStore();
    }

    @Override
    public NAPlace getPlace() {
        return device.getPlace();
    }

    @Override
    public Integer getWifiStatus() {
        return device.getWifiStatus();
    }

    @Override
    public String getType() {
        return device.getType();
    }

    @Override
    public String getId() {
        return device.getId();
    }

    @Override
    public String getTypeName() {
        return device.getName();
    }

    public NADashboardData getDashboardData() {
        return device.getDashboardData();
    }

    @Override
    protected void populateModules() {
    }
}
