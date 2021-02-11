/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.synopanalyzer.internal;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.openhab.binding.synopanalyser.internal.synop.SynopDecoder;

/**
 * Tests cases for {@see SynopDecoder}
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class SynopDecoderTest {

    @Test
    public void testDecode() {
        List<String> messages = List.of(
                "201809051400 AAXX 05141 10224 42680 50704 10230 20139 30174 40180 58010 81101 333 55309 22094 30345 81845 85080 91007 90710",
                "AAXX 10124 07610 04565 72107 10080 20063 39877 40098 51037 60031 759// 333 4/000 60027 90710 91112 555 69905");
        messages.forEach(message -> {
            SynopDecoder decoder = new SynopDecoder(message);
            // String s0 = decoder.section0;
        });
    }

}
