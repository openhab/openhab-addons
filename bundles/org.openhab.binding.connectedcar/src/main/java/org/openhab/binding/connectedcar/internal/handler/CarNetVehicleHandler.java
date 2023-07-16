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
package org.openhab.binding.connectedcar.internal.handler;

import static org.openhab.binding.connectedcar.internal.BindingConstants.*;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.*;
import static org.openhab.binding.connectedcar.internal.util.Helpers.*;

import java.time.ZoneId;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.ApiActionRequest;
import org.openhab.binding.connectedcar.internal.api.ApiErrorDTO;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApi;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNErrorMessage2Details;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetServiceCarFinder;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetServiceCharger;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetServiceClimater;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetServiceDestinations;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetServiceGeoFenceAlerts;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetServiceHonkFlash;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetServicePreHeat;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetServiceRLU;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetServiceSpeedAlerts;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetServiceStatus;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetServiceTripData;
import org.openhab.binding.connectedcar.internal.provider.CarChannelTypeProvider;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions;
import org.openhab.binding.connectedcar.internal.provider.ChannelDefinitions.ChannelIdMapEntry;
import org.openhab.binding.connectedcar.internal.util.TextResources;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PointType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CarNetVehicleHandler} implements the Vehicle Handler for CarNet
 *
 * @author Markus Michels - Initial contribution
 * @author Lorenzo Bernardi - Additional contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class CarNetVehicleHandler extends ThingBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(CarNetVehicleHandler.class);

    public CarNetVehicleHandler(Thing thing, TextResources resources, ZoneId zoneId, ChannelDefinitions idMapper,
            CarChannelTypeProvider channelTypeProvider) throws ApiException {
        super(thing, resources, zoneId, idMapper, channelTypeProvider);
    }

    @Override
    public boolean createBrandChannels(Map<String, ChannelIdMapEntry> channels) {
        addChannels(channels, true, CHANNEL_GENERAL_RATELIM, CHANNEL_STATUS_LOCKED, CHANNEL_STATUS_MAINTREQ,
                CHANNEL_STATUS_WINCLOSED, CHANNEL_STATUS_TIRESOK);
        return true;
    }

    @Override
    public boolean handleBrandCommand(ChannelUID channelUID, Command command) throws ApiException {
        String channelId = channelUID.getIdWithoutGroup();
        boolean processed = true;
        String action = "";
        String actionStatus = "";
        boolean switchOn = (command instanceof OnOffType) && (OnOffType) command == OnOffType.ON;
        try {
            switch (channelId) {
                case CHANNEL_CONTROL_LOCK:
                    action = switchOn ? "lock" : "unlock";
                    actionStatus = api.controlLock(switchOn);
                    break;
                case CHANNEL_CONTROL_CLIMATER:
                    action = switchOn ? "startClimater" : "stopClimater";
                    actionStatus = api.controlClimater(switchOn, getHeaterSource());
                    break;
                case CHANNEL_CONTROL_TARGET_TEMP:
                    actionStatus = api.controlClimaterTemp(((DecimalType) command).doubleValue(), getHeaterSource());
                    break;
                case CHANNEL_CLIMATER_HEATSOURCE:
                    String heaterSource = command.toString().toLowerCase();
                    logger.debug("{}: Set heater source for climatisation to {}", thingId, heaterSource);
                    cache.setValue(channelId, channelUID.getId(), new StringType(heaterSource));
                    break;
                case CHANNEL_CONTROL_CHARGER:
                    action = switchOn ? "startCharging" : "stopCharging";
                    actionStatus = api.controlCharger(switchOn);
                    break;
                case CHANNEL_CHARGER_MAXCURRENT:
                    int maxCurrent = ((DecimalType) command).intValue();
                    logger.info("{}: Setting max charging current to {}A", thingId, maxCurrent);
                    action = "controlMaxCurrent";
                    actionStatus = api.controlMaxCharge(maxCurrent);
                    break;
                case CHANNEL_CONTROL_WINHEAT:
                    action = switchOn ? "startWindowHeat" : "stopWindowHeat";
                    actionStatus = api.controlWindowHeating(switchOn);
                    break;
                case CHANNEL_CONTROL_PREHEAT:
                    action = switchOn ? "startPreHeat" : "stopPreHeat";
                    actionStatus = api.controlPreHeating(switchOn, 30);
                    break;
                case CHANNEL_CONTROL_VENT:
                    action = switchOn ? "startVentilation" : "stopVentilation";
                    actionStatus = api.controlVentilation(switchOn, getVentDuration());
                    break;
                case CHANNEL_CONTROL_DURATION:
                    DecimalType value = new DecimalType(((DecimalType) command).intValue());
                    logger.debug("{}: Set ventilation/pre-heat duration to {}", thingId, value);
                    cache.setValue(channelUID.getId(), value);
                    break;
                case CHANNEL_CONTROL_FLASH:
                case CHANNEL_CONTROL_HONKFLASH:
                    if (command == OnOffType.ON) {
                        State point = cache.getValue(mkChannelId(CHANNEL_GROUP_LOCATION, CHANNEL_LOCATTION_GEO));
                        if (point != UnDefType.NULL) {
                            action = CHANNEL_CONTROL_FLASH == channelId ? "flash" : "honk";
                            actionStatus = api.controlHonkFlash(CHANNEL_CONTROL_HONKFLASH.equals(channelId),
                                    (PointType) point, getHfDuration());
                        } else {
                            logger.warn("{}: Geo position is not available, can't execute command", thingId);
                        }
                    }
                    break;
                case CHANNEL_CONTROL_HFDURATION:
                    DecimalType hfd = new DecimalType(((DecimalType) command).intValue());
                    logger.debug("{}: Set honk%flash duration to {}", thingId, hfd);
                    cache.setValue(channelUID.getId(), hfd);
                    break;
                default:
                    processed = false;
            }
        } catch (ApiException e) {
            if (command instanceof OnOffType) {
                updateState(channelUID.getId(), OnOffType.OFF);
            }
            throw e;
        }

        if (processed && !action.isEmpty()) {
            logger.debug("{}: Action {} submitted, initial status={}", thingId, action, actionStatus);
        }
        return processed;
    }

    private String getHeaterSource() {
        State value = cache.getValue(CHANNEL_GROUP_CONTROL, CHANNEL_CLIMATER_HEATSOURCE);
        return value != UnDefType.NULL ? ((StringType) value).toString().toLowerCase() : CNAPI_HEATER_SOURCE_ELECTRIC;
    }

    private int getVentDuration() {
        State state = cache.getValue(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_DURATION);
        return state != UnDefType.NULL ? ((DecimalType) state).intValue() : VENT_DEFAULT_DURATION_MIN;
    }

    private int getHfDuration() {
        State state = cache.getValue(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_HFDURATION);
        return state != UnDefType.NULL ? ((DecimalType) state).intValue() : HF_DEFAULT_DURATION_SEC;
    }

    @Override
    public boolean updateActionStatus(String service, String action, String statusDetail) {
        boolean updated = false;
        updated |= updateChannel(CHANNEL_GENERAL_ACTION, getStringType(service + "." + action));
        updated |= updateChannel(CHANNEL_GENERAL_ACTION_STATUS, getStringType(statusDetail));
        updated |= updateChannel(CHANNEL_GENERAL_ACTION_PENDING,
                ApiActionRequest.isInProgress(statusDetail) ? OnOffType.ON : OnOffType.OFF);

        if (!ApiActionRequest.isInProgress(statusDetail)) {
            String channel = "";
            switch (action) {
                case CNAPI_CMD_FLASH:
                    channel = CHANNEL_CONTROL_FLASH;
                    break;
                case CNAPI_CMD_HONK_FLASH:
                    channel = CHANNEL_CONTROL_HONKFLASH;
                    break;
            }
            if (!channel.isEmpty()) {
                updateChannel(channel, OnOffType.OFF);
            }

            forceUpdate = true; // refresh vehicle status
            updated = true;
        }
        return updateLastUpdate(updated);
    }

    /**
     * Register all available services
     */
    @Override
    public void registerServices() {
        services.clear();
        addService(new CarNetServiceStatus(this, (CarNetApi) api));
        addService(new CarNetServiceCarFinder(this, (CarNetApi) api));
        addService(new CarNetServiceRLU(this, (CarNetApi) api));
        addService(new CarNetServiceClimater(this, (CarNetApi) api));
        addService(new CarNetServicePreHeat(this, (CarNetApi) api));
        addService(new CarNetServiceCharger(this, (CarNetApi) api));
        addService(new CarNetServiceTripData(this, (CarNetApi) api));
        addService(new CarNetServiceDestinations(this, (CarNetApi) api));
        addService(new CarNetServiceHonkFlash(this, (CarNetApi) api));
        addService(new CarNetServiceGeoFenceAlerts(this, (CarNetApi) api));
        addService(new CarNetServiceSpeedAlerts(this, (CarNetApi) api));
    }

    @Override
    protected String getReason(ApiErrorDTO error) {
        CNErrorMessage2Details details = error.details;
        if (details != null) {
            return getString(details.reason);
        }
        return "";
    }
}
