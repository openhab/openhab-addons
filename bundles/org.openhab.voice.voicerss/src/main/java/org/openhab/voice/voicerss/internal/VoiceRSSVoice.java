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
package org.openhab.voice.voicerss.internal;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.voice.Voice;
import org.openhab.voice.voicerss.internal.cloudapi.VoiceRSSCloudImpl;

/**
 * Implementation of the Voice interface for VoiceRSS. Label is only "default"
 * as only voice supported.
 *
 * @author Jochen Hiller - Initial contribution and API
 */
@NonNullByDefault
public class VoiceRSSVoice implements Voice {

    /**
     * Voice locale
     */
    private final Locale locale;

    /**
     * Voice label
     */
    private final String label;

    /**
     * Constructs a VoiceRSS Voice for the passed data
     *
     * @param locale
     *            The Locale of the voice
     * @param label
     *            The label of the voice
     */
    public VoiceRSSVoice(Locale locale, String label) {
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
        String uid = "voicerss:" + locale.toLanguageTag().replaceAll("[^a-zA-Z0-9_]", "");
        if (!label.equals(VoiceRSSCloudImpl.DEFAULT_VOICE)) {
            uid += "_" + label.replaceAll("[^a-zA-Z0-9_]", "");
        }
        return uid;
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

    // automatically inherit doc
    @Override
    public Locale getLocale() {
        return locale;
    }
}
