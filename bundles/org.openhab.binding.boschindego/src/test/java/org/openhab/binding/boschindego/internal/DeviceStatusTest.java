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
package org.openhab.binding.boschindego.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.boschindego.internal.dto.DeviceCommand;

/**
 * Unit tests for {@link DeviceStatus}.
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class DeviceStatusTest {
    @Test
    public void unknownIdleStateHasReturnCommand() {
        assertThat(DeviceStatus.fromCode(256).getAssociatedCommand(), is(DeviceCommand.RETURN));
    }

    @Test
    public void unknownMowStateHasReturnCommand() {
        assertThat(DeviceStatus.fromCode(520).getAssociatedCommand(), is(DeviceCommand.MOW));
    }

    @Test
    public void unknownReturnStateHasReturnCommand() {
        assertThat(DeviceStatus.fromCode(777).getAssociatedCommand(), is(DeviceCommand.RETURN));
    }

    @Test
    public void chargingIsCharging() {
        assertThat(DeviceStatus.fromCode(257).isCharging(), is(true));
    }

    @Test
    public void dockedLoadingMapIsActive() {
        assertThat(DeviceStatus.fromCode(262).isActive(), is(true));
    }

    @Test
    public void lawnCompleteIsCompleted() {
        assertThat(DeviceStatus.fromCode(775).isCompleted(), is(true));
    }
}
