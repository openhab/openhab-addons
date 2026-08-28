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
package org.openhab.binding.plivo.internal.action;

import static org.openhab.binding.plivo.internal.PlivoBindingConstants.ANSWER_TOKEN_PARAM;
import static org.openhab.binding.plivo.internal.PlivoBindingConstants.WEBHOOK_ANSWER;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plivo.internal.api.PlivoApiClient;
import org.openhab.binding.plivo.internal.api.PlivoApiException;
import org.openhab.binding.plivo.internal.handler.PlivoAccountHandler;
import org.openhab.binding.plivo.internal.handler.PlivoPhoneHandler;
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
 * Rule actions for the Plivo binding. Provides methods for sending SMS/MMS,
 * WhatsApp messages, and making voice calls.
 *
 * @author Sarvesh Patil - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = PlivoActions.class)
@ThingActionsScope(name = "plivo")
@NonNullByDefault
public class PlivoActions implements ThingActions {

    private final Logger logger = LoggerFactory.getLogger(PlivoActions.class);

    private @NonNullByDefault({}) PlivoPhoneHandler phoneHandler;

    @RuleAction(label = "send SMS", description = "Send an SMS message")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean sendSMS(
            @ActionInput(name = "to", label = "To", description = "Recipient phone number (E.164 format)", type = "java.lang.String", required = true) String to,
            @ActionInput(name = "message", label = "Message", description = "Message body", type = "java.lang.String", required = true) String message) {
        return sendSMS(to, message, null);
    }

    public static Boolean sendSMS(ThingActions actions, String to, String message) {
        return ((PlivoActions) actions).sendSMS(to, message);
    }

    @RuleAction(label = "send MMS", description = "Send an MMS message with media")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean sendSMS(
            @ActionInput(name = "to", label = "To", description = "Recipient phone number (E.164 format)", type = "java.lang.String", required = true) String to,
            @ActionInput(name = "message", label = "Message", description = "Message body (optional for MMS)", type = "java.lang.String", required = false) @Nullable String message,
            @ActionInput(name = "mediaUrl", label = "Media URL", description = "URL of media to attach", type = "java.lang.String") @Nullable String mediaUrl) {
        logger.trace("sendSMS called: to='{}', message='{}', mediaUrl='{}'", to, message, mediaUrl);
        PlivoPhoneHandler handler = getHandler();
        if (handler == null) {
            return false;
        }
        PlivoApiClient client = getApiClient(handler);
        if (client == null) {
            logger.warn("Cannot send message: the Plivo account is not available");
            return false;
        }
        try {
            client.sendMessage(handler.getPhoneNumber(), to, message, mediaUrl, handler.getStatusCallbackUrl());
            return true;
        } catch (PlivoApiException e) {
            logger.warn("Failed to send message to {}: {}", to, e.getMessage());
            return false;
        }
    }

    public static Boolean sendSMS(ThingActions actions, String to, @Nullable String message,
            @Nullable String mediaUrl) {
        return ((PlivoActions) actions).sendSMS(to, message, mediaUrl);
    }

    @RuleAction(label = "send WhatsApp", description = "Send a WhatsApp message")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean sendWhatsApp(
            @ActionInput(name = "to", label = "To", description = "Recipient phone number (E.164 format)", type = "java.lang.String", required = true) String to,
            @ActionInput(name = "message", label = "Message", description = "Message body", type = "java.lang.String", required = true) String message) {
        return sendWhatsApp(to, message, null);
    }

    public static Boolean sendWhatsApp(ThingActions actions, String to, String message) {
        return ((PlivoActions) actions).sendWhatsApp(to, message);
    }

    @RuleAction(label = "send WhatsApp with media", description = "Send a WhatsApp message with media")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean sendWhatsApp(
            @ActionInput(name = "to", label = "To", description = "Recipient phone number (E.164 format)", type = "java.lang.String", required = true) String to,
            @ActionInput(name = "message", label = "Message", description = "Message body (optional with media)", type = "java.lang.String", required = false) @Nullable String message,
            @ActionInput(name = "mediaUrl", label = "Media URL", description = "URL of media to attach", type = "java.lang.String") @Nullable String mediaUrl) {
        logger.trace("sendWhatsApp called: to='{}', message='{}', mediaUrl='{}'", to, message, mediaUrl);
        PlivoPhoneHandler handler = getHandler();
        if (handler == null) {
            return false;
        }
        PlivoApiClient client = getApiClient(handler);
        if (client == null) {
            logger.warn("Cannot send WhatsApp message: the Plivo account is not available");
            return false;
        }
        try {
            client.sendWhatsApp(handler.getPhoneNumber(), to, message, mediaUrl, handler.getStatusCallbackUrl());
            return true;
        } catch (PlivoApiException e) {
            logger.warn("Failed to send WhatsApp message to {}: {}", to, e.getMessage());
            return false;
        }
    }

