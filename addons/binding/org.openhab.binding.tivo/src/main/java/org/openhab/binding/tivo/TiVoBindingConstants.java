/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tivo;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link TiVoBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jayson Kubilis - Initial contribution
 */
public class TiVoBindingConstants {

    public static final String BINDING_ID = "tivo";

    public static final int CONFIG_SOCKET_TIMEOUT = 1000;

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_TIVO = new ThingTypeUID(BINDING_ID, "sckt");

    // List of all Channel ids
    public final static String CHANNEL_TIVO_CHANNEL_FORCE = "tivoChannelForce";
    public final static String CHANNEL_TIVO_CHANNEL_SET = "tivoChannelSet";
    public final static String CHANNEL_TIVO_TELEPORT = "tivoTeleport";
    public final static String CHANNEL_TIVO_IRCMD = "tivoIRCommand";
    public final static String CHANNEL_TIVO_KBDCMD = "tivoKBDCommand";
    public final static String CHANNEL_TIVO_COMMAND = "tivoCommand";
    public final static String CHANNEL_TIVO_STATUS = "tivoStatus";

    // List of all Configuration Elements
    public final static String CONFIG_NAME = "deviceName";
    public final static String CONFIG_ADDRESS = "address";
    public final static String CONFIG_PORT = "tcpPort";
    public final static String CONFIG_CONNECTION_RETRY = "numRetry";
    public final static String CONFIG_KEEP_CONNECTION_OPEN = "keepConActive";
    public final static String CONFIG_POLL_FOR_CHANGES = "pollForChanges";
    public final static String CONFIG_POLL_INTERVAL = "pollInterval";
    public final static String CONFIG_CMD_WAIT_INTERVAL = "cmdWaitInterval";
    public final static String CONFIG_IGNORE_CHANNELS = "ignoreChannels";

}
