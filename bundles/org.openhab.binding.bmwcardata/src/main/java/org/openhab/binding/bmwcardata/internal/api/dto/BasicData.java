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
 * Static vehicle data as returned by {@code /customers/vehicles/{vin}/basicData}. Only the fields used as Thing
 * properties are mapped.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class BasicData {
    public @Nullable String brand;
    public @Nullable String modelName;
    public @Nullable String series;
    public @Nullable String driveTrain;
    public @Nullable String propulsionType;
    public @Nullable String colourDescription;
    public @Nullable String constructionDate;
    // false for vehicles that cannot send data to CarData
    public @Nullable Boolean isTelematicsCapable;
}
