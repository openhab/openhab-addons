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
package org.openhab.binding.zway.internal.config;

import static org.openhab.binding.zway.internal.ZWayBindingConstants.DEVICE_CONFIG_NODE_ID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link ZWayZWaveDeviceConfiguration} class defines the model for a Z-Wave device configuration.
 *
 * @author Patrick Hecker - Initial contribution
 */
@NonNullByDefault
public class ZWayZWaveDeviceConfiguration {
    private @Nullable Integer nodeId;

    public @Nullable Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(@Nullable Integer nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{ " + DEVICE_CONFIG_NODE_ID + "=" + getNodeId() + "}";
    }
}
