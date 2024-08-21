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
package org.openhab.binding.satel.internal.event;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.satel.internal.types.IntegraType;

/**
 * Event class describing type and version of connected Integra system.
 *
 * @author Krzysztof Goworek - Initial contribution
 */
@NonNullByDefault
public class IntegraVersionEvent implements SatelEvent {

    private byte type;
    private String version;
    private byte language;
    private boolean settingsInFlash;

    /**
     * Constructs new event class.
     *
     * @param type Integra type
     * @param version string describing version number and firmware revision
     * @param language firmware language: 1 - english
     * @param settingsInFlash settings stored in flash memory
     */
    public IntegraVersionEvent(byte type, String version, byte language, boolean settingsInFlash) {
        this.type = type;
        this.version = version;
        this.language = language;
        this.settingsInFlash = settingsInFlash;
    }

    /**
     * @return Integra type
     * @see IntegraType
     */
    public byte getType() {
        return type;
    }

    /**
     * @return firmware version and date
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return firmware language
     */
    public byte getLanguage() {
        return language;
    }

    /**
     * @return <code>true</code> if configuration is stored in flash memory
     */
    public boolean getSettingsInflash() {
        return this.settingsInFlash;
    }

    @Override
    public String toString() {
        return String.format("IntegraVersionEvent: type = %d, version = %s, language = %d, settingsInFlash = %b",
                this.type & 0xFF, this.version, this.language, this.settingsInFlash);
    }
}
