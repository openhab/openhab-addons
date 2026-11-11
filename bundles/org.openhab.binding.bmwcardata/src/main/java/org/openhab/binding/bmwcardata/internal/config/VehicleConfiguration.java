/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.bmwcardata.internal.config;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VehicleConfiguration} class contains the configuration parameters of a vehicle.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class VehicleConfiguration {
    public String vin = "";
    /** Create channels for descriptors without predefined channel. */
    public boolean dynamicChannels = true;
}
