/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcCarCharger;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;

/**
 * The {@link NhcCarCharger2} class represents the charging station Niko Home Control II communication object. It
 * contains all fields representing a Niko Home Control charging station and has methods to control car charging in Niko
 * Home Control and receive charging station updates.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NhcCarCharger2 extends NhcCarCharger {

    private final String deviceType;
    private final String deviceTechnology;
    private final String deviceModel;

    NhcCarCharger2(String id, String name, String deviceType, String deviceTechnology, String deviceModel,
            @Nullable String location, NikoHomeControlCommunication nhcComm, ScheduledExecutorService scheduler) {
        super(id, name, location, nhcComm, scheduler);
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
