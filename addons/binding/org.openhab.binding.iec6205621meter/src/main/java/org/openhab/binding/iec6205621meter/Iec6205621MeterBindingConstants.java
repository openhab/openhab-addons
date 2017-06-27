/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.iec6205621meter;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link Iec6205621MeterBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Peter Kreutzer - Initial contribution
 */
public class Iec6205621MeterBindingConstants {

    public static final String BINDING_ID = "iec6205621meter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_METER = new ThingTypeUID(BINDING_ID, "meter");

    // List of all Channel ids
    public static final String CHANNEL_TYPE = "meterType";
    public static final String CHANNEL_NUMBER = "obisNumber";
    public static final String CHANNEL_TEXT = "obisString";

    // Custom Properties
    public final static String PROPERTY_PORT = "port";
    public final static String PROPERTY_BAUDRATECHANGEDELAY = "baudRateChangeDelay";
    public final static String PROPERTY_ECHOHANDLING = "echoHandling";
    public final static String PROPERTY_INITMESSAGE = "initMessage";
    public final static String PROPERTY_REFRESH = "refresh";

}
