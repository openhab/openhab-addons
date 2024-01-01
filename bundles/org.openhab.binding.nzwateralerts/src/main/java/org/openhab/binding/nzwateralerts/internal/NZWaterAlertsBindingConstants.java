/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nzwateralerts.internal;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nzwateralerts.internal.api.BeWaterWise;
import org.openhab.binding.nzwateralerts.internal.api.NapierCityCouncil;
import org.openhab.binding.nzwateralerts.internal.api.SmartWater;
import org.openhab.binding.nzwateralerts.internal.api.WaterWebService;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link NZWaterAlertsBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Stewart Cossey - Initial contribution
 */
@NonNullByDefault
public class NZWaterAlertsBindingConstants {

    private static final String BINDING_ID = "nzwateralerts";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_WATERALERT = new ThingTypeUID(BINDING_ID, "wateralert");

    // List of all Channel ids
    public static final String CHANNEL_ALERTLEVEL = "alertlevel";

    // List of all supported services
    public static final List<WaterWebService> WATER_WEB_SERVICES = Arrays
            .asList(new WaterWebService[] { new SmartWater(), new BeWaterWise(), new NapierCityCouncil() });
}
