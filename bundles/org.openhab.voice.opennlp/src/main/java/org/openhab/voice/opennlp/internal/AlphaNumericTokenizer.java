/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.voice.opennlp.internal;

import java.util.Arrays;
import java.util.stream.Stream;

import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.util.Span;

/**
 * This tokenizer splits by ignoring all non alphanumeric characters.
 * It performs better than the white space tokenizer for certain languages.
 *
 * @author Yannick Schaus - Initial contribution
 */
public class AlphaNumericTokenizer extends SimpleTokenizer {

    public static final AlphaNumericTokenizer INSTANCE;

    static {
        INSTANCE = new AlphaNumericTokenizer();
    }

    /**
     * @deprecated Use INSTANCE field instead to obtain an instance, constructor
     *             will be made private in the future.
     */
    @Deprecated
    public AlphaNumericTokenizer() {
    }

    @Override
    public Span[] tokenizePos(String s) {
        Span[] tokens = super.tokenizePos(s);
        Stream<Span> filteredTokens = Arrays.stream(tokens)
                .filter(span -> Character.isLetter(span.getCoveredText(s).charAt(0))
                        || Character.isDigit(span.getCoveredText(s).charAt(0)));

        return filteredTokens.toArray(Span[]::new);
    }

}
