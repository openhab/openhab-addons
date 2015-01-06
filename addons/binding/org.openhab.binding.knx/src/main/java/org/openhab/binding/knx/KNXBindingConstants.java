/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link KNXBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kai Kreuzer / Karel Goderis - Initial contribution
 */
public class KNXBindingConstants {

    /**
     * the default multicast ip address (see
     * <a href="http://www.iana.org/assignments/multicast-addresses/multicast-addresses.xml">iana</a> EIBnet/IP)
     */
    public static final String DEFAULT_MULTICAST_IP = "224.0.23.12";

    /**
     * The group address for identification of this KNX/IP gateway within the KNX bus. Default is 0.0.0
     */
    public static final String DEFAULT_LOCAL_SOURCE_ADDRESS = "0.0.0";

    public static final String BINDING_ID = "knx";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_IP_BRIDGE = new ThingTypeUID(BINDING_ID, "ip");
    public final static ThingTypeUID THING_TYPE_SERIAL_BRIDGE = new ThingTypeUID(BINDING_ID, "serial");
    public final static ThingTypeUID THING_TYPE_GROUPADDRESS = new ThingTypeUID(BINDING_ID, "ga");
    public final static ThingTypeUID THING_TYPE_GENERIC = new ThingTypeUID(BINDING_ID, "generic");
    public final static ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");
    public final static ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");
    public final static ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public final static ThingTypeUID THING_TYPE_ENERGY_SWITCH = new ThingTypeUID(BINDING_ID, "energyswitch");
    public final static ThingTypeUID THING_TYPE_ROLLERSHUTTER = new ThingTypeUID(BINDING_ID, "rollershutter");
    public final static ThingTypeUID THING_TYPE_ROLLERSHUTTERSWITCH = new ThingTypeUID(BINDING_ID,
            "rollershutterswitch");
    public final static ThingTypeUID THING_TYPE_VIRTUALSWITCH = new ThingTypeUID(BINDING_ID, "virtualswitch");

    // List of all Channel ids
    public final static String ERRORS_STARTUP = "errorsall";
    public final static String ERRORS_INTERVAL = "errors5min";
    public final static String CHANNEL_RESET = "reset";

    // List of Property ids
    public final static String FIRMWARE_TYPE = "firmwaretype";
    public final static String FIRMWARE_VERSION = "firmwareversion";
    public final static String FIRMWARE_SUBVERSION = "firmwaresubversion";
    public final static String MANUFACTURER_NAME = "manfacturername";
    public final static String MANUFACTURER_SERIAL_NO = "manfacturerserialnumber";
    public final static String MANUFACTURER_HARDWARE_TYPE = "manfacturerhardwaretype";
    public final static String MANUFACTURER_FIRMWARE_REVISION = "manfacturerfirmwarerevision";

}
