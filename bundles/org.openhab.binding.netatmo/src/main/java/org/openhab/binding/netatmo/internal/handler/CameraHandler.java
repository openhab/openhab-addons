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
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.*;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.dto.NAEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeEvent;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.NAPerson;
import org.openhab.binding.netatmo.internal.api.dto.NAWelcome;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CameraHandler} is the class used to handle Camera Data
 *
 * @author Sven Strohschein - Initial contribution (partly moved code from NAWelcomeCameraHandler to introduce
 *         inheritance, see NAWelcomeCameraHandler)
 *
 */
@NonNullByDefault
public class CameraHandler extends DeviceWithEventHandler {
    private final Logger logger = LoggerFactory.getLogger(CameraHandler.class);
    protected @Nullable CameraAddress cameraAddress;

    public CameraHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, descriptionProvider);
    }

    public void setPersons(List<NAPerson> knownPersons) {
        descriptionProvider.setStateOptions(
                new ChannelUID(getThing().getUID(), GROUP_WELCOME_EVENT, CHANNEL_EVENT_PERSON_ID),
                knownPersons.stream().map(p -> new StateOption(p.getId(), p.getName())).collect(Collectors.toList()));
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if ((command instanceof OnOffType) && (CHANNEL_CAMERA_IS_MONITORING.equals(channelUID.getIdWithoutGroup()))) {
            CameraAddress camAddress = cameraAddress;
            if (camAddress != null) {
                String localURL = camAddress.getLocalURL();
                if (localURL != null) {
                    tryApiCall(() -> apiBridge.getHomeApi().changeStatus(localURL, command == OnOffType.ON));
                }
            }
        } else {
            super.handleCommand(channelUID, command);
        }
    }

    @Override
    protected void internalSetNewEvent(NAEvent event) {
        logger.debug("Updating camera with event : {}", event.toString());
        updateIfLinked(GROUP_WELCOME_EVENT, CHANNEL_EVENT_TYPE, toStringType(event.getEventType()));
        updateIfLinked(GROUP_WELCOME_EVENT, CHANNEL_EVENT_MESSAGE, toStringType(event.getMessage()));
        updateIfLinked(GROUP_WELCOME_EVENT, CHANNEL_EVENT_TIME, new DateTimeType(event.getTime()));
        updateIfLinked(GROUP_WELCOME_EVENT, CHANNEL_EVENT_PERSON_ID, toStringType(event.getPersonId()));
        updateIfLinked(GROUP_WELCOME_EVENT, CHANNEL_EVENT_SUBTYPE,
                event.getSubTypeDescription().map(d -> toStringType(d)).orElse(UnDefType.NULL));

        event.getSnapshot().ifPresent(snapshot -> {
            String url = snapshot.getUrl();
            updateIfLinked(GROUP_WELCOME_EVENT, CHANNEL_EVENT_SNAPSHOT, toRawType(url));
            updateIfLinked(GROUP_WELCOME_EVENT, CHANNEL_EVENT_SNAPSHOT_URL, toStringType(url));
        });
        if (event instanceof NAHomeEvent) {
            NAHomeEvent homeEvent = (NAHomeEvent) event;
            updateIfLinked(GROUP_WELCOME_EVENT, CHANNEL_EVENT_VIDEO_STATUS, toStringType(homeEvent.getVideoStatus()));
            updateIfLinked(GROUP_WELCOME_EVENT, CHANNEL_EVENT_VIDEO_URL,
                    toStringType(getStreamURL(homeEvent.getVideoId())));
        }
    }

    @Override
    public void setNewData(NAObject newData) {
        if (newData instanceof NAWelcome) {
            NAWelcome camera = (NAWelcome) newData;
            String vpn = camera.getVpnUrl();
            if (vpn != null) {
                CameraAddress address = cameraAddress;
                if (address == null || address.vpnURLChanged(vpn)) {
                    cameraAddress = new CameraAddress(vpn, camera.isLocal(), pingVpnUrl(vpn));
                }
            }
        }
        super.setNewData(newData);
    }

    private @Nullable String getStreamURL(@Nullable String videoId) {
        CameraAddress camAddress = cameraAddress;
        return (camAddress != null && videoId != null) ? camAddress.getStreamUrl(videoId) : null;
    }

    private @Nullable String pingVpnUrl(String vpnUrl) {
        try {
            return apiBridge.getHomeApi().ping(vpnUrl);
        } catch (NetatmoException e) {
            logger.warn("Error pinging camera : {}", e.getMessage());
            return null;
        }
    }
}
