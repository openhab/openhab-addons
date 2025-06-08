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
package org.openhab.binding.sedif.internal.dto;

import java.time.LocalDate;

import org.openhab.binding.sedif.internal.types.SedifException;

/**
 * The {@link SedifState} a base class for Value
 *
 * @author Laurent Arnal - Initial contribution
 */
public class SedifState {
    public boolean hasModifications() {
        return hasModifications;
    }

    private boolean hasModifications;

    private MeterReading meterReading;
    private LocalDate lastIndexDate;

    public LocalDate getLastIndexDate() {
        return lastIndexDate;
    }

    public void setLastIndexDate(LocalDate lastIndexDate) {
        this.lastIndexDate = lastIndexDate;
        hasModifications = true;
    }

    public MeterReading updateMeterReading(MeterReading incomingMeterReading) throws SedifException {
        if (incomingMeterReading == null) {
            return null;
        }

        incomingMeterReading.check();

        if (this.meterReading == null) {
            this.meterReading = new MeterReading();
        }

        return this.meterReading.merge(incomingMeterReading);
    }
}
