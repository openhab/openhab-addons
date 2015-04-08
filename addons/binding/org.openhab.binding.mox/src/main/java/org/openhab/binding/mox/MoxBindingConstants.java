/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mox;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link MoxBinding} class defines common constants, which are 
 * used across the whole binding.
 * 
 * @author Thomas Eichstaedt-Engelen (innoQ) - Initial contribution
 * @since 0.8.0
 */
public class MoxBindingConstants {

    public static final String BINDING_ID = "mox";
    
    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");
    public final static ThingTypeUID THING_TYPE_1G_ONOFF = new ThingTypeUID(BINDING_ID, "1_gang_onoff");
    public final static ThingTypeUID THING_TYPE_1G_DIMMER = new ThingTypeUID(BINDING_ID, "1_gang_dimmer");
    public final static ThingTypeUID THING_TYPE_1G_FAN = new ThingTypeUID(BINDING_ID, "1_gang_fan");
    public final static ThingTypeUID THING_TYPE_1G_CURTAIN = new ThingTypeUID(BINDING_ID, "1_gang_curtain");

    // List of all Channel ids
    public final static String STATE = "state";
    
    public final static String CHANNEL_ACTIVE_POWER = "activePower";
    public final static String CHANNEL_REACTIVE_POWER = "reactivePower";
    public final static String CHANNEL_APPARENT_POWER = "apparentPower";
    public final static String CHANNEL_ACTIVE_ENERGY = "activeEnergy";
    public final static String CHANNEL_POWER_FACTOR = "powerFactor";
    
    // Gateway config properties
    public final static String UDP_HOST = "udpHost";
    public final static String UDP_PORT = "udpPort";
    
    // Device config properties
    public final static String OID = "oid";

}
