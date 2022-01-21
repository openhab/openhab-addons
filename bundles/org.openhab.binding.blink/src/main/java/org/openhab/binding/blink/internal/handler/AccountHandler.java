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
package org.openhab.binding.blink.internal.handler;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.openhab.binding.blink.internal.servlet.AccountVerificationServlet;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
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
 */
@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler {

    public static final String GENERATED_CLIENT_ID = "generatedClientId";
    private static final OffsetDateTime EPOCH_UTC = Instant.EPOCH.atOffset(ZoneOffset.UTC);
    private static final int TOKEN_TTL = 12; // hours
    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);
    BundleContext bundleContext;

    @NonNullByDefault({})
    AccountConfiguration config;
    AccountService blinkService;
    private final HttpService httpService;
    private final NetworkAddressService networkAddressService;
    private String generatedClientId = "";
    @Nullable
    AccountVerificationServlet accountServlet;
    @Nullable
    BlinkAccount blinkAccount;
    @Nullable
    BlinkHomescreen cachedHomescreen;
    OffsetDateTime eventSince = EPOCH_UTC;
    final Map<Long, BlinkEvents.Media> eventStore = new ConcurrentHashMap<>();
    @Nullable
    ScheduledFuture<?> refreshStateJob;
    private final Object refreshStateMonitor = new Object();

    public AccountHandler(Bridge bridge, HttpService httpService, BundleContext bundleContext,
            NetworkAddressService networkAddressService, HttpClientFactory httpClientFactory, Gson gson) {
        super(bridge);
        this.httpService = httpService;
        this.bundleContext = bundleContext;
        this.networkAddressService = networkAddressService;
        this.blinkService = new AccountService(httpClientFactory.getCommonHttpClient(), gson);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(BlinkDiscoveryService.class);
    }

    @Override
    public void initialize() {
        config = getConfigAs(AccountConfiguration.class);

        // set the thing status to UNKNOWN temporarily and let the background task decide for the real status.
        // the framework is then able to reuse the resources from the thing handler initialization.
        // we set this upfront to reliably check status updates in unit tests.
        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {

            // register 2FA Verification Servlet for this thing
            if (accountServlet == null) {
                try {
                    accountServlet = new AccountVerificationServlet(httpService, bundleContext, this, blinkService);
                } catch (IllegalStateException e) {
                    logger.warn("Failed to create account servlet", e);
                }
            }

            // get (or generate for new bridges) the blink client id
            boolean start2FA = false;
            Map<String, String> properties = editProperties();
            String generatedClientId = properties.get(GENERATED_CLIENT_ID);
            if (generatedClientId == null) {
                generatedClientId = blinkService.generateClientId();
                start2FA = true;
                properties.put(GENERATED_CLIENT_ID, generatedClientId);
            }
            this.generatedClientId = generatedClientId;
            try {
                // call login api
                blinkAccount = blinkService.login(config, generatedClientId, start2FA);
                blinkAccount.lastTokenRefresh = Instant.now();
                properties.putAll(blinkAccount.toAccountProperties());
                // don't know how to get scheme and port from openhab, right now it's hardcoded...
                String scheme = "http://";
                int port = 8080;
                String validationUrl = scheme + networkAddressService.getPrimaryIpv4HostAddress() + ":" + port
                        + "/blink/" + thing.getUID().getId();
                properties.put("validationUrl", validationUrl);
                updateProperties(properties);
                // do 2FA if necessary
                if (blinkAccount.account.client_verification_required) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "Waiting for 2 Factor Authentication. Please go to " + validationUrl
                                    + " to enter the PIN you received via SMS or email");
                } else {
                    setOnline();
                }
            } catch (Exception e) {
                logger.error("Error connecting to Blink servers with given credentials", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        });
    }

    @Override
    public void dispose() {
        disposeServlet();
        blinkService.dispose();
        cleanup();
        super.dispose();
    }

    private void disposeServlet() {
        AccountVerificationServlet accountServlet = this.accountServlet;
        if (accountServlet != null) {
            accountServlet.dispose();
        }
        this.accountServlet = null;
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
        refreshState(false);
        updateStatus(ThingStatus.ONLINE);
    }

    public void setOffline(Throwable cause) {
        logger.error("Blink Account is going offline after communication error", cause);
        blinkAccount = null;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, cause.getMessage());
    }

    boolean ensureBlinkAccount() {
        boolean refreshToken = (blinkAccount != null &&
                Instant.now().isAfter(blinkAccount.lastTokenRefresh.plus(TOKEN_TTL, ChronoUnit.HOURS)));
        if (blinkAccount != null && !refreshToken) {
            return true;
        }
        try {
            if (refreshToken)
                logger.debug("Refreshing blink security token");
            else
                logger.debug("blink security token lost. Getting new one.");
            Map<String, String> properties = editProperties();
            blinkAccount = blinkService.login(config, generatedClientId, false);
            blinkAccount.lastTokenRefresh = Instant.now();
            properties.putAll(blinkAccount.toAccountProperties());
            updateProperties(properties);
            updateStatus(ThingStatus.ONLINE);
            return true;
        } catch (IOException e) {
            logger.error("Could not update blink security token.", e);
            if (refreshToken)
                setOffline(e);
            return false;
        }
    }

    /**
     * Perform a homescreen update, either because refreshInterval has elapsed or due to an event.
     *
     * @param scheduled true if refreshInterval has elapsed, otherwise false
     */
    void refreshState(boolean scheduled) {
        synchronized (refreshStateMonitor) {
            try {
                if (!scheduled && refreshStateJob != null) {
                    refreshStateJob.cancel(true);
                }
                if (ensureBlinkAccount()) {
                    loadDevices();
                    loadEvents();
                }
            } finally {
                refreshStateJob = scheduler.schedule(() -> refreshState(true), config.refreshInterval,
                        TimeUnit.SECONDS);
            }
        }
    }

    public @Nullable BlinkHomescreen getDevices(boolean refresh) {
        if (refresh || cachedHomescreen == null) {
            refreshState(false);
        }
        return cachedHomescreen;
    }

    void loadDevices() {
        logger.debug("Loading devices from Blink API");
        try {
            cachedHomescreen = blinkService.getDevices(blinkAccount);
            fireHomescreenUpdate();
        } catch (IOException e) {
            setOffline(e);
        }
    }

    void loadEvents() {
        logger.debug("Loading events from Blink API");
        try {
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
        } catch (IOException e) {
            setOffline(e);
        }
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
        Long cameraId = camera.cameraId;
        BlinkHomescreen devices = getDevices(refresh);
        if (blinkAccount == null) {
            logger.error("Blink Account not set in bridge");
            throw new IOException("Blink Account not set in bridge");
        }
        if (devices == null) {
            throw new IOException("No cameras found for account");
        }
        List<BlinkCamera> cameras = (camera.cameraType == CameraConfiguration.CameraType.CAMERA) ?
                devices.cameras : devices.owls;
        if (cameras == null || cameras.isEmpty()) {
            logger.error("Unknown camera {} for account {}", cameraId, blinkAccount.account.account_id);
            throw new IOException("No cameras found for account");
        }
        List<BlinkCamera> foundCameras = cameras.stream().filter(c -> Objects.equals(c.network_id, camera.networkId))
                .filter(c -> Objects.equals(c.id, cameraId)).collect(Collectors.toUnmodifiableList());
        if (foundCameras.size() > 1) {
            logger.error("More than one camera {} for account {}", cameraId, blinkAccount.account.account_id);
            throw new IOException("More than one camera found for id " + cameraId);
        } else if (foundCameras.isEmpty()) {
            logger.error("Unknown camera {} for account {}", cameraId, blinkAccount.account.account_id);
            throw new IOException("Unknown camera");
        }
        return foundCameras.get(0);
    }

    BlinkNetwork getNetworkState(String networkId, boolean refresh) throws IOException {
        BlinkHomescreen devices = getDevices(refresh);
        if (blinkAccount == null) {
            logger.error("Blink Account not set in bridge");
            throw new IOException("Blink Account not set in bridge");
        }
        if (devices == null || devices.networks == null || devices.networks.isEmpty()) {
            logger.error("Unknown network {} for account {}", networkId, blinkAccount.account.account_id);
            throw new IOException("No networks found for account");
        }
        try {
            List<BlinkNetwork> networks = devices.networks.stream().filter(n -> n.id.equals(Long.parseLong(networkId)))
                    .collect(Collectors.toUnmodifiableList());
            if (networks.size() == 1)
                return networks.get(0);
            else if (networks.size() > 1)
                throw new IOException("More than one network found with id " + networkId);
        } catch (NumberFormatException e) {
            logger.error("Bad network id, must be numeric: {}", networkId);
        }
        logger.error("Unknown network {} for account {}", networkId, blinkAccount.account.account_id);
        throw new IOException("Unknown network");
    }

    public OnOffType getBattery(CameraConfiguration camera) throws IOException {
        String battery = getCameraState(camera, false).battery;
        if ("ok".equals(battery))
            return OnOffType.OFF;
        else
            return OnOffType.ON;
    }

    public double getTemperature(CameraConfiguration camera) throws IOException {
        return getCameraState(camera, false).signals.temp;
    }

    public OnOffType getMotionDetection(CameraConfiguration camera, boolean refreshCache) throws IOException {
        return OnOffType.from(getCameraState(camera, refreshCache).enabled);
    }

    public OnOffType getNetworkArmed(String networkId, boolean refreshCache) throws IOException {
        return OnOffType.from(getNetworkState(networkId, refreshCache).armed);
    }

    private void fireHomescreenUpdate() {
        streamEventListeners().forEach(EventListener::handleHomescreenUpdate);
    }

    void fireMediaEvent(BlinkEvents.Media mediaEvent) {
        streamEventListeners().forEach(listener -> listener.handleMediaEvent(mediaEvent));
    }

    private Stream<EventListener> streamEventListeners() {
        return getThing().getThings().stream().map(Thing::getHandler).filter(Objects::nonNull)
                .map(EventListener.class::cast);
    }
}
