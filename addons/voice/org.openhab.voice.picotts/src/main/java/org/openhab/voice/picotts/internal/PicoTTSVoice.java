/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.voice.picotts.internal;

import java.util.Locale;

import org.eclipse.smarthome.core.voice.Voice;

/**
 * Implementation of the Voice interface for PicoTTS
 *
 * @author Florian Schmidt - Initial Contribution
 */
public class PicoTTSVoice implements Voice {
    private final String languageTag;

    public PicoTTSVoice(String languageTag) {
        this.languageTag = languageTag;
    }

    @Override
    public String getUID() {
        return "picotts:" + languageTag.replaceAll("[^a-zA-Z0-9_]", "");
    }

    @Override
    public String getLabel() {
        return languageTag;
    }

    @Override
    public Locale getLocale() {
        return Locale.forLanguageTag(languageTag);
    }
}
