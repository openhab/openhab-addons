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

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcMeter;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.MeterType;

/**
 * The {@link NhcMeter2} class represents the meter Niko Home Control communication object. It contains all fields
 * representing a Niko Home Control meter and has methods to receive meter usage information.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NhcMeter2 extends NhcMeter {

    private final String deviceType;
    private final String deviceTechnology;
    private final String deviceModel;

    protected NhcMeter2(String id, String name, MeterType meterType, String deviceType, String deviceTechnology,
            String deviceModel, @Nullable LocalDateTime referenceDate, @Nullable String location,
            NikoHomeControlCommunication nhcComm, ScheduledExecutorService scheduler) {
        super(id, name, meterType, referenceDate, location, nhcComm, scheduler);
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
