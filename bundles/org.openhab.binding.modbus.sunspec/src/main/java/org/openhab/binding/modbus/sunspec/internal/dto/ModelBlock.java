/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.modbus.sunspec.internal.dto;

/**
 * Descriptor for a model block found on the device
 * This DTO contains only the metadata required to
 * address the block at the modbus register space
 *
 * @author Nagy Attila Gabor - Initial contribution
 */
public class ModelBlock {

    /**
     * Base address of this block in 16bit words
     */
    public int address;

    /**
     * Length of this block in 16bit words
     */
    public int length;

    /**
     * Module identifier
     */
    public int moduleID;

    @Override
    public String toString() {
        return String.format("ModelBlock type=%d address=%d length=%d", moduleID, address, length);
    }
}
