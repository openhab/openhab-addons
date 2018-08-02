/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
