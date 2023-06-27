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
package org.openhab.binding.velux.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link VeluxBindingConstants} class defines common constants, which are
 * used across the whole binding.
 * <P>
 * For an in-depth view of the available Item types with description of parameters, take a look onto
 * {@link org.openhab.binding.velux.internal.VeluxItemType VeluxItemType}.
 * </P>
 * This class contains the Thing identifications:
 * <UL>
 * <LI>{@link #THING_VELUX_BRIDGE} for the bridge itself,</LI>
 * <LI>{@link #THING_VELUX_SCENE} for the scenes summarizing a set of actuator states,</LI>
 * <LI>{@link #THING_VELUX_ACTUATOR} for the general actuators identified on the bridge,</LI>
 * <LI>{@link #THING_VELUX_ROLLERSHUTTER} for the rollershutters identified on the bridge,</LI>
 * <LI>{@link #THING_VELUX_WINDOW} for the windows identified on the bridge.</LI>
 * <LI>{@link #THING_VELUX_VSHUTTER} for the virtual shutters defined in the configuration</LI>
 * </UL>
 *
 * @author Guenther Schreiner - Initial contribution
 */
@NonNullByDefault
public class VeluxBindingConstants {

    /** Basic binding identification. */
    public static final String BINDING_ID = "velux";

    // Id of support bridge
    /**
     * The Thing identification of the binding.
     */
    private static final String THING_VELUX_BINDING = "binding";
    /**
     * The Thing identification of the <B>Velux</B> bridge.
     */
    private static final String THING_VELUX_BRIDGE = "klf200";
    /**
     * The Thing identification of a scene defined on the <B>Velux</B> bridge.
     */
    private static final String THING_VELUX_SCENE = "scene";
    /**
     * The Thing identification of a generic actuator defined on the <B>Velux</B> bridge.
     */
    private static final String THING_VELUX_ACTUATOR = "actuator";
    /**
     * The Thing identification of a rollershutter defined on the <B>Velux</B> bridge.
     */
    private static final String THING_VELUX_ROLLERSHUTTER = "rollershutter";
    /**
     * The Thing identification of a window defined on the <B>Velux</B> bridge.
     */
    private static final String THING_VELUX_WINDOW = "window";
    /**
     * The Thing identification of a virtual shutter defined on the <B>Velux</B> bridge.
     */
    private static final String THING_VELUX_VSHUTTER = "vshutter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BINDING = new ThingTypeUID(BINDING_ID, THING_VELUX_BINDING);

    // List of all Bridge Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, THING_VELUX_BRIDGE);

    // List of all Thing Type UIDs beyond the bridge(s)
    public static final ThingTypeUID THING_TYPE_VELUX_SCENE = new ThingTypeUID(BINDING_ID, THING_VELUX_SCENE);
    public static final ThingTypeUID THING_TYPE_VELUX_ACTUATOR = new ThingTypeUID(BINDING_ID, THING_VELUX_ACTUATOR);
    public static final ThingTypeUID THING_TYPE_VELUX_ROLLERSHUTTER = new ThingTypeUID(BINDING_ID,
            THING_VELUX_ROLLERSHUTTER);
    public static final ThingTypeUID THING_TYPE_VELUX_WINDOW = new ThingTypeUID(BINDING_ID, THING_VELUX_WINDOW);
    public static final ThingTypeUID THING_TYPE_VELUX_VSHUTTER = new ThingTypeUID(BINDING_ID, THING_VELUX_VSHUTTER);

    // Definitions of different set of Things
    public static final Set<ThingTypeUID> SUPPORTED_THINGS_BINDING = new HashSet<>(Arrays.asList(THING_TYPE_BINDING));
    public static final Set<ThingTypeUID> SUPPORTED_THINGS_BRIDGE = new HashSet<>(Arrays.asList(THING_TYPE_BRIDGE));

    public static final Set<ThingTypeUID> SUPPORTED_THINGS_ITEMS = new HashSet<>(
            Arrays.asList(THING_TYPE_VELUX_SCENE, THING_TYPE_VELUX_ACTUATOR, THING_TYPE_VELUX_ROLLERSHUTTER,
                    THING_TYPE_VELUX_WINDOW, THING_TYPE_VELUX_VSHUTTER));

    public static final Set<ThingTypeUID> DISCOVERABLE_THINGS = Set.of(THING_TYPE_VELUX_SCENE,
            THING_TYPE_VELUX_ACTUATOR, THING_TYPE_VELUX_ROLLERSHUTTER, THING_TYPE_VELUX_WINDOW,
            THING_TYPE_VELUX_VSHUTTER, THING_TYPE_BINDING, THING_TYPE_BRIDGE);

    public static final Set<ThingTypeUID> ACTUATOR_THINGS = Set.of(THING_TYPE_VELUX_ACTUATOR,
            THING_TYPE_VELUX_ROLLERSHUTTER, THING_TYPE_VELUX_WINDOW);

    // *** List of all Channel ids ***

    // List of all binding channel ids

    /** Channel identifier describing the current Binding State. */
    public static final String CHANNEL_BINDING_INFORMATION = "information";

    // List of all bridge channel ids

    /** Channel/Property identifier describing the current Bridge State. */
    public static final String CHANNEL_BRIDGE_STATUS = "status";
    public static final String CHANNEL_BRIDGE_RELOAD = "reload";
    public static final String CHANNEL_BRIDGE_DOWNTIME = "downtime";
    public static final String CHANNEL_BRIDGE_DO_DETECTION = "doDetection";

    public static final String PROPERTY_BRIDGE_TIMESTAMP_SUCCESS = "connectionSuccess";
    public static final String PROPERTY_BRIDGE_TIMESTAMP_ATTEMPT = "connectionAttempt";
    public static final String PROPERTY_BRIDGE_FIRMWARE = "firmware";
    public static final String PROPERTY_BRIDGE_ADDRESS = "address";
    public static final String PROPERTY_BRIDGE_SUBNETMASK = "subnetMask";
    public static final String PROPERTY_BRIDGE_DEFAULTGW = "defaultGW";
    public static final String PROPERTY_BRIDGE_DHCP = "DHCP";
    public static final String PROPERTY_BRIDGE_WLANSSID = "WLANSSID";
    public static final String PROPERTY_BRIDGE_WLANPASSWORD = "WLANPassword";

    public static final String PROPERTY_BRIDGE_CHECK = "check";
    public static final String PROPERTY_BRIDGE_PRODUCTS = "products";
    public static final String PROPERTY_BRIDGE_SCENES = "scenes";

    // List of all scene channel ids
    public static final String CHANNEL_SCENE_ACTION = "action";
    public static final String CHANNEL_SCENE_SILENTMODE = "silentMode";

    // List of all actuator channel ids
    public static final String CHANNEL_ACTUATOR_POSITION = "position";
    public static final String CHANNEL_ACTUATOR_STATE = "state";
    public static final String CHANNEL_ACTUATOR_SILENTMODE = "silentMode";
    public static final String CHANNEL_ACTUATOR_LIMIT_MINIMUM = "limitMinimum";
    public static final String CHANNEL_ACTUATOR_LIMIT_MAXIMUM = "limitMaximum";
    public static final String CHANNEL_VANE_POSITION = "vanePosition";

    // List of all virtual shutter channel ids
    public static final String CHANNEL_VSHUTTER_POSITION = "vposition";

    public static final Set<String> POSITION_CHANNELS = Set.of(CHANNEL_ACTUATOR_POSITION, CHANNEL_ACTUATOR_STATE,
            CHANNEL_VANE_POSITION, CHANNEL_VSHUTTER_POSITION);

    // Helper definitions
    public static final String BINDING_VALUES_SEPARATOR = ",";
    public static final String OUTPUT_VALUE_SEPARATOR = ",";
    public static final String UNKNOWN = "unknown";

    // Critical issues to be reported will use the following message
    public static final String LOGGING_CONTACT = "Please report to maintainer: ";

    public static final String UNKNOWN_THING_TYPE_ID = "FAILED";
    public static final String UNKNOWN_IP_ADDRESS = "xxx.xxx.xxx.xxx";
}
