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
package org.openhab.binding.nikohomecontrol.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.nhc2.NhcEnergyMeter2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NhcEnergyMeter} class represents the energyMeters metering Niko Home Control communication object. It
 * contains all fields representing a Niko Home Control energyMeters meter and has methods to receive energyMeters usage
 * information. A specific implementation is {@link NhcEnergyMeter2}.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public abstract class NhcEnergyMeter {

    private final Logger logger = LoggerFactory.getLogger(NhcEnergyMeter.class);

    protected NikoHomeControlCommunication nhcComm;

    protected String id;
    protected String name;
    protected @Nullable String location;
    // This can be null as long as we do not receive power readings
    protected volatile @Nullable Integer power = null;

    private @Nullable NhcEnergyMeterEvent eventHandler;

    protected NhcEnergyMeter(String id, String name, @Nullable String location, NikoHomeControlCommunication nhcComm) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.nhcComm = nhcComm;
    }

    /**
     * Update all values of the energyMeters meter without touching the energyMeters meter definition (id, name) and
     * without changing the ThingHandler callback.
     *
     * @param power current power consumption/production in W (positive for consumption)
     */
    public void updateState(int power) {
        NhcEnergyMeterEvent handler = eventHandler;
        if (handler != null) {
            logger.debug("update channel for {}", id);
            handler.energyMeterEvent(power);
        }
    }

    /**
     * Method called when energyMeters meter is removed from the Niko Home Control Controller.
     */
    public void energyMeterRemoved() {
        logger.debug("action removed {}, {}", id, name);
        NhcEnergyMeterEvent eventHandler = this.eventHandler;
        if (eventHandler != null) {
            eventHandler.energyMeterRemoved();
            unsetEventHandler();
        }
    }

    /**
     * This method should be called when an object implementing the {@NhcEnergyMeterEvent} interface is initialized.
     * It keeps a record of the event handler in that object so it can be updated when the action receives an update
     * from the Niko Home Control IP-interface.
     *
     * @param eventHandler
     */
    public void setEventHandler(NhcEnergyMeterEvent eventHandler) {
        this.eventHandler = eventHandler;
    }

    /**
     * This method should be called when an object implementing the {@NhcEnergyMeterEvent} interface is disposed.
     * It resets the reference, so no updates go to the handler anymore.
     *
     */
    public void unsetEventHandler() {
        this.eventHandler = null;
    }

    /**
     * Get id of meter.
     *
     * @return id
     */
    public String getId() {
        return id;
    }

    /**
     * Get name of meter.
     *
     * @return energyMeter name
     */
    public String getName() {
        return name;
    }

    /**
     * Set name of meter.
     *
     * @param name meter name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get location name of meter.
     *
     * @return location energyMeter location
     */
    public @Nullable String getLocation() {
        return location;
    }

    /**
     * Set location name of meter.
     *
     * @param location meter location name
     */
    public void setLocation(@Nullable String location) {
        this.location = location;
    }

    /**
     * @return the power in W (positive for consumption, negative for production), return null if no reading received
     *         yet
     */
    public @Nullable Integer getPower() {
        return power;
    }

    /**
     * @param power the power to set in W (positive for consumption, negative for production), null if an empty reading
     *            was received
     */
    public void setPower(@Nullable Integer power) {
        this.power = power;
        NhcEnergyMeterEvent handler = eventHandler;
        if (handler != null) {
            logger.debug("update power channel for {} with {}", id, power);
            handler.energyMeterEvent(power);
        }
    }
}
