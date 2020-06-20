/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

/**
 * A {@link BluetoothDiscoveryParticipant} that is registered as a service is picked up by the BluetoothDiscoveryService
 * and can thus contribute {@link DiscoveryResult}s from Bluetooth scans.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Connor Petty - added 'requiresConnection' method
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
    public @Nullable DiscoveryResult createResult(BluetoothDiscoveryDevice device);

    /**
     * Returns the thing UID for a Bluetooth device
     *
     * @param device the Bluetooth device
     * @return a thing UID or <code>null</code>, if the device is not supported by this participant
     */
    public @Nullable ThingUID getThingUID(BluetoothDiscoveryDevice device);

    /**
     * Returns true if this participant requires the device to be connected before it can produce a
     * DiscoveryResult (or null) from {@link createResult(BluetoothDevice)}.
     * <p>
     * Implementors should only return 'true' conservatively, and make sure to return 'false' in circumstances where a
     * 'null' result would be guaranteed from {@link createResult(BluetoothDevice)} even if a connection was available
     * (e.g. the advertised manufacturerId already mismatches).
     * <p>
     * In general, returning 'true' is equivalent to saying <i>"the device might match, but I need a connection to
     * make sure"</i>.
     *
     * @param device the Bluetooth device
     * @return true if a connection is required before calling {@link createResult(BluetoothDevice)}
     */
    public default boolean requiresConnection(BluetoothDiscoveryDevice device) {
        return false;
    }
}
