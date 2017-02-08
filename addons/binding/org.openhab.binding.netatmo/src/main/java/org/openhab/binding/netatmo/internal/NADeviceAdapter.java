/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal;

import java.util.HashMap;
import java.util.Map;

import io.swagger.client.model.NAPlace;
import io.swagger.client.model.NAStationDataBody;
import io.swagger.client.model.NAThermostatDataBody;
import io.swagger.client.model.NAUserAdministrative;

/**
 * {@link NADeviceAdapter} is designed to handle common parts of distinct devices
 * within Netatmo Device families
 * Should be able to disappear once inheritance is operational in swagger definition file
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public abstract class NADeviceAdapter<DeviceClass> {
    private final NAUserAdministrative userAdministrative;
    protected Map<String, NAModuleAdapter> modules = new HashMap<String, NAModuleAdapter>();
    protected DeviceClass device;

    public NADeviceAdapter(DeviceClass device) {
        if (device != null) {
            this.device = device;
            this.userAdministrative = null;
        } else {
            throw new IllegalArgumentException("device can not be null");
        }
    }

    @SuppressWarnings("unchecked")
    public NADeviceAdapter(NAStationDataBody stationDataBody) {
        if (stationDataBody != null) {
            this.userAdministrative = stationDataBody.getUser().getAdministrative();
            this.device = (DeviceClass) stationDataBody.getDevices().get(0);
        } else {
            throw new IllegalArgumentException("stationDataBody can not be null");
        }
    }

    @SuppressWarnings("unchecked")
    public NADeviceAdapter(NAThermostatDataBody thermostatDataBody) {
        if (thermostatDataBody != null) {
            this.userAdministrative = thermostatDataBody.getUser().getAdministrative();
            this.device = (DeviceClass) thermostatDataBody.getDevices().get(0);
        } else {
            throw new IllegalArgumentException("thermostatDataBody can not be null");
        }
    }

    abstract public Integer getLastStatusStore();

    abstract public NAPlace getPlace();

    abstract public Integer getWifiStatus();

    abstract public String getType();

    abstract public String getId();

    abstract public String getTypeName();

    public NAUserAdministrative getUserAdministrative() {
        return userAdministrative;
    }

    // I transform the original list to a map that will be more convenient to handle
    public Map<String, NAModuleAdapter> getModules() {
        modules.clear();
        populateModules();
        return modules;
    }

    abstract protected void populateModules();

}
