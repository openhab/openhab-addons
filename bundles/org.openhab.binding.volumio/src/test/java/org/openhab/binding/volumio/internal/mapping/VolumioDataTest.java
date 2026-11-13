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
package org.openhab.binding.volumio.internal.mapping;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link VolumioData}, in particular publishing of the first received values.
 *
 * @author Michael Loercher - Initial contribution
 */
@NonNullByDefault
public class VolumioDataTest {

    @Test
    public void firstPayloadWithFalseAndZeroValuesIsDirty() {
        VolumioData data = new VolumioData();
        data.update(new JSONObject("{\"mute\":false,\"stream\":false,\"updatedb\":false,\"repeatSingle\":false,"
                + "\"random\":false,\"repeat\":false,\"duration\":0,\"position\":0,\"volume\":0}"));

        assertTrue(data.isMuteDirty());
        assertTrue(data.isStreamDirty());
        assertTrue(data.isUpdateDbDirty());
        assertTrue(data.isRepeatSingleDirty());
        assertTrue(data.isRandomDirty());
        assertTrue(data.isRepeatDirty());
        assertTrue(data.isDurationDirty());
        assertTrue(data.isPositionDirty());
        assertTrue(data.isVolumeDirty());
    }

    @Test
    public void secondIdenticalPayloadIsNotDirty() {
        VolumioData data = new VolumioData();
        String payload = "{\"mute\":false,\"stream\":false,\"duration\":0,\"position\":0}";
        data.update(new JSONObject(payload));
        data.update(new JSONObject(payload));

        assertFalse(data.isMuteDirty());
        assertFalse(data.isStreamDirty());
        assertFalse(data.isDurationDirty());
        assertFalse(data.isPositionDirty());
    }

    @Test
    public void changedValueIsDirtyAndMissingFieldIsNot() {
        VolumioData data = new VolumioData();
        data.update(new JSONObject("{\"mute\":false,\"position\":1}"));
        data.update(new JSONObject("{\"mute\":true}"));

        assertTrue(data.isMuteDirty());
        assertFalse(data.isPositionDirty());
    }

    @Test
    public void fieldFirstSeenInLaterPayloadIsDirty() {
        VolumioData data = new VolumioData();
        data.update(new JSONObject("{\"position\":0}"));
        data.update(new JSONObject("{\"position\":0,\"stream\":false}"));

        assertTrue(data.isStreamDirty());
        assertFalse(data.isPositionDirty());
    }
}
