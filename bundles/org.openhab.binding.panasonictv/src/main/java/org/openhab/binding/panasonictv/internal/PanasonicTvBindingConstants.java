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
package org.openhab.binding.panasonictv.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link PanasonicTvBindingConstants} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Prakashbabu Sidaraddi - Initial contribution
 */
@NonNullByDefault
public class PanasonicTvBindingConstants {

    public static final String BINDING_ID = "panasonictv";

    public static final ThingTypeUID THING_TYPE_PANASONICTV = new ThingTypeUID(BINDING_ID, "tv");

    public static final String CONFIG_REMOTECONTROLLER_UDN = "remoteControllerUdn";
    public static final String CONFIG_MEDIARENDERER_UDN = "mediaRendererUdn";
    public static final String PROPERTY_SERIAL = "serialNumber";

    // List of all remote controller thing channel id's
    public static final String KEY_CODE = "keyCode";
    public static final String POWER = "power";
    public static final String SOURCE_NAME = "sourceName";
    public static final String SOURCE_ID = "sourceId";

    // List of all media renderer thing channel id's
    public static final String VOLUME = "volume";
    public static final String MUTE = "mute";
}
