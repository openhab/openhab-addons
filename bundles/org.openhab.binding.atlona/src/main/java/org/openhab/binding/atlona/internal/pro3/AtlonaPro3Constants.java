/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.atlona.internal.pro3;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AtlonaPro3Binding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
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
