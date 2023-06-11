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
package org.openhab.binding.plugwise.internal.protocol.field;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The motion sensitivity range of a Scan.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public enum Sensitivity {

    HIGH(0x14),
    MEDIUM(0x1E),
    OFF(0xFF);

    private final int value;

    Sensitivity(int value) {
        this.value = value;
    }

    public int toInt() {
        return value;
    }
}
