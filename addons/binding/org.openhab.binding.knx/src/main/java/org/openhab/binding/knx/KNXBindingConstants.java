/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
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
 * @author Karel Goderis - Initial contribution
 */
public class KNXBindingConstants {

    public final static String BINDING_ID = "knx";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_IP_BRIDGE = new ThingTypeUID(BINDING_ID, "ip");
    public final static ThingTypeUID THING_TYPE_SERIAL_BRIDGE = new ThingTypeUID(BINDING_ID, "serial");
    public final static ThingTypeUID THING_TYPE_GENERIC = new ThingTypeUID(BINDING_ID, "generic");

    // List of all Channel ids
    public final static String CHANNEL_CURRENT = "current";
    public final static String CHANNEL_DIMMER = "dimmer";
    public final static String CHANNEL_ENERGY = "energy";
    public final static String CHANNEL_NUMBER = "number";
    public final static String CHANNEL_STRING = "string";
    public final static String CHANNEL_CONTACT = "contact";
    public final static String CHANNEL_DATETIME = "datetime";
    public final static String CHANNEL_OPERATING_HOURS = "operatinghours";
    public final static String CHANNEL_RESET = "reset";
    public final static String CHANNEL_ROLLERSHUTTER = "rollershutter";
    public final static String CHANNEL_SETPOINT = "setpoint";
    public final static String CHANNEL_STATUSSWITCH = "statusswitch";
    public final static String CHANNEL_SWITCH = "switch";
    public final static String CHANNEL_GENERIC = "generic";
    public final static String ERRORS_INTERVAL = "errors5min";
    public final static String ERRORS_STARTUP = "errorsall";

    // List of Property ids
    public final static String FIRMWARE_TYPE = "firmwaretype";
    public final static String FIRMWARE_VERSION = "firmwareversion";
    public final static String FIRMWARE_SUBVERSION = "firmwaresubversion";
    public final static String MANUFACTURER_NAME = "manfacturername";
    public final static String MANUFACTURER_SERIAL_NO = "manfacturerserialnumber";
    public final static String MANUFACTURER_HARDWARE_TYPE = "manfacturerhardwaretype";
    public final static String MANUFACTURER_FIRMWARE_REVISION = "manfacturerfirmwarerevision";

    // List of all Configuration parameters
    public final static String ADDRESS = "address";
    public final static String CURRENT_GA = "currentGA";
    public final static String DPT = "dpt";
    public final static String DESCRIPTION = "description";
    public final static String ENERGY_GA = "energyGA";
    public final static String FETCH = "fetch";
    public final static String GROUPADDRESS = "groupaddress";
    public final static String INCREASE_DECREASE_ADDRESS = "increasedecreaseGA";
    public final static String INCREASE_DECREASE_DPT = "increasedecreaseDPT";
    public final static String INCREASE_DECREASE_GA = "increaseDecreaseGA";
    public final static String INTERVAL = "interval";
    public final static String IP_ADDRESS = "ipAddress";
    public final static String IP_CONNECTION_TYPE = "ipConnectionType";
    public final static String LOCAL_IP = "localIp";
    public final static String LOCAL_SOURCE_ADDRESS = "localSourceAddr";
    public final static String OPERATING_HOURS_GA = "operatingGA";
    public final static String PERCENT_DPT = "percentDPT";
    public final static String PERCENT_GA = "percentGA";
    public final static String PORT_NUMBER = "portNumber";
    public final static String POSITION_GA = "positionGA";
    public final static String POSITION_STATUS_GA = "positionStatusGA";
    public final static String READ = "read";
    public final static String SERIAL_PORT = "serialPort";
    public final static String SETPOINT_GA = "setpointGA";
    public final static String STATUS_GA = "statusGA";
    public final static String STOP_MOVE_GA = "stopMoveGA";
    public final static String STOP_MOVE_STATUS_GA = "stopMoveStatusGA";
    public final static String SWITCH_GA = "switchGA";
    public final static String TRANSMIT = "transmit";
    public final static String UNIT = "unit";
    public final static String UP_DOWN_GA = "upDownGA";
    public final static String UP_DOWN_STATUS_GA = "upDownStatusGA";
    public final static String UPDATE = "update";
    public final static String WRITE = "write";

    // List of all knxproj Namespace Identifierss
    public final static String KNX_PROJECT_12 = "http://knx.org/xml/project/12";
    public final static String KNX_PROJECT_13 = "http://knx.org/xml/project/13";

    // The default multicast ip address (see <a
    // href="http://www.iana.org/assignments/multicast-addresses/multicast-addresses.xml">iana</a> EIBnet/IP
    public final static String DEFAULT_MULTICAST_IP = "224.0.23.12";

    // The group address for identification of this KNX/IP gateway within the KNX bus. Default is 0.0.0
    public final static String DEFAULT_LOCAL_SOURCE_ADDRESS = "0.0.0";

}
