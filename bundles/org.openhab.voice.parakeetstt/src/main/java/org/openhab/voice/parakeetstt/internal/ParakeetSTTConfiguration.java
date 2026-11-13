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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ParakeetSTTConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Florian Hotze - Initial contribution
 */
@NonNullByDefault
public class ParakeetSTTConfiguration {

    public enum Mode {
        AUTO,
        OFFLINE,
        STREAMING
    }

    public enum Decoder {
        DEFAULT,
        CTC,
        TRANSDUCER
    }

    // Model
    public String modelName = "";
    public boolean preloadModel = false;

    // Transcription Behavior
    public Mode mode = Mode.AUTO;
    public Decoder decoder = Decoder.DEFAULT;
    public String language = "";
    public boolean singleUtteranceMode = true;

    // Audio Stream & Stopping Limits
    public double stepSeconds = 0.1;
    public int maxSeconds = 10;
    public double initSilenceSeconds = 5.0;
    public double maxSilenceSeconds = 1.0;

    // Voice Activity Detection
    public String vadModelName = "";
    public double vadThreshold = 0.5;
    public double minSpeechSeconds = 0.25;
    public double speechPadSeconds = 0.03;

    // CPU Performance
    public int threads = 0;
    public int backends = 1;
}
