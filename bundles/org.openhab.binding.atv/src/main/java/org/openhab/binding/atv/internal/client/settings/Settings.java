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
 * Settings container for a single device.
 *
 * <p>
 * Instances are immutable; use the {@code withX} methods to derive updated copies.
 *
 * @param info information related settings (never {@code null})
 * @param protocols protocol specific settings (never {@code null})
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public record Settings(InfoSettings info, ProtocolSettings protocols) {

    /**
     * Creates default settings.
     *
     * @return default settings
     */
    public static Settings ofDefaults() {
        return new Settings(InfoSettings.ofDefaults(), ProtocolSettings.ofDefaults());
    }

    /**
     * Returns a copy with other information settings.
     *
     * @param info new information settings
     * @return updated copy
     */
    public Settings withInfo(InfoSettings info) {
        return new Settings(info, protocols);
    }

    /**
     * Returns a copy with other protocol settings.
     *
     * @param protocols new protocol settings
     * @return updated copy
     */
    public Settings withProtocols(ProtocolSettings protocols) {
        return new Settings(info, protocols);
    }
}
