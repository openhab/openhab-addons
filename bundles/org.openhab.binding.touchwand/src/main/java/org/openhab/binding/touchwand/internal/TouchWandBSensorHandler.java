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
package org.openhab.binding.touchwand.internal;

import static org.openhab.binding.touchwand.internal.TouchWandBindingConstants.*;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.touchwand.internal.dto.TouchWandBSensorUnitData;
import org.openhab.binding.touchwand.internal.dto.TouchWandUnitData;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;

/**
 * The {@link TouchWandBSensorHandler} is responsible for handling command for Binary Sensor unit
 *
 * @author Roie Geron - Initial contribution
 */
@NonNullByDefault
public class TouchWandBSensorHandler extends TouchWandBaseUnitHandler {

    private boolean isFirstUpdateTouchWandUnitState = true;

    public TouchWandBSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    void updateTouchWandUnitState(TouchWandUnitData unitData) {
        if (unitData instanceof TouchWandBSensorUnitData) {
            if (isFirstUpdateTouchWandUnitState) {
                removeUnsupportedChannels((TouchWandBSensorUnitData) unitData);
                isFirstUpdateTouchWandUnitState = false;
            }
            String sensorSubType = ((TouchWandBSensorUnitData) unitData).getIdData().getSubType();
            switch (sensorSubType) {
                case BSENSOR_SUBTYPE_DOORWINDOW:
                    updateChannelDoorWindow((TouchWandBSensorUnitData) unitData);
                    break;
                case BSENSOR_SUBTYPE_MOTION:
                    updateChannelMotion((TouchWandBSensorUnitData) unitData);
                    break;
                case BSENSOR_SUBTYPE_SMOKE:
                    updateChannelSmoke((TouchWandBSensorUnitData) unitData);
                    break;
                default:
            }
        } else {
            logger.warn("updateTouchWandUnitState incompatible TouchWandUnitData instance");
        }
    }

    @Override
    void touchWandUnitHandleCommand(Command command) {
    }

    void updateChannelDoorWindow(TouchWandBSensorUnitData unitData) {
        OpenClosedType myOpenClose;
        String isOpen = unitData.getCurrStatus();
        logger.debug("recieved status {} from door unit {} ", isOpen, unitData.getName());
        if (isOpen.equals(BSENSOR_STATUS_OPEN)) {
            myOpenClose = OpenClosedType.OPEN;
        } else if (isOpen.equals(BSENSOR_STATUS_CLOSE)) {
            myOpenClose = OpenClosedType.CLOSED;
        } else {
            logger.debug("TouchWandBSensorUnitData illegal update value {}", isOpen);
            return;
        }
        updateState(CHANNEL_DOORWINDOW, myOpenClose);
    }

    void updateChannelMotion(TouchWandBSensorUnitData unitData) {
        String motion = unitData.getCurrStatus();
        logger.debug("recieved status {} from motion unit {} ", motion, unitData.getName());
        OnOffType status;
        if (motion.equals(BSENSOR_STATUS_OPEN)) {
            status = OnOffType.ON;
        } else if (motion.equals(BSENSOR_STATUS_CLOSE)) {
            status = OnOffType.OFF;
        } else {
            logger.debug("TouchWandBSensorUnitData illegal update value {}", motion);
            return;
        }
        updateState(CHANNEL_MOTION, status);
    }

    void updateChannelSmoke(TouchWandBSensorUnitData unitData) {
        String hasSmoke = unitData.getCurrStatus();
        OnOffType status;
        if (hasSmoke.equals(BSENSOR_STATUS_OPEN)) {
            status = OnOffType.ON;
        } else if (hasSmoke.equals(BSENSOR_STATUS_CLOSE)) {
            status = OnOffType.OFF;
        } else {
            logger.debug("TouchWandBSensorUnitData illegal update value {}", hasSmoke);
            return;
        }
        updateState(CHANNEL_SMOKE, status);
    }

    void removeUnsupportedChannels(TouchWandBSensorUnitData unitData) {
        ArrayList<Channel> toBeRemovedChannels = new ArrayList<>(thing.getChannels());
        String sensorSubType = unitData.getIdData().getSubType();
        switch (sensorSubType) {
            case BSENSOR_SUBTYPE_DOORWINDOW:
                toBeRemovedChannels.remove(thing.getChannel(CHANNEL_DOORWINDOW));
                break;
            case BSENSOR_SUBTYPE_MOTION:
                toBeRemovedChannels.remove(thing.getChannel(CHANNEL_MOTION));
                break;
            case BSENSOR_SUBTYPE_SMOKE:
                Channel channel = thing.getChannel(CHANNEL_SMOKE);
                toBeRemovedChannels.remove(channel);
                break;
        }

        ThingBuilder thingBuilder = editThing();
        thingBuilder.withoutChannels(toBeRemovedChannels);
        updateThing(thingBuilder.build());
    }
}
