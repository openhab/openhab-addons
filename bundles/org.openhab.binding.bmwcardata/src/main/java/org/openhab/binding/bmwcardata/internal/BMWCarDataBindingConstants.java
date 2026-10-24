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
package org.openhab.binding.bmwcardata.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BMWCarDataBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class BMWCarDataBindingConstants {

    public static final String BINDING_ID = "bmwcardata";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_VEHICLE = new ThingTypeUID(BINDING_ID, "vehicle");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_VEHICLE);

    // Configuration and property names
    public static final String CONFIG_VIN = "vin";
    public static final String PROPERTY_BRAND = "brand";
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_SERIES = "series";
    public static final String PROPERTY_DRIVE_TRAIN = "driveTrain";
    public static final String PROPERTY_PROPULSION_TYPE = "propulsionType";
    public static final String PROPERTY_COLOR = "color";
    public static final String PROPERTY_CONSTRUCTION_DATE = "constructionDate";
    public static final String PROPERTY_HAS_SUNROOF = "hasSunroof";
    public static final String PROPERTY_HAS_NAVIGATION = "hasNavigation";

    // Channel groups and derived channels; descriptor-backed channels are defined in VehicleChannel
    public static final String GROUP_STATUS = "status";
    public static final String GROUP_RANGE = "range";
    public static final String GROUP_CHARGE = "charge";
    public static final String GROUP_NAVIGATION = "navigation";
    public static final String GROUP_CLIMATE = "climate";
    public static final String GROUP_TRIP = "trip";
    public static final String GROUP_TYRES = "tyres";
    public static final String GROUP_SECURITY = "security";
    public static final String GROUP_BATTERY = "battery";
    public static final String GROUP_SERVICE = "service";
    public static final String GROUP_STATISTICS = "statistics";
    public static final String GROUP_OTHER = "other";
    public static final String CHANNEL_LAST_UPDATE = GROUP_STATUS + "#last-update";
    public static final String CHANNEL_GPS = GROUP_NAVIGATION + "#gps";
}
