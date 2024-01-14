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
package org.openhab.binding.lgtvserial.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link LgTvSerialBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marius Bjoernstad - Initial contribution
 * @author Richard Lavoie - Moved channels id to a factory class
 */
@NonNullByDefault
public class LgTvSerialBindingConstants {

    public static final String BINDING_ID = "lgtvserial";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LGTV_GENERIC = new ThingTypeUID(BINDING_ID, "lgtv");
    public static final ThingTypeUID THING_TYPE_LGTV_LK_SERIES = new ThingTypeUID(BINDING_ID, "lgtv-LK-series");
    public static final ThingTypeUID THING_TYPE_LGTV_LV_SERIES = new ThingTypeUID(BINDING_ID, "lgtv-LV-series");
    public static final ThingTypeUID THING_TYPE_LGTV_LVX55_SERIES = new ThingTypeUID(BINDING_ID, "lgtv-LVx55-series");
    public static final ThingTypeUID THING_TYPE_LGTV_M6503C = new ThingTypeUID(BINDING_ID, "lgtv-M6503C");
    public static final ThingTypeUID THING_TYPE_LGTV_PW_SERIES = new ThingTypeUID(BINDING_ID, "lgtv-PW-series");
}
