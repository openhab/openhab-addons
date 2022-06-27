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
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.AlimentationStatus;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.SdCardStatus;
import org.openhab.binding.netatmo.internal.api.dto.HomeDataPerson;
import org.openhab.binding.netatmo.internal.api.dto.HomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.deserialization.NAObjectMap;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.binding.netatmo.internal.handler.channelhelper.CameraChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.ChannelHelper;
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
public class CameraCapability extends HomeSecurityThingCapability {
    private final CameraChannelHelper cameraHelper;
    private final ChannelUID personChannelUID;

    protected @Nullable String localUrl;

    public CameraCapability(CommonInterface handler, NetatmoDescriptionProvider descriptionProvider,
            List<ChannelHelper> channelHelpers) {
        super(handler, descriptionProvider, channelHelpers);
        this.personChannelUID = new ChannelUID(thing.getUID(), GROUP_LAST_EVENT, CHANNEL_EVENT_PERSON_ID);
        this.cameraHelper = (CameraChannelHelper) channelHelpers.stream().filter(c -> c instanceof CameraChannelHelper)
                .findFirst().orElseThrow(() -> new IllegalArgumentException(
                        "CameraCapability must find a CameraChannelHelper, please file a bug report."));
    }

    @Override
    public void updateHomeStatusModule(HomeStatusModule newData) {
        super.updateHomeStatusModule(newData);
        String vpnUrl = newData.getVpnUrl();
        if (vpnUrl != null) {
            localUrl = newData.isLocal() ? securityCapability.map(cap -> cap.ping(vpnUrl)).orElse(null) : null;
            cameraHelper.setUrls(vpnUrl, localUrl);
            eventHelper.setUrls(vpnUrl, localUrl);
        }
        if (!SdCardStatus.SD_CARD_WORKING.equals(newData.getSdStatus())
                || !AlimentationStatus.ALIM_CORRECT_POWER.equals(newData.getAlimStatus())) {
            statusReason = String.format("%s, %s", newData.getSdStatus(), newData.getAlimStatus());
        }
    }

    @Override
    public void handleCommand(String channelName, Command command) {
        if (command instanceof OnOffType && CHANNEL_MONITORING.equals(channelName)) {
            securityCapability.ifPresent(cap -> cap.changeStatus(localUrl, OnOffType.ON.equals(command)));
        } else {
            super.handleCommand(channelName, command);
        }
    }

    @Override
    protected void beforeNewData() {
        super.beforeNewData();
        homeCapability.ifPresent(cap -> {
            NAObjectMap<HomeDataPerson> persons = cap.getPersons();
            descriptionProvider.setStateOptions(personChannelUID, persons.values().stream()
                    .map(p -> new StateOption(p.getId(), p.getName())).collect(Collectors.toList()));
        });
    }

    @Override
    public List<NAObject> updateReadings() {
        List<NAObject> result = new ArrayList<>();
        securityCapability.ifPresent(cap -> {
            Collection<HomeEvent> events = cap.getDeviceEvents(handler.getId(), moduleType.apiName);
            if (!events.isEmpty()) {
                HomeEvent event = events.iterator().next();
                result.add(event);
                result.addAll(event.getSubevents());
            }
        });
        return result;
    }
}
