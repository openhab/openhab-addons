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
package org.openhab.binding.digitalstrom.internal.lib.structure.devices.deviceparameters.constants;

/**
 * The {@link DeviceParameterClassEnum} lists all digitalSTROM-device parameter classes.
 *
 * @author Alexander Betker - Initial contribution
 * @version digitalSTROM-API 1.14.5
 */
public enum DeviceParameterClassEnum {

    /**
     * communication specific parameters
     */
    CLASS_0(0),

    /**
     * digitalSTROM device specific parameters
     */
    CLASS_1(1),

    /**
     * function specific parameters
     */
    CLASS_3(3),

    /**
     * sensor event table
     */
    CLASS_6(6),

    /**
     * output status
     *
     * possible OffsetParameters:
     * - READ_OUTPUT
     */
    CLASS_64(64),

    /**
     * read scene table
     * use index/offset 0-127
     */
    CLASS_128(128);

    private final int classIndex;

    DeviceParameterClassEnum(int index) {
        this.classIndex = index;
    }

    /**
     * Returns the index of the {@link DeviceParameterClassEnum}.
     *
     * @return index
     */
    public Integer getClassIndex() {
        return this.classIndex;
    }
}
