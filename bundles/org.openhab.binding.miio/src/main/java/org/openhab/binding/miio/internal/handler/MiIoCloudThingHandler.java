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

import static org.openhab.binding.miio.internal.MiIoBindingConstants.CHANNEL_CAPTCHA_RESPONSE;
import static org.openhab.binding.miio.internal.MiIoBindingConstants.CHANNEL_LOGON_IMAGE;
import static org.openhab.binding.miio.internal.MiIoBindingConstants.CHANNEL_TWOFA;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.miio.internal.cloud.CloudConnector;
import org.openhab.binding.miio.internal.cloud.CloudLogonListener;
import org.openhab.binding.miio.internal.cloud.MiCloudConnector;
import org.openhab.binding.miio.internal.cloud.MiCloudConnector.CloudLoginMode;
import org.openhab.binding.miio.internal.cloud.MiCloudConnector.CloudLoginState;
import org.openhab.binding.miio.internal.cloud.MiCloudException;
import org.openhab.binding.miio.internal.cloud.MiCloudQRConnector;
import org.openhab.binding.miio.internal.cloud.MiCloudUserIdLogonConnector;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpUtil;
import org.openhab.core.library.types.RawType;
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
public class MiIoCloudThingHandler extends BaseThingHandler implements CloudLogonListener {

    private static final int CLIENT_ID_LENGTH = 6;
    private static final String DEFAULT_COUNTRY = "ru,us,tw,sg,cn,de,i2";

    private final Logger logger = LoggerFactory.getLogger(MiIoCloudThingHandler.class);
    private final HttpClient httpClient;
    private @Nullable ScheduledFuture<?> loginFuture;

    private final CloudConnector cloudConnector;
    private @Nullable MiCloudUserIdLogonConnector miCloudConnector;

    private String username = "";
    private String password = "";
    private String country = "";
    private String userId = "";
    private String clientId = "";
    private String ssecurity = "";
    private String serviceToken = "";
    private CloudLoginMode loginMethod = CloudLoginMode.QRCODE;

