/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.handler;

import static org.openhab.binding.enocean.EnOceanBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polito.elite.enocean.enj.eep.EEPAttribute;
import it.polito.elite.enocean.enj.eep.EEPAttributeChangeListener;
import it.polito.elite.enocean.enj.eep.eep26.attributes.EEP26RockerSwitch2RockerAction;
import it.polito.elite.enocean.enj.eep.eep26.attributes.EEP26RockerSwitch2RockerButtonCount;

/**
 * The {@link EnOceanRockerSwitchHandler} is responsible for handling commands, which are
 * sent to one of the channels. It also forwards the updates from the device to the items.
 * It currently forwards up_pressd, up_release, down_pressed and down_released events.
 *
 * @author Jan Kemmler - Initial contribution
 */
@NonNullByDefault
public class EnOceanRockerSwitchHandler extends BaseThingHandler implements EEPAttributeChangeListener {

    @SuppressWarnings("null")
    private final Logger logger = LoggerFactory.getLogger(EnOceanRockerSwitchHandler.class);

    private boolean wasUpPressedA = false;
    private boolean wasDownPressedA = false;
    private boolean wasUpPressedB = false;
    private boolean wasDownPressedB = false;

    public EnOceanRockerSwitchHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        // Only called if status ONLINE or OFFLINE
        logger.debug("EnOcean bridge status changed: {}", bridgeStatusInfo);

        if (bridgeStatusInfo.getStatus().equals(ThingStatus.OFFLINE)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge status is offline.");
        } else if (bridgeStatusInfo.getStatus().equals(ThingStatus.ONLINE)) {
            updateStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, "Bridge status is online.");
        }
    }

    /**
     * Gets called by the enj library whenever the device for which this handler is registered has transmitted an event
     * to the binding.
     *
     * @see it.polito.elite.enocean.enj.eep.EEPAttributeChangeListener#handleAttributeChange(int,
     *      it.polito.elite.enocean.enj.eep.EEPAttribute)
     *
     * @param channelId
     *            The EnOcean channel for which this event was posted.
     * @param attribute
     *            The attribute that makes up the event.
     */
    @Override
    public void handleAttributeChange(int channelId, @Nullable EEPAttribute<?> attribute) {
        // cast
        if (attribute instanceof EEP26RockerSwitch2RockerAction) {
            EEP26RockerSwitch2RockerAction action = (EEP26RockerSwitch2RockerAction) attribute;
            boolean upPressedA = action.getButtonValue(EEP26RockerSwitch2RockerAction.AI);
            boolean downPressedA = action.getButtonValue(EEP26RockerSwitch2RockerAction.AO);
            boolean upPressedB = action.getButtonValue(EEP26RockerSwitch2RockerAction.BI);
            boolean downPressedB = action.getButtonValue(EEP26RockerSwitch2RockerAction.BO);

            if (upPressedA) {
                wasDownPressedA = false;
                if (!wasUpPressedA) {
                    wasUpPressedA = true;
                    triggerChannel(CHANNEL_A_UP_BUTTON, "PRESSED");
                    triggerChannel(CHANNEL_A_ROCKER, "UP_PRESSED");
                    updateState(CHANNEL_A_ON_OFF, OnOffType.ON);
                }
            } else if (downPressedA) {
                wasUpPressedA = false;
                if (!wasDownPressedA) {
                    wasDownPressedA = true;
                    triggerChannel(CHANNEL_A_DOWN_BUTTON, "PRESSED");
                    triggerChannel(CHANNEL_A_ROCKER, "DOWN_PRESSED");
                    updateState(CHANNEL_A_ON_OFF, OnOffType.OFF);
                }
            }

            if (upPressedB) {
                wasDownPressedB = false;
                if (!wasUpPressedB) {
                    wasUpPressedB = true;
                    triggerChannel(CHANNEL_B_UP_BUTTON, "PRESSED");
                    triggerChannel(CHANNEL_B_ROCKER, "UP_PRESSED");
                    updateState(CHANNEL_B_ON_OFF, OnOffType.ON);
                }
            } else if (downPressedB) {
                wasUpPressedB = false;
                if (!wasDownPressedB) {
                    wasDownPressedB = true;
                    triggerChannel(CHANNEL_B_DOWN_BUTTON, "PRESSED");
                    triggerChannel(CHANNEL_B_ROCKER, "DOWN_PRESSED");
                    updateState(CHANNEL_B_ON_OFF, OnOffType.OFF);
                }
            }

            this.logger.debug("A0: {} A1: {} B0: {} B1: {}", downPressedA, upPressedA, downPressedB, upPressedB);

        } else if (attribute instanceof EEP26RockerSwitch2RockerButtonCount) {
            if (wasUpPressedA) {
                wasUpPressedA = false;
                triggerChannel(CHANNEL_A_ROCKER, "UP_RELEASED");
            }

            if (wasDownPressedA) {
                wasDownPressedA = false;
                triggerChannel(CHANNEL_A_ROCKER, "DOWN_RELEASED");
            }

            if (wasUpPressedB) {
                wasUpPressedB = false;
                triggerChannel(CHANNEL_B_ROCKER, "UP_RELEASED");
            }

            if (wasDownPressedB) {
                wasDownPressedB = false;
                triggerChannel(CHANNEL_B_ROCKER, "DOWN_RELEASED");
            }
        }

    }

}
