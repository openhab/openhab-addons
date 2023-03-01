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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants;

/**
 * The {@link MeteringUnitsEnum} lists all available digitalSTROM metering units.
 *
 * @author Alexander Betker - Initial contribution
 * @author Michael Ochel - remove W, because it does not exist any more
 * @author Matthias Siegele - remove W, because it does not exist any more
 */
public enum MeteringUnitsEnum {
    WH("Wh"),
    WS("Ws");

    public final String unit;

    private MeteringUnitsEnum(String unit) {
        this.unit = unit;
    }
}