    public static Boolean sendWhatsApp(ThingActions actions, String to, @Nullable String message,
            @Nullable String mediaUrl) {
        return ((PlivoActions) actions).sendWhatsApp(to, message, mediaUrl);
    }

    @RuleAction(label = "make call", description = "Make a voice call with Plivo XML")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean makeCall(
            @ActionInput(name = "to", label = "To", description = "Recipient phone number (E.164 format)", type = "java.lang.String", required = true) String to,
            @ActionInput(name = "xml", label = "XML", description = "Plivo XML instructions for the call", type = "java.lang.String", required = true) String xml) {
        logger.trace("makeCall called: to='{}', xml='{}'", to, xml);

        PlivoPhoneHandler handler = getHandler();
        if (handler == null) {
            return false;
        }
        PlivoApiClient client = getApiClient(handler);
        if (client == null) {
            logger.warn("Cannot make call: the Plivo account is not available");
            return false;
        }

        String answerBaseUrl = handler.getWebhookUrl(WEBHOOK_ANSWER);
        if (answerBaseUrl == null) {
            logger.warn(
                    "Cannot make call: configure publicUrl or useCloudWebhook on the bridge so Plivo can fetch the answer XML");
            return false;
        }

        String processedXml = handler.replaceXmlPlaceholders(xml);
        String token = handler.getCallbackServlet().createCallXmlEntry(processedXml);
        String answerUrl = answerBaseUrl + "?" + ANSWER_TOKEN_PARAM + "=" + token;

        try {
            client.makeCall(handler.getPhoneNumber(), to, answerUrl, handler.getStatusCallbackUrl());
            return true;
        } catch (PlivoApiException e) {
            logger.warn("Failed to make call to {}: {}", to, e.getMessage());
            return false;
        }
    }

    public static Boolean makeCall(ThingActions actions, String to, String xml) {
        return ((PlivoActions) actions).makeCall(to, xml);
    }

    @RuleAction(label = "make TTS call", description = "Make a voice call with text-to-speech")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean makeTTSCall(
            @ActionInput(name = "to", label = "To", description = "Recipient phone number (E.164 format)", type = "java.lang.String", required = true) String to,
            @ActionInput(name = "text", label = "Text", description = "Text to speak", type = "java.lang.String", required = true) String text) {
        return makeTTSCall(to, text, null);
    }

    public static Boolean makeTTSCall(ThingActions actions, String to, String text) {
        return ((PlivoActions) actions).makeTTSCall(to, text);
    }

    @RuleAction(label = "make TTS call with voice", description = "Make a voice call with text-to-speech using a specific voice")
    public @ActionOutput(label = "Success", type = "java.lang.Boolean") Boolean makeTTSCall(
            @ActionInput(name = "to", label = "To", description = "Recipient phone number (E.164 format)", type = "java.lang.String", required = true) String to,
            @ActionInput(name = "text", label = "Text", description = "Text to speak", type = "java.lang.String", required = true) String text,
            @ActionInput(name = "voice", label = "Voice", description = "Voice to use (e.g. 'WOMAN', 'Polly.Joanna')", type = "java.lang.String") @Nullable String voice) {
        logger.trace("makeTTSCall called: to='{}', text='{}', voice='{}'", to, text, voice);
        String escapedText = StringUtils.escapeXml(text);
        String voiceAttr = (voice != null && !voice.isBlank()) ? " voice=\"" + StringUtils.escapeXml(voice) + "\"" : "";
        String xml = "<Response><Speak" + voiceAttr + ">" + escapedText + "</Speak></Response>";
        return makeCall(to, xml);
    }

    public static Boolean makeTTSCall(ThingActions actions, String to, String text, @Nullable String voice) {
        return ((PlivoActions) actions).makeTTSCall(to, text, voice);
    }

