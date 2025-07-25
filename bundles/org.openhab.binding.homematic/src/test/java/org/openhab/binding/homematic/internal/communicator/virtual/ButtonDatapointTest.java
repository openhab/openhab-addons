/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.homematic.internal.communicator.virtual;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.homematic.internal.misc.HomematicClientException;
import org.openhab.binding.homematic.internal.misc.HomematicConstants;
import org.openhab.binding.homematic.internal.misc.MiscUtils;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmInterface;
import org.openhab.binding.homematic.internal.model.HmParamsetType;
import org.openhab.binding.homematic.internal.model.HmValueType;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.CommonTriggerEvents;

/**
 * Tests for {@link ButtonVirtualDatapointHandler}.
 *
 * @author Michael Reitler - Initial Contribution
 *
 */
public class ButtonDatapointTest extends JavaTest {

    private static final int DISABLE_DATAPOINT_DELAY = 50;

    private MockEventReceiver mockEventReceiver;
    private final ButtonVirtualDatapointHandler bvdpHandler = new ButtonVirtualDatapointHandler();

    @BeforeEach
    public void setup() throws IOException {
        this.mockEventReceiver = new MockEventReceiver();
    }

    @Test
    public void testShortPress() throws IOException, HomematicClientException {
        HmDatapoint shortPressDp = createPressDatapoint("PRESS_SHORT", Boolean.TRUE);
        HmDatapoint buttonVirtualDatapoint = getButtonVirtualDatapoint(shortPressDp);

        mockEventReceiver.eventReceived(shortPressDp);

        assertThat(buttonVirtualDatapoint.getValue(), is(CommonTriggerEvents.SHORT_PRESSED));
    }

    @Test
    public void testLongPressHm() throws IOException, HomematicClientException {
        HmDatapoint longPressDp = createPressDatapoint("PRESS_LONG", Boolean.TRUE);
        HmDatapoint buttonVirtualDatapoint = getButtonVirtualDatapoint(longPressDp);

        mockEventReceiver.eventReceived(longPressDp);
        assertThat(buttonVirtualDatapoint.getValue(), is(CommonTriggerEvents.LONG_PRESSED));

        HmDatapoint contPressDp = createPressDatapointFrom(longPressDp, "PRESS_CONT", Boolean.TRUE);
        mockEventReceiver.eventReceived(contPressDp);
        assertThat(buttonVirtualDatapoint.getValue(), is("LONG_REPEATED"));
        assertThat(buttonVirtualDatapoint.getPreviousValue(), nullValue());

        // Receiving another LONG event during the long press should be ignored
        mockEventReceiver.eventReceived(longPressDp);
        assertThat(buttonVirtualDatapoint.getValue(), is("LONG_REPEATED"));
        assertThat(buttonVirtualDatapoint.getPreviousValue(), is("LONG_REPEATED"));

        HmDatapoint releaseDp = createPressDatapointFrom(longPressDp, "PRESS_LONG_RELEASE", Boolean.TRUE);
        mockEventReceiver.eventReceived(releaseDp);
        assertThat(buttonVirtualDatapoint.getValue(), is("LONG_RELEASED"));

        // A CONT event right after a SHORT event should not trigger another SHORT_PRESSED event
        HmDatapoint shortPressDp = createPressDatapointFrom(longPressDp, "PRESS_SHORT", Boolean.TRUE);
        mockEventReceiver.eventReceived(shortPressDp);
        mockEventReceiver.eventReceived(contPressDp);
        assertThat(buttonVirtualDatapoint.getValue(), nullValue());
    }

