/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
     * The time in seconds after which, if only silence (no speech) is detected in the audio, the connection is closed.
     */
    public int inactivityTimeout = 3;
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
