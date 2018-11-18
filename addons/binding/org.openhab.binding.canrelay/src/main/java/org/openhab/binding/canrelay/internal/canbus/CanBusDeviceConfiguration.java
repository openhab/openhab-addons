/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.canrelay.internal.canbus;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Wrapper for configuration needed for a specific CANBusDevice
 *
 * @author Lubos Housa - Initial contribution
 */
@NonNullByDefault
public class CanBusDeviceConfiguration {

    private final int baudRate;
    private final int dataBits;
    private final int stopBits;
    private final int parity;

    private CanBusDeviceConfiguration(int baudRate, int dataBits, int stopBits, int parity) {
        this.baudRate = baudRate;
        this.dataBits = dataBits;
        this.stopBits = stopBits;
        this.parity = parity;
    }

    public int getBaudRate() {
        return baudRate;
    }

    public int getDataBits() {
        return dataBits;
    }

    public int getStopBits() {
        return stopBits;
    }

    public int getParity() {
        return parity;
    }

    public static class Builder {
        private int baudRate;
        private int dataBits;
        private int stopBits;
        private int parity;

        public Builder baudRate(int baudRate) {
            this.baudRate = baudRate;
            return this;
        }

        public Builder dataBits(int dataBits) {
            this.dataBits = dataBits;
            return this;
        }

        public Builder stopBits(int stopBits) {
            this.stopBits = stopBits;
            return this;
        }

        public Builder parity(int parity) {
            this.parity = parity;
            return this;
        }

        /**
         * Build the CanBusDeviceConfiguration using this builder
         *
         * @return new instance of CanBusDeviceConfiguration initiated using this builder
         */
        public CanBusDeviceConfiguration build() {
            return new CanBusDeviceConfiguration(this.baudRate, this.dataBits, this.stopBits, this.parity);
        }
    }

    /**
     * Create new builder to instantiate CanBusDeviceConfiguration instances
     */
    public static Builder newBuilder() {
        return new Builder();
    }
}
