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
package org.openhab.io.homekit.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link HomekitException} class defines an exception for handling HomekitException
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class HomekitException extends Exception {
    private static final long serialVersionUID = -8178227920946730286L;

    public HomekitException(String message) {
        super(message);
    }
}
