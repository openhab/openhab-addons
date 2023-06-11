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
package org.openhab.binding.iammeter.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link IammeterBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author yang bo - Initial contribution
 */
@NonNullByDefault
public class IammeterBindingConstants {

    public static final String BINDING_ID = "iammeter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_POWERMETER = new ThingTypeUID(BINDING_ID, "powermeter");
    public static final ThingTypeUID THING_TYPE_POWERMETER_3080T = new ThingTypeUID(BINDING_ID, "powermeter3080T");
}
