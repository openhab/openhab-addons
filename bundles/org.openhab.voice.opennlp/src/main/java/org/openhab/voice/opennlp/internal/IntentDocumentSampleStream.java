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

import java.io.IOException;
import java.util.Vector;

import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.ObjectStream;

/**
 * This class allows reusing the format used by the @{link NameSampleDataStream} for document categorization - it simply
 * removes the tags.
 *
 * @author Yannick Schaus - Initial contribution
 */
public class IntentDocumentSampleStream implements ObjectStream<DocumentSample> {

    String category;
    ObjectStream<String> stream;

    public IntentDocumentSampleStream(String category, ObjectStream<String> stream) {
        this.category = category;
        this.stream = stream;
    }

    @Override
    public DocumentSample read() throws IOException {
        String sampleString = stream.read();

        if (sampleString != null) {
            // Whitespace tokenize entire string
            String[] tokens = WhitespaceTokenizer.INSTANCE.tokenize(sampleString);

            // remove entities
            Vector<String> vector = new Vector<String>(tokens.length);
            // boolean skip = false;
            for (String token : tokens) {
                if (!token.startsWith("<")) {
                    // System.out.print(token + " ");
                    vector.add(token);
                }
                // if (token.startsWith("<")) {
                // skip = !skip;
                // } else if (!skip) {
                // System.out.print(token + " ");
                // vector.add(token);
                // }
            }
            // System.out.println();

            tokens = new String[vector.size()];
            vector.copyInto(tokens);

            DocumentSample sample;

            if (tokens.length > 0) {
                sample = new DocumentSample(category, tokens);
            } else {
                throw new IOException("Empty lines are not allowed!");
            }

            return sample;
        } else {
            return null;
        }
    }

    @Override
    public void reset() throws IOException, UnsupportedOperationException {
        stream.reset();
    }

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
