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
package org.openhab.binding.renault.internal.api.exceptions;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown while trying to perform action on the My Renault car.
 * 
 * @author Doug Culnane - Initial contribution
 */
@NonNullByDefault
public class RenaultActionException extends Exception {

    private static final long serialVersionUID = 1L;

    public RenaultActionException(String message) {
        super(message);
    }
}
