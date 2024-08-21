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
package org.openhab.binding.urtsi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link UrtsiBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Oliver Libutzki - Initial contribution
 */
@NonNullByDefault
public class UrtsiBindingConstants {

    public static final String BINDING_ID = "urtsi";

    // List of all Thing Type UIDs
    /**
     * URTSI II box
     */
    public static final ThingTypeUID URTSI_DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, "urtsidevice");

    /**
     * RTS Device (e.g. rollershutter)
     */
    public static final ThingTypeUID RTS_DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, "rtsdevice");

    /**
     * Rollershutter's position
     */
    public static final String POSITION = "position";
}
