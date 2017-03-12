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
import io.swagger.client.model.NAStationModule;
import io.swagger.client.model.NAThermostat;

/**
 * {@link NAModuleAdapter} is designed to handle common parts of distinct module type
 * within Netatmo Device families
 * Should be able to disappear once inheritance is operational in swagger definition file
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NAModuleAdapter {
    NAStationModule stationModule = null;
    NAThermostat thermostat = null;

    public NAModuleAdapter(NAStationModule module) {
        if (module != null) {
            this.stationModule = module;
        } else {
            throw new IllegalArgumentException("module can not be null");
        }
    }

    public NAModuleAdapter(NAThermostat module) {
        if (module != null) {
            this.thermostat = module;
        } else {
            throw new IllegalArgumentException("module can not be null");
        }
    }

    public Integer getBatteryVp() {
        if (stationModule != null) {
            return stationModule.getBatteryVp();
        } else {
            return thermostat.getBatteryVp();
        }
    }

    public Integer getLastMessage() {
        if (stationModule != null) {
            return stationModule.getLastMessage();
        } else {
            return thermostat.getLastMessage();
        }
    }

    public Integer getRfStatus() {
        if (stationModule != null) {
            return stationModule.getRfStatus();
        } else {
            return thermostat.getRfStatus();
        }
    }

    public NAThermostat getThermostat() {
        return this.thermostat;
    }

    public NAStationModule getStationModule() {
        return this.stationModule;
    }

    public String getType() {
        if (stationModule != null) {
            return stationModule.getType();
        } else {
            return thermostat.getType();
        }
    }

    public String getId() {
        if (stationModule != null) {
            return stationModule.getId();
        } else {
            return thermostat.getId();
        }
    }

    public String getModuleName() {
        if (stationModule != null) {
            return stationModule.getModuleName();
        } else {
            return thermostat.getModuleName();
        }
    }

    public NADashboardData getDashboardData() {
        return stationModule.getDashboardData();
    }

}
