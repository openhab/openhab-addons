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
package org.openhab.binding.bmwcardata.internal.api.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A vehicle mapped to the customer account, as returned by {@code /customers/vehicles/mappings}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class VehicleMapping {
    public static final String MAPPING_TYPE_PRIMARY = "PRIMARY";

    public @Nullable String vin;
    public @Nullable String mappingType;
    public @Nullable String mappedSince;
}
