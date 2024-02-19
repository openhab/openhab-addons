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
package org.openhab.binding.boschshc.internal.devices;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Object representation of a configuration for a device with two child devices
 * such as the Light Control II.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class BoschSHCConfigurationWithTwoChildDevices extends BoschSHCConfiguration {

    /**
     * ID of the first logical child device.
     */
    public @Nullable String childId1;

    /**
     * ID of the second logical child device.
     */
    public @Nullable String childId2;
}
