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
package org.openhab.io.transport.modbus;

/**
 * Modbus read function codes supported by this transport
 *
 * @author Sami Salonen - Initial contribution
 */
public enum ModbusReadFunctionCode {
    READ_COILS,
    READ_INPUT_DISCRETES,
    READ_MULTIPLE_REGISTERS,
    READ_INPUT_REGISTERS
}
