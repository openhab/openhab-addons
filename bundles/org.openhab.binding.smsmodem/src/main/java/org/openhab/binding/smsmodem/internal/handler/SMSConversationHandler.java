/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.smsmodem.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.smsmodem.internal.SMSConversationConfiguration;
import org.openhab.binding.smsmodem.internal.SMSModemBindingConstants;
import org.openhab.core.i18n.ConfigurationException;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SMSConversationHandler} is responsible for managing
 * discussion channels.
 *
 * @author Gwendal ROULLEAU - Initial contribution
 */
@NonNullByDefault
public class SMSConversationHandler extends BaseThingHandler {

    public static final ThingTypeUID SUPPORTED_THING_TYPES_UIDS = SMSModemBindingConstants.SMSCONVERSATION_THING_TYPE;

    private final Logger logger = LoggerFactory.getLogger(SMSConversationHandler.class);

    private @Nullable SMSModemBridgeHandler bridgeHandler;

    private SMSConversationConfiguration config;

    public SMSConversationHandler(Thing thing) {
        super(thing);
        this.config = new SMSConversationConfiguration();
    }

    public String getRecipient() {
        return config.recipient.trim();
    }

    private synchronized void checkBridgeHandler() {
        if (this.bridgeHandler == null) {
            Bridge bridge = getBridge();
            if (bridge == null) {
                throw new ConfigurationException("Required bridge not defined for SMSconversation {} with {}.",
                        thing.getUID(), getRecipient());
            }
            ThingHandler handler = bridge.getHandler();
            if (handler instanceof SMSModemBridgeHandler) {
                this.bridgeHandler = (SMSModemBridgeHandler) handler;
            } else {
                throw new ConfigurationException("No available bridge handler found for SMSConversation {} bridge {} .",
                        thing.getUID(), bridge.getUID());
            }
        }
    }

    protected void checkAndReceive(String sender, String text) {
        String conversationRecipient = config.recipient.trim();
        // is the recipient the one handled by this conversation ? :
        if (conversationRecipient.equals(sender)) {
            updateState(SMSModemBindingConstants.CHANNEL_RECEIVED, new StringType(text));
        }
    }

    protected void checkAndUpdateDeliveryStatus(String messageRecipient, DeliveryStatus sentStatus) {
        String conversationRecipient = config.recipient.trim();
        // is the recipient the one handled by this conversation ? :
        if (conversationRecipient.equals(messageRecipient)) {
            updateState(SMSModemBindingConstants.CHANNEL_DELIVERYSTATUS, new StringType(sentStatus.name()));
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }
        if (channelUID.getId().equals(SMSModemBindingConstants.CHANNEL_SEND)) {
            send(command.toString());
            updateState(SMSModemBindingConstants.CHANNEL_SEND, new StringType(command.toString()));
        }
    }

    public void send(String text) {
        SMSModemBridgeHandler bridgeHandlerFinal = bridgeHandler;
        if (bridgeHandlerFinal != null) {
            bridgeHandlerFinal.send(getRecipient(), text, config.deliveryReport, config.encoding);
        } else {
            logger.warn("Only channel 'send' in SMSConversation can receive command");
        }
    }

    @Override
    public void initialize() {
        config = getConfigAs(SMSConversationConfiguration.class);
        try {
            checkBridgeHandler();
            updateStatus(ThingStatus.ONLINE);
        } catch (ConfigurationException confe) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, confe.getMessage());
        }
    }
}
