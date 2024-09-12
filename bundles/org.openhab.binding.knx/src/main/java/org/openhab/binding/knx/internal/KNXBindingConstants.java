/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.knx.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link KNXBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class KNXBindingConstants {

    public static final String BINDING_ID = "knx";

    // Global config
    public static final String CONFIG_DISABLE_UOM = "disableUoM";
    public static boolean disableUoM = false;

    // Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_IP_BRIDGE = new ThingTypeUID(BINDING_ID, "ip");
    public static final ThingTypeUID THING_TYPE_SERIAL_BRIDGE = new ThingTypeUID(BINDING_ID, "serial");
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    // Property IDs
    public static final String DEVICE_MASK_VERSION = "deviceMaskVersion";
    public static final String DEVICE_PROFILE = "deviceProfile";
    public static final String DEVICE_MEDIUM_TYPE = "deviceMediumType";
    public static final String FRIENDLY_NAME = "deviceName";
    public static final String MANUFACTURER_NAME = "manufacturerName";
    public static final String MANUFACTURER_SERIAL_NO = "manufacturerSerialNumber";
    public static final String MANUFACTURER_HARDWARE_TYPE = "manufacturerHardwareType";
    public static final String MANUFACTURER_FIRMWARE_REVISION = "manufacturerFirmwareRevision";
    public static final String MANUFACTURER_ORDER_INFO = "manufacturerOrderInfo";
    public static final String MAX_APDU_LENGTH = "maxApduLength";

    // Thing Configuration parameters
    public static final String IP_ADDRESS = "ipAddress";
    public static final String IP_CONNECTION_TYPE = "type";
    public static final String LOCAL_IP = "localIp";
    public static final String LOCAL_SOURCE_ADDRESS = "localSourceAddr";
    public static final String PORT_NUMBER = "portNumber";
    public static final String SERIAL_PORT = "serialPort";
    public static final String USE_CEMI = "useCemi";
    public static final String ROUTER_BACKBONE_GROUP_KEY = "routerBackboneGroupKey";
    public static final String TUNNEL_USER_ID = "tunnelUserId";
    public static final String TUNNEL_USER_PASSWORD = "tunnelUserPassword";
    public static final String TUNNEL_DEVICE_AUTHENTICATION = "tunnelDeviceAuthentication";

    // The default multicast ip address (see <a
    // href="http://www.iana.org/assignments/multicast-addresses/multicast-addresses.xml">iana</a> EIBnet/IP
    public static final String DEFAULT_MULTICAST_IP = "224.0.23.12";

    // Channel Type IDs
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_COLOR_CONTROL = "color-control";
    public static final String CHANNEL_CONTACT = "contact";
    public static final String CHANNEL_CONTACT_CONTROL = "contact-control";
    public static final String CHANNEL_DATETIME = "datetime";
    public static final String CHANNEL_DATETIME_CONTROL = "datetime-control";
    public static final String CHANNEL_DIMMER = "dimmer";
    public static final String CHANNEL_DIMMER_CONTROL = "dimmer-control";
    public static final String CHANNEL_NUMBER = "number";
    public static final String CHANNEL_NUMBER_CONTROL = "number-control";
    public static final String CHANNEL_ROLLERSHUTTER = "rollershutter";
    public static final String CHANNEL_ROLLERSHUTTER_CONTROL = "rollershutter-control";
    public static final String CHANNEL_STRING = "string";
    public static final String CHANNEL_STRING_CONTROL = "string-control";
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_SWITCH_CONTROL = "switch-control";

    public static final ChannelTypeUID CHANNEL_CONTACT_CONTROL_UID = new ChannelTypeUID(BINDING_ID,
            CHANNEL_CONTACT_CONTROL);

    public static final Set<String> CONTROL_CHANNEL_TYPES = Set.of( //
            CHANNEL_COLOR_CONTROL, //
            CHANNEL_CONTACT_CONTROL, //
            CHANNEL_DATETIME_CONTROL, //
            CHANNEL_DIMMER_CONTROL, //
            CHANNEL_NUMBER_CONTROL, //
            CHANNEL_ROLLERSHUTTER_CONTROL, //
            CHANNEL_STRING_CONTROL, //
            CHANNEL_SWITCH_CONTROL //
    );

    public static final String CHANNEL_RESET = "reset";

    // Channel Configuration parameters
    public static final String GA = "ga";
    public static final String HSB_GA = "hsb";
    public static final String INCREASE_DECREASE_GA = "increaseDecrease";
    public static final String POSITION_GA = "position";
    public static final String REPEAT_FREQUENCY = "frequency";
    public static final String STOP_MOVE_GA = "stopMove";
    public static final String SWITCH_GA = "switch";
    public static final String UP_DOWN_GA = "upDown";

    public static final Map<Integer, String> MANUFACTURER_MAP = readManufacturerMap();

    private static Map<Integer, String> readManufacturerMap() {
        ClassLoader classLoader = KNXBindingConstants.class.getClassLoader();
        if (classLoader == null) {
            return Map.of();
        }

        try (InputStream is = classLoader.getResourceAsStream("manufacturer.properties")) {
            if (is == null) {
                return Map.of();
            }

            Properties properties = new Properties();
            properties.load(is);
            return properties.entrySet().stream()
                    .collect(Collectors.toMap(e -> Integer.parseInt((String) e.getKey()), e -> (String) e.getValue()));
        } catch (IOException e) {
            return Map.of();
        }
    }
}
