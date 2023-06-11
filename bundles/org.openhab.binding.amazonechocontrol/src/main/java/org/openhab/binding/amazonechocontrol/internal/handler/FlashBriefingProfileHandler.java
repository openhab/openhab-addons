/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.handler;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.*;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FlashBriefingProfileHandler} is responsible for storing and loading of a flash briefing configuration
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class FlashBriefingProfileHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FlashBriefingProfileHandler.class);

    @Nullable
    AccountHandler accountHandler;
    Storage<String> stateStorage;
    boolean updatePlayOnDevice = true;
    String currentConfigurationJson = "";
    private @Nullable ScheduledFuture<?> updateStateJob;

    public FlashBriefingProfileHandler(Thing thing, Storage<String> storage) {
        super(thing);
        this.stateStorage = storage;
    }

    public @Nullable AccountHandler findAccountHandler() {
        return this.accountHandler;
    }

    @Override
    public void initialize() {
        updatePlayOnDevice = true;
        logger.info("{} initialized", getClass().getSimpleName());
        if (!this.currentConfigurationJson.isEmpty()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            Bridge bridge = this.getBridge();
            if (bridge != null) {
                AccountHandler account = (AccountHandler) bridge.getHandler();
                if (account != null) {
                    account.addFlashBriefingProfileHandler(this);
                }
            }
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> updateStateJob = this.updateStateJob;
        this.updateStateJob = null;
        if (updateStateJob != null) {
            updateStateJob.cancel(false);
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        AccountHandler accountHandler = this.accountHandler;
        if (accountHandler == null) {
            return;
        }
        int waitForUpdate = -1;

        ScheduledFuture<?> updateStateJob = this.updateStateJob;
        this.updateStateJob = null;
        if (updateStateJob != null) {
            updateStateJob.cancel(false);
        }
        String channelId = channelUID.getId();
        if (command instanceof RefreshType) {
            waitForUpdate = 0;
        }
        if (channelId.equals(CHANNEL_SAVE)) {
            if (command.equals(OnOffType.ON)) {
                saveCurrentProfile(accountHandler);
                waitForUpdate = 500;
            }
        }
        if (channelId.equals(CHANNEL_ACTIVE)) {
            if (command.equals(OnOffType.ON)) {
                String currentConfigurationJson = this.currentConfigurationJson;
                if (!currentConfigurationJson.isEmpty()) {
                    accountHandler.setEnabledFlashBriefingsJson(currentConfigurationJson);
                    updateState(CHANNEL_ACTIVE, OnOffType.ON);
                    waitForUpdate = 500;
                }
            }
        }
        if (channelId.equals(CHANNEL_PLAY_ON_DEVICE)) {
            if (command instanceof StringType) {
                String deviceSerialOrName = command.toFullString();
                String currentConfigurationJson = this.currentConfigurationJson;
                if (!currentConfigurationJson.isEmpty()) {
                    String old = accountHandler.getEnabledFlashBriefingsJson();
                    accountHandler.setEnabledFlashBriefingsJson(currentConfigurationJson);
                    Device device = accountHandler.findDeviceJsonBySerialOrName(deviceSerialOrName);
                    if (device == null) {
                        logger.warn("Device '{}' not found", deviceSerialOrName);
                    } else {
                        @Nullable
                        Connection connection = accountHandler.findConnection();
                        if (connection == null) {
                            logger.warn("Connection for '{}' not found", accountHandler.getThing().getUID().getId());
                        } else {
                            connection.executeSequenceCommand(device, "Alexa.FlashBriefing.Play", Map.of());

                            scheduler.schedule(() -> accountHandler.setEnabledFlashBriefingsJson(old), 1000,
                                    TimeUnit.MILLISECONDS);

                            updateState(CHANNEL_ACTIVE, OnOffType.ON);
                        }
                    }
                    updatePlayOnDevice = true;
                    waitForUpdate = 1000;
                }
            }
        }
        if (waitForUpdate >= 0) {
            this.updateStateJob = scheduler.schedule(() -> accountHandler.updateFlashBriefingHandlers(), waitForUpdate,
                    TimeUnit.MILLISECONDS);
        }
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public boolean initialize(AccountHandler handler, String currentConfigurationJson) {
        updateState(CHANNEL_SAVE, OnOffType.OFF);
        if (updatePlayOnDevice) {
            updateState(CHANNEL_PLAY_ON_DEVICE, new StringType(""));
        }
        if (this.accountHandler != handler) {
            this.accountHandler = handler;
            String configurationJson = this.stateStorage.get("configurationJson");
            if (configurationJson == null || configurationJson.isEmpty()) {
                this.currentConfigurationJson = saveCurrentProfile(handler);
            } else {
                this.currentConfigurationJson = configurationJson;
            }
            if (!this.currentConfigurationJson.isEmpty()) {
                updateStatus(ThingStatus.ONLINE);

            } else {
                updateStatus(ThingStatus.UNKNOWN);
            }
        }
        if (this.currentConfigurationJson.equals(currentConfigurationJson)) {
            updateState(CHANNEL_ACTIVE, OnOffType.ON);
        } else {
            updateState(CHANNEL_ACTIVE, OnOffType.OFF);
        }
        return this.currentConfigurationJson.equals(currentConfigurationJson);
    }

    private String saveCurrentProfile(AccountHandler connection) {
        String configurationJson = "";
        configurationJson = connection.getEnabledFlashBriefingsJson();
        this.currentConfigurationJson = configurationJson;
        if (!configurationJson.isEmpty()) {
            this.stateStorage.put("configurationJson", configurationJson);
        }
        return configurationJson;
    }
}
