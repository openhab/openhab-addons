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
 * Minimal representation of a modbus response.
 *
 * Only function code is exposed, which allows detecting MODBUS exception codes from normal codes.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public interface ModbusResponse {

    /**
     * Function code of the response.
     *
     * Note that in case of Slave responding with Modbus exception response, the response
     * function code might differ from request function code
     *
     * @return function code of the response
     */
    public int getFunctionCode();

}
