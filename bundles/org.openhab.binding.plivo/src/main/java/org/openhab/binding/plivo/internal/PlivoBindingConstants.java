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
package org.openhab.binding.plivo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link PlivoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sarvesh Patil - Initial contribution
 */
@NonNullByDefault
public class PlivoBindingConstants {

    public static final String BINDING_ID = "plivo";

    // Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_PHONE = new ThingTypeUID(BINDING_ID, "phone");

    // Channel IDs - Message state channels
    public static final String CHANNEL_LAST_MESSAGE_BODY = "last-message-body";
    public static final String CHANNEL_LAST_MESSAGE_FROM = "last-message-from";
    public static final String CHANNEL_LAST_MESSAGE_DATE = "last-message-date";
    public static final String CHANNEL_LAST_MESSAGE_MEDIA_URL = "last-message-media-url";
    public static final String CHANNEL_LAST_MESSAGE_UUID = "last-message-uuid";

    // Channel IDs - Call state channels
    public static final String CHANNEL_LAST_CALL_FROM = "last-call-from";
    public static final String CHANNEL_LAST_CALL_STATUS = "last-call-status";
    public static final String CHANNEL_LAST_CALL_DATE = "last-call-date";
    public static final String CHANNEL_LAST_DTMF_DIGITS = "last-dtmf-digits";

    // Channel IDs - Trigger channels
    public static final String CHANNEL_SMS_RECEIVED = "sms-received";
    public static final String CHANNEL_WHATSAPP_RECEIVED = "whatsapp-received";
    public static final String CHANNEL_CALL_RECEIVED = "call-received";
    public static final String CHANNEL_DTMF_RECEIVED = "dtmf-received";
    public static final String CHANNEL_MESSAGE_STATUS = "message-status";
    public static final String CHANNEL_CALL_STATUS_TRIGGER = "call-status-update";

    // Plivo API
    public static final String API_BASE_URL = "https://api.plivo.com/v1/Account/";

    // Webhook servlet
    public static final String SERVLET_PATH = "/plivo/callback";

    // Thing properties
    public static final String PROPERTY_MESSAGE_WEBHOOK_URL = "messageWebhookUrl";
    public static final String PROPERTY_VOICE_WEBHOOK_URL = "voiceWebhookUrl";
    public static final String PROPERTY_STATUS_CALLBACK_URL = "statusCallbackUrl";

    // Default Plivo XML templates
    public static final String DEFAULT_VOICE_GREETING = "<Response><GetInput action=\"{gatherUrl}\" inputType=\"dtmf\" numDigits=\"1\"><Speak>Hello. This is the openHAB smart home system. Press any key.</Speak></GetInput><Speak>No input received. Goodbye.</Speak></Response>";
    public static final String DEFAULT_GATHER_RESPONSE = "<Response><Speak>Thank you. Goodbye.</Speak></Response>";
    public static final String EMPTY_XML_RESPONSE = "<Response/>";

    // Webhook path segments
    public static final String WEBHOOK_SMS = "sms";
    public static final String WEBHOOK_VOICE = "voice";
    public static final String WEBHOOK_GATHER = "gather";
    public static final String WEBHOOK_STATUS = "status";
    public static final String WEBHOOK_ANSWER = "answer";
    public static final String WEBHOOK_MEDIA = "media";

    // Outbound answer XML token parameter
    public static final String ANSWER_TOKEN_PARAM = "token";

    // Media serving
    public static final int MEDIA_EXPIRY_MINUTES = 5;
    public static final int MAX_PROXY_MEDIA_BYTES = 5 * 1024 * 1024;

    // XML response timeout
    public static final int DEFAULT_RESPONSE_TIMEOUT = 10;
}
