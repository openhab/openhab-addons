/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.max.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.cul.max.internal.messages.constants.MaxCulDevice;
import org.openhab.core.thing.Bridge;

/**
 * The {@link DevicePairingListener} is notified when a new device tries pairing
 *
 * @author Johannes Goehr (johgoe) - Initial contribution
 */
@NonNullByDefault
public interface DevicePairingListener {

    /**
     * This method is called whenever a device tries pairing.
     *
     * @param bridge The MAX! CUL bridge the added device was connected to
     * @param rfAddress The device rfAddress which is added
     * @param deviceType The device type which is added
     * @param serialNumber The device serialNumber which is added
     */
    void onDeviceAdded(Bridge bridge, String rfAddress, MaxCulDevice deviceType, String serialNumber);
}
