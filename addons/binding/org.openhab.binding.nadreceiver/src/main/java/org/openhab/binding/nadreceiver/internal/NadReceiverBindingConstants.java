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
package org.openhab.binding.nadreceiver.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link NadReceiverBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marc Chételat - Initial contribution
 */
@NonNullByDefault
public class NadReceiverBindingConstants {

    private static final String BINDING_ID = "nadreceiver";

    // List of all Thing Type UIDs
    public static final ThingTypeUID NAD_RECEIVER_THING_TYPE = new ThingTypeUID(BINDING_ID, "receiver");

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_SOURCE = "source";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_MODEL = "model";
}
