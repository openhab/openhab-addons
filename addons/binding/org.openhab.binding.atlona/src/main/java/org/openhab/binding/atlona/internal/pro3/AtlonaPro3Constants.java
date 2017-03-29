/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
    final static String PROPERTY_VERSION = "version";
    final static String PROPERTY_TYPE = "type";

    final static String GROUP_PRIMARY = "primary";
    final static String GROUP_PORT = "port";
    final static String GROUP_MIRROR = "mirror";
    final static String GROUP_VOLUME = "volume";

    // List of all Channel ids
    final static String CHANNEL_POWER = "power";
    final static String CHANNEL_PANELLOCK = "panellock";
    final static String CHANNEL_IRENABLE = "irenable";
    final static String CHANNEL_PRESETCMDS = "presetcmd";
    final static String CHANNEL_MATRIXCMDS = "matrixcmd";

    final static String CHANNEL_PORTPOWER = "portpower";
    final static String CHANNEL_PORTOUTPUT = "portoutput";

    final static String CHANNEL_PORTMIRROR = "portmirror";
    final static String CHANNEL_PORTMIRRORENABLED = "portmirrorenabled";

    final static String CHANNEL_VOLUME = "volume";
    final static String CHANNEL_VOLUME_MUTE = "volumemute";
    // final static String CHANNEL_RS232 = "rs232cmd";

    final static String CONFIG_HOSTNAME = "hostname";
    final static String CONFIG_OUTPUT = "output";

    // Preset commands
    final static String CMD_PRESETSAVE = "save";
    final static String CMD_PRESETRECALL = "recall";
    final static String CMD_PRESETCLEAR = "clear";

    // Matrix commands
    final static String CMD_MATRIXRESET = "resetmatrix";
    final static String CMD_MATRIXRESETPORTS = "resetports";
    final static String CMD_MATRIXPORTALL = "allports";

}
