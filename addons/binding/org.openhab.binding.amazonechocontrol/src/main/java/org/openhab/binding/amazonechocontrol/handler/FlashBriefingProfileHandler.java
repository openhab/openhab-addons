/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.handler;

import static org.openhab.binding.amazonechocontrol.AmazonEchoControlBindingConstants.*;

import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.amazonechocontrol.internal.StateStorage;
import org.openhab.binding.amazonechocontrol.internal.discovery.AmazonEchoDiscovery;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FlashBriefingProfileHandler} is responsible for storing and loading of a flash briefing configuration
 *
 * @author Michael Geramb - Initial contribution
 */
public class FlashBriefingProfileHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FlashBriefingProfileHandler.class);
    private static HashMap<ThingUID, FlashBriefingProfileHandler> instances = new HashMap<ThingUID, FlashBriefingProfileHandler>();

    AccountHandler handler;
    StateStorage stateStorage;
    boolean updatePlayOnDevice = true;
    String currentConfigurationJson = "";
    private @Nullable ScheduledFuture<?> updateStateJob;

    public FlashBriefingProfileHandler(Thing thing) {
        super(thing);
        stateStorage = new StateStorage(thing);
    }

    public AccountHandler findAccountHandler() {
        return this.handler;
    }

    public static @Nullable FlashBriefingProfileHandler find(ThingUID uid) {
        synchronized (instances) {
            return instances.get(uid);
        }
    }

    public static boolean exist(String profileJson) {
        synchronized (instances) {
            for (FlashBriefingProfileHandler handler : instances.values()) {
                if (handler.currentConfigurationJson.equals(profileJson)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void initialize() {
        updatePlayOnDevice = true;
        logger.info("{} initialized", getClass().getSimpleName());
        synchronized (instances) {
            instances.put(this.getThing().getUID(), this);
        }
        if (this.currentConfigurationJson != null && !this.currentConfigurationJson.isEmpty()) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void dispose() {
        synchronized (instances) {
            instances.remove(getThing().getUID(), this);
        }
        ScheduledFuture<?> updateStateJob = this.updateStateJob;
        this.updateStateJob = null;
        if (updateStateJob != null) {
            updateStateJob.cancel(false);
        }
        removeFromDiscovery();
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        AccountHandler temp = this.handler;
        if (temp == null) {
            return;
        }
        int waitForUpdate = -1;

        ScheduledFuture<?> updateStateJob = this.updateStateJob;
        this.updateStateJob = null;
        if (updateStateJob != null) {
            updateStateJob.cancel(false);
        }

        try {
            String channelId = channelUID.getId();
            if (command instanceof RefreshType) {
                waitForUpdate = 0;
            }

            if (channelId.equals(CHANNEL_SAVE)) {
                if (command.equals(OnOffType.ON)) {
                    saveCurrentProfile(temp);
                    waitForUpdate = 500;
                }
            }
            if (channelId.equals(CHANNEL_ACTIVE)) {
                if (command.equals(OnOffType.ON)) {
                    String currentConfigurationJson = this.currentConfigurationJson;
                    if (currentConfigurationJson != null && !currentConfigurationJson.isEmpty()) {
                        temp.setEnabledFlashBriefingsJson(currentConfigurationJson);

                        updateState(CHANNEL_ACTIVE, OnOffType.ON);
                        waitForUpdate = 500;
                    }
                }
            }
            if (channelId.equals(CHANNEL_PLAY_ON_DEVICE)) {
                if (command instanceof StringType) {
                    String deviceSerialOrName = ((StringType) command).toFullString();
                    String currentConfigurationJson = this.currentConfigurationJson;
                    if (currentConfigurationJson != null && !currentConfigurationJson.isEmpty()) {

                        String old = temp.getEnabledFlashBriefingsJson();
                        temp.setEnabledFlashBriefingsJson(currentConfigurationJson);

                        Device device = temp.findDeviceJsonBySerialOrName(deviceSerialOrName);
                        if (device == null) {
                            logger.warn("Device '{}' not found", deviceSerialOrName);
                        } else {
                            temp.findConnection().executeSequenceCommand(device, "Alexa.FlashBriefing.Play");

                            scheduler.schedule(() -> temp.setEnabledFlashBriefingsJson(old), 1000,
                                    TimeUnit.MILLISECONDS);

                            updateState(CHANNEL_ACTIVE, OnOffType.ON);
                        }
                        updatePlayOnDevice = true;
                        waitForUpdate = 1000;

                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Handle command failed {}", e);
        }
        if (waitForUpdate >= 0) {
            this.updateStateJob = scheduler.schedule(() -> temp.updateFlashBriefingHandlers(), waitForUpdate,
                    TimeUnit.MILLISECONDS);
        }
    }

    public void initialize(AccountHandler handler, String currentConfigurationJson) {

        updateState(CHANNEL_SAVE, OnOffType.OFF);
        if (updatePlayOnDevice) {
            updateState(CHANNEL_PLAY_ON_DEVICE, new StringType(""));
        }
        if (this.handler != handler) {

            this.handler = handler;
            String configurationJson = this.stateStorage.findState("configurationJson");
            if (configurationJson == null || configurationJson.isEmpty()) {
                this.currentConfigurationJson = saveCurrentProfile(handler);

            } else {
                removeFromDiscovery();
                this.currentConfigurationJson = configurationJson;
            }
            if (this.currentConfigurationJson != null && !this.currentConfigurationJson.isEmpty()) {
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

    }

    private String saveCurrentProfile(AccountHandler connection) {
        String configurationJson = "";
        try {
            configurationJson = connection.getEnabledFlashBriefingsJson();
            removeFromDiscovery();
            this.currentConfigurationJson = configurationJson;
        } catch (Exception e) {
            logger.warn("get flash briefing configuration failed {}", e);
        }
        if (!configurationJson.isEmpty()) {
            this.stateStorage.storeState("configurationJson", configurationJson);
        }
        return configurationJson;
    }

    private void removeFromDiscovery() {
        AmazonEchoDiscovery instance = AmazonEchoDiscovery.instance;
        if (instance != null) {
            instance.removeExistingFlashBriefingProfile(this.currentConfigurationJson);
        }
    }
}
