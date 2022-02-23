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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.SecurityApi;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeDataPerson;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;
import org.openhab.binding.netatmo.internal.handler.capability.SecurityCapability;
import org.openhab.binding.netatmo.internal.handler.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.CameraChannelHelper;
import org.openhab.binding.netatmo.internal.handler.propertyhelper.PropertyHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.webhook.NetatmoServlet;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CameraHandler} is the class handling Camera things
 *
 * @author Ing. Peter Weiss - Initial contribution
 *
 */
@NonNullByDefault
public class CameraHandler extends NetatmoHandler {
    private final Logger logger = LoggerFactory.getLogger(CameraHandler.class);
    private final NetatmoDescriptionProvider descriptionProvider;
    protected final CameraChannelHelper cameraHelper;
    private Optional<SecurityCapability> securityCap = Optional.empty();

    public CameraHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider, NetatmoServlet webhookServlet) {
        super(bridge, channelHelpers, apiBridge, descriptionProvider, webhookServlet);
        this.descriptionProvider = descriptionProvider;
        this.cameraHelper = (CameraChannelHelper) channelHelpers.stream().filter(c -> c instanceof CameraChannelHelper)
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "CameraHandler must have a CameraChannelHelper, file a bug."));
    }

    @Override
    protected PropertyHelper getPropertyHelper() {
        return new PropertyHelper(getThing());
    }

    @Override
    public void initialize() {
        super.initialize();

        NetatmoHandler bridgeHandler = getBridgeHandler();
        if (bridgeHandler instanceof HomeHandler) {
            HomeHandler homeHandler = (HomeHandler) bridgeHandler;
            securityCap = homeHandler.getSecurityCap();
            homeHandler.getHomeCap().ifPresent(cap -> {
                NAObjectMap<NAHomeDataPerson> persons = cap.getPersons();
                updateStatus(ThingStatus.ONLINE);
                descriptionProvider
                        .setStateOptions(new ChannelUID(getThing().getUID(), GROUP_LAST_EVENT, CHANNEL_EVENT_PERSON_ID),
                                persons.values().stream().filter(person -> person.isKnown())
                                        .map(p -> new StateOption(p.getId(), p.getName()))
                                        .collect(Collectors.toList()));
            });
        }
    }

    @Override
    protected List<NAObject> updateReadings() throws NetatmoException {
        List<NAObject> result = new ArrayList<>();
        securityCap.ifPresent(cap -> {
            Collection<NAHomeEvent> events = cap.getCameraEvents(getId());
            if (!events.isEmpty()) {
                result.add(events.iterator().next());
            }
        });
        return result;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof OnOffType && CHANNEL_MONITORING.equals(channelUID.getIdWithoutGroup())) {
            securityCap.ifPresent(cap -> cap.changeStatus(cameraHelper.getLocalURL(), OnOffType.ON.equals(command)));
        } else {
            super.handleCommand(channelUID, command);
        }
    }

    @Override
    public void setNewData(NAObject newData) {
        SecurityApi securityApi = apiBridge.getRestManager(SecurityApi.class);
        if (newData instanceof NAHomeStatusModule && securityApi != null) {
            NAHomeStatusModule camera = (NAHomeStatusModule) newData;
            String vpnUrl = camera.getVpnUrl();
            boolean isLocal = camera.isLocal();
            if (vpnUrl != null && isLocal) {
                try {
                    cameraHelper.setUrls(vpnUrl, securityApi.ping(vpnUrl));
                } catch (NetatmoException e) {
                    logger.warn("Error pinging camera : {}", e.getMessage());
                }
            }
        }
        super.setNewData(newData);
    }
}
