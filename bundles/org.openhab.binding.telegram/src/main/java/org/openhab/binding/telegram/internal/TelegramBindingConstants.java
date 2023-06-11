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
package org.openhab.binding.telegram.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TelegramBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jens Runge - Initial contribution
 */
@NonNullByDefault
public class TelegramBindingConstants {

    private static final String BINDING_ID = "telegram";
    public static final Set<String> PHOTO_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".jpe", ".jif", ".jfif",
            ".jfi", ".webp");

    // List of all Thing Type UIDs
    public static final ThingTypeUID TELEGRAM_THING = new ThingTypeUID(BINDING_ID, "telegramBot");

    // List of all Channel ids
    public static final String LASTMESSAGETEXT = "lastMessageText";
    public static final String LASTMESSAGEURL = "lastMessageURL";
    public static final String LASTMESSAGEDATE = "lastMessageDate";
    public static final String LASTMESSAGENAME = "lastMessageName";
    public static final String LASTMESSAGEUSERNAME = "lastMessageUsername";
    public static final String CHATID = "chatId";
    public static final String REPLYID = "replyId";
    public static final String LONGPOLLINGTIME = "longPollingTime";
    public static final String MESSAGEEVENT = "messageEvent";
    public static final String MESSAGERAWEVENT = "messageRawEvent";
    public static final String CALLBACKEVENT = "callbackEvent";
    public static final String CALLBACKRAWEVENT = "callbackRawEvent";
}
