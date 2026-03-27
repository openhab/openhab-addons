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
package org.openhab.binding.twilio.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TwilioBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class TwilioBindingConstants {

    public static final String BINDING_ID = "twilio";

    // Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_PHONE = new ThingTypeUID(BINDING_ID, "phone");

    // Channel IDs - Message state channels
    public static final String CHANNEL_LAST_MESSAGE_BODY = "lastMessageBody";
    public static final String CHANNEL_LAST_MESSAGE_FROM = "lastMessageFrom";
    public static final String CHANNEL_LAST_MESSAGE_DATE = "lastMessageDate";
    public static final String CHANNEL_LAST_MESSAGE_MEDIA_URL = "lastMessageMediaUrl";
    public static final String CHANNEL_LAST_MESSAGE_SID = "lastMessageSid";

    // Channel IDs - Call state channels
    public static final String CHANNEL_LAST_CALL_FROM = "lastCallFrom";
    public static final String CHANNEL_LAST_CALL_STATUS = "lastCallStatus";
    public static final String CHANNEL_LAST_CALL_DATE = "lastCallDate";
    public static final String CHANNEL_LAST_DTMF_DIGITS = "lastDtmfDigits";

    // Channel IDs - Trigger channels
    public static final String CHANNEL_SMS_RECEIVED = "smsReceived";
    public static final String CHANNEL_WHATSAPP_RECEIVED = "whatsappReceived";
    public static final String CHANNEL_CALL_RECEIVED = "callReceived";
    public static final String CHANNEL_DTMF_RECEIVED = "dtmfReceived";
    public static final String CHANNEL_MESSAGE_STATUS = "messageStatus";

    // Twilio API
    public static final String API_BASE_URL = "https://api.twilio.com/2010-04-01/Accounts/";

    // Webhook servlet
    public static final String SERVLET_PATH = "/twilio/callback";

    // Thing properties
    public static final String PROPERTY_SMS_WEBHOOK_URL = "smsWebhookUrl";
    public static final String PROPERTY_VOICE_WEBHOOK_URL = "voiceWebhookUrl";
    public static final String PROPERTY_STATUS_CALLBACK_URL = "statusCallbackUrl";

    // Default TwiML templates
    public static final String DEFAULT_VOICE_GREETING = "<Response><Gather numDigits=\"1\" action=\"{gatherUrl}\"><Say>Hello. This is the open hab smart home system. Press any key.</Say></Gather><Say>No input received. Goodbye.</Say></Response>";
    public static final String DEFAULT_GATHER_RESPONSE = "<Response><Say>Thank you. Goodbye.</Say></Response>";
    public static final String EMPTY_TWIML_RESPONSE = "<Response/>";

    // Webhook path segments
    public static final String WEBHOOK_SMS = "sms";
    public static final String WEBHOOK_WHATSAPP = "whatsapp";
    public static final String WEBHOOK_VOICE = "voice";
    public static final String WEBHOOK_GATHER = "gather";
    public static final String WEBHOOK_STATUS = "status";
    public static final String WEBHOOK_MEDIA = "media";

    // Media serving
    public static final int MEDIA_EXPIRY_MINUTES = 5;

    // TwiML response timeout
    public static final int DEFAULT_RESPONSE_TIMEOUT = 10;
}
