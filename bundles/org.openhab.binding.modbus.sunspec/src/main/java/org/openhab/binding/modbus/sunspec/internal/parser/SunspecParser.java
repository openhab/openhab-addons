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
package org.openhab.binding.modbus.sunspec.internal.parser;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;

/**
 * General interface for sunspec parsers
 *
 * Parsers are responsible to take the raw register array
 * that was read from the device and to parse them into a SunSpecMessageBlock
 * They should parse all reasonable fields into separate properties
 * in the message block.
 *
 * Fields with unsupported values should be parsed as null values.
 *
 * In no way should a parser handle value scaling or device specific
 * workarounds. These should be done in the handler.
 *
 * @author Nagy Attila Gabor - Initial contribution
 *
 */
@NonNullByDefault
public interface SunspecParser<T> {

    /**
     * This method should parser an incoming register array and
     * return a not-null sunspec block
     */
    T parse(ModbusRegisterArray raw);
}
