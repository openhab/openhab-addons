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
package org.openhab.binding.netatmo.internal.presence;

import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.toOnOffType;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.camera.CameraHandler;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import io.swagger.client.model.NAWelcomeCamera;

/**
 * {@link NAPresenceCameraHandler} is the class used to handle Presence camera data
 *
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class NAPresenceCameraHandler extends CameraHandler {

    private static final String FLOODLIGHT_SET_URL_PATH = "/command/floodlight_set_config";

    private State floodlightAutoModeState = UnDefType.UNDEF;

    public NAPresenceCameraHandler(final Thing thing, final TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();
        switch (channelId) {
            case CHANNEL_CAMERA_FLOODLIGHT:
                if (command == OnOffType.ON) {
                    switchFloodlight(true);
                } else if (command == OnOffType.OFF) {
                    switchFloodlight(false);
                }
                break;
            case CHANNEL_CAMERA_FLOODLIGHT_AUTO_MODE:
                if (command == OnOffType.ON) {
                    switchFloodlightAutoMode(true);
                } else if (command == OnOffType.OFF) {
                    switchFloodlightAutoMode(false);
                }
                break;
        }
        super.handleCommand(channelUID, command);
    }

    @Override
    protected State getNAThingProperty(String channelId) {
        switch (channelId) {
            case CHANNEL_CAMERA_FLOODLIGHT:
                return getFloodlightState();
            case CHANNEL_CAMERA_FLOODLIGHT_AUTO_MODE:
                // The auto-mode state shouldn't be updated, because this isn't a dedicated information. When the
                // floodlight is switched on the state within the Netatmo API is "on" and the information if the
                // previous
                // state was "auto" instead of "off" is lost... Therefore the binding handles its own auto-mode state.
                if (floodlightAutoModeState == UnDefType.UNDEF) {
                    floodlightAutoModeState = getFloodlightAutoModeState();
                }
                return floodlightAutoModeState;
        }
        return super.getNAThingProperty(channelId);
    }

    private State getFloodlightState() {
        return getModule().map(m -> toOnOffType(m.getLightModeStatus() == NAWelcomeCamera.LightModeStatusEnum.ON))
                .orElse(UnDefType.UNDEF);
    }

    private State getFloodlightAutoModeState() {
        return getModule().map(m -> toOnOffType(m.getLightModeStatus() == NAWelcomeCamera.LightModeStatusEnum.AUTO))
                .orElse(UnDefType.UNDEF);
    }

    private void switchFloodlight(boolean isOn) {
        if (isOn) {
            changeFloodlightMode(NAWelcomeCamera.LightModeStatusEnum.ON);
        } else {
            switchFloodlightAutoMode(floodlightAutoModeState == OnOffType.ON);
        }
    }

    private void switchFloodlightAutoMode(boolean isAutoMode) {
        floodlightAutoModeState = toOnOffType(isAutoMode);
        if (isAutoMode) {
            changeFloodlightMode(NAWelcomeCamera.LightModeStatusEnum.AUTO);
        } else {
            changeFloodlightMode(NAWelcomeCamera.LightModeStatusEnum.OFF);
        }
    }

    private void changeFloodlightMode(NAWelcomeCamera.LightModeStatusEnum mode) {
        Optional<String> localCameraURL = getLocalCameraURL();
        if (localCameraURL.isPresent()) {
            String url = localCameraURL.get() + FLOODLIGHT_SET_URL_PATH + "?config=%7B%22mode%22:%22" + mode.toString()
                    + "%22%7D";
            executeGETRequest(url);

            invalidateParentCacheAndRefresh();
        }
    }
}
