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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.PresenceLightMode;
import org.openhab.binding.netatmo.internal.api.dto.NAObject;
import org.openhab.binding.netatmo.internal.api.dto.NAWelcome;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.PresenceChannelHelper;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * {@link PresenceHandler} is the class used to handle Presence camera data
 *
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class PresenceHandler extends CameraHandler {
    public class FloodLightModeHolder {
        public State autoMode = UnDefType.UNDEF;
    }

    private final FloodLightModeHolder modeHolder = new FloodLightModeHolder();

    public PresenceHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            NetatmoDescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, descriptionProvider);
        channelHelpers.stream().filter(c -> c instanceof PresenceChannelHelper).findFirst()
                .map(PresenceChannelHelper.class::cast).ifPresent(helper -> helper.setFloodLightMode(modeHolder));
    }

    @Override
    public void setNewData(NAObject newData) {
        if (newData instanceof NAWelcome && modeHolder.autoMode == UnDefType.UNDEF) {
            // Auto-mode state shouldn't be updated, because this isn't a dedicated information. When the floodlight
            // is switched on the state within the Netatmo API is "on" and the information if the previous state was
            // "auto" instead of "off" is lost... Therefore the binding handles its own auto-mode state.
            NAWelcome camera = (NAWelcome) newData;
            modeHolder.autoMode = OnOffType.from(camera.getLightModeStatus() == PresenceLightMode.AUTO);
        }
        super.setNewData(newData);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getIdWithoutGroup()) {
            case CHANNEL_CAMERA_FLOODLIGHT:
                if (command == OnOffType.ON) {
                    changeFloodlightMode(PresenceLightMode.ON);
                } else {
                    switchFloodlightAutoMode(modeHolder.autoMode == OnOffType.ON);
                }
                break;
            case CHANNEL_CAMERA_FLOODLIGHT_AUTO_MODE:
                switchFloodlightAutoMode(command == OnOffType.ON);
                break;
            default:
                super.handleCommand(channelUID, command);
        }
    }

    private void switchFloodlightAutoMode(boolean isAutoMode) {
        modeHolder.autoMode = OnOffType.from(isAutoMode);
        changeFloodlightMode(isAutoMode ? PresenceLightMode.AUTO : PresenceLightMode.OFF);
    }

    private void changeFloodlightMode(PresenceLightMode mode) {
        CameraAddress camAddress = cameraAddress;
        if (camAddress != null) {
            String localUrl = camAddress.getLocalURL();
            if (localUrl != null) {
                tryApiCall(() -> apiBridge.getHomeApi().changeFloodLightMode(localUrl, mode));
            }
        }
    }
}
