/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cul.internal.serial;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.cul.internal.CULConfig;
import org.openhab.binding.cul.internal.CULMode;

/**
 * Configuration for serial device handler implementation.
 *
 * @author Patrick Ruckstuhl - Initial contribution
 * @author Johannes Goehr (johgoe) - Migration to OpenHab 3.0
 * @since 1.9.0
 */
@NonNullByDefault
public class CULSerialConfig extends CULConfig {

    private int baudRate;
    private int parityMode;

    public CULSerialConfig(String deviceType, String deviceAddress, CULMode mode, int baudRate, int parityMode) {
        super(deviceType, deviceAddress, mode);
        this.baudRate = baudRate;
        this.parityMode = parityMode;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public Integer getParityMode() {
        return parityMode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), baudRate, parityMode);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CULSerialConfig other = (CULSerialConfig) obj;
        return baudRate == other.baudRate && parityMode == other.parityMode;
    }
}
