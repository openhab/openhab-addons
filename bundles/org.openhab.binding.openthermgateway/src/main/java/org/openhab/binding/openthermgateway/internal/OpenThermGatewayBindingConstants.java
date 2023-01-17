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
package org.openhab.binding.openthermgateway.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OpenThermGatewayBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Arjen Korevaar - Initial contribution
 */
@NonNullByDefault
public class OpenThermGatewayBindingConstants {

    // Binding Id
    public static final String BINDING_ID = "openthermgateway";

    // List of all the ThingType UID's
    public static final ThingTypeUID OPENTHERM_GATEWAY_THING_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "openthermgateway");
    public static final ThingTypeUID BOILER_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "boiler");
    public static final ThingTypeUID VENTILATION_HEATRECOVERY_THING_TYPE_UID = new ThingTypeUID(BINDING_ID,
            "ventilationheatrecovery");
    public static final ThingTypeUID LEGACY_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, "otgw");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Set.of(OPENTHERM_GATEWAY_THING_TYPE_UID,
            BOILER_THING_TYPE_UID, VENTILATION_HEATRECOVERY_THING_TYPE_UID, LEGACY_THING_TYPE_UID);

    // List of id's for writeable channels
    public static final String CHANNEL_SEND_COMMAND = "sendcommand";
    public static final String CHANNEL_OVERRIDE_SETPOINT_TEMPORARY = "temperaturetemporary";
    public static final String CHANNEL_OVERRIDE_SETPOINT_CONSTANT = "temperatureconstant";
    public static final String CHANNEL_OVERRIDE_DHW_SETPOINT = "overridedhwsetpoint";
    public static final String CHANNEL_OVERRIDE_CENTRAL_HEATING_WATER_SETPOINT = "controlsetpointoverride";
    public static final String CHANNEL_OVERRIDE_CENTRAL_HEATING2_WATER_SETPOINT = "controlsetpoint2override";
    public static final String CHANNEL_OVERRIDE_CENTRAL_HEATING_ENABLED = "ch_enableoverride";
    public static final String CHANNEL_OVERRIDE_CENTRAL_HEATING2_ENABLED = "ch2_enableoverride";
    public static final String CHANNEL_OUTSIDE_TEMPERATURE = "outsidetemp";
    public static final String CHANNEL_OVERRIDE_VENTILATION_SETPOINT = "vh_ventilationsetpoint";

    // Generic channel type for Transparent Slave Parameter and Fault History Buffer values
    public static final String CHANNEL_TSPFHB = "tspfhb";
}
