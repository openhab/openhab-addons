/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.voice.marytts.internal;

import java.util.Locale;

import org.openhab.core.voice.Voice;

/**
 * Implementation of the Voice interface for MaryTTS
 *
 * @author Kelly Davis - Initial contribution and API
 * @author Kai Kreuzer - Refactored to updated APIs and moved to openHAB
 */
public class MaryTTSVoice implements Voice {

    /**
     * Voice locale
     */
    private final Locale locale;

    /**
     * Voice label
     */
    private final String label;

    /**
     * Constructs a MaryTTS Voice for the passed data
     *
     * @param locale The Locale of the voice
     * @param label The label of the voice
     */
    public MaryTTSVoice(Locale locale, String label) {
        this.locale = locale;
        this.label = label;
    }

    /**
     * Globally unique identifier of the voice.
     *
     * @return A String uniquely identifying the voice globally
     */
    @Override
    public String getUID() {
        return "marytts:" + label.replaceAll("[^a-zA-Z0-9_]", "");
    }

    /**
     * The voice label, used for GUI's or VUI's
     *
     * @return The voice label, may not be globally unique
     */
    @Override
    public String getLabel() {
        return label;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Locale getLocale() {
        return locale;
    }
}
