/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.max.internal.message;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This enumeration represents the different message types provided by the MAX! Cube protocol.
 *
 * @author Andreas Heil - Initial contribution
 */
@NonNullByDefault
public enum MessageType {
    H,
    M,
    C,
    L,
    S,
    N,
    F,
    A
}
