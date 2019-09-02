/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.bluetooth.discovery;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.bluetooth.BluetoothDevice;

/**
 * A {@link BluetoothDiscoveryParticipant} that is registered as a service is picked up by the BluetoothDiscoveryService
 * and can thus contribute {@link DiscoveryResult}s from Bluetooth scans.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
@NonNullByDefault
public interface BluetoothDiscoveryParticipant {

    /**
     * Defines the list of thing types that this participant can identify
     *
     * @return a set of thing type UIDs for which results can be created
     */
    public Set<ThingTypeUID> getSupportedThingTypeUIDs();

    /**
     * Creates a discovery result for a Bluetooth device
     *
     * @param device the Bluetooth device found on the network
     * @return the according discovery result or <code>null</code>, if device is not
     *         supported by this participant
     */
    @Nullable
    public DiscoveryResult createResult(BluetoothDevice device);

    /**
     * Returns the thing UID for a Bluetooth device
     *
     * @param device the Bluetooth device
     * @return a thing UID or <code>null</code>, if the device is not supported by this participant
     */
    @Nullable
    public ThingUID getThingUID(BluetoothDevice device);
}
