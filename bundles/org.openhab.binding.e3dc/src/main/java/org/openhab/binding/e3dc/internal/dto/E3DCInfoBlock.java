/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.e3dc.internal.dto;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;

/**
 * The {@link E3DCInfoBlock} Data object for E3DC Info Block
 *
 * @author Bernd Weymann - Initial contribution
 */
public class E3DCInfoBlock {
    public StringType modbusId;
    public StringType modbusVersion;
    public DecimalType supportedRegisters;
    public StringType manufacturer;
    public StringType modelName;
    public StringType serialNumber;
    public StringType firmware;

    public void setSupportedRegisters(int sr) {
        supportedRegisters = new DecimalType(sr);
    }
}
