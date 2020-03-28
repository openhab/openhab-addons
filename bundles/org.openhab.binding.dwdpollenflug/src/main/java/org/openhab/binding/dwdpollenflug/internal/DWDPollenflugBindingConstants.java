/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.dwdpollenflug.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link DWDPollenflugBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Johannes DerOetzi Ott - Initial contribution
 */
@NonNullByDefault
public class DWDPollenflugBindingConstants {

    private static final String BINDING_ID = "dwdpollenflug";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_REGION = new ThingTypeUID(BINDING_ID, "region");

    public static final String PROPERTY_REGION_ID = "region_id";
    public static final String PROPERTY_REGION_NAME = "region_name";
    public static final String PROPERTY_PARTREGION_ID = "partregion_id";
    public static final String PROPERTY_PARTREGION_NAME = "partregion_name";
}
