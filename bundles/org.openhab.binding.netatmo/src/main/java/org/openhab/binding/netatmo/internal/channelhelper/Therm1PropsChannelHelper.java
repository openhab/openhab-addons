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
import org.openhab.binding.netatmo.internal.api.dto.NAThermostat;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;

/**
 * The {@link Therm1PropsChannelHelper} handle specific behavior
 * of the thermostat module
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class Therm1PropsChannelHelper extends AbstractChannelHelper {

    public Therm1PropsChannelHelper() {
        super(Set.of(GROUP_TH_PROPERTIES));
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing naThing) {
        if (naThing instanceof NAThermostat) {
            NAThermostat thermostat = (NAThermostat) naThing;
            switch (channelId) {
                case CHANNEL_THERM_RELAY:
                    return OnOffType.from(thermostat.getBoilerStatus());
                case CHANNEL_ANTICIPATING:
                    return OnOffType.from(thermostat.isAnticipating());
            }
        }
        return null;
    }
}
