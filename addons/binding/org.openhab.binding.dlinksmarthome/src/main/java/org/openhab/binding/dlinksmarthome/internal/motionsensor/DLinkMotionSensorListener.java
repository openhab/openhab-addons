/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.dlinksmarthome.internal.motionsensor;

import org.openhab.binding.dlinksmarthome.internal.motionsensor.DLinkMotionSensorCommunication.DeviceStatus;

/**
 * The {@link DLinkMotionSensorListener} provides callbacks for motion detection
 * and device status changes.
 *
 * @author Mike Major - Initial contribution
 */
public interface DLinkMotionSensorListener {

    /**
     * Callback to indicate motion has been detected
     */
    void motionDetected();

    /**
     * Callback to indicate a change in the device status
     */
    void sensorStatus(final DeviceStatus status);
}
