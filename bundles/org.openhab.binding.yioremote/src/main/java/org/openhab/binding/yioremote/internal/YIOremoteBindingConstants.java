/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.yioremote.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link YIOremoteBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Loercher - Initial contribution
 */
@NonNullByDefault
public class YIOremoteBindingConstants {

    public static final String BINDING_ID = "yioremote";

    // List of all used global variables
    public static enum YioRemoteDockHandleStatus {
        UNINITIALIZED_STATE,
        AUTHENTICATION_PROCESS,
        AUTHENTICATION_FAILED,
        AUTHENTICATION_COMPLETE,
        SEND_PING,
        CHECK_PONG,
        CONNECTION_FAILED,
        CONNECTION_ESTABLISHED,
        COMMUNICATION_ERROR,
        RECONNECTION_PROCESS;
    }

    public static enum YioRemoteMessages {
        IR_SEND,
        AUTHENTICATE_MESSAGE,
        HEARTBEAT_MESSAGE,
        IR_RECEIVER_ON,
        IR_RECEIVER_OFF;
    }

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_YIOREMOTEDOCK = new ThingTypeUID(BINDING_ID, "yioRemoteDock");

    // List of all Channel Groups Group Channel ids
    public static final String GROUP_INPUT = "input";
    public static final String GROUP_OUTPUT = "output";

    // List of all Channel ids
    public static final String RECEIVER_SWITCH_CHANNEL = "receiverswitch";
    public static final String STATUS_STRING_CHANNEL = "status";

    // Configuration elements
    public static final String CONFIG_YIODOCKHOST = "host";
    public static final String CONFIG_YIODOCKACCESSTOKEN = "accesstoken";
}
