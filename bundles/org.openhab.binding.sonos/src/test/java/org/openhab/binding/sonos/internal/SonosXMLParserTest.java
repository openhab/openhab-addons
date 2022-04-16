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
package org.openhab.binding.sonos.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class SonosXMLParserTest {

    @Test
    public void extractModelName() {
        assertEquals("Move", SonosXMLParser.extractModelName("Sonos Move"));
        assertEquals("PLAY5", SonosXMLParser.extractModelName("Sonos PLAY:5"));
        assertEquals("RoamSL", SonosXMLParser.extractModelName("Sonos Roam SL"));
        assertEquals("Five", SonosXMLParser.extractModelName("Sonos Five (OpenHome)"));
        assertEquals("OneSL", SonosXMLParser.extractModelName("Sonos One SL (OpenHome)"));
    }
}
