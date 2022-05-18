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
    public void buildThingTypeIdFromModelWithoutSpace() {
        assertEquals("Move", SonosXMLParser.buildThingTypeIdFromModelName("Sonos Move"));
    }

    @Test
    public void buildThingTypeIdFromModelWithSpace() {
        assertEquals("RoamSL", SonosXMLParser.buildThingTypeIdFromModelName("Sonos Roam SL"));
    }

    @Test
    public void buildThingTypeIdFromModelWithColon() {
        assertEquals("PLAY5", SonosXMLParser.buildThingTypeIdFromModelName("Sonos PLAY:5"));
    }

    @Test
    public void buildThingTypeIdFromSymfoniskModel() {
        assertEquals("SYMFONISK", SonosXMLParser.buildThingTypeIdFromModelName("SYMFONISK Table lamp"));
        assertEquals("SYMFONISK", SonosXMLParser.buildThingTypeIdFromModelName("Symfonisk Table lamp"));
        assertEquals("SYMFONISK", SonosXMLParser.buildThingTypeIdFromModelName("Sonos Symfonisk"));
    }

    @Test
    public void buildThingTypeIdFromZP80Model() {
        assertEquals("CONNECT", SonosXMLParser.buildThingTypeIdFromModelName("Sonos ZP80"));
    }

    @Test
    public void buildThingTypeIdFromZP100Model() {
        assertEquals("CONNECTAMP", SonosXMLParser.buildThingTypeIdFromModelName("Sonos ZP100"));
    }

    @Test
    public void buildThingTypeIdFromModelWithAdditionalTextInParenthesis() {
        assertEquals("OneSL", SonosXMLParser.buildThingTypeIdFromModelName("Sonos One SL (OpenHome)"));
    }
}
