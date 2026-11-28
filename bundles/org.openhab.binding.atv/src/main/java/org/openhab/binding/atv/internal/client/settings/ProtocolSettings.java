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
package org.openhab.binding.atv.internal.client.settings;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Container for protocol specific settings. DMAP is not supported.
 *
 * @param airplay AirPlay settings (never {@code null})
 * @param companion Companion settings (never {@code null})
 * @param mrp MRP settings (never {@code null})
 * @param raop RAOP settings (never {@code null})
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record ProtocolSettings(AirPlaySettings airplay, CompanionSettings companion, MrpSettings mrp,
        RaopSettings raop) {

    /**
     * Creates default settings for all protocols.
     *
     * @return default settings
     */
    public static ProtocolSettings ofDefaults() {
        return new ProtocolSettings(AirPlaySettings.ofDefaults(), CompanionSettings.ofDefaults(),
                MrpSettings.ofDefaults(), RaopSettings.ofDefaults());
    }

    /**
     * Returns a copy with other AirPlay settings.
     *
     * @param airplay new AirPlay settings
     * @return updated copy
     */
    public ProtocolSettings withAirplay(AirPlaySettings airplay) {
        return new ProtocolSettings(airplay, companion, mrp, raop);
    }

    /**
     * Returns a copy with other Companion settings.
     *
     * @param companion new Companion settings
     * @return updated copy
     */
    public ProtocolSettings withCompanion(CompanionSettings companion) {
        return new ProtocolSettings(airplay, companion, mrp, raop);
    }

    /**
     * Returns a copy with other MRP settings.
     *
     * @param mrp new MRP settings
     * @return updated copy
     */
    public ProtocolSettings withMrp(MrpSettings mrp) {
        return new ProtocolSettings(airplay, companion, mrp, raop);
    }

    /**
     * Returns a copy with other RAOP settings.
     *
     * @param raop new RAOP settings
     * @return updated copy
     */
    public ProtocolSettings withRaop(RaopSettings raop) {
        return new ProtocolSettings(airplay, companion, mrp, raop);
    }
}
