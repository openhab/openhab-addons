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
package org.openhab.binding.wemo.internal.handler.test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.openhab.binding.wemo.internal.WemoBindingConstants.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.wemo.internal.WemoBindingConstants;
import org.openhab.binding.wemo.internal.handler.WemoInsightHandler;
import org.openhab.binding.wemo.internal.http.WemoHttpCall;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;

/**
 * Tests for {@link WemoInsightHandler}.
 *
 * @author Svilen Valkanov - Initial contribution
 * @author Stefan Triller - Ported Tests from Groovy to Java
 */
public class WemoInsightHandlerTest {

    private static final ThingTypeUID THING_TYPE = WemoBindingConstants.THING_TYPE_INSIGHT;
    private static final String THING_ID = "test";

    private MockWemoInsightHandler handler;

    private static final String SERVICE_ID = "insight";
    private static final String PARAMS_NAME = "InsightParams";
    private WemoInsightParams insightParams;

    /** Used for all tests, where expected value is time in seconds **/
    private static final int TIME_PARAM = 4702;

    /** Represents a state parameter, where 1 stays for ON and 0 stays for OFF **/
    private static final int STATE_PARAM = 1;

    /** Represents power in Wats **/
    private static final int POWER_PARAM = 54;

    private final Thing thing = mock(Thing.class);

    @BeforeEach
    public void setUp() {
        insightParams = new WemoInsightParams();
        when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE, THING_ID));
        when(thing.getThingTypeUID()).thenReturn(THING_TYPE);
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
    }

    @AfterEach
    public void clear() {
        handler.channelState = null;
        handler.channelToWatch = null;
    }

    @Test
    public void assertThatChannelSTATEisUpdatedOnReceivedValue() {
        insightParams.state = STATE_PARAM;
        State expectedStateType = OnOffType.ON;
        String expectedChannel = CHANNEL_STATE;

        testOnValueReceived(expectedChannel, expectedStateType, insightParams.toString());
    }

    @Test
    public void assertThatChannelLASTONFORIsUpdatedOnReceivedValue() {
        insightParams.lastOnFor = TIME_PARAM;
        State expectedStateType = new DecimalType(TIME_PARAM);
        String expectedChannel = CHANNEL_LAST_ON_FOR;

        testOnValueReceived(expectedChannel, expectedStateType, insightParams.toString());
    }

    @Test
    public void assertThatChannelONTODAYIsUpdatedOnReceivedValue() {
        insightParams.onToday = TIME_PARAM;
        State expectedStateType = new DecimalType(TIME_PARAM);
        String expectedChannel = CHANNEL_ON_TODAY;

        testOnValueReceived(expectedChannel, expectedStateType, insightParams.toString());
    }

    @Test
    public void assertThatChannelONTOTALIsUpdatedOnReceivedValue() {
        insightParams.onTotal = TIME_PARAM;
        State expectedStateType = new DecimalType(TIME_PARAM);
        String expectedChannel = CHANNEL_ON_TOTAL;

        testOnValueReceived(expectedChannel, expectedStateType, insightParams.toString());
    }

    @Test
    public void assertThatChannelTIMESPANIsUpdatedOnReceivedValue() {
        insightParams.timespan = TIME_PARAM;
        State expectedStateType = new DecimalType(TIME_PARAM);
        String expectedChannel = CHANNEL_TIMESPAN;

        testOnValueReceived(expectedChannel, expectedStateType, insightParams.toString());
    }

    @Test
    public void assertThatChannelAVERAGEPOWERIsUpdatedOnReceivedValue() {
        insightParams.avgPower = POWER_PARAM;
        State expectedStateType = new QuantityType<>(POWER_PARAM, Units.WATT);
        String expectedChannel = CHANNEL_AVERAGE_POWER;

        testOnValueReceived(expectedChannel, expectedStateType, insightParams.toString());
    }

    private void testOnValueReceived(String expectedChannel, State expectedState, String insightParams) {
        handler = new MockWemoInsightHandler(thing, expectedChannel);

        handler.onValueReceived(PARAMS_NAME, insightParams, SERVICE_ID);
        assertThat(handler.channelState, is(notNullValue()));
        assertThat(handler.channelState, is(expectedState));
    }

    class MockWemoInsightHandler extends WemoInsightHandler {
        State channelState;
        String channelToWatch;

        public MockWemoInsightHandler(Thing thing, String channelToWatch) {
            super(thing, null, new WemoHttpCall());
            this.channelToWatch = channelToWatch;
        }

        @Override
        protected void updateState(String channelID, State channelState) {
            if (channelID.equals(channelToWatch)) {
                this.channelState = channelState;
            }
        }

        @Override
        protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, String description) {
        }

        @Override
        protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail) {
        }

        @Override
        protected void updateStatus(ThingStatus status) {
        }
    }

    class WemoInsightParams {
        int state, lastChangedAt, lastOnFor, onToday, onTotal, timespan, avgPower, currPower, todayEnergy, totalEnergy,
                standbyLimit;

        @Override
        public String toString() {
            // Example string looks like "1|1427230660|4702|25528|82406|1209600|39|40880|15620649|54450534.000000|8000"
            return state + "|" + lastChangedAt + "|" + lastOnFor + "|" + onToday + "|" + onTotal + "|" + timespan + "|"
                    + avgPower + "|" + currPower + "|" + todayEnergy + "|" + totalEnergy + "|" + standbyLimit;
        }
    }
}
