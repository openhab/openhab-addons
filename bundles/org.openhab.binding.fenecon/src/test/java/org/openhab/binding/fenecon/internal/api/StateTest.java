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
package org.openhab.binding.fenecon.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fenecon.internal.FeneconBindingConstants;

/**
 * Test for {@link State}.
 *
 * @author Philipp Schneider - Initial contribution
 */
@NonNullByDefault
public class StateTest {

    @Test
    void testStateOk() {
        State state = State.get(
                new FeneconResponse(FeneconBindingConstants.STATE_ADDRESS, "0:Ok, 1:Info, 2:Warning, 3:Fault", "0"));
        assertEquals("Ok", state.state());
    }

    @Test
    void testStateInfo() {
        State state = State.get(
                new FeneconResponse(FeneconBindingConstants.STATE_ADDRESS, "0:Ok, 1:Info, 2:Warning, 3:Fault", "1"));
        assertEquals("Info", state.state());
    }

    @Test
    void testStateWarning() {
        State state = State.get(
                new FeneconResponse(FeneconBindingConstants.STATE_ADDRESS, "0:Ok, 1:Info, 2:Warning, 3:Fault", "2"));
        assertEquals("Warning", state.state());
    }

    @Test
    void testStateFault() {
        State state = State.get(
                new FeneconResponse(FeneconBindingConstants.STATE_ADDRESS, "0:Ok, 1:Info, 2:Warning, 3:Fault", "3"));
        assertEquals("Fault", state.state());
    }

    @Test
    void testStateUnknown() {
        State state = State.get(
                new FeneconResponse(FeneconBindingConstants.STATE_ADDRESS, "0:Ok, 1:Info, 2:Warning, 3:Fault", "4"));
        assertEquals("Unknown", state.state());
    }
}
