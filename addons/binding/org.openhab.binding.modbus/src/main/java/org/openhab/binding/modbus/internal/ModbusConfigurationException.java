/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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