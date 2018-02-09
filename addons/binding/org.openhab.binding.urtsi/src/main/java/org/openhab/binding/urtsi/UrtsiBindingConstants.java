/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.urtsi;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link UrtsiBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Oliver Libutzki - Initial contribution
 */
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
