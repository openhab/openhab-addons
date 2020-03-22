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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.verisure.internal.model.VerisureDoorWindows;
import org.openhab.binding.verisure.internal.model.VerisureDoorWindows.DoorWindow;
import org.openhab.binding.verisure.internal.model.VerisureThing;

/**
 * Handler for the Smart Lock Device thing type that Verisure provides.
 *
 * @author Jan Gustafsson - Initial contribution
 *
 */
@NonNullByDefault
public class VerisureDoorWindowThingHandler extends VerisureThingHandler {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Collections.singleton(THING_TYPE_DOORWINDOW);

    public VerisureDoorWindowThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public synchronized void update(@Nullable VerisureThing thing) {
        logger.debug("update on thing: {}", thing);
        if (thing != null && SUPPORTED_THING_TYPES.contains(thing.getThingTypeUID())) {
            updateStatus(ThingStatus.ONLINE);
            VerisureDoorWindows obj = (VerisureDoorWindows) thing;
            updateDoorWindowState(obj);
        } else {
            logger.warn("Can't handle this thing typeuid: {}", getThing().getThingTypeUID());
        }
    }

    private void updateDoorWindowState(VerisureDoorWindows doorWindowJSON) {
        DoorWindow doorWindow = doorWindowJSON.getData().getInstallation().getDoorWindows().get(0);

        getThing().getChannels().stream().map(Channel::getUID)
                .filter(channelUID -> isLinked(channelUID) && !channelUID.getId().equals("timestamp"))
                .forEach(channelUID -> {
                    State state = getValue(channelUID.getId(), doorWindow);
                    updateState(channelUID, state);

                });
        updateTimeStamp(doorWindow.getReportTime());
        super.update(doorWindowJSON);
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
}
