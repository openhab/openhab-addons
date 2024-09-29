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
package org.openhab.voice.voskstt.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link VoskSTTConfiguration} class contains Vosk STT Service configuration.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class VoskSTTConfiguration {
    /**
     * Single phrase mode.
     */
    public boolean singleUtteranceMode = true;
    /**
     * Max seconds to wait to force stop the transcription.
     */
    public int maxTranscriptionSeconds = 60;
    /**
     * Only works when singleUtteranceMode is disabled, max seconds without getting new transcriptions to stop
     * listening.
     */
    public int maxSilenceSeconds = 3;
    /**
     * Message to be told when no results.
     */
    public String noResultsMessage = "Sorry, I didn't understand you";
    /**
     * Message to be told when an error has happened.
     */
    public String errorMessage = "Sorry, something went wrong";
    /**
     * Keep language model loaded
     */
    public boolean preloadModel = true;
}
