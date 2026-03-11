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
package org.openhab.binding.atmofrance.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AtmoFranceBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class AtmoFranceBindingConstants {

    public static final String BINDING_ID = "atmofrance";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_API = new ThingTypeUID(BINDING_ID, "api");
    public static final ThingTypeUID THING_TYPE_CITY = new ThingTypeUID(BINDING_ID, "city");

    // List of all channels
    public static final String CHANNEL_TIMESTAMP = "timestamp";
    public static final String CHANNEL_COMMENT = "comment";
    public static final String CHANNEL_INDEX = "index";
    public static final String CHANNEL_INDEX_NO2 = "index-no2";
    public static final String CHANNEL_INDEX_SO2 = "index-so2";
    public static final String CHANNEL_INDEX_O3 = "index-o3";
    public static final String CHANNEL_INDEX_PM10 = "index-pm10";
    public static final String CHANNEL_INDEX_PM25 = "index-pm25";
    public static final String CHANNEL_DATE_ECH = "date-ech";
    public static final String CHANNEL_DATE_DIF = "date-dif";
    public static final String CHANNEL_ALDER_CONC = "alder-conc";
    public static final String CHANNEL_BIRCH_CONC = "birch-conc";
    public static final String CHANNEL_OLIVE_CONC = "olive-conc";
    public static final String CHANNEL_GRASSES_CONC = "grasses-conc";
    public static final String CHANNEL_WORMWOOD_CONC = "wormwood-conc";
    public static final String CHANNEL_RAGWEED_CONC = "ragweed-conc";
    public static final String CHANNEL_ALDER_LVL = "alder-level";
    public static final String CHANNEL_BIRCH_LVL = "birch-level";
    public static final String CHANNEL_OLIVE_LVL = "olive-level";
    public static final String CHANNEL_GRASSES_LVL = "grasses-level";
    public static final String CHANNEL_WORMWOOD_LVL = "wormwood-level";
    public static final String CHANNEL_RAGWEED_LVL = "ragweed-level";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_API, THING_TYPE_CITY);
}
