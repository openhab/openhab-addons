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
package org.openhab.binding.enigma2.internal.actions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.enigma2.internal.Enigma2BindingConstants;
import org.openhab.binding.enigma2.internal.handler.Enigma2Handler;

/**
 * The {@link Enigma2ActionsTest} class is responsible for testing {@link Enigma2Actions}.
 *
 * @author Guido Dolfen - Initial contribution
 */
@NonNullByDefault
public class Enigma2ActionsTest {
    private @NonNullByDefault({}) Enigma2Actions enigma2Actions;
    private @NonNullByDefault({}) Enigma2Handler enigma2Handler;
    public static final String SOME_TEXT = "some Text";

    @BeforeEach
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
}
