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
package org.openhab.voice.picotts.internal;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.voice.Voice;

/**
 * Implementation of the Voice interface for PicoTTS
 *
 * @author Florian Schmidt - Initial Contribution
 */
@NonNullByDefault
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
