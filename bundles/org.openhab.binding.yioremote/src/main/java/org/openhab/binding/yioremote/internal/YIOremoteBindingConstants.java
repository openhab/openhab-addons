/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.yioremote.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link YIOremoteBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Loercher - Initial contribution
 */
@NonNullByDefault
public class YIOremoteBindingConstants {

    private static final String BINDING_ID = "yioremote";

    // List of all used global variables

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_YIOREMOTE = new ThingTypeUID(BINDING_ID, "yioremote");

    // List of all Channel ids
    public static final String CHANNEL_1 = "channel1";
    // Configuration elements
    public static final String CONFIG_YIODOCKHOSTIP = "yiodockhostip";
    public static final String CONFIG_YIODOCKACCESSTOKEN = "yiodockaccesstoken";

}
