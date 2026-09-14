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
package org.openhab.voice.marytts.internal;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.stream.IntStream;

import javax.sound.sampled.AudioInputStream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.util.dom.MaryDomUtils;

/**
 * Runtime compatibility tests for MaryTTS and its upgraded dependencies.
 *
 * @author Leo Siepel - Initial contribution
 */
public class MaryTTSRuntimeTest {

    private static MaryInterface marytts;

    @BeforeAll
    public static void setUp() throws Exception {
        System.setProperty("mary.base", "target");
        marytts = new LocalMaryInterface();
        marytts.setLocale(Locale.US);
        marytts.setInputType("TEXT");
    }

    @Test
    public void posTaggerRetainsPennTreebankTags() throws Exception {
        marytts.setOutputType("PARTSOFSPEECH");

        Document document = marytts.generateXML("I want to go to the store.");
        NodeList tokens = document.getElementsByTagName("t");
        String[] words = IntStream.range(0, tokens.getLength())
                .mapToObj(i -> MaryDomUtils.tokenText((Element) tokens.item(i))).toArray(String[]::new);
        String[] tags = IntStream.range(0, tokens.getLength())
                .mapToObj(i -> ((Element) tokens.item(i)).getAttribute("pos")).toArray(String[]::new);

        assertArrayEquals(new String[] { "I", "want", "to", "go", "to", "the", "store", "." }, words);
        assertArrayEquals(new String[] { "PRP", "VBP", "TO", "VB", "TO", "DT", "NN", "." }, tags);
    }

    @Test
    public void englishSynthesisProducesAudio() throws Exception {
        marytts.setOutputType("AUDIO");

        try (AudioInputStream audio = marytts.generateAudio("This is a synthesis test.")) {
            int frameSize = Math.max(1, audio.getFormat().getFrameSize());
            byte[] audioData = new byte[frameSize * 256];
            assertTrue(audio.read(audioData) > 0);
        }
    }
}
