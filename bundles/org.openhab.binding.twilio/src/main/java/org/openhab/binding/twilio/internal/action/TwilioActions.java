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
package org.openhab.binding.twilio.internal.action;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.twilio.internal.api.TwilioApiClient;
import org.openhab.binding.twilio.internal.api.TwilioApiException;
import org.openhab.binding.twilio.internal.handler.TwilioAccountHandler;
import org.openhab.binding.twilio.internal.handler.TwilioPhoneHandler;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.library.types.RawType;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.State;
import org.openhab.core.util.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Rule actions for the Twilio binding. Provides methods for sending SMS/MMS,
 * WhatsApp messages, and making voice calls.
 *
 * @author Dan Cunningham - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = TwilioActions.class)
@ThingActionsScope(name = "twilio")
@NonNullByDefault
public class TwilioActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(TwilioActions.class);

    private @NonNullByDefault({}) TwilioPhoneHandler phoneHandler;

    // --- SMS Actions ---

    @RuleAction(label = "send SMS", description = "Send an SMS message")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean sendSMS(
            @ActionInput(name = "to", label = "To", description = "Recipient phone number (E.164 format)", type = "java.lang.String", required = true) String to,
            @ActionInput(name = "message", label = "Message", description = "Message body", type = "java.lang.String", required = true) String message) {
        return sendSMS(to, message, null);
    }

    public static Boolean sendSMS(ThingActions actions, String to, String message) {
        return ((TwilioActions) actions).sendSMS(to, message);
    }

    @RuleAction(label = "send MMS", description = "Send an MMS message with media")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean sendSMS(
            @ActionInput(name = "to", label = "To", description = "Recipient phone number (E.164 format)", type = "java.lang.String", required = true) String to,
            @ActionInput(name = "message", label = "Message", description = "Message body (optional for MMS)", type = "java.lang.String", required = false) @Nullable String message,
            @ActionInput(name = "mediaUrl", label = "Media URL", description = "URL of media to attach", type = "java.lang.String") @Nullable String mediaUrl) {
        logger.trace("sendSMS called: to='{}', message='{}', mediaUrl='{}'", to, message, mediaUrl);
        return doSendMessage(getPhoneNumber(), to, message, mediaUrl);
    }

    public static Boolean sendSMS(ThingActions actions, String to, @Nullable String message,
            @Nullable String mediaUrl) {
        return ((TwilioActions) actions).sendSMS(to, message, mediaUrl);
    }

    // --- WhatsApp Actions ---

    @RuleAction(label = "send WhatsApp", description = "Send a WhatsApp message")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean sendWhatsApp(
            @ActionInput(name = "to", label = "To", description = "Recipient phone number (E.164 format)", type = "java.lang.String", required = true) String to,
            @ActionInput(name = "message", label = "Message", description = "Message body", type = "java.lang.String", required = true) String message) {
        return sendWhatsApp(to, message, null);
    }

    public static Boolean sendWhatsApp(ThingActions actions, String to, String message) {
        return ((TwilioActions) actions).sendWhatsApp(to, message);
    }

    @RuleAction(label = "send WhatsApp with media", description = "Send a WhatsApp message with media")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean sendWhatsApp(
            @ActionInput(name = "to", label = "To", description = "Recipient phone number (E.164 format)", type = "java.lang.String", required = true) String to,
            @ActionInput(name = "message", label = "Message", description = "Message body (optional with media)", type = "java.lang.String", required = false) @Nullable String message,
            @ActionInput(name = "mediaUrl", label = "Media URL", description = "URL of media to attach", type = "java.lang.String") @Nullable String mediaUrl) {
        logger.trace("sendWhatsApp called: to='{}', message='{}', mediaUrl='{}'", to, message, mediaUrl);
        String whatsappFrom = "whatsapp:" + getPhoneNumber();
        String whatsappTo = to.startsWith("whatsapp:") ? to : "whatsapp:" + to;
        return doSendMessage(whatsappFrom, whatsappTo, message, mediaUrl);
    }

    public static Boolean sendWhatsApp(ThingActions actions, String to, @Nullable String message,
            @Nullable String mediaUrl) {
        return ((TwilioActions) actions).sendWhatsApp(to, message, mediaUrl);
    }

    // --- Voice Actions ---

    @RuleAction(label = "make call", description = "Make a voice call with TwiML")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean makeCall(
            @ActionInput(name = "to", label = "To", description = "Recipient phone number (E.164 format)", type = "java.lang.String", required = true) String to,
            @ActionInput(name = "twiml", label = "TwiML", description = "TwiML instructions for the call", type = "java.lang.String", required = true) String twiml) {
        logger.trace("makeCall called: to='{}', twiml='{}'", to, twiml);

        TwilioApiClient client = getApiClient();
        if (client == null) {
            logger.debug("Cannot make call: API client not available");
            return false;
        }

        try {
            String processedTwiml = phoneHandler.replaceTwimlPlaceholders(twiml);
            client.makeCall(getPhoneNumber(), to, processedTwiml, phoneHandler.getStatusCallbackUrl());
            return true;
        } catch (TwilioApiException e) {
            logger.debug("Failed to make call: {}", e.getMessage());
            return false;
        }
    }

    public static Boolean makeCall(ThingActions actions, String to, String twiml) {
        return ((TwilioActions) actions).makeCall(to, twiml);
    }

    @RuleAction(label = "make TTS call", description = "Make a voice call with text-to-speech")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean makeTTSCall(
            @ActionInput(name = "to", label = "To", description = "Recipient phone number (E.164 format)", type = "java.lang.String", required = true) String to,
            @ActionInput(name = "text", label = "Text", description = "Text to speak", type = "java.lang.String", required = true) String text) {
        return makeTTSCall(to, text, null);
    }

    public static Boolean makeTTSCall(ThingActions actions, String to, String text) {
        return ((TwilioActions) actions).makeTTSCall(to, text);
    }

    @RuleAction(label = "make TTS call with voice", description = "Make a voice call with text-to-speech using a specific voice")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean makeTTSCall(
            @ActionInput(name = "to", label = "To", description = "Recipient phone number (E.164 format)", type = "java.lang.String", required = true) String to,
            @ActionInput(name = "text", label = "Text", description = "Text to speak", type = "java.lang.String", required = true) String text,
            @ActionInput(name = "voice", label = "Voice", description = "Voice to use (e.g. 'alice', 'Polly.Joanna')", type = "java.lang.String") @Nullable String voice) {
        logger.trace("makeTTSCall called: to='{}', text='{}', voice='{}'", to, text, voice);
        String escapedText = StringUtils.escapeXml(text);
        String voiceAttr = (voice != null && !voice.isBlank()) ? " voice=\"" + StringUtils.escapeXml(voice) + "\"" : "";
        String twiml = "<Response><Say" + voiceAttr + ">" + escapedText + "</Say></Response>";
        return makeCall(to, twiml);
    }

    public static Boolean makeTTSCall(ThingActions actions, String to, String text, @Nullable String voice) {
        return ((TwilioActions) actions).makeTTSCall(to, text, voice);
    }

    // --- Media URL Actions ---

    @RuleAction(label = "create item media URL", description = "Create a temporary public URL for an openHAB Image item")
    public @ActionOutput(label = "Media URL", type = "java.lang.String") @Nullable String createItemMediaUrl(
            @ActionInput(name = "itemName", label = "Item Name", description = "Name of an Image item", type = "java.lang.String", required = true) String itemName) {
        logger.trace("createItemMediaUrl called: itemName='{}'", itemName);

        String mediaBaseUrl = phoneHandler.getMediaBaseUrl();
        if (mediaBaseUrl == null) {
            logger.debug("Cannot create media URL: publicUrl not configured on bridge");
            return null;
        }

        try {
            Item item = phoneHandler.getItemRegistry().getItem(itemName);
            State state = item.getState();
            if (state instanceof RawType rawType) {
                String uuid = phoneHandler.getCallbackServlet().createMediaEntry(rawType.getBytes(),
                        rawType.getMimeType());
                return mediaBaseUrl + "/" + uuid;
            } else {
                logger.debug("Item '{}' state is not RawType (Image), got: {}", itemName,
                        state.getClass().getSimpleName());
                return null;
            }
        } catch (ItemNotFoundException e) {
            logger.debug("Item '{}' not found", itemName);
            return null;
        }
    }

    public static @Nullable String createItemMediaUrl(ThingActions actions, String itemName) {
        return ((TwilioActions) actions).createItemMediaUrl(itemName);
    }

    @RuleAction(label = "create proxy media URL", description = "Create a temporary public URL that proxies a local/internal URL")
    public @ActionOutput(label = "Media URL", type = "java.lang.String") @Nullable String createProxyMediaUrl(
            @ActionInput(name = "sourceUrl", label = "Source URL", description = "Local URL to proxy (e.g. http://192.168.1.100/snapshot.jpg)", type = "java.lang.String", required = true) String sourceUrl) {
        logger.trace("createProxyMediaUrl called: sourceUrl='{}'", sourceUrl);

        String mediaBaseUrl = phoneHandler.getMediaBaseUrl();
        if (mediaBaseUrl == null) {
            logger.debug("Cannot create media URL: publicUrl not configured on bridge");
            return null;
        }

        String uuid = phoneHandler.getCallbackServlet().createProxyEntry(sourceUrl);
        return mediaBaseUrl + "/" + uuid;
    }

    public static @Nullable String createProxyMediaUrl(ThingActions actions, String sourceUrl) {
        return ((TwilioActions) actions).createProxyMediaUrl(sourceUrl);
    }

    // --- TwiML Response Actions ---

    @RuleAction(label = "respond with TwiML", description = "Respond to an active call with TwiML. Must be called during a call-received or dtmf-received trigger.")
    public void respondWithTwiml(
            @ActionInput(name = "callSid", label = "Call SID", description = "The CallSid from the trigger event", type = "java.lang.String", required = true) String callSid,
            @ActionInput(name = "twiml", label = "TwiML", description = "TwiML response (e.g. <Response><Say>Hello</Say></Response>)", type = "java.lang.String", required = true) String twiml) {
        logger.trace("respondWithTwiml called: callSid='{}', twiml='{}'", callSid, twiml);
        CompletableFuture<String> future = phoneHandler.getCallbackServlet().getPendingResponse(callSid);
        if (future != null) {
            future.complete(twiml);
        } else {
            logger.debug("No pending response found for CallSid {}. The response timeout may have elapsed.", callSid);
        }
    }

    public static void respondWithTwiml(ThingActions actions, String callSid, String twiml) {
        ((TwilioActions) actions).respondWithTwiml(callSid, twiml);
    }

    // --- ThingActions interface ---

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.phoneHandler = (TwilioPhoneHandler) handler;
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return phoneHandler;
    }

    // --- Private helpers ---

    private Boolean doSendMessage(String from, String to, @Nullable String message, @Nullable String mediaUrl) {
        TwilioApiClient client = getApiClient();
        if (client == null) {
            logger.debug("Cannot send message: API client not available");
            return false;
        }

        try {
            client.sendMessage(from, to, message, mediaUrl, phoneHandler.getStatusCallbackUrl());
            return true;
        } catch (TwilioApiException e) {
            logger.debug("Failed to send message: {}", e.getMessage());
            return false;
        }
    }

    private @Nullable TwilioApiClient getApiClient() {
        TwilioAccountHandler accountHandler = phoneHandler.getAccountHandler();
        if (accountHandler != null) {
            return accountHandler.getApiClient();
        }
        return null;
    }

    private String getPhoneNumber() {
        return phoneHandler.getPhoneNumber();
    }
}
