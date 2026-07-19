/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.atv.internal.client.capability;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.atv.internal.client.dto.OutputDevice;

/**
 * Listener interface for audio updates.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface AudioListener {

    /**
     * Informs that the device volume was updated.
     *
     * @param oldLevel previous volume level in percent [0.0-100.0]
     * @param newLevel new volume level in percent [0.0-100.0]
     */
    void volumeUpdate(double oldLevel, double newLevel);

    /**
     * Informs that the volume of an output device was updated.
     *
     * @param outputDevice output device that changed
     * @param oldLevel previous volume level in percent [0.0-100.0]
     * @param newLevel new volume level in percent [0.0-100.0]
     */
    default void volumeDeviceUpdate(OutputDevice outputDevice, double oldLevel, double newLevel) {
    }

    /**
     * Informs that the output devices were updated.
     *
     * @param oldDevices previous output devices
     * @param newDevices new output devices
     */
    void outputDevicesUpdate(List<OutputDevice> oldDevices, List<OutputDevice> newDevices);
}
