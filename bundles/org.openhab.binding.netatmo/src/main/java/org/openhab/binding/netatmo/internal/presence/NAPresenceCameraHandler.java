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
package org.openhab.binding.netatmo.internal.presence;

import static org.openhab.binding.netatmo.internal.ChannelTypeUtils.toOnOffType;

import io.swagger.client.model.NAWelcomeCamera;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.netatmo.internal.camera.CameraHandler;

import java.util.Optional;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.CHANNEL_CAMERA_FLOODLIGHT;
import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.CHANNEL_CAMERA_FLOODLIGHT_AUTO_MODE;

/**
 * {@link NAPresenceCameraHandler} is the class used to handle Presence camera data
 *
 * @author Sven Strohschein
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
    protected State getNAThingProperty(@NonNull String channelId) {
        switch (channelId) {
            case CHANNEL_CAMERA_FLOODLIGHT:
                return getFloodlightState();
            case CHANNEL_CAMERA_FLOODLIGHT_AUTO_MODE:
                //The auto-mode state shouldn't be updated, because this isn't a dedicated information. When the
                // floodlight is switched on the state within the Netatmo API is "on" and the information if the previous
                // state was "auto" instead of "off" is lost... Therefore the binding handles its own auto-mode state.
                if (floodlightAutoModeState == UnDefType.UNDEF) {
                    floodlightAutoModeState = getFloodlightAutoModeState();
                }
                return floodlightAutoModeState;
        }
        return super.getNAThingProperty(channelId);
    }

    private State getFloodlightState() {
        if (module != null) {
            final boolean isOn = module.getLightModeStatus() == NAWelcomeCamera.LightModeStatusEnum.ON;
            return toOnOffType(isOn);
        }
        return UnDefType.UNDEF;
    }

    private State getFloodlightAutoModeState() {
        if (module != null) {
            return toOnOffType(module.getLightModeStatus() == NAWelcomeCamera.LightModeStatusEnum.AUTO);
        }
        return UnDefType.UNDEF;
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
            String url = localCameraURL.get()
                    + FLOODLIGHT_SET_URL_PATH
                    + "?config=%7B%22mode%22:%22"
                    + mode.toString()
                    + "%22%7D";
            executeGETRequest(url);
        }
    }
}
