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
package org.openhab.binding.upb.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * A device on the UPB network.
 *
 * @author Marcus Better - Initial contribution
 *
 */
@NonNullByDefault
public class UPBDevice {
    private final byte networkId;
    private final byte unitId;

    private DeviceState state = DeviceState.INITIALIZING;

    public enum DeviceState {
        INITIALIZING,
        ALIVE,
        DEAD,
        FAILED
    }

    public UPBDevice(final byte networkId, final byte unitId) {
        this.networkId = networkId;
        this.unitId = unitId;
    }

    public byte getNetworkId() {
        return networkId;
    }

    public byte getUnitId() {
        return unitId;
    }

    public DeviceState getState() {
        return state;
    }

    public void setState(final DeviceState state) {
        this.state = state;
    }
}
