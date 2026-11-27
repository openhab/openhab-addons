/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.util.regex.Pattern;

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

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_YIOREMOTEDOCK = new ThingTypeUID(BINDING_ID, "yioRemoteDock");

    // List of all Channel Groups Group Channel ids
    public static final String GROUP_INPUT = "input";
    public static final String GROUP_OUTPUT = "output";

    // List of all Channel ids
    public static final String RECEIVER_SWITCH_CHANNEL = "receiverswitch";
    public static final String STATUS_STRING_CHANNEL = "status";

    // YIO Dock WebSocket connection
    public static final int DOCK_PORT = 946;
    public static final int HEARTBEAT_INTERVAL_SECONDS = 40;

    // YIO Dock WebSocket protocol
    public static final String MESSAGE_TYPE_AUTH = "auth";
    public static final String MESSAGE_TYPE_AUTH_REQUIRED = "auth_required";
    public static final String MESSAGE_TYPE_AUTH_OK = "auth_ok";
    public static final String MESSAGE_TYPE_DOCK = "dock";
    public static final String COMMAND_PING = "ping";
    public static final String COMMAND_IR_SEND = "ir_send";
    public static final String COMMAND_IR_RECEIVE = "ir_receive";
    public static final String COMMAND_IR_RECEIVE_ON = "ir_receive_on";
    public static final String COMMAND_IR_RECEIVE_OFF = "ir_receive_off";
    public static final String MESSAGE_PONG = "pong";
    public static final String IR_CODE_FORMAT_HEX = "hex";

    // IR code format, e.g. "3;0x20DF40BF;32;0"
    public static final Pattern IR_CODE_PATTERN = Pattern.compile("[0-9]?[0-9];0[xX][0-9a-fA-F]+;[0-9]+;[0-9]");

    // Values published on the status channel
    public static final String STATUS_IR_SEND_SUCCESS = "Send IR Code successfully";
    public static final String STATUS_IR_SEND_FAILURE = "Send IR Code failure";
}
