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
import static org.openhab.binding.netatmo.internal.utils.ChannelTypeUtils.toStringType;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link SirenChannelHelper} handles specific behavior of the siren module
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class SirenChannelHelper extends ChannelHelper {

    public SirenChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing naThing, Configuration config) {
        if (naThing instanceof HomeStatusModule) {
            HomeStatusModule homeStatus = (HomeStatusModule) naThing;
            switch (channelId) {
                case CHANNEL_MONITORING:
                    return homeStatus.getMonitoring();
                case CHANNEL_STATUS:
                    return homeStatus.getStatus().map(status -> toStringType(status)).orElse(UnDefType.UNDEF);
            }
        }
        return null;
    }
}
