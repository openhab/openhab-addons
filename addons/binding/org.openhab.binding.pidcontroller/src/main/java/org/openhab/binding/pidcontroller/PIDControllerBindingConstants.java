/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.pidcontroller;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link PIDControllerBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author George Erhan - Initial contribution
 */
public class PIDControllerBindingConstants {

    public static final String BINDING_ID = "pidcontroller";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "Controller");

    // List of all Channel ids
    public final static String input = "input";
    public final static String setpoint = "setpoint";
    public final static String LoopTime = "LoopTime";
    public final static String output = "output";
    public final static String kpadjuster = "kpadjuster";
    public final static String kiadjuster = "kiadjuster";
    public final static String kdadjuster = "kdadjuster";
    public final static String pidlowerlimit = "pidlowerlimit";
    public final static String pidupperlimit = "pidupperlimit";
    public final static int LoopTimeDefault = 1000;
    public final static int PIDrangeDefault = 510;

}
