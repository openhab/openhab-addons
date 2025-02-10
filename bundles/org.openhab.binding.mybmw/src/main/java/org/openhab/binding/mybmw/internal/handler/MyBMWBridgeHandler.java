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
package org.openhab.binding.mybmw.internal.handler;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.mybmw.internal.MyBMWBridgeConfiguration;
import org.openhab.binding.mybmw.internal.MyBMWConstants;
import org.openhab.binding.mybmw.internal.discovery.VehicleDiscovery;
import org.openhab.binding.mybmw.internal.handler.auth.MyBMWAuthServlet;
import org.openhab.binding.mybmw.internal.handler.backend.MyBMWFileProxy;
import org.openhab.binding.mybmw.internal.handler.backend.MyBMWHttpProxy;
import org.openhab.binding.mybmw.internal.handler.backend.MyBMWProxy;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.net.NetUtil;
import org.openhab.core.net.NetworkAddressService;
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

/**
 * The {@link MyBMWBridgeHandler} is responsible for handling commands, which
 * are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 * @author Martin Grassl - refactored, all discovery functionality moved to VehicleDiscovery
 */
@NonNullByDefault
public class MyBMWBridgeHandler extends BaseBridgeHandler {

    private static final String ENVIRONMENT = "ENVIRONMENT";
    private static final String TEST = "test";
    private static final String TESTUSER = "testuser";

    private final Logger logger = LoggerFactory.getLogger(MyBMWBridgeHandler.class);

    private final HttpClient httpClient;
    private final OAuthFactory oAuthFactory;
    private final HttpService httpService;
    private final NetworkAddressService networkAddressService;
    private Optional<MyBMWProxy> myBmwProxy = Optional.empty();
    private Optional<ScheduledFuture<?>> initializerJob = Optional.empty();
    private Optional<VehicleDiscovery> vehicleDiscovery = Optional.empty();
    private LocaleProvider localeProvider;

    private CompletableFuture<Boolean> isInitialized = new CompletableFuture<>();

    private Optional<MyBMWAuthServlet> authServlet = Optional.empty();
    private boolean tokenInitError = false;

    public MyBMWBridgeHandler(Bridge bridge, HttpClientFactory httpClientFactory, OAuthFactory oAuthFactory,
            HttpService httpService, NetworkAddressService networkAddressService, LocaleProvider localeProvider) {
        super(bridge);
        this.httpClient = httpClientFactory.getCommonHttpClient();
        this.oAuthFactory = oAuthFactory;
        this.httpService = httpService;
        this.networkAddressService = networkAddressService;
        this.localeProvider = localeProvider;
    }

    public void setVehicleDiscovery(VehicleDiscovery vehicleDiscovery) {
        logger.trace("MyBMWBridgeHandler.setVehicleDiscovery");
        this.vehicleDiscovery = Optional.of(vehicleDiscovery);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands available
        logger.trace("MyBMWBridgeHandler.handleCommand");
    }

    @Override
    public void initialize() {
        isInitialized = new CompletableFuture<>();
        tokenInitError = false;

        logger.trace("MyBMWBridgeHandler.initialize");
        updateStatus(ThingStatus.UNKNOWN);

        MyBMWBridgeConfiguration localBridgeConfiguration = getConfigAs(MyBMWBridgeConfiguration.class);

        if (Constants.EMPTY.equals(localBridgeConfiguration.getUserName())
                || Constants.EMPTY.equals(localBridgeConfiguration.getPassword())) {
            logger.warn("username or password no set");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    MyBMWConstants.STATUS_USER_DETAILS_MISSING);
            return;
        }

