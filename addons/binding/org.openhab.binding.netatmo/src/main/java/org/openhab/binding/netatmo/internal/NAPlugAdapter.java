/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo.internal;

import io.swagger.client.model.NAPlace;
import io.swagger.client.model.NAPlug;
import io.swagger.client.model.NAThermostat;
import io.swagger.client.model.NAThermostatDataBody;

/**
 * {@link NAPlugAdapter} handles specifics of the NAPlug device
 *
 * @author Gaël L'hopital - Initial contribution OH2 version
 *
 */
public class NAPlugAdapter extends NADeviceAdapter<NAPlug> {

    public NAPlugAdapter(NAPlug device) {
        super(device);
    }

    public NAPlugAdapter(NAThermostatDataBody thermostatDataBody) {
        super(thermostatDataBody);
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

    @Override
    protected void populateModules() {
        for (NAThermostat module : device.getModules()) {
            modules.put(module.getId(), new NAModuleAdapter(module));
        }
    }

}
