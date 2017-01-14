/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tankerkoenig;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link TankerkoenigBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dennis Dollinger - Initial contribution
 */
public class TankerkoenigBindingConstants {

    public static final String BINDING_ID = "tankerkoenig";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_TANKSTELLE = new ThingTypeUID(BINDING_ID, "tankstelle");

    // List of all Channel ids
    public final static String CHANNEL_DIESEL = "diesel";
    public final static String CHANNEL_E10 = "e10";
    public final static String CHANNEL_E5 = "e5";

}
