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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service;

import static org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager.OBJECT_MAPPER;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.*;

import java.io.IOException;

import org.apache.http.ParseException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.WakeOnLanUtil;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.api.PhilipsTVService;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.power.PowerStateDTO;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PowerService} is responsible for handling power states commands, which are sent to the
 * power channel.
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
@NonNullByDefault
public class PowerService implements PhilipsTVService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PhilipsTVConnectionManager handler;

    private final ConnectionManager connectionManager;

    private final boolean isWakeOnLanEnabled;

    public PowerService(PhilipsTVConnectionManager handler, ConnectionManager connectionManager) {
        this.handler = handler;
        this.connectionManager = connectionManager;
        this.isWakeOnLanEnabled = handler.getMacAddress().isEmpty() ? false : true;
    }

    @Override
    public void handleCommand(String channel, Command command) {
        try {
            if (command instanceof RefreshType) {
                PowerStateDTO powerStateDTO = getPowerState();
                if (powerStateDTO.isPoweredOn()) {
                    handler.postUpdateThing(ThingStatus.ONLINE, ThingStatusDetail.NONE, "online.online");
                } else if (powerStateDTO.isStandby()) {
                    handler.postUpdateThing(ThingStatus.ONLINE, ThingStatusDetail.NONE, "online.standby");
                    if (powerStateDTO.isStandbyKeep()) {
                        handler.checkPendingPowerOn();
                    }
                } else {
                    handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, EMPTY);
                }
            } else if (command instanceof OnOffType) {
                setPowerState((OnOffType) command);
                if (command == OnOffType.ON) {
                    handler.postUpdateThing(ThingStatus.ONLINE, ThingStatusDetail.NONE, "online.online");
                } else {
                    handler.postUpdateThing(ThingStatus.ONLINE, ThingStatusDetail.NONE, "online.standby");
                }
            } else {
                logger.warn("Unknown command: {} for Channel {}", command, channel);
            }
        } catch (Exception e) {
            if (isTvOfflineException(e)) {
                handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
            } else if (isTvNotListeningException(e)) {
                handler.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        TV_NOT_LISTENING_MSG);
            } else {
                logger.warn("Unexpected Error handling the PowerState command {} for Channel {}: {}", command, channel,
                        e.getMessage());
            }
        }
    }

    private PowerStateDTO getPowerState() throws IOException, ParseException {
        return OBJECT_MAPPER.readValue(connectionManager.doHttpsGet(TV_POWERSTATE_PATH), PowerStateDTO.class);
    }

    private void setPowerState(OnOffType onOffType) throws IOException, InterruptedException {
        PowerStateDTO powerStateDTO = new PowerStateDTO();
        if (onOffType == OnOffType.ON) {
            if (isWakeOnLanEnabled && !WakeOnLanUtil.isReachable(handler.config.ipAddress)) {
                WakeOnLanUtil.wakeOnLan(handler.config.ipAddress, handler.getMacAddress());
            }
            powerStateDTO.setPowerState(POWER_ON);
        } else {
            powerStateDTO.setPowerState(STANDBY);
        }

        String powerStateJson = OBJECT_MAPPER.writeValueAsString(powerStateDTO);
        logger.debug("PowerState Json sent: {}", powerStateJson);
        connectionManager.doHttpsPost(TV_POWERSTATE_PATH, powerStateJson);
    }
}
