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
package org.openhab.binding.meross.internal.handler;

import java.io.File;
import java.net.ConnectException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.api.MerossEnum;
import org.openhab.binding.meross.internal.api.MerossHttpConnector;
import org.openhab.binding.meross.internal.config.MerossBridgeConfiguration;
import org.openhab.binding.meross.internal.discovery.MerossDiscoveryService;
import org.openhab.binding.meross.internal.dto.HttpConnectorBuilder;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MerossBridgeHandler} is responsible for handling http communication with and retrieve data from Meross
 * Host.
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class MerossBridgeHandler extends BaseBridgeHandler {
    private final Logger logger = LoggerFactory.getLogger(MerossBridgeHandler.class);
    private static final String CREDENTIAL_FILE_NAME = "meross" + File.separator + "meross_credentials.json";
    private static final String DEVICE_FILE_NAME = "meross" + File.separator + "meross_devices.json";
    private @Nullable MerossBridgeConfiguration config;
    public static @Nullable MerossHttpConnector connector;
    public static final File credentialfile = new File(
            OpenHAB.getUserDataFolder() + File.separator + CREDENTIAL_FILE_NAME);
    public static final File deviceFile = new File(OpenHAB.getUserDataFolder() + File.separator + DEVICE_FILE_NAME);

    public MerossBridgeHandler(Thing thing) {
        super((Bridge) thing);
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(MerossDiscoveryService.class);
    }

    @Override
    public void initialize() {
        config = getConfigAs(MerossBridgeConfiguration.class);
        connector = HttpConnectorBuilder.newBuilder().setApiBaseUrl(config.hostName).setUserEmail(config.userEmail)
                .setUserPassword(config.userPassword).setCredentialFile(credentialfile).setDeviceFile(deviceFile)
                .build();
        if (connector == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }
        try {
            int httpStatusCode = connector.login().statusCode();
            connector.logout();
            int apiStatusCode = connector.apiStatus();
            connector.logout();
            String apiMessage = MerossEnum.ApiStatusCode.getMessageByApiStatusCode(apiStatusCode);
            if (httpStatusCode != 200) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            } else if (apiStatusCode != MerossEnum.ApiStatusCode.OK.value()) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, apiMessage);
            } else {
                fetchDataAsync();
                connector.logout();
                updateStatus(ThingStatus.ONLINE);
            }
        } catch (ConnectException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Couldn't connect to bridge");
        }
    }

    private void fetchDataAsync() {
        CompletableFuture.runAsync(() -> {
            if (connector != null) {
                connector.fetchCredentialsAndWrite(credentialfile);
            }
        }).thenRunAsync(() -> {
            if (connector != null) {
                connector.fetchDevicesAndWrite(deviceFile);
            }
        }).exceptionally(e -> {
            logger.warn("Cannot fetch data {}", e.getMessage());
            return null;
        }).join();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }
}
