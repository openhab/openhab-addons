/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rf24;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link rf24Binding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin Grzeslowski - Initial contribution
 */
public class rf24BindingConstants {

    public static final String BINDING_ID = "rf24";

    // List of all Thing Type UIDs
    public final static ThingTypeUID RF24_ON_OFF_THING_TYPE = new ThingTypeUID(BINDING_ID, "rf24OnOffThing");
    public final static ThingTypeUID RF24_DHT11_THING_TYPE = new ThingTypeUID(BINDING_ID, "rf24Dht11Thing");

    // List of all Channel ids
    public final static String RF24_DEVICE_ID_CHANNEL = "rf24DeviceId";
    public final static String DHT11_TEMPERATURE_CHANNEL = "dht11Temperature";
    public final static String DHT11_HUMIDITY_CHANNEL = "dht11Humidity";
    public final static String RF24_ON_OFF_CHANNEL = "rf24OnOff";

    public final static String RECIVER_PIPE_CONFIGURATION = "reciverPipe";
}
