/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc2;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcAlarm;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;

/**
 * The {@link NhcAlarm2} class represents the alarm Niko Home Control II communication object. It contains all fields
 * representing a Niko Home Control alarm and has methods to arm/disarm the alarm in Niko Home Control and receive
 * alarms.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NhcAlarm2 extends NhcAlarm {

    private final String deviceType;
    private final String deviceTechnology;
    private final String deviceModel;

    NhcAlarm2(String id, String name, String deviceType, String deviceTechnology, String deviceModel,
            @Nullable String location, NikoHomeControlCommunication nhcComm) {
        super(id, name, location, nhcComm);
        this.deviceType = deviceType;
        this.deviceTechnology = deviceTechnology;
        this.deviceModel = deviceModel;
    }

    /**
     * @return type as returned from Niko Home Control
     */
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * @return technology as returned from Niko Home Control
     */
    public String getDeviceTechnology() {
        return deviceTechnology;
    }

    /**
     * @return model as returned from Niko Home Control
     */
    public String getDeviceModel() {
        return deviceModel;
    }
}
