/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.blink.internal.handler;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.blink.internal.config.AccountConfiguration;
import org.openhab.binding.blink.internal.discovery.BlinkDiscoveryService;
import org.openhab.binding.blink.internal.dto.BlinkAccount;
import org.openhab.binding.blink.internal.dto.BlinkCamera;
import org.openhab.binding.blink.internal.dto.BlinkHomescreen;
import org.openhab.binding.blink.internal.dto.BlinkNetwork;
import org.openhab.binding.blink.internal.service.AccountService;
import org.openhab.binding.blink.internal.servlet.AccountVerificationServlet;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.osgi.service.http.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;

/**
 * The {@link AccountHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler {

    public static final String GENERATED_CLIENT_ID = "generatedClientId";
    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);

    private @Nullable AccountConfiguration config;
    private final AccountService blinkService;
    private final HttpService httpService;
    private Gson gson;
    private @Nullable AccountVerificationServlet accountServlet;
    private @Nullable BlinkAccount blinkAccount;
    @NonNullByDefault({}) ExpiringCache<@Nullable BlinkHomescreen> homescreenCache;

    public AccountHandler(Bridge bridge, HttpService httpService, HttpClientFactory httpClientFactory, Gson gson) {
        super(bridge);
        this.httpService = httpService;
        this.gson = gson;
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
                    accountServlet = new AccountVerificationServlet(httpService, this, blinkService);
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
            }
            try {
                // call login api
                blinkAccount = blinkService.login(config, generatedClientId, start2FA);
                properties.putAll(blinkAccount.toAccountProperties());
                String validationUrl = "/blink/" + thing.getUID().getId();
                properties.put("validationUrl", validationUrl);
                updateProperties(properties);
                // do 2FA if necessary
                if (blinkAccount.account.client_verification_required) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                            "Waiting for 2 Factor Authentication. Please go to <youropenhab>" +
                                    validationUrl + " to enter the PIN you received via SMS or email");
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

    private void cleanup() {
        logger.debug("cleanup {}", getThing().getUID().getAsString());
    }

    public void setOnline() {
        this.homescreenCache = new ExpiringCache<>(Duration.ofSeconds(config.refreshInterval), this::loadDevices);
        updateStatus(ThingStatus.ONLINE);
    }

    public @Nullable BlinkHomescreen getDevices(boolean refresh) {
        if (refresh) {
            return homescreenCache.refreshValue();
        } else {
            return homescreenCache.getValue();
        }
    }

    private @Nullable BlinkHomescreen loadDevices() {
        try {
            return blinkService.getDevices(blinkAccount);
        } catch (IOException e) {
            logger.error("Error retrieving devices from Blink API: {}", e.getMessage());
            return null;
        }
    }

    public final @Nullable BlinkAccount getBlinkAccount() {
        return this.blinkAccount;
    }

    public final @Nullable AccountConfiguration getConfiguration() {
        return this.config;
    }

    private BlinkCamera getCameraState(String cameraId, boolean refresh) throws IOException {
        BlinkHomescreen devices = getDevices(refresh);
        if (blinkAccount == null) {
            logger.error("Blink Account not set in bridge");
            throw new IOException("Blink Account not set in bridge");
        }
        if (devices == null || devices.cameras == null) {
            logger.error("Unknown camera {} for account {}", cameraId, blinkAccount.account.account_id);
            throw new IOException("Unknown camera");
        }
        try {
            List<BlinkCamera> cameras = devices.cameras.stream().filter(c -> c.id.equals(Long.parseLong(cameraId)))
                    .collect(Collectors.toUnmodifiableList());
            if (cameras.size() == 1)
                return cameras.get(0);
        } catch (NumberFormatException e) {
            logger.error("Bad camera id, must be numeric: {}", cameraId);
        }
        logger.error("Unknown camera {} for account {}", cameraId, blinkAccount.account.account_id);
        throw new IOException("Unknown camera");
    }

    private BlinkNetwork getNetworkState(String networkId, boolean refresh) throws IOException {
        BlinkHomescreen devices = getDevices(refresh);
        if (blinkAccount == null) {
            logger.error("Blink Account not set in bridge");
            throw new IOException("Blink Account not set in bridge");
        }
        if (devices == null || devices.networks == null) {
            logger.error("Unknown network {} for account {}", networkId, blinkAccount.account.account_id);
            throw new IOException("Unknown network");
        }
        try {
            List<BlinkNetwork> networks = devices.networks.stream().filter(n -> n.id.equals(Long.parseLong(networkId)))
                    .collect(Collectors.toUnmodifiableList());
            if (networks.size() == 1)
                return networks.get(0);
        } catch (NumberFormatException e) {
            logger.error("Bad network id, must be numeric: {}", networkId);
        }
        logger.error("Unknown network {} for account {}", networkId, blinkAccount.account.account_id);
        throw new IOException("Unknown network");
    }

    public OnOffType getBattery(String cameraId) throws IOException {
        String battery = getCameraState(cameraId, false).battery;
        if (!"ok".equals(battery))
            return OnOffType.OFF;
        else
            return OnOffType.ON;
    }

    public long getTemperature(String cameraId) throws IOException {
        return getCameraState(cameraId, false).signals.temp;
    }

    public OnOffType getMotionDetection(String cameraId, boolean refreshCache) throws IOException {
        return OnOffType.from(getCameraState(cameraId, refreshCache).enabled);
    }

    public OnOffType getNetworkArmed(String networkId, boolean refreshCache) throws IOException {
        return OnOffType.from(getNetworkState(networkId, refreshCache).armed);
    }
}
