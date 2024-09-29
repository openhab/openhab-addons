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
package org.openhab.binding.plugwise.internal.handler;

import static org.openhab.binding.plugwise.internal.PlugwiseBindingConstants.CHANNEL_TRIGGERED;
import static org.openhab.core.thing.ThingStatus.*;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.plugwise.internal.protocol.AcknowledgementMessage;
import org.openhab.binding.plugwise.internal.protocol.AnnounceAwakeRequestMessage;
import org.openhab.binding.plugwise.internal.protocol.AnnounceAwakeRequestMessage.AwakeReason;
import org.openhab.binding.plugwise.internal.protocol.BroadcastGroupSwitchResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.InformationResponseMessage;
import org.openhab.binding.plugwise.internal.protocol.Message;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Thing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractPlugwiseThingHandler} handles common Plugwise sleeping end device (SED) channel updates and
 * commands.
 *
 * @author Wouter Born - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractSleepingEndDeviceHandler extends AbstractPlugwiseThingHandler {

    private static final int SED_PROPERTIES_COUNT = 3;

    private final Logger logger = LoggerFactory.getLogger(AbstractSleepingEndDeviceHandler.class);

    protected AbstractSleepingEndDeviceHandler(Thing thing) {
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
        updateState(CHANNEL_TRIGGERED, OnOffType.from(message.getPowerState()));
    }

    protected void handleInformationResponse(InformationResponseMessage message) {
        updateProperties(message);
    }

    @Override
    public void handleResponseMessage(Message message) {
        updateLastSeen();

        switch (message.getType()) {
            case ACKNOWLEDGEMENT_V1, ACKNOWLEDGEMENT_V2:
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
