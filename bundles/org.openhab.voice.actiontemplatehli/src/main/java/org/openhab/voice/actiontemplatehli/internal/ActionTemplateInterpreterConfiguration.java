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
package org.openhab.voice.actiontemplatehli.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ActionTemplateInterpreterConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class ActionTemplateInterpreterConfiguration {
    /**
     * Convert the input text to lower case before processing
     */
    public boolean lowerText = false;
    /**
     * Enable case sensitivity for pos and ner static values.
     */
    public boolean caseSensitive = false;
    /**
     * Message for successful command
     */
    public String commandSentMessage = "Done";
    /**
     * Message for unsuccessful processing
     */
    public String unhandledMessage = "I can not do that";
    /**
     * Message for error during processing
     */
    public String failureMessage = "There was an error";
    /**
     * POS tags that will be optional when comparing
     */
    public String optionalLanguageTags = "";
    /**
     * Prefer simple tokenizer over white space tokenizer
     */
    public boolean useSimpleTokenizer = false;
    /**
     * Enables build-in detokenization based on original text, otherwise string join by space is used
     */
    public boolean detokenizeOptimization = true;
}
