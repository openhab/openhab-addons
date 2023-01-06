/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.dominoswiss.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link DominoswissBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Frieso Aeschbacher - Initial contribution
 */
@NonNullByDefault
public class DominoswissBindingConstants {

    private static final String BINDING_ID = "dominoswiss";

    // List of all Thing Type UIDs
    public static final ThingTypeUID DOMINOSWISSBLINDS_THING_TYPE = new ThingTypeUID(BINDING_ID, "blind");
    public static final ThingTypeUID DOMINOSWISSEGATE_THING_TYPE = new ThingTypeUID(BINDING_ID, "egate");

    // List of all Channel ids
    public static final String CHANNEL_PULSEUP = "pulseUp";
    public static final String CHANNEL_PULSEDOWN = "pulseDown";
    public static final String CHANNEL_CONTINOUSUP = "continousUp";
    public static final String CHANNEL_CONTINOUSDOWN = "continousDown";
    public static final String CHANNEL_STOP = "STOP";
    public static final String UP = "UP";
    public static final String DOWN = "DOWN";
    public static final String SHUTTER = "shutter";
    public static final String TILTUP = "tiltUp";
    public static final String TILTDOWN = "tiltDown";
    public static final String SHUTTERTILT = "shutterTilt";

    public static final String GETCONFIG = "getConfig";

    public static final String CR = "\r";
}
