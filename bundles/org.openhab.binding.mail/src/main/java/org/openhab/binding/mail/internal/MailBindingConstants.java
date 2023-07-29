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
package org.openhab.binding.mail.internal;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link MailBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class MailBindingConstants {
    private static final String BINDING_ID = "mail";

    public static final ThingTypeUID THING_TYPE_SMTPSERVER = new ThingTypeUID(BINDING_ID, "smtp");
    public static final ThingTypeUID THING_TYPE_IMAPSERVER = new ThingTypeUID(BINDING_ID, "imap");
    public static final ThingTypeUID THING_TYPE_POP3SERVER = new ThingTypeUID(BINDING_ID, "pop3");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = new HashSet<>(
            Arrays.asList(THING_TYPE_SMTPSERVER, THING_TYPE_IMAPSERVER, THING_TYPE_POP3SERVER));

    public static final ChannelTypeUID CHANNEL_TYPE_UID_FOLDER_MAILCOUNT = new ChannelTypeUID(BINDING_ID, "mailcount");
    public static final ChannelTypeUID CHANNEL_TYPE_UID_MAIL_CONTENT = new ChannelTypeUID(BINDING_ID, "content");
}
