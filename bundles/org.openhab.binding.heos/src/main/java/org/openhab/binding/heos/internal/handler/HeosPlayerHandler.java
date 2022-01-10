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
package org.openhab.binding.heos.internal.handler;

import static org.openhab.binding.heos.internal.HeosBindingConstants.*;
import static org.openhab.binding.heos.internal.handler.FutureUtil.cancel;
import static org.openhab.binding.heos.internal.json.dto.HeosCommunicationAttribute.PLAYER_ID;
import static org.openhab.binding.heos.internal.json.dto.HeosEvent.GROUP_VOLUME_CHANGED;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.heos.internal.configuration.PlayerConfiguration;
import org.openhab.binding.heos.internal.exception.HeosFunctionalException;
import org.openhab.binding.heos.internal.json.dto.HeosErrorCode;
import org.openhab.binding.heos.internal.json.dto.HeosEventObject;
import org.openhab.binding.heos.internal.json.dto.HeosResponseObject;
import org.openhab.binding.heos.internal.json.payload.Media;
import org.openhab.binding.heos.internal.json.payload.Player;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosPlayerHandler} handles the actions for a HEOS player.
 * Channel commands are received and send to the dedicated channels
 *
 * @author Johannes Einig - Initial contribution
 */
@NonNullByDefault
public class HeosPlayerHandler extends HeosThingBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(HeosPlayerHandler.class);

    private @NonNullByDefault({}) String pid;
    private @Nullable Future<?> scheduledFuture;

    public HeosPlayerHandler(Thing thing, HeosDynamicStateDescriptionProvider heosDynamicStateDescriptionProvider) {
        super(thing, heosDynamicStateDescriptionProvider);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        @Nullable
        HeosChannelHandler channelHandler = getHeosChannelHandler(channelUID);
        if (channelHandler != null) {
            try {
                channelHandler.handlePlayerCommand(command, getId(), thing.getUID());
                handleSuccess();
            } catch (IOException | ReadException e) {
                handleError(e);
            }
        }
    }

    @Override
    public void initialize() {
        super.initialize();

        PlayerConfiguration configuration = thing.getConfiguration().as(PlayerConfiguration.class);
        pid = configuration.pid;

        cancel(scheduledFuture);
        scheduledFuture = scheduler.submit(this::delayedInitialize);
    }

    private synchronized void delayedInitialize() {
        try {
            refreshPlayState(pid);

            handleThingStateUpdate(getApiConnection().getPlayerInfo(pid));

            updateStatus(ThingStatus.ONLINE);
        } catch (HeosFunctionalException e) {
            if (e.getCode() == HeosErrorCode.INVALID_ID) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, e.getCode().toString());
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getCode().toString());
            }
        } catch (IOException | ReadException e) {
            logger.debug("Failed to initialize, will try again", e);
            cancel(scheduledFuture, false);
            scheduledFuture = scheduler.schedule(this::delayedInitialize, 3, TimeUnit.SECONDS);
        }
    }

    @Override
    void refreshPlayState(String id) throws IOException, ReadException {
        super.refreshPlayState(id);

        handleThingStateUpdate(getApiConnection().getPlayerMuteState(id));
        handleThingStateUpdate(getApiConnection().getPlayerVolume(id));
    }

    @Override
    public void dispose() {
        cancel(scheduledFuture);
        super.dispose();
    }

    @Override
    public String getId() {
        return pid;
    }

    @Override
    public void setNotificationSoundVolume(PercentType volume) {
    }

    @Override
    public void playerStateChangeEvent(HeosEventObject eventObject) {
        if (!pid.equals(eventObject.getAttribute(PLAYER_ID))) {
            return;
        }

        if (GROUP_VOLUME_CHANGED == eventObject.command) {
            logger.debug("Ignoring group-volume changes for players");
            return;
        }

        handleThingStateUpdate(eventObject);
    }

    @Override
    public void playerStateChangeEvent(HeosResponseObject<?> responseObject) throws HeosFunctionalException {
        if (!pid.equals(responseObject.getAttribute(PLAYER_ID))) {
            return;
        }

        handleThingStateUpdate(responseObject);
    }

    @Override
    public void playerMediaChangeEvent(String eventPid, Media media) {
        if (!pid.equals(eventPid)) {
            return;
        }

        handleThingMediaUpdate(media);
    }

    @Override
    public void setStatusOffline() {
        updateStatus(ThingStatus.OFFLINE);
    }

    @Override
    public void setStatusOnline() {
        updateStatus(ThingStatus.ONLINE);
    }

    public static void propertiesFromPlayer(Map<String, ? super String> prop, Player player) {
        prop.put(PROP_NAME, player.name);
        prop.put(PROP_PID, String.valueOf(player.playerId));
        prop.put(Thing.PROPERTY_MODEL_ID, player.model);
        prop.put(Thing.PROPERTY_FIRMWARE_VERSION, player.version);
        prop.put(PROP_NETWORK, player.network);
        prop.put(PROP_IP, player.ip);
        @Nullable
        String serialNumber = player.serial;
        if (serialNumber != null) {
            prop.put(Thing.PROPERTY_SERIAL_NUMBER, serialNumber);
        } else {
            prop.put(Thing.PROPERTY_SERIAL_NUMBER, String.valueOf(player.playerId)); // If no serial number is provided,
                                                                                     // write an empty string to
            // prevent error during runtime
        }
    }
}
