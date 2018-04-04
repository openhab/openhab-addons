/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.plugwise.handler;

import static org.eclipse.smarthome.core.thing.ThingStatus.*;
import static org.openhab.binding.plugwise.PlugwiseBindingConstants.CHANNEL_TRIGGERED;

import java.time.Duration;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Thing;
import org.openhab.binding.plugwise.internal.protocol.AcknowledgementMessage;
import org.openhab.binding.plugwise.internal.protocol.AnnounceAwakeRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.AnnounceAwakeRequestMessage.AwakeReason;
import org.openhab.binding.plugwise.internal.protocol.BroadcastGroupSwitchResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.InformationResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractPlugwiseThingHandler} handles common Plugwise sleeping end device (SED) channel updates and
 * commands.
 *
 * @author Wouter Born - Initial contribution
 */
public abstract class AbstractSleepingEndDeviceHandler extends AbstractPlugwiseThingHandler {

    private static final int SED_PROPERTIES_COUNT = 3;

    private final Logger logger = LoggerFactory.getLogger(AbstractSleepingEndDeviceHandler.class);

    public AbstractSleepingEndDeviceHandler(Thing thing) {
        super(thing);
    }

    protected abstract Duration getWakeupDuration();

    protected void handleAcknowledgement(AcknowledgementMessage message) {
        updateStatusOnDetailChange();
    }

    protected void handleAnnounceAwakeRequest(AnnounceAwakeRequestMessage message) {
        AwakeReason awakeReason = message.getAwakeReason();
        if (awakeReason == AwakeReason.MAINTENANCE || awakeReason == AwakeReason.WAKEUP_BUTTON
                || editProperties().size() < SED_PROPERTIES_COUNT) {
            updateInformation();
            if (isConfigurationPending() && !recentlySendConfigurationUpdate()) {
                sendConfigurationUpdateCommands();
            }
        }
    }

    protected void handleBroadcastGroupSwitchResponseMessage(BroadcastGroupSwitchResponseMessage message) {
        updateState(CHANNEL_TRIGGERED, message.getPowerState() ? OnOffType.ON : OnOffType.OFF);
    }

    protected void handleInformationResponse(InformationResponseMessage message) {
        updateProperties(message);
    }

    @Override
    public void handleReponseMessage(Message message) {
        updateLastSeen();

        switch (message.getType()) {
            case ACKNOWLEDGEMENT_V1:
            case ACKNOWLEDGEMENT_V2:
                handleAcknowledgement((AcknowledgementMessage) message);
                break;
            case ANNOUNCE_AWAKE_REQUEST:
                handleAnnounceAwakeRequest((AnnounceAwakeRequestMessage) message);
                break;
            case BROADCAST_GROUP_SWITCH_RESPONSE:
                handleBroadcastGroupSwitchResponseMessage((BroadcastGroupSwitchResponseMessage) message);
                break;
            case DEVICE_INFORMATION_RESPONSE:
                handleInformationResponse((InformationResponseMessage) message);
                break;
            default:
                logger.trace("Received unhandled {} message from {} ({})", message.getType(), getDeviceType(),
                        getMACAddress());
                break;
        }
    }

    @Override
    protected boolean shouldOnlineTaskBeScheduled() {
        return thing.getStatus() == ONLINE;
    }

    @Override
    protected void updateOnlineState() {
        if (thing.getStatus() == ONLINE && getWakeupDuration().minus(durationSinceLastSeen()).isNegative()) {
            updateStatus(OFFLINE, getThingStatusDetail());
        }
    }

}
