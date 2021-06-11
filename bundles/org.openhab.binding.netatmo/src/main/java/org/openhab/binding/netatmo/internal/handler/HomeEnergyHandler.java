/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.handler;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.EnergyApi;
import org.openhab.binding.netatmo.internal.api.HomeApi;
import org.openhab.binding.netatmo.internal.api.ModuleType;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.SetpointMode;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.dto.NAHome;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEnergy;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HomeEnergyHandler} is the class used to handle the plug
 * device of a thermostat set
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class HomeEnergyHandler extends NetatmoDeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(HomeEnergyHandler.class);
    private NAHome home = new NAHomeEnergy();

    public HomeEnergyHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, descriptionProvider);
    }

    @Override
    protected NAHome updateReadings() throws NetatmoException {
        EnergyApi api = apiBridge.getRestManager(EnergyApi.class);
        HomeApi homeapi = apiBridge.getRestManager(HomeApi.class);
        if (api != null && homeapi != null) {
            home = homeapi.getHomeList(config.id, ModuleType.NAPlug).iterator().next();
            if (home instanceof NAHomeEnergy) {
                NAHomeEnergy homeEnergy = (NAHomeEnergy) home;
                NAHome status = api.getHomeStatus(config.id);
                // could not find out how to persist retrieved /homesdata and /homestatus so that the information later
                // is accesssible by the other handlers
                home.getRooms().addAll(status.getRooms());
                home.getModules().putAll(status.getModules());
                ChannelUID channelUID = new ChannelUID(getThing().getUID(), GROUP_HOME_ENERGY, CHANNEL_PLANNING);
                descriptionProvider.setStateOptions(channelUID, homeEnergy.getThermSchedules().stream()
                        .map(p -> new StateOption(p.getId(), p.getName())).collect(Collectors.toList()));
                return home;
            }
        }
        throw new NetatmoException("No api available to access Energy or Home Api");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            super.handleCommand(channelUID, command);
        } else {
            String channelName = channelUID.getIdWithoutGroup();
            if (CHANNEL_PLANNING.equals(channelName)) {
                apiBridge.getEnergyApi().ifPresent(api -> {
                    tryApiCall(() -> api.switchSchedule(config.id, command.toString()));
                });
            } else if (channelName.equals(CHANNEL_SETPOINT_MODE)) {
                SetpointMode targetMode = SetpointMode.valueOf(command.toString());
                if (targetMode == SetpointMode.MANUAL) {
                    // updateState(channelUID, toStringType(currentData.getSetpointMode()));
                    logger.info("Switch to 'Manual' is done by setting a setpoint temp, command ignored");
                } else {
                    callSetThermMode(config.id, targetMode);
                }
            }
        }
    }

    @Override
    protected void updateChildModules() {
        super.updateChildModules();
        if (naThing instanceof NAHomeEnergy) {
            NAHomeEnergy localNaThing = (NAHomeEnergy) naThing;
            localNaThing.getRooms().forEach(entry -> notifyListener(entry.getId(), entry));
        }
    }

    private void callSetThermMode(String homeId, SetpointMode targetMode) {
        apiBridge.getEnergyApi().ifPresent(api -> {
            tryApiCall(() -> api.setThermMode(homeId, targetMode.getDescriptor()));
        });
    }

    public int getSetpointDefaultDuration() {
        NAHomeEnergy localHome = getHome();
        if (localHome != null) {
            return localHome.getThermSetpointDefaultDuration();
        }
        return -1;
    }

    public @Nullable NAHomeEnergy getHome() {
        if (home instanceof NAHomeEnergy) {
            return (NAHomeEnergy) home;
        }
        return null;
    }
}
