/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.enigma2.actions;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import org.openhab.binding.enigma2.handler.Enigma2Handler;
import org.openhab.binding.enigma2.internal.Enigma2BindingConstants;

/**
 * The {@link Enigma2ActionsTest} class is responsible for testing {@link Enigma2Actions}.
 *
 * @author Guido Dolfen - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public class Enigma2ActionsTest {
    @Nullable
    private Enigma2Actions enigma2Actions;
    @Nullable
    private Enigma2Handler enigma2Handler;
    public static final String SOME_TEXT = "some Text";

    @Before
    public void setUp() {
        enigma2Handler = mock(Enigma2Handler.class);
        enigma2Actions = new Enigma2Actions();
        enigma2Actions.setThingHandler(enigma2Handler);
    }

    @Test
    public void testGetThingHandler() {
        assertThat(enigma2Actions.getThingHandler(), is(enigma2Handler));
    }

    @Test
    public void testSendRcCommand() {
        enigma2Actions.sendRcCommand("KEY_1");
        verify(enigma2Handler).sendRcCommand("KEY_1");
    }

    @Test
    public void testSendInfo() {
        enigma2Actions.sendInfo(SOME_TEXT);
        verify(enigma2Handler).sendInfo(Enigma2BindingConstants.MESSAGE_TIMEOUT, SOME_TEXT);
    }

    @Test
    public void testSendInfoTimeout() {
        enigma2Actions.sendInfo(SOME_TEXT, 10);
        verify(enigma2Handler).sendInfo(10, SOME_TEXT);
    }

    @Test
    public void testSendError() {
        enigma2Actions.sendError(SOME_TEXT);
        verify(enigma2Handler).sendError(Enigma2BindingConstants.MESSAGE_TIMEOUT, SOME_TEXT);
    }

    @Test
    public void testSendErrorTimeout() {
        enigma2Actions.sendError(SOME_TEXT, 10);
        verify(enigma2Handler).sendError(10, SOME_TEXT);
    }

    @Test
    public void testSendWarning() {
        enigma2Actions.sendWarning(SOME_TEXT);
        verify(enigma2Handler).sendWarning(Enigma2BindingConstants.MESSAGE_TIMEOUT, SOME_TEXT);
    }

    @Test
    public void testSendWarningTimeout() {
        enigma2Actions.sendWarning(SOME_TEXT, 10);
        verify(enigma2Handler).sendWarning(10, SOME_TEXT);
    }

    @Test
    public void testSendQuestion() {
        enigma2Actions.sendQuestion(SOME_TEXT);
        verify(enigma2Handler).sendQuestion(Enigma2BindingConstants.MESSAGE_TIMEOUT, SOME_TEXT);
    }

    @Test
    public void testSendQuestionTimeout() {
        enigma2Actions.sendQuestion(SOME_TEXT, 10);
        verify(enigma2Handler).sendQuestion(10, SOME_TEXT);
    }

    @Test
    public void testSendRcCommandStatic() {
        Enigma2Actions.sendRcCommand(enigma2Actions, "KEY_1");
        verify(enigma2Handler).sendRcCommand("KEY_1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendRcCommandStaticWithException() {
        Enigma2Actions.sendRcCommand(null, "KEY_1");
    }

    @Test
    public void testSendInfoStatic() {
        Enigma2Actions.sendInfo(enigma2Actions, SOME_TEXT);
        verify(enigma2Handler).sendInfo(Enigma2BindingConstants.MESSAGE_TIMEOUT, SOME_TEXT);
    }

    @Test
    public void testSendInfoTimeoutStatic() {
        Enigma2Actions.sendInfo(enigma2Actions, SOME_TEXT, 10);
        verify(enigma2Handler).sendInfo(10, SOME_TEXT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendInfoStaticWithException() {
        Enigma2Actions.sendInfo(null, SOME_TEXT);
    }

    @Test
    public void testSendErrorStatic() {
        Enigma2Actions.sendError(enigma2Actions, SOME_TEXT);
        verify(enigma2Handler).sendError(Enigma2BindingConstants.MESSAGE_TIMEOUT, SOME_TEXT);
    }

    @Test
    public void testSendErrorTimeoutStatic() {
        Enigma2Actions.sendError(enigma2Actions, SOME_TEXT, 10);
        verify(enigma2Handler).sendError(10, SOME_TEXT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendErrorStaticWithException() {
        Enigma2Actions.sendError(null, SOME_TEXT);
    }

    @Test
    public void testSendWarningStatic() {
        Enigma2Actions.sendWarning(enigma2Actions, SOME_TEXT);
        verify(enigma2Handler).sendWarning(Enigma2BindingConstants.MESSAGE_TIMEOUT, SOME_TEXT);
    }

    @Test
    public void testSendWarningTimeoutStatic() {
        Enigma2Actions.sendWarning(enigma2Actions, SOME_TEXT, 10);
        verify(enigma2Handler).sendWarning(10, SOME_TEXT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendWarningStaticWithException() {
        Enigma2Actions.sendWarning(null, SOME_TEXT);
    }

    @Test
    public void testSendQuestionStatic() {
        Enigma2Actions.sendQuestion(enigma2Actions, SOME_TEXT);
        verify(enigma2Handler).sendQuestion(Enigma2BindingConstants.MESSAGE_TIMEOUT, SOME_TEXT);
    }

    @Test
    public void testSendQuestionTimeoutStatic() {
        Enigma2Actions.sendQuestion(enigma2Actions, SOME_TEXT, 10);
        verify(enigma2Handler).sendQuestion(10, SOME_TEXT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSendQuestionStaticWithException() {
        Enigma2Actions.sendQuestion(null, SOME_TEXT);
    }
}