/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pulseaudio;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link PulseaudioBinding} class defines common constants, which are 
 * used across the whole binding.
 * 
 * @author Tobias Br„utigam - Initial contribution
 */
public class PulseaudioBindingConstants {

    public static final String BINDING_ID = "pulseaudio";
    
    // List of all Thing Type UIDs
    public final static ThingTypeUID COMBINED_SINK_THING_TYPE= new ThingTypeUID(BINDING_ID, "combinedSink");
    public final static ThingTypeUID SINK_THING_TYPE= new ThingTypeUID(BINDING_ID, "sink");
    public final static ThingTypeUID SOURCE_THING_TYPE= new ThingTypeUID(BINDING_ID, "source");
    public final static ThingTypeUID SINK_INPUT_THING_TYPE= new ThingTypeUID(BINDING_ID, "sinkInput");
    public final static ThingTypeUID SOURCE_OUTPUT_THING_TYPE= new ThingTypeUID(BINDING_ID, "sourceOutput");
    
    public final static ThingTypeUID BRIDGE_THING_TYPE= new ThingTypeUID(BINDING_ID, "bridge");

    // List of all Channel ids
    public final static String VOLUME_CHANNEL = "volume";
    public final static String MUTE_CHANNEL = "mute";
    public final static String STATE_CHANNEL = "state";
    public final static String SLAVES_CHANNEL = "slaves";
    public final static String ROUTE_TO_SINK_CHANNEL = "routeToSink";

    // List of all Parameters
    public final static String BRIDGE_PARAMETER_HOST = "host";
    public final static String BRIDGE_PARAMETER_PORT = "port";
    public final static String BRIDGE_PARAMETER_REFRESH_INTERVAL = "refresh";
    
    public final static String DEVICE_PARAMETER_NAME = "name";
}
