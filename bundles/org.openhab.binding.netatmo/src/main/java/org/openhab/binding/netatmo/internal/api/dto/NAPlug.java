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
package org.openhab.binding.netatmo.internal.api.dto;

import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 *         DTO for the NAPlug object.
 *
 */

@NonNullByDefault
public class NAPlug extends NADevice {
    private @Nullable OpenClosedType plugConnectedBoiler;
    private Map<String, @Nullable Integer> lastBilan = Map.of();

    public State getPlugConnectedBoiler() {
        OpenClosedType connected = plugConnectedBoiler;
        return connected != null ? connected : UnDefType.NULL;
    }

    public @Nullable ZonedDateTime getLastBilan() {
        Integer year = lastBilan.get("y");
        Integer month = lastBilan.get("m");
        if (year != null && month != null) {
            return ZonedDateTime.of(year, month, 1, 0, 0, 0, 0, ZonedDateTime.now().getZone())
                    .with(TemporalAdjusters.lastDayOfMonth());
        }
        return null;
    }
}
