/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mqtt;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link MqttBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marcus of Wetware Labs - Initial contribution
 */
public class MqttBindingConstants {

    public static final String BINDING_ID = "mqtt";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public final static ThingTypeUID THING_TYPE_TOPIC = new ThingTypeUID(BINDING_ID, "topic");

    // List of all Channel ids
    public final static String CHANNEL_NUMBER = "number";
    public final static String CHANNEL_SWITCH = "switch";
    public final static String CHANNEL_COLOR = "color";
    public final static String CHANNEL_CONTACT = "contact";
    public final static String CHANNEL_DIMMER = "dimmer";
    public final static String CHANNEL_DATETIME = "datetime";
    public final static String CHANNEL_ROLLERSHUTTER = "rollershutter";
    public final static String CHANNEL_STRING = "string";

    // Bridge properties
    public final static String BROKER = "broker";
    public final static String URL = "url";
    public final static String USER = "user";
    public final static String PWD = "pwd";
    public final static String CLIENTID = "clientId";

    // Topic properties
    public final static String TOPIC_ID = "topicId";
    public final static String TYPE = "type";
    public final static String TRANSFORM = "transform";

    // misc
    public final static String MQTT_SERVICE_PID = "org.eclipse.smarthome.mqtt"; // for some reason
                                                                                // it is not named
                                                                                // org.eclipse.smarthome.io.transport.mqtt
                                                                                // ??

}
