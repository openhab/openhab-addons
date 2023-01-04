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
package org.openhab.binding.mybmw.internal.handler;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mybmw.internal.MyBMWConfiguration;
import org.openhab.binding.mybmw.internal.discovery.VehicleDiscovery;
import org.openhab.binding.mybmw.internal.dto.network.NetworkError;
import org.openhab.binding.mybmw.internal.dto.vehicle.Vehicle;
import org.openhab.binding.mybmw.internal.utils.BimmerConstants;
import org.openhab.binding.mybmw.internal.utils.Constants;
import org.openhab.binding.mybmw.internal.utils.Converter;
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
 * The {@link MyBMWBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class MyBMWBridgeHandler extends BaseBridgeHandler implements StringResponseCallback {
    private final Logger logger = LoggerFactory.getLogger(MyBMWBridgeHandler.class);
    private HttpClientFactory httpClientFactory;
    private Optional<VehicleDiscovery> discoveryService = Optional.empty();
    private Optional<MyBMWProxy> proxy = Optional.empty();
    private Optional<ScheduledFuture<?>> initializerJob = Optional.empty();
    private Optional<String> troubleshootFingerprint = Optional.empty();
    private String localeLanguage;

    public MyBMWBridgeHandler(Bridge bridge, HttpClientFactory hcf, String language) {
        super(bridge);
        httpClientFactory = hcf;
        localeLanguage = language;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands available
    }

    @Override
    public void initialize() {
        troubleshootFingerprint = Optional.empty();
        updateStatus(ThingStatus.UNKNOWN);
        MyBMWConfiguration config = getConfigAs(MyBMWConfiguration.class);
        if (config.language.equals(Constants.LANGUAGE_AUTODETECT)) {
            config.language = localeLanguage;
        }
        if (!checkConfiguration(config)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            proxy = Optional.of(new MyBMWProxy(httpClientFactory, config));
            initializerJob = Optional.of(scheduler.schedule(this::requestVehicles, 2, TimeUnit.SECONDS));
        }
    }

    public static boolean checkConfiguration(MyBMWConfiguration config) {
        if (Constants.EMPTY.equals(config.userName) || Constants.EMPTY.equals(config.password)) {
            return false;
        } else {
            return BimmerConstants.EADRAX_SERVER_MAP.containsKey(config.region);
        }
    }

    @Override
    public void dispose() {
        initializerJob.ifPresent(job -> job.cancel(true));
    }

    public void requestVehicles() {
        proxy.ifPresent(prox -> prox.requestVehicles(this));
    }

    private void logFingerPrint() {
        logger.debug("###### Discovery Fingerprint Data - BEGIN ######");
        logger.debug("{}", troubleshootFingerprint.get());
        logger.debug("###### Discovery Fingerprint Data - END ######");
    }

    /**
     * Response for vehicle request
     */
    @Override
    public synchronized void onResponse(@Nullable String response) {
        if (response != null) {
            updateStatus(ThingStatus.ONLINE);
            List<Vehicle> vehicleList = Converter.getVehicleList(response);
            discoveryService.get().onResponse(vehicleList);
            troubleshootFingerprint = Optional.of(Converter.anonymousFingerprint(response));
            logFingerPrint();
        }
    }

    @Override
    public void onError(NetworkError error) {
        troubleshootFingerprint = Optional.of(error.toJson());
        logFingerPrint();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error.reason);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(VehicleDiscovery.class);
    }

    public Optional<MyBMWProxy> getProxy() {
        return proxy;
    }

    public void setDiscoveryService(VehicleDiscovery discoveryService) {
        this.discoveryService = Optional.of(discoveryService);
    }
}
