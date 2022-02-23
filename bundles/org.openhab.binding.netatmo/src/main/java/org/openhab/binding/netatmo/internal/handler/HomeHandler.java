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
package org.openhab.binding.netatmo.internal.handler;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeData;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatus.HomeStatus;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.handler.capability.EnergyCapability;
import org.openhab.binding.netatmo.internal.handler.capability.EventListenerCapability;
import org.openhab.binding.netatmo.internal.handler.capability.HomeCapability;
import org.openhab.binding.netatmo.internal.handler.capability.SecurityCapability;
import org.openhab.binding.netatmo.internal.handler.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.handler.propertyhelper.NAHomePropertyHelper;
import org.openhab.binding.netatmo.internal.handler.propertyhelper.PropertyHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.webhook.NetatmoServlet;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link HomeHandler} is the class handling home things
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class HomeHandler extends NetatmoHandler {
    private final Logger logger = LoggerFactory.getLogger(HomeHandler.class);

    private Optional<SecurityCapability> securityCap = Optional.empty();
    private Optional<EnergyCapability> energyCap = Optional.empty();
    private HomeCapability homeCap;
    private @NonNullByDefault({}) EventListenerCapability eventCap;

    public HomeHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider, NetatmoServlet webhookServlet) {
        super(bridge, channelHelpers, apiBridge, descriptionProvider, webhookServlet);
        homeCap = new HomeCapability(getThing(), apiBridge, getId());
    }

    @Override
    protected PropertyHelper getPropertyHelper() {
        return new NAHomePropertyHelper(getThing());
    }

    @Override
    public void initialize() {
        super.initialize();
        eventCap = new EventListenerCapability(getThing(), apiBridge, webhookServlet);
    }

    @Override
    public void dispose() {
        super.dispose();
        eventCap.dispose();
    }

    @Override
    protected List<NAObject> updateReadings() throws NetatmoException {
        List<NAObject> result = homeCap.updateReadings();

        getActiveChildren().forEach(handler -> {
            try {
                result.addAll(handler.updateReadings());
            } catch (NetatmoException e) {
                logger.warn("Error updating child informations : {}", e.getMessage());
            }
        });

        return result;
    }

    @Override
    protected void updateChilds(NAObject newData) {
        super.updateChilds(newData);
        if (newData instanceof NAHomeData) {
            NAHomeData homeData = (NAHomeData) newData;
            securityCap.ifPresent(cap -> cap.updateHomeData(homeData));
            energyCap.ifPresent(cap -> cap.updateHomeData(homeData));
        }
        if (newData instanceof HomeStatus) {
            HomeStatus homeStatus = (HomeStatus) newData;
            securityCap.ifPresent(cap -> cap.updateHomeStatus(homeStatus));
            energyCap.ifPresent(cap -> cap.updateHomeStatus(homeStatus));
        }
        if (newData instanceof NAHomeEvent) {
            NAHomeEvent homeEvent = (NAHomeEvent) newData;
            securityCap.ifPresent(cap -> cap.updateHomeEvent(homeEvent));
        }
    }

    @Override
    public void setNewData(NAObject newData) {
        super.setNewData(newData);

        NAHomePropertyHelper propHelper = (NAHomePropertyHelper) propertyHelper;
        if (propHelper.hasFeature(FeatureArea.SECURITY) && securityCap.isEmpty()) {
            securityCap = Optional.of(new SecurityCapability(getThing(), apiBridge, getId()));
        }
        if (propHelper.hasFeature(FeatureArea.ENERGY) && energyCap.isEmpty()) {
            energyCap = Optional.of(new EnergyCapability(getThing(), apiBridge, getId()));
        }

        List<Channel> channelsToRemove = new ArrayList<>();
        if (securityCap.isEmpty()) {
            channelsToRemove.addAll(getThing().getChannelsOfGroup(GROUP_HOME_SECURITY));
        }
        if (energyCap.isEmpty()) {
            channelsToRemove.addAll(getThing().getChannelsOfGroup(GROUP_HOME_ENERGY));
        }
        if (!channelsToRemove.isEmpty()) {
            ThingBuilder builder = editThing().withoutChannels(channelsToRemove);
            updateThing(builder.build());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO : ce n'est pas super optimal parceque si la command a été traitée par l'un des cap il n'y a pas lieu de
        // continuer.
        energyCap.ifPresent(cap -> handleCommand(channelUID, command));
        securityCap.ifPresent(cap -> handleCommand(channelUID, command));
        super.handleCommand(channelUID, command);
    }

    public Optional<EnergyCapability> getEnergyCap() {
        return energyCap;
    }

    public Optional<HomeCapability> getHomeCap() {
        return Optional.of(homeCap);
    }

    public Optional<SecurityCapability> getSecurityCap() {
        return securityCap;
    }
}