    public MiIoCloudThingHandler(Thing thing, CloudConnector cloudConnector, HttpClient httpClientFactory) {
        super(thing);
        this.cloudConnector = cloudConnector;
        this.httpClient = httpClientFactory;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Cloud Connector handler '{}'", getThing().getUID());

        Configuration properties = getThing().getConfiguration();
        this.username = getConfigString(properties, "username");
        this.password = getConfigString(properties, "password");
        String country = getConfigString(properties, "country");
        this.country = country.isBlank() ? DEFAULT_COUNTRY : country;
        this.clientId = getConfigString(properties, "clientId");
        this.userId = getConfigString(properties, "userId");
        this.serviceToken = getConfigString(properties, "serviceToken");
        this.ssecurity = getConfigString(properties, "ssecurity");
        String loginMethodvalue = getConfigString(properties, "loginMethod");
        this.loginMethod = "PASSWORD".equalsIgnoreCase(loginMethodvalue) ? CloudLoginMode.PASSWORD
                : CloudLoginMode.QRCODE;

        validateAndGenerateClientId();
        updateState(CHANNEL_LOGON_IMAGE, UnDefType.NULL);
        updateState(CHANNEL_TWOFA, UnDefType.NULL);

        if (hasValidCredentials()) {
            setupCloudConnector();
            cloudConnector.setLoginMode(CloudLoginMode.TOKEN);
            loginFuture = scheduler.schedule(this::connectorLogin, 1, TimeUnit.SECONDS);
        } else {
            if (this.loginMethod == CloudLoginMode.QRCODE) {
                logger.debug("Login method is QR code");
                loginFuture = scheduler.schedule(this::startQRLogin, 1, TimeUnit.SECONDS);
            } else {
                logger.debug("Login method is User ID");
                loginFuture = scheduler.schedule(this::startUserIdLogin, 1, TimeUnit.SECONDS);
            }
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Initiating logon");
    }

    private String getConfigString(Configuration config, String key) {
        String value = (String) config.get(key);
        return value == null ? "" : value;
    }

    private void startQRLogin() {
        logger.info("Logon with QR code for username: [{}]", username);
        try {
            MiCloudQRConnector miCloudQRConnector = new MiCloudQRConnector(username, password, httpClient, clientId,
                    userId, serviceToken, ssecurity);
            miCloudQRConnector.registerListener(this);
            if (miCloudQRConnector.login()) {
                this.userId = miCloudQRConnector.getUserId();
                this.serviceToken = miCloudQRConnector.getServiceToken();
                this.ssecurity = miCloudQRConnector.getSsecurity();
                updateThingProperties(
                        Map.of("userId", this.userId, "serviceToken", this.serviceToken, "ssecurity", this.ssecurity));
                updateState(CHANNEL_LOGON_IMAGE, UnDefType.NULL);
                setupCloudConnector();
                cloudConnector.setLoginMode(CloudLoginMode.TOKEN);
                connectorLogin();
            } else {
                logger.warn("QR code login failed");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "QR code login failed");
            }
        } catch (MiCloudException e) {
            logger.warn("Error during login to Xiaomi cloud", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void connectorLogin() {
        cloudConnector.isConnected(true);
    }

    private void validateAndGenerateClientId() {
        final String clientId = this.clientId;
        if (clientId.isBlank() || clientId.length() != CLIENT_ID_LENGTH) {
            logger.debug("Client Id is empty, generating new one");
            this.clientId = generateRandomClientId();
            scheduler.schedule(() -> updateThingProperties(Map.of("clientId", this.clientId)), 1, TimeUnit.SECONDS);
        }
    }

    private String generateRandomClientId() {
        return new Random().ints(97, 123).limit(CLIENT_ID_LENGTH)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
    }

    private boolean hasValidCredentials() {
        return !userId.isBlank() && !serviceToken.isBlank() && !ssecurity.isBlank();
    }

    private void setupCloudConnector() {
        cloudConnector.setCredentials(username, password, country, clientId, userId, serviceToken, ssecurity);
        cloudConnector.registerListener(this);
    }

    private void startUserIdLogin() {
        logger.debug("Logon with username {}", username);
        try {
            final MiCloudUserIdLogonConnector miCloudConnector = new MiCloudUserIdLogonConnector(username, password,
                    httpClient, clientId, userId, serviceToken, ssecurity);
            this.miCloudConnector = miCloudConnector;
            miCloudConnector.registerListener(this);
            miCloudConnector.login();
        } catch (MiCloudException e) {
            logger.warn("Error during login to Xiaomi cloud", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void onLogonImage(byte[] captcha) {
        logger.debug("QR / Captcha received with length: {}", captcha.length);
        if (loginMethod == CloudLoginMode.QRCODE) {
            logger.info("QR code is ready for scanning. Open the '{}' channel in openHAB and scan the QR code with your Xiaomi app.",
                    CHANNEL_LOGON_IMAGE);
        } else {
            logger.info("Captcha image is available. Check the '{}' channel and submit the response via the '{}' channel.",
                    CHANNEL_LOGON_IMAGE, CHANNEL_CAPTCHA_RESPONSE);
        }
        String mimeType = HttpUtil.guessContentTypeFromData(captcha);
        updateState(CHANNEL_LOGON_IMAGE, new RawType(captcha, mimeType));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command for channel {}, with command: {}", channelUID.getId(), command);
        final MiCloudConnector mcc = miCloudConnector;
        if (mcc == null) {
            logger.debug("Command ignored: cloud connector not available");
            return;
        }
        switch (channelUID.getId()) {
            case CHANNEL_CAPTCHA_RESPONSE:
                logger.debug("Submit captcha response {}", command.toString());
                mcc.login(command.toString());
                break;
            case CHANNEL_TWOFA:
                logger.debug("Received 2-factor authentication response {}", command.toString());
                if (mcc instanceof MiCloudUserIdLogonConnector) {
                    ((MiCloudUserIdLogonConnector) mcc).FAResponse(command.toString());
                }
                break;
            default:
                logger.info("Cannot handle channel {}", channelUID);
        }
    }

    private void updateThingProperties(Map<String, String> updatedProperties) {
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
            final MiCloudUserIdLogonConnector conn = miCloudConnector;
            if (conn != null && !conn.getServiceToken().isEmpty()) {
                this.userId = conn.getUserId();
                this.serviceToken = conn.getServiceToken();
                this.ssecurity = conn.getSsecurity();
                final Map<String, String> tokens = Map.of("userId", this.userId, "serviceToken", this.serviceToken,
                        "ssecurity", this.ssecurity);
                scheduler.schedule(() -> {
                    updateThingProperties(tokens);
                    setupCloudConnector();
                    cloudConnector.setLoginMode(CloudLoginMode.TOKEN);
                    connectorLogin();
                }, 0, TimeUnit.SECONDS);
            }
        } else if (loginState == CloudLoginState.ACCESS_DENIED && hasValidCredentials()) {
            // Stored token was rejected — clear it and fall back to QR or password login
            logger.info("Stored token rejected by Xiaomi cloud. Clearing token and retrying with {} login",
                    loginMethod);
            this.userId = "";
            this.serviceToken = "";
            this.ssecurity = "";
            scheduler.schedule(() -> updateThingProperties(Map.of("userId", "", "serviceToken", "", "ssecurity", "")),
                    0, TimeUnit.SECONDS);
            if (loginMethod == CloudLoginMode.QRCODE) {
                loginFuture = scheduler.schedule(this::startQRLogin, 2, TimeUnit.SECONDS);
            } else {
                loginFuture = scheduler.schedule(this::startUserIdLogin, 2, TimeUnit.SECONDS);
            }
        } else if (loginState == CloudLoginState.AWAITING_2FA) {
            logger.info("Two-factor authentication required. Please submit the 2FA code via the '{}' channel.",
                    CHANNEL_TWOFA);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, loginState.toString());
        } else if (loginState == CloudLoginState.AWAITING_CAPTCHA) {
            logger.info("Captcha is required. Check the '{}' channel image and submit the response via the '{}' channel.",
                    CHANNEL_LOGON_IMAGE, CHANNEL_CAPTCHA_RESPONSE);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, loginState.toString());
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, loginState.toString());
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
    }
}
