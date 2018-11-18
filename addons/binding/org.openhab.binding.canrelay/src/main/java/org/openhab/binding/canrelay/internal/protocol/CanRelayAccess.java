/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.protocol;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.openhab.binding.canrelay.internal.canbus.CanBusDeviceStatus;

/**
 * This interface describes the CanRelay API. See more at https://github.com/PoJD/can
 * Only supports 1 bridge in the system to be configured
 *
 * @author Lubos Housa - Initial Contribution
 */
@NonNullByDefault
public interface CanRelayAccess {

    /**
     * Initiate the given can relay access by connecting to the underlying device
     *
     * @param portName portName to connect to
     * @return status of the can relay access
     */
    CanBusDeviceStatus connect(String portName);

    /**
     * Disconnect the underlying device and unregister any listeners that may have registered
     */
    void disconnect();

    /**
     * Returns the status of the underlying can bus device
     *
     * @return status of the underlying device
     */
    CanBusDeviceStatus getStatus();

    /**
     * Detect state of all lights of all detected CanRelays (potentially for both floors if found)
     *
     * @return collection of light states. This operation would typically be a blocking operation and may timeout after
     *         some time and return "empty" cache. That means that no CanRelay exists on the CANBUs or that there were
     *         some comm issues on the bus
     */
    Collection<LightState> detectLightStates();

    /**
     * Initiates underlying cache if not done yet
     */
    void initCache();

    /**
     * Refresh the internal caches of lights and returns a collection of changed lights as detected on the CANBUS
     *
     * @return collection of changed lights found on the CANBUS
     */
    Collection<LightState> refreshCache();

    /**
     * Handle a switch command for a given nodeID
     *
     * @param nodeID  nodeID to handle switch for (representing the light)
     * @param command ON to switch this light on, OFF to switch it off
     * @return true if command handled successfully, false otherwise
     */
    boolean handleSwitchCommand(int nodeID, OnOffType command);

    /**
     * Register new listener for changes detected in any CanRelay
     *
     * @param listener new listener to register
     */
    void registerListener(CanRelayChangeListener listener);

    /**
     * Unregister the listener.
     *
     * @param listener listener to unregister.
     */
    void unRegisterListener(CanRelayChangeListener listener);
}
