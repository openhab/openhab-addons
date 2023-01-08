/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.siemenshvac;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link SiemensHvacBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Laurent ARNAL - Initial contribution
 */
public class SiemensHvacBindingConstants {

    public static final String BINDING_ID = "siemenshvac";

    public final static String TEMPERATURE = "temperature";

    public final static Set<String> SUPPORTED_DEVICE_MODELS = ImmutableSet.of("Web Server OZW672.01");

    // List of thing parameters names
    public final static String PROTOCOL_PARAMETER = "protocol";
    public final static String HOST_PARAMETER = "address";
    public final static String TCP_PORT_PARAMETER = "tcpPort";
    public final static String USER_PARAMETER = "userName";
    public final static String PASSWORD_PARAMETER = "userPassword";

    public final static String IP_PROTOCOL_NAME = "IP";

    public final static ThingTypeUID HVAC_THING_TYPE = new ThingTypeUID(BINDING_ID, "hvacBridge");
    public final static ThingTypeUID TEST_THING_TYPE = new ThingTypeUID(BINDING_ID, "hvacBridge");
    public final static ThingTypeUID HVAC_UNSUPPORTED_THING_TYPE = new ThingTypeUID(BINDING_ID,
            "hvacBridgeUnsupported");

    // Used for Discovery service
    public final static String MANUFACTURER = "Siemens Switzerland Ltd.";
    public final static String UPNP_DEVICE_TYPE = "Basic";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(HVAC_THING_TYPE,
            HVAC_UNSUPPORTED_THING_TYPE);

}
