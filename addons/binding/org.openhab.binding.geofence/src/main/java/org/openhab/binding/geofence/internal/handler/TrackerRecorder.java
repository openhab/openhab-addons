/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.geofence.internal.handler;

import org.openhab.binding.geofence.internal.message.AbstractBaseMessage;
import org.openhab.binding.geofence.internal.message.Location;
import org.openhab.binding.geofence.internal.message.Transition;

import java.util.List;

/**
 * Tracker recorder interface implemented by device handlers.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public interface TrackerRecorder {
    /**
     * Update configuration with regions defined in tracker application.
     *
     * @param regionName Region name.
     */
    void maintainExternalRegion(String regionName);

    /**
     * Update configuration with tracker remote IP address.
     *
     * @param ipAddress Device IP address
     */
    void updateDeviceIPAddress(String ipAddress);

    /**
     * Device location update.
     *
     * @param lm Location message.
     */
    void updateLocation(Location lm);

    /**
     * Device transition update.
     *
     * @param tm Transition message
     */
    void doTransition(Transition tm);

    /**
     * Get notification messages returned to the device.
     *
     * @return List of Location and transition messages.
     */
    List<AbstractBaseMessage> getNotifications();
}
