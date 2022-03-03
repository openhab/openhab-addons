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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FloodLightMode;
import org.openhab.binding.netatmo.internal.api.dto.NAHomeStatusModule;
import org.openhab.binding.netatmo.internal.handler.NACommonInterface;
import org.openhab.binding.netatmo.internal.handler.channelhelper.ChannelHelper;
import org.openhab.binding.netatmo.internal.handler.channelhelper.PresenceChannelHelper;
import org.openhab.binding.netatmo.internal.providers.NetatmoDescriptionProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * {@link PresenceCapability} give to handle Presence Camera specifics
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class PresenceCapability extends CameraCapability {
    private State autoMode = UnDefType.UNDEF;

    public PresenceCapability(NACommonInterface handler, NetatmoDescriptionProvider descriptionProvider,
            List<ChannelHelper> channelHelpers) {
        super(handler, descriptionProvider, channelHelpers);
        channelHelpers.stream().filter(c -> c instanceof PresenceChannelHelper).findFirst()
                .map(PresenceChannelHelper.class::cast).ifPresent(helper -> helper.setFloodLightMode(autoMode));
    }

    @Override
    public void updateHomeStatusModule(NAHomeStatusModule newData) {
        super.updateHomeStatusModule(newData);
        if (autoMode == UnDefType.UNDEF) {
            // Auto-mode state shouldn't be updated, because this isn't a dedicated information. When the floodlight
            // is switched on the state within the Netatmo API is "on" and the information if the previous state was
            // "auto" instead of "off" is lost... Therefore the binding handles its own auto-mode state.
            // TODO : this will have to be controlled by a Presence owner against the new api usage of HomeStatus
            // apparently now AUTO is part of the answer.
            NAHomeStatusModule camera = newData;
            autoMode = OnOffType.from(camera.getFloodlight() == FloodLightMode.AUTO);
        }
    }

    @Override
    public void handleCommand(String channelName, Command command) {
        if (command instanceof OnOffType && CHANNEL_MONITORING.equals(channelName)) {
            switch (channelName) {
                case CHANNEL_FLOODLIGHT:
                    if (command == OnOffType.ON) {
                        changeFloodlightMode(FloodLightMode.ON);
                    } else {
                        switchFloodlightAutoMode(autoMode == OnOffType.ON);
                    }
                    return;
                case CHANNEL_FLOODLIGHT_AUTO_MODE:
                    switchFloodlightAutoMode(command == OnOffType.ON);
                    return;
            }
        } else {
            super.handleCommand(channelName, command);
        }
    }

    private void switchFloodlightAutoMode(boolean isAutoMode) {
        autoMode = OnOffType.from(isAutoMode);
        changeFloodlightMode(isAutoMode ? FloodLightMode.AUTO : FloodLightMode.OFF);
    }

    private void changeFloodlightMode(FloodLightMode mode) {
        handler.getHomeCapability(SecurityCapability.class).ifPresent(cap -> cap.changeFloodlightMode(localUrl, mode));
    }
}
