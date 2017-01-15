/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.homie;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HomieBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Kolb - Initial contribution
 */
public class HomieBindingConstants {

    public final static String MQTT_TOPIC_SEPARATOR = "/";

    public static final String BINDING_ID = "homie";

    public final static ThingTypeUID HOMIE_DEVICE_THING_TYPE = new ThingTypeUID(BINDING_ID, "homieDeviceV2");
    public final static ThingTypeUID HOMIE_NODE_THING_TYPE = new ThingTypeUID(BINDING_ID, "homieNodeV2");

    public final static int DEVICE_DISCOVERY_TIMEOUT_SECONDS = 30;
    public final static int NODE_DISCOVERY_TIMEOUT_SECONDS = 60;

    public final static String MQTT_CLIENTID = "homieOpenhab2Binding";

    public final static String THING_PROP_SPEC_VERSION = "homie-specification-version";
    public final static String THING_PROP_IMPL_VERSION = "implementation-version";

    public final static String CHANNEL_STATS_UPTIME = "stats_uptime";
    public final static String CHANNEL_ONLINE = "online";
    public final static String CHANNEL_NAME = "name";
    public static final String CHANNEL_IMPLEMENTATION = "implementation";
    public static final String CHANNEL_FIRMWARE_CHECKSUM = "fw_checksum";
    public static final String CHANNEL_FIRMWARE_VERSION = "fw_version";
    public static final String CHANNEL_FIRMWARE_NAME = "fw_name";
    public static final String CHANNEL_STATS_INTERVAL = "stats_interval";
    public static final String CHANNEL_STATS_SIGNAL = "stats_signal";
    public static final String CHANNEL_MAC = "mac";
    public static final String CHANNEL_LOCALIP = "localip";
    public static final String CHANNEL_STATS_SIGNAL_ESH = "system-stats_signal";

    public static final String CHANNELPROPERTY_TOPICSUFFIX = "topic_suffix";

}
