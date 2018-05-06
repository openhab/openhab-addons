/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
