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
import org.eclipse.jdt.annotation.Nullable;

/**
 * A channel address as the binding has it configured, in the words of the project check.
 *
 * @param rawAddress the 16-bit group address
 * @param datapointType the effective data type as {@code main.sub} or {@code main}, {@code null} for none; text
 *            that does not parse counts as none
 * @param read whether the address is marked {@code <} (read at startup)
 * @param main whether it is the main address of its configuration, else a listening address
 * @param status whether it is the first listening address as written, where the wizard puts the status address
 * @param label where the address comes from, for the report
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxConfiguredAddress(int rawAddress, @Nullable String datapointType, boolean read, boolean main,
        boolean status, String label) {

    public KnxConfiguredAddress(int rawAddress, @Nullable String datapointType, boolean read, boolean main,
            String label) {
        this(rawAddress, datapointType, read, main, false, label);
    }
}
