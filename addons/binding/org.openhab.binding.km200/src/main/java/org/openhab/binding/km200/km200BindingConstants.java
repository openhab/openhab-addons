/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.km200;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link km200Binding} class defines common constants, which are 
 * used across the whole binding.
 * 
 * @author Markus Eckhardt - Initial contribution
 */
public class km200BindingConstants {

    public static final String BINDING_ID = "km200";
    
    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "sample");

    // List of all Channel ids
    public final static String CHANNEL_1 = "channel1";

}
