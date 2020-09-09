/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link YIOremoteBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Loercher - Initial contribution
 */
@NonNullByDefault
public class YIOremoteBindingConstants {

    private static final String BINDING_ID = "yioremote";

    // List of all used global variables
    public static enum YIO_REMOTE_DOCK_HANDLE_STATUS {
        UNINITIALIZED,
        AUTHENTICATION_PROCESS,
        AUTHENTICATION_PROCESS_FAILED,
        AUTHENTICATED,
        AUTHENTICATED_FAILED,
        CONNECTION_FAILED,
        CONNECTION_ESTABLISHED;
    }

    public static enum YIOREMOTEMESSAGETYPE {
        IRSEND,
        AUTHENTICATE,
        HEARTBEAT,
        IRRECEIVERON,
        IRRECEIVEROFF;
    }

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_YIOREMOTEDOCK = new ThingTypeUID(BINDING_ID, "yioremotedock");

    // List of all Channel Groups Group Channel ids
    public static final String GROUP_INPUT = "input";
    public static final String GROUP_OUTPUT = "output";

    // List of all Channel ids
    public static final String YIODOCKRECEIVERSWITCH = "receiverswitch";
    public static final String YIODOCKSENDIRCODE = "sendircode";
    public static final String YIODOCKSTATUS = "status";

    // Configuration elements
    public static final String CONFIG_YIODOCKHOST = "host";
    public static final String CONFIG_YIODOCKACCESSTOKEN = "accesstoken";
}
