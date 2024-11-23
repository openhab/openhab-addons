/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * A {@link BluetoothDiscoveryParticipant} that is registered as a service is picked up by the BluetoothDiscoveryService
 * and can thus contribute {@link DiscoveryResult}s from Bluetooth scans.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Connor Petty - added 'requiresConnection' and 'publishAdditionalResults' methods
 */
@NonNullByDefault
public interface BluetoothDiscoveryParticipant {

    /**
     * Defines the list of thing types that this participant can identify
     *
     * @return a set of thing type UIDs for which results can be created
     */
    Set<ThingTypeUID> getSupportedThingTypeUIDs();

    /**
     * Creates a discovery result for a Bluetooth device
     *
     * @param device the Bluetooth device found on the network
     * @return the according discovery result or <code>null</code>, if device is not
     *         supported by this participant
     */
    @Nullable
    DiscoveryResult createResult(BluetoothDiscoveryDevice device);

    /**
     * Returns the thing UID for a Bluetooth device
     *
     * @param device the Bluetooth device
     * @return a thing UID or <code>null</code>, if the device is not supported by this participant
     */
    @Nullable
    ThingUID getThingUID(BluetoothDiscoveryDevice device);

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
    default boolean requiresConnection(BluetoothDiscoveryDevice device) {
        return false;
    }

    /**
     * Allows participants to perform any post-processing on each DiscoveryResult as well
     * as produce additional DiscoveryResults as they see fit.
     * Additional results can be published using the provided {@code publisher}.
     * Results published in this way will create a new DiscoveryResult and ThingUID
     * using the provided {@link BluetoothAdapter} as the bridge instead.
     * A BluetoothAdapter instance must be provided for any additional results sent to the publisher.
     * <p>
     * Note: Any additional results will not be subject to post-processing.
     *
     * @param result the DiscoveryResult to post-process
     * @param publisher the consumer to publish additional results to.
     */
    default void publishAdditionalResults(DiscoveryResult result,
            BiConsumer<BluetoothAdapter, DiscoveryResult> publisher) {
        // do nothing by default
    }

    /**
     * Overriding this method allows discovery participants to dictate the order in which they should be evaluated
     * relative to other discovery participants. Participants with a lower order value are evaluated first.
     *
     * @return the order of this participant, default 0
     */
    default int order() {
        return 0;
    }
}
