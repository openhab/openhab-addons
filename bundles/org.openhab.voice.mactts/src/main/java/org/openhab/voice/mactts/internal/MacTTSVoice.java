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
package org.openhab.voice.mactts.internal;

import java.util.Locale;
import java.util.StringTokenizer;

import org.openhab.core.voice.Voice;

/**
 * Implementation of the Voice interface for macOS
 *
 * @author Kelly Davis - Initial contribution and API
 */
public class MacTTSVoice implements Voice {

    /**
     * Voice label
     */
    private String label;

    /**
     * Voice language (ISO 639 alpha-2)
     */
    private String language;

    /**
     * Voice country (ISO 3166 alpha-2)
     */
    private String country;

    /**
     * Voice variant
     */
    private String variant;

    /**
     * Constructs a MacTTSVoice instance corresponding to a single line
     * returned from a call to the command
     *
     * {@code 'say -v ?'}
     *
     * For example, a single line from the call above could have the form
     *
     * {@code Agnes   en_US       # Isn't it nice to have a computer that will talk to you?}
     *
     * Generically, a single line from the call above has the form
     *
     * {@code  <Label>  <Locale>  # <Sentence>}
     *
     * where <Label> is the voice name (which may contain spaces), <Locale>
     * is the locale ISO 639 alpha-2 + "_" + ISO 3166 alpha-2 and <Sentence>
     * is an example sentence in <Locale>.
     *
     * @param line Line from a 'say -v ?' call.
     */
    public MacTTSVoice(String line) {
        // Default to null's
        this.label = null;
        this.country = null;
        this.variant = null;
        this.language = null;

        // Parse line into tokens
        StringTokenizer stringTokenizer = new StringTokenizer(line);
        while (stringTokenizer.hasMoreTokens()) {
            // Get next token
            String token = stringTokenizer.nextToken();

            // Ignore <Sentence>
            if (token.startsWith("#")) {
                break;
            }

            // Check that we are parsing <Label> or <Locale> for Scotland
            int underscore = token.indexOf('_');
            if (-1 == underscore) {
                // Check we're dealing with <Label>
                if (!token.equals("en-scotland")) {
                    if (null == this.label) {
                        this.label = token;
                    } else {
                        this.label = this.label + " " + token;
                    }
                } else { // Else we're dealnig with <Locale> for Scotland
                    this.language = "en";
                    this.country = "GB";
                    this.variant = "scotland";
                }
            } else { // Parse non-Scottish <Locale>
                this.language = token.substring(0, underscore);
                this.country = token.substring(underscore + 1);
            }
        }
    }

    /**
     * Globally unique identifier of the voice.
     *
     * @return A String uniquely identifying the voice globally
     */
    @Override
    public String getUID() {
        return "mactts:" + label.replaceAll("[^a-zA-Z0-9_]", "");
    }

    /**
     * The voice label, used for GUIs
     *
     * @return The voice label, may not be globally unique
     */
    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Locale getLocale() {
        Locale locale;

        if (variant != null) {
            locale = new Locale(language, country, variant);
        } else if (country != null) {
            locale = new Locale(language, country);
        } else {
            locale = new Locale(language);
        }

        return locale;
    }
}
