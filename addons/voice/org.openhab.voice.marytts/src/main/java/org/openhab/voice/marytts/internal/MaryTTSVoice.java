/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.marytts.internal;

import java.util.Locale;

import org.eclipse.smarthome.core.voice.Voice;

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
        return this.label;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Locale getLocale() {
        return this.locale;
    }
}
