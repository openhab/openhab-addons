/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mybmw.internal.MyBMWBridgeConfiguration;
import org.openhab.binding.mybmw.internal.discovery.VehicleDiscovery;
import org.openhab.binding.mybmw.internal.handler.backend.MyBMWFileProxy;
import org.openhab.binding.mybmw.internal.handler.backend.MyBMWHttpProxy;
import org.openhab.binding.mybmw.internal.handler.backend.MyBMWProxy;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.MyBMWConfigurationChecker;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
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

    private HttpClientFactory httpClientFactory;
    private Optional<MyBMWProxy> myBmwProxy = Optional.empty();
    private Optional<ScheduledFuture<?>> initializerJob = Optional.empty();
    private Optional<VehicleDiscovery> vehicleDiscovery = Optional.empty();
    private LocaleProvider localeProvider;

    public MyBMWBridgeHandler(Bridge bridge, HttpClientFactory hcf, LocaleProvider localeProvider) {
        super(bridge);
        httpClientFactory = hcf;
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
        logger.trace("MyBMWBridgeHandler.initialize");
        updateStatus(ThingStatus.UNKNOWN);
        MyBMWBridgeConfiguration config = getConfigAs(MyBMWBridgeConfiguration.class);
        if (config.language.equals(Constants.LANGUAGE_AUTODETECT)) {
            config.language = localeProvider.getLocale().getLanguage().toLowerCase();
        }
        if (!MyBMWConfigurationChecker.checkConfiguration(config)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            // there is no risk in this functionality as several steps have to happen to get the file proxy working:
            // 1. environment variable ENVIRONMENT has to be available
            // 2. username of the myBMW account must be set to "testuser" which is anyhow no valid username
            // 3. the jar file must contain the fingerprints which will only happen if it has been built with the
            // test-jar profile
            String environment = System.getenv(ENVIRONMENT);

            if (environment == null) {
                environment = "";
            }

            createMyBmwProxy(config, environment);
            initializerJob = Optional.of(scheduler.schedule(this::discoverVehicles, 2, TimeUnit.SECONDS));
        }
    }

    private synchronized void createMyBmwProxy(MyBMWBridgeConfiguration config, String environment) {
        if (!myBmwProxy.isPresent()) {
            if (!(TEST.equals(environment) && TESTUSER.equals(config.userName))) {
                myBmwProxy = Optional.of(new MyBMWHttpProxy(httpClientFactory, config));
            } else {
                myBmwProxy = Optional.of(new MyBMWFileProxy(httpClientFactory, config));
            }
            logger.trace("MyBMWBridgeHandler proxy set");
        }
    }

    @Override
    public void dispose() {
        logger.trace("MyBMWBridgeHandler.dispose");
        initializerJob.ifPresent(job -> job.cancel(true));
    }

    public void vehicleDiscoveryError() {
        logger.trace("MyBMWBridgeHandler.vehicleDiscoveryError");
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Request vehicles failed");
    }

    public void vehicleDiscoverySuccess() {
        logger.trace("MyBMWBridgeHandler.vehicleDiscoverySuccess");
        updateStatus(ThingStatus.ONLINE);
    }

    private void discoverVehicles() {
        logger.trace("MyBMWBridgeHandler.requestVehicles");

        MyBMWBridgeConfiguration config = getConfigAs(MyBMWBridgeConfiguration.class);

        myBmwProxy.ifPresent(proxy -> proxy.setBridgeConfiguration(config));

        vehicleDiscovery.ifPresent(discovery -> discovery.discoverVehicles());
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        logger.trace("MyBMWBridgeHandler.getServices");
        return List.of(VehicleDiscovery.class);
    }

    public Optional<MyBMWProxy> getMyBmwProxy() {
        logger.trace("MyBMWBridgeHandler.getProxy");
        createMyBmwProxy(getConfigAs(MyBMWBridgeConfiguration.class), ENVIRONMENT);
        return myBmwProxy;
    }
}
