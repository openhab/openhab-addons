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
package org.openhab.binding.openthermgateway.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link MessageType} indicates the type of message received by the OpenTherm Gateway, based
 * on the OpenTherm specification.
 * 
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public enum MessageType {
    READDATA, // 000
    READACK, // 100
    WRITEDATA, // 001
    WRITEACK, // 101
    INVALIDDATA, // 010
    DATAINVALID, // 110
    RESERVED, // 011
    UNKNOWNDATAID // 111
}
