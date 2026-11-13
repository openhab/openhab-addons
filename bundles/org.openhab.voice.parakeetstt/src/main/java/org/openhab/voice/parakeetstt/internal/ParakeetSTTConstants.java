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
package org.openhab.voice.parakeetstt.internal;

import java.nio.file.Path;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;
import org.openhab.core.audio.AudioFormat;

/**
 * The {@link ParakeetSTTConstants} class defines common constants used across the Parakeet Speech-to-Text add-on.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class ParakeetSTTConstants {
    // OSGi / openHAB service constants
    public static final String SERVICE_NAME = "Parakeet";
    public static final String SERVICE_ID = "parakeetstt";
    public static final String SERVICE_CATEGORY = "voice";
    public static final String SERVICE_PID = "org.openhab." + SERVICE_CATEGORY + "." + SERVICE_ID;
    public static final String CONFIG_URI = SERVICE_CATEGORY + ":" + SERVICE_ID;

    // Parakeet STT configuration parameter names
    public static final String PARAM_MODEL_NAME = "modelName";
    public static final String PARAM_VAD_MODEL_NAME = "vadModelName";

    public static final Path PARAKEET_FOLDER = Path.of(OpenHAB.getUserDataFolder(), "parakeet");
    public static final String MODEL_EXTENSION = ".gguf";
    public static final String BUNDLE_VAD_COMPONENT = "vad";

    public static final String THREAD_POOL = "ParakeetSTT";

    public static final int SAMPLE_RATE = 16000;
    public static final AudioFormat AUDIO_FORMAT_PCM = new AudioFormat(AudioFormat.CONTAINER_NONE,
            AudioFormat.CODEC_PCM_SIGNED, false, 16, null, (long) SAMPLE_RATE, 1);
    public static final AudioFormat AUDIO_FORMAT_WAVE = new AudioFormat(AudioFormat.CONTAINER_WAVE,
            AudioFormat.CODEC_PCM_SIGNED, false, 16, null, (long) SAMPLE_RATE, 1);
}
