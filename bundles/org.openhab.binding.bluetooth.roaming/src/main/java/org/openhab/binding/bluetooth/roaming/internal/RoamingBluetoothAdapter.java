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
package org.openhab.binding.bluetooth.roaming.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.bluetooth.BluetoothAdapter;
import org.openhab.core.thing.ThingUID;

/**
 * The {@link RoamingBluetoothAdapter} adds additional functionality to {@link BluetoothAdapter}
 * but more importantly serves as a tagging interface to expose it as an OSGI service.
 *
 * @author Connor Petty - Initial contribution
 */
@NonNullByDefault
public interface RoamingBluetoothAdapter extends BluetoothAdapter {

    void addBluetoothAdapter(BluetoothAdapter adapter);

    void removeBluetoothAdapter(BluetoothAdapter adapter);

    boolean isDiscoveryEnabled();

    boolean isRoamingMember(ThingUID adapterUID);
}
