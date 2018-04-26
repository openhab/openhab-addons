/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.alarm;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * Defines common constants, which are used across the binding.
 *
 * @author Gerhard Riegler - Initial contribution
 */
public class AlarmBindingConstants {

    public static final String BINDING_ID = "alarm";

    public static final String CONTROLLER = "controller";
    public static final String HOME = "home";
    public static final String ALARM_CONTROLLER_LABEL = "Alarm Controller";

    public static final ThingTypeUID THING_TYPE_ALARM_CONTROLLER = new ThingTypeUID(BINDING_ID, CONTROLLER);

    public static final String CHANNEL_TYPE_ID_ALARMZONE = "alarmZone";
    public static final String CHANNEL_ID_ALARMZONE = "alarmZone_";

    public static final String CHANNEL_ID_STATUS = "status";
    public static final String CHANNEL_ID_COUNTDOWN = "countdown";
    public static final String CHANNEL_ID_COMMAND = "command";
    public static final String CHANNEL_ID_INTERNAL_ARMING_POSSIBLE = "internalArmingPossible";
    public static final String CHANNEL_ID_EXTERNAL_ARMING_POSSIBLE = "externalArmingPossible";
    public static final String CHANNEL_ID_PASSTHROUGH_POSSIBLE = "passthroughPossible";

}
