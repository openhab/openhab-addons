/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.miio.internal.MiIoBindingConfiguration;
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
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
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

    private final Bundle bundle;
    private final Logger logger = LoggerFactory.getLogger(MiIoCloudThingHandler.class);
    private final HttpClient httpClient;
    private final ScheduledExecutorService miIoScheduler;

    private @Nullable MiIoBindingConfiguration configuration;
    private final CloudConnector cloudConnector;
    // private @Nullable MiCloudConnector miCloudConnector;
    private @Nullable MiCloudUserIdLogonConnector miCloudConnector;

    private String cloudServer = "";
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
        this.bundle = FrameworkUtil.getBundle(this.getClass());
        this.miIoScheduler = scheduler; // new ScheduledThreadPoolExecutor(3,
        // new NamedThreadFactory("binding-" + getThing().getUID().getAsString(), true));
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
        this.loginMethod = "USERID".equalsIgnoreCase(loginMethodvalue) ? CloudLoginMode.PASSWORD
                : CloudLoginMode.QRCODE;

        validateAndGenerateClientId();

        if (hasValidCredentials()) {
            setupCloudConnector();
            cloudConnector.setLoginMode(CloudLoginMode.TOKEN);
            miIoScheduler.schedule(this::connectorLogin, 1, TimeUnit.SECONDS);

            // miIoScheduler.schedule(this::startqrLogin, 1, TimeUnit.SECONDS);
            // miIoScheduler.schedule(this::startLogin, 1, TimeUnit.SECONDS);
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Initiating logon");

        } else {
            // setupCloudConnector();

            if (this.loginMethod == CloudLoginMode.QRCODE) {
                logger.debug("Login method is QR code");
                miIoScheduler.schedule(this::startqrLogin, 1, TimeUnit.SECONDS);
            } else {
                logger.debug("Login method is User ID");
                miIoScheduler.schedule(this::startLogin, 1, TimeUnit.SECONDS);
            }

            // scheduleLogin();
        }

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Initiating logon");
    }

    private String getConfigString(Configuration config, String key) {
        String value = (String) config.get(key);
        return value == null ? "" : value;
    }

    private void startqrLogin() {

        logger.debug("Logon with QR code for username {}", username);
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
            } else {
                logger.info("QR code login failed");
            }
            ;
        } catch (MiCloudException e) {
            logger.info("Error during login to Xiaomi cloud", e);
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
            miIoScheduler.schedule(() -> updateThingProperties(Map.of("clientId", this.clientId)), 1, TimeUnit.SECONDS);
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
        // cloudConnector.setLoginMode(loginMethod);
        cloudConnector.registerListener(this);
    }

    private void scheduleLogin() {
        miIoScheduler.schedule(this::startLogin, 1, TimeUnit.SECONDS);
        // miIoScheduler.schedule(this::qrcode, 1, TimeUnit.SECONDS);
    }

    private void startLogin() {
        logger.debug("Logon with username {}", username);
        try {

            // miCloudConnector = new MiCloudQRConnector(username, password, httpClient, clientId, userId, serviceToken,
            // ssecurity);
            final MiCloudUserIdLogonConnector miCloudConnector = new MiCloudUserIdLogonConnector(username, password,
                    httpClient, clientId, userId, serviceToken, ssecurity);
            this.miCloudConnector = miCloudConnector;
            miCloudConnector.registerListener(this);
            miCloudConnector.login();
        } catch (MiCloudException e) {
            logger.info("Error during login to Xiaomi cloud", e);
        }
    }

    @Override
    public void onCaptcha(byte[] captcha) {
        logger.debug("Captcha received with length: {}", captcha.length);
        String mimeType = HttpUtil.guessContentTypeFromData(captcha);
        updateState(CHANNEL_CAPTCHA, new RawType(captcha, mimeType));
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
        logger.info("Cloud login state updated: {} - {}", loginState, status);
        if (loginState == CloudLoginState.ONLINE) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, loginState.toString());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Xiaomi Mi IO handler '{}'", getThing().getUID());
        // shutdownScheduler();
    }

    private void shutdownScheduler() {

        miIoScheduler.shutdown();
        try {
            if (!miIoScheduler.awaitTermination(3, TimeUnit.SECONDS)) {
                miIoScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            miIoScheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
