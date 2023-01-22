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
package org.openhab.voice.watsonstt.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link WatsonSTTConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class WatsonSTTConfiguration {

    /**
     * Api key for Speech-to-Text instance created on IBM Cloud.
     */
    public String apiKey = "";
    /**
     * Url for Speech-to-Text instance created on IBM Cloud.
     */
    public String instanceUrl = "";
    /**
     * Prefer multimedia to telephony models. Multimedia models are intended for audio that has a minimum sampling rate
     * of 16 kHz, while telephony models are intended for audio that has a minimum sampling rate of 8 kHz.
     */
    public boolean preferMultimediaModel = true;
    /**
     * Use the parameter to suppress side conversations or background noise.
     */
    public float backgroundAudioSuppression = 0f;
    /**
     * Use the parameter to suppress word insertions from music, coughing, and other non-speech events.
     */
    public float speechDetectorSensitivity = 0.5f;
    /**
     * If true, the service converts dates, times, series of digits and numbers, phone numbers, currency values, and
     * internet addresses into more readable.
     */
    public boolean smartFormatting = false;
    /**
     * If true, the service redacts, or masks, numeric data from final transcripts.
     */
    public boolean redaction = false;
    /**
     * Single phrase mode.
     */
    public boolean singleUtteranceMode = true;
    /**
     * max seconds without getting new transcriptions to stop listening.
     */
    public int maxSilenceSeconds = 3;
    /**
     * Message to be told when no results
     */
    public String noResultsMessage = "No results";
    /**
     * By default, all IBM Watson™ services log requests and their results. Logging is done only to improve the services
     * for future users. The logged data is not shared or made public.
     */
    public boolean optOutLogging = true;
}
