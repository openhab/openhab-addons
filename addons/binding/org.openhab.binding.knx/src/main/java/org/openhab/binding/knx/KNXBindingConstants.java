/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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

    public static final String BINDING_ID = "knx";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_IP_BRIDGE = new ThingTypeUID(BINDING_ID, "ip");
    public static final ThingTypeUID THING_TYPE_SERIAL_BRIDGE = new ThingTypeUID(BINDING_ID, "serial");
    public static final ThingTypeUID THING_TYPE_GENERIC = new ThingTypeUID(BINDING_ID, "generic");

    // List of all Channel ids
    public static final String CHANNEL_CURRENT = "current";
    public static final String CHANNEL_DIMMER = "dimmer";
    public static final String CHANNEL_ENERGY = "energy";
    public static final String CHANNEL_NUMBER = "number";
    public static final String CHANNEL_STRING = "string";
    public static final String CHANNEL_CONTACT = "contact";
    public static final String CHANNEL_DATETIME = "datetime";
    public static final String CHANNEL_OPERATING_HOURS = "operatinghours";
    public static final String CHANNEL_RESET = "reset";
    public static final String CHANNEL_ROLLERSHUTTER = "rollershutter";
    public static final String CHANNEL_SETPOINT = "setpoint";
    public static final String CHANNEL_STATUSSWITCH = "statusswitch";
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_GENERIC = "generic";
    public static final String ERRORS_INTERVAL = "errors5min";
    public static final String ERRORS_STARTUP = "errorsall";

    // List of Property ids
    public static final String FIRMWARE_TYPE = "firmwaretype";
    public static final String FIRMWARE_VERSION = "firmwareversion";
    public static final String FIRMWARE_SUBVERSION = "firmwaresubversion";
    public static final String MANUFACTURER_NAME = "manfacturername";
    public static final String MANUFACTURER_SERIAL_NO = "manfacturerserialnumber";
    public static final String MANUFACTURER_HARDWARE_TYPE = "manfacturerhardwaretype";
    public static final String MANUFACTURER_FIRMWARE_REVISION = "manfacturerfirmwarerevision";

    // List of all Configuration parameters
    public static final String ADDRESS = "address";
    public static final String CURRENT_GA = "currentGA";
    public static final String DPT = "dpt";
    public static final String DESCRIPTION = "description";
    public static final String ENERGY_GA = "energyGA";
    public static final String FETCH = "fetch";
    public static final String GROUPADDRESS = "groupaddress";
    public static final String INCREASE_DECREASE_ADDRESS = "increasedecreaseGA";
    public static final String INCREASE_DECREASE_DPT = "increasedecreaseDPT";
    public static final String INCREASE_DECREASE_GA = "increaseDecreaseGA";
    public static final String INTERVAL = "interval";
    public static final String IP_ADDRESS = "ipAddress";
    public static final String IP_CONNECTION_TYPE = "ipConnectionType";
    public static final String LOCAL_IP = "localIp";
    public static final String LOCAL_SOURCE_ADDRESS = "localSourceAddr";
    public static final String OPERATING_HOURS_GA = "operatingGA";
    public static final String PERCENT_DPT = "percentDPT";
    public static final String PERCENT_GA = "percentGA";
    public static final String PORT_NUMBER = "portNumber";
    public static final String POSITION_GA = "positionGA";
    public static final String POSITION_STATUS_GA = "positionStatusGA";
    public static final String READ = "read";
    public static final String SERIAL_PORT = "serialPort";
    public static final String SETPOINT_GA = "setpointGA";
    public static final String STATUS_GA = "statusGA";
    public static final String STOP_MOVE_GA = "stopMoveGA";
    public static final String STOP_MOVE_STATUS_GA = "stopMoveStatusGA";
    public static final String SWITCH_GA = "switchGA";
    public static final String TRANSMIT = "transmit";
    public static final String UNIT = "unit";
    public static final String UP_DOWN_GA = "upDownGA";
    public static final String UP_DOWN_STATUS_GA = "upDownStatusGA";
    public static final String UPDATE = "update";
    public static final String WRITE = "write";

    // List of all knxproj Namespace Identifierss
    public static final String KNX_PROJECT_12 = "http://knx.org/xml/project/12";
    public static final String KNX_PROJECT_13 = "http://knx.org/xml/project/13";

    // The default multicast ip address (see <a
    // href="http://www.iana.org/assignments/multicast-addresses/multicast-addresses.xml">iana</a> EIBnet/IP
    public static final String DEFAULT_MULTICAST_IP = "224.0.23.12";

    // The group address for identification of this KNX/IP gateway within the KNX bus. Default is 0.0.0
    public static final String DEFAULT_LOCAL_SOURCE_ADDRESS = "0.0.0";

}
