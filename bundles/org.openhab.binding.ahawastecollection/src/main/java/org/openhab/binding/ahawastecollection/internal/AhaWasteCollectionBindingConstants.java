/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.ahawastecollection.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AhaWasteCollectionBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sönke Küper - Initial contribution
 */
@NonNullByDefault
public class AhaWasteCollectionBindingConstants {

    private static final String BINDING_ID = "ahawastecollection";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SCHEDULE = new ThingTypeUID(BINDING_ID, "collectionSchedule");

    // List of all Channel ids
    public static final String GENERAL_WASTE = "generalWaste";
    public static final String LEIGHTWEIGHT_PACKAGING = "leightweightPackaging";
    public static final String BIOWASTE = "bioWaste";
    public static final String PAPER = "paper";
}
