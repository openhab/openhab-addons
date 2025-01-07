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
package org.openhab.voice.openaitts.internal;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.voice.Voice;

/**
 * @author Artur Fedjukevits - Initial contribution
 */
@NonNullByDefault
public class OpenAITTSVoice implements Voice {

    private final String label;

    public OpenAITTSVoice(String label) {
        this.label = label;
    }

    /**
     * The unique identifier of the voice, used for internal purposes
     *
     * @return The unique identifier of the voice
     */
    @Override
    public String getUID() {
        return "openaitts:" + label;
    }

    /**
     * The voice label, used for GUI's or VUI's
     *
     * @return The voice label
     */
    @Override
    public String getLabel() {
        return Character.toUpperCase(label.charAt(0)) + label.substring(1);
    }

    /**
     * The locale of the voice
     *
     * @return The locale of the voice
     */
    @Override
    public Locale getLocale() {
        return Locale.ENGLISH;
    }
}
