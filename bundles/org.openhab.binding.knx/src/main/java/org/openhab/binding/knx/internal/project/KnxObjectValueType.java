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
 * What the project says about the values of a group object: a datapoint type when one is known, and the size
 * from the application program. "Unknown type, n bits" is a normal result.
 *
 * @param datapointType the datapoint type, {@code null} when neither the address nor the object gives one
 * @param sizeBits the object size in bits, {@code null} when not known
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
public record KnxObjectValueType(@Nullable KnxDatapointType datapointType, @Nullable Integer sizeBits) {

    public boolean hasDatapointType() {
        return datapointType != null;
    }
}
