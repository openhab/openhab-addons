/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.smartthings.internal.statehandler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.handler.SmartThingsThingHandler;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Base converter class.
 * The converter classes are responsible for converting "state" messages from SmartThings into openHAB States.
 * And, converting handler.handleCommand() into messages to be sent to SmartThings
 *
 * @author Laurent Arnal - Initial contribution
 *
 */
@NonNullByDefault
public class SmartThingsStateHandlerLight extends SmartThingsStateHandler {
    SmartThingsStateHandlerLight() {
    }

    @Override
    public void handleStateChange(ThingUID thingUID, ChannelUID channelUID, String deviceType, String componentId,
            State state, SmartThingsThingHandler thingHandler) {
        String channelName = getChannelName(channelUID);

        super.putState(thingUID, channelName, state);

        State oldHueState = getState(thingUID, SmartThingsBindingConstants.CHANNEL_NAME_HUE);
        State oldSaturationState = getState(thingUID, SmartThingsBindingConstants.CHANNEL_NAME_SATURATION);
        State oldLevelState = getState(thingUID, SmartThingsBindingConstants.CHANNEL_NAME_LEVEL);

        if (oldHueState == UnDefType.UNDEF) {
            oldHueState = new DecimalType(0);
        }

        if (oldSaturationState == UnDefType.UNDEF) {
            oldSaturationState = new PercentType(0);
        }

        if (oldLevelState == UnDefType.UNDEF) {
            oldLevelState = new PercentType(0);
        }

        String groupId = deviceType + "_" + componentId + "_" + "colorControl";

        ChannelUID channelUIDColor = new ChannelUID(thingHandler.getThing().getUID(), groupId,
                SmartThingsBindingConstants.CHANNEL_NAME_COLOR);

        if (SmartThingsBindingConstants.CHANNEL_NAME_HUE.equals(channelName)) {
            putState(thingUID, SmartThingsBindingConstants.CHANNEL_NAME_HUE, state);
            HSBType newColorState = new HSBType((DecimalType) state, (PercentType) oldSaturationState,
                    (PercentType) oldLevelState);

            thingHandler.sendUpdateState(channelUIDColor, newColorState);
        }
        if (SmartThingsBindingConstants.CHANNEL_NAME_SATURATION.equals(channelName)) {
            PercentType pcState = convToPercentTypeIfNeed(state);
            putState(thingUID, SmartThingsBindingConstants.CHANNEL_NAME_SATURATION, pcState);
            HSBType newColorState = new HSBType((DecimalType) oldHueState, pcState, (PercentType) oldLevelState);

            thingHandler.sendUpdateState(channelUIDColor, newColorState);
        }
        if (SmartThingsBindingConstants.CHANNEL_NAME_LEVEL.equals(channelName)) {
            PercentType pcState = convToPercentTypeIfNeed(state);
            putState(thingUID, SmartThingsBindingConstants.CHANNEL_NAME_LEVEL, pcState);
            HSBType newColorState = new HSBType((DecimalType) oldHueState, (PercentType) oldSaturationState, pcState);

            thingHandler.sendUpdateState(channelUIDColor, newColorState);
        }
    }

    private String getChannelName(ChannelUID channelUID) {
        String result = channelUID.getIdWithoutGroup();
        return result;
    }
}
