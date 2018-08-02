/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Write request for coils
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public interface ModbusWriteCoilRequestBlueprint extends ModbusWriteRequestBlueprint {

    /**
     * Coil data to write
     *
     * @return coils to write
     */
    public BitArray getCoils();

    @Override
    public default void accept(ModbusWriteRequestBlueprintVisitor visitor) {
        visitor.visit(this);
    }
}
