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
package org.openhab.binding.heos.internal.exception;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Exception to inform the caller that there is no connection to the HEOS system (yet)
 *
 * @author Martin van Wingerden - Initial contribution
 */
@NonNullByDefault
public class HeosNotConnectedException extends IOException {
    public HeosNotConnectedException() {
        super("HEOS not connected");
    }
}
