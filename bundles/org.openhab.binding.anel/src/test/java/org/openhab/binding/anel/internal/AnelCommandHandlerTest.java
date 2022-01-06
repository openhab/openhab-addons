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
package org.openhab.binding.anel.internal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.anel.internal.state.AnelCommandHandler;
import org.openhab.binding.anel.internal.state.AnelState;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.RefreshType;

/**
 * This class tests {@link AnelCommandHandler}.
 *
 * @author Patrick Koenemann - Initial contribution
 */
@NonNullByDefault
public class AnelCommandHandlerTest {

    private static final String CHANNEL_R1 = IAnelConstants.CHANNEL_RELAY_STATE.get(0);
    private static final String CHANNEL_R3 = IAnelConstants.CHANNEL_RELAY_STATE.get(2);
    private static final String CHANNEL_R4 = IAnelConstants.CHANNEL_RELAY_STATE.get(3);
    private static final String CHANNEL_IO1 = IAnelConstants.CHANNEL_IO_STATE.get(0);
    private static final String CHANNEL_IO6 = IAnelConstants.CHANNEL_IO_STATE.get(5);

    private static final AnelState STATE_INVALID = AnelState.of(null);
    private static final AnelState STATE_HOME = AnelState.of(IAnelTestStatus.STATUS_HOME_V46);
    private static final AnelState STATE_HUT = AnelState.of(IAnelTestStatus.STATUS_HUT_V65);

    private final AnelCommandHandler commandHandler = new AnelCommandHandler();

    @Test
    public void refreshCommand() {
        // given & when
        final String cmd = commandHandler.toAnelCommandAndUnsetState(STATE_INVALID, CHANNEL_R1, RefreshType.REFRESH,
                "a");
        // then
        assertNull(cmd);
    }

    @Test
    public void decimalCommandReturnsNull() {
        // given & when
        final String cmd = commandHandler.toAnelCommandAndUnsetState(STATE_HOME, CHANNEL_R1, new DecimalType("1"), "a");
        // then
        assertNull(cmd);
    }

    @Test
    public void stringCommandReturnsNull() {
        // given & when
        final String cmd = commandHandler.toAnelCommandAndUnsetState(STATE_HOME, CHANNEL_R1, new StringType("ON"), "a");
        // then
        assertNull(cmd);
    }

    @Test
    public void increaseDecreaseCommandReturnsNull() {
        // given & when
        final String cmd = commandHandler.toAnelCommandAndUnsetState(STATE_HOME, CHANNEL_R1,
                IncreaseDecreaseType.INCREASE, "a");
        // then
        assertNull(cmd);
    }

    @Test
    public void upDownCommandReturnsNull() {
        // given & when
        final String cmd = commandHandler.toAnelCommandAndUnsetState(STATE_HOME, CHANNEL_R1, UpDownType.UP, "a");
        // then
        assertNull(cmd);
    }

    @Test
    public void unlockedSwitchReturnsCommand() {
        // given & when
        final String cmdOn1 = commandHandler.toAnelCommandAndUnsetState(STATE_HOME, CHANNEL_R1, OnOffType.ON, "a");
        final String cmdOff1 = commandHandler.toAnelCommandAndUnsetState(STATE_HOME, CHANNEL_R1, OnOffType.OFF, "a");
        final String cmdOn3 = commandHandler.toAnelCommandAndUnsetState(STATE_HOME, CHANNEL_R3, OnOffType.ON, "a");
        final String cmdOff3 = commandHandler.toAnelCommandAndUnsetState(STATE_HOME, CHANNEL_R3, OnOffType.OFF, "a");
        // then
        assertThat(cmdOn1, equalTo("Sw_on1a"));
        assertThat(cmdOff1, equalTo("Sw_off1a"));
        assertThat(cmdOn3, equalTo("Sw_on3a"));
        assertThat(cmdOff3, equalTo("Sw_off3a"));
    }

    @Test
    public void lockedSwitchReturnsNull() {
        // given & when
        final String cmd = commandHandler.toAnelCommandAndUnsetState(STATE_HOME, CHANNEL_R4, OnOffType.ON, "a");
        // then
        assertNull(cmd);
    }

    @Test
    public void nullIOSwitchReturnsCommand() {
        // given & when
        final String cmdOn = commandHandler.toAnelCommandAndUnsetState(STATE_HOME, CHANNEL_IO1, OnOffType.ON, "a");
        final String cmdOff = commandHandler.toAnelCommandAndUnsetState(STATE_HOME, CHANNEL_IO1, OnOffType.OFF, "a");
        // then
        assertThat(cmdOn, equalTo("IO_on1a"));
        assertThat(cmdOff, equalTo("IO_off1a"));
    }

    @Test
    public void inputIOSwitchReturnsNull() {
        // given & when
        final String cmd = commandHandler.toAnelCommandAndUnsetState(STATE_HUT, CHANNEL_IO6, OnOffType.ON, "a");
        // then
        assertNull(cmd);
    }

    @Test
    public void outputIOSwitchReturnsCommand() {
        // given & when
        final String cmdOn = commandHandler.toAnelCommandAndUnsetState(STATE_HUT, CHANNEL_IO1, OnOffType.ON, "a");
        final String cmdOff = commandHandler.toAnelCommandAndUnsetState(STATE_HUT, CHANNEL_IO1, OnOffType.OFF, "a");
        // then
        assertThat(cmdOn, equalTo("IO_on1a"));
        assertThat(cmdOff, equalTo("IO_off1a"));
    }

    @Test
    public void ioDirectionSwitchReturnsNull() {
        // given & when
        final String cmd = commandHandler.toAnelCommandAndUnsetState(STATE_HUT, IAnelConstants.CHANNEL_IO_MODE.get(0),
                OnOffType.ON, "a");
        // then
        assertNull(cmd);
    }

    @Test
    public void sensorTemperatureCommandReturnsNull() {
        // given & when
        final String cmd = commandHandler.toAnelCommandAndUnsetState(STATE_HUT,
                IAnelConstants.CHANNEL_SENSOR_TEMPERATURE, new DecimalType("1.0"), "a");
        // then
        assertNull(cmd);
    }

    @Test
    public void relayChannelIdIndex() {
        for (int i = 0; i < IAnelConstants.CHANNEL_RELAY_STATE.size(); i++) {
            final String relayStateChannelId = IAnelConstants.CHANNEL_RELAY_STATE.get(i);
            final String relayIndex = relayStateChannelId.substring(1, 2);
            final String expectedIndex = String.valueOf(i + 1);
            assertThat(relayIndex, equalTo(expectedIndex));
        }
    }

    @Test
    public void ioChannelIdIndex() {
        for (int i = 0; i < IAnelConstants.CHANNEL_IO_STATE.size(); i++) {
            final String ioStateChannelId = IAnelConstants.CHANNEL_IO_STATE.get(i);
            final String ioIndex = ioStateChannelId.substring(2, 3);
            final String expectedIndex = String.valueOf(i + 1);
            assertThat(ioIndex, equalTo(expectedIndex));
        }
    }
}
