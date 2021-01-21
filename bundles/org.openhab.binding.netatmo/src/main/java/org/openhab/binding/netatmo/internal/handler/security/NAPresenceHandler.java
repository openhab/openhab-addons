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
package org.openhab.binding.netatmo.internal.handler.security;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.doc.NetatmoConstants.PresenceLightMode;
import org.openhab.binding.netatmo.internal.api.home.HomeApi;
import org.openhab.binding.netatmo.internal.api.security.NAWelcome;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.handler.energy.NADescriptionProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * {@link NAPresenceHandler} is the class used to handle Presence camera data
 *
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class NAPresenceHandler extends NACameraHandler {
    private State floodlightAutoModeState = UnDefType.UNDEF;
    private final HomeApi homeApi;

    public NAPresenceHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            TimeZoneProvider timeZoneProvider, NADescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, timeZoneProvider, descriptionProvider);
        this.homeApi = apiBridge.getHomeApi();
    }

    @Override
    public @Nullable State getHandlerProperty(ChannelUID channelUID) {
        if (naThing instanceof NAWelcome) {
            NAWelcome camera = (NAWelcome) naThing;
            String channelId = channelUID.getIdWithoutGroup();
            switch (channelId) {
                case CHANNEL_CAMERA_FLOODLIGHT:
                    return OnOffType.from(camera.getLightModeStatus() == PresenceLightMode.ON);
                case CHANNEL_CAMERA_FLOODLIGHT_AUTO_MODE:
                    // The auto-mode state shouldn't be updated, because this isn't a dedicated information. When the
                    // floodlight is switched on the state within the Netatmo API is "on" and the information if the
                    // previous
                    // state was "auto" instead of "off" is lost... Therefore the binding handles its own auto-mode
                    // state.
                    if (floodlightAutoModeState == UnDefType.UNDEF) {
                        floodlightAutoModeState = OnOffType.from(camera.getLightModeStatus() == PresenceLightMode.AUTO);
                    }
                    return floodlightAutoModeState;
            }
        }
        return super.getHandlerProperty(channelUID);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();
        switch (channelId) {
            case CHANNEL_CAMERA_FLOODLIGHT:
                switchFloodlight(command == OnOffType.ON);
                break;
            case CHANNEL_CAMERA_FLOODLIGHT_AUTO_MODE:
                switchFloodlightAutoMode(command == OnOffType.ON);
                break;
            default:
                super.handleCommand(channelUID, command);
        }
    }

    private void switchFloodlight(boolean isOn) {
        if (isOn) {
            changeFloodlightMode(PresenceLightMode.ON);
        } else {
            switchFloodlightAutoMode(floodlightAutoModeState == OnOffType.ON);
        }
    }

    private void switchFloodlightAutoMode(boolean isAutoMode) {
        floodlightAutoModeState = OnOffType.from(isAutoMode);
        if (isAutoMode) {
            changeFloodlightMode(PresenceLightMode.AUTO);
        } else {
            changeFloodlightMode(PresenceLightMode.OFF);
        }
    }

    private void changeFloodlightMode(PresenceLightMode mode) {
        String localCameraURL = getLocalCameraURL();
        if (localCameraURL != null) {
            tryApiCall(() -> homeApi.changeFloodLightMode(localCameraURL, mode));
        }
    }
}
