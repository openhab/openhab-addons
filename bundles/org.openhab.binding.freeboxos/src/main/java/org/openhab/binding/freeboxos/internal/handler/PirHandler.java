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
package org.openhab.binding.freeboxos.internal.handler;

import static org.openhab.binding.freeboxos.internal.FreeboxOsBindingConstants.*;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.Endpoint;
import org.openhab.binding.freeboxos.internal.api.rest.HomeManager.EndpointState;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link PirHandler} is responsible for handling everything associated to
 * any Freebox Home PIR motion detection thing type.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class PirHandler extends HomeNodeHandler {

    public PirHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected State getChannelState(String channelId, EndpointState state, Optional<Endpoint> endPoint) {
        if (PIR_TAMPER_UPDATE.equals(channelId) || PIR_TRIGGER_UPDATE.equals(channelId)) {
            return Objects.requireNonNull(endPoint.map(ep -> ep.getLastChange()
                    .map(change -> (State) new DateTimeType(
                            ZonedDateTime.ofInstant(Instant.ofEpochSecond(change.timestamp()), ZoneOffset.UTC)))
                    .orElse(UnDefType.UNDEF)).orElse(UnDefType.UNDEF));
        }

        String value = state.value();

        if (value == null) {
            return UnDefType.NULL;
        }

        return switch (channelId) {
            case NODE_BATTERY -> DecimalType.valueOf(value);
            case PIR_TAMPER -> state.asBoolean() ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            case PIR_TRIGGER -> OnOffType.from(value);
            default -> UnDefType.NULL;
        };
    }
}
