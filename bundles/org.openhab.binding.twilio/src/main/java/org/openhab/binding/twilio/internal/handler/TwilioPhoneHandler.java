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
import java.util.concurrent.Future;

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

import com.google.gson.JsonArray;
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
    private String phoneNumber = "";
    private @Nullable Future<?> initializeTask;

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
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error.missing-phone-number");
            return;
        }
        phoneNumber = number;

        updateStatus(ThingStatus.UNKNOWN);
        initializeTask = scheduler.submit(this::asyncInitialize);
    }

    @Override
    public void dispose() {
        Future<?> initTask = initializeTask;
        if (initTask != null) {
            initTask.cancel(true);
            initializeTask = null;
        }
        callbackServlet.unregisterHandler(thing.getUID().getAsString());
        super.dispose();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            initializeTask = scheduler.submit(this::asyncInitialize);
        } else {
            callbackServlet.unregisterHandler(thing.getUID().getAsString());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
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
        if (getBridge() instanceof Bridge bridge && bridge.getHandler() instanceof TwilioAccountHandler handler) {
            return handler;
        }
        return null;
    }

    /**
     * Returns the configured phone number.
     *
     * @return the phone number in E.164 format
     */
    public String getPhoneNumber() {
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
     * Returns the webhook URL for a specific endpoint. Checks cloud webhook URLs first,
     * then falls back to publicUrl-based URLs.
     *
     * @param endpoint the webhook endpoint (e.g. "sms", "voice", "gather", "status")
     * @return the full webhook URL, or null if no public URL is available
     */
    public @Nullable String getWebhookUrl(String endpoint) {
        TwilioAccountHandler accountHandler = getAccountHandler();
        if (accountHandler == null) {
            return null;
        }
        String baseUrl = accountHandler.getWebhookBaseUrl(thing.getUID().getAsString());
        return baseUrl != null ? baseUrl + "/" + endpoint : null;
    }

    /**
     * Returns true if cloud webhooks are active for this phone's account.
     */
    public boolean isUsingCloudWebhooks() {
        TwilioAccountHandler accountHandler = getAccountHandler();
        return accountHandler != null && accountHandler.isUsingCloudWebhooks();
    }

    /**
     * Returns the media serving base URL. Media UUIDs can be appended as sub-paths.
     *
     * @return the media base URL, or null if neither cloud webhook nor publicUrl is available
     */
    public @Nullable String getMediaBaseUrl() {
        TwilioAccountHandler accountHandler = getAccountHandler();
        return accountHandler != null ? accountHandler.getMediaBaseUrl() : null;
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
        JsonArray mediaUrlsArray = new JsonArray();
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
        String gatherUrl = getWebhookUrl(WEBHOOK_GATHER);
        if (gatherUrl != null) {
            return twiml.replace("{gatherUrl}", gatherUrl);
        }
        return twiml;
    }

    /**
     * Returns the status callback URL, or null if not available.
     */
    public @Nullable String getStatusCallbackUrl() {
        return getWebhookUrl(WEBHOOK_STATUS);
    }

    private void updateWebhookProperties() {
        updateProperty(PROPERTY_SMS_WEBHOOK_URL, getWebhookUrl(WEBHOOK_SMS));
        updateProperty(PROPERTY_VOICE_WEBHOOK_URL, getWebhookUrl(WEBHOOK_VOICE));
        updateProperty(PROPERTY_STATUS_CALLBACK_URL, getWebhookUrl(WEBHOOK_STATUS));
    }

    private void asyncInitialize() {
        TwilioAccountHandler accountHandler = getAccountHandler();
        if (accountHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "@text/offline.bridge-uninitialized.bridge-handler-not-available");
            return;
        }

        TwilioApiClient client = accountHandler.getApiClient();
        if (client == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "@text/offline.bridge-uninitialized.api-client-not-available");
            return;
        }

        // Register with the callback servlet
        callbackServlet.registerHandler(thing.getUID().getAsString(), this);

        // Set webhook URL properties
        updateWebhookProperties();

        // Auto-configure webhooks if enabled
        configureWebhooksOnTwilio();

        updateStatus(ThingStatus.ONLINE);
    }

    private void configureWebhooksOnTwilio() {
        TwilioAccountHandler accountHandler = getAccountHandler();
        if (accountHandler == null) {
            return;
        }
        TwilioAccountConfiguration accountConfig = accountHandler.getAccountConfig();
        if (!accountConfig.autoConfigureWebhooks) {
            return;
        }
        TwilioApiClient client = accountHandler.getApiClient();
        if (client == null) {
            return;
        }
        String smsUrl = getWebhookUrl(WEBHOOK_SMS);
        String voiceUrl = getWebhookUrl(WEBHOOK_VOICE);
        String statusUrl = getWebhookUrl(WEBHOOK_STATUS);
        if (smsUrl != null && voiceUrl != null && statusUrl != null) {
            try {
                String phoneSid = client.lookupPhoneNumberSid(phoneNumber);
                if (phoneSid != null) {
                    client.configureWebhooks(phoneSid, smsUrl, voiceUrl, statusUrl);
                    logger.debug("Auto-configured webhooks for phone number {}", phoneNumber);
                } else {
                    logger.debug("Could not find phone number SID for {}", phoneNumber);
                }
            } catch (TwilioApiException e) {
                logger.debug("Failed to auto-configure webhooks: {}", e.getMessage());
            }
        } else {
            logger.debug("Cannot auto-configure webhooks: no webhook URLs available");
        }
    }
}
