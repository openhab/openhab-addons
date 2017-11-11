/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jenocean.handler;

import static org.openhab.binding.jenocean.JEnOceanBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.polito.elite.enocean.enj.eep.EEPAttribute;
import it.polito.elite.enocean.enj.eep.EEPAttributeChangeListener;
import it.polito.elite.enocean.enj.eep.eep26.attributes.EEP26RockerSwitch2RockerAction;
import it.polito.elite.enocean.enj.eep.eep26.attributes.EEP26RockerSwitch2RockerButtonCount;

/**
 * The {@link JEnOceanHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jan Kemmler - Initial contribution
 */
@NonNullByDefault
public class JEnOceanHandler extends BaseThingHandler implements EEPAttributeChangeListener {

    private final Logger logger = LoggerFactory.getLogger(JEnOceanHandler.class);

    private boolean wasUpPressedA = false;
    private boolean wasDownPressedA = false;
    private boolean wasUpPressedB = false;
    private boolean wasDownPressedB = false;

    public JEnOceanHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_A_ON_OFF)) {
            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    @Override
    public void initialize() {
        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    /*
     * (non-Javadoc)
     *
     * @see it.polito.elite.enocean.enj.eep.EEPAttributeChangeListener#handleAttributeChange(int,
     * it.polito.elite.enocean.enj.eep.EEPAttribute)
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

            // scheduler.scheduleWithFixedDelay(new DimmerIncreaseTask(this.getThing()), 500, 200,
            // TimeUnit.MILLISECONDS);
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
