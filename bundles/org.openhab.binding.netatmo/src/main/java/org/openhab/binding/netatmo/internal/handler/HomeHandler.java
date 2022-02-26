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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FeatureArea;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.handler.capability.EnergyCapability;
import org.openhab.binding.netatmo.internal.handler.capability.EventCapability;
import org.openhab.binding.netatmo.internal.handler.capability.HomeCapability;
import org.openhab.binding.netatmo.internal.handler.capability.SecurityCapability;
import org.openhab.binding.netatmo.internal.handler.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.handler.propertyhelper.HomePropertyHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.webhook.NetatmoServlet;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.binding.builder.ThingBuilder;
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

    public HomeHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider, NetatmoServlet webhookServlet) {
        super(bridge, channelHelpers, apiBridge, descriptionProvider, webhookServlet, new HomePropertyHelper(bridge));
        defineCapability(new HomeCapability(getThing(), apiBridge, getId()));
        defineCapability(new EventCapability(getThing(), apiBridge, webhookServlet));
    }

    @Override
    protected List<NAObject> updateReadings() throws NetatmoException {
        List<NAObject> result = new ArrayList<>();
        getCapability(HomeCapability.class).ifPresent(cap -> {
            try {
                result.addAll(cap.updateReadings());
            } catch (NetatmoException e) {
                logger.warn("Error updating child informations : {}", e.getMessage());
            }
        });

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
    public void setNewData(NAObject newData) {
        super.setNewData(newData);

        HomePropertyHelper propHelper = (HomePropertyHelper) propertyHelper;
        if (propHelper.hasFeature(FeatureArea.SECURITY) && (getCapability(SecurityCapability.class).isEmpty())) {
            defineCapability(new SecurityCapability(getThing(), apiBridge, getId()));
        }
        if (propHelper.hasFeature(FeatureArea.ENERGY) && (getCapability(EnergyCapability.class).isEmpty())) {
            defineCapability(new EnergyCapability(getThing(), apiBridge, descriptionProvider, getId()));
        }

        List<Channel> channelsToRemove = new ArrayList<>();
        if (getCapability(SecurityCapability.class).isEmpty()) {
            channelsToRemove.addAll(getThing().getChannelsOfGroup(GROUP_HOME_SECURITY));
        }
        if (getCapability(EnergyCapability.class).isEmpty()) {
            channelsToRemove.addAll(getThing().getChannelsOfGroup(GROUP_HOME_ENERGY));
        }
        if (!channelsToRemove.isEmpty()) {
            ThingBuilder builder = editThing().withoutChannels(channelsToRemove);
            updateThing(builder.build());
        }
    }

}
