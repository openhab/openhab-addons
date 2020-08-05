/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.verisure.internal.handler;

import static org.openhab.binding.verisure.internal.VerisureBindingConstants.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.verisure.internal.dto.VerisureDoorWindowsDTO;
import org.openhab.binding.verisure.internal.dto.VerisureDoorWindowsDTO.DoorWindow;

/**
 * Handler for the Smart Lock Device thing type that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureDoorWindowThingHandler extends VerisureThingHandler<VerisureDoorWindowsDTO> {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_DOORWINDOW);

    public VerisureDoorWindowThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public Class<VerisureDoorWindowsDTO> getVerisureThingClass() {
        return VerisureDoorWindowsDTO.class;
    }

    @Override
    public synchronized void update(VerisureDoorWindowsDTO thing) {
        updateDoorWindowState(thing);
        updateStatus(ThingStatus.ONLINE);
    }

    private void updateDoorWindowState(VerisureDoorWindowsDTO doorWindowJSON) {
        List<DoorWindow> doorWindowList = doorWindowJSON.getData().getInstallation().getDoorWindows();
        if (!doorWindowList.isEmpty()) {
            DoorWindow doorWindow = doorWindowList.get(0);

            getThing().getChannels().stream().map(Channel::getUID)
                    .filter(channelUID -> isLinked(channelUID) && !channelUID.getId().equals("timestamp"))
                    .forEach(channelUID -> {
                        State state = getValue(channelUID.getId(), doorWindow);
                        updateState(channelUID, state);

                    });
            updateTimeStamp(doorWindow.getReportTime());
            updateInstallationChannels(doorWindowJSON);
        } else {
            logger.debug("DoorWindow list is empty!");
        }
    }

    public State getValue(String channelId, DoorWindow doorWindow) {
        switch (channelId) {
            case CHANNEL_STATE:
                return "OPEN".equals(doorWindow.getState()) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            case CHANNEL_LOCATION:
                String location = doorWindow.getDevice().getArea();
                return location != null ? new StringType(location) : UnDefType.UNDEF;
        }
        return UnDefType.UNDEF;
    }

    @Override
    public void updateTriggerChannel(String event) {
        logger.debug("DoorWindowThingHandler trigger event {}", event);
        triggerChannel(CHANNEL_DOOR_WINDOW_TRIGGER_CHANNEL, event);
    }
}
