/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mailserver;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link MailServerBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jereme Guenther - Initial contribution
 */
public class MailServerBindingConstants {

    public static final String BINDING_ID = "mailserver";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public final static ThingTypeUID THING_MailBox = new ThingTypeUID(BINDING_ID, "mailbox");

    // List of all Channel ids
    public final static String CHANNEL_ReceivedMessageCount = "receivedmessagecount";
    public final static String CHANNEL_MessageBody = "messagebody";
    public final static String CHANNEL_OpenHabCommand = "openhabcommand";
    public final static String CHANNEL_OpenHabValue = "openhabvalue";

    // List of Configuration Ids
    public final static String LISTEN_PORT = "port";

    public final static String TO_ADDRESS = "toaddress";
    public final static String PATTERN_COMMAND = "commandpattern";
    public final static String PATTERN_COMMAND_INDEX = "commandindex";
    public final static String PATTERN_VALUE = "valuepattern";
    public final static String PATTERN_VALUE_INDEX = "valueindex";
}
