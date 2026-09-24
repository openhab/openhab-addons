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
package org.openhab.binding.lghorizon.internal.handler;

import static org.openhab.binding.lghorizon.internal.LGHorizonBindingConstants.*;
import static org.openhab.binding.lghorizon.internal.api.LGHorizonApiConstants.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.util.InputStreamResponseListener;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.lghorizon.internal.LGHorizonContentAnonymizer;
import org.openhab.binding.lghorizon.internal.api.LGHorizonApiException;
import org.openhab.binding.lghorizon.internal.api.LGHorizonAuthClient;
import org.openhab.binding.lghorizon.internal.api.ProviderPresets;
import org.openhab.binding.lghorizon.internal.api.dto.ChannelDto;
import org.openhab.binding.lghorizon.internal.api.dto.CustomerDto;
import org.openhab.binding.lghorizon.internal.api.dto.EntitlementsDto;
import org.openhab.binding.lghorizon.internal.api.dto.EventDetailDto;
import org.openhab.binding.lghorizon.internal.api.dto.RecordingDetailDto;
import org.openhab.binding.lghorizon.internal.api.dto.VodDetailDto;
import org.openhab.binding.lghorizon.internal.discovery.LGHorizonDiscoveryService;
import org.openhab.binding.lghorizon.internal.mqtt.LGHorizonMqttClient;
import org.openhab.binding.lghorizon.internal.mqtt.LGHorizonMqttListener;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.RawType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Bridge handler for one LG Horizon (Telenet/Ziggo/Virgin Media/UPC/Sunrise/ BASE TV) account. Owns:
 * <ul>
 * <li>the REST auth session (access/refresh token lifecycle)</li>
 * <li>the household's channel line-up</li>
 * <li>the single MQTT connection shared by all set-top boxes on the account</li>
 * </ul>
 * Individual set-top boxes are represented by child {@link LGHorizonBoxHandler} things, which register themselves here
 * to receive status updates and to send commands.
 *
 * @author Mark Herwege - Initial contribution
 */
@NonNullByDefault
public class LGHorizonAccountHandler extends BaseBridgeHandler implements LGHorizonMqttListener {

    private final Logger logger = LoggerFactory.getLogger(LGHorizonAccountHandler.class);
    private static final Gson GSON = new Gson();

    private final HttpClient httpClient;

    private @Nullable LGHorizonAuthClient authClient;
    private @Nullable LGHorizonMqttClient mqttClient;
    private @Nullable CustomerDto customer;
    private @Nullable LGHorizonDiscoveryService discoveryService;

    private Map<String, String> languageByProfileId = Map.of();
    private Map<String, Map<String, ChannelDto>> channelsByLanguage = Map.of();

    private final Map<String, LGHorizonBoxHandler> registeredBoxes = new ConcurrentHashMap<>();

    // Fields used to debounce repeated state requests from the same box
    private final Map<String, Long> lastStateRequestMillis = new ConcurrentHashMap<>();
    private static final long STATE_REQUEST_DEBOUNCE_MILLIS = 2000;

    // How long the box's own per-publish display time lasts, in seconds
    private static final int DISPLAY_MESSAGE_REPEAT_INTERVAL_SECONDS = 3;
    private static final int DISPLAY_MESSAGE_DURATION_MAX_SECONDS = 120;

    // Keep track of display messages sent to filter out the returned failure status messages that are not relevant.
    private final java.util.Set<String> pendingDisplayMessageIds = ConcurrentHashMap.newKeySet();
    private static final int MAX_TRACKED_DISPLAY_MESSAGE_IDS = 50;

    // Limit size of fetched images
    private static final int MAX_IMAGE_BYTES = 8 * 1024 * 1024; // 8 MiB

    // Last known status, account does not send refreshes
    private final Map<String, JsonObject> lastKnownStatusByDeviceId = new ConcurrentHashMap<>();

    // Used by the fingerprint and capture console commands
    private final Map<String, CompletableFuture<JsonObject>> pendingStatusCaptures = new ConcurrentHashMap<>();
    private final List<BiConsumer<String, JsonObject>> liveCaptureListeners = new CopyOnWriteArrayList<>();
    private volatile @Nullable Consumer<String> imageCaptureListener;

    // Retry for a transient failure during initialize
    private static final int INITIALIZE_RETRY_FIRST_DELAY_SECONDS = 30;
    private static final int INITIALIZE_RETRY_MAX_DELAY_SECONDS = 300;
    private final AtomicInteger initializeRetryAttempt = new AtomicInteger();

    private @Nullable ScheduledFuture<?> initializeFuture;
    private @Nullable ScheduledFuture<?> tokenRefreshFuture;

    private volatile boolean disposed = false;

    public LGHorizonAccountHandler(Bridge bridge, HttpClient httpClient) {
        super(bridge);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        initializeFuture = scheduler.schedule(this::doInitialize, 0, TimeUnit.SECONDS);
    }

    private void doInitialize() {
        if (disposed) {
            return;
        }
        LGHorizonAccountConfiguration config = getConfigAs(LGHorizonAccountConfiguration.class);

        ResolvedProvider provider;
        try {
            provider = resolveProvider(config);
        } catch (IllegalArgumentException e) {
            if (!disposed) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            }
            return;
        }
        persistResolvedProviderFields(config, provider);

