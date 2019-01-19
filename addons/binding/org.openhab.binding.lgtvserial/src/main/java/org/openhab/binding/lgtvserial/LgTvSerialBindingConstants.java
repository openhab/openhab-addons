/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.lgtvserial;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link LgTvSerialBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marius Bjoernstad - Initial contribution
 * @author Richard Lavoie - Moved channels id to a factory class
 */
@NonNullByDefault
public class LgTvSerialBindingConstants {

    public static final String BINDING_ID = "lgtvserial";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LGTV = new ThingTypeUID(BINDING_ID, "lgtv");

}
