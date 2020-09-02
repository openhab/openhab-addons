/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConstants.DISCOVERY_FINGERPRINT;
import static org.openhab.binding.bmwconnecteddrive.internal.utils.Constants.ANONYMOUS;

import java.util.Hashtable;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.bmwconnecteddrive.internal.ConnectedDriveConfiguration;
import org.openhab.binding.bmwconnecteddrive.internal.discovery.ConnectedCarDiscovery;
import org.openhab.binding.bmwconnecteddrive.internal.dto.NetworkError;
import org.openhab.binding.bmwconnecteddrive.internal.dto.discovery.Dealer;
import org.openhab.binding.bmwconnecteddrive.internal.dto.discovery.VehiclesContainer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link ConnectedDriveBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class ConnectedDriveBridgeHandler extends BaseBridgeHandler implements StringResponseCallback {
    private final Logger logger = LoggerFactory.getLogger(ConnectedDriveBridgeHandler.class);
    private static final Gson GSON = new Gson();
    private HttpClient httpClient;
    private BundleContext bundleContext;
    private ConnectedCarDiscovery discoveryService;
    private ServiceRegistration<?> discoveryServiceRegstration;
    private Optional<ConnectedDriveProxy> proxy = Optional.empty();
    private Optional<ConnectedDriveConfiguration> configuration = Optional.empty();
    private Optional<ScheduledFuture<?>> refreshJob = Optional.empty();
    private Optional<String> troubleshootFingerprint = Optional.empty();

    private ChannelUID discoveryfingerPrint;

    public ConnectedDriveBridgeHandler(Bridge bridge, HttpClient hc, BundleContext bc) {
        super(bridge);
        httpClient = hc;
        bundleContext = bc;
        discoveryService = new ConnectedCarDiscovery(this);
        discoveryServiceRegstration = bundleContext.registerService(DiscoveryService.class.getName(), discoveryService,
                new Hashtable<>());
        discoveryfingerPrint = new ChannelUID(thing.getUID(), DISCOVERY_FINGERPRINT);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType) {
            if (command.equals(OnOffType.ON)) {
                if (channelUID.getIdWithoutGroup().equals(DISCOVERY_FINGERPRINT)) {
                    if (troubleshootFingerprint.isPresent()) {
                        logger.warn("BMW ConnectedDrive Binding - Discovery Troubleshoot fingerprint - BEGIN");
                        logger.warn("{}", troubleshootFingerprint.get());
                        logger.warn("BMW ConnectedDrive Binding - Discovery Troubleshoot fingerprint - END");
                    } else {
                        logger.warn(
                                "BMW ConnectedDrive Binding - No Discovery Troubleshoot fingerprint available. Please check for valid username and password Settings for proper connection towards ConnectDrive");
                    }
                    // Switch back to off immediately
                    updateState(discoveryfingerPrint, OnOffType.OFF);
                }
            }
        }
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        configuration = Optional.of(getConfigAs(ConnectedDriveConfiguration.class));
        if (configuration.isPresent()) {
            proxy = Optional.of(new ConnectedDriveProxy(httpClient, configuration.get()));
            // give the system some time to create all predefined Vehicles
            scheduler.schedule(this::requestVehicles, 5, TimeUnit.SECONDS);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        }
        updateState(discoveryfingerPrint, OnOffType.OFF);
    }

    @Override
    public void dispose() {
        if (refreshJob.isPresent()) {
            refreshJob.get().cancel(true);
        }
    }

    public void requestVehicles() {
        if (proxy.isPresent()) {
            proxy.get().requestVehicles(this);
        }
    }

    /**
     * There's only the Vehicles response available
     */
    @Override
    public void onResponse(Optional<String> response) {
        if (response.isPresent()) {
            VehiclesContainer container = GSON.fromJson(response.get(), VehiclesContainer.class);
            discoveryService.onResponse(container);
            updateStatus(ThingStatus.ONLINE);
            if (container.vehicles != null) {
                if (container.vehicles.isEmpty()) {
                    troubleshootFingerprint = Optional.of("No Cars found in your ConnectedDrive Account");
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
                    troubleshootFingerprint = Optional.of(GSON.toJson(container));
                }
            }
        } else {
            logger.info("No Vehciles found");
        }
    }

    @Override
    public void onError(NetworkError error) {
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error.reason);
    }

    public Optional<ConnectedDriveProxy> getProxy() {
        return proxy;
    }

    public void close() {
        discoveryServiceRegstration.unregister();
    }
}
