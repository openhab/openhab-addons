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
package org.openhab.binding.ekey.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EkeyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public class EkeyBindingConstants {

    public static final String BINDING_ID = "ekey";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CVLAN = new ThingTypeUID(BINDING_ID, "cvlan");

    // List of all Channel ids
    public static final String CHANNEL_TYPE_USERID = "userId";
    public static final String CHANNEL_TYPE_USERNAME = "userName";
    public static final String CHANNEL_TYPE_USERSTATUS = "userStatus";
    public static final String CHANNEL_TYPE_FINGERID = "fingerId";
    public static final String CHANNEL_TYPE_KEYID = "keyId";
    public static final String CHANNEL_TYPE_FSSERIAL = "fsSerial";
    public static final String CHANNEL_TYPE_FSNAME = "fsName";
    public static final String CHANNEL_TYPE_ACTION = "action";
    public static final String CHANNEL_TYPE_INPUTID = "inputId";
    public static final String CHANNEL_TYPE_RELAYID = "relayId";
    public static final String CHANNEL_TYPE_TERMID = "termId";
    public static final String CHANNEL_TYPE_RESERVED = "relayId";
    public static final String CHANNEL_TYPE_EVENT = "event";
    public static final String CHANNEL_TYPE_TIMESTAMP = "timestamp";
}
