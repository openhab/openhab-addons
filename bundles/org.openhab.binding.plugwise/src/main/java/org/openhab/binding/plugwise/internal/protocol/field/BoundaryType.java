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
package org.openhab.binding.plugwise.internal.protocol.field;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The boundary type that a Sense uses for switching.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public enum BoundaryType {

    HUMIDITY(0),
    TEMPERATURE(1),
    NONE(2);

    private final int identifier;

    BoundaryType(int identifier) {
        this.identifier = identifier;
    }

    public int toInt() {
        return identifier;
    }
}