    @Test
    public void testLongPressHmIp() throws IOException, HomematicClientException {
        HmDatapoint longPressDp = createPressDatapoint("PRESS_LONG_START", Boolean.TRUE);
        HmDatapoint buttonVirtualDatapoint = getButtonVirtualDatapoint(longPressDp);

        mockEventReceiver.eventReceived(longPressDp);
        assertThat(buttonVirtualDatapoint.getValue(), is(CommonTriggerEvents.LONG_PRESSED));

        HmDatapoint contPressDp = createPressDatapointFrom(longPressDp, "PRESS_LONG", Boolean.TRUE);
        mockEventReceiver.eventReceived(contPressDp);
        assertThat(buttonVirtualDatapoint.getValue(), is("LONG_REPEATED"));
        assertThat(buttonVirtualDatapoint.getPreviousValue(), nullValue());

        HmDatapoint releaseDp = createPressDatapointFrom(longPressDp, "PRESS_LONG_RELEASE", Boolean.TRUE);
        mockEventReceiver.eventReceived(releaseDp);
        assertThat(buttonVirtualDatapoint.getValue(), is("LONG_RELEASED"));

        // A LONG event right after a SHORT event should not trigger another SHORT_PRESSED event
        HmDatapoint shortPressDp = createPressDatapointFrom(longPressDp, "PRESS_SHORT", Boolean.TRUE);
        mockEventReceiver.eventReceived(shortPressDp);
        mockEventReceiver.eventReceived(contPressDp);
        assertThat(buttonVirtualDatapoint.getValue(), nullValue());
    }

    @Test
    public void testUnsupportedEvents() throws IOException, HomematicClientException {
        HmDatapoint contPressDp = createPressDatapoint("PRESS_CONT", Boolean.TRUE);
        HmDatapoint contButtonVirtualDatapoint = getButtonVirtualDatapoint(contPressDp);

        mockEventReceiver.eventReceived(contPressDp);

        HmDatapoint releaseDp = createPressDatapoint("PRESS_LONG_RELEASE", Boolean.TRUE);
        HmDatapoint releaseButtonVirtualDatapoint = getButtonVirtualDatapoint(releaseDp);

        mockEventReceiver.eventReceived(releaseDp);

        HmDatapoint crapDp = createPressDatapoint("CRAP", Boolean.TRUE);
        HmDatapoint crapButtonVirtualDatapoint = getButtonVirtualDatapoint(crapDp);

        mockEventReceiver.eventReceived(crapDp);

        // CONT and LONG_RELEASE events without previous LONG event are supposed to yield no trigger
        assertThat(contButtonVirtualDatapoint.getValue(), nullValue());
        assertThat(releaseButtonVirtualDatapoint.getValue(), nullValue());
        assertThat(crapButtonVirtualDatapoint, nullValue());
    }

    private HmDatapoint createPressDatapoint(String channelName, Object value) {
        HmDatapoint pressDp = new HmDatapoint(channelName, "", HmValueType.ACTION, value, true, HmParamsetType.VALUES);
        HmChannel hmChannel = new HmChannel(channelName, 1);
        HmDevice device = new HmDevice("ABC12345", HmInterface.RF, "HM-MOCK", "mockid", "mockid", "mockfw");
        hmChannel.setDevice(device);
        device.addChannel(hmChannel);
        hmChannel.addDatapoint(pressDp);
        pressDp.setChannel(hmChannel);
        bvdpHandler.initialize(device);

        return pressDp;
    }

    private HmDatapoint createPressDatapointFrom(HmDatapoint originalDatapoint, String channelName, Object value) {
        HmDatapoint pressDp = new HmDatapoint(channelName, "", HmValueType.ACTION, value, true, HmParamsetType.VALUES);
        HmChannel hmChannel = originalDatapoint.getChannel();
        hmChannel.addDatapoint(pressDp);
        pressDp.setChannel(hmChannel);

        return pressDp;
    }

    private HmDatapoint getButtonVirtualDatapoint(HmDatapoint originalDatapoint) {
        return originalDatapoint.getChannel().getDatapoints().stream()
                .filter(dp -> HomematicConstants.VIRTUAL_DATAPOINT_NAME_BUTTON.equals(dp.getName())).findFirst()
                .orElse(null);
    }

    /**
     * Mock parts of {@linkplain org.openhab.binding.homematic.internal.communicator.AbstractHomematicGateway}
     */
    private class MockEventReceiver {

        public void eventReceived(HmDatapoint dp) throws IOException, HomematicClientException {
            if (bvdpHandler.canHandleEvent(dp)) {
                bvdpHandler.handleEvent(null, dp);
            }
            if (dp.isPressDatapoint() && MiscUtils.isTrueValue(dp.getValue())) {
                disableDatapoint(dp);
            }
        }

        private void disableDatapoint(HmDatapoint dp) {
            new Thread(() -> {
                try {
                    Thread.sleep(DISABLE_DATAPOINT_DELAY);
                    dp.setValue(Boolean.FALSE);
                } catch (InterruptedException e) {
                }
            }).start();
        }
    }
}
