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
package org.openhab.binding.sleepiq.internal.api.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link FoundationOutletOperation} represents the controls on a foundation outlet.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public enum FoundationOutletOperation {
    OFF(0),
    ON(1);

    private final int outletControl;

    FoundationOutletOperation(final int outletControl) {
        this.outletControl = outletControl;
    }

    public int value() {
        return outletControl;
    }

    public static FoundationOutletOperation forValue(int value) {
        for (FoundationOutletOperation s : FoundationOutletOperation.values()) {
            if (s.outletControl == value) {
                return s;
            }
        }
        throw new IllegalArgumentException("Invalid outletControl: " + value);
    }

    @Override
    public String toString() {
        return String.valueOf(outletControl);
    }
}
