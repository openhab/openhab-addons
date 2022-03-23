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
package org.openhab.binding.netatmo.internal.handler.channelhelper;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.NetatmoConstants.FloodLightMode;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 * The {@link PresenceChannelHelper} handles specific channels of Presence external cameras
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class PresenceChannelHelper extends ChannelHelper {
    private @NonNullByDefault({}) State autoMode;

    public PresenceChannelHelper() {
        super(GROUP_PRESENCE);
    }

    public void setFloodLightMode(State autoMode) {
        this.autoMode = autoMode;
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing naThing, Configuration config) {
        if (naThing instanceof HomeStatusModule) {
            HomeStatusModule camera = (HomeStatusModule) naThing;
            switch (channelId) {
                case CHANNEL_FLOODLIGHT_AUTO_MODE:
                    return autoMode;
                case CHANNEL_FLOODLIGHT:
                    return OnOffType.from(camera.getFloodlight() == FloodLightMode.ON);
            }
        }
        return null;
    }
}
