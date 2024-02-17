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
package org.openhab.binding.neohub.internal;

import java.math.BigDecimal;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An abstract prototype for wrappers around JSON responses to JSON INFO or
 * GET_LIVE_DATA requests
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public abstract class NeoHubAbstractDeviceData {

    @SuppressWarnings("null")
    @NonNullByDefault
    public abstract static class AbstractRecord {

        public abstract String getDeviceName();

        public abstract BigDecimal getTargetTemperature();

        public abstract BigDecimal getActualTemperature();

        public abstract BigDecimal getFloorTemperature();

        public abstract boolean isStandby();

        public abstract boolean isHeating();

        public abstract boolean isPreHeating();

        public abstract boolean isTimerOn();

        public abstract boolean offline();

        public abstract boolean stateManual();

        public abstract boolean stateAuto();

        public abstract boolean isWindowOpen();

        public abstract boolean isBatteryLow();

        protected BigDecimal safeBigDecimal(@Nullable BigDecimal value) {
            return value != null ? value : BigDecimal.ZERO;
        }
    }

    /**
     * returns the device record corresponding to a given device name
     * 
     * @param deviceName the device name
     * @return its respective device record
     */
    public abstract @Nullable AbstractRecord getDeviceRecord(String deviceName);

    /**
     * @return the full list of device records
     */
    public abstract @Nullable List<? extends AbstractRecord> getDevices();
}
