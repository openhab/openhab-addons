/**
 * Copyright (c) 2014-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tivo;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link TiVoBinding} class defines common constants that are
 * used across the whole binding.
 *
 * @author Jayson Kubilis (DigitalBytes) - Initial contribution
 * @author Andrew Black (AndyXMB) - Addition of Min / Max Channel and channel scanning properties
 */

public class TiVoBindingConstants {
    public static final String BINDING_ID = "tivo";
    public static final int CONFIG_SOCKET_TIMEOUT = 1000;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_TIVO = new ThingTypeUID(BINDING_ID, "sckt");

    // List of all Channel ids
    public static final String CHANNEL_TIVO_CHANNEL_FORCE = "channelForce";
    public static final String CHANNEL_TIVO_CHANNEL_SET = "channelSet";
    public static final String CHANNEL_TIVO_TELEPORT = "menuTeleport";
    public static final String CHANNEL_TIVO_IRCMD = "irCommand";
    public static final String CHANNEL_TIVO_KBDCMD = "kbdCommand";
    public static final String CHANNEL_TIVO_STATUS = "dvrStatus";
    public static final String CHANNEL_TIVO_COMMAND = "customCmd";

    // List of all configuration Properties
    public static final String CONFIG_NAME = "deviceName";
    public static final String CONFIG_ADDRESS = "address";
    public static final String CONFIG_PORT = "tcpPort";
    public static final String CONFIG_CONNECTION_RETRY = "numRetry";
    public static final String CONFIG_KEEP_CONNECTION_OPEN = "keepConActive";
    public static final String CONFIG_POLL_FOR_CHANGES = "pollForChanges";
    public static final String CONFIG_POLL_INTERVAL = "pollInterval";
    public static final String CONFIG_CMD_WAIT_INTERVAL = "cmdWaitInterval";
    public static final String CONFIG_IGNORE_CHANNELS = "ignoreChannels";
    public static final String CONFIG_CH_START = "minChannel";
    public static final String CONFIG_CH_END = "maxChannel";
    public static final String CONFIG_IGNORE_SCAN = "ignoreChannelScan";

}
