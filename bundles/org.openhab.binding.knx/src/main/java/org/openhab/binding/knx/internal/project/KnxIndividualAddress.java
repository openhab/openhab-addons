/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal.project;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The individual address of a device: area, line and device number.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxIndividualAddress(int area, int line, int device) {

    /** The usual text form, {@code 1.1.5}. */
    @Override
    public String toString() {
        return area + "." + line + "." + device;
    }
}
