/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.internal.device;

/**
 * MAX! wall mounted thermostat.
 *
 * @author Andreas Heil (info@aheil.de) - Initial contribution
 */
public class WallMountedThermostat extends HeatingThermostat {

    public WallMountedThermostat(DeviceConfiguration c) {
        super(c);
    }

    @Override
    public DeviceType getType() {
        return DeviceType.WallMountedThermostat;
    }
}
