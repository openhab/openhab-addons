/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus.sunspec.internal.parser;

import org.eclipse.jdt.annotation.NonNull;
import org.openhab.binding.modbus.sunspec.internal.block.AbstractSunSpecMessageBlock;
import org.openhab.io.transport.modbus.ModbusRegisterArray;

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
public interface SunspecParser<T extends AbstractSunSpecMessageBlock> {

    /**
     * This method should parser an incoming register array and
     * return a not-null sunspec block
     */
    public @NonNull T parse(@NonNull ModbusRegisterArray raw);
}
