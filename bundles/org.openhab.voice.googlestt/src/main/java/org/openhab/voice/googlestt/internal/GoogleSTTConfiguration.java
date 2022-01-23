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
package org.openhab.voice.googlestt.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GoogleSTTConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class GoogleSTTConfiguration {
    /**
     * Google Cloud Client ID, needs Speech To Text API enabled
     */
    public String clientId = "";
    /**
     * Google Cloud Client Secret
     */
    public String clientSecret = "";
    /**
     * Code for obtain oauth access token
     */
    public String oauthCode = "";
    /**
     * Message to be told when no results.
     */
    public String noResultsMessage = "";
    /**
     * Message to be told when an error has happened.
     */
    public String errorMessage = "";
    /**
     * Max seconds to wait to force stop the transcription.
     */
    public int maxTranscriptionSeconds = 60;
    /**
     * Only works when singleUtteranceMode is disabled, max seconds without getting new transcriptions to stop
     * listening.
     */
    public int maxSilenceSeconds = 5;
    /**
     * Single phrase mode.
     */
    public boolean singleUtteranceMode = true;
    /**
     * Try loading supported locales from the documentation page.
     */
    public boolean refreshSupportedLocales = false;
}
