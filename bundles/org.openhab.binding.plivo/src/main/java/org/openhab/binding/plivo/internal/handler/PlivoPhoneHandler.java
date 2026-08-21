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
package org.openhab.binding.plivo.internal.handler;

import static org.openhab.binding.plivo.internal.PlivoBindingConstants.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.plivo.internal.action.PlivoActions;
import org.openhab.binding.plivo.internal.api.PlivoApiClient;
import org.openhab.binding.plivo.internal.api.PlivoApiException;
import org.openhab.binding.plivo.internal.config.PlivoAccountConfiguration;
import org.openhab.binding.plivo.internal.config.PlivoPhoneConfiguration;
import org.openhab.binding.plivo.internal.servlet.PlivoCallbackServlet;
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
 * The {@link PlivoPhoneHandler} handles a Plivo phone number thing.
 * It manages channels for incoming messages/calls and provides actions for sending.
 *
 * @author Sarvesh Patil - Initial contribution
 */
@NonNullByDefault
public class PlivoPhoneHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(PlivoPhoneHandler.class);

    private final PlivoCallbackServlet callbackServlet;
    private final ItemRegistry itemRegistry;

    private PlivoPhoneConfiguration config = new PlivoPhoneConfiguration();
    private String phoneNumber = "";
    private volatile @Nullable Future<?> initializeTask;

    public PlivoPhoneHandler(Thing thing, PlivoCallbackServlet callbackServlet, ItemRegistry itemRegistry) {
        super(thing);
        this.callbackServlet = callbackServlet;
        this.itemRegistry = itemRegistry;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        config = getConfigAs(PlivoPhoneConfiguration.class);

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
        cancelInitializeTask();
        callbackServlet.unregisterHandler(thing.getUID().getAsString());
        super.dispose();
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        cancelInitializeTask();
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            initializeTask = scheduler.submit(this::asyncInitialize);
        } else {
            callbackServlet.unregisterHandler(thing.getUID().getAsString());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    private void cancelInitializeTask() {
        Future<?> initTask = initializeTask;
        if (initTask != null) {
            initTask.cancel(true);
            initializeTask = null;
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(PlivoActions.class);
    }

    /**
     * Returns the bridge handler for this phone thing.
     *
     * @return the bridge handler, or null if not available
     */
    public @Nullable PlivoAccountHandler getAccountHandler() {
        if (getBridge() instanceof Bridge bridge && bridge.getHandler() instanceof PlivoAccountHandler handler) {
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
    public PlivoPhoneConfiguration getPhoneConfig() {
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
        PlivoAccountHandler accountHandler = getAccountHandler();
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
        PlivoAccountHandler accountHandler = getAccountHandler();
        return accountHandler != null && accountHandler.isUsingCloudWebhooks();
    }

    /**
     * Returns the media serving base URL. Media UUIDs can be appended as sub-paths.
     *
     * @return the media base URL, or null if neither cloud webhook nor publicUrl is available
     */
    public @Nullable String getMediaBaseUrl() {
        PlivoAccountHandler accountHandler = getAccountHandler();
        return accountHandler != null ? accountHandler.getMediaBaseUrl() : null;
    }

    /**
     * Returns a reference to the callback servlet for media and outbound-answer operations.
     */
    public PlivoCallbackServlet getCallbackServlet() {
        return callbackServlet;
    }

    /**
     * Called by the servlet when an SMS/MMS/WhatsApp message is received.
     */
    public void handleIncomingSms(Map<String, String> params) {
        String from = params.getOrDefault("From", "");
        String body = params.getOrDefault("Text", "");
        String messageUuid = params.getOrDefault("MessageUUID", "");
        String type = params.getOrDefault("Type", "sms");

        updateState(CHANNEL_LAST_MESSAGE_BODY, new StringType(body));
        updateState(CHANNEL_LAST_MESSAGE_FROM, new StringType(from));
        updateState(CHANNEL_LAST_MESSAGE_DATE, new DateTimeType(ZonedDateTime.now(ZoneId.systemDefault())));
        updateState(CHANNEL_LAST_MESSAGE_UUID, new StringType(messageUuid));

        JsonArray mediaUrlsArray = new JsonArray();
        for (int i = 0; i < 10; i++) {
            String media = params.get("Media" + i);
            if (media == null || media.isBlank()) {
                break;
            }
            mediaUrlsArray.add(media);
        }
        String firstMedia = mediaUrlsArray.size() > 0 ? mediaUrlsArray.get(0).getAsString() : "";
        updateState(CHANNEL_LAST_MESSAGE_MEDIA_URL, new StringType(firstMedia));

        JsonObject payload = new JsonObject();
        payload.addProperty("from", from);
        payload.addProperty("to", params.getOrDefault("To", ""));
        payload.addProperty("body", body);
        payload.addProperty("messageUuid", messageUuid);
        payload.addProperty("type", type);
        payload.add("mediaUrls", mediaUrlsArray);

        if ("whatsapp".equalsIgnoreCase(type)) {
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
        String callUuid = params.getOrDefault("CallUUID", "");
        String callStatus = params.getOrDefault("CallStatus", "");

        updateState(CHANNEL_LAST_CALL_FROM, new StringType(from));
        updateState(CHANNEL_LAST_CALL_STATUS, new StringType(callStatus));
        updateState(CHANNEL_LAST_CALL_DATE, new DateTimeType(ZonedDateTime.now(ZoneId.systemDefault())));

        JsonObject payload = new JsonObject();
        payload.addProperty("from", from);
        payload.addProperty("to", params.getOrDefault("To", ""));
        payload.addProperty("callUuid", callUuid);
        payload.addProperty("callStatus", callStatus);

        triggerChannel(CHANNEL_CALL_RECEIVED, payload.toString());
        logger.debug("Received call from {}, status: {}", from, callStatus);
    }

    /**
     * Called by the servlet when DTMF digits are gathered.
     */
    public void handleDtmfInput(Map<String, String> params) {
        String digits = params.getOrDefault("Digits", "");
        String callUuid = params.getOrDefault("CallUUID", "");
        String from = params.getOrDefault("From", "");

        updateState(CHANNEL_LAST_DTMF_DIGITS, new StringType(digits));

        JsonObject payload = new JsonObject();
        payload.addProperty("digits", digits);
        payload.addProperty("callUuid", callUuid);
        payload.addProperty("from", from);
        payload.addProperty("to", params.getOrDefault("To", ""));

        triggerChannel(CHANNEL_DTMF_RECEIVED, payload.toString());
        logger.debug("Received DTMF digits: {} from call {}", digits, callUuid);
    }

    /**
     * Called by the servlet when a message or call status update is received.
     */
    public void handleStatusCallback(Map<String, String> params) {
        JsonObject payload = new JsonObject();

        String messageUuid = params.get("MessageUUID");
        String callUuid = params.get("CallUUID");

        if (messageUuid != null) {
            String messageStatus = params.getOrDefault("Status", "");
            payload.addProperty("messageUuid", messageUuid);
            payload.addProperty("messageStatus", messageStatus);
            payload.addProperty("to", params.getOrDefault("To", ""));
            triggerChannel(CHANNEL_MESSAGE_STATUS, payload.toString());
            logger.debug("Message {} status: {}", messageUuid, messageStatus);
        } else if (callUuid != null) {
            String callStatus = params.getOrDefault("CallStatus", params.getOrDefault("Status", ""));
            updateState(CHANNEL_LAST_CALL_STATUS, new StringType(callStatus));
            payload.addProperty("callUuid", callUuid);
            payload.addProperty("callStatus", callStatus);
            payload.addProperty("from", params.getOrDefault("From", ""));
            payload.addProperty("to", params.getOrDefault("To", ""));
            triggerChannel(CHANNEL_CALL_STATUS_TRIGGER, payload.toString());
            logger.debug("Call {} status: {}", callUuid, callStatus);
        }
    }

    /**
     * Returns the Plivo XML for the voice greeting, with placeholders replaced.
     */
    public String getVoiceGreetingXml() {
        return replaceXmlPlaceholders(config.voiceGreeting);
    }

    /**
     * Returns the Plivo XML for the gather response.
     */
    public String getGatherResponseXml() {
        return config.gatherResponse;
    }

    /**
     * Replaces the {gatherUrl} placeholder in Plivo XML with the actual gather webhook URL.
     */
    public String replaceXmlPlaceholders(String xml) {
        String gatherUrl = getWebhookUrl(WEBHOOK_GATHER);
        if (gatherUrl != null) {
            return xml.replace("{gatherUrl}", gatherUrl);
        }
        return xml;
    }

    /**
     * Returns the status callback URL, or null if not available.
     */
    public @Nullable String getStatusCallbackUrl() {
        return getWebhookUrl(WEBHOOK_STATUS);
    }

    private void updateWebhookProperties() {
        updateProperty(PROPERTY_MESSAGE_WEBHOOK_URL, getWebhookUrl(WEBHOOK_SMS));
        updateProperty(PROPERTY_VOICE_WEBHOOK_URL, getWebhookUrl(WEBHOOK_VOICE));
        updateProperty(PROPERTY_STATUS_CALLBACK_URL, getWebhookUrl(WEBHOOK_STATUS));
    }

    private void asyncInitialize() {
        PlivoAccountHandler accountHandler = getAccountHandler();
        if (accountHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "@text/offline.bridge-uninitialized.bridge-handler-not-available");
            return;
        }

        PlivoApiClient client = accountHandler.getApiClient();
        if (client == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED,
                    "@text/offline.bridge-uninitialized.api-client-not-available");
            return;
        }

        callbackServlet.registerHandler(thing.getUID().getAsString(), this);
        updateWebhookProperties();
        try {
            configureWebhooksOnPlivo();
        } catch (PlivoApiException e) {
            if (Thread.currentThread().isInterrupted() || e.getCause() instanceof InterruptedException) {
                // A newer initialization (bridge status change or webhook availability) cancelled this
                // one, so leave the status for the superseding run rather than reporting the
                // interruption as a communication error.
                logger.debug("Auto-configuration for {} was superseded by a newer initialization", phoneNumber);
                return;
            }
            ThingStatusDetail detail = e.isConfigurationError() ? ThingStatusDetail.CONFIGURATION_ERROR
                    : ThingStatusDetail.COMMUNICATION_ERROR;
            updateStatus(ThingStatus.OFFLINE, detail, e.getMessage());
            logger.debug("Failed to auto-configure Plivo application for {}: {}", phoneNumber, e.getMessage());
            return;
        }

        updateStatus(ThingStatus.ONLINE);
    }

    /**
     * Called by the account handler when the openHAB Cloud webhook URL becomes available after this
     * phone thing already initialized. Re-runs initialization so the webhook properties refresh and
     * automatic application configuration is retried against the now-available URL.
     */
    public void onWebhookUrlAvailable() {
        cancelInitializeTask();
        initializeTask = scheduler.submit(this::asyncInitialize);
    }

    private void configureWebhooksOnPlivo() throws PlivoApiException {
        PlivoAccountHandler accountHandler = getAccountHandler();
        if (accountHandler == null) {
            return;
        }
        PlivoAccountConfiguration accountConfig = accountHandler.getAccountConfig();
        if (!accountConfig.autoConfigureWebhooks) {
            return;
        }
        PlivoApiClient client = accountHandler.getApiClient();
        if (client == null) {
            return;
        }
        String answerUrl = getWebhookUrl(WEBHOOK_VOICE);
        String messageUrl = getWebhookUrl(WEBHOOK_SMS);
        String statusUrl = getWebhookUrl(WEBHOOK_STATUS);
        if (answerUrl == null || messageUrl == null || statusUrl == null) {
            // No inbound webhook URL is available yet. Outbound SMS, WhatsApp, and calls do not need
            // one, so this is not an error and the Thing stays ONLINE. If a cloud webhook URL becomes
            // available later, onWebhookUrlAvailable() retries this configuration.
            logger.debug("No webhook URL available for {} yet; skipping inbound auto-configuration", phoneNumber);
            return;
        }
        // Plivo application names accept only alphanumerics, hyphens, and underscores, so the Thing
        // UID (which contains colons) must be sanitized before it can be used as a stable app name.
        String appName = "openHAB-" + thing.getUID().getAsString().replaceAll("[^A-Za-z0-9_-]", "-");
        String appId = client.createOrUpdateApplication(appName, answerUrl, messageUrl, statusUrl);
        client.assignApplicationToNumber(phoneNumber, appId);
        logger.debug("Auto-configured Plivo application {} ({}) for phone number {}", appId, appName, phoneNumber);
    }
}
