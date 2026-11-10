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
package org.openhab.binding.netatmo.internal.handler.capability;

import static org.openhab.binding.netatmo.internal.NetatmoBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.netatmo.internal.api.data.EventType;
import org.openhab.binding.netatmo.internal.api.data.ModuleType;
import org.openhab.binding.netatmo.internal.api.dto.WebhookExtraParams;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.core.library.types.OpenClosedType;

/**
 * {@link Therm1Capability} gives the ability to handle NAPlug specifics
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class Therm1Capability extends WebhookListenerCapability {
    public Therm1Capability(CommonInterface handler) {
        super(handler, EventType.BOILER_EVENT);
    }

    @Override
    public void pushData(WebhookExtraParams newData) {
        if (newData.deviceType.equals(ModuleType.PLUG) && newData.eventType.equals(EventType.BOILER_EVENT)
                && newData.boilerStatus instanceof String boilerStatus) {
            handler.updateState(GROUP_PROPERTIES, CHANNEL_THERM_RELAY,
                    "boiler_off".equals(boilerStatus) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        }
    }
}
