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
package org.openhab.binding.mqtt.generic.values;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;

/**
 * Tests that formatBeforePublish is applied to rollershutter positions, but not to configured command strings.
 */
@NonNullByDefault
public class RollershutterValueFormatTest {

    @Test
    public void patternIsAppliedToPositions() {
        RollershutterValue v = new RollershutterValue(null, null, "Stop", null, null, false, false);
        assertEquals("pos,90", v.getMQTTpublishValue(new PercentType(90), "pos,%s"));
        assertEquals("pos,90", v.getMQTTpublishValue(new PercentType(90), "pos,%d"));
        assertEquals("pos,0", v.getMQTTpublishValue(UpDownType.UP, "pos,%s"));
        assertEquals("pos,100", v.getMQTTpublishValue(UpDownType.DOWN, "pos,%s"));
    }

    @Test
    public void stopStringIsNotFormatted() {
        RollershutterValue v = new RollershutterValue(null, null, "Stop", null, null, false, false);
        assertEquals("Stop", v.getMQTTpublishValue(StopMoveType.STOP, "pos,%s"));
    }

    @Test
    public void mixedConfigurationKeepsCommandStrings() {
        RollershutterValue v = new RollershutterValue("Open", null, "Stop", null, null, false, false);
        assertEquals("Open", v.getMQTTpublishValue(UpDownType.UP, "pos,%s"));
        assertEquals("Stop", v.getMQTTpublishValue(StopMoveType.STOP, "pos,%s"));
        assertEquals("pos,50", v.getMQTTpublishValue(new PercentType(50), "pos,%s"));
        assertEquals("pos,100", v.getMQTTpublishValue(UpDownType.DOWN, "pos,%s"));
    }

    @Test
    public void customUpDownStringsAreNotFormatted() {
        RollershutterValue v = new RollershutterValue("Open", "Close", "Stop", null, null, false, false);
        assertEquals("Open", v.getMQTTpublishValue(UpDownType.UP, "Cover.%s"));
        assertEquals("Close", v.getMQTTpublishValue(UpDownType.DOWN, "Cover.%s"));
        assertEquals("Cover.50", v.getMQTTpublishValue(new PercentType(50), "Cover.%s"));
    }

    @Test
    public void extentsTransformedToStringAreNotFormatted() {
        RollershutterValue v = new RollershutterValue("Open", "Close", "Stop", null, null, false, true);
        assertEquals("Open", v.getMQTTpublishValue(PercentType.ZERO, "pos,%s"));
        assertEquals("Close", v.getMQTTpublishValue(PercentType.HUNDRED, "pos,%s"));
        assertEquals("pos,50", v.getMQTTpublishValue(new PercentType(50), "pos,%s"));
    }

    @Test
    public void withoutPatternValueIsUnchanged() {
        RollershutterValue v = new RollershutterValue(null, null, "Stop", null, null, false, false);
        assertEquals("90", v.getMQTTpublishValue(new PercentType(90), null));
        assertEquals("Stop", v.getMQTTpublishValue(StopMoveType.STOP, null));
    }

    @Test
    public void invertedValueIsFormatted() {
        RollershutterValue v = new RollershutterValue(null, null, "Stop", null, null, true, false);
        assertEquals("pos,10", v.getMQTTpublishValue(new PercentType(90), "pos,%s"));
        assertEquals("pos,100", v.getMQTTpublishValue(UpDownType.UP, "pos,%s"));
        assertEquals("pos,0", v.getMQTTpublishValue(UpDownType.DOWN, "pos,%s"));
    }
}
