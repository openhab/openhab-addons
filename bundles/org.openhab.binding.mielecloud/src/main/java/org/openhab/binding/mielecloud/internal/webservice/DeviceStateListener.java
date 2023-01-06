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
package org.openhab.binding.mielecloud.internal.webservice;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mielecloud.internal.webservice.api.ActionsState;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;

/**
 * Listener for the device states.
 *
 * @author Björn Lange and Roland Edelhoff - Initial contribution
 */
@NonNullByDefault
public interface DeviceStateListener {
    /**
     * Invoked when new status information is available for a device.
     *
     * @param deviceState The device state information.
     */
    void onDeviceStateUpdated(DeviceState deviceState);

    /**
     * Invoked when a new process action is available for a device.
     *
     * @param ActionsState The action state information.
     */
    void onProcessActionUpdated(ActionsState actionState);

    /**
     * Invoked when a device got removed from the Miele cloud and no information is available about it.
     *
     * @param deviceIdentifier The identifier of the removed device.
     */
    void onDeviceRemoved(String deviceIdentifier);
}
