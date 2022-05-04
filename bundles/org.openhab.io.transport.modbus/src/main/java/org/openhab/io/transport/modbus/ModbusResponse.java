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
