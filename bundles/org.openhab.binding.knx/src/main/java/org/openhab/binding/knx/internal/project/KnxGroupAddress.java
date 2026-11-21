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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A group address of the project.
 *
 * @param id the full id, {@code P-xxxx-n_GA-n}; links of objects use the part after the last {@code _}
 * @param rawAddress the 16-bit number ETS stores
 * @param address the address written in the project's style, for example {@code 1/2/3}
 * @param name the name, {@code ""} when it has none
 * @param description the description, if any
 * @param datapointTypes the datapoint types in the order the project lists them, empty when it gives none
 * @param secured whether the project holds a Data Secure key for this address (the key itself is not kept)
 * @param rangePath the names of the group ranges from the top down to the one holding this address
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxGroupAddress(String id, int rawAddress, String address, String name, @Nullable String description,
        List<KnxDatapointType> datapointTypes, boolean secured, List<String> rangePath) {

    public KnxGroupAddress {
        datapointTypes = List.copyOf(datapointTypes);
        rangePath = List.copyOf(rangePath);
    }

    /** The first datapoint type, or {@code null} when the project gives none. */
    public @Nullable KnxDatapointType datapointType() {
        return datapointTypes.isEmpty() ? null : datapointTypes.get(0);
    }
}
