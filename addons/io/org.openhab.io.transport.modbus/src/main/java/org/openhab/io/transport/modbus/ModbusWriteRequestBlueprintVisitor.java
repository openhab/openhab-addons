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
 * <p>
 * ModbusWriteRequestBlueprintVisitor interface.
 * </p>
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public interface ModbusWriteRequestBlueprintVisitor {

    /**
     * Visit request writing coil data
     *
     * @param blueprint
     */
    public void visit(ModbusWriteCoilRequestBlueprint blueprint);

    /**
     * Visit request writing register data
     *
     * @param blueprint
     */
    public void visit(ModbusWriteRegisterRequestBlueprint blueprint);

}
