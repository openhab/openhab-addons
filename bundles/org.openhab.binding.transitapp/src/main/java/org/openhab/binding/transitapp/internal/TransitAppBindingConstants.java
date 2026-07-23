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
package org.openhab.binding.transitapp.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

@NonNullByDefault
public class TransitAppBindingConstants {
    public static final String BINDING_ID = "transitapp";

    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_STOP = new ThingTypeUID(BINDING_ID, "stop");
    public static final ThingTypeUID THING_TYPE_ROUTE_DETAILS = new ThingTypeUID(BINDING_ID, "routedetails");
    public static final ThingTypeUID THING_TYPE_TRIP_DETAILS = new ThingTypeUID(BINDING_ID, "tripdetails");
}
