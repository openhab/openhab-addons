/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.toStringType;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link PresenceChannelHelper} handles specific channels of Presence external cameras
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class PresenceChannelHelper extends CameraChannelHelper {

    public PresenceChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing naThing, Configuration config) {
        if (naThing instanceof HomeStatusModule presence) {
            switch (channelId) {
                case CHANNEL_FLOODLIGHT:
                    return toStringType(presence.getFloodlight());
                case CHANNEL_SIREN:
                    switch (presence.getSirenStatus()) {
                        case NO_SOUND:
                            return OnOffType.OFF;
                        case SOUND:
                            return OnOffType.ON;
                        case UNKNOWN:
                            return UnDefType.NULL;
                    }
            }
        }
        return super.internalGetProperty(channelId, naThing, config);
    }
}
