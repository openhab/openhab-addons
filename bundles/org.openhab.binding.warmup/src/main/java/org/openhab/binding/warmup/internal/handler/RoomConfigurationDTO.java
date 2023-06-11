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
package org.openhab.binding.warmup.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link RoomConfigurationDTO} class contains fields mapping thing configuration parameters for the Warmup Room.
 *
 * @author James Melville - Initial contribution
 */
@NonNullByDefault
public class RoomConfigurationDTO {

    private String serialNumber = "";
    private int overrideDuration = 60;

    public String getSerialNumber() {
        return serialNumber;
    }

    public int getOverrideDuration() {
        return overrideDuration;
    }
}
