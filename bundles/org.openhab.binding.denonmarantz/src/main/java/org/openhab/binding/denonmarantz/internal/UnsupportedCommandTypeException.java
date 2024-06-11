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
package org.openhab.binding.denonmarantz.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception thrown when an unsupported command type is sent to a channel.
 *
 * @author Jan-Willem Veldhuis - Initial contribution
 *
 */
@NonNullByDefault
public class UnsupportedCommandTypeException extends Exception {

    private static final long serialVersionUID = 42L;

    public UnsupportedCommandTypeException() {
        super();
    }

    public UnsupportedCommandTypeException(String message) {
        super(message);
    }
}
