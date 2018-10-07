/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Signals that {@link ModbusEndpointThingHandler} is not properly initialized yet, and the requested operation cannot
 * be completed.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class EndpointNotInitializedException extends Exception {

    private static final long serialVersionUID = -6721646244844348903L;

}
