/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.coolmasternet;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link coolmasternetBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Two Feathers Pty Ltd - Initial contribution
 */
public class coolmasternetBindingConstants {

    public static final String BINDING_ID = "coolmasternet";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_HVAC = new ThingTypeUID(BINDING_ID, "hvac");

    // List of all Channel ids
    public final static String ON = "on";
    public final static String MODE = "mode";
    public final static String SET_TEMP = "set_temp";
    public final static String FAN = "fan";
    public final static String LOUVRE = "louvre";
    public final static String CURRENT_TEMP = "current_temp";

}
