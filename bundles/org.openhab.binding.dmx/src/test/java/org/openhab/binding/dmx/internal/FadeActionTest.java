/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.dmx.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.dmx.internal.action.ActionState;
import org.openhab.binding.dmx.internal.action.FadeAction;
import org.openhab.binding.dmx.internal.multiverse.DmxChannel;

/**
 * Tests cases FadeAction
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class FadeActionTest {
    private static final int TEST_VALUE = 200;
    private static final int TEST_FADE_TIME = 1000;
    private static final int TEST_HOLD_TIME = 1000;

    @Test
    public void checkWithFadingWithoutHold() {
        FadeAction fadeAction = new FadeAction(TEST_FADE_TIME, TEST_VALUE, 0);
        DmxChannel testChannel = new DmxChannel(0, 1, 0);
        testChannel.setValue(0);

        long startTime = System.currentTimeMillis();

        assertThat(fadeAction.getState(), is(ActionState.WAITING));
        assertThat(fadeAction.getNewValue(testChannel, startTime), is(0));
        assertThat(fadeAction.getState(), is(ActionState.RUNNING));
        assertThat(fadeAction.getNewValue(testChannel, startTime + TEST_FADE_TIME / 2), is(256 * TEST_VALUE / 2));
        assertThat(fadeAction.getNewValue(testChannel, startTime + 1000), is(256 * TEST_VALUE));
        assertThat(fadeAction.getState(), is(ActionState.COMPLETED));

        fadeAction.reset();
        assertThat(fadeAction.getState(), is(ActionState.WAITING));
    }

    @Test
    public void checkWithFadingWithHold() {
        FadeAction fadeAction = new FadeAction(TEST_FADE_TIME, TEST_VALUE, TEST_HOLD_TIME);
        DmxChannel testChannel = new DmxChannel(0, 1, 0);
        testChannel.setValue(0);

        long startTime = System.currentTimeMillis();

        assertThat(fadeAction.getState(), is(ActionState.WAITING));
        assertThat(fadeAction.getNewValue(testChannel, startTime), is(0));
        assertThat(fadeAction.getState(), is(ActionState.RUNNING));
        assertThat(fadeAction.getNewValue(testChannel, startTime + TEST_FADE_TIME / 2), is(256 * TEST_VALUE / 2));
        assertThat(fadeAction.getNewValue(testChannel, startTime + TEST_FADE_TIME), is(256 * TEST_VALUE));
        assertThat(fadeAction.getState(), is(ActionState.RUNNING));
        assertThat(fadeAction.getNewValue(testChannel, startTime + TEST_FADE_TIME + TEST_HOLD_TIME / 2),
                is(256 * TEST_VALUE));
        assertThat(fadeAction.getState(), is(ActionState.RUNNING));
        assertThat(fadeAction.getNewValue(testChannel, startTime + TEST_FADE_TIME + TEST_HOLD_TIME),
                is(256 * TEST_VALUE));
        assertThat(fadeAction.getState(), is(ActionState.COMPLETED));

        fadeAction.reset();
        assertThat(fadeAction.getState(), is(ActionState.WAITING));
    }

    @Test
    public void checkWithFadingWithInfiniteHold() {
        FadeAction fadeAction = new FadeAction(TEST_FADE_TIME, TEST_VALUE, -1);
        DmxChannel testChannel = new DmxChannel(0, 1, 0);
        testChannel.setValue(0);

        long startTime = System.currentTimeMillis();

        assertThat(fadeAction.getState(), is(ActionState.WAITING));
        assertThat(fadeAction.getNewValue(testChannel, startTime), is(0));
        assertThat(fadeAction.getState(), is(ActionState.RUNNING));
        assertThat(fadeAction.getNewValue(testChannel, startTime + TEST_FADE_TIME / 2), is(256 * TEST_VALUE / 2));
        assertThat(fadeAction.getNewValue(testChannel, startTime + TEST_FADE_TIME), is(256 * TEST_VALUE));
        assertThat(fadeAction.getState(), is(ActionState.COMPLETEDFINAL));

        fadeAction.reset();
        assertThat(fadeAction.getState(), is(ActionState.WAITING));
    }

    @Test
    public void checkWithoutFadingWithHold() {
        FadeAction fadeAction = new FadeAction(0, TEST_VALUE, TEST_HOLD_TIME);
        DmxChannel testChannel = new DmxChannel(0, 1, 0);
        testChannel.setValue(0);

        long startTime = System.currentTimeMillis();

        assertThat(fadeAction.getState(), is(ActionState.WAITING));
        assertThat(fadeAction.getNewValue(testChannel, startTime), is(256 * TEST_VALUE));
        assertThat(fadeAction.getState(), is(ActionState.RUNNING));
        assertThat(fadeAction.getNewValue(testChannel, startTime + TEST_HOLD_TIME / 2), is(256 * TEST_VALUE));
        assertThat(fadeAction.getState(), is(ActionState.RUNNING));
        assertThat(fadeAction.getNewValue(testChannel, startTime + TEST_HOLD_TIME), is(256 * TEST_VALUE));
        assertThat(fadeAction.getState(), is(ActionState.COMPLETED));

        fadeAction.reset();
        assertThat(fadeAction.getState(), is(ActionState.WAITING));
    }

    @Test
    public void checkWithoutFadingWithoutHold() {
        FadeAction fadeAction = new FadeAction(0, TEST_VALUE, 0);
        DmxChannel testChannel = new DmxChannel(0, 1, 0);
        testChannel.setValue(0);

        long startTime = System.currentTimeMillis();

        assertThat(fadeAction.getState(), is(ActionState.WAITING));
        assertThat(fadeAction.getNewValue(testChannel, startTime), is(256 * TEST_VALUE));
        assertThat(fadeAction.getState(), is(ActionState.COMPLETED));

        fadeAction.reset();
        assertThat(fadeAction.getState(), is(ActionState.WAITING));
    }

    @Test
    public void checkWithoutFadingWithInfiniteHold() {
        FadeAction fadeAction = new FadeAction(0, TEST_VALUE, -1);
        DmxChannel testChannel = new DmxChannel(0, 1, 0);
        testChannel.setValue(0);

        long startTime = System.currentTimeMillis();

        assertThat(fadeAction.getState(), is(ActionState.WAITING));
        assertThat(fadeAction.getNewValue(testChannel, startTime), is(256 * TEST_VALUE));
        assertThat(fadeAction.getState(), is(ActionState.COMPLETEDFINAL));

        fadeAction.reset();
        assertThat(fadeAction.getState(), is(ActionState.WAITING));
    }
}
