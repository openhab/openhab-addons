/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sonyps4.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SonyPS4BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Fredrik Ahlström - Initial contribution
 */
@NonNullByDefault
public class SonyPS4BindingConstants {

    private static final String BINDING_ID = "sonyps4";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SONYPS4 = new ThingTypeUID(BINDING_ID, "SonyPS4");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_SONYPS4);

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_APPLICATION = "application";
    public static final String CHANNEL_NAME = "name";
}