        LGHorizonAuthClient auth = new LGHorizonAuthClient(httpClient, provider.apiUrl(), provider.countryCode(),
                provider.useRefreshToken(), config.username, config.password, config.refreshToken);
        auth.setRefreshTokenListener(this::persistRefreshToken);
        this.authClient = auth;

        LGHorizonMqttClient mqtt = null;
        try {
            auth.initialize();
            String householdId = auth.getHouseholdId();
            if (householdId == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/offline.account-missing-household-id");
                return;
            }
            updateProperty(PROPERTY_HOUSEHOLD_ID, householdId);

            refreshCustomerAndChannels(auth);

            mqtt = new LGHorizonMqttClient(auth, this, scheduler);
            mqtt.subscribeAccountTopics(householdId);
            mqtt.connect();

            boolean committed;
            synchronized (this) {
                committed = !disposed;
                if (committed) {
                    this.mqttClient = mqtt;
                    this.tokenRefreshFuture = scheduler.scheduleWithFixedDelay(this::checkTokenRefresh, 1, 1,
                            TimeUnit.HOURS);
                }
            }
            if (!committed) {
                mqtt.disconnect();
                return;
            }

            updateStatus(ThingStatus.ONLINE);
            initializeRetryAttempt.set(0);

            LGHorizonDiscoveryService discoveryService = this.discoveryService;
            if (discoveryService != null) {
                discoveryService.discoverDevices();
            }

            tokenRefreshFuture = scheduler.scheduleWithFixedDelay(this::checkTokenRefresh, 1, 1, TimeUnit.HOURS);
        } catch (LGHorizonApiException | IllegalArgumentException e) {
            if (mqtt != null) {
                mqtt.disconnect();
            }
            if (disposed || Thread.currentThread().isInterrupted()) {
                return;
            }
            if (e instanceof LGHorizonApiException apiException && apiException.isAuthenticationFailure()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        textWithArg("offline.account-configuration-error", String.valueOf(e.getMessage())));
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        textWithArg("offline.account-communication-error", String.valueOf(e.getMessage())));
                scheduleInitializeRetry();
            }
        }
    }

    private void scheduleInitializeRetry() {
        if (disposed) {
            return;
        }
        int attempt = initializeRetryAttempt.getAndIncrement();
        int delaySeconds = (int) Math.min(INITIALIZE_RETRY_FIRST_DELAY_SECONDS * Math.pow(2, attempt),
                INITIALIZE_RETRY_MAX_DELAY_SECONDS);
        logger.debug("LG Horizon account initialization failed, retrying in {}s", delaySeconds);
        initializeFuture = scheduler.schedule(this::doInitialize, delaySeconds, TimeUnit.SECONDS);
    }

    /**
     * @param discoveryService
     */
    public void setDiscoveryService(LGHorizonDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
        if (ThingStatus.ONLINE.equals(thing.getStatus())) {
            discoveryService.discoverDevices();
        }
    }

    private record ResolvedProvider(String apiUrl, String countryCode, boolean useRefreshToken) {
    }

    private ResolvedProvider resolveProvider(LGHorizonAccountConfiguration config) throws IllegalArgumentException {
        String provider = config.provider;
        if (provider != null && !provider.isBlank()) {
            ProviderPresets.Preset preset = ProviderPresets.get(provider); // throws if unknown
            return new ResolvedProvider(preset.apiUrl(), preset.countryCode(), preset.useRefreshToken());
        }
        if (config.apiUrl.isBlank() || config.country.isBlank()) {
            throw new IllegalArgumentException("@text/offline.account-provider-or-country-apiurl-required");
        }
        String normalizedApiUrl = requireHttpsUrl(config.apiUrl);
        return new ResolvedProvider(normalizedApiUrl, config.country, config.useRefreshToken);
    }

    /**
     * Rejects anything but a well-formed https:// URL.
     *
     * @return the URL, with a missing scheme filled in as https:// if needed
     */
    private String requireHttpsUrl(String url) throws IllegalArgumentException {
        if (!url.contains("://")) {
            return requireHttpsUrl("https://" + url);
        }
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(textWithArg("offline.account-invalid-api-url", url));
        }
        if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) {
            throw new IllegalArgumentException(textWithArg("offline.account-api-url-not-https", url));
        }
        return url;
    }

    private void persistResolvedProviderFields(LGHorizonAccountConfiguration config, ResolvedProvider provider) {
        String currentProvider = config.provider;
        if (currentProvider == null || currentProvider.isBlank()) {
            return;
        }
        if (provider.countryCode().equals(config.country) && provider.apiUrl().equals(config.apiUrl)
                && provider.useRefreshToken() == config.useRefreshToken) {
            return;
        }
        Configuration configuration = editConfiguration();
        configuration.put(CONFIG_COUNTRY, provider.countryCode());
        configuration.put(CONFIG_API_URL, provider.apiUrl());
        configuration.put(CONFIG_USE_REFRESH_TOKEN, provider.useRefreshToken());
        updateConfiguration(configuration);
    }

    /**
     * Keeps {@code provider} and the advanced {@code country}/{@code apiUrl}/{@code useRefreshToken} fields in
     * sync as the account configuration is edited.
     *
     * @param configurationParameters the submitted configuration parameters
     */
    @Override
    public void handleConfigurationUpdate(Map<String, Object> configurationParameters) {
        Map<String, Object> updated = new HashMap<>(configurationParameters);

        String oldProvider = stringConfig(CONFIG_PROVIDER);
        Object submittedProvider = updated.get(CONFIG_PROVIDER);
        String newProvider = submittedProvider == null ? oldProvider : submittedProvider.toString();

        if (!newProvider.equals(oldProvider) && !newProvider.isBlank()) {
            // A provider was (newly) selected, or switched to a different one: preset wins.
            try {
                ProviderPresets.Preset preset = ProviderPresets.get(newProvider);
                updated.put(CONFIG_COUNTRY, preset.countryCode());
                updated.put(CONFIG_API_URL, preset.apiUrl());
                updated.put(CONFIG_USE_REFRESH_TOKEN, preset.useRefreshToken());
            } catch (IllegalArgumentException e) {
                // Unknown provider id - leave fields as-is
            }
        } else if (newProvider.equals(oldProvider) && !oldProvider.isBlank()
                && (configFieldChanged(updated, CONFIG_COUNTRY) || configFieldChanged(updated, CONFIG_API_URL)
                        || configFieldChanged(updated, CONFIG_USE_REFRESH_TOKEN))) {
            // A preset was active, but one of its fields was hand-edited: fall through to manual/"Custom" mode.
            updated.put(CONFIG_PROVIDER, "");
        }

        super.handleConfigurationUpdate(updated);
    }

    private String stringConfig(String key) {
        Object value = getConfig().get(key);
        return value == null ? "" : value.toString();
    }

    /**
     * Builds a {@code "@text/key [\"arg\"]"}-style status description safely, avoiding issues with quotes in messages.
     */
    private static String textWithArg(String key, String arg) {
        JsonArray args = new JsonArray();
        args.add(arg);
        return "@text/" + key + " " + args;
    }

    private boolean configFieldChanged(Map<String, Object> submitted, String key) {
        return submitted.containsKey(key) && !Objects.equals(getConfig().get(key), submitted.get(key));
    }

    /**
     * Fetches the raw (untyped) JSON for the REST calls this handler makes during normal operation, for the
     * {@code lghorizon fingerprint} console command.
     *
     * @return map of a short logical name (used as a filename) to the raw JSON response
     */
    public Map<String, String> fetchDiagnosticRestSnapshot() {
        Map<String, String> snapshot = new LinkedHashMap<>();
        LGHorizonAuthClient auth = authClient;
        String householdId = getHouseholdId();
        if (auth == null || householdId == null) {
            return snapshot;
        }

        tryFetch(snapshot, "service-config", () -> auth.getServiceConfigAsJsonElement().toString());
        try {
            String personalizationServiceUrl = auth.getServiceConfig().getServiceUrl(PERSONALIZATION_SERVICE_URL_FIELD);
            tryFetch(snapshot, "customer", () -> auth
                    .getAsJsonElement(personalizationServiceUrl, CUSTOMER_PATH.formatted(householdId)).toString());

            String purchaseServiceUrl = auth.getServiceConfig().getServiceUrl(PURCHASE_SERVICE_URL_FIELD);
            tryFetch(snapshot, "entitlements", () -> auth
                    .getAsJsonElement(purchaseServiceUrl, ENTITLEMENTS_PATH.formatted(householdId)).toString());

            String linearServiceUrl = auth.getServiceConfig().getServiceUrl(LINEAR_SERVICE_URL_FIELD);
            CustomerDto c = customer;
            Integer cityId = c != null ? c.cityId : null;
            if (cityId == null) {
                throw new LGHorizonApiException("Cannot fetch channels: customer cityId not available");
            }
            for (String lang : languageByProfileId.values().stream().distinct().toList()) {
                tryFetch(snapshot, "channels-" + lang, () -> auth
                        .getAsJsonElement(linearServiceUrl, CHANNELS_PATH.formatted(cityId, lang)).toString());
            }
        } catch (LGHorizonApiException | IllegalArgumentException e) {
            logger.debug("Could not resolve service URLs for diagnostic snapshot: {}", e.getMessage());
        }
        return snapshot;
    }

    private interface JsonFetcher {
        String get() throws LGHorizonApiException;
    }

    private void tryFetch(Map<String, String> snapshot, String name, JsonFetcher fetcher) {
        try {
            snapshot.put(name, fetcher.get());
        } catch (LGHorizonApiException e) {
            logger.debug("Diagnostic fetch '{}' failed: {}", name, e.getMessage());
        }
    }

    /**
     * Registers a callback invoked for every MQTT message this account's connection sees, used by the
     * {@code lghorizon capture} console command.
     *
     * @param onMessage a callback that receives the topic and payload of each message
     */
    public void startLiveCapture(BiConsumer<String, JsonObject> onMessage) {
        liveCaptureListeners.add(onMessage);
    }

    /**
     * Stops a live capture previously started with {@link #startLiveCapture}.
     *
     * @param onMessage the same callback that was passed to {@link #startLiveCapture}
     */
    public void stopLiveCapture(BiConsumer<String, JsonObject> onMessage) {
        liveCaptureListeners.remove(onMessage);
    }

    private void notifyLiveCapture(String topic, JsonObject payload) {
        liveCaptureListeners.forEach(listener -> listener.accept(topic, payload));
    }

    /**
     * Starts capturing every REST call this account's auth client makes, for the {@code lghorizon capture} console
     * command's live-capture window.
     *
     * @param onCall a callback that receives the URL and raw JSON response of each REST call
     */
    public void startRestCapture(BiConsumer<String, String> onCall) {
        LGHorizonAuthClient auth = authClient;
        if (auth != null) {
            auth.setCallCaptureListener(onCall);
        }
    }

    /**
     * Stops capturing REST calls previously started with {@link #startRestCapture}.
     */
    public void stopRestCapture() {
        LGHorizonAuthClient auth = authClient;
        if (auth != null) {
            auth.setCallCaptureListener(null);
        }
    }

    /**
     * Starts capturing metadata (not bytes) about every image fetch - see {@link #fetchImage}.
     *
     * @param onImageFetch a callback that receives the URL of each image fetch
     */
    public void startImageCapture(Consumer<String> onImageFetch) {
        this.imageCaptureListener = onImageFetch;
    }

    /**
     * Stops capturing image fetches previously started with {@link #startImageCapture}.
     */
    public void stopImageCapture() {
        this.imageCaptureListener = null;
    }

    /**
     * Captures the next {@code .../status} message for the given device.
     *
     * @param deviceId the device to capture the next status for
     * @return a future that completes with the next status message, or immediately with the last known cached status
     */
    public CompletableFuture<JsonObject> captureNextStatus(String deviceId) {
        JsonObject cached = lastKnownStatusByDeviceId.get(deviceId);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        pendingStatusCaptures.put(deviceId, future);
        return future;
    }

    private void refreshCustomerAndChannels(LGHorizonAuthClient auth)
            throws LGHorizonApiException, IllegalArgumentException {
        String householdId = auth.getHouseholdId();
        CustomerDto customerDto = auth.get(auth.getServiceConfig().getServiceUrl(PERSONALIZATION_SERVICE_URL_FIELD),
                CUSTOMER_PATH.formatted(householdId), CustomerDto.class);
        logger.trace("Received: {} customer info: {}", LGHorizonContentAnonymizer.anonymizeTopic(householdId),
                LGHorizonContentAnonymizer.anonymizeMessage(GSON.toJson(customerDto)));
        this.customer = customerDto;

        List<CustomerDto.ProfileDto> profiles = customerDto.profiles;
        if (profiles == null || profiles.isEmpty()) {
            throw new LGHorizonApiException("LG Horizon account has no profiles");
        }

        this.languageByProfileId = profiles.stream().filter(p -> p.profileId != null)
                .collect(Collectors.<CustomerDto.ProfileDto, String, String> toMap(p -> p.profileId,
                        p -> p.options != null && p.options.lang != null ? p.options.lang : DEFAULT_LANGUAGE));

        EntitlementsDto entitlementsDto = auth.get(auth.getServiceConfig().getServiceUrl(PURCHASE_SERVICE_URL_FIELD),
                ENTITLEMENTS_PATH.formatted(householdId), EntitlementsDto.class);
        logger.trace("Received: {} entitlements: {}", LGHorizonContentAnonymizer.anonymizeTopic(householdId),
                LGHorizonContentAnonymizer.anonymizeMessage(GSON.toJson(entitlementsDto)));

        List<String> entitlementIds = entitlementsDto.getEntitlementIds();
        Map<String, Map<String, ChannelDto>> channelsByLanguage = new HashMap<>();
        for (String lang : languageByProfileId.values()) {
            Integer cityId = customerDto.cityId;
            if (cityId == null) {
                throw new LGHorizonApiException("Cannot fetch channels: customer cityId not available");
            }
            ChannelDto[] channelArray = auth.get(auth.getServiceConfig().getServiceUrl(LINEAR_SERVICE_URL_FIELD),
                    CHANNELS_PATH.formatted(cityId, lang), ChannelDto[].class);
            logger.trace("Received: {}, {} channels: {}", LGHorizonContentAnonymizer.anonymizeTopic(householdId), lang,
                    Arrays.toString(channelArray));

            Map<String, ChannelDto> filtered = List.of(channelArray).stream().filter(c -> c.id != null)
                    .filter(c -> c.getLinearProducts().stream().anyMatch(entitlementIds::contains))
                    .collect(Collectors.toMap(c -> c.id, c -> c));
            logger.debug("LG Horizon account {}, {}: {} entitled channels loaded", getThing().getUID(), lang,
                    filtered.size());
            channelsByLanguage.put(lang, filtered);
        }
        this.channelsByLanguage = Map.copyOf(channelsByLanguage);
        registeredBoxes.values().forEach(LGHorizonBoxHandler::updateChannelNumberOptions);
        registeredBoxes.values().forEach(LGHorizonBoxHandler::updateDeviceProperties);

        String customerId = customerDto.customerId;
        if (customerId != null) {
            updateProperty(PROPERTY_CUSTOMER_ID, customerId);
        }
        String countryId = customerDto.countryId;
        if (countryId != null) {
            updateProperty(PROPERTY_COUNTRY_ID, countryId);
        }
        updateProperty(PROPERTY_CITY_ID, String.valueOf(customerDto.cityId));
    }

    private void persistRefreshToken(String newRefreshToken) {
        Configuration configuration = editConfiguration();
        configuration.put(CONFIG_REFRESH_TOKEN, newRefreshToken);
        updateConfiguration(configuration);
    }

    private void checkTokenRefresh() {
        LGHorizonAuthClient auth = authClient;
        if (auth == null) {
            return;
        }
        try {
            auth.fetchAccessToken();
        } catch (LGHorizonApiException e) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }
            if (e.isAuthenticationFailure()) {
                logger.warn("Background LG Horizon token refresh failed authentication, going offline: {}",
                        e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        textWithArg("offline.account-configuration-error", String.valueOf(e.getMessage())));
                ScheduledFuture<?> refresh = tokenRefreshFuture;
                if (refresh != null) {
                    refresh.cancel(false);
                }
            } else {
                logger.debug("Background LG Horizon token refresh failed: {}", e.getMessage());
            }
        }
    }

    /**
     * Get all devices assigned to a customerId. Called by {@link LGHorizonDiscoveryService}.
     *
     * @return a list of assigned devices, or an empty list if the customer is not yet loaded or has no devices
     */
    public List<CustomerDto.DeviceDto> getAssignedDevices() {
        CustomerDto c = customer;
        List<CustomerDto.DeviceDto> devices = c == null ? null : c.assignedDevices;
        return devices == null ? List.of() : new ArrayList<>(devices);
    }

    /**
     * @param deviceId the deviceId to look up
     * @return the assigned device with the given deviceId, or {@code null} if not found
     */
    /**
     * @param deviceId the deviceId to look up
     * @return the assigned device with the given deviceId, or {@code null} if not found
     */
    public CustomerDto.@Nullable DeviceDto getAssignedDevice(String deviceId) {
        CustomerDto c = customer;
        if (c == null || c.assignedDevices == null) {
            return null;
        }
        return c.assignedDevices.stream().filter(d -> deviceId.equals(d.deviceId)).findFirst().orElse(null);
    }

    /**
     * @return the list of profiles for this account, or an empty list if the customer is not yet loaded or has no
     */
    public List<CustomerDto.ProfileDto> getProfiles() {
        CustomerDto c = customer;
        List<CustomerDto.ProfileDto> profiles = c == null ? null : c.profiles;
        return profiles == null ? List.of() : new ArrayList<>(profiles);
    }

    /**
     * @param profileId the profileId to look up
     * @return the language code for the given profile, or the default language if not found
     */
    public String getLanguageForProfile(@Nullable String profileId) {
        String lang = profileId != null ? languageByProfileId.get(profileId) : null;
        return lang != null ? lang : DEFAULT_LANGUAGE;
    }

    /**
     * @param language the language code to look up
     * @return the map of channelId -> ChannelDto for the given language, or an empty map if not found
     */
    public Map<String, ChannelDto> getChannels(String language) {
        return channelsByLanguage.getOrDefault(language, Map.of());
    }

    /**
     * The given profile's favorite channel ids (channel ids, not numbers/names), for populating the
     * favorite-channel-number selection list. Empty if the profile isn't found or has no favorites set.
     *
     * @param profileId the profileId to look up
     * @return the list of favorite channel ids, or an empty list if not found
     */
    public List<String> getFavoriteChannelIds(String profileId) {
        CustomerDto.ProfileDto profile = getProfiles().stream().filter(p -> profileId.equals(p.profileId)).findFirst()
                .orElse(null);
        List<String> favorites = profile == null ? null : profile.favoriteChannels;
        return favorites == null ? List.of() : favorites;
    }

    /**
     * @return the customer id, or {@code null} if not yet loaded
     */
    public @Nullable String getCustomerId() {
        CustomerDto c = customer;
        return c == null ? null : c.customerId;
    }

    /**
     * Resolves title/episode metadata for a linear/reviewBuffer/replay program from its {@code eventId}
     * (CRID).
     * <p>
     * Performs a real network call - callers must invoke this off the MQTT/event-bus thread.
     *
     * @param eventId the eventId (CRID) to look up
     * @param language the language code to use for the request
     * @return the event detail, or {@code null} if it could not be resolved
     */
    public @Nullable EventDetailDto getEventDetail(String eventId, String language) {
        LGHorizonAuthClient auth = authClient;
        if (auth == null) {
            return null;
        }
        try {
            String linearServiceUrl = auth.getServiceConfig().getServiceUrl(LINEAR_SERVICE_URL_FIELD);
            return auth.get(linearServiceUrl, EVENT_DETAIL_PATH.formatted(eventId, language), EventDetailDto.class);
        } catch (LGHorizonApiException | IllegalArgumentException e) {
            logger.debug("Could not resolve event detail for {}: {}", eventId, e.getMessage());
            return null;
        }
    }

    /**
     * Resolves title/episode metadata for a VOD (video on demand) asset from its {@code titleId}.
     * <p>
     * Performs a real network call - callers must invoke this off the MQTT/event-bus thread.
     *
     * @param titleId the titleId to look up
     * @param profileId the profileId to use for the request
     * @param language the language code to use for the request
     * @return the VOD detail, or {@code null} if it could not be resolved
     */
    public @Nullable VodDetailDto getVodDetail(String titleId, String profileId, String language) {
        LGHorizonAuthClient auth = authClient;
        CustomerDto c = customer;
        if (auth == null || c == null) {
            return null;
        }
        try {
            String vodServiceUrl = auth.getServiceConfig().getServiceUrl(VOD_SERVICE_URL_FIELD);
            Integer cityId = c.cityId;
            if (cityId == null) {
                throw new LGHorizonApiException("Cannot fetch VOD detail: customer cityId not available");
            }
            return auth.get(vodServiceUrl, VOD_DETAIL_PATH.formatted(titleId, language, profileId, cityId),
                    VodDetailDto.class);
        } catch (LGHorizonApiException | IllegalArgumentException e) {
            logger.debug("Could not resolve VOD detail for {}: {}", titleId, e.getMessage());
            return null;
        }
    }

    /**
     * Resolves title/episode metadata for an nDVR (network recording) from its {@code recordingId}.
     * <p>
     * Performs a real network call - callers must invoke this off the MQTT/event-bus thread.
     *
     * @param recordingId the recordingId to look up
     * @param profileId the profileId to use for the request
     * @param language the language code to use for the request
     * @return the recording detail, or {@code null} if it could not be resolved
     */
    public @Nullable RecordingDetailDto getRecordingDetail(String recordingId, String profileId, String language) {
        LGHorizonAuthClient auth = authClient;
        String householdId = getHouseholdId();
        if (auth == null || householdId == null) {
            return null;
        }
        try {
            String recordingServiceUrl = auth.getServiceConfig().getServiceUrl(RECORDING_SERVICE_URL_FIELD);
            return auth.get(recordingServiceUrl,
                    RECORDING_DETAIL_PATH.formatted(householdId, recordingId, profileId, language),
                    RecordingDetailDto.class);
        } catch (LGHorizonApiException | IllegalArgumentException e) {
            logger.debug("Could not resolve recording detail for {}: {}", recordingId, e.getMessage());
            return null;
        }
    }

    /**
     * Fetches raw image bytes from a (public, unauthenticated) CDN image URL and wraps them as a
     * {@link RawType} suitable for an {@code Image} channel. Performs a real network call - callers must
     * invoke this off the MQTT/event-bus thread.
     *
     * @param url the image URL to fetch
     * @return the image, or {@code null} if it could not be fetched
     */
    public @Nullable RawType fetchImage(String url) {
        String anonymizedUrl = LGHorizonContentAnonymizer.anonymizeTopic(url);
        try {
            InputStreamResponseListener listener = new InputStreamResponseListener();
            httpClient.newRequest(url).timeout(10, TimeUnit.SECONDS).send(listener);
            Response response = listener.get(10, TimeUnit.SECONDS);
            if (response.getStatus() >= 300) {
                notifyImageCapture("GET " + anonymizedUrl + " -> status=" + response.getStatus() + " (fetch failed)");
                try {
                    listener.getInputStream().close();
                } catch (IOException e) {
                    // Expected/harmless: closing before EOF signals to "abandon this response" to Jetty.
                }
                return null;
            }
            byte[] bytes;
            try (InputStream input = listener.getInputStream()) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] chunk = new byte[8192];
                int total = 0;
                int read;
                while ((read = input.read(chunk)) != -1) {
                    total += read;
                    if (total > MAX_IMAGE_BYTES) {
                        logger.debug("Image from {} exceeded {} bytes, aborting", anonymizedUrl, MAX_IMAGE_BYTES);
                        notifyImageCapture("GET " + anonymizedUrl + " -> exceeded " + MAX_IMAGE_BYTES
                                + " bytes, aborted (fetch failed)");
                        return null;
                    }
                    buffer.write(chunk, 0, read);
                }
                bytes = buffer.toByteArray();
            }
            String contentType = response.getHeaders().get(HttpHeader.CONTENT_TYPE);
            notifyImageCapture("GET " + anonymizedUrl + " -> status=" + response.getStatus() + ", contentType="
                    + contentType + ", bytes=" + bytes.length + " (content omitted)");
            return new RawType(bytes, contentType != null ? contentType : IMAGE_JPEG);
        } catch (TimeoutException | ExecutionException | IOException e) {
            logger.debug("Could not fetch image from {}: {}", anonymizedUrl, e.getMessage());
            notifyImageCapture("GET " + anonymizedUrl + " -> exception: " + e.getMessage());
            return null;
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.debug("Interrupted while fetching image from {}", anonymizedUrl);
            return null;
        }
    }

    private void notifyImageCapture(String description) {
        Consumer<String> listener = imageCaptureListener;
        if (listener != null) {
            listener.accept(description);
        }
    }

    /**
     * @return the household id for this account, or {@code null} if not yet loaded
     */
    public @Nullable String getHouseholdId() {
        LGHorizonAuthClient auth = authClient;
        return auth == null ? null : auth.getHouseholdId();
    }

    /**
     * Registers a box handler so it receives status/UI-status updates for its device id, and (re)subscribes the MQTT
     * topics needed to actually receive them for this specific box.
     *
     * @param deviceId the box's device id
     */
    public void registerBox(String deviceId, LGHorizonBoxHandler handler) {
        registeredBoxes.put(deviceId, handler);
        JsonObject cachedState = lastKnownStatusByDeviceId.get(deviceId);
        if (cachedState != null) {
            JsonElement stateElement = cachedState.get("state");
            if (stateElement != null && stateElement.isJsonPrimitive()) {
                handler.handleStatusMessage(cachedState.get("state").getAsString());
            }
        }
        handler.updateChannelNumberOptions();
        LGHorizonMqttClient mqtt = mqttClient;
        String householdId = getHouseholdId();
        if (mqtt != null && householdId != null) {
            requestBoxState(deviceId);
        }
    }

    /**
     * @param deviceId the box to unregister, e.g. when its thing is removed or disabled
     */
    public void unregisterBox(String deviceId) {
        registeredBoxes.remove(deviceId);
    }

    /**
     * @param deviceId the box to send the command to
     * @param w3cKey the W3C key value to send, e.g. {@code "ArrowUp"}, {@code "Enter"}, {@code "MediaPlayPause"}
     */
    public void sendKey(String deviceId, String w3cKey) {
        JsonObject payload = new JsonObject();
        payload.addProperty("type", "CPE.KeyEvent");
        payload.addProperty("runtimeType", "key");
        payload.addProperty("id", "openhab");
        payload.addProperty("source", deviceId.toLowerCase());
        JsonObject status = new JsonObject();
        status.addProperty("w3cKey", w3cKey);
        status.addProperty("eventType", "keyDownUp");
        payload.add("status", status);
        publishToBox(deviceId, payload);
    }

    /**
     * @param deviceId the box to send the command to
     * @param channelId the channel id to tune to (not the channel number)
     */
    public void tuneToChannel(String deviceId, String channelId) {
        JsonObject payload = new JsonObject();
        payload.addProperty("id", randomId(8));
        payload.addProperty("type", "CPE.pushToTV");
        JsonObject source = new JsonObject();
        source.addProperty("clientId", mqttClientIdOrEmpty());
        source.addProperty("friendlyDeviceName", "openHAB");
        payload.add("source", source);
        JsonObject status = new JsonObject();
        status.addProperty("sourceType", "linear");
        JsonObject statusSource = new JsonObject();
        statusSource.addProperty("channelId", channelId);
        status.add("source", statusSource);
        status.addProperty("relativePosition", 0);
        status.addProperty("speed", 1);
        payload.add("status", status);
        publishToBox(deviceId, payload);
    }

    /**
     * Displays an on-screen message for approximately {@code durationSeconds}, by publishing it repeatedly
     * every {@link #DISPLAY_MESSAGE_REPEAT_INTERVAL_SECONDS} seconds.
     *
     * @param deviceId the box to display the message on
     * @param message the message to display
     * @param durationSeconds how long to display the message, in seconds
     */
    public void displayMessage(String deviceId, String message, int durationSeconds) {
        JsonObject payload = new JsonObject();
        payload.addProperty("type", "CPE.pushToTV");
        JsonObject source = new JsonObject();
        source.addProperty("clientId", mqttClientIdOrEmpty());
        source.addProperty("friendlyDeviceName", "\n\n" + message);
        payload.add("source", source);
        JsonObject status = new JsonObject();
        status.addProperty("sourceType", "linear");
        JsonObject statusSource = new JsonObject();
        statusSource.addProperty("channelId", "1234");
        status.add("source", statusSource);
        status.addProperty("relativePosition", 0);
        status.addProperty("speed", 1);
        payload.add("status", status);

        int duration = Math.min(DISPLAY_MESSAGE_DURATION_MAX_SECONDS, durationSeconds);
        int repeats = Math.max(1, Math.round(duration / (float) DISPLAY_MESSAGE_REPEAT_INTERVAL_SECONDS));
        for (int i = 0; i < repeats; i++) {
            long delaySeconds = i * DISPLAY_MESSAGE_REPEAT_INTERVAL_SECONDS;
            scheduler.schedule(() -> publishDisplayMessage(deviceId, payload), delaySeconds, TimeUnit.SECONDS);
        }
    }

    private void publishDisplayMessage(String deviceId, JsonObject payload) {
        String id = randomId(8);
        payload.addProperty("id", id);
        if (pendingDisplayMessageIds.size() >= MAX_TRACKED_DISPLAY_MESSAGE_IDS) {
            pendingDisplayMessageIds.clear();
        }
        pendingDisplayMessageIds.add(id);
        publishToBox(deviceId, payload);
    }

    /**
     * @param deviceId the box to request the state for
     */
    public void requestBoxState(String deviceId) {
        long now = System.currentTimeMillis();
        Long last = lastStateRequestMillis.put(deviceId, now);
        if (last != null && now - last < STATE_REQUEST_DEBOUNCE_MILLIS) {
            return; // a request for this device was already sent very recently - the box is already answering it
        }
        JsonObject payload = new JsonObject();
        payload.addProperty("id", randomId(8));
        payload.addProperty("type", "CPE.getUiStatus");
        payload.addProperty("source", mqttClientIdOrEmpty());
        publishToBox(deviceId, payload);
    }

    private void publishToBox(String deviceId, JsonObject payload) {
        LGHorizonMqttClient mqtt = mqttClient;
        String householdId = getHouseholdId();
        if (mqtt == null || householdId == null) {
            logger.debug("Cannot send command to box {}: account not connected",
                    LGHorizonContentAnonymizer.anonymizeTopic(deviceId));
            return;
        }
        mqtt.publish(householdId + "/" + deviceId, payload);
    }

    private String mqttClientIdOrEmpty() {
        LGHorizonMqttClient mqtt = mqttClient;
        return mqtt == null ? "" : mqtt.getClientId();
    }

    private static String randomId(int length) {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(letters.charAt(random.nextInt(letters.length())));
        }
        return sb.toString();
    }

    @Override
    public void onConnected() {
        LGHorizonMqttClient mqtt = mqttClient;
        String householdId = getHouseholdId();
        if (mqtt == null || householdId == null) {
            return;
        }
        updateStatus(ThingStatus.ONLINE);
        for (String deviceId : registeredBoxes.keySet()) {
            requestBoxState(deviceId);
        }
    }

    @Override
    public void onConnectionLost(Throwable cause) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                textWithArg("offline.account-mqtt-connection-lost", String.valueOf(cause.getMessage())));
    }

    @Override
    public void onMessage(String topic, JsonObject payload) {
        logger.debug("MQTT Received on {}: {}", LGHorizonContentAnonymizer.anonymizeTopic(topic),
                LGHorizonContentAnonymizer.anonymizeMessage(payload.toString()));

        notifyLiveCapture(topic, payload);

        if (topic.contains("status") && payload.has("source") && payload.has("state")) {
            String source = payload.get("source").getAsString();
            lastKnownStatusByDeviceId.put(source, payload);
            LGHorizonBoxHandler handler = registeredBoxes.get(source);
            if (handler != null) {
                handler.handleStatusMessage(payload.get("state").getAsString());
            }
            CompletableFuture<JsonObject> pendingStatus = pendingStatusCaptures.remove(source);
            if (pendingStatus != null) {
                pendingStatus.complete(payload);
            }
            return;
        }

        if (payload.has("type") && "CPE.uiStatus".equals(payload.get("type").getAsString()) && payload.has("source")) {
            String source = payload.get("source").getAsString();
            LGHorizonBoxHandler handler = registeredBoxes.get(source);
            if (handler != null) {
                handler.handleUiStatusMessage(payload);
            }
            return;
        }

        if (payload.has("type") && "CPE.pushToTV.rsp".equals(payload.get("type").getAsString())) {
            JsonObject status = payload.has("status") && payload.get("status").isJsonObject()
                    ? payload.getAsJsonObject("status")
                    : null;
            String response = status != null && status.has("response") && !status.get("response").isJsonNull()
                    ? status.get("response").getAsString()
                    : null;
            if ("failed".equalsIgnoreCase(response)) {
                String id = payload.has("id") && !payload.get("id").isJsonNull() ? payload.get("id").getAsString()
                        : null;
                if (id != null && pendingDisplayMessageIds.remove(id)) {
                    // Expected: displayMessage()'s deliberate fake-channel tune always gets rejected this
                    // way, unrelated to whether the on-screen notification itself renders.
                    logger.debug("LG Horizon box rejected displayMessage()'s internal fake-tune (expected): {}",
                            LGHorizonContentAnonymizer.anonymizeMessage(payload.toString()));
                } else {
                    logger.warn("LG Horizon box rejected a command: {}",
                            LGHorizonContentAnonymizer.anonymizeMessage(payload.toString()));
                }
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // The account bridge itself has no channels.
    }

    @Override
    public void dispose() {
        LGHorizonMqttClient mqtt;
        synchronized (this) {
            disposed = true;
            mqtt = mqttClient;
            mqttClient = null;
        }

        ScheduledFuture<?> init = initializeFuture;
        if (init != null) {
            init.cancel(true);
        }
        ScheduledFuture<?> refresh = tokenRefreshFuture;
        if (refresh != null) {
            refresh.cancel(true);
        }
        if (mqtt != null) {
            mqtt.disconnect();
        }

        registeredBoxes.clear();
        lastKnownStatusByDeviceId.clear();
        pendingStatusCaptures.values().forEach(f -> f.cancel(true));
        pendingStatusCaptures.clear();
        liveCaptureListeners.clear();
        super.dispose();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(LGHorizonDiscoveryService.class);
    }
}
