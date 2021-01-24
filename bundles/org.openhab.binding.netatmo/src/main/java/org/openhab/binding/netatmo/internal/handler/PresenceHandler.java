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
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.NetatmoDescriptionProvider;
import org.openhab.binding.netatmo.internal.api.ApiBridge;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.PresenceLightMode;
import org.openhab.binding.netatmo.internal.channelhelper.AbstractChannelHelper;
import org.openhab.binding.netatmo.internal.channelhelper.PresenceChannelHelper;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;

/**
 * {@link PresenceHandler} is the class used to handle Presence camera data
 *
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class PresenceHandler extends CameraHandler {
    private final Optional<PresenceChannelHelper> presenceHelper;

    public PresenceHandler(Bridge bridge, List<AbstractChannelHelper> channelHelpers, ApiBridge apiBridge,
            TimeZoneProvider timeZoneProvider, NetatmoDescriptionProvider descriptionProvider) {
        super(bridge, channelHelpers, apiBridge, timeZoneProvider, descriptionProvider);
        presenceHelper = channelHelpers.stream().filter(c -> c instanceof PresenceChannelHelper).findFirst()
                .map(PresenceChannelHelper.class::cast);
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
            switchFloodlightAutoMode(presenceHelper.get().getAutoMode() == OnOffType.ON);
        }
    }

    private void switchFloodlightAutoMode(boolean isAutoMode) {
        presenceHelper.get().setAutoMode(OnOffType.from(isAutoMode));
        changeFloodlightMode(isAutoMode ? PresenceLightMode.AUTO : PresenceLightMode.OFF);
    }

    private void changeFloodlightMode(PresenceLightMode mode) {
        String localCameraURL = getLocalCameraURL();
        if (localCameraURL != null) {
            tryApiCall(() -> apiBridge.getHomeApi().changeFloodLightMode(localCameraURL, mode));
        }
    }
}
