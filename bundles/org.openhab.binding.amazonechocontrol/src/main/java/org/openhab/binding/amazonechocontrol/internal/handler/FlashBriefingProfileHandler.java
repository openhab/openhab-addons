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
package org.openhab.binding.amazonechocontrol.internal.handler;

import static org.eclipse.jetty.util.StringUtil.isBlank;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.connection.Connection;
import org.openhab.binding.amazonechocontrol.internal.dto.DeviceTO;
import org.openhab.binding.amazonechocontrol.internal.dto.EnabledFeedTO;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * The {@link FlashBriefingProfileHandler} is responsible for storing and loading of a flash briefing configuration
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class FlashBriefingProfileHandler extends BaseThingHandler {
    @SuppressWarnings("unchecked")
    public static final TypeToken<List<EnabledFeedTO>> LIST_TYPE_TOKEN = (TypeToken<List<EnabledFeedTO>>) TypeToken
            .getParameterized(List.class, EnabledFeedTO.class);
    public static final String ENABLED_FEEDS = "enabledFeeds";

    private final Logger logger = LoggerFactory.getLogger(FlashBriefingProfileHandler.class);
    private final Gson gson;

    private @Nullable AccountHandler accountHandler;
    private final Storage<? super Object> stateStorage;
    private List<EnabledFeedTO> ourFeeds = List.of();

    public FlashBriefingProfileHandler(Thing thing, Storage<? super Object> storage, Gson gson) {
        super(thing);
        this.stateStorage = storage;
        this.gson = gson;
    }

    @Override
    public void initialize() {
        @SuppressWarnings("unchecked")
        List<EnabledFeedTO> restoredFeeds = (List<EnabledFeedTO>) stateStorage.get(ENABLED_FEEDS);
        if (restoredFeeds == null) {
            // convert from old
            String configurationJson = (String) stateStorage.get("configurationJson");
            if (!isBlank(configurationJson)) {
                restoredFeeds = Objects.requireNonNullElse(gson.fromJson(configurationJson, LIST_TYPE_TOKEN),
                        List.of());
                stateStorage.put("enabledFeeds", restoredFeeds);
            } else {
                restoredFeeds = List.of();
            }
        }

        if (restoredFeeds.isEmpty()) {
            saveCurrentProfile();
        } else {
            ourFeeds = restoredFeeds;
        }

        Bridge bridge = this.getBridge();
        if (bridge != null) {
            accountHandler = (AccountHandler) bridge.getHandler();
        }
        if (accountHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Bridge handler not found.");
            return;
        }

        AccountHandler handler = this.accountHandler;
        if (handler != null) {
            bridgeStatusChanged(handler.getThing().getStatusInfo());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatus) {
        if (bridgeStatus.getStatus() != ThingStatus.ONLINE) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }
        if (ourFeeds.isEmpty()) {
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Waiting for feed configuration");
        } else {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        AccountHandler accountHandler = this.accountHandler;
        if (accountHandler == null) {
            return;
        }

        String channelId = channelUID.getId();
        if (command instanceof RefreshType) {
            accountHandler.updateFlashBriefingHandlers();
        } else if (channelId.equals(CHANNEL_SAVE) && command.equals(OnOffType.ON)) {
            saveCurrentProfile();
            accountHandler.updateFlashBriefingHandlers();
        } else if (channelId.equals(CHANNEL_ACTIVE) && command.equals(OnOffType.ON)) {
            if (!ourFeeds.isEmpty()) {
                accountHandler.setEnabledFlashBriefing(ourFeeds);
            }
        } else if (channelId.equals(CHANNEL_PLAY_ON_DEVICE) && command instanceof StringType) {
            String deviceSerialOrName = command.toFullString();
            if (!ourFeeds.isEmpty()) {
                DeviceTO device = accountHandler.findDeviceBySerialOrName(deviceSerialOrName);
                if (device == null) {
                    logger.warn("Device '{}' not found", deviceSerialOrName);
                    return;
                }

                List<EnabledFeedTO> oldEnabledFeeds = accountHandler.getEnabledFlashBriefings();
                accountHandler.setEnabledFlashBriefing(ourFeeds);

                Connection connection = accountHandler.getConnection();
                if (!connection.isLoggedIn()) {
                    logger.warn("Can't execute command when account is logged out.");
                } else {
                    connection.executeSequenceCommand(device, "Alexa.FlashBriefing.Play", Map.of());
                    scheduler.schedule(() -> accountHandler.setEnabledFlashBriefing(oldEnabledFeeds), 1000,
                            TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    /**
     * Update the current profile's state and check if it is the same
     *
     * @param configurationJson a flash briefing profile configuration
     * @return {@code true} if the provided feed configuration is the same as this thing's configuration
     */
    public boolean updateAndCheck(List<EnabledFeedTO> configurationJson) {
        boolean isSame = ourFeeds.equals(configurationJson);
        updateState(CHANNEL_ACTIVE, OnOffType.from(isSame));
        return isSame;
    }

    private void saveCurrentProfile() {
        AccountHandler accountHandler = this.accountHandler;
        if (accountHandler == null) {
            ourFeeds = List.of();
        } else {
            List<EnabledFeedTO> newFeeds = accountHandler.getEnabledFlashBriefings();
            if (!newFeeds.isEmpty()) {
                ourFeeds = newFeeds;
                stateStorage.put(ENABLED_FEEDS, newFeeds);
            }
        }
    }
}
