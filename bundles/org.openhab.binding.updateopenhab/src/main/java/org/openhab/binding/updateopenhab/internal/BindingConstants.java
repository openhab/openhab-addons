/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.updateopenhab.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class BindingConstants {

    private static final String BINDING_ID = "updateopenhab";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_OH_UPDATER = new ThingTypeUID(BINDING_ID, "updater");

    // List of all Channel ids
    public static final String CHANNEL_UPDATE_COMMAND = "updateCommand";
    public static final String CHANNEL_ACTUAL_OH_VERSION = "actualVersion";
    public static final String CHANNEL_LATEST_OH_VERSION = "latestVersion";
    public static final String CHANNEL_UPDATE_AVAILABLE = "updateAvailable";

    // List of all property names
    public static final String PROPERTY_OPERATING_SYSTEM = "operatingSystem";
}
