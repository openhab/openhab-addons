/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.sbus.handler.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

import ro.ciprianpascu.sbus.Sbus;

/**
 * The {@link SbusDeviceConfig} class contains fields mapping thing configuration parameters.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public class SbusDeviceConfig {
    /**
     * The ID of the Sbus device
     */
    public int id = Sbus.DEFAULT_UNIT_ID;

    /**
     * The subnet ID for Sbus communication
     */
    public int subnetId = Sbus.DEFAULT_SUBNET_ID;

    /**
     * Refresh interval in seconds
     */
    public int refresh = 30; // Default value from thing-types.xml
}
