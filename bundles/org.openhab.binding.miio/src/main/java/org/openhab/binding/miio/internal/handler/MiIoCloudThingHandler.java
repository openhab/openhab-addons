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
package org.openhab.binding.miio.internal.handler;

import static org.openhab.binding.miio.internal.MiIoBindingConstants.*;

import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miio.internal.Utils;
import org.openhab.binding.miio.internal.cloud.CloudConnector;
import org.openhab.binding.miio.internal.cloud.CloudLoginListener;
import org.openhab.binding.miio.internal.cloud.MiCloudConnector.CloudLoginMode;
import org.openhab.binding.miio.internal.cloud.MiCloudConnector.CloudLoginState;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.RawType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MiIoCloudThingHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiIoCloudThingHandler extends BaseThingHandler implements CloudLoginListener {

    private static final int CLIENT_ID_LENGTH = 6;
    private static final String DEFAULT_COUNTRY = "ru,us,tw,sg,cn,de,i2";
    private static final long MIN_RETRY_INTERVAL_MS = 30_000L;

    private static final RawType HOURGLASS_IMAGE = loadStatusImage("cloud_initiating.svg");
    private static final RawType HAPPY_IMAGE = loadStatusImage("cloud_online.svg");

    private final Logger logger = LoggerFactory.getLogger(MiIoCloudThingHandler.class);
    private @Nullable ScheduledFuture<?> loginFuture;
    private @Nullable ScheduledFuture<?> propertyUpdateFuture;
    private @Nullable ScheduledFuture<?> tokenReadFuture;

    private final CloudConnector cloudConnector;

    private String username = "";
    private String password = "";
    private String country = "";
    private volatile String userId = "";
    private volatile String clientId = "";
    private volatile String ssecurity = "";
    private volatile String serviceToken = "";
    private CloudLoginMode loginMethod = CloudLoginMode.QRCODE;
    private String cloudDiscoveryMode = "disabled";
    private volatile long lastLoginTriggerTime = 0;

    public MiIoCloudThingHandler(Thing thing, CloudConnector cloudConnector) {
        super(thing);
        this.cloudConnector = cloudConnector;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Cloud Connector handler '{}'", getThing().getUID());

        Configuration properties = getThing().getConfiguration();
        this.username = getConfigString(properties, CONFIG_USERNAME);
        this.password = getConfigString(properties, CONFIG_PASSWORD);
        String country = getConfigString(properties, CONFIG_COUNTRY);
        this.country = country.isBlank() ? DEFAULT_COUNTRY : country;
        this.clientId = getConfigString(properties, CONFIG_CLIENT_ID);
        this.userId = getConfigString(properties, CONFIG_USER_ID);
        this.serviceToken = getConfigString(properties, CONFIG_SERVICE_TOKEN);
        this.ssecurity = getConfigString(properties, CONFIG_SSECURITY);
        String loginMethodvalue = getConfigString(properties, CONFIG_LOGIN_METHOD);
        this.loginMethod = "PASSWORD".equalsIgnoreCase(loginMethodvalue) ? CloudLoginMode.PASSWORD
                : CloudLoginMode.QRCODE;
        this.cloudDiscoveryMode = getConfigString(properties, CONFIG_CLOUD_DISCOVERY_MODE);

        validateAndGenerateClientId();
        setupCloudConnector();
        loginFuture = scheduler.schedule(this::connectorLogin, 1, TimeUnit.SECONDS);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.cloud-initiating");
        updateState(CHANNEL_LOGIN_IMAGE, HOURGLASS_IMAGE);
        updateState(CHANNEL_TWOFA, UnDefType.NULL);
        updateState(CHANNEL_TRIGGER_LOGIN, OnOffType.OFF);
    }

    private String getConfigString(Configuration config, String key) {
        String value = (String) config.get(key);
        return value == null ? "" : value;
    }

    private void connectorLogin() {
        cloudConnector.isConnected(true);
    }

    private void validateAndGenerateClientId() {
        final String clientId = this.clientId;
        if (clientId.isBlank() || clientId.length() != CLIENT_ID_LENGTH) {
            logger.debug("Client Id is empty, generating new one");
            this.clientId = generateRandomClientId();
            propertyUpdateFuture = scheduler.schedule(
                    () -> updateThingProperties(Map.of(CONFIG_CLIENT_ID, this.clientId)), 1, TimeUnit.SECONDS);
        }
    }

    private String generateRandomClientId() {
        return new SecureRandom().ints(97, 123).limit(CLIENT_ID_LENGTH)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

    private boolean hasValidCredentials() {
        return !userId.isBlank() && !serviceToken.isBlank() && !ssecurity.isBlank();
    }

    private void setupCloudConnector() {
        cloudConnector.setCredentials(username, password, country, clientId, userId, serviceToken, ssecurity);
        cloudConnector.setLoginMode(hasValidCredentials() ? CloudLoginMode.TOKEN : loginMethod);
        cloudConnector.setCloudDiscoveryMode(cloudDiscoveryMode);
        cloudConnector.registerListener(this);
    }

    @Override
    public void onLoginImage(byte[] captcha) {
        logger.debug("QR / Captcha received with length: {}", captcha.length);
        if (loginMethod == CloudLoginMode.QRCODE) {
            logger.info("QR code is ready for scanning. Open the '{}' channel in openHAB and scan the QR code.",
                    CHANNEL_LOGIN_IMAGE);
        } else {
            logger.info(
                    "Captcha image is available. Check the '{}' channel and submit the response via the '{}' channel.",
                    CHANNEL_LOGIN_IMAGE, CHANNEL_CAPTCHA_RESPONSE);
        }
        String mimeType = HttpUtil.guessContentTypeFromData(captcha);
        updateState(CHANNEL_LOGIN_IMAGE, new RawType(captcha, mimeType));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command for channel {}, with command: {}", channelUID.getId(), command);
        switch (channelUID.getId()) {
            case CHANNEL_CAPTCHA_RESPONSE:
                if (!(command instanceof StringType)) {
                    return;
                }
                logger.debug("Submit captcha response {}", Utils.obfuscateToken(command.toString()));
                cloudConnector.submitCaptcha(command.toString());
                break;
            case CHANNEL_TWOFA:
                if (!(command instanceof StringType)) {
                    return;
                }
                logger.debug("Received 2-factor authentication response {}", Utils.obfuscateToken(command.toString()));
                cloudConnector.submit2FA(command.toString());
                break;
            case CHANNEL_TRIGGER_LOGIN:
                if (OnOffType.ON.equals(command)) {
                    updateState(CHANNEL_TRIGGER_LOGIN, OnOffType.OFF);
                    triggerNewLoginSequence();
                }
                break;
            default:
                logger.info("Cannot handle channel {}", channelUID);
        }
    }

    private void updateThingProperties(Map<String, String> updatedProperties) {
        Configuration currentConfig = getThing().getConfiguration();
        boolean hasChanges = updatedProperties.entrySet().stream()
                .anyMatch(e -> !e.getValue().equals(currentConfig.get(e.getKey())));
        if (!hasChanges) {
            return;
        }
        logger.debug("Updating thing properties for {}", getThing().getUID());
        Configuration config = editConfiguration();
        updatedProperties.forEach(config::put);
        updateConfiguration(config);
    }

    @Override
    public void onStatusUpdated(CloudLoginState loginState, String status) {
        logger.debug("Cloud login state updated: {} - {}", loginState, status);
        if (loginState == CloudLoginState.ONLINE) {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
            updateState(CHANNEL_TWOFA, UnDefType.NULL);
            updateState(CHANNEL_LOGIN_IMAGE, HAPPY_IMAGE);
            // Read back tokens after login() completes; schedule with a short delay because
            // CloudConnector.login() syncs its token fields after login() returns (i.e., after this callback fires)
            ScheduledFuture<?> existingTokenFuture = tokenReadFuture;
            if (existingTokenFuture != null && !existingTokenFuture.isDone()) {
                existingTokenFuture.cancel(true);
            }
            tokenReadFuture = scheduler.schedule(() -> {
                String newUserId = cloudConnector.getUserId();
                String newServiceToken = cloudConnector.getServiceToken();
                String newSsecurity = cloudConnector.getSsecurity();
                if (!newServiceToken.equals(this.serviceToken) || !newUserId.equals(this.userId)
                        || !newSsecurity.equals(this.ssecurity)) {
                    this.userId = newUserId;
                    this.serviceToken = newServiceToken;
                    this.ssecurity = newSsecurity;
                    updateThingProperties(Map.of(CONFIG_USER_ID, this.userId, CONFIG_SERVICE_TOKEN, this.serviceToken,
                            CONFIG_SSECURITY, this.ssecurity));
                }
            }, 1, TimeUnit.SECONDS);
        } else if (loginState == CloudLoginState.ACCESS_DENIED && hasValidCredentials()) {
            // Stored token was rejected — clear it and fall back to QR or password login
            logger.info("Stored token rejected by Xiaomi cloud. Clearing token and retrying with {} login",
                    loginMethod);
            this.userId = "";
            this.serviceToken = "";
            this.ssecurity = "";
            ScheduledFuture<?> existingLoginFuture = loginFuture;
            if (existingLoginFuture != null && !existingLoginFuture.isDone()) {
                existingLoginFuture.cancel(true);
            }
            loginFuture = scheduler.schedule(() -> {
                updateThingProperties(Map.of(CONFIG_USER_ID, "", CONFIG_SERVICE_TOKEN, "", CONFIG_SSECURITY, ""));
                cloudConnector.setCredentials(username, password, country, clientId, "", "", "");
                cloudConnector.setLoginMode(loginMethod);
                cloudConnector.setCloudDiscoveryMode(cloudDiscoveryMode);
                connectorLogin();
            }, 2, TimeUnit.SECONDS);
        } else if (loginState == CloudLoginState.AWAITING_2FA) {
            logger.info("Two-factor authentication required. Please submit the 2FA code via the '{}' channel.",
                    CHANNEL_TWOFA);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.cloud-awaiting-2fa");
        } else if (loginState == CloudLoginState.AWAITING_CAPTCHA) {
            logger.info(
                    "Captcha is required. Check the '{}' channel image and submit the response via the '{}' channel.",
                    CHANNEL_LOGIN_IMAGE, CHANNEL_CAPTCHA_RESPONSE);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.cloud-awaiting-captcha");
        } else if (loginState == CloudLoginState.AWAITING_QRLOGIN) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/offline.cloud-awaiting-qr");
        } else if (loginState == CloudLoginState.ACCESS_DENIED) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.cloud-access-denied");
        } else if (loginState == CloudLoginState.CAPTCHA_FAILED) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.cloud-captcha-failed");
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.cloud-comm-error");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Xiaomi Mi IO Cloudconnector handler '{}'", getThing().getUID());
        ScheduledFuture<?> future = loginFuture;
        if (future != null) {
            future.cancel(true);
            loginFuture = null;
        }
        ScheduledFuture<?> propFuture = propertyUpdateFuture;
        if (propFuture != null) {
            propFuture.cancel(true);
            propertyUpdateFuture = null;
        }
        ScheduledFuture<?> tokenFuture = tokenReadFuture;
        if (tokenFuture != null) {
            tokenFuture.cancel(true);
            tokenReadFuture = null;
        }
        cloudConnector.unregisterListener(this);
    }

    /**
     * Cancels any in-progress login, resets the connector state, and schedules a fresh login
     * sequence. Has a 30-second cooldown to prevent rapid re-triggering.
     */
    private void triggerNewLoginSequence() {
        long now = System.currentTimeMillis();
        if (now - lastLoginTriggerTime < MIN_RETRY_INTERVAL_MS) {
            logger.debug("Login re-trigger ignored: another login attempt was started less than {} seconds ago.",
                    MIN_RETRY_INTERVAL_MS / 1000);
            return;
        }
        lastLoginTriggerTime = now;
        logger.info("Re-triggering login sequence for Cloud Connector '{}'", getThing().getUID());

        ScheduledFuture<?> existingLoginFuture = loginFuture;
        if (existingLoginFuture != null) {
            existingLoginFuture.cancel(true);
            loginFuture = null;
        }

        // Clear stored token credentials from handler state so the connector uses interactive login
        this.userId = "";
        this.serviceToken = "";
        this.ssecurity = "";

        // Stop any in-progress connector and reset the login state
        cloudConnector.resetLogin();

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "@text/offline.cloud-retrigger");
        updateState(CHANNEL_LOGIN_IMAGE, HOURGLASS_IMAGE);
        updateState(CHANNEL_TWOFA, UnDefType.NULL);

        loginFuture = scheduler.schedule(() -> {
            updateThingProperties(Map.of(CONFIG_USER_ID, "", CONFIG_SERVICE_TOKEN, "", CONFIG_SSECURITY, ""));
            cloudConnector.setCredentials(username, password, country, clientId, "", "", "");
            cloudConnector.setLoginMode(loginMethod);
            cloudConnector.setCloudDiscoveryMode(cloudDiscoveryMode);
            connectorLogin();
        }, 1, TimeUnit.SECONDS);
    }

    private static RawType loadStatusImage(String filename) {
        try (InputStream stream = MiIoCloudThingHandler.class.getResourceAsStream("/images/" + filename)) {
            if (stream != null) {
                return new RawType(stream.readAllBytes(), "image/svg+xml");
            }
        } catch (Exception e) {
            // fall through to empty image — must never throw here or the class fails to initialise
        }
        return new RawType(new byte[0], "image/svg+xml");
    }
}
