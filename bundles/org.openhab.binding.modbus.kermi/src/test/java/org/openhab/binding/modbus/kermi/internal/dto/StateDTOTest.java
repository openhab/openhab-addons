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
package org.openhab.binding.modbus.kermi.internal.dto;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StateDTO}.
 *
 * @author Peter Winterer - Initial contribution
 */
public class StateDTOTest {

    @Test
    public void globalStateIdTest() {
        StateDTO state = new StateDTO(new byte[] { 0x00, 0x02 });

        assertThat(state.globalStateId.intValue(), is(2));
    }
}