    @RuleAction(label = "create item media URL", description = "Create a temporary public URL for an openHAB Image item")
    public @ActionOutput(label = "Media URL", type = "java.lang.String") @Nullable String createItemMediaUrl(
            @ActionInput(name = "itemName", label = "Item Name", description = "Name of an Image item", type = "java.lang.String", required = true) String itemName) {
        logger.trace("createItemMediaUrl called: itemName='{}'", itemName);

        PlivoPhoneHandler handler = getHandler();
        if (handler == null) {
            return null;
        }
        String mediaBaseUrl = handler.getMediaBaseUrl();
        if (mediaBaseUrl == null) {
            logger.debug("Cannot create media URL: neither publicUrl nor useCloudWebhook is configured on bridge");
            return null;
        }

        try {
            Item item = handler.getItemRegistry().getItem(itemName);
            State state = item.getState();
            if (state instanceof RawType rawType) {
                String uuid = handler.getCallbackServlet().createMediaEntry(rawType.getBytes(), rawType.getMimeType());
                return mediaBaseUrl + "/" + uuid;
            } else {
                logger.warn("Cannot create a media URL: item '{}' is not an Image item (state is {})", itemName,
                        state.getClass().getSimpleName());
                return null;
            }
        } catch (ItemNotFoundException e) {
            logger.warn("Cannot create a media URL: item '{}' was not found", itemName);
            return null;
        }
    }

    public static @Nullable String createItemMediaUrl(ThingActions actions, String itemName) {
        return ((PlivoActions) actions).createItemMediaUrl(itemName);
    }

    @RuleAction(label = "create proxy media URL", description = "Create a temporary public URL that proxies a local/internal URL")
    public @ActionOutput(label = "Media URL", type = "java.lang.String") @Nullable String createProxyMediaUrl(
            @ActionInput(name = "sourceUrl", label = "Source URL", description = "Local URL to proxy (e.g. http://192.168.1.100/snapshot.jpg)", type = "java.lang.String", required = true) String sourceUrl) {
        logger.trace("createProxyMediaUrl called: sourceUrl='{}'", sourceUrl);

        PlivoPhoneHandler handler = getHandler();
        if (handler == null) {
            return null;
        }
        String mediaBaseUrl = handler.getMediaBaseUrl();
        if (mediaBaseUrl == null) {
            logger.debug("Cannot create media URL: neither publicUrl nor useCloudWebhook is configured on bridge");
            return null;
        }

        String uuid = handler.getCallbackServlet().createProxyEntry(sourceUrl);
        return mediaBaseUrl + "/" + uuid;
    }

    public static @Nullable String createProxyMediaUrl(ThingActions actions, String sourceUrl) {
        return ((PlivoActions) actions).createProxyMediaUrl(sourceUrl);
    }

    @RuleAction(label = "respond with XML", description = "Respond to an active call with Plivo XML. Must be called during a call-received or dtmf-received trigger.")
    public void respondWithXml(
            @ActionInput(name = "callUuid", label = "Call UUID", description = "The CallUUID from the trigger event", type = "java.lang.String", required = true) String callUuid,
            @ActionInput(name = "xml", label = "XML", description = "Plivo XML response (e.g. <Response><Speak>Hello</Speak></Response>)", type = "java.lang.String", required = true) String xml) {
        logger.trace("respondWithXml called: callUuid='{}', xml='{}'", callUuid, xml);
        PlivoPhoneHandler handler = getHandler();
        if (handler == null) {
            return;
        }
        CompletableFuture<String> future = handler.getCallbackServlet().getPendingResponse(callUuid);
        if (future != null) {
            future.complete(xml);
        } else {
            logger.warn("No pending response for CallUUID {}; the responseTimeout may have elapsed already.", callUuid);
        }
    }

    public static void respondWithXml(ThingActions actions, String callUuid, String xml) {
        ((PlivoActions) actions).respondWithXml(callUuid, xml);
    }

    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        if (handler instanceof PlivoPhoneHandler plivoHandler) {
            this.phoneHandler = plivoHandler;
        }
    }

    @Override
    public @Nullable ThingHandler getThingHandler() {
        return phoneHandler;
    }

    private @Nullable PlivoPhoneHandler getHandler() {
        PlivoPhoneHandler handler = phoneHandler;
        if (handler == null) {
            logger.warn("Plivo action invoked but the Thing handler is not set");
        }
        return handler;
    }

    private @Nullable PlivoApiClient getApiClient(PlivoPhoneHandler handler) {
        PlivoAccountHandler accountHandler = handler.getAccountHandler();
        if (accountHandler != null) {
            return accountHandler.getApiClient();
        }
        return null;
    }
}
