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
package org.openhab.binding.twilio.internal.handler;

import static org.openhab.binding.twilio.internal.TwilioBindingConstants.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.twilio.internal.action.TwilioActions;
import org.openhab.binding.twilio.internal.api.TwilioApiClient;
import org.openhab.binding.twilio.internal.api.TwilioApiException;
import org.openhab.binding.twilio.internal.config.TwilioAccountConfiguration;
import org.openhab.binding.twilio.internal.config.TwilioPhoneConfiguration;
import org.openhab.binding.twilio.internal.servlet.TwilioCallbackServlet;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link TwilioPhoneHandler} handles a Twilio phone number thing.
 * It manages channels for incoming messages/calls and provides actions for sending.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class TwilioPhoneHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(TwilioPhoneHandler.class);

    private final TwilioCallbackServlet callbackServlet;
    private final ItemRegistry itemRegistry;
    private TwilioPhoneConfiguration config = new TwilioPhoneConfiguration();
    private @Nullable String phoneNumber;

    public TwilioPhoneHandler(Thing thing, TwilioCallbackServlet callbackServlet, ItemRegistry itemRegistry) {
        super(thing);
        this.callbackServlet = callbackServlet;
        this.itemRegistry = itemRegistry;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // All channels are read-only or trigger-only
    }

    @Override
    public void initialize() {
        config = getConfigAs(TwilioPhoneConfiguration.class);

        String number = config.phoneNumber;
        if (number == null || number.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Phone number is required");
            return;
        }
        phoneNumber = number;

        updateStatus(ThingStatus.UNKNOWN);
        scheduler.submit(this::asyncInitialize);
    }

    @Override
    public void dispose() {
        callbackServlet.unregisterHandler(thing.getUID().getAsString());
        super.dispose();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            scheduler.submit(this::asyncInitialize);
        } else {
            callbackServlet.unregisterHandler(thing.getUID().getAsString());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge is not online");
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(TwilioActions.class);
    }

    /**
     * Returns the bridge handler for this phone thing.
     *
     * @return the bridge handler, or null if not available
     */
    public @Nullable TwilioAccountHandler getAccountHandler() {
        Bridge bridge = getBridge();
        if (bridge != null) {
            return (TwilioAccountHandler) bridge.getHandler();
        }
        return null;
    }

    /**
     * Returns the configured phone number.
     *
     * @return the phone number in E.164 format
     */
    public @Nullable String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Returns the phone configuration.
     */
    public TwilioPhoneConfiguration getPhoneConfig() {
        return config;
    }

    /**
     * Returns the item registry for looking up openHAB items.
     */
    public ItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    /**
     * Returns the configured response timeout in seconds.
     */
    public int getResponseTimeout() {
        return config.responseTimeout;
    }

    /**
     * Returns the webhook base URL for this phone thing, or null if publicUrl is not configured.
     */
    public @Nullable String getWebhookBaseUrl() {
        TwilioAccountHandler accountHandler = getAccountHandler();
        if (accountHandler == null) {
            return null;
        }
        String publicUrl = accountHandler.getAccountConfig().publicUrl;
        if (publicUrl == null || publicUrl.isBlank()) {
            return null;
        }
        if (publicUrl.endsWith("/")) {
            publicUrl = publicUrl.substring(0, publicUrl.length() - 1);
        }
        return publicUrl + SERVLET_PATH + "/" + thing.getUID().getAsString();
    }

    /**
     * Returns the media serving base URL, or null if publicUrl is not configured.
     * Media URLs don't include the thingUID since UUIDs are globally unique.
     */
    public @Nullable String getMediaBaseUrl() {
        TwilioAccountHandler accountHandler = getAccountHandler();
        if (accountHandler == null) {
            return null;
        }
        String publicUrl = accountHandler.getAccountConfig().publicUrl;
        if (publicUrl == null || publicUrl.isBlank()) {
            return null;
        }
        if (publicUrl.endsWith("/")) {
            publicUrl = publicUrl.substring(0, publicUrl.length() - 1);
        }
        return publicUrl + SERVLET_PATH + "/" + WEBHOOK_MEDIA;
    }

    /**
     * Returns a reference to the callback servlet for media operations.
     */
    public TwilioCallbackServlet getCallbackServlet() {
        return callbackServlet;
    }

    /**
     * Called by the servlet when an SMS/MMS is received.
     */
    public void handleIncomingSms(Map<String, String> params) {
        String from = params.getOrDefault("From", "");
        String body = params.getOrDefault("Body", "");
        String messageSid = params.getOrDefault("MessageSid", "");
        String numMedia = params.getOrDefault("NumMedia", "0");

        updateState(CHANNEL_LAST_MESSAGE_BODY, new StringType(body));
        updateState(CHANNEL_LAST_MESSAGE_FROM, new StringType(from));
        updateState(CHANNEL_LAST_MESSAGE_DATE, new DateTimeType(ZonedDateTime.now(ZoneId.systemDefault())));
        updateState(CHANNEL_LAST_MESSAGE_SID, new StringType(messageSid));

        String mediaUrl = params.getOrDefault("MediaUrl0", "");
        updateState(CHANNEL_LAST_MESSAGE_MEDIA_URL, new StringType(mediaUrl));

        // Build JSON payload for trigger
        JsonObject payload = new JsonObject();
        payload.addProperty("from", from);
        payload.addProperty("to", params.getOrDefault("To", ""));
        payload.addProperty("body", body);
        payload.addProperty("messageSid", messageSid);
        payload.addProperty("numMedia", numMedia);

        // Add media URLs as a JSON array
        int mediaCount = 0;
        try {
            mediaCount = Integer.parseInt(numMedia);
        } catch (NumberFormatException e) {
            // ignore
        }
        com.google.gson.JsonArray mediaUrlsArray = new com.google.gson.JsonArray();
        for (int i = 0; i < mediaCount; i++) {
            mediaUrlsArray.add(params.getOrDefault("MediaUrl" + i, ""));
        }
        payload.add("mediaUrls", mediaUrlsArray);

        // Determine if SMS or WhatsApp based on From prefix
        if (from.startsWith("whatsapp:")) {
            triggerChannel(CHANNEL_WHATSAPP_RECEIVED, payload.toString());
        } else {
            triggerChannel(CHANNEL_SMS_RECEIVED, payload.toString());
        }

        logger.debug("Received message from {} with body: {}", from, body);
    }

    /**
     * Called by the servlet when an incoming voice call is received.
     */
    public void handleIncomingCall(Map<String, String> params) {
        String from = params.getOrDefault("From", "");
        String callSid = params.getOrDefault("CallSid", "");
        String callStatus = params.getOrDefault("CallStatus", "");

        // Update state channels
        updateState(CHANNEL_LAST_CALL_FROM, new StringType(from));
        updateState(CHANNEL_LAST_CALL_STATUS, new StringType(callStatus));
        updateState(CHANNEL_LAST_CALL_DATE, new DateTimeType(ZonedDateTime.now(ZoneId.systemDefault())));

        // Build JSON payload for trigger
        JsonObject payload = new JsonObject();
        payload.addProperty("from", from);
        payload.addProperty("to", params.getOrDefault("To", ""));
        payload.addProperty("callSid", callSid);
        payload.addProperty("callStatus", callStatus);

        triggerChannel(CHANNEL_CALL_RECEIVED, payload.toString());
        logger.debug("Received call from {}, status: {}", from, callStatus);
    }

    /**
     * Called by the servlet when DTMF digits are gathered.
     */
    public void handleDtmfInput(Map<String, String> params) {
        String digits = params.getOrDefault("Digits", "");
        String callSid = params.getOrDefault("CallSid", "");
        String from = params.getOrDefault("From", "");

        // Update state channel
        updateState(CHANNEL_LAST_DTMF_DIGITS, new StringType(digits));

        // Build JSON payload for trigger
        JsonObject payload = new JsonObject();
        payload.addProperty("digits", digits);
        payload.addProperty("callSid", callSid);
        payload.addProperty("from", from);
        payload.addProperty("to", params.getOrDefault("To", ""));

        triggerChannel(CHANNEL_DTMF_RECEIVED, payload.toString());
        logger.debug("Received DTMF digits: {} from call {}", digits, callSid);
    }

    /**
     * Called by the servlet when a message or call status update is received.
     */
    public void handleStatusCallback(Map<String, String> params) {
        JsonObject payload = new JsonObject();

        // Could be message status or call status
        String messageSid = params.get("MessageSid");
        String callSid = params.get("CallSid");

        if (messageSid != null) {
            String messageStatus = params.getOrDefault("MessageStatus", "");
            payload.addProperty("messageSid", messageSid);
            payload.addProperty("messageStatus", messageStatus);
            payload.addProperty("to", params.getOrDefault("To", ""));
            triggerChannel(CHANNEL_MESSAGE_STATUS, payload.toString());
            logger.debug("Message {} status: {}", messageSid, messageStatus);
        } else if (callSid != null) {
            String callStatus = params.getOrDefault("CallStatus", "");
            updateState(CHANNEL_LAST_CALL_STATUS, new StringType(callStatus));
            payload.addProperty("callSid", callSid);
            payload.addProperty("callStatus", callStatus);
            payload.addProperty("from", params.getOrDefault("From", ""));
            payload.addProperty("to", params.getOrDefault("To", ""));
            triggerChannel(CHANNEL_CALL_STATUS_TRIGGER, payload.toString());
            logger.debug("Call {} status: {}", callSid, callStatus);
        }
    }

    /**
     * Returns the TwiML for the voice greeting, with placeholders replaced.
     */
    public String getVoiceGreetingTwiml() {
        return replaceTwimlPlaceholders(config.voiceGreeting);
    }

    /**
     * Returns the TwiML for the gather response.
     */
    public String getGatherResponseTwiml() {
        return config.gatherResponse;
    }

    /**
     * Replaces {gatherUrl} placeholder in TwiML with the actual gather webhook URL.
     */
    public String replaceTwimlPlaceholders(String twiml) {
        String baseUrl = getWebhookBaseUrl();
        if (baseUrl != null) {
            return twiml.replace("{gatherUrl}", baseUrl + "/" + WEBHOOK_GATHER);
        }
        return twiml;
    }

    /**
     * Returns the status callback URL, or null if not available.
     */
    public @Nullable String getStatusCallbackUrl() {
        String baseUrl = getWebhookBaseUrl();
        return baseUrl != null ? baseUrl + "/" + WEBHOOK_STATUS : null;
    }

    private void updateWebhookProperties() {
        Map<String, String> properties = new java.util.HashMap<>(editProperties());
        String baseUrl = getWebhookBaseUrl();
        if (baseUrl != null) {
            properties.put(PROPERTY_SMS_WEBHOOK_URL, baseUrl + "/" + WEBHOOK_SMS);
            properties.put(PROPERTY_VOICE_WEBHOOK_URL, baseUrl + "/" + WEBHOOK_VOICE);
            properties.put(PROPERTY_STATUS_CALLBACK_URL, baseUrl + "/" + WEBHOOK_STATUS);
        } else {
            String localBase = "http://localhost:8080" + SERVLET_PATH + "/" + thing.getUID().getAsString();
            properties.put(PROPERTY_SMS_WEBHOOK_URL, localBase + "/" + WEBHOOK_SMS + " (local only)");
            properties.put(PROPERTY_VOICE_WEBHOOK_URL, localBase + "/" + WEBHOOK_VOICE + " (local only)");
            properties.put(PROPERTY_STATUS_CALLBACK_URL, localBase + "/" + WEBHOOK_STATUS + " (local only)");
        }
        updateProperties(properties);
    }

    private void asyncInitialize() {
        TwilioAccountHandler accountHandler = getAccountHandler();
        if (accountHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Bridge handler not available");
            return;
        }

        TwilioApiClient client = accountHandler.getApiClient();
        if (client == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "API client not available");
            return;
        }

        // Register with the callback servlet
        callbackServlet.registerHandler(thing.getUID().getAsString(), this);

        // Set webhook URL properties
        updateWebhookProperties();

        // Auto-configure webhooks if enabled
        TwilioAccountConfiguration accountConfig = accountHandler.getAccountConfig();
        if (accountConfig.autoConfigureWebhooks) {
            String baseUrl = getWebhookBaseUrl();
            String localPhoneNumber = phoneNumber;
            if (baseUrl != null && localPhoneNumber != null) {
                try {
                    String phoneSid = client.lookupPhoneNumberSid(localPhoneNumber);
                    if (phoneSid != null) {
                        client.configureWebhooks(phoneSid, baseUrl + "/" + WEBHOOK_SMS, baseUrl + "/" + WEBHOOK_VOICE,
                                baseUrl + "/" + WEBHOOK_STATUS);
                        logger.debug("Auto-configured webhooks for phone number {}", phoneNumber);
                    } else {
                        logger.debug("Could not find phone number SID for {}", phoneNumber);
                    }
                } catch (TwilioApiException e) {
                    logger.debug("Failed to auto-configure webhooks: {}", e.getMessage());
                }
            } else {
                logger.debug("Cannot auto-configure webhooks: publicUrl not set on bridge");
            }
        }

        updateStatus(ThingStatus.ONLINE);
    }
}
