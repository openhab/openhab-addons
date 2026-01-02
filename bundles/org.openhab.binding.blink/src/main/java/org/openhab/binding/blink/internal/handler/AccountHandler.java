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
package org.openhab.binding.blink.internal.handler;

import static org.openhab.binding.blink.internal.BlinkBindingConstants.PROPERTY_HARDWARE_ID;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.blink.internal.config.AccountConfiguration;
import org.openhab.binding.blink.internal.config.CameraConfiguration;
import org.openhab.binding.blink.internal.discovery.BlinkDiscoveryService;
import org.openhab.binding.blink.internal.dto.BlinkAccount;
import org.openhab.binding.blink.internal.dto.BlinkCamera;
import org.openhab.binding.blink.internal.dto.BlinkEvents;
import org.openhab.binding.blink.internal.dto.BlinkHomescreen;
import org.openhab.binding.blink.internal.dto.BlinkNetwork;
import org.openhab.binding.blink.internal.service.AccountService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link AccountHandler} is responsible for initializing the blink account which is used as a bridge
 * for the blink things camera and network.
 *
 * It also provides the methods for updating camera and network states.
 *
 * @author Matthias Oesterheld - Initial contribution
 * @author Robert T. Brown (-rb) - support Blink Authentication changes in 2025 (OAUTHv2)
 * @author Volker Bier - add support for Doorbells
 */
