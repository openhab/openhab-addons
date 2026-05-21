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
import static org.openhab.binding.miio.internal.MiIoBindingConstants.CONFIG_CLIENT_ID;
import static org.openhab.binding.miio.internal.MiIoBindingConstants.CONFIG_COUNTRY;
import static org.openhab.binding.miio.internal.MiIoBindingConstants.CONFIG_LOGIN_METHOD;
import static org.openhab.binding.miio.internal.MiIoBindingConstants.CONFIG_PASSWORD;
import static org.openhab.binding.miio.internal.MiIoBindingConstants.CONFIG_SERVICE_TOKEN;
import static org.openhab.binding.miio.internal.MiIoBindingConstants.CONFIG_SSECURITY;
import static org.openhab.binding.miio.internal.MiIoBindingConstants.CONFIG_USERNAME;
import static org.openhab.binding.miio.internal.MiIoBindingConstants.CONFIG_USER_ID;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.miio.internal.cloud.CloudConnector;
import org.openhab.binding.miio.internal.cloud.CloudLogonListener;
import org.openhab.binding.miio.internal.cloud.MiCloudConnector.CloudLoginMode;
import org.openhab.binding.miio.internal.cloud.MiCloudConnector.CloudLoginState;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpUtil;
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
public class MiIoCloudThingHandler extends BaseThingHandler implements CloudLogonListener {

    private static final int CLIENT_ID_LENGTH = 6;
    private static final String DEFAULT_COUNTRY = "ru,us,tw,sg,cn,de,i2";

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

        validateAndGenerateClientId();
        setupCloudConnector();
        loginFuture = scheduler.schedule(this::connectorLogin, 1, TimeUnit.SECONDS);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Initiating logon");
        updateState(CHANNEL_LOGON_IMAGE, UnDefType.NULL);
        updateState(CHANNEL_TWOFA, UnDefType.NULL);
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
        cloudConnector.registerListener(this);
    }

    @Override
    public void onLogonImage(byte[] captcha) {
        logger.debug("QR / Captcha received with length: {}", captcha.length);
        if (loginMethod == CloudLoginMode.QRCODE) {
            logger.info(
                    "QR code is ready for scanning. Open the '{}' channel in openHAB and scan the QR code with your Xiaomi app.",
                    CHANNEL_LOGON_IMAGE);
        } else {
            logger.info(
                    "Captcha image is available. Check the '{}' channel and submit the response via the '{}' channel.",
                    CHANNEL_LOGON_IMAGE, CHANNEL_CAPTCHA_RESPONSE);
        }
        String mimeType = HttpUtil.guessContentTypeFromData(captcha);
        updateState(CHANNEL_LOGON_IMAGE, new RawType(captcha, mimeType));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command for channel {}, with command: {}", channelUID.getId(), command);
        switch (channelUID.getId()) {
            case CHANNEL_CAPTCHA_RESPONSE:
                if (!(command instanceof StringType)) {
                    return;
                }
                logger.debug("Submit captcha response {}", command);
                cloudConnector.submitCaptcha(command.toString());
                break;
            case CHANNEL_TWOFA:
                if (!(command instanceof StringType)) {
                    return;
                }
                logger.debug("Received 2-factor authentication response {}", command);
                cloudConnector.submit2FA(command.toString());
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
            updateState(CHANNEL_LOGON_IMAGE, UnDefType.NULL);
            // Read back tokens after logon() completes; schedule with a short delay because
            // CloudConnector.logon() syncs its token fields after login() returns (i.e., after this callback fires)
            tokenReadFuture = scheduler.schedule(() -> {
                String newUserId = cloudConnector.getUserId();
                String newServiceToken = cloudConnector.getServiceToken();
                String newSsecurity = cloudConnector.getSsecurity();
                if (!newServiceToken.equals(this.serviceToken) || !newUserId.equals(this.userId)) {
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
            loginFuture = scheduler.schedule(() -> {
                updateThingProperties(Map.of(CONFIG_USER_ID, "", CONFIG_SERVICE_TOKEN, "", CONFIG_SSECURITY, ""));
                cloudConnector.setCredentials(username, password, country, clientId, "", "", "");
                cloudConnector.setLoginMode(loginMethod);
                connectorLogin();
            }, 2, TimeUnit.SECONDS);
        } else if (loginState == CloudLoginState.AWAITING_2FA) {
            logger.info("Two-factor authentication required. Please submit the 2FA code via the '{}' channel.",
                    CHANNEL_TWOFA);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Two-factor authentication required. Enter code via the twofa channel.");
        } else if (loginState == CloudLoginState.AWAITING_CAPTCHA) {
            logger.info(
                    "Captcha is required. Check the '{}' channel image and submit the response via the '{}' channel.",
                    CHANNEL_LOGON_IMAGE, CHANNEL_CAPTCHA_RESPONSE);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Captcha verification required. Check the logonimage channel and submit via captcharesponse.");
        } else if (loginState == CloudLoginState.AWAITING_QRLOGIN) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "QR code ready. Scan with the Mi Home app via the logonimage channel.");
        } else if (loginState == CloudLoginState.ACCESS_DENIED) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Authentication denied by Xiaomi cloud. Check credentials or re-scan QR.");
        } else if (loginState == CloudLoginState.CAPTCHA_FAILED) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Captcha verification failed. Check the logonimage channel and try again.");
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "Cloud communication error. Login state: " + loginState);
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
}
