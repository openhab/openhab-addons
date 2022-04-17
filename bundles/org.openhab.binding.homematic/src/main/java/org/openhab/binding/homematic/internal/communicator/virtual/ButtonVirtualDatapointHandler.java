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
package org.openhab.binding.homematic.internal.communicator.virtual;

import static org.openhab.binding.homematic.internal.misc.HomematicConstants.VIRTUAL_DATAPOINT_NAME_BUTTON;

import java.util.HashSet;

import org.openhab.binding.homematic.internal.misc.MiscUtils;
import org.openhab.binding.homematic.internal.model.HmChannel;
import org.openhab.binding.homematic.internal.model.HmDatapoint;
import org.openhab.binding.homematic.internal.model.HmDevice;
import org.openhab.binding.homematic.internal.model.HmValueType;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.DefaultSystemChannelTypeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A virtual String datapoint which adds a BUTTON datapoint. It will forward key events to the
 * system channel {@link DefaultSystemChannelTypeProvider#SYSTEM_BUTTON}.
 *
 * @author Michael Reitler - Initial contribution
 */
public class ButtonVirtualDatapointHandler extends AbstractVirtualDatapointHandler {
    private final Logger logger = LoggerFactory.getLogger(ButtonVirtualDatapointHandler.class);

    private static final String LONG_REPEATED_EVENT = "LONG_REPEATED";
    private static final String LONG_RELEASED_EVENT = "LONG_RELEASED";

    private HashSet<String> devicesUsingLongStartEvent = new HashSet<>();

    @Override
    public String getName() {
        return VIRTUAL_DATAPOINT_NAME_BUTTON;
    }

    @Override
    public void initialize(HmDevice device) {
        for (HmChannel channel : device.getChannels()) {
            if (channel.hasPressDatapoint()) {
                HmDatapoint dp = addDatapoint(device, channel.getNumber(), getName(), HmValueType.STRING, null, false);
                dp.setTrigger(true);
                dp.setOptions(new String[] { CommonTriggerEvents.SHORT_PRESSED, CommonTriggerEvents.LONG_PRESSED,
                        LONG_REPEATED_EVENT, LONG_RELEASED_EVENT });
            }
        }
    }

    @Override
    public boolean canHandleEvent(HmDatapoint dp) {
        return dp.isPressDatapoint();
    }

    @Override
    public void handleEvent(VirtualGateway gateway, HmDatapoint dp) {
        HmChannel channel = dp.getChannel();
        String deviceSerial = channel.getDevice().getAddress();
        HmDatapoint vdp = getVirtualDatapoint(channel);
        int usPos = dp.getName().indexOf("_");
        String pressType = usPos == -1 ? dp.getName() : dp.getName().substring(usPos + 1);
        boolean isLongPressActive = CommonTriggerEvents.LONG_PRESSED.equals(vdp.getValue())
                || LONG_REPEATED_EVENT.equals(vdp.getValue());
        if (MiscUtils.isTrueValue(dp.getValue())) {
            switch (pressType) {
                case "SHORT": {
                    vdp.setValue(null); // Force sending new event
                    vdp.setValue(CommonTriggerEvents.SHORT_PRESSED);
                    break;
                }
                case "LONG":
                    if (isLongPressActive) {
                        // HM-IP devices do long press repetitions via LONG instead of CONT events,
                        // so clear previous value to force re-triggering of event
                        vdp.setValue(null);
                        vdp.setValue(LONG_REPEATED_EVENT);
                    } else {
                        // HM devices start long press via LONG events
                        vdp.setValue(CommonTriggerEvents.LONG_PRESSED);
                    }
                    break;
                case "LONG_START":
                    vdp.setValue(CommonTriggerEvents.LONG_PRESSED);
                    devicesUsingLongStartEvent.add(deviceSerial);
                    break;
                case "LONG_RELEASE":
                    // Only send release events if we sent a pressed event before
                    vdp.setValue(isLongPressActive ? LONG_RELEASED_EVENT : null);
                    break;
                case "CONT":
                    // Clear previous value to force re-triggering of repetition
                    vdp.setValue(null);
                    // Only send repetitions if there was a pressed event before
                    // (a CONT might arrive simultaneously with the initial LONG event)
                    if (isLongPressActive) {
                        vdp.setValue(LONG_REPEATED_EVENT);
                    }
                    break;
                default:
                    vdp.setValue(null);
                    logger.warn("Unexpected vaule '{}' for PRESS virtual datapoint", pressType);
            }
        } else {
            String usedStartEvent = devicesUsingLongStartEvent.contains(deviceSerial) ? "LONG_START" : "LONG";
            if (usedStartEvent.equals(pressType) && LONG_REPEATED_EVENT.equals(vdp.getValue())) {
                // If we're currently processing a repeated long-press event, don't let the initial LONG
                // event time out the repetitions, the CONT delay handler will take care of it
                vdp.setValue(LONG_REPEATED_EVENT);
            } else if (isLongPressActive) {
                // We seemingly missed an event (either a CONT or the final LONG_RELEASE),
                // so end the long press cycle now
                vdp.setValue(LONG_RELEASED_EVENT);
            } else {
                vdp.setValue(null);
            }
        }
        logger.debug("Handled virtual button event on {}:{}: press type {}, value {}, button state {} -> {}",
                channel.getDevice().getAddress(), channel.getNumber(), pressType, dp.getValue(), vdp.getPreviousValue(),
                vdp.getValue());
    }
}
