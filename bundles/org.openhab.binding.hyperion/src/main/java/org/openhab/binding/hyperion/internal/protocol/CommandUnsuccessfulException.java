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
package org.openhab.binding.hyperion.internal.protocol;

/**
 * The {@link CommandUnsuccessfulException} should be raised when the Hyperion server
 * rejects a command.
 *
 * @author Daniel Walters - Initial contribution
 */
public class CommandUnsuccessfulException extends Exception {

    private static final long serialVersionUID = 1421923610566223857L;

    public CommandUnsuccessfulException(String message) {
        super(message);
    }
}
