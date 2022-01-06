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
package org.openhab.binding.bmwconnecteddrive.internal.handler;

import static org.openhab.binding.bmwconnecteddrive.internal.utils.Constants.ANONYMOUS;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.discovery.VehicleDiscovery;
import org.openhab.binding.bmwconnecteddrive.internal.dto.NetworkError;
import org.openhab.binding.bmwconnecteddrive.internal.dto.discovery.Dealer;
import org.openhab.binding.bmwconnecteddrive.internal.dto.discovery.VehiclesContainer;
import org.openhab.binding.bmwconnecteddrive.internal.utils.BimmerConstants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Constants;
import org.openhab.binding.bmwconnecteddrive.internal.utils.Converter;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonParseException;

/**
 * The {@link ConnectedDriveBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ConnectedDriveBridgeHandler extends BaseBridgeHandler implements StringResponseCallback {
    private final Logger logger = LoggerFactory.getLogger(ConnectedDriveBridgeHandler.class);
    private HttpClientFactory httpClientFactory;
    private Optional<VehicleDiscovery> discoveryService = Optional.empty();
    private Optional<ConnectedDriveProxy> proxy = Optional.empty();
    private Optional<ScheduledFuture<?>> initializerJob = Optional.empty();
    private Optional<String> troubleshootFingerprint = Optional.empty();

    public ConnectedDriveBridgeHandler(Bridge bridge, HttpClientFactory hcf) {
        super(bridge);
        httpClientFactory = hcf;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // no commands available
    }

    @Override
    public void initialize() {
        troubleshootFingerprint = Optional.empty();
        updateStatus(ThingStatus.UNKNOWN);
        ConnectedDriveConfiguration config = getConfigAs(ConnectedDriveConfiguration.class);
        logger.debug("Prefer MyBMW API {}", config.preferMyBmw);
        if (!checkConfiguration(config)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        } else {
            proxy = Optional.of(new ConnectedDriveProxy(httpClientFactory, config));
            // give the system some time to create all predefined Vehicles
            // check with API call if bridge is online
            initializerJob = Optional.of(scheduler.schedule(this::requestVehicles, 2, TimeUnit.SECONDS));
            Bridge b = super.getThing();
            List<Thing> children = b.getThings();
            logger.debug("Update {} things", children.size());
            children.forEach(entry -> {
                ThingHandler th = entry.getHandler();
                if (th != null) {
                    th.dispose();
                    th.initialize();
                } else {
                    logger.debug("Handler is null");
                }
            });
        }
    }

    public static boolean checkConfiguration(ConnectedDriveConfiguration config) {
        if (Constants.EMPTY.equals(config.userName) || Constants.EMPTY.equals(config.password)) {
            return false;
        } else {
            return BimmerConstants.AUTH_SERVER_MAP.containsKey(config.region);
        }
    }

    @Override
    public void dispose() {
        initializerJob.ifPresent(job -> job.cancel(true));
    }

    public void requestVehicles() {
        proxy.ifPresent(prox -> prox.requestVehicles(this));
    }

    // https://www.bmw-connecteddrive.de/api/me/vehicles/v2?all=true&brand=BM
    public String getDiscoveryFingerprint() {
        return troubleshootFingerprint.map(fingerprint -> {
            VehiclesContainer container = null;
            try {
                container = Converter.getGson().fromJson(fingerprint, VehiclesContainer.class);
                if (container != null) {
                    if (container.vehicles != null) {
                        if (container.vehicles.isEmpty()) {
                            return Constants.EMPTY_JSON;
                        } else {
                            container.vehicles.forEach(entry -> {
                                entry.vin = ANONYMOUS;
                                entry.breakdownNumber = ANONYMOUS;
                                if (entry.dealer != null) {
                                    Dealer d = entry.dealer;
                                    d.city = ANONYMOUS;
                                    d.country = ANONYMOUS;
                                    d.name = ANONYMOUS;
                                    d.phone = ANONYMOUS;
                                    d.postalCode = ANONYMOUS;
                                    d.street = ANONYMOUS;
                                }
                            });
                            return Converter.getGson().toJson(container);
                        }
                    } else {
                        logger.debug("container.vehicles is null");
                    }
                }
            } catch (JsonParseException jpe) {
                logger.debug("Cannot parse fingerprint {}", jpe.getMessage());
            }
            // Not a VehiclesContainer or Vehicles is empty so deliver fingerprint as it is
            return fingerprint;
        }).orElse(Constants.INVALID);
    }

    private void logFingerPrint() {
        logger.debug("###### Discovery Troubleshoot Fingerprint Data - BEGIN ######");
        logger.debug("### Discovery Result ###");
        logger.debug("{}", getDiscoveryFingerprint());
        logger.debug("###### Discovery Troubleshoot Fingerprint Data - END ######");
    }

    /**
     * There's only the Vehicles response available
     */
    @Override
    public void onResponse(@Nullable String response) {
        boolean firstResponse = troubleshootFingerprint.isEmpty();
        if (response != null) {
            updateStatus(ThingStatus.ONLINE);
            troubleshootFingerprint = discoveryService.map(discovery -> {
                try {
                    VehiclesContainer container = Converter.getGson().fromJson(response, VehiclesContainer.class);
                    if (container != null) {
                        if (container.vehicles != null) {
                            discovery.onResponse(container);
                            container.vehicles.forEach(entry -> {
                                entry.vin = ANONYMOUS;
                                entry.breakdownNumber = ANONYMOUS;
                                if (entry.dealer != null) {
                                    Dealer d = entry.dealer;
                                    d.city = ANONYMOUS;
                                    d.country = ANONYMOUS;
                                    d.name = ANONYMOUS;
                                    d.phone = ANONYMOUS;
                                    d.postalCode = ANONYMOUS;
                                    d.street = ANONYMOUS;
                                }
                            });
                        }
                    } else {
                        troubleshootFingerprint = Optional.of(Constants.EMPTY_JSON);
                    }
                } catch (JsonParseException jpe) {
                    logger.debug("Fingerprint parse exception {}", jpe.getMessage());
                }
                // Unparseable or not a VehiclesContainer:
                return response;
            });
        } else {
            troubleshootFingerprint = Optional.of(Constants.EMPTY_JSON);
        }
        if (firstResponse) {
            logFingerPrint();
        }
    }

    @Override
    public void onError(NetworkError error) {
        boolean firstResponse = troubleshootFingerprint.isEmpty();
        troubleshootFingerprint = Optional.of(error.toJson());
        if (firstResponse) {
            logFingerPrint();
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error.reason);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Collections.singleton(VehicleDiscovery.class);
    }

    public Optional<ConnectedDriveProxy> getProxy() {
        return proxy;
    }

    public void setDiscoveryService(VehicleDiscovery discoveryService) {
        this.discoveryService = Optional.of(discoveryService);
    }
}
