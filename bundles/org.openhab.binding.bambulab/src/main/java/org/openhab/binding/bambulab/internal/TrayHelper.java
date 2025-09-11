/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.bambulab.internal;

import static java.lang.Integer.parseInt;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.AmsChannel.*;
import static org.openhab.binding.bambulab.internal.BambuLabBindingConstants.AmsChannel.TrayId.MAX_AMS_TRAYS;
import static org.openhab.core.types.UnDefType.UNDEF;

import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
class TrayHelper {
    static final int MAX_TRAY_VALUE = MAX_AMS * MAX_AMS_TRAYS - 1;
    private static final Logger LOGGER = LoggerFactory.getLogger(TrayHelper.class);

    static Optional<State> findStateForTrayLoaded(@Nullable String tray) {
        if (tray == null) {
            return Optional.empty();
        }
        try {
            var integer = parseInt(tray);
            return Optional.of(parseTrayLoaded(integer));
        } catch (NumberFormatException e) {
            LOGGER.debug("Cannot parse: {}", tray, e);
            return Optional.of(UNDEF);
        }
    }

    private static State parseTrayLoaded(int tray) {
        if (tray < 0) {
            return UNDEF;
        }
        if (tray == 255) {
            return StringType.valueOf("EMPTY");
        }
        if (tray == 254) {
            return StringType.valueOf("VTRAY");
        }
        if (tray > MAX_TRAY_VALUE) {
            LOGGER.warn("There should never be tray with value {}", tray);
            return UNDEF;
        }
        var amsNr = (tray / MAX_AMS) + 1;
        var trayNr = (tray % MAX_AMS_TRAYS) + 1;
        return StringType.valueOf("AMS_%s_%s".formatted(amsNr, trayNr));
    }
}
