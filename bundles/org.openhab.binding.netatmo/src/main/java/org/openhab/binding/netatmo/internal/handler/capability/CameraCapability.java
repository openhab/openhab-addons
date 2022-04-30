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
package org.openhab.binding.netatmo.internal.handler.capability;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataPerson;
import org.openhab.binding.netatmo.internal.api.dto.HomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.binding.netatmo.internal.handler.channelhelper.CameraChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.ChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.EventChannelHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;

/**
 * {@link CameraCapability} give to handle Welcome Camera specifics
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class CameraCapability extends Capability {
    private final CameraChannelHelper cameraHelper;
    private final EventChannelHelper eventHelper;
    protected @Nullable String localUrl;
    private final NetatmoDescriptionProvider descriptionProvider;
    private final ChannelUID personChannelUID;

    public CameraCapability(CommonInterface handler, NetatmoDescriptionProvider descriptionProvider,
            List<ChannelHelper> channelHelpers) {
        super(handler);
        this.descriptionProvider = descriptionProvider;
        this.personChannelUID = new ChannelUID(thing.getUID(), GROUP_LAST_EVENT, CHANNEL_EVENT_PERSON_ID);
        this.cameraHelper = (CameraChannelHelper) channelHelpers.stream().filter(c -> c instanceof CameraChannelHelper)
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "CameraHandler must have a CameraChannelHelper, file a bug."));
        this.eventHelper = (EventChannelHelper) channelHelpers.stream().filter(c -> c instanceof EventChannelHelper)
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "CameraHandler must have a EventChannelHelper, file a bug."));
    }

    @Override
    public void updateHomeStatusModule(HomeStatusModule newData) {
        super.updateHomeStatusModule(newData);
        String vpnUrl = newData.getVpnUrl();
        boolean isLocal = newData.isLocal();
        if (vpnUrl != null) {
            localUrl = isLocal
                    ? handler.getHomeCapability(SecurityCapability.class).map(cap -> cap.ping(vpnUrl)).orElse(null)
                    : null;
            cameraHelper.setUrls(vpnUrl, localUrl);
            eventHelper.setUrls(vpnUrl, localUrl);
        }
    }

    @Override
    public void handleCommand(String channelName, Command command) {
        if (command instanceof OnOffType && CHANNEL_MONITORING.equals(channelName)) {
            handler.getHomeCapability(SecurityCapability.class)
                    .ifPresent(cap -> cap.changeStatus(localUrl, OnOffType.ON.equals(command)));
        } else {
            super.handleCommand(channelName, command);
        }
    }

    @Override
    protected void beforeNewData() {
        super.beforeNewData();
        handler.getHomeCapability(HomeCapability.class).ifPresent(cap -> {
            NAObjectMap<HomeDataPerson> persons = cap.getPersons();
            descriptionProvider.setStateOptions(personChannelUID, persons.values().stream()
                    .map(p -> new StateOption(p.getId(), p.getName())).collect(Collectors.toList()));
        });
    }

    @Override
    public List<NAObject> updateReadings() {
        List<NAObject> result = new ArrayList<>();
        handler.getHomeCapability(SecurityCapability.class).ifPresent(cap -> {
            Collection<HomeEvent> events = cap.getCameraEvents(handler.getId());
            if (!events.isEmpty()) {
                // Get the most recent event
                List<HomeEvent> listEvents = new ArrayList<>(events);
                Collections.sort(listEvents, new Comparator<HomeEvent>() {
                    @Override
                    public int compare(HomeEvent o1, HomeEvent o2) {
                        if (o1.getTime().isBefore(o2.getTime())) {
                            return 1;
                        } else if (o1.getTime().isAfter(o2.getTime())) {
                            return -1;
                        }
                        return 0;
                    }
                });
                result.add(listEvents.get(0));
            }
        });
        return result;
    }
}
