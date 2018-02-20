/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.atlona.internal.pro3;

/**
 * The {@link AtlonaPro3Binding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tim Roberts
 */
class AtlonaPro3Constants {

    // Properties
    static final String PROPERTY_VERSION = "version";
    static final String PROPERTY_TYPE = "type";

    static final String GROUP_PRIMARY = "primary";
    static final String GROUP_PORT = "port";
    static final String GROUP_MIRROR = "mirror";
    static final String GROUP_VOLUME = "volume";

    // List of all Channel ids
    static final String CHANNEL_POWER = "power";
    static final String CHANNEL_PANELLOCK = "panellock";
    static final String CHANNEL_IRENABLE = "irenable";
    static final String CHANNEL_PRESETCMDS = "presetcmd";
    static final String CHANNEL_MATRIXCMDS = "matrixcmd";

    static final String CHANNEL_PORTPOWER = "portpower";
    static final String CHANNEL_PORTOUTPUT = "portoutput";

    static final String CHANNEL_PORTMIRROR = "portmirror";
    static final String CHANNEL_PORTMIRRORENABLED = "portmirrorenabled";

    static final String CHANNEL_VOLUME = "volume";
    static final String CHANNEL_VOLUME_MUTE = "volumemute";
    // static final String CHANNEL_RS232 = "rs232cmd";

    static final String CONFIG_HOSTNAME = "hostname";
    static final String CONFIG_OUTPUT = "output";

    // Preset commands
    static final String CMD_PRESETSAVE = "save";
    static final String CMD_PRESETRECALL = "recall";
    static final String CMD_PRESETCLEAR = "clear";

    // Matrix commands
    static final String CMD_MATRIXRESET = "resetmatrix";
    static final String CMD_MATRIXRESETPORTS = "resetports";
    static final String CMD_MATRIXPORTALL = "allports";

}
