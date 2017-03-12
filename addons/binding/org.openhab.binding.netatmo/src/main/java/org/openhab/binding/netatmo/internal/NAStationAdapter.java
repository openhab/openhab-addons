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
import io.swagger.client.model.NAMain;
import io.swagger.client.model.NAPlace;
import io.swagger.client.model.NAStationDataBody;
import io.swagger.client.model.NAStationModule;

/**
 * {@link NAStationAdapter} handles specifics of the NAMain device
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NAStationAdapter extends NADeviceAdapter<NAMain> {

    public NAStationAdapter(NAMain device) {
        super(device);
    }

    public NAStationAdapter(NAStationDataBody stationDataBody) {
        super(stationDataBody);
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
        return device.getStationName();
    }

    public NADashboardData getDashboardData() {
        return device.getDashboardData();
    }

    @Override
    protected void populateModules() {
        for (NAStationModule module : device.getModules()) {
            modules.put(module.getId(), new NAModuleAdapter(module));
        }
    }

}
