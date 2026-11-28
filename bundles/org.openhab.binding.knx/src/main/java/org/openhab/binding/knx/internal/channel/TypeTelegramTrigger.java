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
package org.openhab.binding.knx.internal.channel;

import static org.openhab.binding.knx.internal.KNXBindingConstants.CHANNEL_TELEGRAM_TRIGGER;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.Type;

/**
 * Trigger channel that fires for every GroupValueWrite to its group addresses. It has no item type and no default
 * data type: without a data type in the configuration the event carries the raw bytes.
 * <p>
 * The base class needs a non-empty list of accepted types, takes its first entry as the preferred type and warns
 * when a configured data type allows none of them. The list holds {@link Type} first, so a value decodes to the
 * type the binding gives a data type by default, then every type the binding maps a data type to, so that no valid
 * data type is reported as incompatible.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class TypeTelegramTrigger extends KNXChannel {
    public static final Set<String> SUPPORTED_CHANNEL_TYPES = Set.of(CHANNEL_TELEGRAM_TRIGGER);

    static final List<Class<? extends Type>> ACCEPTED_TYPES = List.of(Type.class, OnOffType.class, DecimalType.class,
            QuantityType.class, StringType.class, DateTimeType.class, HSBType.class, PercentType.class,
            IncreaseDecreaseType.class, UpDownType.class, OpenClosedType.class, StopMoveType.class);

    TypeTelegramTrigger(Channel channel) {
        super(ACCEPTED_TYPES, channel);
    }

    @Override
    protected String getDefaultDPT(String gaConfigKey) {
        return "";
    }
}
