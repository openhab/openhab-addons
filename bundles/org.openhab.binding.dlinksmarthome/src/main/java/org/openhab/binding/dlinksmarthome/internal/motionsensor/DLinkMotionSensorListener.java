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
