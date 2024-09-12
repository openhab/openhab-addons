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
package org.openhab.binding.modbus.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception for binding configuration exceptions
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class ModbusConfigurationException extends Exception {

    public ModbusConfigurationException(String errmsg) {
        super(errmsg);
    }

    private static final long serialVersionUID = -466597103876477780L;
}
