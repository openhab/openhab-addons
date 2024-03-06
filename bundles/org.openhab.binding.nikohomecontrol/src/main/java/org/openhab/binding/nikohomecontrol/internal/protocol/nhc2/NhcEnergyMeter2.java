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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcEnergyMeter;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;

/**
 * The {@link NhcEnergyMeter} class represents the energyMeters metering Niko Home Control communication object. It
 * contains all fields representing a Niko Home Control energyMeters meter and has methods to receive energyMeters usage
 * information.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NhcEnergyMeter2 extends NhcEnergyMeter {

    private ScheduledExecutorService scheduler;
    private volatile @Nullable ScheduledFuture<?> restartTimer;

    private String deviceType;
    private String deviceTechnology;
    private String deviceModel;

    protected NhcEnergyMeter2(String id, String name, String deviceType, String deviceTechnology, String deviceModel,
            @Nullable String location, NikoHomeControlCommunication nhcComm, ScheduledExecutorService scheduler) {
        super(id, name, location, nhcComm);
        this.deviceType = deviceType;
        this.deviceTechnology = deviceTechnology;
        this.deviceModel = deviceModel;

        this.scheduler = scheduler;
    }

    /**
     * Start the flow from energy information from the energy meter. The Niko Home Control energy meter will send power
     * information every 2s for 30s. This method will retrigger every 25s to make sure the information continues
     * flowing. If the information is no longer required, make sure to use the {@link stopEnergyMeter} method to stop
     * the flow of information.
     *
     * @param topic topic the start event will have to be sent to every 25s
     * @param gsonMessage content of message
     */
    public void startEnergyMeter(String topic, String gsonMessage) {
        stopEnergyMeter();
        restartTimer = scheduler.scheduleWithFixedDelay(() -> {
            ((NikoHomeControlCommunication2) nhcComm).executeEnergyMeter(topic, gsonMessage);
        }, 0, 25, TimeUnit.SECONDS);
    }

    /**
     * Cancel receiving energy information from the controller. We therefore stop the automatic retriggering of the
     * subscription, see {@link startEnergyMeter}.
     */
    public void stopEnergyMeter() {
        ScheduledFuture<?> timer = restartTimer;
        if (timer != null) {
            timer.cancel(true);
            restartTimer = null;
        }
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
