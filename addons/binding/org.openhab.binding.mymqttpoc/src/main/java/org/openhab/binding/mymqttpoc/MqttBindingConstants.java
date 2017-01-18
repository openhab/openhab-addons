/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mymqttpoc;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link MyMqttPocBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public class MqttBindingConstants {

    public static final String BINDING_ID = "mymqttpoc";

    // ZoneMinder Server Bridge
    public static final String BRIDGE_MQTTBUS = "mqtt-bus";

    /*
     * MQTT Bridge Constants
     */
    // Thing Type UID for Server
    public final static ThingTypeUID THING_TYPE_BRIDGE_MQTTBUS = new ThingTypeUID(BINDING_ID, BRIDGE_MQTTBUS);

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_MQTT_CLIENT = new ThingTypeUID(BINDING_ID, "client");

    // List of all Channel ids
    public final static String CHANNELID_PUBLISH = "publish";
    public final static String CHANNELID_SUBSCRIBE = "subscribe";

}