        if (Constants.EMPTY.equals(localBridgeConfiguration.getRegion())) {
            logger.warn("region not set");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    MyBMWConstants.STATUS_REGION_MISSING);
            return;
        }

        Configuration config = super.editConfiguration();
        if (Constants.LANGUAGE_AUTODETECT.equals(localBridgeConfiguration.getLanguage())) {
            config.put("language", localeProvider.getLocale().getLanguage().toLowerCase());
        }
        String ipConfig = localBridgeConfiguration.getCallbackIP();
        if (Constants.EMPTY.equals(ipConfig) || NetUtil.getAllInterfaceAddresses().stream()
                .map(cidr -> cidr.getAddress().getHostAddress()).noneMatch(a -> ipConfig.equals(a))) {
            String ip = networkAddressService.getPrimaryIpv4HostAddress();
            if (ip != null) {
                config.put("callbackIP", ipConfig);
            } else {
                logger.warn("the callback IP address could not be retrieved");
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        MyBMWConstants.STATUS_IP_MISSING);
                return;
            }
        }
        // Update the central configuration and get the updates configuration back
        super.updateConfiguration(config);
        localBridgeConfiguration = getConfigAs(MyBMWBridgeConfiguration.class);

        // there is no risk in this functionality as several steps have to happen to get the file proxy working:
        // 1. environment variable ENVIRONMENT has to be available
        // 2. username of the myBMW account must be set to "testuser" which is anyhow no valid username
        // 3. the jar file must contain the fingerprints which will only happen if it has been built with the
        // test-jar profile
        String environment = System.getenv(ENVIRONMENT);

        if (environment == null) {
            environment = "";
        }

        createMyBmwProxy(localBridgeConfiguration, environment);
        initializerJob = Optional.of(scheduler.schedule(this::discoverVehicles, 2, TimeUnit.SECONDS));
        isInitialized.complete(true);
    }

    private void createMyBmwProxy(MyBMWBridgeConfiguration config, String environment) {
        if (!myBmwProxy.isPresent()) {
            if (!(TEST.equals(environment) && TESTUSER.equals(config.getUserName()))) {
                myBmwProxy = Optional.of(new MyBMWHttpProxy(this, httpClient, oAuthFactory, config));
            } else {
                myBmwProxy = Optional.of(new MyBMWFileProxy(httpClient, config));
            }
            logger.trace("MyBMWBridgeHandler proxy set");
        } else {
            myBmwProxy.get().setBridgeConfiguration(config);
            logger.trace("MyBMWBridgeHandler update proxy with bridge configuration");
        }
    }

    @Override
    public void dispose() {
        logger.trace("MyBMWBridgeHandler.dispose");
        initializerJob.ifPresent(job -> job.cancel(true));
        authServlet.ifPresent(servlet -> servlet.dispose());
        authServlet = Optional.empty();
        isInitialized.cancel(true);
    }

    public void vehicleDiscoveryError(String message) {
        logger.trace("MyBMWBridgeHandler.vehicleDiscoveryError");
        if (!tokenInitError) {
            String errorMessage = message.isEmpty() ? MyBMWConstants.STATUS_VEHICLE_RETRIEVAL_ERROR : message;
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, errorMessage);
        }
    }

    public void vehicleQuotaDiscoveryError(Instant nextQuota) {
        logger.trace("MyBMWBridgeHandler.vehicleQuotaDiscoveryError");
        if (!tokenInitError) {
            String timeString = DateTimeFormatter.ofPattern("HH:mm:ss")
                    .format(LocalDateTime.ofInstant(nextQuota, ZoneId.systemDefault()));
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    (MyBMWConstants.STATUS_QUOTA_ERROR + " [%s]").formatted(timeString));
        }
    }

    public void vehicleDiscoverySuccess() {
        logger.trace("MyBMWBridgeHandler.vehicleDiscoverySuccess");
        updateStatus(ThingStatus.ONLINE);
    }

    private void discoverVehicles() {
        logger.trace("MyBMWBridgeHandler.requestVehicles");

        vehicleDiscovery.ifPresent(discovery -> discovery.discoverVehicles());
    }

    public void tokenInitError() {
        Configuration config = super.editConfiguration();
        config.remove("hcaptchatoken");
        super.updateConfiguration(config);

        authServlet.ifPresent(servlet -> servlet.dispose());
        MyBMWAuthServlet servlet = new MyBMWAuthServlet(this, getConfigAs(MyBMWBridgeConfiguration.class).getRegion(),
                httpService);
        servlet.startListening();
        this.authServlet = Optional.of(servlet);

        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                (MyBMWConstants.STATUS_AUTH_NEEDED + " [ \"http(s)://<YOUROPENHAB>:<YOURPORT>%s\" ]")
                        .formatted(servlet.getPath()));
        tokenInitError = true;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        logger.trace("MyBMWBridgeHandler.getServices");
        return List.of(VehicleDiscovery.class);
    }

    public Optional<MyBMWProxy> getMyBmwProxy() {
        // wait for initialization to complete
        try {
            isInitialized.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.debug("exception waiting for bridge initialization: {}", e.toString());
        }
        return myBmwProxy;
    }

    public void setHCaptchaToken(String hCaptchaToken) {
        Configuration config = super.editConfiguration();
        config.put("hcaptchatoken", hCaptchaToken);
        super.updateConfiguration(config);

        if (!hCaptchaToken.isEmpty()) {
            initializerJob.ifPresent(job -> job.cancel(true));
            authServlet.ifPresent(servlet -> servlet.dispose());
            authServlet = Optional.empty();
            initialize();
        }
    }
}
