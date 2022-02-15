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
package org.openhab.binding.modbus.sungrow.internal.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class contains information parsed from the Common Model block
 *
 * @author Nagy Attila Gabor - Initial contribution
 * @author Ferdinand Schwenk - reused for sungrow bundle
 *
 */
@NonNullByDefault
public class CommonModelBlock {

    /**
     * Value = 0x0001. Uniquely identifies this as a Sungrow Common Model Block
     */
    public int sungrowDID = 0x0001;

    /**
     * Length of block in 16-bit registers
     */
    public int length = 0;

    /**
     * Modbus unit ID - this is a unique identifier of the device
     */
    public int deviceAddress = 0;

    // Manufacturer specific values
    public String manufacturer = "";
    public String model = "";
    public String version = "";
    public String serialNumber = "";

    @Override
    public String toString() {
        return this.getClass().getCanonicalName() + "[\n" + "  deviceAddress=" + this.deviceAddress + "\n" + "  length="
                + this.length + "\n" + "  manufacturer=" + this.manufacturer + "\n" + "  model=" + this.model + "\n"
                + "  serialNumber=" + this.serialNumber + "\n" + "  sungrowDID=" + this.sungrowDID + "\n" + "  version="
                + this.version + "\n" + "]";
    }
}
