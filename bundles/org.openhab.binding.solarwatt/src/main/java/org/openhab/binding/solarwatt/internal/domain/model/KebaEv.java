/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.solarwatt.internal.domain.model;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.solarwatt.internal.domain.dto.DeviceDTO;

/**
 * Specialised class for the Keba Wallbox.
 *
 * No additional fields where found.
 * com.kiwigrid.devices.keba.ev.KebaEv=[]
 * 
 * @author Sven Carstens - Initial contribution
 */
@NonNullByDefault
public class KebaEv extends EVStation {
    public static final String SOLAR_WATT_CLASSNAME = "com.kiwigrid.devices.keba.ev.KebaEv";

    public KebaEv(DeviceDTO deviceDTO) {
        super(deviceDTO);
    }

    @Override
    protected String getSolarWattLabel() {
        return "KebaEVStation";
    }
}
