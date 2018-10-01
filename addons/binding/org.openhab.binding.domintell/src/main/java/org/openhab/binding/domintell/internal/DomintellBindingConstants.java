/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.domintell.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The {@link DomintellBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class DomintellBindingConstants {
    private static final String BINDING_ID = "domintell";

    //bridge
    private static final String BRIDGE = "bridge";
    public static final String DETH02 = "DETH02";
    public static final ThingTypeUID BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, BRIDGE);

    //group thing types
    public static final ThingTypeUID THING_TYPE_GROUP = new ThingTypeUID(BINDING_ID, "group");

    static final String MODULE_TEX = "moduleTEx";
    static final String MODULE_BIR = "moduleBIR";
    static final String MODULE_DMR = "moduleDMR";
    static final String MODULE_IS8 = "moduleIS8";
    static final String MODULE_IS4 = "moduleIS4";
    static final String MODULE_PBX = "modulePBx";
    static final String MODULE_DIM = "moduleDIM";
    static final String MODULE_D10 = "moduleD10";

    //module thing types
    public static final ThingTypeUID THING_TYPE_MODULE_TEX = new ThingTypeUID(BINDING_ID, MODULE_TEX);
    public static final ThingTypeUID THING_TYPE_MODULE_BIR = new ThingTypeUID(BINDING_ID, MODULE_BIR);
    public static final ThingTypeUID THING_TYPE_MODULE_DMR = new ThingTypeUID(BINDING_ID, MODULE_DMR);
    public static final ThingTypeUID THING_TYPE_MODULE_IS8 = new ThingTypeUID(BINDING_ID, MODULE_IS8);
    public static final ThingTypeUID THING_TYPE_MODULE_IS4 = new ThingTypeUID(BINDING_ID, MODULE_IS4);
    public static final ThingTypeUID THING_TYPE_MODULE_PBX = new ThingTypeUID(BINDING_ID, MODULE_PBX);
    public static final ThingTypeUID THING_TYPE_MODULE_DIM = new ThingTypeUID(BINDING_ID, MODULE_DIM);
    public static final ThingTypeUID THING_TYPE_MODULE_D10 = new ThingTypeUID(BINDING_ID, MODULE_D10);

    // thing type sets
    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(BRIDGE_THING_TYPE);
    static final Set<ThingTypeUID> SUPPORTED_GROUP_THING_TYPES_UIDS = Stream.of(THING_TYPE_GROUP).collect(Collectors.toSet());
    static final Set<ThingTypeUID> SUPPORTED_MODULE_THING_TYPES_UIDS = Stream.of(THING_TYPE_MODULE_TEX, THING_TYPE_MODULE_BIR, THING_TYPE_MODULE_DMR, THING_TYPE_MODULE_IS8, THING_TYPE_MODULE_IS4, THING_TYPE_MODULE_PBX, THING_TYPE_MODULE_DIM, THING_TYPE_MODULE_D10).collect(Collectors.toSet());

    //system channel names
    public static final String CHANNEL_SYSTEM_DATE = "systemDate";
    public static final String CHANNEL_SYSTEM_COMMAND = "systemCommand";

    //TE channels
    public static final String CHANNEL_HEATING_CURRENT_VALUE = "heating#currentValue";
    public static final String CHANNEL_HEATING_PRESET_VALUE = "heating#presetValue";
    public static final String CHANNEL_HEATING_PROFILE_VALUE = "heating#profileValue";
    public static final String CHANNEL_HEATING_MODE = "heating#mode";
    public static final String CHANNEL_COOLING_CURRENT_VALUE = "cooling#currentValue";
    public static final String CHANNEL_COOLING_PRESET_VALUE = "cooling#presetValue";
    public static final String CHANNEL_COOLING_PROFILE_VALUE = "cooling#profileValue";
    public static final String CHANNEL_COOLING_MODE = "cooling#mode";

    private static final String CHANNEL_NUMERIC_VAR = "numericVar";
    private static final String CHANNEL_BOOLEAN_VAR = "booleanVar";
    public static final String CHANNEL_COMMAND = "command";
    public static final String CHANNEL_CONTACT = "contact";

    public static final ChannelTypeUID CHANNEL_TYPE_VARIABLE_NUM = new ChannelTypeUID(BINDING_ID, CHANNEL_NUMERIC_VAR);
    public static final ChannelTypeUID CHANNEL_TYPE_VARIABLE_BOOLEAN = new ChannelTypeUID(BINDING_ID, CHANNEL_BOOLEAN_VAR);
    public static final ChannelTypeUID CHANNEL_TYPE_CONTACT = new ChannelTypeUID(BINDING_ID, CHANNEL_CONTACT);

    //config
    public static final String CONFIG_SERIAL_NUMBER = "serialNumber";
    public static final String CONFIG_MODULE_TYPE = "moduleType";
}
