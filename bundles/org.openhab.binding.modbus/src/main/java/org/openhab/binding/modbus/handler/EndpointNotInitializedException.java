/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
