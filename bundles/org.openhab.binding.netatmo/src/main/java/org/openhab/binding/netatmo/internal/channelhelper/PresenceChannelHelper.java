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
package org.openhab.binding.netatmo.internal.channelhelper;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.NetatmoConstants.PresenceLightMode;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.api.dto.NAWelcome;
import org.openhab.binding.netatmo.internal.handler.PresenceHandler.FloodLightModeHolder;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 * The {@link PresenceChannelHelper} handle specific behavior
 * of Presence external cameras
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class PresenceChannelHelper extends AbstractChannelHelper {
    private @NonNullByDefault({}) FloodLightModeHolder modeHolder;

    public PresenceChannelHelper() {
        super(Set.of(GROUP_PRESENCE));
    }

    public void setFloodLightMode(FloodLightModeHolder modeHolder) {
        this.modeHolder = modeHolder;
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing naThing) {
        if (naThing instanceof NAWelcome) {
            switch (channelId) {
                case CHANNEL_CAMERA_FLOODLIGHT_AUTO_MODE:
                    return modeHolder.autoMode;
                case CHANNEL_CAMERA_FLOODLIGHT:
                    NAWelcome camera = (NAWelcome) naThing;
                    return OnOffType.from(camera.getLightModeStatus() == PresenceLightMode.ON);
            }
        }
        return null;
    }
}
