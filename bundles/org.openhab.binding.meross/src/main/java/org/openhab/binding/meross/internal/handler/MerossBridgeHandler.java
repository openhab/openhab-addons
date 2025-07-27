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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.meross.internal.api.MerossHttpConnector;
import org.openhab.binding.meross.internal.config.MerossBridgeConfiguration;
import org.openhab.binding.meross.internal.discovery.MerossDiscoveryService;
import org.openhab.binding.meross.internal.dto.HttpConnectorBuilder;
import org.openhab.binding.meross.internal.exception.MerossApiException;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;

/**
 * The {@link MerossBridgeHandler} is responsible for handling http communication with and retrieve data from Meross
 * Host.
 *
 * @author Giovanni Fabiani - Initial contribution
 */
@NonNullByDefault
public class MerossBridgeHandler extends BaseBridgeHandler {
    private MerossBridgeConfiguration config = new MerossBridgeConfiguration();
    private @Nullable MerossHttpConnector merossHttpConnector;
    private static final String CREDENTIAL_FILE_NAME = "meross" + File.separator + "meross_credentials.json";
    private static final String DEVICE_FILE_NAME = "meross" + File.separator + "meross_devices.json";
    public static final File CREDENTIALFILE = new File(
            OpenHAB.getUserDataFolder() + File.separator + CREDENTIAL_FILE_NAME);
    public static final File DEVICE_FILE = new File(OpenHAB.getUserDataFolder() + File.separator + DEVICE_FILE_NAME);

    public MerossBridgeHandler(Thing thing) {
        super((Bridge) thing);
    }

    @Override
    public void initialize() {
        config = getConfigAs(MerossBridgeConfiguration.class);

        if (config.hostName.isBlank() || config.userEmail.isBlank() || config.userPassword.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }

        MerossHttpConnector merossHttpConnectorLocal = merossHttpConnector = HttpConnectorBuilder.newBuilder()
                .setApiBaseUrl(config.hostName).setUserEmail(config.userEmail).setUserPassword(config.userPassword)
                .setCredentialFile(CREDENTIALFILE).setDeviceFile(DEVICE_FILE).build();

        if (merossHttpConnectorLocal == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }
        try {
            merossHttpConnectorLocal.fetchDataAsync();
            updateStatus(ThingStatus.ONLINE);
        } catch (ConnectException | MerossApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(MerossDiscoveryService.class);
    }

    public @Nullable MerossHttpConnector getMerossHttpConnector() {
        return merossHttpConnector;
    }
}
