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
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link PresenceChannelHelper} handle specific behavior
 * of modules using batteries
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class PresenceChannelHelper extends AbstractChannelHelper {
    private State floodlightAutoMode = UnDefType.UNDEF;

    public PresenceChannelHelper(Thing thing, TimeZoneProvider timeZoneProvider) {
        super(thing, timeZoneProvider, Set.of(GROUP_PRESENCE));
    }

    @Override
    protected @Nullable State internalGetProperty(NAThing naThing, String channelId) {
        NAWelcome camera = (NAWelcome) naThing;
        switch (channelId) {
            case CHANNEL_CAMERA_FLOODLIGHT:
                return OnOffType.from(camera.getLightModeStatus() == PresenceLightMode.ON);
            case CHANNEL_CAMERA_FLOODLIGHT_AUTO_MODE:
                // The auto-mode state shouldn't be updated, because this isn't a dedicated information. When the
                // floodlight is switched on the state within the Netatmo API is "on" and the information if the
                // previous
                // state was "auto" instead of "off" is lost... Therefore the binding handles its own auto-mode
                // state.
                if (floodlightAutoMode == UnDefType.UNDEF) {
                    floodlightAutoMode = OnOffType.from(camera.getLightModeStatus() == PresenceLightMode.AUTO);
                }
                return floodlightAutoMode;
        }
        return null;
    }

    public State getAutoMode() {
        return floodlightAutoMode;
    }

    public void setAutoMode(State mode) {
        this.floodlightAutoMode = mode;
    }
}
