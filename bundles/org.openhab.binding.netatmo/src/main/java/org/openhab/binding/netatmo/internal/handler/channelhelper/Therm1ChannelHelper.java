/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.netatmo.internal.api.data.EventType;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.dto.HomeStatusModule;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.api.dto.WebhookExtraParams;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.State;

/**
 * The {@link Therm1ChannelHelper} handles specific behavior of the thermostat module
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class Therm1ChannelHelper extends ChannelHelper {

    public Therm1ChannelHelper(Set<String> providedGroups) {
        super(providedGroups);
    }

    @Override
    protected @Nullable State internalGetProperty(String channelId, NAThing naThing, Configuration config) {
        return (naThing instanceof HomeStatusModule homeStatusModule && CHANNEL_THERM_RELAY.equals(channelId))
                ? homeStatusModule.getBoilerStatus()
                : null;
    }

    @Override
    protected @Nullable State internalGetExtraParams(String channelId, @Nullable String groupId,
            WebhookExtraParams extraParams) {
        if (extraParams.deviceType.equals(ModuleType.PLUG) && extraParams.eventType.equals(EventType.BOILER_EVENT)
                && extraParams.boilerStatus instanceof String boilerStatus) {
            if (GROUP_PROPERTIES.equals(groupId) && CHANNEL_THERM_RELAY.equals(channelId)) {
                return "boiler_off".equals(boilerStatus) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            }
        }
        return null;
    }
}
