/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.toon.internal.api;

/**
 * The {@link PowerUsage} class defines the json object as received by the api.
 *
 * @author Jorg de Jong - Initial contribution
 */
public class PowerUsage {
    private Long meterReading;
    private Long meterReadingLow;
    private Long value;

    public Long getMeterReading() {
        return meterReading;
    }

    public void setMeterReading(Long meterReading) {
        this.meterReading = meterReading;
    }

    public Long getMeterReadingLow() {
        return meterReadingLow;
    }

    public void setMeterReadingLow(Long meterReadingLow) {
        this.meterReadingLow = meterReadingLow;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }
}
