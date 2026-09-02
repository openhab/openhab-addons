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
package org.openhab.voice.pipertts.internal;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The {@link PiperVoiceConfig} class is a Data Transfer Object for Piper voice configuration from .onnx.json.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
@JsonIgnoreProperties(ignoreUnknown = true)
class PiperVoiceConfig {
    public String dataset = "";
    public Audio audio = new Audio();
    public Language language = new Language();

    @JsonProperty("num_speakers")
    public int numSpeakers = 1;

    @JsonProperty("speaker_id_map")
    public @Nullable Map<String, Integer> speakerIdMap;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Audio {
        public String quality = "";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Language {
        public String family = "";
        public String region = "";
    }
}