@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler {

    private static final OffsetDateTime EPOCH_UTC = Instant.EPOCH.atOffset(ZoneOffset.UTC);
    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);
    BundleContext bundleContext;

    @NonNullByDefault({})
    AccountConfiguration config;
    AccountService blinkService;
    Storage<String> storage;
    private String generatedClientId = "";
    @Nullable
    BlinkAccount blinkAccount;
    @Nullable
    BlinkHomescreen cachedHomescreen;
    OffsetDateTime eventSince = EPOCH_UTC;
    final Map<Long, BlinkEvents.Media> eventStore = new ConcurrentHashMap<>();
    @Nullable
    ScheduledFuture<?> refreshStateJob;
    private final Object refreshStateMonitor = new Object();
    private int consecutiveRefreshFailures = 0;
    private Gson gson;
    private static int MAX_FAILURES_BEFORE_OFFLINE = 5;
    private static String STORAGE_KEY_BLINKACCOUNT = "BlinkAccount";

    public AccountHandler(Bridge bridge, BundleContext bundleContext, StorageService storageService,
            HttpClientFactory httpClientFactory, Gson gson) {
        super(bridge);
        this.bundleContext = bundleContext;
        this.gson = gson;
        String uuid = this.getThing().getUID().getAsString();
        uuid = uuid.replace(':', '_'); // The filesystem storage service converts ":" to "%3A", I prefer a clean "_"S
        String storageName = "org.openhab.binding.blink." + uuid;
        this.storage = storageService.getStorage(storageName, String.class.getClassLoader());
        this.blinkService = new AccountService(httpClientFactory.getCommonHttpClient(), storage, gson);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(BlinkDiscoveryService.class);
    }

    @SuppressWarnings("null")
    @Override
    public void initialize() {
        // logger.warn("WARNING: This is a pre-release version of the Blink Binding, 2026-01-08");
        AccountConfiguration newConfig = getConfigAs(AccountConfiguration.class);
        final boolean userEmailUpdated;
        String explanation = "Wait for login";
        if (config != null && !config.email.equals(newConfig.email)) {
            // the user modified the BlinkAccount Configuration to change their email address.
            // In this situation we do not want to continue to use old tokens, particularly if
            // they are trying to SWITCH to a different blink account. Force a full login.
            logger.warn("User's email address has changed, must perform full login (was {}, now {})", config.email,
                    newConfig.email);
            userEmailUpdated = true;
            explanation = "Wait for login (email address changed!)";
        } else {
            userEmailUpdated = false;
        }
        config = getConfigAs(AccountConfiguration.class);

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING, explanation);

        scheduler.execute(() -> {

            // get (or generate for new bridges) the blink hardware id.
            // hardwareId is stored in Storage, but also in Properties in case Storage is lost.
            Map<String, String> properties = editProperties();
            String hardwareId = properties.get(PROPERTY_HARDWARE_ID);
            if (!userEmailUpdated) {
                // must not resume via tokens if user modifies their email address (blink account)
                boolean fullyAuthenticated = resumeSessionFromTokens();
                if (fullyAuthenticated) {
                    return;
                }
            }
            // If the hardwareId from Storage is different from the one in Properties, use Storage.
            // Why? because it's tied to the tokens. However, Storage isn't saved until a successful
            // login, including MFA. Until that time, we need to retain the HardwareId. Also, if the
            // user creates a new blink Account but wants to copy the Storage to the new account
            // (e.g. migration to a new machine?), then honor the one from Storage.
            if (blinkAccount != null && !blinkAccount.account.hardware_id.equals(hardwareId)) {
                hardwareId = blinkAccount.account.hardware_id;
                updateProperty(PROPERTY_HARDWARE_ID, hardwareId);
            }
            // remove old Properties used by openhab blink binding v4.1.
            // need to call updateProperty() one at a time in order to erase them.
            updateProperty("lastTokenRefresh", null);
            updateProperty("clientId", null);
            updateProperty("token", null);
            updateProperty("generatedClientId", null);
            updateProperty("validationUrl", null);

            // unable to find or use any previous authentication tokens.
            // Determine if we are at stage 1 (ready to send user/pass) or stage 2 (user typed in the MFA code)
            boolean needLoginStage1 = false;
            if ((config.mfaCode == null) || config.mfaCode.isBlank() || (hardwareId == null) || hardwareId.isBlank()) {
                needLoginStage1 = true;
                logger.trace("Initial Login required to initialize OAUTH parameters. Existing parameters:");
                logger.trace("  MFA Code = {}", config.mfaCode);
                logger.trace("  Hardware Id = {}", hardwareId);
                // The hardware_id needs to stay consistent across OAUTH flow.
                // Hopefully we loaded one from Storage from a previous successful authentication.
                // If not, hopefully we retrieved it from Properties, from a previous (failed) authentication.
                // If not, we need to make a fresh one, save it in Properties, use it in authentication, and
                // ultimately save it in Storage after successful authentication.
                if ((hardwareId == null) || (hardwareId.isBlank())) {
                    hardwareId = UUID.randomUUID().toString().toUpperCase();
                    updateProperty(PROPERTY_HARDWARE_ID, hardwareId);
                }
            }
            String potentialCause = "username and password";
            try {
                if (needLoginStage1) {
                    logger.debug("Logging into Blink Servers using OAUTH. Our hardware id is {}", hardwareId);
                    blinkService.loginStage1WithUsername(config, hardwareId);
                    logger.debug("Successfully sent username & password. Now waiting for MFA Code");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "Waiting for MFA Code. Enter the MFA Code you received via SMS or email, then Save");
                } else {
                    logger.debug("Submitting MFA Code to Blink Servers to complete authentication process");
                    potentialCause = "MFA code";
                    blinkAccount = blinkService.loginStage2WithMfa(config, hardwareId);
                    Configuration updatedConfig = editConfiguration();
                    updatedConfig.put("mfaCode", ""); // MFA can only be used one time, and we did, so clear it now
                    updateConfiguration(updatedConfig);
                    setOnline();
                    storeBlinkAccount(blinkAccount);
                }
            } catch (Exception e) {
                logger.error("Error connecting to Blink servers with given credentials. Check {}", potentialCause, e);
                // After a failure, discard the MFA code so as to start the process over again
                Configuration updatedConfig = editConfiguration();
                updatedConfig.put("mfaCode", "");
                updateConfiguration(updatedConfig);
                // Have observed that during an authentication failure, Blink is sending back an HTTP status
                // indicating a Challenge failure, but they are not sending a WWW-Authentication header
                // as required by the HTTP protocol. Because of this, the openhab framework's built-in
                // jetty client throws an exception which says:
                // "HTTP protocol violation: Authentication challenge without WWW-Authenticate header"
                // And this is totally Blink's fault, and of no value to the openhab user. So I'm going
                // to send a more user-friendly explanation in the Offline details.
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "Credentials were not accepted by Blink. Verify the " + potentialCause);
            }
        });
    }

    /**
     * When blinkAccount is null, we can attempt to load the BlinkAccount from Storage, which
     * may include usable authentication tokens. This is useful at a bootup, as well as during
     * operations if the Blink Servers are unavailable for a period of time, forcing us OFFLINE.
     *
     * This method modifies the instance variable this.blinkAccount.
     *
     * If it fails to load anything, then this.blinkAccount will remain null.
     * However, if it is able to load information from Storage, then the contents of this.blinkAccount
     * with either be:
     *
     * (A: if we are fully authenticated to commercial Blink servers), this.blinkAccount will
     * be complete, including authentication tokens,
     *
     * OR
     *
     * (B: if Storage contained the user's account information, but the Blink servers rejected the auth tokens)
     * this.blinkAccount will be partially populated, meaning that this.blinkAccount.account has our previously
     * stored information, but this.blinkAccount.auth contains only null fields.)
     *
     * @return boolean indicating if we are fully authenticated. If false, then the caller should initiate the
     *         stage1 login flow, providing the username and password, and wait for the user to receive an MFA code.
     *
     */
    @SuppressWarnings("null")
    private boolean resumeSessionFromTokens() {
        if (blinkAccount == null || (blinkAccount.auth.refresh_token == null)) {
            // we don't have any tokens in memory. Load from storage to see if there are any there.
            String blinkAccountJson = storage.get(STORAGE_KEY_BLINKACCOUNT);
            if (blinkAccountJson != null) {
                // See if we can refresh the authentication token from the previous execution
                BlinkAccount storedBlinkAccount = gson.fromJson(blinkAccountJson, BlinkAccount.class);
                if (storedBlinkAccount == null) {
                    logger.debug("BlinkAccount information from persistent storage appears to be corrupt: {}",
                            storedBlinkAccount);
                    return false;
                }
                logger.debug("Inspecting BlinkAccount information from persistent storage: {}", storedBlinkAccount);
                if (storedBlinkAccount.auth == null || storedBlinkAccount.auth.refresh_token == null
                        || storedBlinkAccount.auth.refresh_token.isBlank()) {
                    logger.debug("Stored BlinkAccount does not contain any tokens");
                    return false;
                }
                blinkAccount = new BlinkAccount(storedBlinkAccount);
            }
        }
        if (blinkAccount == null) {
            // no account in memory, and no account in JSON (maybe we just upgraded from openhab v4.1?)
            // in any case, we can't resume a session, so return false to initiate the complete login flow.
            return false;
        }
        // at this point, we have a populated BlinkAccount, either from memory, or from Storage. Try it!
        try {
            boolean tokenValid = blinkService.verifyAuthentication(blinkAccount);
            if (!tokenValid) {
                blinkAccount = blinkService.refreshToken(blinkAccount);
            }
            setOnline();
            storeBlinkAccount(blinkAccount);
            return true;
        } catch (IOException e) {
            logger.info("Unable to refresh existing authentication token (fresh login may be required)");
        }

        return false;
    }

    private void storeBlinkAccount(@Nullable BlinkAccount account) {
        if (account == null || account.auth == null || account.account == null) {
            throw new IllegalArgumentException("This Blink Account is not authenticated yet");
        }
        Map<String, String> newProps = editProperties();
        newProps = account.toAccountProperties();
        updateProperties(newProps);
        String accountJson = gson.toJson(account);
        storage.put(STORAGE_KEY_BLINKACCOUNT, accountJson);
    }

    @Override
    public void dispose() {
        logger.debug("Disposing account handler");
        blinkService.dispose();
        cleanup();
        super.dispose();
    }

    void cleanup() {
        synchronized (refreshStateMonitor) {
            if (refreshStateJob != null) {
                refreshStateJob.cancel(true);
                refreshStateJob = null;
            }
        }
        logger.debug("cleanup {}", getThing().getUID().getAsString());
    }

    public void setOnline() {
        logger.debug("Setting blink account bridge online");
        boolean scheduleAutoRefresh;
        synchronized (refreshStateMonitor) {
            scheduleAutoRefresh = (refreshStateJob == null);
        }
        refreshState(scheduleAutoRefresh);
        updateStatus(ThingStatus.ONLINE);
    }

    public void setOffline(Throwable cause) {
        setOffline(cause.getMessage());
    }

    public void setOffline(@Nullable String reason) {
        logger.error("Blink Account is going offline after communication error: {}", reason);
        blinkAccount = null;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, reason);
    }

    @SuppressWarnings("null")
    boolean ensureAccessTokenIsValid() {
        if (blinkAccount == null || blinkAccount.auth == null) {
            // If we had been ONLINE, but went OFFLINE due to network issues, we would arrive here
            // with blinkAccount being null, but perfectly resumable using the tokens in Storage
            // (once the network connectivity is restored, of course). So try that first.
            if (!resumeSessionFromTokens()) {
                logger.debug("Not currently able to connect to the commercial Blink Servers");
                return false;
            }
        }
        boolean refreshTokenNow = (blinkAccount.auth.tokenExpiresAt == null
                || Instant.now().isAfter(blinkAccount.auth.tokenExpiresAt));
        if (!refreshTokenNow) {
            return true;
        }
        // The access_token is expiring. Use the refresh_token to refresh it.
        try {
            logger.debug("Refreshing blink authentication token (it expired at {})", blinkAccount.auth.tokenExpiresAt);
            blinkAccount = blinkService.refreshToken(blinkAccount);
            updateStatus(ThingStatus.ONLINE);
            storeBlinkAccount(blinkAccount);
            return true;
        } catch (IOException e) {
            logger.error("Could not refresh blink authentication token", e);
            // the java exception is logged, let's use something slightly more user friendly for the UI
            setOffline("Unable to refresh the authentication token. Re-Enable to login using a new MFA code.");
            return false;
        }
    }

    /**
     * Perform a homescreen update, either because refreshInterval has elapsed or due to an event.
     *
     * @param autoScheduleMore true if refreshInterval has elapsed, otherwise false
     */
    void refreshState(boolean autoScheduleMore) {
        synchronized (refreshStateMonitor) {
            try {
                if (ensureAccessTokenIsValid()) {
                    loadDevices();
                    loadEvents();
                    consecutiveRefreshFailures = 0;
                    updateStatus(ThingStatus.ONLINE);
                }
            } catch (IOException e) {
                consecutiveRefreshFailures++;
                logger.warn("Communication error ({}/{}) while refreshing devices: Error: {}",
                        consecutiveRefreshFailures, MAX_FAILURES_BEFORE_OFFLINE, e.getMessage());
                if (consecutiveRefreshFailures >= MAX_FAILURES_BEFORE_OFFLINE) {
                    setOffline("Unable to connect to commercial Blink Servers");
                    consecutiveRefreshFailures = 0;
                } else {
                    if (getThing().getStatus() == ThingStatus.ONLINE) {
                        // We are "online" but our periodic refresh task has failed to connect.
                        // Could be a transient sort of error, but let's reflect the condition.
                        updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Transient Communication Error ("
                                + consecutiveRefreshFailures + "/" + MAX_FAILURES_BEFORE_OFFLINE + "). Retrying...");
                    }
                }
            } finally {
                if (autoScheduleMore) {
                    config = getConfigAs(AccountConfiguration.class);
                    logger.trace("Handler ({}) is scheduling another refresh in {} seconds", this.hashCode(),
                            config.refreshInterval);
                    refreshStateJob = scheduler.schedule(() -> refreshState(true), config.refreshInterval,
                            TimeUnit.SECONDS);
                }
            }
        }
    }

    public @Nullable BlinkHomescreen getDevices(boolean refresh) {
        if (refresh || cachedHomescreen == null) {
            refreshState(false);
        }
        return cachedHomescreen;
    }

    void loadDevices() throws IOException {
        logger.debug("Loading devices from Blink API");
        cachedHomescreen = blinkService.getDevices(blinkAccount);
        fireHomescreenUpdate();
    }

    void loadEvents() throws IOException {
        logger.debug("Loading events from Blink API");
        BlinkEvents events = blinkService.getEvents(blinkAccount, eventSince);
        OffsetDateTime nextEventSince = eventSince;
        for (BlinkEvents.Media mediaEvent : events.media) {
            if (mediaEvent.deleted) {
                eventStore.remove(mediaEvent.id);
            } else {
                eventStore.put(mediaEvent.id, mediaEvent);
            }
            if (nextEventSince.isBefore(mediaEvent.updated_at)) {
                nextEventSince = mediaEvent.updated_at;
            }
            // only trigger events if not polling for the first time
            if (!eventSince.isEqual(EPOCH_UTC)) {
                fireMediaEvent(mediaEvent);
            }
        }
        eventSince = nextEventSince;
    }

    public final @Nullable BlinkAccount getBlinkAccount() {
        return this.blinkAccount;
    }

    public final AccountConfiguration getConfiguration() {
        return this.config;
    }

    public final String getGeneratedClientId() {
        return generatedClientId;
    }

    BlinkCamera getCameraState(CameraConfiguration camera, boolean refresh) throws IOException {
        BlinkAccount account = blinkAccount;
        if (account == null) {
            logger.error("Blink Account is not authenticated yet");
            throw new IOException("Blink Account is not authenticated yet");
        }
        return getCameraState(account, camera, refresh);
    }

    private BlinkCamera getCameraState(BlinkAccount account, CameraConfiguration camera, boolean refresh)
            throws IOException {
        Long cameraId = camera.cameraId;
        BlinkHomescreen devices = getDevices(refresh);

        if (devices == null) {
            throw new IOException("No cameras found for account");
        }
        List<BlinkCamera> listToSearch = null;
        switch (camera.cameraType) {
            case CameraConfiguration.CameraType.CAMERA:
                listToSearch = devices.cameras;
                break;
            case CameraConfiguration.CameraType.DOORBELL:
                listToSearch = devices.doorbells;
                break;
            case CameraConfiguration.CameraType.OWL:
                listToSearch = devices.owls;
                break;
        }
        if (listToSearch == null || listToSearch.isEmpty()) {
            logger.debug("camera type is {}, and its list is empty", camera.cameraType);
            logger.error("Unknown camera {} for account {}", cameraId, account.account.account_id);
            throw new IOException("No cameras of type " + camera.cameraType + " found for account");
        }
        List<BlinkCamera> foundCameras = listToSearch.stream()
                .filter(c -> Objects.equals(c.network_id, camera.networkId)).filter(c -> Objects.equals(c.id, cameraId))
                .collect(Collectors.toUnmodifiableList());
        if (foundCameras.size() > 1) {
            logger.error("More than one camera {} for account {}", cameraId, account.account.account_id);
            throw new IOException("More than one camera found for id " + cameraId);
        } else if (foundCameras.isEmpty()) {
            logger.error("Unknown camera {} for account {} (there are {} other cameras of type {})", cameraId,
                    account.account.account_id, listToSearch.size(), camera.cameraType);
            throw new IOException("Unknown camera " + cameraId);
        }
        return foundCameras.get(0);
    }

    BlinkNetwork getNetworkState(String networkId, boolean refresh) throws IOException {
        BlinkAccount account = blinkAccount;
        if (account == null) {
            logger.error("Blink Account is not authenticated yet");
            throw new IOException("Blink Account is not authenticated yet");
        }
        return getNetworkState(account, networkId, refresh);
    }

    private BlinkNetwork getNetworkState(BlinkAccount account, String networkId, boolean refresh) throws IOException {
        BlinkHomescreen devices = getDevices(refresh);
        if (devices == null || devices.networks == null || devices.networks.isEmpty()) {
            logger.error("Unknown network {} for account {}", networkId, account.account.account_id);
            throw new IOException("No networks found for account");
        }
        try {
            @SuppressWarnings("null")
            List<BlinkNetwork> networks = devices.networks.stream().filter(n -> n.id.equals(Long.parseLong(networkId)))
                    .collect(Collectors.toUnmodifiableList());
            if (networks.size() == 1) {
                return networks.get(0);
            } else if (networks.size() > 1) {
                throw new IOException("More than one network found with id " + networkId);
            }
        } catch (NumberFormatException e) {
            logger.error("Bad network id, must be numeric: {}", networkId);
        }
        logger.error("Unknown network {} for account {}", networkId, account.account.account_id);
        throw new IOException("Unknown network");
    }

    public OnOffType getBattery(CameraConfiguration camera) throws IOException {
        BlinkAccount account = blinkAccount;
        if (account == null) {
            logger.error("Blink Account is not authenticated yet");
            throw new IOException("Blink Account is not authenticated yet");
        }

        String battery = getCameraState(account, camera, false).battery;
        if ("ok".equals(battery)) {
            return OnOffType.OFF;
        } else {
            return OnOffType.ON;
        }
    }

    public double getTemperature(CameraConfiguration camera) throws IOException {
        BlinkAccount account = blinkAccount;
        if (account == null) {
            logger.error("Blink Account is not authenticated yet");
            throw new IOException("Blink Account is not authenticated yet");
        }

        return getCameraState(account, camera, false).signals.temp;
    }

    public OnOffType getMotionDetection(CameraConfiguration camera, boolean refreshCache) throws IOException {
        BlinkAccount account = blinkAccount;
        if (account == null) {
            logger.error("Blink Account is not authenticated yet");
            throw new IOException("Blink Account is not authenticated yet");
        }
        return OnOffType.from(getCameraState(account, camera, refreshCache).enabled);
    }

    public OnOffType getNetworkArmed(String networkId, boolean refreshCache) throws IOException {
        BlinkAccount account = blinkAccount;

        if (account == null) {
            logger.error("Blink Account is not authenticated yet");
            throw new IOException("Blink Account is not authenticated yet");
        }
        return OnOffType.from(getNetworkState(account, networkId, refreshCache).armed);
    }

    private void fireHomescreenUpdate() {
        streamEventListeners().forEach(EventListener::handleHomescreenUpdate);
    }

    void fireMediaEvent(BlinkEvents.Media mediaEvent) {
        streamEventListeners().forEach(listener -> listener.handleMediaEvent(mediaEvent));
    }

    private Stream<EventListener> streamEventListeners() {
        // -rb removed the filter for status ONLINE, otherwise dead battery cameras will never return online
        // .filter(thing -> thing.getStatus() == ThingStatus.ONLINE)
        return getThing().getThings().stream().map(Thing::getHandler).filter(Objects::nonNull)
                .map(EventListener.class::cast);
    }
}
