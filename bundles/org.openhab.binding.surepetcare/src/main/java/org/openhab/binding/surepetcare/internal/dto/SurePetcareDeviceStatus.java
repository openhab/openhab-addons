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
package org.openhab.binding.surepetcare.internal.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SurePetcareDeviceStatus} class is used to serialize a JSON object to report the status of a device (e.g.
 * locking mode, LED mode etc.).
 *
 * @author Rene Scherer - Initial contribution
 */
public class SurePetcareDeviceStatus {

    @SerializedName("led_mode")
    public Integer ledModeId;
    @SerializedName("pairing_mode")
    public Integer pairingModeId;
    public Locking locking;
    public Version version;
    public Float battery;
    // learn_mode - unknown type
    public Boolean online;
    public Signal signal = new Signal();

    public SurePetcareDeviceStatus assign(SurePetcareDeviceStatus source) {
        this.ledModeId = source.ledModeId;
        this.pairingModeId = source.pairingModeId;
        this.locking = source.locking;
        this.version = source.version;
        this.battery = source.battery;
        this.online = source.online;
        this.signal = source.signal;
        return this;
    }

    public class Locking {
        @SerializedName("mode")
        public Integer modeId;
    }

    public class Version {
        public class Device {
            public String hardware;
            public String firmware;
        }

        // for Cat flaps only
        public Device device = new Device();
        // for Pet flaps only
        public Device lcd = new Device();
        public Device rf = new Device();
    }

    public class Signal {
        public Float deviceRssi;
        public Float hubRssi;
    }
}
