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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service;

import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.EMPTY;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.POWER_ON;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.STANDBY;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.TV_NOT_LISTENING_MSG;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.TV_OFFLINE_MSG;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVBindingConstants.TV_POWERSTATE_PATH;
import static org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVConnectionManager.OBJECT_MAPPER;

import java.io.IOException;
import java.util.function.Predicate;

import org.apache.http.ParseException;
import org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.WakeOnLanUtil;
import org.openhab.binding.androidtv.internal.protocol.philipstv.config.PhilipsTVConfiguration;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.api.PhilipsTVService;
import org.openhab.binding.androidtv.internal.protocol.philipstv.service.model.power.PowerStateDto;
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
 */
public class PowerService implements PhilipsTVService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PhilipsTVConnectionManager connectionManager;

    private final Predicate<PhilipsTVConfiguration> isWakeOnLanEnabled = config -> config.macAddress != null
            && !config.macAddress.isEmpty();

    public PowerService(PhilipsTVConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void handleCommand(String channel, Command command) {
        try {
            if (command instanceof RefreshType) {
                PowerStateDto powerStateDto = getPowerState();
                if (powerStateDto.isPoweredOn()) {
                    connectionManager.postUpdateThing(ThingStatus.ONLINE, ThingStatusDetail.NONE, EMPTY);
                } else if (powerStateDto.isStandby()) {
                    connectionManager.postUpdateThing(ThingStatus.ONLINE, ThingStatusDetail.NONE, STANDBY);
                } else {
                    connectionManager.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, EMPTY);
                }
            } else if (command instanceof OnOffType) {
                setPowerState((OnOffType) command);
                if (command == OnOffType.ON) {
                    connectionManager.postUpdateThing(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Tv turned on.");
                } else {
                    connectionManager.postUpdateThing(ThingStatus.ONLINE, ThingStatusDetail.NONE, STANDBY);
                }
            } else {
                logger.warn("Unknown command: {} for Channel {}", command, channel);
            }
        } catch (Exception e) {
            if (isTvOfflineException(e)) {
                connectionManager.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.NONE, TV_OFFLINE_MSG);
            } else if (isTvNotListeningException(e)) {
                connectionManager.postUpdateThing(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        TV_NOT_LISTENING_MSG);
            } else {
                logger.warn("Unexpected Error handling the PowerState command {} for Channel {}: {}", command, channel,
                        e.getMessage());
            }
        }
    }

    private PowerStateDto getPowerState() throws IOException, ParseException {
        return OBJECT_MAPPER.readValue(connectionManager.doHttpsGet(TV_POWERSTATE_PATH), PowerStateDto.class);
    }

    private void setPowerState(OnOffType onOffType) throws IOException, InterruptedException {
        PowerStateDto powerStateDto = new PowerStateDto();
        if (onOffType == OnOffType.ON) {
            if (isWakeOnLanEnabled.test(connectionManager.config)
                    && !WakeOnLanUtil.isReachable(connectionManager.config.host)) {
                WakeOnLanUtil.wakeOnLan(connectionManager.config.host, connectionManager.config.macAddress);
            }
            powerStateDto.setPowerState(POWER_ON);
        } else {
            powerStateDto.setPowerState(STANDBY);
        }

        String powerStateJson = OBJECT_MAPPER.writeValueAsString(powerStateDto);
        logger.debug("PowerState Json sent: {}", powerStateJson);
        connectionManager.doHttpsPost(TV_POWERSTATE_PATH, powerStateJson);
    }
}
