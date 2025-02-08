/*
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
package org.openhab.binding.modbus.lambda.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;

/*
 * Thrown when the lambda-heatpump handler sees an error.
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 */
@SuppressWarnings("serial")
@NonNullByDefault
public class LambdaException extends Exception {

    public LambdaException() {
    }

    public LambdaException(String message) {
        super(message);
    }
}
