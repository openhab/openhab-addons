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
package org.openhab.binding.veluxactive.internal.handler;

import static org.openhab.binding.veluxactive.internal.VeluxActiveBindingConstants.*;

import java.time.DateTimeException;
import java.time.Instant;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.veluxactive.internal.api.dto.Module;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.UnDefType;

/**
 * Shared diagnostics for battery-powered {@link org.openhab.binding.veluxactive.internal.api.ModuleType#INDOOR_SENSOR}
 * and
 * {@link org.openhab.binding.veluxactive.internal.api.ModuleType#DEPARTURE_SWITCH} modules.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public abstract class VeluxActiveBatteryModuleHandler extends VeluxActiveModuleHandler {

    protected VeluxActiveBatteryModuleHandler(Thing thing) {
        super(thing);
    }

    protected void updateDiagnostics(Module module) {
        Integer firmware = module.firmwareRevision;
        if (firmware != null) {
            updateProperty(Thing.PROPERTY_FIRMWARE_VERSION, firmware.toString());
        }
        Integer battery = module.batteryPercent;
        updateState(CHANNEL_BATTERY,
                battery != null && battery >= 0 && battery <= 100 ? new DecimalType(battery) : UnDefType.UNDEF);
        String batteryState = module.batteryState;
        updateState(CHANNEL_LOW_BATTERY, batteryState == null ? UnDefType.UNDEF : switch (batteryState) {
            case "low", "very_low" -> OnOffType.ON;
            case "full", "high", "medium" -> OnOffType.OFF;
            default -> UnDefType.UNDEF;
        });
        String rf = module.rfState;
        updateState(CHANNEL_SIGNAL, rf == null ? UnDefType.UNDEF : switch (rf) {
            case "full" -> new DecimalType(4);
            case "high" -> new DecimalType(3);
            case "medium" -> new DecimalType(2);
            case "low" -> new DecimalType(1);
            case "very_low" -> new DecimalType(0);
            default -> UnDefType.UNDEF;
        });
        Long seen = module.lastSeen;
        try {
            updateState(CHANNEL_LAST_SEEN,
                    seen == null ? UnDefType.UNDEF : new DateTimeType(Instant.ofEpochSecond(seen)));
        } catch (DateTimeException e) {
            updateState(CHANNEL_LAST_SEEN, UnDefType.UNDEF);
        }
    }

    @Override
    protected void clearState() {
        updateState(CHANNEL_BATTERY, UnDefType.UNDEF);
        updateState(CHANNEL_LOW_BATTERY, UnDefType.UNDEF);
        updateState(CHANNEL_SIGNAL, UnDefType.UNDEF);
        updateState(CHANNEL_LAST_SEEN, UnDefType.UNDEF);
    }
}
