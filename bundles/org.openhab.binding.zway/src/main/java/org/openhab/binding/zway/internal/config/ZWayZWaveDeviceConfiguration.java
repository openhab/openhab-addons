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
package org.openhab.binding.zway.internal.config;

import static org.openhab.binding.zway.internal.ZWayBindingConstants.DEVICE_CONFIG_NODE_ID;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * The {@link ZWayZWaveDeviceConfiguration} class defines the model for a Z-Wave device configuration.
 *
 * @author Patrick Hecker - Initial contribution
 */
public class ZWayZWaveDeviceConfiguration {
    private Integer nodeId;

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append(DEVICE_CONFIG_NODE_ID, this.getNodeId()).toString();
    }
}
