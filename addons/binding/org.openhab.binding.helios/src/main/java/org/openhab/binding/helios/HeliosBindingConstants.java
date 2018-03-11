/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.helios;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link HeliosBinding} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Karel Goderis - Initial contribution
 */
@NonNullByDefault
public class HeliosBindingConstants {

    public static final String BINDING_ID = "helios";

    // List of all Thing Type UIDs
    public static final ThingTypeUID HELIOS_VARIO_IP_2_7_TYPE = new ThingTypeUID(BINDING_ID, "ipvario27");
    public static final ThingTypeUID HELIOS_VARIO_IP_2_21_TYPE = new ThingTypeUID(BINDING_ID, "ipvario221");

    // List of all Channel ids
    public static final String KEY_PRESSED = "keypressed";
    public static final String KEY_PRESSED_STAMP = "keypressedstamp";
    public static final String CALL_STATE = "callstate";
    public static final String CALL_DIRECTION = "calldirection";
    public static final String CALL_STATE_STAMP = "callstatestamp";
    public static final String CARD = "card";
    public static final String CARD_VALID = "cardvalid";
    public static final String CARD_STAMP = "cardstamp";
    public static final String CODE = "code";
    public static final String CODE_VALID = "codevalid";
    public static final String CODE_STAMP = "codestamp";
    public static final String DEVICE_STATE = "devicestate";
    public static final String DEVICE_STATE_STAMP = "devicestamp";
    public static final String AUDIO_LOOP_TEST = "audiolooptest";
    public static final String AUDIO_LOOP_TEST_STAMP = "audioloopteststamp";
    public static final String MOTION = "motion";
    public static final String MOTION_STAMP = "motionstamp";
    public static final String NOISE = "noise";
    public static final String NOISE_STAMP = "noisestamp";
    public static final String KEY_RELEASED = "keyreleased";
    public static final String KEY_RELEASED_STAMP = "keyreleasedstamp";
    public static final String SWITCH_STATE = "switchstate";
    public static final String SWITCH_STATE_ORIGINATOR = "switchstateoriginator";
    public static final String SWITCH_STATE_SWITCH = "switchstateswitch";
    public static final String SWITCH_STATE_STAMP = "switchstatestamp";

    // List of all Channel type ids
    public static final String SWITCH_ENABLER = "switchenabler";
    public static final String SWITCH_TRIGGER = "switchtrigger";
    public static final String IO_TRIGGER = "io";

    // List of all Thing properties
    public static final String VARIANT = "variant";
    public static final String SERIAL_NUMBER = "serialNumber";
    public static final String HW_VERSION = "hardwareVersion";
    public static final String SW_VERSION = "softwareVersion";
    public static final String BUILD_TYPE = "buildType";
    public static final String DEVICE_NAME = "deviceName";
}
