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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Static vehicle data as returned by {@code /customers/vehicles/{vin}/basicData}. Only the fields used as Thing
 * properties or in the binding logic are mapped.
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
    public @Nullable Boolean hasSunRoof;
    public @Nullable Boolean hasNavi;
    // not documented by BMW; read as no telematics only when explicitly false
    public @Nullable Boolean isTelematicsCapable;
    // shown as properties, e.g. to decide later which channels apply to the vehicle
    public @Nullable String modelKey;
    public @Nullable String seriesDevt;
    public @Nullable String bodyType;
    public @Nullable Integer numberOfDoors;
    public @Nullable String steering;
    public @Nullable String engine;
    public @Nullable String headUnit;
    public @Nullable String puStep;
    public @Nullable List<String> chargingModes;
    public @Nullable String hvsMaxEnergyAbsolute;
    public @Nullable String reessNominalCapacityGross;
    public @Nullable String simStatus;
    public @Nullable String countryCode;
}
